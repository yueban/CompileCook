package com.yueban.compilecook.ui.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.yueban.compilecook.ui.theme.AppTheme

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      content = content
    )
  }
}
