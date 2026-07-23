package com.yueban.compilecook.ui.ai

import io.github.ismoy.imagepickerkmp.domain.models.PhotoResult
import kotlin.io.encoding.Base64

internal actual fun isCameraSupported(): Boolean = false

internal actual suspend fun PhotoResult.loadBytesSuspend(): ByteArray {
  return try {
    val base64Prefix = ";base64,"
    if (uri.startsWith("data:") && uri.contains(base64Prefix)) {
      Base64.decode(uri.substringAfter(base64Prefix))
    } else {
      byteArrayOf()
    }
  } catch (_: Exception) {
    byteArrayOf()
  }
}
