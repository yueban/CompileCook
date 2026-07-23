package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.util.currentTimeMillis
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import compilecook.composeapp.generated.resources.ai_chat_list_delete_confirm_message
import compilecook.composeapp.generated.resources.ai_chat_list_delete_confirm_title
import compilecook.composeapp.generated.resources.ai_chat_list_des_delete
import compilecook.composeapp.generated.resources.ai_chat_list_empty
import compilecook.composeapp.generated.resources.ai_chat_list_time_days_ago
import compilecook.composeapp.generated.resources.ai_chat_list_time_hours_ago
import compilecook.composeapp.generated.resources.ai_chat_list_time_just_now
import compilecook.composeapp.generated.resources.ai_chat_list_time_minutes_ago
import compilecook.composeapp.generated.resources.ai_chat_list_time_yesterday
import compilecook.composeapp.generated.resources.ai_chat_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiChatListContent(
  component: AiChatListComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorScheme.background)
  ) {
    ListTopBar(onBackClick = component::onBackClicked)

    AsyncContent(
      async = state.conversationsAsync,
      modifier = Modifier.weight(1f),
      emptyContent = {
        Text(
          text = stringResource(Res.string.ai_chat_list_empty),
          style = AppTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.onSurfaceVariant,
        )
      },
    ) { conversations ->
      ConversationList(
        conversations = conversations,
        onConversationClick = component::onConversationSelected,
        onDeleteConversation = component::onDeleteConversation,
      )
    }
  }
}

@Composable
private fun ListTopBar(
  onBackClick: () -> Unit,
) {
  val onTopBarColor = AppTheme.colorScheme.onSurfaceVariant

  AiDrawerTopBar {
    IconButton(
      onClick = onBackClick,
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
      colors = IconButtonDefaults.iconButtonColors(contentColor = onTopBarColor),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
        contentDescription = null,
      )
    }

    Text(
      text = stringResource(Res.string.ai_chat_list_title),
      style = AppTheme.typography.labelMedium,
      color = onTopBarColor,
      modifier = Modifier.padding(start = AppTheme.dimens.smallGap),
    )
  }
}

@Composable
private fun ConversationList(
  conversations: List<AiChatConversation>,
  onConversationClick: (Long) -> Unit,
  onDeleteConversation: (Long) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
  ) {
    items(conversations, key = { it.id }) { conversation ->
      ConversationItem(
        conversation = conversation,
        onClick = { onConversationClick(conversation.id) },
        onDelete = { onDeleteConversation(conversation.id) },
      )
    }
  }
}

@Composable
private fun ConversationItem(
  conversation: AiChatConversation,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog) {
    DeleteConversationDialog(
      onConfirm = {
        showDeleteDialog = false
        onDelete()
      },
      onDismiss = { showDeleteDialog = false },
    )
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(
        horizontal = AppTheme.dimens.screenPadding,
        vertical = AppTheme.dimens.smallGap,
      ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = conversation.title,
        style = AppTheme.typography.titleSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = conversation.context.displayName,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = relativeTimeAgo(conversation.updatedAt),
        style = AppTheme.typography.labelSmall,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )
    }

    IconButton(
      onClick = { showDeleteDialog = true },
      modifier = Modifier.size(AppTheme.dimens.iconLarge),
    ) {
      Icon(
        imageVector = Icons.Outlined.Delete,
        contentDescription = stringResource(Res.string.ai_chat_list_des_delete),
        tint = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun DeleteConversationDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.ai_chat_list_delete_confirm_title)) },
    text = { Text(stringResource(Res.string.ai_chat_list_delete_confirm_message)) },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = stringResource(Res.string.ai_chat_confirm),
          color = AppTheme.colorScheme.error,
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.ai_chat_cancel))
      }
    },
  )
}

@Suppress("MagicNumber")
@Composable
private fun relativeTimeAgo(timestamp: Long): String {
  val now = currentTimeMillis
  val diff = now - timestamp

  val seconds = diff / 1000
  val minutes = seconds / 60
  val hours = minutes / 60
  val days = hours / 24

  return when {
    seconds < 60 -> stringResource(Res.string.ai_chat_list_time_just_now)
    minutes < 60 -> stringResource(Res.string.ai_chat_list_time_minutes_ago, minutes.toInt())
    hours < 24 -> stringResource(Res.string.ai_chat_list_time_hours_ago, hours.toInt())
    hours < 48 -> stringResource(Res.string.ai_chat_list_time_yesterday)
    else -> stringResource(Res.string.ai_chat_list_time_days_ago, days.toInt())
  }
}
