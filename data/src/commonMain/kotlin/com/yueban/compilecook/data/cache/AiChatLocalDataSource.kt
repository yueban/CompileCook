package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yueban.compilecook.data.db.entity.AiChatConversationLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatMessageLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatQueries
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface AiChatLocalDataSource {
  fun getConversations(): Flow<List<AiChatConversationLocalEntity>>
  suspend fun getConversationById(id: String): AiChatConversationLocalEntity?
  suspend fun upsertConversation(conversation: AiChatConversationLocalEntity)
  suspend fun updateConversationTitle(id: String, title: String, updatedAt: Long)
  suspend fun updateConversationTimestamp(id: String, updatedAt: Long)
  suspend fun deleteConversationById(id: String)
  suspend fun deleteAllConversations()
  fun getMessagesByConversationId(conversationId: String): Flow<List<AiChatMessageLocalEntity>>
  suspend fun getMessageCount(conversationId: String): Long
  suspend fun upsertMessage(message: AiChatMessageLocalEntity)
  suspend fun upsertMessages(messages: List<AiChatMessageLocalEntity>)
  suspend fun deleteMessagesByConversationId(conversationId: String)
  suspend fun deleteAllMessages()
}

class AiChatLocalDataSourceImpl(
  private val aiChatQueries: AiChatQueries,
  defaultDispatcher: CoroutineDispatcher,
) : BaseLocalDataSource(defaultDispatcher), AiChatLocalDataSource {
  override fun getConversations(): Flow<List<AiChatConversationLocalEntity>> =
    aiChatQueries.getConversations().asFlow().mapToList(defaultDispatcher)

  override suspend fun getConversationById(id: String): AiChatConversationLocalEntity? =
    withContext(defaultDispatcher) { aiChatQueries.getConversationById(id).awaitAsOneOrNull() }

  override suspend fun upsertConversation(conversation: AiChatConversationLocalEntity) = write {
    aiChatQueries.upsertConversation(conversation)
    Logger.d("upsert conversation: ${conversation.id}")
  }

  override suspend fun updateConversationTitle(id: String, title: String, updatedAt: Long) = write {
    aiChatQueries.updateConversationTitle(title = title, updatedAt = updatedAt, id = id)
    Logger.d("update conversation title: $id")
  }

  override suspend fun updateConversationTimestamp(id: String, updatedAt: Long) = write {
    aiChatQueries.updateConversationTimestamp(updatedAt = updatedAt, id = id)
    Logger.d("update conversation timestamp: $id")
  }

  override suspend fun deleteConversationById(id: String) = write {
    aiChatQueries.deleteMessagesByConversationId(id)
    aiChatQueries.deleteConversationById(id)
    Logger.d("delete conversation: $id")
  }

  override suspend fun deleteAllConversations() = transactionWrite {
    aiChatQueries.deleteAllMessages()
    aiChatQueries.deleteAllConversations()
    Logger.d("delete all conversations and messages")
  }

  override fun getMessagesByConversationId(conversationId: String): Flow<List<AiChatMessageLocalEntity>> =
    aiChatQueries.getMessagesByConversationId(conversationId).asFlow().mapToList(defaultDispatcher)

  override suspend fun getMessageCount(conversationId: String): Long =
    withContext(defaultDispatcher) { aiChatQueries.getMessageCount(conversationId).awaitAsOne() }

  override suspend fun upsertMessage(message: AiChatMessageLocalEntity) = write {
    aiChatQueries.upsertMessage(message)
    Logger.d("upsert message: ${message.id}")
  }

  override suspend fun upsertMessages(messages: List<AiChatMessageLocalEntity>) = transactionWrite {
    aiChatQueries.transaction {
      messages.forEach { aiChatQueries.upsertMessage(it) }
    }
    Logger.d("upsert messages: ${messages.size}")
  }

  override suspend fun deleteMessagesByConversationId(conversationId: String) = write {
    aiChatQueries.deleteMessagesByConversationId(conversationId)
    Logger.d("delete messages by conversation: $conversationId")
  }

  override suspend fun deleteAllMessages() = write {
    aiChatQueries.deleteAllMessages()
    Logger.d("delete all messages")
  }
}

private suspend fun AiChatQueries.upsertConversation(conversation: AiChatConversationLocalEntity) =
  upsertConversation(
    id = conversation.id,
    title = conversation.title,
    contextType = conversation.contextType,
    contextName = conversation.contextName,
    createdAt = conversation.createdAt,
    updatedAt = conversation.updatedAt,
  )

private suspend fun AiChatQueries.upsertMessage(message: AiChatMessageLocalEntity) =
  upsertMessage(
    id = message.id,
    conversationId = message.conversationId,
    role = message.role,
    content = message.content,
    timestamp = message.timestamp,
  )
