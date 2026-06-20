@file:Suppress("UnusedPrivateProperty")

package com.yueban.compilecook.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object ImageFileCache {
  private val cacheDir: String by lazy {
    val base = NSFileManager.defaultManager.URLForDirectory(
      NSCachesDirectory,
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

  actual suspend fun saveToCache(bytes: ByteArray, prefix: String): String {
    val fileName = "${prefix}_${NSUUID.UUID().UUIDString}.jpg"
    val path = "$cacheDir/$fileName"
    val data = bytes.toNSData()
    data.writeToFile(path, true)
    return path
  }

  actual fun readBytes(path: String): ByteArray {
    val data = platform.Foundation.NSData.dataWithContentsOfFile(path) ?: return ByteArray(0)
    return data.toByteArray()
  }

  actual fun delete(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, error = null)
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
