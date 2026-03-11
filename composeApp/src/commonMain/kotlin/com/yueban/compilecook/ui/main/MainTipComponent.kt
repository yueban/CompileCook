package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.TipSummary
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable

@Serializable
data class MainTipState(
  val loadingAsync: Async<Unit> = Uninitialized,
  val groupedTipsAsync: Async<List<Pair<TipType, List<TipSummary>>>> = Uninitialized,
)

interface MainTipComponent : UiStateComponent<MainTipState> {
  fun onRetry()
  fun onTipClicked(tip: TipSummary)

  sealed interface Output {
    data class TipClicked(val tip: TipSummary) : Output
  }
}

class DefaultMainTipComponent(
  componentContext: ComponentContext,
  private val dishRepo: DishRepo,
  private val onOutput: (MainTipComponent.Output) -> Unit,
) : MainTipComponent, UiStateComponentImpl<MainTipState>(
  componentContext = componentContext,
  initialState = MainTipState(),
  serializer = MainTipState.serializer(),
) {
  init {
    dishRepo.getGroupedTipsSortedByPinyin()
      .execute(retainValue = MainTipState::groupedTipsAsync) {
        copy(groupedTipsAsync = it)
      }
  }

  override fun onRetry() {
    suspend { dishRepo.updateTips() }
      .execute { copy(loadingAsync = it) }
  }

  override fun onTipClicked(tip: TipSummary) = onOutput(MainTipComponent.Output.TipClicked(tip))
}
