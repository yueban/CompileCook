package com.yueban.compilecook

import com.yueban.compilecook.logger.Logger

object UtilInitializer {
  fun init(debug: Boolean) {
    Logger.init(debug)
  }
}
