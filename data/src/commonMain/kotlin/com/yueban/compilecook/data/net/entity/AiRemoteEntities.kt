package com.yueban.compilecook.data.net.entity

data class AiChatRequestMessage(
  val role: String,
  val content: String,
)

data class AiChatRequest(
  val messages: List<AiChatRequestMessage>,
  val context: AiChatRequestContext?,
)

data class AiChatRequestContext(
  val type: String,
  val name: String,
)
