package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.AiChatConversationLocalEntity
import com.yueban.compilecook.data.db.entity.AiChatMessageLocalEntity
import com.yueban.compilecook.util.serialName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class AiChatConversation(
  val id: String,
  val title: String,
  val context: AiChatContext,
  val createdAt: Long,
  val updatedAt: Long,
)

data class AiChatMessage(
  val id: String,
  val role: AiChatRole,
  val content: String,
  val timestamp: Long,
)

@Serializable
enum class AiChatRole {
  @SerialName("user") USER,
  @SerialName("assistant") ASSISTANT,
  @SerialName("system") SYSTEM;

  companion object {
    fun fromValue(value: String): AiChatRole = entries.find { it.serialName() == value } ?: USER
  }
}

sealed interface AiChatContext {
  data class Dish(val name: String) : AiChatContext
  data class Tip(val name: String) : AiChatContext
  data object General : AiChatContext
  data object DishList : AiChatContext
  data class DishCategory(val category: com.yueban.compilecook.repo.entity.DishCategory) : AiChatContext
  data class DishDifficulty(val level: Int) : AiChatContext
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
  )

// -- Mapping: Domain -> Local --

fun AiChatConversation.toLocalEntity(): AiChatConversationLocalEntity =
  AiChatConversationLocalEntity(
    id = id,
    title = title,
    contextType = context.toContextType().serialName(),
    contextName = context.toContextName(),
    createdAt = createdAt,
    updatedAt = updatedAt,
  )

fun AiChatMessage.toLocalEntity(conversationId: String): AiChatMessageLocalEntity =
  AiChatMessageLocalEntity(
    id = id,
    conversationId = conversationId,
    role = role.serialName(),
    content = content,
    timestamp = timestamp,
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
