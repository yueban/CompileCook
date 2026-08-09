package com.yueban.compilecook.data.net.entity

data class AiChatRequestMessage(
  val role: String,
  val content: String,
  val imageRefs: List<String> = emptyList(),
)

data class AiChatRequest(
  val messages: List<AiChatRequestMessage>,
  val systemMessage: String?,
)
