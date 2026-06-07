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
  suspend fun getConversationById(id: Long): AiChatConversationLocalEntity?
  suspend fun insertConversation(conversation: AiChatConversationLocalEntity): Long
  suspend fun updateConversationTitle(id: Long, title: String, updatedAt: Long)
  suspend fun updateConversationTimestamp(id: Long, updatedAt: Long)
  suspend fun deleteConversationById(id: Long)
  suspend fun deleteAllConversations()
  fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessageLocalEntity>>
  suspend fun getMessageById(id: Long): AiChatMessageLocalEntity?
  suspend fun getMessageCount(conversationId: Long): Long
  suspend fun insertMessage(message: AiChatMessageLocalEntity): Long
  suspend fun insertMessages(messages: List<AiChatMessageLocalEntity>): List<Long>
  suspend fun updateMessageContent(id: Long, content: String)
  suspend fun updateMessageStatus(id: Long, status: Long)
  suspend fun updateMessageStatusByConversationAndStatus(conversationId: Long, fromStatus: Long, toStatus: Long)
  suspend fun deleteMessageById(id: Long)
  suspend fun deleteMessagesByIds(ids: List<Long>)
  suspend fun deleteMessagesByConversationId(conversationId: Long)
  suspend fun deleteAllMessages()
}

class AiChatLocalDataSourceImpl(
  private val aiChatQueries: AiChatQueries,
  defaultDispatcher: CoroutineDispatcher,
) : BaseLocalDataSource(defaultDispatcher), AiChatLocalDataSource {
  override fun getConversations(): Flow<List<AiChatConversationLocalEntity>> =
    aiChatQueries.getConversations().asFlow().mapToList(defaultDispatcher)

  override suspend fun getConversationById(id: Long): AiChatConversationLocalEntity? =
    withContext(defaultDispatcher) { aiChatQueries.getConversationById(id).awaitAsOneOrNull() }

  override suspend fun insertConversation(conversation: AiChatConversationLocalEntity): Long = write {
    aiChatQueries.transactionWithResult {
      aiChatQueries.insertConversation(conversation)
      val id = aiChatQueries.selectLastInsertRowId().awaitAsOne()
      Logger.d("insert conversation: $id")
      id
    }
  }

  override suspend fun updateConversationTitle(id: Long, title: String, updatedAt: Long) = write {
    aiChatQueries.updateConversationTitle(title = title, updatedAt = updatedAt, id = id)
    Logger.d("update conversation title: $id")
  }

  override suspend fun updateConversationTimestamp(id: Long, updatedAt: Long) = write {
    aiChatQueries.updateConversationTimestamp(updatedAt = updatedAt, id = id)
    Logger.d("update conversation timestamp: $id")
  }

  override suspend fun deleteConversationById(id: Long) = write {
    aiChatQueries.deleteMessagesByConversationId(id)
    aiChatQueries.deleteConversationById(id)
    Logger.d("delete conversation: $id")
  }

  override suspend fun deleteAllConversations() = transactionWrite {
    aiChatQueries.deleteAllMessages()
    aiChatQueries.deleteAllConversations()
    Logger.d("delete all conversations and messages")
  }

  override fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessageLocalEntity>> =
    aiChatQueries.getMessagesByConversationId(conversationId).asFlow().mapToList(defaultDispatcher)

  override suspend fun getMessageCount(conversationId: Long): Long =
    withContext(defaultDispatcher) { aiChatQueries.getMessageCount(conversationId).awaitAsOne() }

  override suspend fun getMessageById(id: Long): AiChatMessageLocalEntity? =
    withContext(defaultDispatcher) { aiChatQueries.getMessageById(id).awaitAsOneOrNull() }

  override suspend fun insertMessage(message: AiChatMessageLocalEntity): Long = write {
    aiChatQueries.transactionWithResult {
      aiChatQueries.insertMessage(message)
      val id = aiChatQueries.selectLastInsertRowId().awaitAsOne()
      Logger.d("insert message: $id")
      id
    }
  }

  override suspend fun insertMessages(messages: List<AiChatMessageLocalEntity>): List<Long> = transactionWrite {
    aiChatQueries.transactionWithResult {
      val ids = mutableListOf<Long>()
      messages.forEach { message ->
        aiChatQueries.insertMessage(message)
        ids.add(aiChatQueries.selectLastInsertRowId().awaitAsOne())
      }
      Logger.d("insert messages: ${ids.size}")
      ids
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

  override suspend fun deleteMessageById(id: Long) = write {
    aiChatQueries.deleteMessageById(id)
    Logger.d("delete message: $id")
  }

  override suspend fun deleteMessagesByIds(ids: List<Long>) = write {
    aiChatQueries.deleteMessagesByIds(ids)
    Logger.d("delete messages by ids: ${ids.size}")
  }

  override suspend fun deleteMessagesByConversationId(conversationId: Long) = write {
    aiChatQueries.deleteMessagesByConversationId(conversationId)
    Logger.d("delete messages by conversation: $conversationId")
  }

  override suspend fun deleteAllMessages() = write {
    aiChatQueries.deleteAllMessages()
    Logger.d("delete all messages")
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
