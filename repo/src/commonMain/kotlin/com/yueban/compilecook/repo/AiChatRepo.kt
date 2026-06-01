package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.AiChatLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatMessageStatus
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.repo.entity.toAiChatConversation
import com.yueban.compilecook.repo.entity.toAiChatMessage
import com.yueban.compilecook.repo.entity.toLocalEntity
import com.yueban.compilecook.util.currentTimeMillis
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface AiChatRepo {
  suspend fun chat(conversationId: Long, userMessage: String, messages: List<AiChatMessage>, systemMessage: String)
  suspend fun insertUserMessage(conversationId: Long, content: String): Long
  suspend fun getContextContent(context: AiChatContext): String
  fun getConversations(): Flow<List<AiChatConversation>>
  suspend fun getConversationById(id: Long): AiChatConversation?
  fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessage>>
  suspend fun saveConversation(conversation: AiChatConversation): Long
  suspend fun updateConversationTitle(id: Long, title: String, updatedAt: Long)
  suspend fun updateConversationTimestamp(id: Long, updatedAt: Long)
  suspend fun updateMessageContent(id: Long, content: String)
  suspend fun updateMessageStatus(id: Long, status: AiChatMessageStatus)
  suspend fun deleteConversation(id: Long)
  suspend fun deleteMessagesByConversationId(conversationId: Long)
  suspend fun deleteAllConversations()
  suspend fun deleteAllMessages()
}

internal class AiChatRepoImpl(
  private val aiRemoteDataSource: AiChatRemoteDataSource,
  private val aiLocalDataSource: AiChatLocalDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiChatRepo {
  @Suppress("TooGenericExceptionCaught")
  override suspend fun chat(
    conversationId: Long,
    userMessage: String,
    messages: List<AiChatMessage>,
    systemMessage: String,
  ) {
    val assistantPlaceholder = AiChatMessage(
      id = 0L,
      role = AiChatRole.ASSISTANT,
      content = "",
      timestamp = currentTimeMillis,
      status = AiChatMessageStatus.STREAMING,
    )
    val assistantMessageId = aiLocalDataSource.insertMessage(assistantPlaceholder.toLocalEntity(conversationId))

    val request = AiChatRequest(
      messages = messages.map {
        AiChatRequestMessage(role = it.role.serialName(), content = it.content)
      },
      systemMessage = systemMessage,
    )
    try {
      val responseBuilder = StringBuilder()
      aiRemoteDataSource.chat(request).collect { token ->
        responseBuilder.append(token)
        aiLocalDataSource.updateMessageContent(assistantMessageId, responseBuilder.toString())
      }
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.COMPLETED.value.toLong())
    } catch (e: Exception) {
      // TODO: map specific exceptions to appropriate AiChatMessageStatus types
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.UNKNOWN_ERROR.value.toLong())
      throw e
    }
  }

  override suspend fun insertUserMessage(conversationId: Long, content: String): Long {
    val userMsg = AiChatMessage(
      id = 0L,
      role = AiChatRole.USER,
      content = content,
      timestamp = currentTimeMillis,
    )
    return aiLocalDataSource.insertMessage(userMsg.toLocalEntity(conversationId))
  }

  override suspend fun getContextContent(context: AiChatContext): String = when (context) {
    is AiChatContext.Dish -> dishLocalDataSource.getDishByName(context.name).firstOrNull()?.content.orEmpty()
    is AiChatContext.Tip -> dishLocalDataSource.getTipDetail(context.name).firstOrNull()?.content.orEmpty()
    else -> ""
  }

  override fun getConversations(): Flow<List<AiChatConversation>> =
    aiLocalDataSource.getConversations().map { list -> list.map { it.toAiChatConversation() } }

  override suspend fun getConversationById(id: Long): AiChatConversation? =
    aiLocalDataSource.getConversationById(id)?.toAiChatConversation()

  override fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessage>> =
    aiLocalDataSource.getMessagesByConversationId(conversationId).map { list -> list.map { it.toAiChatMessage() } }

  override suspend fun saveConversation(conversation: AiChatConversation): Long =
    aiLocalDataSource.insertConversation(conversation.toLocalEntity())

  override suspend fun updateConversationTitle(id: Long, title: String, updatedAt: Long) =
    aiLocalDataSource.updateConversationTitle(id, title, updatedAt)

  override suspend fun updateConversationTimestamp(id: Long, updatedAt: Long) =
    aiLocalDataSource.updateConversationTimestamp(id, updatedAt)

  override suspend fun updateMessageContent(id: Long, content: String) =
    aiLocalDataSource.updateMessageContent(id, content)

  override suspend fun updateMessageStatus(id: Long, status: AiChatMessageStatus) =
    aiLocalDataSource.updateMessageStatus(id, status.value.toLong())

  override suspend fun deleteConversation(id: Long) =
    aiLocalDataSource.deleteConversationById(id)

  override suspend fun deleteMessagesByConversationId(conversationId: Long) =
    aiLocalDataSource.deleteMessagesByConversationId(conversationId)

  override suspend fun deleteAllConversations() =
    aiLocalDataSource.deleteAllConversations()

  override suspend fun deleteAllMessages() =
    aiLocalDataSource.deleteAllMessages()
}
