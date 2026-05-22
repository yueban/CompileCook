package com.yueban.compilecook.repo.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
  @SerialName("system") SYSTEM,
}

sealed interface AiChatContext {
  data class Dish(val name: String) : AiChatContext
  data class Tip(val name: String) : AiChatContext
  data object General : AiChatContext
  data object DishList : AiChatContext
  data class DishCategory(val category: com.yueban.compilecook.repo.entity.DishCategory) : AiChatContext
  data class DishDifficulty(val level: Int) : AiChatContext
}
