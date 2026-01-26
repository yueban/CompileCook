package com.yueban.compilecook

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.root.RootComponent
import com.yueban.compilecook.ui.root.RootContent
import com.yueban.compilecook.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App(rootFactory: @Composable () -> RootComponent) {
  AppTheme {
    val appInitializerSignal: AppInitializerSignal = koinInject()
    val isReady by appInitializerSignal.isReady.collectAsState()
    if (!isReady) return@AppTheme
    val root = rootFactory()
    RootContent(component = root, modifier = Modifier.fillMaxSize())
  }
}
