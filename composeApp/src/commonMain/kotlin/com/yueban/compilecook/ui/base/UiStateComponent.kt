package com.yueban.compilecook.ui.base

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.di.DispatcherType
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.service.MessageService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

interface UiStateComponent<S : Any> {
  val uiState: StateFlow<S>
  fun showMessage(text: String) = Unit
  fun showGlobalError(error: Throwable) = Unit
}

/**
 * @param S The UI State class (Must be @Serializable)
 * @param componentContext Decompose context
 * @param initialState The default state
 * @param serializer The serializer for S. If passed, state is auto-saved/restored.
 */
abstract class UiStateComponentImpl<S : Any>(
  componentContext: ComponentContext,
  initialState: S,
  private val serializer: KSerializer<S>? = null,
) : UiStateComponent<S>, BaseComponent(componentContext) {
  private val defaultDispatcher = get<CoroutineDispatcher>(named(DispatcherType.Default))
  private val _uiState = createStateFlow(initialState, serializer)
  override val uiState: StateFlow<S> = _uiState.asStateFlow()
  private val messageService = KoinPlatform.getKoin().get<MessageService>()

  protected fun setState(reducer: S.() -> S) {
    _uiState.update(reducer)
  }

  @Suppress("TooGenericExceptionCaught")
  protected fun <T> (suspend () -> T).execute(
    dispatcher: CoroutineDispatcher = defaultDispatcher,
    retainValue: ((S) -> Async<T>)? = null,
    reducer: S.(Async<T>) -> S,
  ): Job = componentScope.launch {
    setState { reducer(Loading(value = retainValue?.invoke(this)?.invoke())) }
    try {
      val result = withContext(dispatcher) { this@execute() }
      setState { reducer(Success(result)) }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      setState { reducer(Fail(e, value = retainValue?.invoke(this)?.invoke())) }
    }
  }

  protected fun <T> Flow<T>.execute(
    dispatcher: CoroutineDispatcher = defaultDispatcher,
    retainValue: ((S) -> Async<T>)? = null,
    reducer: S.(Async<T>) -> S,
  ): Job =
    flowOn(dispatcher)
      .onStart { setState { reducer(Loading(value = retainValue?.invoke(this)?.invoke())) } }
      .catch { e -> setState { reducer(Fail(e, value = retainValue?.invoke(this)?.invoke())) } }
      .onEach { data -> setState { reducer(Success(data)) } }
      .launchIn(componentScope)

  protected fun launch(
    context: CoroutineContext = defaultDispatcher,
    showError: Boolean = true,
    onException: ((e: Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit,
  ) = componentScope.launch(
    CoroutineExceptionHandler { _, t ->
      if (t !is CancellationException) {
        Logger.e(t)
        onException?.invoke(t)
        if (showError) showGlobalError(t)
      }
    } + context
  ) {
    block.invoke(this)
  }

  override fun showMessage(text: String) {
    messageService.showMessage(text)
  }

  override fun showGlobalError(error: Throwable) {
    messageService.showError(error)
  }

  @Suppress("TooGenericExceptionCaught")
  private fun createStateFlow(
    initialState: S,
    serializer: KSerializer<S>?,
  ): MutableStateFlow<S> {
    // Auto-Restore
    val restored = if (serializer != null) {
      try {
        // the json instance used by Essenty library doesn't support ignoreUnknownKeys, which may cause crash.
        // TODO: replace with our own json instance when Essenty adds support for this.
        stateKeeper.consume(KEY_SAVED_STATE, serializer) ?: initialState
      } catch (e: Exception) {
        Logger.e("Error restoring state: ${e.message}")
        initialState
      }
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

  companion object Companion {
    private const val KEY_SAVED_STATE = "SAVED_STATE"
  }
}
