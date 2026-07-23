package com.yueban.compilecook.ui.ai

import io.github.ismoy.imagepickerkmp.domain.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.domain.models.PhotoResult

internal actual fun isCameraSupported(): Boolean = true
internal actual suspend fun PhotoResult.loadBytesSuspend(): ByteArray = loadBytes()
