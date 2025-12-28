package com.yueban.compilecook

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.yueban.compilecook.ui.root.DefaultRootComponent
import com.yueban.compilecook.ui.root.RootComponent

class MainActivity : ComponentActivity() {
  private lateinit var root: RootComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    root = DefaultRootComponent(
      componentContext = defaultComponentContext(),
      deepLinkUrl = intent?.getUrl()
    )

    setContent {
      App(root)
    }
  }

  override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
    super.onNewIntent(intent, caller)
    intent.getUrl()?.let { root.onDeepLink(it) }
  }

  private fun Intent.getUrl(): String? = takeIf { action == Intent.ACTION_VIEW }?.data?.toString()
}
