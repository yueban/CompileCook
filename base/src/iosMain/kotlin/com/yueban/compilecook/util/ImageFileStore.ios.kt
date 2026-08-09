@file:Suppress("UnusedPrivateProperty")

package com.yueban.compilecook.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object ImageFileStore {
  private val storageDir: String by lazy {
    val base = NSFileManager.defaultManager.URLForDirectory(
      NSApplicationSupportDirectory,
      NSUserDomainMask,
      null,
      true,
      null,
    )!!.path!!
    val dir = "$base/ai_chat_images"
    NSFileManager.defaultManager.createDirectoryAtPath(
      dir,
      withIntermediateDirectories = true,
      attributes = null,
      error = null,
    )
    dir
  }

  actual suspend fun save(bytes: ByteArray, prefix: String): String {
    val fileName = "${prefix}_${NSUUID.UUID().UUIDString}.jpg"
    val path = "$storageDir/$fileName"
    val data = bytes.toNSData()
    data.writeToFile(path, true)
    return "$LOCAL_IMAGE_SCHEME$fileName"
  }

  actual suspend fun saveFromPath(path: String, prefix: String): String {
    val fileName = "${prefix}_${NSUUID.UUID().UUIDString}.jpg"
    val dest = "$storageDir/$fileName"
    NSFileManager.defaultManager.copyItemAtPath(path, toPath = dest, error = null)
    return "$LOCAL_IMAGE_SCHEME$fileName"
  }

  actual fun resolve(imageRef: String): String = pathFor(imageRef)

  actual fun readBytes(imageRef: String): ByteArray {
    val data = platform.Foundation.NSData.dataWithContentsOfFile(pathFor(imageRef)) ?: return ByteArray(0)
    return data.toByteArray()
  }

  actual fun delete(imageRef: String) {
    NSFileManager.defaultManager.removeItemAtPath(pathFor(imageRef), error = null)
  }

  private fun pathFor(imageRef: String): String = if (imageRef.startsWith(LOCAL_IMAGE_SCHEME)) {
    "$storageDir/${imageRef.removePrefix(LOCAL_IMAGE_SCHEME)}"
  } else {
    imageRef
  }

  private fun ByteArray.toNSData(): platform.Foundation.NSData = platform.Foundation.NSData.create(
    bytes = this.usePinned { it.addressOf(0) },
    length = this.size.toULong(),
  )

  private fun platform.Foundation.NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
      bytes.usePinned { memcpy(it.addressOf(0), this.bytes, this.length) }
    }
    return bytes
  }
}
