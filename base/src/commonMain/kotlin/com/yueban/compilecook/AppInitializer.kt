package com.yueban.compilecook

import com.yueban.compilecook.di.BuildConfig
import com.yueban.compilecook.di.coroutineModule
import com.yueban.compilecook.logger.KoinLogger
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.util.CoilUtil
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

object AppInitializer {
  fun init(debug: Boolean, koinInitializer: KoinApplication.() -> Unit = {}) {
    Logger.init(debug)
    CoilUtil.init()
    initKoin(debug, koinInitializer)
  }

  private fun initKoin(debug: Boolean, koinInitializer: KoinApplication.() -> Unit = {}) {
    startKoin {
      koinInitializer()
      logger(KoinLogger(debug))
      modules(
        module {
          single {
            BuildConfig(isDebug = debug)
          }
        },
        coroutineModule,
      )
    }
  }
}
