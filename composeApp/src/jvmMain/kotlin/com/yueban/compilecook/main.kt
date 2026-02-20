@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.yueban.compilecook.ui.root.DefaultRootComponent
import com.yueban.compilecook.util.FileUtils
import com.yueban.compilecook.util.readSerializableContainer
import com.yueban.compilecook.util.runOnUiThread
import com.yueban.compilecook.util.writeToFile
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import java.io.File

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

fun main() {
  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  val backDispatcher = BackDispatcher()
  val stateKeeperFile = File(FileUtils.getUserCacheDir(), SAVED_STATE_FILE_NAME)
  val stateKeeper = StateKeeperDispatcher(stateKeeperFile.readSerializableContainer())

  application {
    val windowState = rememberWindowState()

    Window(
      onCloseRequest = {
        stateKeeper.save().writeToFile(stateKeeperFile)
        exitApplication()
      },
      onKeyEvent = { event ->
        if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
          backDispatcher.back()
        } else {
          false
        }
      },
      state = windowState,
      title = "CompileCook",
      icon = painterResource(Res.drawable.app_icon)
    ) {
      LifecycleController(
        lifecycleRegistry = lifecycle,
        windowState = windowState,
        windowInfo = LocalWindowInfo.current,
      )

      App {
        remember {
          runOnUiThread {
            DefaultRootComponent(
              componentContext = DefaultComponentContext(
                lifecycle = LifecycleRegistry(),
                stateKeeper = stateKeeper,
                backHandler = backDispatcher,
              )
            )
          }
        }
      }
    }
  }
}
