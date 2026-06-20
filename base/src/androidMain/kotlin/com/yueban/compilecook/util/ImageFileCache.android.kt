package com.yueban.compilecook.util

import android.content.Context
import java.io.File
import java.util.UUID

actual object ImageFileCache {
  @Volatile
  private lateinit var cacheDir: File

  fun init(context: Context) {
    cacheDir = File(context.cacheDir, "ai_chat_images").apply { mkdirs() }
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
