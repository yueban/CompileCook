package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yueban.compilecook.data.db.entity.AiChatConversationLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatMessageLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatQueries
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface AiChatLocalDataSource {
  fun getConversations(): Flow<List<AiChatConversationLocalEntity>>
  fun getConversationById(id: Long): Flow<AiChatConversationLocalEntity?>
  suspend fun insertConversation(conversation: AiChatConversationLocalEntity): Long
  suspend fun deleteConversationById(id: Long)
  fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessageLocalEntity>>
  suspend fun getMessageById(id: Long): AiChatMessageLocalEntity?
  suspend fun insertMessage(message: AiChatMessageLocalEntity): Long
  suspend fun updateMessageContent(id: Long, content: String)
  suspend fun updateMessageStatus(id: Long, status: Long)
  suspend fun updateMessageStatusByConversationAndStatus(conversationId: Long, fromStatus: Long, toStatus: Long)
  suspend fun deleteMessagesByIds(ids: List<Long>)
  suspend fun deleteMessagesByConversationId(conversationId: Long)
}

class AiChatLocalDataSourceImpl(
  private val aiChatQueries: AiChatQueries,
  defaultDispatcher: CoroutineDispatcher,
) : BaseLocalDataSource(defaultDispatcher), AiChatLocalDataSource {
  override fun getConversations(): Flow<List<AiChatConversationLocalEntity>> =
    aiChatQueries.getConversations().asFlow().mapToList(defaultDispatcher)

  override fun getConversationById(id: Long): Flow<AiChatConversationLocalEntity?> =
    aiChatQueries.getConversationById(id).asFlow().mapToOneOrNull(defaultDispatcher)

  override suspend fun insertConversation(conversation: AiChatConversationLocalEntity): Long = write {
    aiChatQueries.transactionWithResult {
      aiChatQueries.insertConversation(conversation)
      val id = aiChatQueries.selectLastInsertRowId().awaitAsOne()
      Logger.d("insert conversation: $id")
      id
    }
  }

  override suspend fun deleteConversationById(id: Long) = write {
    aiChatQueries.deleteConversationById(id)
    Logger.d("delete conversation: $id")
  }

  override fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessageLocalEntity>> =
    aiChatQueries.getMessagesByConversationId(conversationId).asFlow().mapToList(defaultDispatcher)

  override suspend fun getMessageById(id: Long): AiChatMessageLocalEntity? =
    withContext(defaultDispatcher) { aiChatQueries.getMessageById(id).awaitAsOneOrNull() }

  override suspend fun insertMessage(message: AiChatMessageLocalEntity): Long = write {
    aiChatQueries.transactionWithResult {
      aiChatQueries.insertMessage(message)
      val id = aiChatQueries.selectLastInsertRowId().awaitAsOne()
      aiChatQueries.updateConversationTimestamp(updatedAt = message.timestamp, id = message.conversationId)
      Logger.d("insert message: $id")
      id
    }
  }

  override suspend fun updateMessageContent(id: Long, content: String) = write {
    aiChatQueries.updateMessageContent(content = content, id = id)
    Logger.d("update message content: $id")
  }

  override suspend fun updateMessageStatus(id: Long, status: Long) = write {
    aiChatQueries.updateMessageStatus(status = status, id = id)
    Logger.d("update message status: $id")
  }

  override suspend fun updateMessageStatusByConversationAndStatus(
    conversationId: Long,
    fromStatus: Long,
    toStatus: Long,
  ) = write {
    aiChatQueries.updateMessageStatusByConversationAndStatus(
      newStatus = toStatus,
      conversationId = conversationId,
      status = fromStatus,
    )
    Logger.d("update message status by conversation: $conversationId, from=$fromStatus, to=$toStatus")
  }

  override suspend fun deleteMessagesByIds(ids: List<Long>) = write {
    aiChatQueries.deleteMessagesByIds(ids)
    Logger.d("delete messages by ids: ${ids.size}")
  }

  override suspend fun deleteMessagesByConversationId(conversationId: Long) = write {
    aiChatQueries.deleteMessagesByConversationId(conversationId)
    Logger.d("delete messages by conversation: $conversationId")
  }
}

private suspend fun AiChatQueries.insertConversation(conversation: AiChatConversationLocalEntity) =
  insertConversation(
    title = conversation.title,
    contextType = conversation.contextType,
    contextName = conversation.contextName,
    createdAt = conversation.createdAt,
    updatedAt = conversation.updatedAt,
  )

private suspend fun AiChatQueries.insertMessage(message: AiChatMessageLocalEntity) =
  insertMessage(
    conversationId = message.conversationId,
    role = message.role,
    content = message.content,
    timestamp = message.timestamp,
    status = message.status,
  )
