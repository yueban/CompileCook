package com.yueban.compilecook.util

actual object ImageCompressor {
  actual suspend fun compressAndSave(imageBytes: ByteArray, maxWidth: Int, quality: Int): String {
    // wasmJS image compression requires async canvas operations.
    // Save as-is for now — the in-memory Map handles storage.
    return ImageFileCache.saveToCache(imageBytes)
  }
}
