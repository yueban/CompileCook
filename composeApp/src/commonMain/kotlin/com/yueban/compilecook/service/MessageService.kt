package com.yueban.compilecook.service

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.StringResource

sealed interface UiMessage {
  data class Text(val value: String) : UiMessage
  data class Resource(val res: StringResource, val args: List<Any> = emptyList()) : UiMessage
  data class Error(val error: Throwable) : UiMessage
}

interface MessageService {
  val messageFlow: Flow<UiMessage>
  fun showMessage(message: UiMessage)
  fun showMessage(text: String) = showMessage(UiMessage.Text(text))
  fun showError(error: Throwable) = showMessage(UiMessage.Error(error))
}

class DefaultMessageService : MessageService {
  private val channel = Channel<UiMessage>(Channel.BUFFERED)
  override val messageFlow = channel.receiveAsFlow()

  override fun showMessage(message: UiMessage) {
    channel.trySend(message)
  }
}
