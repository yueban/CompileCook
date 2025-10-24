@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  AppInitializer.init()

  ComposeViewport {
    App()
  }
}
