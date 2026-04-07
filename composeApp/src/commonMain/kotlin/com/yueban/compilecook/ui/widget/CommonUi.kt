package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalWidgetPreview
import com.yueban.compilecook.ui.util.stringRes
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_empty_data
import compilecook.composeapp.generated.resources.common_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoadingComposable(modifier: Modifier = Modifier) {
  Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
      strokeWidth = AppTheme.dimens.indicatorWidth
    )
  }
}

@Composable
fun ErrorComposable(
  error: Throwable,
  message: String? = null,
  onRetry: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val displayMessage = message ?: stringResource(error.stringRes)

  InfoStateComposable(
    icon = Icons.Filled.ErrorOutline,
    message = displayMessage,
    color = AppTheme.colorScheme.error,
    modifier = modifier,
    action = if (onRetry != null) {
      { Button(onClick = onRetry) { Text(stringResource(Res.string.common_retry)) } }
    } else {
      null
    }
  )
}

@Composable
fun EmptyComposable(
  message: String? = null,
  modifier: Modifier = Modifier,
  action: @Composable (() -> Unit)? = null,
) {
  val displayMessage = message ?: stringResource(Res.string.common_empty_data)

  InfoStateComposable(
    icon = Icons.Outlined.Info,
    message = displayMessage,
    color = AppTheme.colorScheme.onSurfaceVariant,
    modifier = modifier,
    action = action
  )
}

@Composable
private fun InfoStateComposable(
  icon: ImageVector,
  message: String,
  color: Color,
  modifier: Modifier = Modifier,
  action: (@Composable () -> Unit)? = null,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(AppTheme.dimens.extraLargeGap),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      modifier = Modifier.size(AppTheme.dimens.categoryIconBox),
      tint = color
    )
    Spacer(Modifier.height(AppTheme.dimens.screenPadding))
    Text(text = message, style = AppTheme.typography.titleMedium, color = color, textAlign = TextAlign.Center)
    if (action != null) {
      Spacer(Modifier.height(AppTheme.dimens.largeGap))
      action()
    }
  }
}

@UniversalWidgetPreview
@Composable
private fun PreviewLoadingComposable() = PreviewWrapper {
  LoadingComposable()
}

@UniversalWidgetPreview
@Composable
private fun PreviewEmptyComposable() = PreviewWrapper {
  EmptyComposable(
    message = stringResource(Res.string.common_empty_data)
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewEmptyComposable_WithAction() = PreviewWrapper {
  EmptyComposable(
    message = stringResource(Res.string.common_empty_data),
    action = {
      Button(onClick = {}) {
        Text(stringResource(Res.string.common_retry))
      }
    }
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewErrorComposable() = PreviewWrapper {
  ErrorComposable(
    error = IllegalStateException("unknown error"),
    onRetry = {}
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewErrorComposable_NoRetry() = PreviewWrapper {
  ErrorComposable(
    error = Exception("unknown error"),
    onRetry = null
  )
}
