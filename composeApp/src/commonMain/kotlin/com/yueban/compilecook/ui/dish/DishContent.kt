package com.yueban.compilecook.ui.dish

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.FavoriteButton
import com.yueban.compilecook.ui.widget.markdown.MarkdownDetailContent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun DishContent(component: DishComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val toc = remember(state.tocAsync) {
    (state.tocAsync as? Success)?.value.orEmpty()
  }

  MarkdownDetailContent(
    title = state.dishName,
    contentAsync = state.contentAsync,
    toc = toc,
    onBackClick = component::onBackClicked,
    onImageClick = component::onImageClicked,
    onAiClick = component::onAiClicked,
    topBarActions = {
      state.dishAsync.value?.let {
        FavoriteButton(
          isFavorite = it.isFavorite,
          onClick = component::onFavoriteToggle
        )
      }
    },
  )
}

private class PreviewDishComponent : DishComponent {
  override val uiState = MutableStateFlow(PreviewData.dishState)
  override fun onBackClicked() = Unit
  override fun onAiClicked() = Unit
  override fun onFavoriteToggle() = Unit
  override fun onImageClicked(imageUrl: String) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewDishContent() = PreviewWrapper {
  DishContent(component = PreviewDishComponent())
}
