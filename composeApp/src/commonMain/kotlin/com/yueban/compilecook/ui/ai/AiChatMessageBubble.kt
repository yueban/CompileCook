package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatMessageStatus
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_error_network
import compilecook.composeapp.generated.resources.ai_chat_error_server
import compilecook.composeapp.generated.resources.ai_chat_error_timeout
import compilecook.composeapp.generated.resources.ai_chat_error_unknown
import compilecook.composeapp.generated.resources.ai_chat_retry
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MessageBubble(message: AiChatMessage, isLoading: Boolean, onRetry: () -> Unit) {
  val isRetryable = message.status == AiChatMessageStatus.NETWORK_ERROR ||
    message.status == AiChatMessageStatus.TIMEOUT_ERROR ||
    message.status == AiChatMessageStatus.SERVER_ERROR ||
    message.status == AiChatMessageStatus.CANCELLED

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
      verticalAlignment = Alignment.Bottom,
    ) {
      MessageBubbleContent(
        content = message.content,
        images = message.images,
        isUser = message.isUser,
        status = message.status,
      )
      if (message.isStreaming) {
        CircularProgressIndicator(
          modifier = Modifier.size(AppTheme.dimens.aiChatLoadingSize).padding(start = AppTheme.dimens.tinyGap),
          strokeWidth = AppTheme.dimens.aiChatLoadingStroke,
        )
      }
    }
    if (isRetryable) {
      TextButton(
        onClick = onRetry,
        enabled = !isLoading,
        modifier = Modifier.padding(top = AppTheme.dimens.tinyGap),
      ) {
        Text(
          text = stringResource(Res.string.ai_chat_retry),
          style = AppTheme.typography.labelMedium,
          color = AppTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
private fun MessageBubbleContent(
  content: String,
  images: List<String> = emptyList(),
  isUser: Boolean,
  status: AiChatMessageStatus = AiChatMessageStatus.COMPLETED,
) {
  val isError = status != AiChatMessageStatus.COMPLETED &&
    status != AiChatMessageStatus.STREAMING &&
    status != AiChatMessageStatus.CANCELLED
  val backgroundColor = when {
    isUser -> AppTheme.colorScheme.primary
    isError -> AppTheme.colorScheme.errorContainer
    else -> AppTheme.colorScheme.surfaceVariant
  }
  val textColor = when {
    isUser -> AppTheme.colorScheme.onPrimary
    isError -> AppTheme.colorScheme.onErrorContainer
    else -> AppTheme.colorScheme.onSurfaceVariant
  }
  val errorMessage = status.errorMessage()
  val displayText = when {
    isError && content.isNotBlank() -> "$content\n\n$errorMessage"
    isError -> errorMessage.orEmpty()
    else -> content
  }

  Box(
    modifier = Modifier
      .widthIn(max = AppTheme.dimens.aiChatMessageMaxWidth)
      .clip(
        RoundedCornerShape(
          topStart = AppTheme.dimens.radiusLarge,
          topEnd = AppTheme.dimens.radiusLarge,
          bottomStart = if (isUser) AppTheme.dimens.radiusLarge else AppTheme.dimens.radiusExtraSmall,
          bottomEnd = if (isUser) AppTheme.dimens.radiusExtraSmall else AppTheme.dimens.radiusLarge,
        )
      )
      .background(backgroundColor)
      .padding(AppTheme.dimens.mediumGap)
  ) {
    Column {
      if (images.isNotEmpty()) {
        MessageImageGrid(images = images)
        if (displayText.isNotBlank()) {
          Spacer(modifier = Modifier.height(AppTheme.dimens.smallGap))
        }
      }
      if (displayText.isNotBlank()) {
        // TODO: render markdown in assistant messages (lists, code blocks, links, etc.)
        Text(
          text = displayText,
          style = AppTheme.typography.bodyMedium,
          color = textColor,
        )
      }
    }
  }
}

@Composable
private fun MessageImageGrid(images: List<String>) {
  val columns = if (images.size == 1) 1 else 2
  Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.tinyGap)) {
    images.chunked(columns).forEach { row ->
      Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.tinyGap)) {
        row.forEach { imagePath ->
          AiChatImage(
            path = imagePath,
            contentDescription = null,
            modifier = Modifier
              .weight(1f)
              .height(AppTheme.dimens.aiChatMessageImageHeight)
              .clip(RoundedCornerShape(AppTheme.dimens.radiusSmall)),
          )
        }
        if (row.size < columns) {
          repeat(columns - row.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun AiChatMessageStatus.errorMessage(): String? = when (this) {
  AiChatMessageStatus.NETWORK_ERROR -> stringResource(Res.string.ai_chat_error_network)
  AiChatMessageStatus.TIMEOUT_ERROR -> stringResource(Res.string.ai_chat_error_timeout)
  AiChatMessageStatus.SERVER_ERROR -> stringResource(Res.string.ai_chat_error_server)
  AiChatMessageStatus.UNKNOWN_ERROR -> stringResource(Res.string.ai_chat_error_unknown)
  else -> null
}
