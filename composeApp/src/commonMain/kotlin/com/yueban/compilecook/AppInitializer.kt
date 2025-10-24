package com.yueban.compilecook

import com.yueban.compilecook.di.BuildConfig
import com.yueban.compilecook.di.coroutineModule
import com.yueban.compilecook.logger.KoinLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

object AppInitializer {
  fun init(koinInitializer: KoinApplication.() -> Unit = {}) {
    UtilInitializer.init(BuildKonfig.DEBUG)
    initKoin(koinInitializer)
  }

  private fun initKoin(koinInitializer: KoinApplication.() -> Unit = {}) {
    startKoin {
      koinInitializer()
      logger(KoinLogger(BuildKonfig.DEBUG))
      modules(
        module {
          single {
            BuildConfig(isDebug = BuildKonfig.DEBUG)
          }
        },
        coroutineModule,
      )
    }
  }
}
