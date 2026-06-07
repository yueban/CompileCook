package com.yueban.compilecook.data.net.error

sealed class AiChatError(cause: Throwable? = null) : Exception(cause)
class AiChatTimeoutError(cause: Throwable? = null) : AiChatError(cause)
class AiChatNetworkError(cause: Throwable? = null) : AiChatError(cause)
class AiChatServerError(cause: Throwable? = null) : AiChatError(cause)
class AiChatApiError(cause: Throwable? = null) : AiChatError(cause)
class AiChatUnknownError(cause: Throwable? = null) : AiChatError(cause)
