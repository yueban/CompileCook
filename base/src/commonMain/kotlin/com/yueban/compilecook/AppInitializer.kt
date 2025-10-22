package com.yueban.compilecook

import com.yueban.compilecook.logger.Logger

object AppInitializer {
  fun init(debug: Boolean) {
    Logger.init(debug)
  }
}
