package com.yueban.compilecook.ui.base

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.yueban.compilecook.di.DispatcherType
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

abstract class BaseComponent(componentContext: ComponentContext) : ComponentContext by componentContext, KoinComponent {
  private val mainImmediateDispatcher: CoroutineDispatcher = get(named(DispatcherType.MainImmediate))
  protected val scope: CoroutineScope = componentContext.coroutineScope()

  private fun ComponentContext.coroutineScope(): CoroutineScope {
    val scope = CoroutineScope(
      mainImmediateDispatcher +
        SupervisorJob() +
        CoroutineExceptionHandler { _, throwable ->
          Logger.e(throwable)
          throw throwable
        }
    )
    lifecycle.doOnDestroy { scope.cancel() }
    return scope
  }
}
