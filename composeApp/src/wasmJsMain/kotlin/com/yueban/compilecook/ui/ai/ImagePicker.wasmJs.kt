package com.yueban.compilecook.ui.ai

import com.yueban.compilecook.util.ImageSource
import io.github.ismoy.imagepickerkmp.picker.PhotoResult
import kotlin.io.encoding.Base64

internal actual fun isCameraSupported(): Boolean = false

internal actual suspend fun PhotoResult.toImageSource(): ImageSource? {
  // WasmJS has no filesystem — the picker returns a data: URI, so bytes are unavoidable.
  val base64Prefix = ";base64,"
  return if (uri.startsWith("data:") && uri.contains(base64Prefix)) {
    ImageSource.Bytes(Base64.decode(uri.substringAfter(base64Prefix)))
  } else {
    null
  }
}
