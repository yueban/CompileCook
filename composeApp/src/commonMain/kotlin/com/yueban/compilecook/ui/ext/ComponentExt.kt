package com.yueban.compilecook.ui.ext

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import com.yueban.compilecook.logger.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

fun <T : Any> Flow<T>.toValue(
  componentContext: ComponentContext,
  initialValue: T,
): Value<T> {
  val value = MutableValue(initialValue)
  val scope = componentContext.coroutineScope()

  scope.launch { collect { value.value = it } }

  return value
}

fun ComponentContext.coroutineScope(): CoroutineScope {
  val scope = CoroutineScope(
    Dispatchers.Main.immediate +
      SupervisorJob() +
      CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable)
        throw throwable
      }
  )
  lifecycle.doOnDestroy { scope.cancel() }
  return scope
}

inline fun <reified T : Any> StateKeeperOwner.saveableFlow(
  serializer: KSerializer<T>,
  key: String? = null,
  crossinline init: () -> T,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, MutableStateFlow<T>>> =
  PropertyDelegateProvider { _, property ->
    val stateKey = key ?: "SAVEABLE_${property.name}"
    val restored = stateKeeper.consume(stateKey, serializer) ?: init()
    val flow = MutableStateFlow(restored)
    stateKeeper.register(stateKey, serializer) { flow.value }
    ReadOnlyProperty { _, _ -> flow }
  }
