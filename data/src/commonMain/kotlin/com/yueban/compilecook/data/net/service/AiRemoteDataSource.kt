package com.yueban.compilecook.data.net.service

import com.yueban.compilecook.data.net.entity.AiChatRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AiRemoteDataSource {
  suspend fun chat(request: AiChatRequest): Flow<String>
}

internal class AiRemoteDataSourceImpl : AiRemoteDataSource {
  override suspend fun chat(request: AiChatRequest): Flow<String> = flow {
    val lastUserMessage = request.messages.lastOrNull { it.role == "user" }?.content ?: ""

    // TODO: Replace with real API call (e.g. SSE streaming)
    val response = buildString {
      append("This is a placeholder AI response. ")
      append("You said: \"$lastUserMessage\"")
      request.context?.let {
        append(". Current context: ${it.type} - ${it.name}")
      }
      append(". Full AI integration coming soon.")
    }

    for (char in response) {
      @Suppress("MagicNumber")
      delay(20)
      emit(char.toString())
    }
  }
}
