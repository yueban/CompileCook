package com.yueban.compilecook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
  AppTheme {
    val appInitializerSignal: AppInitializerSignal = koinInject()
    val isReady by appInitializerSignal.isReady.collectAsState()
    if (!isReady) return@AppTheme

    val scrollState = rememberScrollState()
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState, true),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // TODO: display dish content from net
    }
  }
}
