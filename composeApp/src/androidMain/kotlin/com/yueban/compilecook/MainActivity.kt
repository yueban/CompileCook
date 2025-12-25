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
import com.yueban.compilecook.util.Url

class MainActivity : ComponentActivity() {
  private lateinit var root: RootComponent

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    root = DefaultRootComponent(
      componentContext = defaultComponentContext(),
      deepLinkUrl = intent?.toDeepLinkUrl()
    )

    setContent {
      App(root)
    }
  }

  override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
    super.onNewIntent(intent, caller)
    intent.toDeepLinkUrl()?.let { root.onDeepLink(it) }
  }

  private fun Intent.toDeepLinkUrl(): Url? =
    if (action == Intent.ACTION_VIEW && data != null) {
      Url.parse(data.toString())
    } else {
      null
    }
}
