package com.yueban.compilecook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontFamily
import coil3.compose.LocalAsyncImagePreviewHandler
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.noto_sans_sc_regular
import org.jetbrains.compose.resources.Font

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  PlatformSpecificThemeEffects(darkTheme)

  val extendedColorScheme = extendedColorScheme(darkTheme)
  val extendedTypography = extendedTypography()

  CompositionLocalProvider(
    LocalExtendedColorScheme provides extendedColorScheme,
    LocalExtendedTypography provides extendedTypography,
    LocalAsyncImagePreviewHandler provides coilPreviewHandler,
  ) {
    MaterialTheme(
      colorScheme = extendedColorScheme.colorScheme,
      typography = extendedTypography.typography.withCustomFont(),
      content = content
    )
  }
}

@Composable
fun Typography.withCustomFont(): Typography {
  // TODO: temporary solution for CJK characters display in web browser
  // TODO: browser font should be officially supported by KMP
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
