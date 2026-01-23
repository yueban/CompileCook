package com.yueban.compilecook.app

import android.app.Application
import com.yueban.compilecook.AppInitializer
import org.koin.android.ext.koin.androidContext

class AndroidApp : Application() {
  override fun onCreate() {
    super.onCreate()
    AppInitializer.init {
      androidContext(this@AndroidApp)
    }
  }
}
