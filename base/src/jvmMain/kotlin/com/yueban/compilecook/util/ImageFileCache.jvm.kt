package com.yueban.compilecook.util

import java.io.File
import java.util.UUID

actual object ImageFileCache {
  private val cacheDir: File by lazy {
    File(FileUtils.getUserCacheDir(), "ai_chat_images").apply { mkdirs() }
  }

  actual suspend fun saveToCache(bytes: ByteArray, prefix: String): String {
    val file = File(cacheDir, "${prefix}_${UUID.randomUUID()}.jpg")
    file.writeBytes(bytes)
    return file.absolutePath
  }

  actual fun readBytes(path: String): ByteArray {
    val file = File(path)
    return if (file.exists()) file.readBytes() else ByteArray(0)
  }

  actual fun delete(path: String) {
    val file = File(path)
    if (file.exists()) file.delete()
  }
}
