package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface AiRepo {
  suspend fun chat(messages: List<AiChatMessage>, systemMessage: String): Flow<String>
  suspend fun getContextContent(context: AiContext): String
}

internal class AiRepoImpl(
  private val aiRemoteDataSource: AiRemoteDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiRepo {
  override suspend fun chat(messages: List<AiChatMessage>, systemMessage: String): Flow<String> {
    val request = AiChatRequest(
      messages = messages.map {
        AiChatRequestMessage(role = it.role.serialName(), content = it.content)
      },
      systemMessage = systemMessage,
    )
    return aiRemoteDataSource.chat(request)
  }

  override suspend fun getContextContent(context: AiContext): String = when (context) {
    is AiContext.Dish -> dishLocalDataSource.getDishByName(context.name).firstOrNull()?.content.orEmpty()
    is AiContext.Tip -> dishLocalDataSource.getTipDetail(context.name).firstOrNull()?.content.orEmpty()
    else -> ""
  }
}
