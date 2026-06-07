package com.yueban.compilecook.data.net.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.exception.OpenAIIOException
import com.aallam.openai.api.exception.OpenAIServerException
import com.aallam.openai.api.exception.OpenAITimeoutException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.yueban.compilecook.AIKonfig
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.error.AiChatApiError
import com.yueban.compilecook.data.net.error.AiChatNetworkError
import com.yueban.compilecook.data.net.error.AiChatServerError
import com.yueban.compilecook.data.net.error.AiChatTimeoutError
import com.yueban.compilecook.data.net.error.AiChatUnknownError
import com.yueban.compilecook.logger.openAiLoggingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AiChatRemoteDataSource {
  suspend fun chat(request: AiChatRequest): Flow<String>
}

internal class AiChatRemoteDataSourceImpl : AiChatRemoteDataSource {
  private val openAi: OpenAI by lazy {
    OpenAI(
      host = OpenAIHost(baseUrl = AIKonfig.MIMO_BASE_URL),
      token = AIKonfig.MIMO_API_KEY,
      logging = openAiLoggingConfig,
    )
  }

  @Suppress("TooGenericExceptionCaught")
  override suspend fun chat(request: AiChatRequest): Flow<String> = flow {
    val messages = request.messages.map {
      ChatMessage(role = it.role.toChatRole(), content = it.content)
    }
    val systemMessage = request.systemMessage?.let {
      ChatMessage(role = ChatRole.System, content = it)
    }
    val allMessages = buildList {
      systemMessage?.let { add(it) }
      addAll(messages)
    }
    val chatRequest = ChatCompletionRequest(
      model = ModelId(AIKonfig.MIMO_MODEL),
      messages = allMessages,
    )
    try {
      openAi.chatCompletions(chatRequest).collect { chunk ->
        chunk.choices.firstOrNull()?.delta?.content?.let { emit(it) }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: OpenAITimeoutException) {
      throw AiChatTimeoutError(e)
    } catch (e: OpenAIIOException) {
      throw AiChatNetworkError(e)
    } catch (e: OpenAIServerException) {
      throw AiChatServerError(e)
    } catch (e: OpenAIAPIException) {
      throw AiChatApiError(e)
    } catch (e: Exception) {
      throw AiChatUnknownError(e)
    }
  }
}

private fun String.toChatRole(): ChatRole = when (this) {
  "user" -> ChatRole.User
  "assistant" -> ChatRole.Assistant
  "system" -> ChatRole.System
  else -> ChatRole.User
}
