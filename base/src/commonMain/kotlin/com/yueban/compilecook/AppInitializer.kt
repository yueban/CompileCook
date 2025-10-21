package com.yueban.compilecook

import com.yueban.compilecook.util.Logger

object AppInitializer {
  fun init(debug: Boolean) {
    Logger.init(debug)
  }
}
