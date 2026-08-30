@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
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
import compilecook.composeapp.generated.resources.app_name
import io.github.kdroidfilter.platformtools.darkmodedetector.mac.setMacOsAdaptiveTitleBar
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Dimension
import java.io.File
import kotlin.math.roundToInt

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"
private val WINDOW_INITIAL_WIDTH = 1280.dp
private val WINDOW_INITIAL_HEIGHT = 800.dp
private val WINDOW_MIN_WIDTH = 800.dp
private val WINDOW_MIN_HEIGHT = 600.dp

fun main() {
  // must be called before AppInitializer.init() because MainScope() triggers AWT initialization,
  // which reads startup system properties like "apple.awt.application.appearance" only once.
  setMacOsAdaptiveTitleBar()

  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  val backDispatcher = BackDispatcher()
  val stateKeeperFile = File(FileUtils.getUserCacheDir(), SAVED_STATE_FILE_NAME)
  val stateKeeper = StateKeeperDispatcher(stateKeeperFile.readSerializableContainer())

  application {
    val windowState = rememberWindowState(
      width = WINDOW_INITIAL_WIDTH,
      height = WINDOW_INITIAL_HEIGHT,
    )

    Window(
      onCloseRequest = {
        stateKeeper.save().writeToFile(stateKeeperFile)
        exitApplication()
      },
      onKeyEvent = { event: KeyEvent ->
        if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
          backDispatcher.back()
        } else {
          false
        }
      },
      state = windowState,
      title = stringResource(Res.string.app_name),
      icon = painterResource(Res.drawable.app_icon)
    ) {
      window.setWindowsAdaptiveTitleBar()
      SideEffect {
        window.minimumSize = Dimension(
          WINDOW_MIN_WIDTH.value.roundToInt(),
          WINDOW_MIN_HEIGHT.value.roundToInt(),
        )
      }

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
