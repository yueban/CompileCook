package com.yueban.compilecook.ui.util

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.yueban.compilecook.ui.theme.AppTheme

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
  AppTheme {
    Surface(
      color = AppTheme.colorScheme.background,
      content = content
    )
  }
}
