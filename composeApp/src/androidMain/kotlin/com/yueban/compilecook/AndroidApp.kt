package com.yueban.compilecook

import android.app.Application
import org.koin.android.ext.koin.androidContext

class AndroidApp : Application() {
  override fun onCreate() {
    super.onCreate()
    AppInitializer.init {
      androidContext(this@AndroidApp)
    }
  }
}
