@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.yueban.compilecook.ui.root.DefaultRootComponent
import com.yueban.compilecook.util.runOnUiThread

fun main() {
  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  val backDispatcher = BackDispatcher()

  val root = runOnUiThread {
    DefaultRootComponent(
      componentContext = DefaultComponentContext(
        lifecycle = LifecycleRegistry(),
        backHandler = backDispatcher,
      )
    )
  }

  application {
    val windowState = rememberWindowState()
    LifecycleController(lifecycle, windowState)

    Window(
      onCloseRequest = ::exitApplication,
      onKeyEvent = { event ->
        if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
          backDispatcher.back()
        } else {
          false
        }
      },
      state = windowState,
      title = "CompileCook",
    ) {
      App(root)
    }
  }
}
