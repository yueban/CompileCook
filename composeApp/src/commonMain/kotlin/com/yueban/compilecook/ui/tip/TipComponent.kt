package com.yueban.compilecook.ui.tip

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Tip
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable

@Serializable
data class TipState(
  val tipName: String,
  val tipAsync: Async<Tip?> = Uninitialized,
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
  }
}
