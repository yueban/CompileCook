package com.yueban.compilecook

import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.util.CoilUtil

object AppInitializer {
  fun init(debug: Boolean) {
    Logger.init(debug)
    CoilUtil.init()
  }
}
