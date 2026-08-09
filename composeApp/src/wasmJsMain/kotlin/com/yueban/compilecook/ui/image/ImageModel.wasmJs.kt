package com.yueban.compilecook.ui.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.yueban.compilecook.util.ImageFileStore
import com.yueban.compilecook.util.MEM_CACHE_SCHEME
import kotlin.io.encoding.Base64

private fun ByteArray.startsWithAt(offset: Int, vararg signature: Int): Boolean {
  if (size < offset + signature.size) return false
  return signature.indices.all { this[offset + it] == signature[it].toByte() }
}

@Suppress("MagicNumber")
private fun detectMimeType(bytes: ByteArray): String = when {
  bytes.startsWithAt(0, 0xFF, 0xD8, 0xFF) -> "image/jpeg"
  bytes.startsWithAt(0, 0x89, 0x50, 0x4E, 0x47) -> "image/png"
  bytes.startsWithAt(0, 0x47, 0x49, 0x46, 0x38) -> "image/gif"
  bytes.startsWithAt(0, 0x52, 0x49, 0x46, 0x46) &&
    bytes.startsWithAt(8, 0x57, 0x45, 0x42, 0x50) -> "image/webp"
  bytes.startsWithAt(0, 0x42, 0x4D) -> "image/bmp"
  bytes.startsWithAt(0, 0x49, 0x49, 0x2A, 0x00) -> "image/tiff"
  bytes.startsWithAt(0, 0x4D, 0x4D, 0x00, 0x2A) -> "image/tiff"
  else -> "image/jpeg"
}

private fun bytesToDataUri(bytes: ByteArray): String =
  "data:${detectMimeType(bytes)};base64,${Base64.encode(bytes)}"

@Composable
internal actual fun rememberImageModel(imageRef: String): Any? {
  if (!imageRef.startsWith(MEM_CACHE_SCHEME)) return imageRef

  return remember(imageRef) {
    ImageFileStore.readBytes(imageRef)
      .takeIf { it.isNotEmpty() }
      ?.let(::bytesToDataUri)
  }
}
