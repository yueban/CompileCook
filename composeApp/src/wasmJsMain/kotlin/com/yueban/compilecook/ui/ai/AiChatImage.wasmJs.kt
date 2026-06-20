package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.yueban.compilecook.util.ImageFileCache
import com.yueban.compilecook.util.MEM_CACHE_SCHEME
import kotlin.io.encoding.Base64

private fun ByteArray.startsWithAt(offset: Int, vararg sig: Int): Boolean {
  if (size < offset + sig.size) return false
  return sig.indices.all { this[offset + it] == sig[it].toByte() }
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

private fun bytesToDataUri(bytes: ByteArray): String {
  return "data:${detectMimeType(bytes)};base64,${Base64.encode(bytes)}"
}

@Composable
actual fun AiChatImage(
  path: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
) {
  if (path.startsWith(MEM_CACHE_SCHEME)) {
    val dataUri = remember(path) {
      val bytes = ImageFileCache.readBytes(path)
      if (bytes.isNotEmpty()) bytesToDataUri(bytes) else null
    }
    dataUri?.let { uri ->
      AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
      )
    }
  } else {
    AsyncImage(
      model = path,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  }
}
