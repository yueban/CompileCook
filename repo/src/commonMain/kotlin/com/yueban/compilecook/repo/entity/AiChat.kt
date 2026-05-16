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

sealed interface AiContext {
  data class Dish(val name: String) : AiContext
  data class Tip(val name: String) : AiContext
  data object General : AiContext
  data object DishList : AiContext
  data class DishCategory(val category: com.yueban.compilecook.repo.entity.DishCategory) : AiContext
  data class DishDifficulty(val level: Int) : AiContext
}
