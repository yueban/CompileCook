package com.yueban.compilecook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.noto_sans_sc_regular
import org.jetbrains.compose.resources.Font

@Composable
fun AppTheme(
  content: @Composable () -> Unit,
) {
  // TODO: temporary solution for CJK characters display in web browser
  // TODO: browser font should be officially supported by KMP
  val notoSansSC = FontFamily(Font(Res.font.noto_sans_sc_regular))
  MaterialTheme(
    typography = Typography(
      displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = notoSansSC),
      displayMedium = MaterialTheme.typography.displayMedium.copy(fontFamily = notoSansSC),
      displaySmall = MaterialTheme.typography.displaySmall.copy(fontFamily = notoSansSC),

      headlineLarge = MaterialTheme.typography.headlineLarge.copy(fontFamily = notoSansSC),
      headlineMedium = MaterialTheme.typography.headlineMedium.copy(fontFamily = notoSansSC),
      headlineSmall = MaterialTheme.typography.headlineSmall.copy(fontFamily = notoSansSC),

      titleLarge = MaterialTheme.typography.titleLarge.copy(fontFamily = notoSansSC),
      titleMedium = MaterialTheme.typography.titleMedium.copy(fontFamily = notoSansSC),
      titleSmall = MaterialTheme.typography.titleSmall.copy(fontFamily = notoSansSC),

      bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = notoSansSC),
      bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = notoSansSC),
      bodySmall = MaterialTheme.typography.bodySmall.copy(fontFamily = notoSansSC),

      labelLarge = MaterialTheme.typography.labelLarge.copy(fontFamily = notoSansSC),
      labelMedium = MaterialTheme.typography.labelMedium.copy(fontFamily = notoSansSC),
      labelSmall = MaterialTheme.typography.labelSmall.copy(fontFamily = notoSansSC)
    ),
  ) {
    content()
  }
}
