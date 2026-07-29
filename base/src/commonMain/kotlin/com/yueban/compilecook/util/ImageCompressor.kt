package com.yueban.compilecook.util

import com.yueban.compilecook.logger.Logger

const val IMAGE_COMPRESSOR_DEFAULT_MAX_WIDTH = 1024
const val IMAGE_COMPRESSOR_DEFAULT_QUALITY = 80
const val IMAGE_COMPRESSOR_DEFAULT_MAX_FILE_SIZE = 5L * 1024 * 1024
const val IMAGE_COMPRESSOR_MIN_QUALITY = 10
const val IMAGE_COMPRESSOR_MIN_MAX_WIDTH = 320
private const val IMAGE_COMPRESSOR_DIMENSION_REDUCTION_FACTOR = 0.8

expect object ImageCompressor {
  suspend fun compress(imageBytes: ByteArray, maxWidth: Int, quality: Int): ByteArray
  suspend fun getDimensions(imageBytes: ByteArray): ImageDimensions?
}

suspend fun compressAndSave(
  imageBytes: ByteArray,
  maxWidth: Int = IMAGE_COMPRESSOR_DEFAULT_MAX_WIDTH,
  quality: Int = IMAGE_COMPRESSOR_DEFAULT_QUALITY,
  maxFileSize: Long = IMAGE_COMPRESSOR_DEFAULT_MAX_FILE_SIZE,
): String {
  val originalDimensions = ImageCompressor.getDimensions(imageBytes)
  Logger.d(
    "Original image: ${FileSizeFormatter.format(imageBytes.size.toLong())}, " +
      "dimensions: ${originalDimensions?.width}x${originalDimensions?.height}"
  )

  if (imageBytes.size <= maxFileSize) {
    return ImageFileCache.saveToCache(imageBytes)
  }

  var currentMaxWidth = maxWidth
  var currentQuality = quality
  var compressed = ImageCompressor.compress(imageBytes, currentMaxWidth, currentQuality)

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
    compressed = ImageCompressor.compress(imageBytes, currentMaxWidth, currentQuality)
  }

  val compressedDimensions = ImageCompressor.getDimensions(compressed)
  Logger.d(
    "Compressed image: ${FileSizeFormatter.format(compressed.size.toLong())}, " +
      "dimensions: ${compressedDimensions?.width}x${compressedDimensions?.height}"
  )

  return ImageFileCache.saveToCache(compressed)
}
