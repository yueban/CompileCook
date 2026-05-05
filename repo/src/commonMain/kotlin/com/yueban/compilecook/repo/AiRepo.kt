package com.yueban.compilecook.repo

import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestContext
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow

interface AiRepo {
  suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext?,
  ): Flow<String>
}

internal class AiRepoImpl(
  private val aiRemoteDataSource: AiRemoteDataSource,
) : AiRepo {
  override suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext?,
  ): Flow<String> {
    val request = AiChatRequest(
      messages = messages.map {
        AiChatRequestMessage(
          role = it.role.serialName(),
          content = it.content,
        )
      },
      context = context?.let {
        AiChatRequestContext(
          type = it.type.name,
          name = it.name,
        )
      },
    )
    return aiRemoteDataSource.chat(request)
  }
}
