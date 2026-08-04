package com.yueban.compilecook.util

expect object ImageFileCache {
  suspend fun saveToCache(bytes: ByteArray, prefix: String = "img"): String

  /** Copies the file at [path] into the cache, avoiding a full in-memory byte load. */
  suspend fun saveToCacheFromPath(path: String, prefix: String = "img"): String

  fun readBytes(path: String): ByteArray
  fun delete(path: String)
}
