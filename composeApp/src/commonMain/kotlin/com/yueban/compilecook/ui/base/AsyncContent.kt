package com.yueban.compilecook.ui.base

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.widget.EmptyComposable
import com.yueban.compilecook.ui.widget.ErrorComposable
import com.yueban.compilecook.ui.widget.LoadingComposable

@Composable
inline fun <reified T> AsyncContent(
  async: Async<T?>,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.Center,
  noinline onRetry: (() -> Unit)? = null,
  noinline isEmpty: ((T) -> Boolean)? = null,
  noinline loadingContent: @Composable BoxScope.() -> Unit = { LoadingComposable() },
  noinline errorContent: @Composable BoxScope.(Throwable) -> Unit = { ErrorComposable(error = it, onRetry = onRetry) },
  noinline emptyContent: @Composable BoxScope.() -> Unit = { EmptyComposable() },
  noinline uninitializedContent: @Composable BoxScope.() -> Unit = loadingContent,
  content: @Composable (T) -> Unit,
) {
  val currentData = async.invoke()

  val isDataEmpty = if (currentData != null) {
    when {
      isEmpty != null -> isEmpty(currentData)
      currentData is Collection<*> -> currentData.isEmpty()
      currentData is Map<*, *> -> currentData.isEmpty()
      else -> false
    }
  } else {
    false
  }

  Box(modifier = modifier, contentAlignment = contentAlignment) {
    when {
      currentData == null ->
        Crossfade(targetState = async) { state ->
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
              is Uninitialized -> uninitializedContent()
              is Loading -> loadingContent()
              is Fail -> errorContent(state.error)
              is Success -> emptyContent()
            }
          }
        }

      !isDataEmpty -> content(currentData)

      isDataEmpty ->
        if (async is Fail) {
          errorContent(async.error)
        } else {
          emptyContent()
        }
    }
  }
}
