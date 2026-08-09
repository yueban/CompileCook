package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.foundation.layout.RowScope
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
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.widget.TitleTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@Composable
fun MarkdownDetailContent(
  title: String,
  contentAsync: Async<State>,
  toc: List<TocItem>,
  onBackClick: () -> Unit,
  onImageClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  onAiClick: (() -> Unit)? = null,
  topBarActions: @Composable RowScope.() -> Unit = {},
) {
  val listState = rememberLazyListState()
  var showToc by remember { mutableStateOf(false) }
  var isAutoScrolling by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  Scaffold(
    topBar = {
      TitleTopBar(
        title = title,
        onBackClick = onBackClick,
        onAiClick = onAiClick,
        actions = topBarActions,
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
    AsyncContent(async = contentAsync, modifier = modifier.padding(padding)) {
      CookMarkdown(
        state = it,
        modifier = Modifier.padding(horizontal = AppTheme.dimens.screenPadding),
        listState = listState,
        onImageClick = onImageClick,
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
