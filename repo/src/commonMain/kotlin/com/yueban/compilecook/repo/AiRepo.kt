package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestContext
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.repo.entity.AiContextType
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface AiRepo {
  suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext?,
  ): Flow<String>
}

internal class AiRepoImpl(
  private val aiRemoteDataSource: AiRemoteDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiRepo {
  override suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext?,
  ): Flow<String> {
    val contextContent = context?.let { ctx ->
      when (ctx.type) {
        AiContextType.DISH -> dishLocalDataSource.getDishByName(ctx.name).firstOrNull()?.content
        AiContextType.TIP -> dishLocalDataSource.getTipDetail(ctx.name).firstOrNull()?.content
        AiContextType.NONE -> null
      }
    }
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
          content = contextContent ?: "",
        )
      },
    )
    return aiRemoteDataSource.chat(request)
  }
}
