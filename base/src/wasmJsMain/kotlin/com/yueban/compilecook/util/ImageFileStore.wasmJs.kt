package com.yueban.compilecook.util

import kotlin.random.Random

/** URI scheme for in-memory cached images, used because wasmJS has no filesystem access. */
const val MEM_CACHE_SCHEME = "mem://"

@Suppress("MagicNumber")
actual object ImageFileStore {
  private val store = mutableMapOf<String, ByteArray>()

  actual suspend fun save(bytes: ByteArray, prefix: String): String {
    val key = "$MEM_CACHE_SCHEME${prefix}_${Random.nextLong().toULong().toString(16)}"
    store[key] = bytes
    return key
  }

  actual suspend fun saveFromPath(path: String, prefix: String): String {
    // WasmJS has no filesystem — image sources are always [ImageSource.Bytes].
    error("saveFromPath is not supported on WasmJS")
  }

  actual fun resolve(imageRef: String): String = imageRef

  actual fun readBytes(imageRef: String): ByteArray = store[imageRef] ?: ByteArray(0)

  actual fun delete(imageRef: String) {
    store.remove(imageRef)
  }
}
