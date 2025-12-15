@file:Suppress("FunctionNaming")

package com.yueban.compilecook

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.ApplicationLifecycle
import com.yueban.compilecook.ui.root.DefaultRootComponent
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
  val rootComponent = DefaultRootComponent(
    componentContext = DefaultComponentContext(lifecycle = ApplicationLifecycle())
  )
  return ComposeUIViewController { App(rootComponent) }
}
