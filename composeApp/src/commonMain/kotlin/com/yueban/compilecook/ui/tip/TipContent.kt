package com.yueban.compilecook.ui.tip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.markdown.MarkdownDetailContent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun TipContent(component: TipComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val toc = remember(state.tocAsync) {
    (state.tocAsync as? Success)?.value.orEmpty()
  }

  MarkdownDetailContent(
    title = state.tipName,
    contentAsync = state.contentAsync,
    toc = toc,
    onBackClick = component::onBackClicked,
    onImageClick = component::onImageClicked,
    onAiClick = component::onAiClicked,
  )
}

private class PreviewTipComponent : TipComponent {
  override val uiState = MutableStateFlow(PreviewData.tipState)
  override fun onBackClicked() = Unit
  override fun onAiClicked() = Unit
  override fun onImageClicked(imageUrl: String) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewTipContent() = PreviewWrapper {
  TipContent(component = PreviewTipComponent())
}
