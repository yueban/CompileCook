package com.yueban.compilecook.util

expect object ImageFileCache {
  suspend fun saveToCache(bytes: ByteArray, prefix: String = "img"): String
  fun readBytes(path: String): ByteArray
  fun delete(path: String)
}
