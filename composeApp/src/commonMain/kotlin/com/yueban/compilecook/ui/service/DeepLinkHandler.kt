package com.yueban.compilecook.ui.service

import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DeepLinkHandler {
  private val _deepLinkFlow = MutableSharedFlow<String>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val deepLinkFlow = _deepLinkFlow.asSharedFlow()

  fun handleDeepLink(url: String) {
    Logger.d("DeepLinkService received: $url")
    _deepLinkFlow.tryEmit(url)
  }
}
