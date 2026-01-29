package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Tip
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable

@Serializable
data class MainTipState(
  val loadingAsync: Async<Unit> = Uninitialized,
  val groupedTipsAsync: Async<List<Pair<TipType, List<Tip>>>> = Uninitialized,
)

interface MainTipComponent : UiStateComponent<MainTipState> {
  fun onRetry()
}

class DefaultMainTipComponent(
  componentContext: ComponentContext,
  private val dishRepo: DishRepo,
) : MainTipComponent, BaseComponent<MainTipState>(
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
}
