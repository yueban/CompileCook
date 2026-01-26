package com.yueban.compilecook.app

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.arkivanov.decompose.defaultComponentContext
import com.yueban.compilecook.App
import com.yueban.compilecook.ui.root.DefaultRootComponent

private const val DEEPLINK_SCHEME = "yueban"
private const val DEEPLINK_HOST = "compilecook"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val deepLinkUrl = intent?.getValidUrl()

    setContent {
      App {
        val context = defaultComponentContext()
        remember {
          DefaultRootComponent(
            componentContext = context,
            deepLinkUrl = deepLinkUrl
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
    super.onNewIntent(intent, caller)
    // TODO: handle deep link in RootComponent by event flow
//    intent.getValidUrl()?.let { root.onDeepLink(it) }
  }

  private fun Intent.getValidUrl(): String? =
    takeIf { action == Intent.ACTION_VIEW }
      ?.data
      ?.takeIf { it.scheme == DEEPLINK_SCHEME && it.host == DEEPLINK_HOST }
      ?.toString()
}
