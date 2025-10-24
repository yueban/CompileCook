package com.yueban.compilecook

import com.yueban.compilecook.di.BuildConfig
import com.yueban.compilecook.di.coroutineModule
import com.yueban.compilecook.logger.KoinLogger
import com.yueban.compilecook.repo.di.initialDatabaseModule
import com.yueban.compilecook.repo.di.loadDatabaseModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppInitializerSignal {
  private val _isReady = MutableStateFlow(false)
  val isReady = _isReady.asStateFlow()

  fun complete() {
    _isReady.value = true
  }
}

val initialAppModule = module {
  single { AppInitializerSignal() }
}

object AppInitializer {
  fun init(koinInitializer: KoinApplication.() -> Unit = {}) {
    UtilInitializer.init(BuildKonfig.DEBUG)
    initKoin(koinInitializer)
  }

  private fun initKoin(koinInitializer: KoinApplication.() -> Unit = {}) {
    val koin = startKoin {
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
      modules(initialAppModule)
      modules(initialDatabaseModule)
    }.koin
    MainScope().launch {
      loadDatabaseModule(koin)
      koin.get<AppInitializerSignal>().complete()
    }
  }
}
