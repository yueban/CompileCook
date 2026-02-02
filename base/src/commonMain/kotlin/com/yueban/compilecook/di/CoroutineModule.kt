package com.yueban.compilecook.di

import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val coroutineDispatcherModule = module {
  single<CoroutineDispatcher>(named(DispatcherType.Main)) { Dispatchers.Main }
  single<CoroutineDispatcher>(named(DispatcherType.MainImmediate)) { Dispatchers.Main.immediate }
  single<CoroutineDispatcher>(named(DispatcherType.Default)) { Dispatchers.Default }
}

val coroutineModule = module {
  single {
    CoroutineExceptionHandler { _, throwable ->
      Logger.e(throwable)
    }
  }
  single {
    CoroutineScope(
      CoroutineName("global") +
        SupervisorJob() +
        get<CoroutineExceptionHandler>() +
        get<CoroutineDispatcher>(named(DispatcherType.MainImmediate))
    )
  }

  includes(coroutineDispatcherModule)
}

enum class DispatcherType { Main, MainImmediate, Default, }
