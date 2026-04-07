package com.yueban.compilecook.ui.theme

import androidx.compose.runtime.Composable

object ExtendedTheme {
  val colors: ExtendedColorScheme
    @Composable
    get() = LocalExtendedColorScheme.current
}
