package com.yueban.compilecook.ui.base

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.service.MessageService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.serialization.KSerializer
import org.koin.mp.KoinPlatform
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

interface UiStateComponent<S : Any> {
  val uiState: StateFlow<S>
  fun showMessage(text: String)
  fun showGlobalError(error: Throwable)
}

/**
 * @param S The UI State class (Must be @Serializable)
 * @param componentContext Decompose context
 * @param initialState The default state
 * @param serializer The serializer for S. If passed, state is auto-saved/restored.
 */
abstract class BaseComponent<S : Any>(
  componentContext: ComponentContext,
  initialState: S,
  private val serializer: KSerializer<S>? = null,
) : UiStateComponent<S>, ComponentContext by componentContext {
  protected val scope: CoroutineScope = coroutineScope()
  private val _uiState = createStateFlow(initialState, serializer)
  override val uiState: StateFlow<S> = _uiState.asStateFlow()
  private val messageService = KoinPlatform.getKoin().get<MessageService>()

  protected fun setState(reducer: S.() -> S) {
    _uiState.update(reducer)
  }

  @Suppress("TooGenericExceptionCaught")
  protected fun <T> (suspend () -> T).execute(
    dispatcher: CoroutineDispatcher? = null,
    retainValue: ((S) -> Async<T>)? = null,
    reducer: S.(Async<T>) -> S,
  ): Job {
    setState { reducer(Loading(value = retainValue?.invoke(this)?.invoke())) }

    return scope.launch(dispatcher ?: EmptyCoroutineContext) {
      try {
        val result = this@execute()
        setState { reducer(Success(result)) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        setState { reducer(Fail(e, value = retainValue?.invoke(this)?.invoke())) }
      }
    }
  }

  protected fun <T> Flow<T>.execute(
    dispatcher: CoroutineDispatcher? = null,
    retainValue: ((S) -> Async<T>)? = null,
    reducer: S.(Async<T>) -> S,
  ): Job {
    setState { reducer(Loading(value = retainValue?.invoke(this)?.invoke())) }

    return catch { e -> setState { reducer(Fail(e, value = retainValue?.invoke(this)?.invoke())) } }
      .onEach { data -> setState { reducer(Success(data)) } }
      .launchIn(scope + (dispatcher ?: EmptyCoroutineContext))
  }

  override fun showMessage(text: String) {
    messageService.showMessage(text)
  }

  override fun showGlobalError(error: Throwable) {
    messageService.showError(error)
  }

  private fun createStateFlow(
    initialState: S,
    serializer: KSerializer<S>?,
  ): MutableStateFlow<S> {
    // Auto-Restore
    val restored = if (serializer != null) {
      stateKeeper.consume(KEY_SAVED_STATE, serializer) ?: initialState
    } else {
      initialState
    }

    val flow = MutableStateFlow(restored)

    // Auto-Save
    if (serializer != null) {
      stateKeeper.register(KEY_SAVED_STATE, serializer) { flow.value }
    }

    return flow
  }

  companion object {
    private const val KEY_SAVED_STATE = "SAVED_STATE"
  }
}

private fun ComponentContext.coroutineScope(): CoroutineScope {
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
