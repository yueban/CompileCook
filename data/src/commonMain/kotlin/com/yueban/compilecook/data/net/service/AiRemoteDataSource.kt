package com.yueban.compilecook.data.net.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.yueban.compilecook.AIKonfig
import com.yueban.compilecook.data.net.entity.AiChatRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AiRemoteDataSource {
  suspend fun chat(request: AiChatRequest): Flow<String>
}

internal class AiRemoteDataSourceImpl : AiRemoteDataSource {
  private val openAi: OpenAI by lazy {
    OpenAI(
      host = OpenAIHost(baseUrl = AIKonfig.MIMO_BASE_URL),
      token = AIKonfig.MIMO_API_KEY,
    )
  }

  @Suppress("TooGenericExceptionCaught")
  override suspend fun chat(request: AiChatRequest): Flow<String> = flow {
    try {
      val messages = request.messages.map {
        ChatMessage(role = it.role.toChatRole(), content = it.content)
      }
      val systemMessage = request.context?.let {
        ChatMessage(
          role = ChatRole.System,
          content = "You are a cooking assistant. The user is viewing a ${it.type.lowercase()}: ${it.name}.",
        )
      }
      val allMessages = buildList {
        systemMessage?.let { add(it) }
        addAll(messages)
      }
      val chatRequest = ChatCompletionRequest(
        model = ModelId(AIKonfig.MIMO_MODEL),
        messages = allMessages,
      )
      openAi.chatCompletions(chatRequest).collect { chunk ->
        chunk.choices.firstOrNull()?.delta?.content?.let { emit(it) }
      }
    } catch (e: Exception) {
      throw AiChatException("AI request failed: ${e.message}", e)
    }
  }
}

class AiChatException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun String.toChatRole(): ChatRole = when (this) {
  "user" -> ChatRole.User
  "assistant" -> ChatRole.Assistant
  "system" -> ChatRole.System
  else -> ChatRole.User
}
