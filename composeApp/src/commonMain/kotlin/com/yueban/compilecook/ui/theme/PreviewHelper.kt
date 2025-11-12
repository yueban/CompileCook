package com.yueban.compilecook.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import coil3.ColorImage
import coil3.compose.AsyncImagePreviewHandler

val coilPreviewHandler = AsyncImagePreviewHandler {
  ColorImage(Color.Red.toArgb())
}
