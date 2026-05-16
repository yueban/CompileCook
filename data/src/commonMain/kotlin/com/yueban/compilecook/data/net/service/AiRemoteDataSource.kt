package com.yueban.compilecook.data.net.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.yueban.compilecook.AIKonfig
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiContextType
import com.yueban.compilecook.logger.openAiLoggingConfig
import compilecook.data.generated.resources.Res
import compilecook.data.generated.resources.ai_system_content_label
import compilecook.data.generated.resources.ai_system_dish_context
import compilecook.data.generated.resources.ai_system_dishcategory_context
import compilecook.data.generated.resources.ai_system_dishdifficulty_context
import compilecook.data.generated.resources.ai_system_dishlist_context
import compilecook.data.generated.resources.ai_system_general_context
import compilecook.data.generated.resources.ai_system_prompt
import compilecook.data.generated.resources.ai_system_tip_context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString

interface AiRemoteDataSource {
  suspend fun chat(request: AiChatRequest): Flow<String>
}

internal class AiRemoteDataSourceImpl : AiRemoteDataSource {
  private val openAi: OpenAI by lazy {
    OpenAI(
      host = OpenAIHost(baseUrl = AIKonfig.MIMO_BASE_URL),
      token = AIKonfig.MIMO_API_KEY,
      logging = openAiLoggingConfig,
    )
  }

  @Suppress("TooGenericExceptionCaught")
  override suspend fun chat(request: AiChatRequest): Flow<String> = flow {
    try {
      val messages = request.messages.map {
        ChatMessage(role = it.role.toChatRole(), content = it.content)
      }
      val systemMessage = request.context?.let {
        val content = buildString {
          append(getString(Res.string.ai_system_prompt))
          when (it.type) {
            AiContextType.GENERAL -> append(getString(Res.string.ai_system_general_context))
            AiContextType.DISH_LIST -> append(getString(Res.string.ai_system_dishlist_context))
            AiContextType.DISH_CATEGORY -> append(getString(Res.string.ai_system_dishcategory_context, it.name))
            AiContextType.DISH_DIFFICULTY -> append(getString(Res.string.ai_system_dishdifficulty_context, it.name))
            AiContextType.DISH -> {
              append(getString(Res.string.ai_system_dish_context, it.name))
              if (it.content.isNotBlank()) {
                append("\n\n")
                append(getString(Res.string.ai_system_content_label))
                append("\n")
                append(it.content)
              }
            }
            AiContextType.TIP -> {
              append(getString(Res.string.ai_system_tip_context, it.name))
              if (it.content.isNotBlank()) {
                append("\n\n")
                append(getString(Res.string.ai_system_content_label))
                append("\n")
                append(it.content)
              }
            }
          }
        }
        ChatMessage(role = ChatRole.System, content = content)
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
