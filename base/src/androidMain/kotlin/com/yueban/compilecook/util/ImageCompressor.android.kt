package com.yueban.compilecook.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

actual object ImageCompressor {
  // reuse the already-decoded bitmap without re-decoding from the original source).
  private var cacheSource: ImageSource? = null
  private var cacheMaxWidth: Int = -1
  private var cacheBitmap: Bitmap? = null

  actual suspend fun compress(
    source: ImageSource,
    maxWidth: Int,
    quality: Int,
  ): ByteArray = withContext(Dispatchers.IO) {
    val bitmap = getOrDecodeBitmap(source, maxWidth) ?: return@withContext fallbackBytes(source)

    val output = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
    output.toByteArray()
  }

  actual suspend fun getDimensions(source: ImageSource): ImageDimensions? = withContext(Dispatchers.IO) {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    decodeBounds(source, opts)
    if (opts.outWidth > 0 && opts.outHeight > 0) {
      ImageDimensions(opts.outWidth, opts.outHeight)
    } else {
      null
    }
  }

  actual suspend fun getFileSize(source: ImageSource): Long? = when (source) {
    is ImageSource.Bytes -> source.bytes.size.toLong()
    is ImageSource.Path -> File(source.path).takeIf { it.exists() }?.length()
  }

  private fun getOrDecodeBitmap(source: ImageSource, maxWidth: Int): Bitmap? {
    // Reuse cached bitmap when the same source and maxWidth (quality-only iteration)
    if (cacheSource == source && cacheMaxWidth == maxWidth) {
      return cacheBitmap
    }

    cacheSource = source
    cacheMaxWidth = maxWidth
    cacheBitmap?.recycle()
    decodeBitmap(source, maxWidth).also { cacheBitmap = it }
    return cacheBitmap
  }

  private fun decodeBitmap(source: ImageSource, maxWidth: Int): Bitmap? {
    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    decodeBounds(source, opts)

    val decodeOpts = BitmapFactory.Options().apply {
      // Downsample during decode so we never load the full-resolution bitmap.
      // Target decoded width ≤ 2x maxWidth (~12MB at 2048px) instead of
      // loading a 48MP photo (~200MB) and scaling after the fact.
      inSampleSize = if (opts.outWidth > 0) calculateInSampleSize(opts.outWidth, maxWidth) else 1
    }

    val bitmap = decodeBitmap(source, decodeOpts) ?: return null

    return if (bitmap.width <= maxWidth) {
      bitmap
    } else {
      val ratio = maxWidth.toFloat() / bitmap.width
      val newHeight = (bitmap.height * ratio).toInt()
      bitmap.scale(maxWidth, newHeight, true).also { bitmap.recycle() }
    }
  }

  private fun decodeBounds(source: ImageSource, opts: BitmapFactory.Options) {
    when (source) {
      is ImageSource.Bytes -> BitmapFactory.decodeByteArray(source.bytes, 0, source.bytes.size, opts)
      is ImageSource.Path -> BitmapFactory.decodeFile(source.path, opts)
    }
  }

  private fun decodeBitmap(source: ImageSource, opts: BitmapFactory.Options): Bitmap? = when (source) {
    is ImageSource.Bytes -> BitmapFactory.decodeByteArray(source.bytes, 0, source.bytes.size, opts)
    is ImageSource.Path -> BitmapFactory.decodeFile(source.path, opts)
  }

  private fun fallbackBytes(source: ImageSource): ByteArray = when (source) {
    is ImageSource.Bytes -> source.bytes
    is ImageSource.Path -> File(source.path).readBytes()
  }

  private fun calculateInSampleSize(rawWidth: Int, maxWidth: Int): Int {
    var inSampleSize = 1
    while (rawWidth / inSampleSize > maxWidth * 2) {
      inSampleSize *= 2
    }
    return inSampleSize
  }
}
