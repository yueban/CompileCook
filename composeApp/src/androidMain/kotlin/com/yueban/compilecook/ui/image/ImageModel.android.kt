package com.yueban.compilecook.ui.image

import androidx.compose.runtime.Composable
import com.yueban.compilecook.util.ImageFileStore

@Composable
internal actual fun rememberImageModel(imageRef: String): Any? = ImageFileStore.resolve(imageRef)
