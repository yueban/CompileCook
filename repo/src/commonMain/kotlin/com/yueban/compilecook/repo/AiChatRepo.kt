package com.yueban.compilecook.repo

import com.yueban.compilecook.data.cache.AiChatLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.net.entity.AiChatRequest
import com.yueban.compilecook.data.net.entity.AiChatRequestMessage
import com.yueban.compilecook.data.net.error.AiChatApiError
import com.yueban.compilecook.data.net.error.AiChatError
import com.yueban.compilecook.data.net.error.AiChatNetworkError
import com.yueban.compilecook.data.net.error.AiChatServerError
import com.yueban.compilecook.data.net.error.AiChatTimeoutError
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.repo.entity.AiChatConversation
import com.yueban.compilecook.repo.entity.AiChatMessage
import com.yueban.compilecook.repo.entity.AiChatMessageStatus
import com.yueban.compilecook.repo.entity.AiChatRole
import com.yueban.compilecook.repo.entity.toAiChatConversation
import com.yueban.compilecook.repo.entity.toAiChatMessage
import com.yueban.compilecook.repo.entity.toAiChatMessages
import com.yueban.compilecook.repo.entity.toLocalEntity
import com.yueban.compilecook.util.currentTimeMillis
import com.yueban.compilecook.util.serialName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface AiChatRepo {
  suspend fun chat(
    conversationId: Long,
    userContent: String,
    imagePaths: List<String>,
    messages: List<AiChatMessage>,
    systemMessage: String,
  )

  suspend fun retryMessage(assistantMessageId: Long, systemMessage: String)
  suspend fun getContextContent(context: AiChatContext): String
  fun getConversations(): Flow<List<AiChatConversation>>
  fun getConversationById(id: Long): Flow<AiChatConversation?>
  fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessage>>
  suspend fun saveConversation(conversation: AiChatConversation): Long
  suspend fun deleteConversation(id: Long)
  suspend fun deleteMessagesByConversationId(conversationId: Long)
  suspend fun resetStreamingMessages(conversationId: Long)
}

internal class AiChatRepoImpl(
  private val aiRemoteDataSource: AiChatRemoteDataSource,
  private val aiLocalDataSource: AiChatLocalDataSource,
  private val dishLocalDataSource: DishLocalDataSource,
) : AiChatRepo {
  override suspend fun chat(
    conversationId: Long,
    userContent: String,
    imagePaths: List<String>,
    messages: List<AiChatMessage>,
    systemMessage: String,
  ) {
    val userMsg = AiChatMessage(
      id = 0L,
      role = AiChatRole.USER,
      content = userContent,
      images = imagePaths,
      timestamp = currentTimeMillis,
    )
    aiLocalDataSource.insertMessageWithImages(userMsg.toLocalEntity(conversationId), imagePaths)

    val assistantPlaceholder = AiChatMessage(
      id = 0L,
      role = AiChatRole.ASSISTANT,
      content = "",
      timestamp = currentTimeMillis,
      status = AiChatMessageStatus.STREAMING,
    )
    val assistantMessageId = aiLocalDataSource.insertMessage(assistantPlaceholder.toLocalEntity(conversationId))

    // Include historical images for all messages
    val requestMessages = messages.map {
      AiChatRequestMessage(role = it.role.serialName(), content = it.content, imagePaths = it.images)
    } +
      AiChatRequestMessage(role = AiChatRole.USER.serialName(), content = userContent, imagePaths = imagePaths)
    val request = AiChatRequest(messages = requestMessages, systemMessage = systemMessage)
    doChat(assistantMessageId, request)
  }

  override suspend fun retryMessage(assistantMessageId: Long, systemMessage: String) {
    val localEntity = aiLocalDataSource.getMessageById(assistantMessageId) ?: return
    val target = localEntity.toAiChatMessage()
    val isValidTarget = target.role == AiChatRole.ASSISTANT &&
      target.status != AiChatMessageStatus.COMPLETED &&
      target.status != AiChatMessageStatus.STREAMING
    val allMessages = if (isValidTarget) {
      aiLocalDataSource.getMessagesWithImagesByConversationId(localEntity.conversationId)
        .firstOrNull().orEmpty()
        .toAiChatMessages()
    } else {
      emptyList()
    }
    val targetIndex = allMessages.indexOfFirst { it.id == assistantMessageId }
    if (!isValidTarget || targetIndex < 0) return

    val idsToDelete = allMessages.drop(targetIndex + 1).map { it.id }
    if (idsToDelete.isNotEmpty()) {
      aiLocalDataSource.deleteMessagesByIds(idsToDelete)
    }

    // Include historical images for all messages
    val requestMessages = allMessages.take(targetIndex).map {
      AiChatRequestMessage(role = it.role.serialName(), content = it.content, imagePaths = it.images)
    }

    aiLocalDataSource.updateMessageContent(assistantMessageId, "")
    aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.STREAMING.value.toLong())
    aiLocalDataSource.updateConversationTimestamp(updatedAt = currentTimeMillis, id = localEntity.conversationId)
    val request = AiChatRequest(messages = requestMessages, systemMessage = systemMessage)
    doChat(assistantMessageId, request)
  }

  @Suppress("TooGenericExceptionCaught")
  private suspend fun doChat(
    assistantMessageId: Long,
    request: AiChatRequest,
  ) {
    try {
      val responseBuilder = StringBuilder()
      aiRemoteDataSource.chat(request).collect { token ->
        responseBuilder.append(token)
        aiLocalDataSource.updateMessageContent(assistantMessageId, responseBuilder.toString())
      }
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.COMPLETED.value.toLong())
    } catch (e: AiChatTimeoutError) {
      Logger.e("AI chat timeout", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.TIMEOUT_ERROR.value.toLong())
    } catch (e: AiChatNetworkError) {
      Logger.e("AI chat network error", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.NETWORK_ERROR.value.toLong())
    } catch (e: AiChatServerError) {
      Logger.e("AI chat server error", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.SERVER_ERROR.value.toLong())
    } catch (e: AiChatApiError) {
      Logger.e("AI chat API error", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.UNKNOWN_ERROR.value.toLong())
    } catch (e: AiChatError) {
      Logger.e("AI chat unknown error", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.UNKNOWN_ERROR.value.toLong())
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e("AI chat unexpected error", e)
      aiLocalDataSource.updateMessageStatus(assistantMessageId, AiChatMessageStatus.UNKNOWN_ERROR.value.toLong())
    }
  }

  override suspend fun getContextContent(context: AiChatContext): String = when (context) {
    is AiChatContext.Dish -> dishLocalDataSource.getDishByName(context.name).firstOrNull()?.content.orEmpty()
    is AiChatContext.Tip -> dishLocalDataSource.getTipDetail(context.name).firstOrNull()?.content.orEmpty()
    else -> ""
  }

  override fun getConversations(): Flow<List<AiChatConversation>> =
    aiLocalDataSource.getConversations().map { list -> list.map { it.toAiChatConversation() } }

  override fun getConversationById(id: Long): Flow<AiChatConversation?> =
    aiLocalDataSource.getConversationById(id).map { it?.toAiChatConversation() }

  override fun getMessagesByConversationId(conversationId: Long): Flow<List<AiChatMessage>> =
    aiLocalDataSource.getMessagesWithImagesByConversationId(conversationId).map { it.toAiChatMessages() }

  override suspend fun saveConversation(conversation: AiChatConversation): Long =
    aiLocalDataSource.insertConversation(conversation.toLocalEntity())

  override suspend fun deleteConversation(id: Long) =
    aiLocalDataSource.deleteConversationById(id)

  override suspend fun deleteMessagesByConversationId(conversationId: Long) =
    aiLocalDataSource.deleteMessagesByConversationId(conversationId)

  override suspend fun resetStreamingMessages(conversationId: Long) {
    aiLocalDataSource.updateMessageStatusByConversationAndStatus(
      conversationId = conversationId,
      fromStatus = AiChatMessageStatus.STREAMING.value.toLong(),
      toStatus = AiChatMessageStatus.CANCELLED.value.toLong(),
    )
  }
}
