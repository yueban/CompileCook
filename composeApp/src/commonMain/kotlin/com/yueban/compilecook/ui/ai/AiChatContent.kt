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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.repo.entity.AiContextType
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_context_changed_format
import compilecook.composeapp.generated.resources.ai_chat_context_format
import compilecook.composeapp.generated.resources.ai_chat_des_camera
import compilecook.composeapp.generated.resources.ai_chat_des_send
import compilecook.composeapp.generated.resources.ai_chat_dismiss
import compilecook.composeapp.generated.resources.ai_chat_input_hint
import compilecook.composeapp.generated.resources.ai_chat_new_chat
import compilecook.composeapp.generated.resources.ai_hint_dish_how_to_cook
import compilecook.composeapp.generated.resources.ai_hint_dish_nutrition
import compilecook.composeapp.generated.resources.ai_hint_dish_substitutions
import compilecook.composeapp.generated.resources.ai_hint_general_recipe
import compilecook.composeapp.generated.resources.ai_hint_general_tips
import compilecook.composeapp.generated.resources.ai_hint_general_what_to_make
import compilecook.composeapp.generated.resources.ai_hint_tip_alternatives
import compilecook.composeapp.generated.resources.ai_hint_tip_explain
import compilecook.composeapp.generated.resources.ai_hint_tip_mistakes
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

  // user sends a message — always scroll to bottom
  val userMessageCount = state.messages.count { it.role == AiChatRole.USER }
  LaunchedEffect(userMessageCount) {
    if (userMessageCount > 0) {
      listState.scrollToItem(state.messages.size)
    }
  }

  // auto scroll on last message changing
  val lastMessage = state.messages.lastOrNull()
  LaunchedEffect(lastMessage) {
    if (lastMessage == null || lastMessage.role != AiChatRole.ASSISTANT) return@LaunchedEffect

    val layoutInfo = listState.layoutInfo
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
    val lastMessageIndex = state.messages.size
    // response generating — keep bottom visible when content grows
    if (lastVisible.index >= lastMessageIndex) {
      val lastItem = layoutInfo.visibleItemsInfo.lastOrNull { it.index == lastMessageIndex } ?: lastVisible
      val offset = (lastItem.size - layoutInfo.viewportEndOffset).coerceAtLeast(0)
      listState.scrollToItem(lastMessageIndex, scrollOffset = offset)
    } else if (lastVisible.index >= lastMessageIndex - 1) {
      // scroll to bottom only if the previous last item was completely visible
      val isFullyVisible =
        lastVisible.offset >= 0 && lastVisible.offset + lastVisible.size <= layoutInfo.viewportEndOffset
      if (isFullyVisible) {
        listState.scrollToItem(lastMessageIndex)
      }
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
          .padding(horizontal = AppTheme.dimens.screenPadding, vertical = AppTheme.dimens.smallGap)
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

    val pendingContext = state.pendingContext
    if (pendingContext != null && state.messages.isNotEmpty()) {
      ContextChangeBanner(
        newContextName = pendingContext.name,
        onNewChat = component::switchContext,
        onDismiss = component::dismissContextChange,
      )
    }

    if (state.messages.isEmpty()) {
      HintContent(
        context = state.currentContext,
        onHintClick = { component.sendMessage(it) },
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = AppTheme.dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap),
      ) {
        item(key = "top_spacer") {
          Spacer(modifier = Modifier.height(AppTheme.dimens.smallGap))
        }

        val lastMessageId = state.messages.lastOrNull()?.id
        items(state.messages, key = { it.id }) { message ->
          MessageBubble(
            message = message,
            isLoading = state.isLoading && message.role == AiChatRole.ASSISTANT && message.id == lastMessageId,
          )
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
      .padding(AppTheme.dimens.smallGap),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap),
  ) {
    OutlinedTextField(
      value = inputText,
      onValueChange = onInputChange,
      modifier = Modifier.weight(1f),
      placeholder = { Text(stringResource(Res.string.ai_chat_input_hint)) },
      maxLines = 3,
      shape = RoundedCornerShape(AppTheme.dimens.aiChatInputFieldRadius),
    )

    IconButton(
      onClick = onCameraClick,
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
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
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
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
private fun MessageBubble(message: AiChatMessage, isLoading: Boolean = false) {
  val isUser = message.role == AiChatRole.USER

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom,
  ) {
    MessageBubbleContent(message.content, isUser)
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(AppTheme.dimens.aiChatLoadingSize).padding(start = AppTheme.dimens.tinyGap),
        strokeWidth = AppTheme.dimens.aiChatLoadingStroke,
      )
    }
  }
}

@Composable
private fun MessageBubbleContent(content: String, isUser: Boolean) {
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
      .background(if (isUser) AppTheme.colorScheme.primary else AppTheme.colorScheme.surfaceVariant)
      .padding(AppTheme.dimens.mediumGap)
  ) {
    Text(
      text = content,
      style = AppTheme.typography.bodyMedium,
      color = if (isUser) AppTheme.colorScheme.onPrimary else AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun HintContent(
  context: AiContext?,
  onHintClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hints = when (context?.type) {
    AiContextType.DISH -> listOf(
      stringResource(Res.string.ai_hint_dish_how_to_cook),
      stringResource(Res.string.ai_hint_dish_substitutions),
      stringResource(Res.string.ai_hint_dish_nutrition),
    )
    AiContextType.TIP -> listOf(
      stringResource(Res.string.ai_hint_tip_explain),
      stringResource(Res.string.ai_hint_tip_alternatives),
      stringResource(Res.string.ai_hint_tip_mistakes),
    )
    else -> listOf(
      stringResource(Res.string.ai_hint_general_recipe),
      stringResource(Res.string.ai_hint_general_tips),
      stringResource(Res.string.ai_hint_general_what_to_make),
    )
  }

  Box(modifier = modifier, contentAlignment = Alignment.BottomStart) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.dimens.screenPadding)) {
      hints.forEach { hint ->
        AssistChip(
          onClick = { onHintClick(hint) },
          label = { Text(hint) },
        )
      }
    }
  }
}

@Composable
private fun ContextChangeBanner(
  newContextName: String,
  onNewChat: () -> Unit,
  onDismiss: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(AppTheme.colorScheme.secondaryContainer)
      .padding(horizontal = AppTheme.dimens.screenPadding, vertical = AppTheme.dimens.smallGap),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(Res.string.ai_chat_context_changed_format, newContextName),
      style = AppTheme.typography.labelMedium,
      color = AppTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier.weight(1f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    TextButton(onClick = onNewChat) {
      Text(
        text = stringResource(Res.string.ai_chat_new_chat),
        style = AppTheme.typography.labelMedium,
        color = AppTheme.colorScheme.primary,
      )
    }
    TextButton(onClick = onDismiss) {
      Text(
        text = stringResource(Res.string.ai_chat_dismiss),
        style = AppTheme.typography.labelMedium,
        color = AppTheme.colorScheme.error,
      )
    }
  }
}
