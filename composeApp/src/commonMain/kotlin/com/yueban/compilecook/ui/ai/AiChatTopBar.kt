package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.displayName
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import compilecook.composeapp.generated.resources.ai_chat_context_changed_format
import compilecook.composeapp.generated.resources.ai_chat_context_format
import compilecook.composeapp.generated.resources.ai_chat_des_history
import compilecook.composeapp.generated.resources.ai_chat_des_new_conversation
import compilecook.composeapp.generated.resources.ai_chat_dismiss
import compilecook.composeapp.generated.resources.ai_chat_new_chat
import compilecook.composeapp.generated.resources.ai_chat_new_conversation_confirm_message
import compilecook.composeapp.generated.resources.ai_chat_new_conversation_confirm_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TopBar(
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
