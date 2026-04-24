package com.yueban.compilecook.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.font.FontFamily
import coil3.compose.LocalAsyncImagePreviewHandler
import com.yueban.compilecook.ui.util.preview.coilPreviewHandler
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.noto_sans_sc_regular
import io.github.kdroidfilter.platformtools.darkmodedetector.isSystemInDarkMode
import org.jetbrains.compose.resources.Font

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
      typography = DefaultTypography.withCustomFont(),
      shapes = DefaultShapes,
      content = content
    )
  }
}

@Composable
fun Typography.withCustomFont(): Typography {
  // TODO: temp solution for CJK characters display in web browser, browser font should be officially supported by KMP
  val notoSansSC = FontFamily(Font(Res.font.noto_sans_sc_regular))
  return copy(
    displayLarge = displayLarge.copy(fontFamily = notoSansSC),
    displayMedium = displayMedium.copy(fontFamily = notoSansSC),
    displaySmall = displaySmall.copy(fontFamily = notoSansSC),

    headlineLarge = headlineLarge.copy(fontFamily = notoSansSC),
    headlineMedium = headlineMedium.copy(fontFamily = notoSansSC),
    headlineSmall = headlineSmall.copy(fontFamily = notoSansSC),

    titleLarge = titleLarge.copy(fontFamily = notoSansSC),
    titleMedium = titleMedium.copy(fontFamily = notoSansSC),
    titleSmall = titleSmall.copy(fontFamily = notoSansSC),

    bodyLarge = bodyLarge.copy(fontFamily = notoSansSC),
    bodyMedium = bodyMedium.copy(fontFamily = notoSansSC),
    bodySmall = bodySmall.copy(fontFamily = notoSansSC),

    labelLarge = labelLarge.copy(fontFamily = notoSansSC),
    labelMedium = labelMedium.copy(fontFamily = notoSansSC),
    labelSmall = labelSmall.copy(fontFamily = notoSansSC)
  )
}
