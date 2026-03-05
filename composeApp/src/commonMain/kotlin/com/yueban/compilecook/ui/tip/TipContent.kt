package com.yueban.compilecook.ui.tip

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewData
import com.yueban.compilecook.ui.util.UniversalPreview
import com.yueban.compilecook.ui.widget.CookMarkdown
import com.yueban.compilecook.ui.widget.TitleTopBar
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TipContent(component: TipComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TitleTopBar(
        title = state.tipName,
        enableBack = true,
        onBackClick = component::onBackClicked
      )
    },
  ) { padding ->
    AsyncContent(async = state.contentAsync, modifier = Modifier.padding(padding)) {
      CookMarkdown(state = it, modifier = Modifier.padding(16.dp))
    }
  }
}

private class PreviewTipComponent : TipComponent {
  override val uiState = MutableStateFlow(PreviewData.tipState)
  override fun onBackClicked() = Unit
}

@UniversalPreview
@Composable
private fun PreviewTipContent() = AppTheme {
  TipContent(component = PreviewTipComponent())
}
