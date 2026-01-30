package com.yueban.compilecook.ui.tip

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Tip
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class TipState(
  val tipName: String,
  val tipAsync: Async<Tip?> = Uninitialized,
  val contentAsync: Async<State> = Uninitialized,
)

interface TipComponent : UiStateComponent<TipState> {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output
  }
}

class DefaultTipComponent(
  componentContext: ComponentContext,
  tipName: String,
  private val onOutput: (TipComponent.Output) -> Unit,
  dishRepo: DishRepo,
  defaultDispatcher: CoroutineDispatcher,
) : TipComponent, BaseComponent<TipState>(
  componentContext = componentContext,
  initialState = TipState(tipName = tipName),
  serializer = TipState.serializer(),
) {
  override fun onBackClicked() = onOutput(TipComponent.Output.BackClicked)

  init {
    dishRepo.getTipByName(tipName)
      .execute(retainValue = TipState::tipAsync) {
        copy(tipAsync = it)
      }

    uiState.map { it.tipAsync }
      .filter { it is Success }
      .map { it.value }
      .filterNotNull()
      .map { it.content }
      .distinctUntilChanged()
      .flatMapLatest { parseMarkdownFlow(it) }
      .flowOn(defaultDispatcher)
      .execute(retainValue = TipState::contentAsync) {
        copy(contentAsync = it)
      }
  }
}
