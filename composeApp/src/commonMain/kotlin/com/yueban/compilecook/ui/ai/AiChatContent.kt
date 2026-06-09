package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatMessageStatus
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.displayName
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import compilecook.composeapp.generated.resources.ai_chat_context_changed_format
import compilecook.composeapp.generated.resources.ai_chat_context_format
import compilecook.composeapp.generated.resources.ai_chat_des_camera
import compilecook.composeapp.generated.resources.ai_chat_des_history
import compilecook.composeapp.generated.resources.ai_chat_des_new_conversation
import compilecook.composeapp.generated.resources.ai_chat_des_send
import compilecook.composeapp.generated.resources.ai_chat_dismiss
import compilecook.composeapp.generated.resources.ai_chat_error_network
import compilecook.composeapp.generated.resources.ai_chat_error_server
import compilecook.composeapp.generated.resources.ai_chat_error_timeout
import compilecook.composeapp.generated.resources.ai_chat_error_unknown
import compilecook.composeapp.generated.resources.ai_chat_input_hint
import compilecook.composeapp.generated.resources.ai_chat_new_chat
import compilecook.composeapp.generated.resources.ai_chat_new_conversation_confirm_message
import compilecook.composeapp.generated.resources.ai_chat_new_conversation_confirm_title
import compilecook.composeapp.generated.resources.ai_chat_retry
import compilecook.composeapp.generated.resources.ai_hint_dish_how_to_cook
import compilecook.composeapp.generated.resources.ai_hint_dish_nutrition
import compilecook.composeapp.generated.resources.ai_hint_dish_substitutions
import compilecook.composeapp.generated.resources.ai_hint_dishcategory_cooking_tips
import compilecook.composeapp.generated.resources.ai_hint_dishcategory_ingredients
import compilecook.composeapp.generated.resources.ai_hint_dishcategory_variations
import compilecook.composeapp.generated.resources.ai_hint_dishdifficulty_appropriate
import compilecook.composeapp.generated.resources.ai_hint_dishdifficulty_improve
import compilecook.composeapp.generated.resources.ai_hint_dishdifficulty_techniques
import compilecook.composeapp.generated.resources.ai_hint_dishlist_pairing
import compilecook.composeapp.generated.resources.ai_hint_dishlist_substitutions
import compilecook.composeapp.generated.resources.ai_hint_dishlist_what_to_pick
import compilecook.composeapp.generated.resources.ai_hint_main_cooking_method
import compilecook.composeapp.generated.resources.ai_hint_main_quick_meal
import compilecook.composeapp.generated.resources.ai_hint_main_seasonal
import compilecook.composeapp.generated.resources.ai_hint_tip_alternatives
import compilecook.composeapp.generated.resources.ai_hint_tip_explain
import compilecook.composeapp.generated.resources.ai_hint_tip_mistakes
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatContent(
  component: AiChatComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsStateWithLifecycle()
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // user sends a message — always scroll to bottom
  val userMessageCount = state.messages.count { it.role == AiChatRole.USER }
  LaunchedEffect(userMessageCount) {
    if (userMessageCount > 0) {
      listState.scrollToItem((state.messages.size - 1).coerceAtLeast(0))
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
    TopBar(
      state = state,
      onNewConversation = { component.clearMessages() },
      onSwitchContext = component::switchContext,
      onDismissContextChange = component::dismissContextChange,
      onHistoryClick = component::onHistoryClick,
    )

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

        items(state.messages, key = { it.id }) { message ->
          MessageBubble(
            message = message,
            isLoading = state.isLoading,
            onRetry = { component.retryMessage(message.id) },
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
      onCameraClick = component::onCameraClick,
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
    // TODO: support keyboard submit (Enter to send) on desktop/web platforms
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
      modifier = Modifier.size(AppTheme.dimens.iconExtraLarge),
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
      modifier = Modifier.size(AppTheme.dimens.iconExtraLarge),
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
private fun MessageBubble(message: AiChatMessage, isLoading: Boolean, onRetry: () -> Unit) {
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
  isUser: Boolean,
  status: AiChatMessageStatus = AiChatMessageStatus.COMPLETED
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
    // TODO: render markdown in assistant messages (lists, code blocks, links, etc.)
    Text(
      text = displayText,
      style = AppTheme.typography.bodyMedium,
      color = textColor,
    )
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

@Composable
private fun HintContent(
  context: AiChatContext,
  onHintClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hints = when (context) {
    is AiChatContext.Dish -> listOf(
      stringResource(Res.string.ai_hint_dish_how_to_cook),
      stringResource(Res.string.ai_hint_dish_substitutions),
      stringResource(Res.string.ai_hint_dish_nutrition),
    )
    is AiChatContext.Tip -> listOf(
      stringResource(Res.string.ai_hint_tip_explain),
      stringResource(Res.string.ai_hint_tip_alternatives),
      stringResource(Res.string.ai_hint_tip_mistakes),
    )
    is AiChatContext.General -> listOf(
      stringResource(Res.string.ai_hint_main_quick_meal),
      stringResource(Res.string.ai_hint_main_seasonal),
      stringResource(Res.string.ai_hint_main_cooking_method),
    )
    is AiChatContext.DishList -> listOf(
      stringResource(Res.string.ai_hint_dishlist_what_to_pick),
      stringResource(Res.string.ai_hint_dishlist_substitutions),
      stringResource(Res.string.ai_hint_dishlist_pairing),
    )
    is AiChatContext.DishCategory -> listOf(
      stringResource(Res.string.ai_hint_dishcategory_cooking_tips),
      stringResource(Res.string.ai_hint_dishcategory_ingredients),
      stringResource(Res.string.ai_hint_dishcategory_variations),
    )
    is AiChatContext.DishDifficulty -> listOf(
      stringResource(Res.string.ai_hint_dishdifficulty_appropriate),
      stringResource(Res.string.ai_hint_dishdifficulty_improve),
      stringResource(Res.string.ai_hint_dishdifficulty_techniques),
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
private fun TopBar(
  state: AiChatState,
  onNewConversation: () -> Unit,
  onSwitchContext: () -> Unit,
  onDismissContextChange: () -> Unit,
  onHistoryClick: () -> Unit,
) {
  var showNewConversationDialog by remember { mutableStateOf(false) }

  if (showNewConversationDialog) {
    NewConversationDialog(
      onConfirm = {
        showNewConversationDialog = false
        onNewConversation()
      },
      onDismiss = { showNewConversationDialog = false },
    )
  }

  val showContextChange = state.pendingContext != null && state.messages.isNotEmpty()
  val topBarColor =
    if (showContextChange) AppTheme.colorScheme.secondaryContainer else AppTheme.colorScheme.surfaceVariant
  val onTopBarColor =
    if (showContextChange) AppTheme.colorScheme.onSecondaryContainer else AppTheme.colorScheme.onSurfaceVariant

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .defaultMinSize(minHeight = AppTheme.dimens.aiChatTopBarMinHeight)
      .background(topBarColor)
      .padding(horizontal = AppTheme.dimens.smallGap),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (showContextChange) {
      ContextChangeBanner(
        context = state.pendingContext,
        onTopBarColor = onTopBarColor,
        onSwitchContext = onSwitchContext,
        onDismissContextChange = onDismissContextChange,
      )
    } else {
      NormalTopBarContent(
        context = state.currentContext,
        hasMessages = state.messages.isNotEmpty(),
        onTopBarColor = onTopBarColor,
        onNewConversation = { showNewConversationDialog = true },
        onHistoryClick = onHistoryClick,
      )
    }
  }
}

@Composable
private fun NewConversationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.ai_chat_new_conversation_confirm_title)) },
    text = { Text(stringResource(Res.string.ai_chat_new_conversation_confirm_message)) },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(stringResource(Res.string.ai_chat_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.ai_chat_cancel))
      }
    },
  )
}

@Composable
private fun RowScope.ContextChangeBanner(
  context: AiChatContext,
  onTopBarColor: Color,
  onSwitchContext: () -> Unit,
  onDismissContextChange: () -> Unit,
) {
  Text(
    text = stringResource(Res.string.ai_chat_context_changed_format, context.displayName),
    style = AppTheme.typography.labelMedium,
    color = onTopBarColor,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.weight(1f).padding(start = AppTheme.dimens.smallGap),
  )

  Row(
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.tinyGap),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(Res.string.ai_chat_new_chat),
      style = AppTheme.typography.labelMedium,
      color = AppTheme.colorScheme.primary,
      modifier = Modifier.clickable(onClick = onSwitchContext)
        .padding(horizontal = AppTheme.dimens.smallGap, vertical = AppTheme.dimens.tinyGap),
    )
    Text(
      text = stringResource(Res.string.ai_chat_dismiss),
      style = AppTheme.typography.labelMedium,
      color = AppTheme.colorScheme.error,
      modifier = Modifier.clickable(onClick = onDismissContextChange)
        .padding(horizontal = AppTheme.dimens.smallGap, vertical = AppTheme.dimens.tinyGap),
    )
  }
}

@Composable
private fun RowScope.NormalTopBarContent(
  context: AiChatContext,
  hasMessages: Boolean,
  onTopBarColor: Color,
  onNewConversation: () -> Unit,
  onHistoryClick: () -> Unit,
) {
  if (context.displayName.isNotBlank()) {
    Text(
      text = stringResource(Res.string.ai_chat_context_format, context.displayName),
      style = AppTheme.typography.labelMedium,
      color = onTopBarColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f).padding(start = AppTheme.dimens.smallGap),
    )
  } else {
    Spacer(modifier = Modifier.weight(1f))
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(end = AppTheme.dimens.tinyGap),
  ) {
    IconButton(
      onClick = onHistoryClick,
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
      colors = IconButtonDefaults.iconButtonColors(contentColor = onTopBarColor),
    ) {
      Icon(
        imageVector = Icons.Outlined.History,
        contentDescription = stringResource(Res.string.ai_chat_des_history),
      )
    }

    IconButton(
      onClick = onNewConversation,
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
      enabled = hasMessages,
      colors = IconButtonDefaults.iconButtonColors(contentColor = onTopBarColor),
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = stringResource(Res.string.ai_chat_des_new_conversation),
      )
    }
  }
}
