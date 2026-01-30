package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.serialization.Serializable

@Serializable
data class MainDishState(
  val loadingAsync: Async<Unit> = Uninitialized,
  val dishCategoriesAsync: Async<List<DishCategory>> = Uninitialized,
)

interface MainDishComponent : UiStateComponent<MainDishState> {
  fun onRetry()
  fun onDishCategoryClicked(dishCategory: DishCategory)

  sealed interface Output {
    data class DishCategoryClicked(val dishCategory: DishCategory) : Output
  }
}

class DefaultMainDishComponent(
  componentContext: ComponentContext,
  private val onOutput: (MainDishComponent.Output) -> Unit,
  private val dishRepo: DishRepo,
) : MainDishComponent, BaseComponent<MainDishState>(
  componentContext = componentContext,
  initialState = MainDishState(),
  serializer = MainDishState.serializer(),
) {
  init {
    dishRepo.getDishCategories()
      .execute(retainValue = MainDishState::dishCategoriesAsync) {
        copy(dishCategoriesAsync = it)
      }
  }

  override fun onRetry() {
    suspend { dishRepo.updateDishes() }
      .execute { copy(loadingAsync = it) }
  }

  override fun onDishCategoryClicked(dishCategory: DishCategory) =
    onOutput(MainDishComponent.Output.DishCategoryClicked(dishCategory))
}
