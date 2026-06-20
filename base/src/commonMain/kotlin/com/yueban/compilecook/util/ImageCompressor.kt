package com.yueban.compilecook.util

expect object ImageCompressor {
  suspend fun compressAndSave(imageBytes: ByteArray, maxWidth: Int = 1024, quality: Int = 80): String
}
