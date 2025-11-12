package com.yueban.compilecook.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun PlatformSpecificThemeEffects(darkTheme: Boolean) {
  // 设置状态栏颜色
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
      }
      // 设置内容全屏显示，自己处理 insets
      WindowCompat.setDecorFitsSystemWindows(window, false)
    }
  }
}
