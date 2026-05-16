package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestContext
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.entity.AiContextType
import com.yueban.compilecook.data.net.service.AiRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface AiRepo {
  suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext,
  ): Flow<String>
}

internal class AiRepoImpl(
  private val aiRemoteDataSource: AiRemoteDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiRepo {
  override suspend fun chat(
    messages: List<AiChatMessage>,
    context: AiContext,
  ): Flow<String> {
    val request = AiChatRequest(
      messages = messages.map {
        AiChatRequestMessage(role = it.role.serialName(), content = it.content)
      },
      context = AiChatRequestContext(
        type = context.contextType(),
        name = context.contextName(),
        content = context.content(),
      ),
    )
    return aiRemoteDataSource.chat(request)
  }

  private fun AiContext.contextType(): AiContextType = when (this) {
    is AiContext.Dish -> AiContextType.DISH
    is AiContext.Tip -> AiContextType.TIP
    is AiContext.General -> AiContextType.GENERAL
    is AiContext.DishList -> AiContextType.DISH_LIST
    is AiContext.DishCategory -> AiContextType.DISH_CATEGORY
    is AiContext.DishDifficulty -> AiContextType.DISH_DIFFICULTY
  }

  private fun AiContext.contextName(): String = when (this) {
    is AiContext.Dish -> name
    is AiContext.Tip -> name
    is AiContext.DishCategory -> category.serialName()
    is AiContext.DishDifficulty -> level.toString()
    is AiContext.General,
    is AiContext.DishList,
    -> ""
  }

  private suspend fun AiContext.content(): String = when (this) {
    is AiContext.Dish -> dishLocalDataSource.getDishByName(name).firstOrNull()?.content.orEmpty()
    is AiContext.Tip -> dishLocalDataSource.getTipDetail(name).firstOrNull()?.content.orEmpty()
    else -> ""
  }
}
