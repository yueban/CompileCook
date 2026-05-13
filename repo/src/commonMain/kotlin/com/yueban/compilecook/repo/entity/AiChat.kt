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

data class AiContext(
  val type: AiContextType,
  val name: String,
)

enum class AiContextType { DISH, TIP, NONE }
