package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable

@Serializable
data class DetailState(
  val dishAsync: Async<Dish?> = Uninitialized,
)

interface DetailComponent : UiStateComponent<DetailState> {
  fun onBackClicked()
}

class DefaultDetailComponent(
  componentContext: ComponentContext,
  dishRepo: DishRepo,
  dishName: String,
  private val onFinished: (dishName: String) -> Unit,
) : DetailComponent,
  BaseComponent<DetailState>(
    componentContext,
    DetailState(),
    DetailState.serializer(),
  ) {
  init {
    dishRepo.getDishByName(dishName).execute(
      retainValue = { it.dishAsync },
      reducer = { copy(dishAsync = it) }
    )
  }

  override fun onBackClicked() {
    onFinished(uiState.value.dishAsync.invoke()?.name ?: "")
  }
}
