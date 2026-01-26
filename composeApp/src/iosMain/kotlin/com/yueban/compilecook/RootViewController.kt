@file:Suppress("FunctionNaming")

package com.yueban.compilecook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.yueban.compilecook.ui.root.RootComponent
import platform.UIKit.UIViewController

fun RootViewController(rootFactory: () -> RootComponent, backDispatcher: BackDispatcher): UIViewController =
  ComposeUIViewController {
    PredictiveBackGestureOverlay(
      backDispatcher = backDispatcher,
      backIcon = { progress, _ ->
        PredictiveBackGestureIcon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          progress = progress,
        )
      },
      modifier = Modifier.fillMaxSize(),
    ) {
      App {
        remember { rootFactory() }
      }
    }
  }
