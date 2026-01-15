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

/**
 * Best Practice AsyncContent.
 *
 * Logic:
 * 1. If data exists (Success, or Loading/Fail with prevData), render Content.
 * 2. If data is missing, render Full Screen Loading/Error/Uninitialized.
 * 3. Handles "Empty List" logic if using the Collection overload.
 */
@Composable
fun <T> AsyncContent(
  async: Async<T?>,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.Center,
  onRetry: (() -> Unit)? = null,
  loadingContent: @Composable BoxScope.() -> Unit = { LoadingComposable() },
  errorContent: @Composable BoxScope.(Throwable) -> Unit = { ErrorComposable(error = it, onRetry = onRetry) },
  emptyContent: @Composable BoxScope.() -> Unit = { EmptyComposable() },
  uninitializedContent: @Composable BoxScope.() -> Unit = loadingContent,
  content: @Composable (T) -> Unit,
) {
  val currentData = async.invoke()

  Box(modifier = modifier, contentAlignment = contentAlignment) {
    if (currentData != null) {
      content(currentData)
    } else {
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
    }
  }
}

/**
 * Specialized version for Collections (List, Set).
 * Handles the "Success but Empty List" scenario automatically.
 */
@Composable
fun <T, C : Collection<T>> AsyncListContent(
  async: Async<C>,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.Center,
  onRetry: () -> Unit = {},
  loadingContent: @Composable BoxScope.() -> Unit = { LoadingComposable() },
  errorContent: @Composable BoxScope.(Throwable) -> Unit = { ErrorComposable(error = it, onRetry = onRetry) },
  emptyContent: @Composable BoxScope.() -> Unit = { EmptyComposable() },
  uninitializedContent: @Composable BoxScope.() -> Unit = loadingContent,
  content: @Composable (C) -> Unit,
) {
  val currentData = async.invoke()

  Box(modifier = modifier, contentAlignment = contentAlignment) {
    when {
      currentData != null && currentData.isNotEmpty() -> content(currentData)

      currentData != null && currentData.isEmpty() ->
        if (async is Fail) errorContent(async.error) else emptyContent()

      else -> {
        Crossfade(targetState = async) { state ->
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
              is Uninitialized -> uninitializedContent()
              is Loading -> loadingContent()
              is Fail -> errorContent(state.error)
              is Success -> Unit
            }
          }
        }
      }
    }
  }
}
