package com.yueban.compilecook.data.net.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.exception.OpenAIAPIException
import com.aallam.openai.api.exception.OpenAIIOException
import com.aallam.openai.api.exception.OpenAIServerException
import com.aallam.openai.api.exception.OpenAITimeoutException
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.error.AiChatApiError
import com.yueban.compilecook.data.net.error.AiChatNetworkError
import com.yueban.compilecook.data.net.error.AiChatServerError
import com.yueban.compilecook.data.net.error.AiChatTimeoutError
import com.yueban.compilecook.data.net.error.AiChatUnknownError
import com.yueban.compilecook.util.ImageFileCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface AiChatRemoteDataSource {
  suspend fun chat(request: AiChatRequest): Flow<String>
}

internal class AiChatRemoteDataSourceImpl(
  private val openAi: OpenAI,
  private val modelId: ModelId,
  private val defaultDispatcher: CoroutineDispatcher,
  private val ioDispatcher: CoroutineDispatcher,
) : AiChatRemoteDataSource {
  @Suppress("TooGenericExceptionCaught")
  override suspend fun chat(request: AiChatRequest): Flow<String> = flow {
    val messages = request.messages.map { it.toChatMessage() }
    val systemMessage = request.systemMessage?.let {
      ChatMessage(role = ChatRole.System, content = it)
    }
    val allMessages = buildList {
      systemMessage?.let { add(it) }
      addAll(messages)
    }
    val chatRequest = ChatCompletionRequest(
      model = modelId,
      messages = allMessages,
    )
    try {
      openAi.chatCompletions(chatRequest).flowOn(ioDispatcher).collect { chunk ->
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
  }.flowOn(defaultDispatcher)
}

@OptIn(ExperimentalEncodingApi::class)
private fun AiChatRequestMessage.toChatMessage(): ChatMessage {
  val chatRole = ChatRole(role)
  return if (imagePaths.isNotEmpty()) {
    val parts = imagePaths.mapNotNull { path ->
      val bytes = ImageFileCache.readBytes(path)
      if (bytes.isEmpty()) null else ImagePart(url = "data:image/jpeg;base64,${Base64.encode(bytes)}")
    } + TextPart(content)
    ChatMessage(role = chatRole, content = parts)
  } else {
    ChatMessage(role = chatRole, content = content)
  }
}
