package com.yueban.compilecook.util

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual object ImageCompressor {
  actual suspend fun compressAndSave(imageBytes: ByteArray, maxWidth: Int, quality: Int): String {
    val compressed = compress(imageBytes, maxWidth, quality)
    return ImageFileCache.saveToCache(compressed)
  }

  private fun compress(imageBytes: ByteArray, maxWidth: Int, quality: Int): ByteArray {
    val original = ImageIO.read(ByteArrayInputStream(imageBytes)) ?: return imageBytes

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
    return output.toByteArray()
  }
}
