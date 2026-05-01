package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_context_format
import compilecook.composeapp.generated.resources.ai_chat_des_camera
import compilecook.composeapp.generated.resources.ai_chat_des_send
import compilecook.composeapp.generated.resources.ai_chat_input_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatContent(
  component: AiChatComponent,
  onCameraClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsStateWithLifecycle()
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(state.messages.size) {
    if (state.messages.isNotEmpty()) {
      listState.animateScrollToItem(state.messages.lastIndex)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorScheme.background)
  ) {
    state.currentContext?.let { context ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(AppTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Text(
          text = stringResource(Res.string.ai_chat_context_format, context.name),
          style = AppTheme.typography.labelMedium,
          color = AppTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item(key = "top_spacer") {
        Spacer(modifier = Modifier.height(8.dp))
      }

      items(state.messages, key = { it.id }) { message ->
        MessageBubble(message)
      }

      if (state.isLoading) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              strokeWidth = 2.dp,
            )
          }
        }
      }
    }

    ChatInputArea(
      inputText = inputText,
      onInputChange = { inputText = it },
      isLoading = state.isLoading,
      onSend = {
        component.sendMessage(inputText)
        inputText = ""
      },
      onCameraClick = onCameraClick,
    )
  }
}

@Composable
private fun ChatInputArea(
  inputText: String,
  onInputChange: (String) -> Unit,
  isLoading: Boolean,
  onSend: () -> Unit,
  onCameraClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    OutlinedTextField(
      value = inputText,
      onValueChange = onInputChange,
      modifier = Modifier.weight(1f),
      placeholder = { Text(stringResource(Res.string.ai_chat_input_hint)) },
      maxLines = 3,
      shape = RoundedCornerShape(24.dp),
    )

    IconButton(
      onClick = onCameraClick,
      modifier = Modifier.size(48.dp),
    ) {
      Icon(
        imageVector = Icons.Default.CameraAlt,
        contentDescription = stringResource(Res.string.ai_chat_des_camera),
        tint = AppTheme.colorScheme.onSurfaceVariant,
      )
    }

    val canSend = inputText.isNotBlank() && !isLoading
    IconButton(
      onClick = {
        if (canSend) {
          onSend()
        }
      },
      modifier = Modifier.size(48.dp),
      enabled = canSend,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Send,
        contentDescription = stringResource(Res.string.ai_chat_des_send),
        tint = if (canSend) {
          AppTheme.colorScheme.primary
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        },
      )
    }
  }
}

@Composable
private fun MessageBubble(message: AiChatMessage) {
  val isUser = message.role == AiChatRole.USER

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
  ) {
    Box(
      modifier = Modifier
        .widthIn(max = 300.dp)
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp,
          )
        )
        .background(
          if (isUser) {
            AppTheme.colorScheme.primary
          } else {
            AppTheme.colorScheme.surfaceVariant
          }
        )
        .padding(12.dp)
    ) {
      Text(
        text = message.content,
        style = AppTheme.typography.bodyMedium,
        color = if (isUser) {
          AppTheme.colorScheme.onPrimary
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        },
      )
    }
  }
}
