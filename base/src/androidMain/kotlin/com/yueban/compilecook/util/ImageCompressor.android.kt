package com.yueban.compilecook.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

actual object ImageCompressor {
  actual suspend fun compressAndSave(imageBytes: ByteArray, maxWidth: Int, quality: Int): String {
    val compressed = compress(imageBytes, maxWidth, quality)
    return ImageFileCache.saveToCache(compressed)
  }

  private fun compress(imageBytes: ByteArray, maxWidth: Int, quality: Int): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
      ?: return imageBytes

    val scaled = if (bitmap.width > maxWidth) {
      val ratio = maxWidth.toFloat() / bitmap.width
      val newHeight = (bitmap.height * ratio).toInt()
      bitmap.scale(maxWidth, newHeight, true)
    } else {
      bitmap
    }

    val output = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
    if (scaled !== bitmap) scaled.recycle()
    bitmap.recycle()
    return output.toByteArray()
  }
}
