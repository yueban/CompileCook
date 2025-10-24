@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
  AppInitializer.init()

  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "CompileCook",
    ) {
      App()
    }
  }
}
