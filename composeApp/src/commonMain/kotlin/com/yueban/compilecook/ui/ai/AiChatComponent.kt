package com.yueban.compilecook.ui.ai

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.AiRepo
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.util.currentTimeMillis
import kotlinx.coroutines.launch

// TODO: persist chat messages across app restarts
data class AiChatState(
  val messages: List<AiChatMessage> = emptyList(),
  val isLoading: Boolean = false,
  val currentContext: AiContext = AiContext.General,
  val pendingContext: AiContext? = null,
)

// TODO: add conversation list management (save/load/switch between multiple conversations)
interface AiChatComponent : UiStateComponent<AiChatState> {
  fun sendMessage(text: String)
  fun clearMessages()
  fun updateContext(context: AiContext)
  fun switchContext()
  fun dismissContextChange()
}

class DefaultAiChatComponent(
  componentContext: ComponentContext,
  private val aiRepo: AiRepo,
) : AiChatComponent, UiStateComponentImpl<AiChatState>(
  componentContext = componentContext,
  initialState = AiChatState(),
) {
  @Suppress("TooGenericExceptionCaught")
  override fun sendMessage(text: String) {
    if (text.isBlank()) return

    val userMessage = AiChatMessage(
      id = "user_$currentTimeMillis",
      role = AiChatRole.USER,
      content = text,
      timestamp = currentTimeMillis,
    )

    setState {
      copy(
        messages = messages + userMessage,
        isLoading = true,
      )
    }

    componentScope.launch {
      try {
        val assistantMessageId = "assistant_$currentTimeMillis"
        val responseBuilder = StringBuilder()

        // TODO: support retrying failed messages
        aiRepo.chat(uiState.value.messages, uiState.value.currentContext).collect { token ->
          responseBuilder.append(token)
          val assistantMessage = AiChatMessage(
            id = assistantMessageId,
            role = AiChatRole.ASSISTANT,
            content = responseBuilder.toString(),
            timestamp = currentTimeMillis,
          )
          setState {
            val existingMessages = messages.filter { it.id != assistantMessageId }
            copy(messages = existingMessages + assistantMessage)
          }
        }
      } catch (e: Exception) {
        Logger.e("AI chat error", e)
        val errorMessage = AiChatMessage(
          id = "error_$currentTimeMillis",
          role = AiChatRole.ASSISTANT,
          content = "Error: ${e.message ?: "Unknown error"}",
          timestamp = currentTimeMillis,
        )
        setState { copy(messages = messages + errorMessage) }
      } finally {
        setState { copy(isLoading = false) }
      }
    }
  }

  override fun clearMessages() {
    setState { copy(messages = emptyList()) }
  }

  override fun updateContext(context: AiContext) {
    setState {
      when {
        context == currentContext -> copy(pendingContext = null)
        messages.isEmpty() -> copy(currentContext = context, pendingContext = null)
        else -> copy(pendingContext = context)
      }
    }
  }

  override fun switchContext() {
    setState {
      val newContext = pendingContext ?: return@setState this
      copy(
        currentContext = newContext,
        pendingContext = null,
        messages = emptyList(),
      )
    }
  }

  override fun dismissContextChange() {
    setState { copy(pendingContext = null) }
  }
}
