package com.yueban.compilecook.ui.tip

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.widget.CommonTopBar
import com.yueban.compilecook.ui.widget.MarkdownViewer

@Composable
fun TipContent(component: TipComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      CommonTopBar(
        title = state.tipName,
        enableBack = true,
        onBackClick = { component.onBackClicked() }
      )
    },
  ) { padding ->
    AsyncContent(async = state.tipAsync, modifier = Modifier.padding(padding)) {
      MarkdownViewer(content = it.content, modifier = Modifier.padding(16.dp))
    }
  }
}
