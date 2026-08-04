package com.yueban.compilecook.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object ImageCompressor {
  actual suspend fun compress(source: ImageSource, maxWidth: Int, quality: Int): ByteArray {
    val image = decodeImage(source)?.takeIf { it.size.useContents { width > 0.0 } }
      ?: return fallbackBytes(source)

    val originalWidth = image.size.useContents { width }
    val targetWidth = minOf(originalWidth, maxWidth.toDouble())
    val targetHeight = image.size.useContents { height } * (targetWidth / originalWidth)

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return resized?.let { UIImageJPEGRepresentation(it, quality / 100.0)?.toByteArray() } ?: fallbackBytes(source)
  }

  actual suspend fun getDimensions(source: ImageSource): ImageDimensions? {
    val image = decodeImage(source) ?: return null
    val width = image.size.useContents { width }
    val height = image.size.useContents { height }
    return if (width > 0 && height > 0) {
      ImageDimensions(width.toInt(), height.toInt())
    } else {
      null
    }
  }

  actual suspend fun getFileSize(source: ImageSource): Long? = when (source) {
    is ImageSource.Bytes -> source.bytes.size.toLong()
    is ImageSource.Path -> {
      val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(source.path, error = null)
      (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue
    }
  }

  private fun decodeImage(source: ImageSource): UIImage? = when (source) {
    is ImageSource.Path -> UIImage(contentsOfFile = source.path)
    is ImageSource.Bytes -> UIImage(data = source.bytes.toNSData())
  }

  private fun fallbackBytes(source: ImageSource): ByteArray = when (source) {
    is ImageSource.Bytes -> source.bytes
    is ImageSource.Path -> NSData.dataWithContentsOfFile(source.path)?.toByteArray() ?: ByteArray(0)
  }

  private fun ByteArray.toNSData(): NSData = NSData.create(
    bytes = this.usePinned { it.addressOf(0) },
    length = this.size.toULong(),
  )

  private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
      bytes.usePinned { memcpy(it.addressOf(0), this.bytes, this.length) }
    }
    return bytes
  }
}
