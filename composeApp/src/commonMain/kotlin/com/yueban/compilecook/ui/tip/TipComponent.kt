package com.yueban.compilecook.ui.tip

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import com.yueban.compilecook.ui.tip.TipComponent.Output.BackClicked
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class TipState(
  val tipName: String,
  val contentAsync: Async<State> = Uninitialized,
)

interface TipComponent : UiStateComponent<TipState> {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultTipComponent(
  componentContext: ComponentContext,
  tipName: String,
  private val onOutput: (TipComponent.Output) -> Unit,
  dishRepo: DishRepo,
) : TipComponent, UiStateComponentImpl<TipState>(
  componentContext = componentContext,
  initialState = TipState(tipName = tipName),
  serializer = TipState.serializer(),
) {
  override fun onBackClicked() = onOutput(BackClicked)

  init {
    dishRepo.getTipByName(tipName)
      .filterNotNull()
      .map { it.content }
      .distinctUntilChanged()
      .flatMapLatest { parseMarkdownFlow(it) }
      .execute(retainValue = TipState::contentAsync) {
        copy(contentAsync = it)
      }
  }
}
