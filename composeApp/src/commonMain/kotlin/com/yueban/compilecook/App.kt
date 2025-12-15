package com.yueban.compilecook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yueban.compilecook.ui.root.RootComponent
import com.yueban.compilecook.ui.root.RootContent
import com.yueban.compilecook.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App(root: RootComponent) {
  AppTheme {
    val appInitializerSignal: AppInitializerSignal = koinInject()
    val isReady by appInitializerSignal.isReady.collectAsState()
    if (!isReady) return@AppTheme

    Column {
      Spacer(Modifier.height(24.dp))
      RootContent(component = root, modifier = Modifier.fillMaxSize())
    }
  }
}
