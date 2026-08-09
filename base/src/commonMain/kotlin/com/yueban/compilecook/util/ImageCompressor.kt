package com.yueban.compilecook.util

import com.yueban.compilecook.logger.Logger

const val IMAGE_COMPRESSOR_DEFAULT_MAX_WIDTH = 1024
const val IMAGE_COMPRESSOR_DEFAULT_QUALITY = 80
const val IMAGE_COMPRESSOR_DEFAULT_MAX_FILE_SIZE = 5L * 1024 * 1024
const val IMAGE_COMPRESSOR_MIN_QUALITY = 10
const val IMAGE_COMPRESSOR_MIN_MAX_WIDTH = 320
private const val IMAGE_COMPRESSOR_DIMENSION_REDUCTION_FACTOR = 0.8

/** Input for image compression. Path avoids loading the full image bytes into memory. */
sealed interface ImageSource {
  // Intentionally NOT a data class: ByteArray equality is referential and the
  // multi-pass compression cache relies on instance identity.
  @Suppress("UseDataClass")
  class Bytes(val bytes: ByteArray) : ImageSource

  /** Real file path on disk (WasmJS uses [Bytes] only — no filesystem). */
  data class Path(val path: String) : ImageSource
}

expect object ImageCompressor {
  suspend fun compress(source: ImageSource, maxWidth: Int, quality: Int): ByteArray
  suspend fun getDimensions(source: ImageSource): ImageDimensions?
  suspend fun getFileSize(source: ImageSource): Long?
}

suspend fun compressAndSave(
  source: ImageSource,
  maxWidth: Int = IMAGE_COMPRESSOR_DEFAULT_MAX_WIDTH,
  quality: Int = IMAGE_COMPRESSOR_DEFAULT_QUALITY,
  maxFileSize: Long = IMAGE_COMPRESSOR_DEFAULT_MAX_FILE_SIZE,
): String {
  val originalSize = ImageCompressor.getFileSize(source)
  val originalDimensions = ImageCompressor.getDimensions(source)
  Logger.d(
    "Original image: ${FileSizeFormatter.format(originalSize ?: 0L)}, " +
      "dimensions: ${originalDimensions?.width}x${originalDimensions?.height}"
  )

  if (originalSize != null && originalSize <= maxFileSize) {
    return when (source) {
      is ImageSource.Bytes -> ImageFileStore.save(source.bytes)
      is ImageSource.Path -> ImageFileStore.saveFromPath(source.path)
    }
  }

  var currentMaxWidth = maxWidth
  var currentQuality = quality
  var compressed = ImageCompressor.compress(source, currentMaxWidth, currentQuality)

  while (compressed.size > maxFileSize) {
    when {
      currentQuality > IMAGE_COMPRESSOR_MIN_QUALITY -> {
        currentQuality /= 2
      }
      currentMaxWidth > IMAGE_COMPRESSOR_MIN_MAX_WIDTH -> {
        currentMaxWidth = (currentMaxWidth * IMAGE_COMPRESSOR_DIMENSION_REDUCTION_FACTOR)
          .toInt()
          .coerceAtLeast(IMAGE_COMPRESSOR_MIN_MAX_WIDTH)
        currentQuality = quality
      }
      else -> break
    }
    compressed = ImageCompressor.compress(source, currentMaxWidth, currentQuality)
  }

  val compressedDimensions = ImageCompressor.getDimensions(ImageSource.Bytes(compressed))
  Logger.d(
    "Compressed image: ${FileSizeFormatter.format(compressed.size.toLong())}, " +
      "dimensions: ${compressedDimensions?.width}x${compressedDimensions?.height}"
  )

  return ImageFileStore.save(compressed)
}
