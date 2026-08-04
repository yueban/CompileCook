package com.yueban.compilecook.ui.ai

import com.yueban.compilecook.util.ImageSource
import io.github.ismoy.imagepickerkmp.extensions.absolutePath
import io.github.ismoy.imagepickerkmp.picker.PhotoResult

internal actual fun isCameraSupported(): Boolean = false

internal actual suspend fun PhotoResult.toImageSource(): ImageSource? = runCatching {
  ImageSource.Path(absolutePath)
}.getOrNull()?.takeIf { it.path.isNotBlank() }
