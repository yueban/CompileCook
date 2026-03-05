package com.yueban.compilecook.ui.util

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "1. Light Mode", showBackground = true)
@Preview(
  name = "2. Dark Mode",
  showBackground = true,
  uiMode = AndroidUiModes.UI_MODE_NIGHT_YES
)
@Preview(name = "3. Desktop", showBackground = true, widthDp = 1024, heightDp = 700)
@Preview(name = "4. English", showBackground = true, locale = "en")
annotation class UniversalPreview
