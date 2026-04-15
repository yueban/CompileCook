package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.slot.ChildSlot
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.image.ImageComponent
import com.yueban.compilecook.ui.image.ImageContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.LocalNavAnimatedVisibilityScope
import com.yueban.compilecook.ui.util.LocalSharedTransitionScope
import com.yueban.compilecook.ui.widget.TitleTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val imageOverlayTransform: ContentTransform =
  fadeIn(initialAlpha = 0f) togetherWith fadeOut(targetAlpha = 1f)

@Suppress("LongParameterList")
@Composable
fun MarkdownDetailContent(
  title: String,
  contentAsync: Async<State>,
  toc: List<TocItem>,
  imageSlot: ChildSlot<String, ImageComponent>,
  onBackClick: () -> Unit,
  onImageClick: (String) -> Unit,
  overlayLabel: String,
  modifier: Modifier = Modifier,
  topBarActions: @Composable RowScope.() -> Unit = {},
) {
  val listState = rememberLazyListState()
  var showToc by remember { mutableStateOf(false) }
  var isAutoScrolling by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  SharedTransitionLayout {
    AnimatedContent(
      targetState = imageSlot.child,
      transitionSpec = { imageOverlayTransform },
      label = overlayLabel,
    ) { imageChild ->
      CompositionLocalProvider(
        LocalSharedTransitionScope provides this@SharedTransitionLayout,
        LocalNavAnimatedVisibilityScope provides this,
      ) {
        Scaffold(
          topBar = {
            TitleTopBar(
              title = title,
              enableBack = true,
              onBackClick = onBackClick,
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
              enableSharedElement = imageChild == null,
            )
          }
        }

        imageChild?.instance?.let { imageComponent ->
          ImageContent(component = imageComponent)
        }
      }
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
