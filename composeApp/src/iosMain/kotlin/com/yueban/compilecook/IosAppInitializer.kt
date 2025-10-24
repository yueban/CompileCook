package com.yueban.compilecook

object IosAppInitializer {
  fun onCreate() {
    AppInitializer.init()
  }

  @Suppress("EmptyFunctionBlock")
  fun onTerminate() {
  }
}
