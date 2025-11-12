package com.yueban.compilecook.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun EdgeToEdgeScreen(
  statusBarColor: Color = MaterialTheme.colorScheme.background,
  fillMaxSize: Boolean = true,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(modifier = if (fillMaxSize) Modifier.fillMaxSize() else Modifier.wrapContentSize()) {
    Box(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .then(if (fillMaxSize) Modifier.fillMaxSize() else Modifier.wrapContentSize())
        .systemBarsPadding()
    ) {
      content()
    }
    // 设置状态栏颜色
    Spacer(
      Modifier
        .windowInsetsTopHeight(WindowInsets.statusBars)
        .fillMaxWidth()
        .background(statusBarColor)
    )
  }
}
