package com.yueban.compilecook

import com.yueban.compilecook.di.appModule
import com.yueban.compilecook.di.coroutineModule
import com.yueban.compilecook.di.uiModule
import com.yueban.compilecook.logger.CustomKoinLogger
import com.yueban.compilecook.repo.di.databaseModule
import com.yueban.compilecook.repo.di.loadDataModules
import com.yueban.compilecook.ui.service.DeepLinkHandler
import com.yueban.compilecook.ui.util.CoilUtil
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
  single { DeepLinkHandler() }
}

object AppInitializer {
  fun init(koinInitializer: KoinApplication.() -> Unit = {}) {
    UtilInitializer.init()
    CoilUtil.init()
    initKoin(koinInitializer)
  }

  private fun initKoin(koinInitializer: KoinApplication.() -> Unit = {}) {
    val koin = startKoin {
      koinInitializer()
      logger(CustomKoinLogger())
      modules(
        coroutineModule,
        initialAppModule,
        databaseModule,
        appModule,
        uiModule,
      )
    }.koin
    MainScope().launch {
      loadDataModules(koin)
      koin.get<AppInitializerSignal>().complete()
    }
  }
}
