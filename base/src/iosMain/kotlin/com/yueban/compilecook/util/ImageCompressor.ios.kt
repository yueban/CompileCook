package com.yueban.compilecook.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object ImageCompressor {
  actual suspend fun compressAndSave(imageBytes: ByteArray, maxWidth: Int, quality: Int): String {
    val compressed = compress(imageBytes, maxWidth, quality)
    return ImageFileCache.saveToCache(compressed)
  }

  private fun compress(imageBytes: ByteArray, maxWidth: Int, quality: Int): ByteArray {
    val data = imageBytes.toNSData()
    val image = UIImage(data = data)
    val originalWidth = image.size.useContents { width }

    if (originalWidth <= 0.0) return imageBytes

    val targetWidth = minOf(originalWidth, maxWidth.toDouble())
    val targetHeight = image.size.useContents { height } * (targetWidth / originalWidth)

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return resized?.let { UIImageJPEGRepresentation(it, quality / 100.0)?.toByteArray() } ?: imageBytes
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
