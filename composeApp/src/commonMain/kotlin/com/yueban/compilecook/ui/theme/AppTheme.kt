package com.yueban.compilecook.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import coil3.compose.LocalAsyncImagePreviewHandler
import com.yueban.compilecook.ui.util.preview.coilPreviewHandler
import io.github.kdroidfilter.platformtools.darkmodedetector.isSystemInDarkMode

object AppTheme {
  val colors: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current

  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography

  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes

  val dimens: ExtendedDimens
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedDimens.current
}

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkMode(),
  content: @Composable () -> Unit,
) {
  PlatformSpecificThemeEffects(darkTheme)

  val extendedColorScheme = extendedColorScheme(darkTheme)

  CompositionLocalProvider(
    LocalExtendedColorScheme provides extendedColorScheme,
    LocalExtendedDimens provides ExtendedDimens(),
    LocalAsyncImagePreviewHandler provides coilPreviewHandler,
  ) {
    MaterialTheme(
      colorScheme = extendedColorScheme.colorScheme,
      typography = DefaultTypography,
      shapes = DefaultShapes,
      content = content
    )
  }
}
