package com.yueban.compilecook.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual object ImageCompressor {
  // Cache the downsampled bitmap across multi-pass iterations (quality-only changes
  // reuse the already-decoded bitmap without re-decoding from the original bytes).
  private var cacheImageBytes: ByteArray? = null
  private var cacheMaxWidth: Int = -1
  private var cacheBitmap: Bitmap? = null

  actual suspend fun compress(
    imageBytes: ByteArray,
    maxWidth: Int,
    quality: Int,
  ): ByteArray = withContext(Dispatchers.IO) {
    val bitmap = getOrDecodeBitmap(imageBytes, maxWidth)

    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
    output.toByteArray()
  }

  actual suspend fun getDimensions(imageBytes: ByteArray): ImageDimensions? = withContext(Dispatchers.IO) {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, opts)
    if (opts.outWidth > 0 && opts.outHeight > 0) {
      ImageDimensions(opts.outWidth, opts.outHeight)
    } else {
      null
    }
  }

  private fun getOrDecodeBitmap(imageBytes: ByteArray, maxWidth: Int): Bitmap {
    // Reuse cached bitmap when the same bytes and maxWidth (quality-only iteration)
    if (cacheImageBytes === imageBytes && cacheMaxWidth == maxWidth) {
      return cacheBitmap ?: decodeBitmap(imageBytes, maxWidth)
    }

    cacheImageBytes = imageBytes
    cacheMaxWidth = maxWidth
    cacheBitmap?.recycle()
    decodeBitmap(imageBytes, maxWidth).also { cacheBitmap = it }
    return cacheBitmap!!
  }

  private fun decodeBitmap(imageBytes: ByteArray, maxWidth: Int): Bitmap {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, opts)

    val decodeOpts = BitmapFactory.Options().apply {
      // Downsample during decode so we never load the full-resolution bitmap.
      // Target decoded width ≤ 2x maxWidth (~12MB at 2048px) instead of
      // loading a 48MP photo (~200MB) and scaling after the fact.
      inSampleSize = calculateInSampleSize(opts.outWidth, maxWidth)
    }

    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOpts)
      ?: return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    return if (bitmap.width <= maxWidth) {
      bitmap
    } else {
      val ratio = maxWidth.toFloat() / bitmap.width
      val newHeight = (bitmap.height * ratio).toInt()
      bitmap.scale(maxWidth, newHeight, true).also { bitmap.recycle() }
    }
  }

  @Suppress("MagicNumber")
  private fun calculateInSampleSize(rawWidth: Int, maxWidth: Int): Int {
    var inSampleSize = 1
    while (rawWidth / inSampleSize > maxWidth * 2) {
      inSampleSize *= 2
    }
    return inSampleSize
  }
}
