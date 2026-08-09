package com.yueban.compilecook.ui.image

import androidx.compose.runtime.Composable

/** Resolves a local image reference into a model that Coil can load on the current platform. */
@Composable
internal expect fun rememberImageModel(imageRef: String): Any?
