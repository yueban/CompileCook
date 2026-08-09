package com.yueban.compilecook.app

import android.app.Application
import com.yueban.compilecook.AppInitializer
import com.yueban.compilecook.util.ImageFileStore
import org.koin.android.ext.koin.androidContext

class AndroidApp : Application() {
  override fun onCreate() {
    super.onCreate()
    ImageFileStore.init(this)
    AppInitializer.init {
      androidContext(this@AndroidApp)
    }
  }
}
