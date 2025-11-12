@file:Suppress("MagicNumber")

package com.yueban.compilecook.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object ExtendedTheme {
  val colors: ExtendedColorScheme
    @Composable
    get() = LocalExtendedColorScheme.current

  val typos: ExtendedTypography
    @Composable
    get() = LocalExtendedTypography.current
}

internal fun extendedColorScheme(darkTheme: Boolean) =
  if (darkTheme) DarkColorScheme else LightColorScheme

internal val LocalExtendedColorScheme = staticCompositionLocalOf<ExtendedColorScheme> {
  error("no extended color scheme specified")
}

@Stable
data class ExtendedColorScheme(
  val colorScheme: ColorScheme,
  val titleText: Color,
  val subTitleText: Color,
  val divider: Color,
  val textFieldHint: Color,
  val bodyMedium: Color,
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF90CAF9),
  onPrimary = Color(0xFF003258),
  primaryContainer = Color(0xFF00497C),
  onPrimaryContainer = Color(0xFFD1E4FF),

  secondary = Color(0xFF81C784),
  onSecondary = Color(0xFF003A03),
  secondaryContainer = Color(0xFF1F5122),
  onSecondaryContainer = Color(0xFFC3EEC4),

  tertiary = Color(0xFFFF8A65),
  onTertiary = Color(0xFF5C1600),
  tertiaryContainer = Color(0xFF832400),
  onTertiaryContainer = Color(0xFFFFDBD0),

  background = Color(0xFF121212),
  onBackground = Color(0xFFE6E1E5),
  surface = Color(0xFF121212),
  onSurface = Color(0xFFE6E1E5),

  error = Color(0xFFCF6679),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6)
).let {
  ExtendedColorScheme(
    colorScheme = it,
    titleText = titleTextDark,
    subTitleText = Color(0xFF61758A),
    divider = Color(0xFF303030),
    textFieldHint = Color(0xFF9BA1A7),
    bodyMedium = Color(0xFF9BA1A7),
  )
}

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF2196F3),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFBBDEFB),
  onPrimaryContainer = Color(0xFF004B91),

  secondary = Color(0xFF4CAF50),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFC8E6C9),
  onSecondaryContainer = Color(0xFF005D04),

  tertiary = Color(0xFFFF5722),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFFFCCBC),
  onTertiaryContainer = Color(0xFF9A2800),

  background = Color(0xFFFFFFFF),
  onBackground = Color(0xFF1C1B1F),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1C1B1F),

  error = Color(0xFFB3261E),
  onError = Color.White,
  errorContainer = Color(0xFFF9DEDC),
  onErrorContainer = Color(0xFF410E0B)
).let {
  ExtendedColorScheme(
    colorScheme = it,
    titleText = titleTextLight,
    subTitleText = Color(0xFF61758A),
    divider = Color(0xFFF0F2F5),
    textFieldHint = Color(0xFF637387),
    bodyMedium = Color(0xFF637387),
  )
}
