package com.yueban.compilecook.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual object ImageCompressor {
  actual suspend fun compress(
    source: ImageSource,
    maxWidth: Int,
    quality: Int,
  ): ByteArray = withContext(Dispatchers.IO) {
    val original = readImage(source) ?: return@withContext readFallbackBytes(source)

    val targetWidth = minOf(original.width, maxWidth)
    val targetHeight = (original.height.toLong() * targetWidth / original.width).toInt()

    val scaled = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = scaled.createGraphics()
    graphics.drawImage(original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null)
    graphics.dispose()

    val output = ByteArrayOutputStream()
    val writer = ImageIO.getImageWritersByFormatName("jpg").next()
    val param = writer.defaultWriteParam.apply {
      compressionMode = ImageWriteParam.MODE_EXPLICIT
      compressionQuality = quality / 100f
    }
    writer.output = ImageIO.createImageOutputStream(output)
    writer.write(null, javax.imageio.IIOImage(scaled, null, null), param)
    writer.dispose()
    output.toByteArray()
  }

  actual suspend fun getDimensions(source: ImageSource): ImageDimensions? = withContext(Dispatchers.IO) {
    val image = readImage(source) ?: return@withContext null
    ImageDimensions(image.width, image.height)
  }

  actual suspend fun getFileSize(source: ImageSource): Long? = when (source) {
    is ImageSource.Bytes -> source.bytes.size.toLong()
    is ImageSource.Path -> File(source.path).takeIf { it.exists() }?.length()
  }

  private fun readImage(source: ImageSource): BufferedImage? = when (source) {
    is ImageSource.Path -> ImageIO.read(File(source.path))
    is ImageSource.Bytes -> ImageIO.read(ByteArrayInputStream(source.bytes))
  }

  private fun readFallbackBytes(source: ImageSource): ByteArray = when (source) {
    is ImageSource.Bytes -> source.bytes
    is ImageSource.Path -> File(source.path).readBytes()
  }
}
