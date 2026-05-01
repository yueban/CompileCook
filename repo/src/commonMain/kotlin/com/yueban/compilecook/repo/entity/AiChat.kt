package com.yueban.compilecook.repo.entity

data class AiChatMessage(
  val id: String,
  val role: AiChatRole,
  val content: String,
  val timestamp: Long,
)

enum class AiChatRole { USER, ASSISTANT, SYSTEM }

data class AiContext(
  val type: AiContextType,
  val name: String,
  val content: String,
)

enum class AiContextType { DISH, TIP, NONE }
