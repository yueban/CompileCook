package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.widget.AnimatedFab

@Composable
fun TocFab(
  listState: LazyListState,
  toc: List<TocItem>,
  isAutoScrolling: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (toc.isEmpty()) return

  val currentIsAutoScrolling by rememberUpdatedState(isAutoScrolling)
  var isScrollingUp by remember { mutableStateOf(true) }

  LaunchedEffect(listState) {
    var previousIndex = listState.firstVisibleItemIndex
    var previousScrollOffset = listState.firstVisibleItemScrollOffset

    snapshotFlow {
      Triple(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset,
        listState.isScrollInProgress
      )
    }.collect { (currentIndex, currentOffset, inProgress) ->
      val hasMoved = previousIndex != currentIndex || previousScrollOffset != currentOffset

      // force isScrollingUp to true when auto-scrolling to avoid fab disappeared after auto-scrolling
      if (currentIsAutoScrolling) {
        isScrollingUp = true
      } else if (inProgress && hasMoved) {
        // update value only when list is scrolling but not triggered by auto-scrolling
        isScrollingUp = if (previousIndex != currentIndex) {
          previousIndex > currentIndex
        } else {
          previousScrollOffset >= currentOffset
        }
      }

      previousIndex = currentIndex
      previousScrollOffset = currentOffset
    }
  }

  val isVisible by remember {
    derivedStateOf {
      isAutoScrolling || isScrollingUp || listState.firstVisibleItemIndex == 0
    }
  }

  AnimatedFab(isVisible = isVisible, onClick = onClick, modifier = modifier)
}
