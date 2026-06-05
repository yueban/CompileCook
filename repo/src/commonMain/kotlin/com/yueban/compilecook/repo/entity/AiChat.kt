package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.AiChatConversationLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatMessageLocalEntity
import com.yueban.compilecook.util.serialName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AiChatConversation(
  val id: Long,
  val title: String,
  val context: AiChatContext,
  val createdAt: Long,
  val updatedAt: Long,
)

data class AiChatMessage(
  val id: Long,
  val role: AiChatRole,
  val content: String,
  val timestamp: Long,
  val status: AiChatMessageStatus = AiChatMessageStatus.COMPLETED,
)

// TODO: optimize error display messages for better user experience
@Suppress("MagicNumber")
enum class AiChatMessageStatus(val value: Int) {
  COMPLETED(0),
  STREAMING(1),
  NETWORK_ERROR(2),
  TIMEOUT_ERROR(3),
  SERVER_ERROR(4),
  UNKNOWN_ERROR(99);

  companion object {
    fun fromValue(value: Int): AiChatMessageStatus = entries.find { it.value == value } ?: UNKNOWN_ERROR
  }
}

@Serializable
enum class AiChatRole {
  @SerialName("user") USER,
  @SerialName("assistant") ASSISTANT,
  @SerialName("system") SYSTEM;

  companion object {
    fun fromValue(value: String): AiChatRole = entries.find { it.serialName() == value } ?: USER
  }
}

@Serializable
sealed interface AiChatContext {
  @Serializable data class Dish(val name: String) : AiChatContext
  @Serializable data class Tip(val name: String) : AiChatContext
  @Serializable data object General : AiChatContext
  @Serializable data object DishList : AiChatContext
  @Serializable data class DishCategory(val category: com.yueban.compilecook.repo.entity.DishCategory) : AiChatContext
  @Serializable data class DishDifficulty(val level: Int) : AiChatContext
}

@Serializable
enum class AiChatContextType {
  @SerialName("dish") DISH,
  @SerialName("tip") TIP,
  @SerialName("general") GENERAL,
  @SerialName("dish_list") DISH_LIST,
  @SerialName("dish_category") DISH_CATEGORY,
  @SerialName("dish_difficulty") DISH_DIFFICULTY;

  companion object {
    fun fromValue(value: String): AiChatContextType = entries.find { it.serialName() == value } ?: GENERAL
  }
}

// -- Mapping: Local -> Domain --

fun AiChatConversationLocalEntity.toAiChatConversation(): AiChatConversation =
  AiChatConversation(
    id = id,
    title = title,
    context = toAiChatContext(contextType, contextName),
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

fun AiChatMessageLocalEntity.toAiChatMessage(): AiChatMessage =
  AiChatMessage(
    id = id,
    role = AiChatRole.fromValue(role),
    content = content,
    timestamp = timestamp,
    status = AiChatMessageStatus.fromValue(status.toInt()),
  )

// -- Mapping: Domain -> Local --

fun AiChatConversation.toLocalEntity(): AiChatConversationLocalEntity =
  AiChatConversationLocalEntity(
    id = 0L,
    title = title,
    contextType = context.toContextType().serialName(),
    contextName = context.toContextName(),
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

fun AiChatMessage.toLocalEntity(conversationId: Long): AiChatMessageLocalEntity =
  AiChatMessageLocalEntity(
    id = 0L,
    conversationId = conversationId,
    role = role.serialName(),
    content = content,
    timestamp = timestamp,
    status = status.value.toLong(),
  )

// -- Internal helpers --

private fun AiChatContext.toContextType(): AiChatContextType = when (this) {
  is AiChatContext.Dish -> AiChatContextType.DISH
  is AiChatContext.Tip -> AiChatContextType.TIP
  is AiChatContext.General -> AiChatContextType.GENERAL
  is AiChatContext.DishList -> AiChatContextType.DISH_LIST
  is AiChatContext.DishCategory -> AiChatContextType.DISH_CATEGORY
  is AiChatContext.DishDifficulty -> AiChatContextType.DISH_DIFFICULTY
}

private fun AiChatContext.toContextName(): String = when (this) {
  is AiChatContext.Dish -> name
  is AiChatContext.Tip -> name
  is AiChatContext.DishCategory -> category.serialName()
  is AiChatContext.DishDifficulty -> level.toString()
  else -> ""
}

private fun toAiChatContext(contextType: String, contextName: String): AiChatContext =
  when (AiChatContextType.fromValue(contextType)) {
    AiChatContextType.DISH -> AiChatContext.Dish(contextName)
    AiChatContextType.TIP -> AiChatContext.Tip(contextName)
    AiChatContextType.DISH_LIST -> AiChatContext.DishList
    AiChatContextType.DISH_CATEGORY -> AiChatContext.DishCategory(DishCategory.fromValue(contextName))
    AiChatContextType.DISH_DIFFICULTY -> AiChatContext.DishDifficulty(contextName.toIntOrNull() ?: 1)
    AiChatContextType.GENERAL -> AiChatContext.General
  }
