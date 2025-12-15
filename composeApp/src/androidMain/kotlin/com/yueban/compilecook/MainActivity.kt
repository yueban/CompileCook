package com.yueban.compilecook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.yueban.compilecook.ui.root.DefaultRootComponent

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val root = DefaultRootComponent(componentContext = defaultComponentContext())

    setContent {
      App(root)
    }
  }
}
