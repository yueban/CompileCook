@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.webhistory.withWebHistory
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.yueban.compilecook.ui.root.DefaultRootComponent
import org.jetbrains.skiko.wasm.onWasmReady
import web.dom.DocumentVisibilityState
import web.dom.document
import web.dom.visible
import web.events.EventType
import web.events.addEventListener

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  lifecycle.attachToDocument()

  onWasmReady {
    ComposeViewport {
      App {
        remember {
          withWebHistory { stateKeeper, deepLink ->
            DefaultRootComponent(
              componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
              ),
              deepLinkUrl = deepLink
            )
          }
        }
      }
    }
  }
}

private fun LifecycleRegistry.attachToDocument() {
  fun onVisibilityChanged() {
    if (document.visibilityState == DocumentVisibilityState.visible) {
      resume()
    } else {
      stop()
    }
  }

  onVisibilityChanged()

  document.addEventListener(type = EventType("visibilitychange"), handler = { onVisibilityChanged() })
}
