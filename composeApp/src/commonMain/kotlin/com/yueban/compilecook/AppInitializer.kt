package com.yueban.compilecook

import com.yueban.compilecook.di.coroutineModule
import com.yueban.compilecook.logger.KoinLogger
import com.yueban.compilecook.repo.di.initialDatabaseModule
import com.yueban.compilecook.repo.di.loadDataModules
import com.yueban.compilecook.util.CoilUtil
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
    UtilInitializer.init(BuildKonfig.IS_DEBUG)
    CoilUtil.init()
    initKoin(koinInitializer)
  }

  private fun initKoin(koinInitializer: KoinApplication.() -> Unit = {}) {
    val koin = startKoin {
      koinInitializer()
      logger(KoinLogger(BuildKonfig.IS_DEBUG))
      modules(
        coroutineModule,
        initialAppModule,
        initialDatabaseModule,
      )
    }.koin
    MainScope().launch {
      loadDataModules(koin)
      koin.get<AppInitializerSignal>().complete()
    }
  }
}
