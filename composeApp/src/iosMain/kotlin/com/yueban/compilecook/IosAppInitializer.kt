package com.yueban.compilecook

object IosAppInitializer {
  fun onCreate() {
    AppInitializer.init(BuildKonfig.DEBUG)
  }

  @Suppress("EmptyFunctionBlock")
  fun onTerminate() {
  }
}
