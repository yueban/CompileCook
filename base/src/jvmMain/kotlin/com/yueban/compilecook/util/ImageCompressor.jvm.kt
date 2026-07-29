package com.yueban.compilecook.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual object ImageCompressor {
  actual suspend fun compress(
    imageBytes: ByteArray,
    maxWidth: Int,
    quality: Int,
  ): ByteArray = withContext(Dispatchers.IO) {
    val original = ImageIO.read(ByteArrayInputStream(imageBytes)) ?: return@withContext imageBytes

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

  actual suspend fun getDimensions(imageBytes: ByteArray): ImageDimensions? = withContext(Dispatchers.IO) {
    val image = ImageIO.read(ByteArrayInputStream(imageBytes)) ?: return@withContext null
    ImageDimensions(image.width, image.height)
  }
}
