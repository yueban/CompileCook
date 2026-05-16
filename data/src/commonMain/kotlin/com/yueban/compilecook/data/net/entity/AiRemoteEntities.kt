package com.yueban.compilecook.data.net.entity

data class AiChatRequestMessage(
  val role: String,
  val content: String,
)

data class AiChatRequest(
  val messages: List<AiChatRequestMessage>,
  val context: AiChatRequestContext?,
)

enum class AiContextType {
  GENERAL,
  DISH,
  TIP,
  DISH_LIST,
  DISH_CATEGORY,
  DISH_DIFFICULTY,
}

data class AiChatRequestContext(
  val type: AiContextType,
  val name: String,
  val content: String,
)
