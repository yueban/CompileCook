package com.yueban.compilecook.ui.ai

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.AiChatRepo
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AiChatListState(
  @Transient
  val conversationsAsync: Async<List<AiChatConversation>> = Uninitialized,
)

interface AiChatListComponent : UiStateComponent<AiChatListState> {
  fun onBackClicked()
  fun onConversationSelected(conversationId: Long)
  fun onDeleteConversation(conversationId: Long)

  sealed interface Output {
    data object BackClicked : Output, BackOutput
    data class ConversationSelected(val conversationId: Long) : Output
  }
}

class DefaultAiChatListComponent(
  componentContext: ComponentContext,
  private val aiRepo: AiChatRepo,
  private val onOutput: (AiChatListComponent.Output) -> Unit,
) : AiChatListComponent, UiStateComponentImpl<AiChatListState>(
  componentContext = componentContext,
  initialState = AiChatListState(),
  serializer = AiChatListState.serializer(),
) {
  init {
    aiRepo.getConversations()
      .execute(retainValue = AiChatListState::conversationsAsync) {
        copy(conversationsAsync = it)
      }
  }

  override fun onBackClicked() {
    onOutput(AiChatListComponent.Output.BackClicked)
  }

  override fun onConversationSelected(conversationId: Long) {
    onOutput(AiChatListComponent.Output.ConversationSelected(conversationId))
  }

  override fun onDeleteConversation(conversationId: Long) {
    launch { aiRepo.deleteConversation(conversationId) }
  }
}
