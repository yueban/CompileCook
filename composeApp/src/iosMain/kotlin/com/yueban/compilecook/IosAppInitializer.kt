package com.yueban.compilecook

object IosAppInitializer {
  fun onCreate(debug: Boolean) {
    AppInitializer.init(debug)
  }

  @Suppress("EmptyFunctionBlock")
  fun onTerminate() {
  }
}
