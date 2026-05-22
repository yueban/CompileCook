package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.AiChatLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.toAiChatConversation
import com.yueban.compilecook.repo.entity.toAiChatMessage
import com.yueban.compilecook.repo.entity.toLocalEntity
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface AiChatRepo {
  suspend fun chat(messages: List<AiChatMessage>, systemMessage: String): Flow<String>
  suspend fun getContextContent(context: AiChatContext): String
  fun getConversations(): Flow<List<AiChatConversation>>
  suspend fun getConversationById(id: String): AiChatConversation?
  fun getMessagesByConversationId(conversationId: String): Flow<List<AiChatMessage>>
  suspend fun saveConversation(conversation: AiChatConversation)
  suspend fun saveMessages(messages: List<AiChatMessage>, conversationId: String)
  suspend fun updateConversationTitle(id: String, title: String, updatedAt: Long)
  suspend fun updateConversationTimestamp(id: String, updatedAt: Long)
  suspend fun deleteConversation(id: String)
  suspend fun deleteAllConversations()
  suspend fun deleteAllMessages()
}

internal class AiChatRepoImpl(
  private val aiRemoteDataSource: AiChatRemoteDataSource,
  private val aiLocalDataSource: AiChatLocalDataSource,
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

  override fun getConversations(): Flow<List<AiChatConversation>> =
    aiLocalDataSource.getConversations().map { list -> list.map { it.toAiChatConversation() } }

  override suspend fun getConversationById(id: String): AiChatConversation? =
    aiLocalDataSource.getConversationById(id)?.toAiChatConversation()

  override fun getMessagesByConversationId(conversationId: String): Flow<List<AiChatMessage>> =
    aiLocalDataSource.getMessagesByConversationId(conversationId).map { list -> list.map { it.toAiChatMessage() } }

  override suspend fun saveConversation(conversation: AiChatConversation) =
    aiLocalDataSource.upsertConversation(conversation.toLocalEntity())

  override suspend fun saveMessages(messages: List<AiChatMessage>, conversationId: String) =
    aiLocalDataSource.upsertMessages(messages.map { it.toLocalEntity(conversationId) })

  override suspend fun updateConversationTitle(id: String, title: String, updatedAt: Long) =
    aiLocalDataSource.updateConversationTitle(id, title, updatedAt)

  override suspend fun updateConversationTimestamp(id: String, updatedAt: Long) =
    aiLocalDataSource.updateConversationTimestamp(id, updatedAt)

  override suspend fun deleteConversation(id: String) =
    aiLocalDataSource.deleteConversationById(id)

  override suspend fun deleteAllConversations() =
    aiLocalDataSource.deleteAllConversations()

  override suspend fun deleteAllMessages() =
    aiLocalDataSource.deleteAllMessages()
}
