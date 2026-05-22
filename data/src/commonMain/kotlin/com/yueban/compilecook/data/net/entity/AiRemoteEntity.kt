package com.yueban.compilecook.data.net.entity

data class AiChatRequestMessage(
  val role: String,
  val content: String,
)

data class AiChatRequest(
  val messages: List<AiChatRequestMessage>,
  val systemMessage: String?,
)
