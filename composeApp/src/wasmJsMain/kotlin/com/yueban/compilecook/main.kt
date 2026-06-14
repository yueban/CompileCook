@file:Suppress("Filename")

package com.yueban.compilecook

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.webhistory.withWebHistory
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import com.yueban.compilecook.ui.root.DefaultRootComponent
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.app_name
import compilecook.composeapp.generated.resources.noto_sans_sc_regular
import org.jetbrains.compose.resources.preloadFont
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.wasm.onWasmReady
import web.dom.DocumentVisibilityState
import web.dom.document
import web.dom.visible
import web.events.EventType
import web.events.addEventListener

fun main() {
  AppInitializer.init()

  val lifecycle = LifecycleRegistry()
  lifecycle.attachToDocument()

  onWasmReady {
    ComposeViewport {
      val cjkFontResult = preloadFont(Res.font.noto_sans_sc_regular)
      val cjkFont = cjkFontResult.value
      var fontsFallbackInitialized by remember { mutableStateOf(false) }
      val fontFamilyResolver = LocalFontFamilyResolver.current

      LaunchedEffect(cjkFont) {
        if (cjkFont != null) {
          fontFamilyResolver.preload(FontFamily(cjkFont))
          fontsFallbackInitialized = true
        }
      }

      if (!fontsFallbackInitialized) return@ComposeViewport

      App {
        val appTitle = stringResource(Res.string.app_name)
        LaunchedEffect(appTitle) {
          document.title = appTitle
        }

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
