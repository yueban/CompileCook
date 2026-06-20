package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.service.MessageService
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_des_camera
import compilecook.composeapp.generated.resources.ai_chat_des_send
import compilecook.composeapp.generated.resources.ai_chat_image_limit_reached
import compilecook.composeapp.generated.resources.ai_chat_input_hint
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AiChatContent(
  component: AiChatComponent,
  modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsStateWithLifecycle()
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  var showImagePickerSheet by remember { mutableStateOf(false) }
  var showImageLimitReached by remember { mutableStateOf(false) }
  val messageService: MessageService = koinInject()

  val imagePickerManager = rememberImagePickerManager { bytes ->
    component.onImageSelected(bytes)
  }

  LaunchedEffect(showImageLimitReached) {
    if (showImageLimitReached) {
      val message = getString(Res.string.ai_chat_image_limit_reached, MAX_IMAGES_PER_MESSAGE)
      messageService.showMessage(message)
      showImageLimitReached = false
    }
  }

  ChatAutoScroll(listState = listState, messages = state.messages)

  if (showImagePickerSheet) {
    ImagePickerSheet(
      isCameraAvailable = imagePickerManager.isCameraAvailable(),
      onTakePhoto = imagePickerManager::capturePhoto,
      onPickFromGallery = imagePickerManager::pickFromGallery,
      onDismiss = { showImagePickerSheet = false },
    )
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

    Column(
      modifier = Modifier.fillMaxWidth().padding(AppTheme.dimens.smallGap),
    ) {
      ImagePreviewRow(
        pendingImages = state.pendingImages,
        compressingImageCount = state.compressingImageCount,
        onRemoveImage = component::removePendingImage,
      )

      ChatInputArea(
        inputText = inputText,
        onInputChange = { inputText = it },
        canSend = (inputText.isNotBlank() || state.pendingImages.isNotEmpty()) && !state.isLoading,
        onSend = {
          component.sendMessage(inputText)
          inputText = ""
        },
        onCameraClick = {
          if (component.canPickImage()) {
            showImagePickerSheet = true
          } else {
            showImageLimitReached = true
          }
        },
        canPickImage = component.canPickImage(),
      )
    }
  }
}

@Composable
private fun ImagePreviewRow(
  pendingImages: List<String>,
  compressingImageCount: Int,
  onRemoveImage: (Int) -> Unit,
) {
  if (pendingImages.isEmpty() && compressingImageCount == 0) return

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(bottom = AppTheme.dimens.smallGap),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap),
  ) {
    pendingImages.forEachIndexed { index, imagePath ->
      PendingImageThumbnail(
        imagePath = imagePath,
        onRemove = { onRemoveImage(index) },
      )
    }
    repeat(compressingImageCount) {
      CompressingImagePlaceholder()
    }
  }
}

@Composable
private fun ChatInputArea(
  inputText: String,
  onInputChange: (String) -> Unit,
  canSend: Boolean,
  onSend: () -> Unit,
  onCameraClick: () -> Unit,
  canPickImage: Boolean = true,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap),
  ) {
    OutlinedTextField(
      value = inputText,
      onValueChange = onInputChange,
      modifier = Modifier.weight(1f).onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
          if (event.isShiftPressed) {
            false // Shift+Enter → default newline behavior
          } else {
            if (canSend) onSend()
            true // always consume plain Enter to prevent newline
          }
        } else {
          false
        }
      },
      placeholder = { Text(stringResource(Res.string.ai_chat_input_hint)) },
      maxLines = 3,
      shape = RoundedCornerShape(AppTheme.dimens.aiChatInputFieldRadius),
    )

    IconButton(
      onClick = onCameraClick,
      modifier = Modifier.size(AppTheme.dimens.iconExtraLarge),
      enabled = canPickImage,
    ) {
      Icon(
        imageVector = Icons.Default.CameraAlt,
        contentDescription = stringResource(Res.string.ai_chat_des_camera),
        tint = if (canPickImage) AppTheme.colorScheme.primary else AppTheme.colorScheme.onSurfaceVariant,
      )
    }

    IconButton(
      onClick = onSend,
      modifier = Modifier.size(AppTheme.dimens.iconExtraLarge),
      enabled = canSend,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.Send,
        contentDescription = stringResource(Res.string.ai_chat_des_send),
        tint = if (canSend) AppTheme.colorScheme.primary else AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
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
private fun ChatAutoScroll(listState: LazyListState, messages: List<AiChatMessage>) {
  // user sends a message — always scroll to bottom
  val userMessageCount = messages.count { it.role == AiChatRole.USER }
  LaunchedEffect(userMessageCount) {
    if (userMessageCount > 0) {
      listState.scrollToItem(messages.size)
    }
  }

  // auto scroll on last message changing
  val lastMessage = messages.lastOrNull()
  LaunchedEffect(lastMessage) {
    if (lastMessage == null || lastMessage.role != AiChatRole.ASSISTANT) return@LaunchedEffect

    val layoutInfo = listState.layoutInfo
    val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@LaunchedEffect
    val lastMessageIndex = messages.size
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
}
