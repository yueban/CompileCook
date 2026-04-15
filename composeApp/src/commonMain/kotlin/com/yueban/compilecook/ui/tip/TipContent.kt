package com.yueban.compilecook.ui.tip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.image.ImageComponent
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.markdown.MarkdownDetailContent
import kotlinx.coroutines.flow.MutableStateFlow

private const val IMAGE_OVERLAY_LABEL = "TIP_IMAGE_OVERLAY"

@Composable
fun TipContent(component: TipComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()
  val imageSlot by component.imageSlot.subscribeAsState()

  val toc = remember(state.tocAsync) {
    (state.tocAsync as? Success)?.value.orEmpty()
  }

  MarkdownDetailContent(
    title = state.tipName,
    contentAsync = state.contentAsync,
    toc = toc,
    imageSlot = imageSlot,
    onBackClick = component::onBackClicked,
    onImageClick = component::onImageClicked,
    overlayLabel = IMAGE_OVERLAY_LABEL,
  )
}

private class PreviewTipComponent : TipComponent {
  override val uiState = MutableStateFlow(PreviewData.tipState)
  override val imageSlot: Value<ChildSlot<String, ImageComponent>> = MutableValue(ChildSlot())
  override fun onBackClicked() = Unit
  override fun onImageClicked(imageUrl: String) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewTipContent() = PreviewWrapper {
  TipContent(component = PreviewTipComponent())
}
