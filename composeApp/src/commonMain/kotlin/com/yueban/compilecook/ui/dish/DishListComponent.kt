package com.yueban.compilecook.ui.dish

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import com.yueban.compilecook.ui.dish.DishListComponent.Output.BackClicked
import kotlinx.serialization.Serializable

@Serializable
data class DishListState(
  val dishCategory: DishCategory?,
  val dishesAsync: Async<List<Dish>> = Uninitialized,
)

interface DishListComponent : UiStateComponent<DishListState> {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultDishListComponent(
  componentContext: ComponentContext,
  dishCategory: DishCategory?,
  private val onOutput: (DishListComponent.Output) -> Unit,
  dishRepo: DishRepo,
) : DishListComponent, BaseComponent<DishListState>(
  componentContext = componentContext,
  initialState = DishListState(dishCategory = dishCategory),
  serializer = DishListState.serializer(),
) {
  init {
    if (dishCategory == null) {
      dishRepo.getAllDishes()
    } else {
      dishRepo.getDishesByCategory(dishCategory)
    }.execute(retainValue = DishListState::dishesAsync) {
      copy(dishesAsync = it)
    }
  }

  override fun onBackClicked() = onOutput(BackClicked)
}
