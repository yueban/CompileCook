@file:Suppress("TooManyFunctions")

package com.yueban.compilecook.ui.ai

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.AiChatRepo
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.util.getDisplayName
import com.yueban.compilecook.util.ImageCompressor
import com.yueban.compilecook.util.ImageFileCache
import com.yueban.compilecook.util.currentTimeMillis
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_system_content_label
import compilecook.composeapp.generated.resources.ai_system_dish_context
import compilecook.composeapp.generated.resources.ai_system_dishcategory_context
import compilecook.composeapp.generated.resources.ai_system_dishdifficulty_context
import compilecook.composeapp.generated.resources.ai_system_dishlist_context
import compilecook.composeapp.generated.resources.ai_system_general_context
import compilecook.composeapp.generated.resources.ai_system_prompt
import compilecook.composeapp.generated.resources.ai_system_tip_context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.getString

internal const val MAX_IMAGES_PER_MESSAGE = 4

@Serializable
data class AiChatState(
  val conversationId: Long = 0L,
  @Transient val messages: List<AiChatMessage> = emptyList(),
  @Transient val isLoading: Boolean = false,
  val currentContext: AiChatContext = AiChatContext.General,
  @Transient val pendingContext: AiChatContext? = null,
  @Transient val pendingImages: List<String> = emptyList(),
  @Transient val compressingImageCount: Int = 0,
)

interface AiChatComponent : UiStateComponent<AiChatState> {
  val onOutput: (Output) -> Unit
  fun onHistoryClick()
  fun sendMessage(text: String)
  fun retryMessage(assistantMessageId: Long)
  fun clearMessages()
  fun updateContext(context: AiChatContext)
  fun switchContext()
  fun dismissContextChange()
  fun selectConversation(conversationId: Long)
  fun onImageSelected(imageBytes: ByteArray)
  fun removePendingImage(index: Int)
  fun canPickImage(): Boolean

  sealed interface Output {
    data object HistoryClicked : Output
  }
}

@Suppress("TooManyFunctions")
class DefaultAiChatComponent(
  componentContext: ComponentContext,
  private val aiRepo: AiChatRepo,
  override val onOutput: (AiChatComponent.Output) -> Unit,
) : AiChatComponent, UiStateComponentImpl<AiChatState>(
  componentContext = componentContext,
  initialState = AiChatState(),
  serializer = AiChatState.serializer(),
) {
  private var chatJob: Job? = null
  private var compressingImageCount = 0

  init {
    lifecycle.doOnDestroy { cleanupPendingImages() }

    uiState
      .map { it.conversationId }
      .distinctUntilChanged()
      .onEach { if (it != 0L) aiRepo.resetStreamingMessages(it) }
      .flatMapLatest { convId ->
        if (convId == 0L) {
          flowOf(emptyList())
        } else {
          aiRepo.getMessagesByConversationId(convId)
        }
      }
      .onEach { setState { copy(messages = it) } }
      .launchIn(componentScope)

    uiState
      .map { it.conversationId }
      .distinctUntilChanged()
      .flatMapLatest { convId ->
        if (convId == 0L) {
          flowOf(null)
        } else {
          aiRepo.getConversationById(convId)
        }
      }
      .onEach { conversation ->
        if (conversation != null) {
          setState { copy(currentContext = conversation.context) }
        } else if (uiState.value.conversationId != 0L) { // conversation has been deleted
          setState { copy(conversationId = 0L) }
        }
      }
      .launchIn(componentScope)
  }

  override fun onHistoryClick() = onOutput(AiChatComponent.Output.HistoryClicked)

  override fun canPickImage(): Boolean =
    uiState.value.pendingImages.size + compressingImageCount < MAX_IMAGES_PER_MESSAGE

  @Suppress("TooGenericExceptionCaught")
  override fun onImageSelected(imageBytes: ByteArray) {
    if (!canPickImage()) return

    compressingImageCount++
    setState { copy(compressingImageCount = compressingImageCount) }
    componentScope.launch {
      val path = try {
        withContext(Dispatchers.Default) {
          ImageCompressor.compressAndSave(imageBytes)
        }
      } catch (e: CancellationException) {
        compressingImageCount--
        setState { copy(compressingImageCount = compressingImageCount) }
        throw e
      } catch (e: Exception) {
        Logger.e("Image compression failed", e)
        compressingImageCount--
        setState { copy(compressingImageCount = compressingImageCount) }
        return@launch
      }
      compressingImageCount--
      setState { copy(pendingImages = pendingImages + path, compressingImageCount = compressingImageCount) }
    }
  }

  override fun removePendingImage(index: Int) {
    val path = uiState.value.pendingImages.getOrNull(index) ?: return
    ImageFileCache.delete(path)
    setState { copy(pendingImages = pendingImages.toMutableList().apply { removeAt(index) }) }
  }

  @Suppress("TooGenericExceptionCaught")
  override fun sendMessage(text: String) {
    if ((text.isBlank() && uiState.value.pendingImages.isEmpty()) || uiState.value.isLoading) return

    val imagePaths = uiState.value.pendingImages
    setState { copy(isLoading = true, pendingImages = emptyList()) }

    chatJob = componentScope.launch {
      try {
        val conversationId = getOrCreateConversationId()
        val messages = uiState.value.messages // snapshot BEFORE insert to avoid stale read
        val systemMessage = buildSystemMessage(uiState.value.currentContext)
        aiRepo.chat(conversationId, text, imagePaths, messages, systemMessage)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Logger.e("AI chat error", e)
      } finally {
        setState { copy(isLoading = false) }
      }
    }
  }

  @Suppress("TooGenericExceptionCaught")
  override fun retryMessage(assistantMessageId: Long) {
    if (uiState.value.isLoading) return

    setState { copy(isLoading = true) }

    chatJob = componentScope.launch {
      try {
        val systemMessage = buildSystemMessage(uiState.value.currentContext)
        aiRepo.retryMessage(assistantMessageId, systemMessage)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Logger.e("AI retry error", e)
      } finally {
        setState { copy(isLoading = false) }
      }
    }
  }

  override fun clearMessages() {
    chatJob?.cancel()
    chatJob = null
    val convId = uiState.value.conversationId
    cleanupPendingImages()
    setState { copy(isLoading = false, pendingImages = emptyList()) }
    if (convId != 0L) {
      componentScope.launch {
        aiRepo.deleteMessagesByConversationId(convId)
      }
    }
  }

  override fun updateContext(context: AiChatContext) {
    setState {
      when {
        context == currentContext -> copy(pendingContext = null)
        messages.isEmpty() -> copy(currentContext = context, pendingContext = null)
        else -> copy(pendingContext = context)
      }
    }
  }

  override fun switchContext() {
    chatJob?.cancel()
    chatJob = null
    cleanupPendingImages()
    setState {
      val newContext = pendingContext ?: return@setState this
      copy(
        conversationId = 0L,
        currentContext = newContext,
        pendingContext = null,
        isLoading = false,
        pendingImages = emptyList(),
      )
    }
  }

  override fun dismissContextChange() {
    setState { copy(pendingContext = null) }
  }

  override fun selectConversation(conversationId: Long) {
    chatJob?.cancel()
    chatJob = null
    cleanupPendingImages()
    setState {
      copy(
        conversationId = conversationId,
        pendingContext = null,
        isLoading = false,
        pendingImages = emptyList(),
      )
    }
  }

  private fun cleanupPendingImages() {
    uiState.value.pendingImages.forEach { ImageFileCache.delete(it) }
  }

  private suspend fun getOrCreateConversationId(): Long {
    val existing = uiState.value.conversationId
    if (existing != 0L) return existing
    val context = uiState.value.currentContext
    val conversation = AiChatConversation(
      id = 0L,
      title = context.getDisplayName(),
      context = context,
      createdAt = currentTimeMillis,
      updatedAt = currentTimeMillis,
    )
    val newId = aiRepo.saveConversation(conversation)
    setState { copy(conversationId = newId) }
    return newId
  }

  private suspend fun buildSystemMessage(context: AiChatContext): String = buildString {
    append(getString(Res.string.ai_system_prompt))
    when (context) {
      AiChatContext.General -> append(getString(Res.string.ai_system_general_context))
      AiChatContext.DishList -> append(getString(Res.string.ai_system_dishlist_context))
      is AiChatContext.DishCategory -> append(
        getString(
          Res.string.ai_system_dishcategory_context,
          context.getDisplayName()
        )
      )
      is AiChatContext.DishDifficulty -> append(
        getString(
          Res.string.ai_system_dishdifficulty_context,
          context.level.toString()
        )
      )
      is AiChatContext.Dish -> {
        append(getString(Res.string.ai_system_dish_context, context.name))
        val content = aiRepo.getContextContent(context)
        if (content.isNotBlank()) {
          append("\n\n")
          append(getString(Res.string.ai_system_content_label))
          append("\n")
          append(content)
        }
      }
      is AiChatContext.Tip -> {
        append(getString(Res.string.ai_system_tip_context, context.name))
        val content = aiRepo.getContextContent(context)
        if (content.isNotBlank()) {
          append("\n\n")
          append(getString(Res.string.ai_system_content_label))
          append("\n")
          append(content)
        }
      }
    }
  }
}
