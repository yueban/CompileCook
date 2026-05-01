package com.yueban.compilecook.ui.ai

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import java.awt.Cursor

actual fun Modifier.dragResizeCursor(): Modifier =
  this.pointerHoverIcon(PointerIcon(Cursor(Cursor.W_RESIZE_CURSOR)))
