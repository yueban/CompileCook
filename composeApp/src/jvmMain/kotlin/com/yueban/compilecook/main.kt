@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.yueban.compilecook.ui.root.DefaultRootComponent
import com.yueban.compilecook.util.runOnUiThread

fun main() {
  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  val root = runOnUiThread {
    DefaultRootComponent(componentContext = DefaultComponentContext(lifecycle = lifecycle))
  }

  application {
    val windowState = rememberWindowState()
    LifecycleController(lifecycle, windowState)

    Window(
      onCloseRequest = ::exitApplication,
      state = windowState,
      title = "CompileCook",
    ) {
      App(root)
    }
  }
}
