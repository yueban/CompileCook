package com.yueban.compilecook.ui.tip

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.TitleTopBar
import com.yueban.compilecook.ui.widget.markdown.CookMarkdown
import com.yueban.compilecook.ui.widget.markdown.TocBottomSheet
import com.yueban.compilecook.ui.widget.markdown.TocFab
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun TipContent(component: TipComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val toc = remember(state.tocAsync) {
    (state.tocAsync as? Success)?.value.orEmpty()
  }
  val listState = rememberLazyListState()
  var showToc by remember { mutableStateOf(false) }
  var isAutoScrolling by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TitleTopBar(
        title = state.tipName,
        enableBack = true,
        onBackClick = component::onBackClicked
      )
    },
    floatingActionButton = {
      TocFab(
        listState = listState,
        toc = toc,
        isAutoScrolling = isAutoScrolling,
        onClick = { showToc = true }
      )
    }
  ) { padding ->
    AsyncContent(async = state.contentAsync, modifier = Modifier.padding(padding)) {
      CookMarkdown(
        state = it,
        modifier = Modifier.padding(horizontal = AppTheme.dimens.screenPadding),
        listState = listState,
        onImageClick = component::onImageClicked,
      )
    }
  }

  if (showToc) {
    TocBottomSheet(
      toc = toc,
      listState = listState,
      onTocItemClick = {
        showToc = false
        coroutineScope.launch {
          isAutoScrolling = true
          runCatching { listState.animateScrollToItem(it.nodeIndex) }
          // small delay to ensure isScrollingUp is not updated right after the auto-scrolling finishes
          delay(100)
          isAutoScrolling = false
        }
      },
      onDismiss = { showToc = false },
    )
  }
}

private class PreviewTipComponent : TipComponent {
  override val uiState = MutableStateFlow(PreviewData.tipState)
  override fun onBackClicked() = Unit
  override fun onImageClicked(imageUrl: String) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewTipContent() = PreviewWrapper {
  TipContent(component = PreviewTipComponent())
}
