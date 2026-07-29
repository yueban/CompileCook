package com.yueban.compilecook.ui.ai

import io.github.ismoy.imagepickerkmp.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.picker.PhotoResult

internal actual fun isCameraSupported(): Boolean = false
internal actual suspend fun PhotoResult.loadBytesSuspend(): ByteArray = loadBytes()
