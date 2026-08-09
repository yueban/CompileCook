package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImagePainter
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatMessageStatus
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.imagePreviewSharedElementPlaceholder
import com.yueban.compilecook.ui.util.imagePreviewSourceBounds
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_error_network
import compilecook.composeapp.generated.resources.ai_chat_error_server
import compilecook.composeapp.generated.resources.ai_chat_error_timeout
import compilecook.composeapp.generated.resources.ai_chat_error_unknown
import compilecook.composeapp.generated.resources.ai_chat_retry
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun MessageBubble(
  message: AiChatMessage,
  isLoading: Boolean,
  onRetry: () -> Unit,
  onImageClick: (String) -> Unit,
) {
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
        onImageClick = onImageClick,
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
  onImageClick: (String) -> Unit,
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
        MessageImageGrid(images = images, onImageClick = onImageClick)
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
private fun MessageImageGrid(
  images: List<String>,
  onImageClick: (String) -> Unit,
) {
  val columns = if (images.size == 1) 1 else 2
  val imageSizes = remember { mutableStateMapOf<String, IntSize>() }

  Column(verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.tinyGap)) {
    images.chunked(columns).forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.tinyGap),
      ) {
        row.forEach { imageRef ->
          val imageSize = imageSizes[imageRef]
          val imageHeightModifier = if (imageSize == null) {
            Modifier.height(AppTheme.dimens.aiChatMessageImageHeight)
          } else {
            Modifier.aspectRatio(imageSize.width.toFloat() / imageSize.height)
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .then(imageHeightModifier)
              .imagePreviewSourceBounds(imageRef)
              .clip(RoundedCornerShape(AppTheme.dimens.radiusSmall))
              .clickable { onImageClick(imageRef) },
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .imagePreviewSharedElementPlaceholder(imageRef)
                .clip(RoundedCornerShape(AppTheme.dimens.radiusSmall)),
            )
            AiChatImage(
              imageRef = imageRef,
              contentDescription = null,
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Fit,
              onState = { state ->
                val intrinsicSize = (state as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize
                if (intrinsicSize != null && intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
                  val size = IntSize(intrinsicSize.width.roundToInt(), intrinsicSize.height.roundToInt())
                  if (imageSizes[imageRef] != size) {
                    imageSizes[imageRef] = size
                  }
                }
              },
            )
          }
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
