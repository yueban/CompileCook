package com.yueban.compilecook.util

const val LOCAL_IMAGE_SCHEME = "local://"

expect object ImageFileStore {
  suspend fun save(bytes: ByteArray, prefix: String = "img"): String

  /** Copies the file at [path] into local image storage. */
  suspend fun saveFromPath(path: String, prefix: String = "img"): String

  fun resolve(imageRef: String): String
  fun readBytes(imageRef: String): ByteArray
  fun delete(imageRef: String)
}
