package com.yueban.compilecook

import android.app.Application

class AndroidApp : Application() {
  override fun onCreate() {
    super.onCreate()
    AppInitializer.init(BuildConfig.DEBUG)
  }
}
