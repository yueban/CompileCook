package com.yueban.compilecook.util

import kotlin.random.Random

/** URI scheme for in-memory cached images, used because wasmJS has no filesystem access. */
const val MEM_CACHE_SCHEME = "mem://"

@Suppress("MagicNumber")
actual object ImageFileCache {
  private val store = mutableMapOf<String, ByteArray>()

  actual suspend fun saveToCache(bytes: ByteArray, prefix: String): String {
    val key = "$MEM_CACHE_SCHEME${prefix}_${Random.nextLong().toULong().toString(16)}"
    store[key] = bytes
    return key
  }

  actual fun readBytes(path: String): ByteArray = store[path] ?: ByteArray(0)

  actual fun delete(path: String) {
    store.remove(path)
  }
}
