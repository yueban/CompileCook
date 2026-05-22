package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface AiChatRepo {
  suspend fun chat(messages: List<AiChatMessage>, systemMessage: String): Flow<String>
  suspend fun getContextContent(context: AiChatContext): String
}

internal class AiChatRepoImpl(
  private val aiRemoteDataSource: AiChatRemoteDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiChatRepo {
  override suspend fun chat(messages: List<AiChatMessage>, systemMessage: String): Flow<String> {
    val request = AiChatRequest(
      messages = messages.map {
        AiChatRequestMessage(role = it.role.serialName(), content = it.content)
      },
      systemMessage = systemMessage,
    )
    return aiRemoteDataSource.chat(request)
  }

  override suspend fun getContextContent(context: AiChatContext): String = when (context) {
    is AiChatContext.Dish -> dishLocalDataSource.getDishByName(context.name).firstOrNull()?.content.orEmpty()
    is AiChatContext.Tip -> dishLocalDataSource.getTipDetail(context.name).firstOrNull()?.content.orEmpty()
    else -> ""
  }
}
