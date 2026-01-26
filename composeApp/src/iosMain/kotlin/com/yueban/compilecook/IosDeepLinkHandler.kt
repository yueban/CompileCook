package com.yueban.compilecook

import com.yueban.compilecook.ui.service.DeepLinkHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object IosDeepLinkHandler : KoinComponent {
  private val handler: DeepLinkHandler by inject()

  fun handle(url: String) {
    handler.handleDeepLink(url)
  }
}
