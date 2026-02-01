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
import com.yueban.compilecook.ui.dish.DishListComponent.Output.DishClicked
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class DishListState(
  val dishCategory: DishCategory?,
  val isSearchActive: Boolean = false,
  val searchQuery: String = "",
  val dishesAsync: Async<List<Dish>> = Uninitialized,
)

interface DishListComponent : UiStateComponent<DishListState> {
  fun onBackClicked()
  fun onDishClicked(dish: Dish)
  fun onSearchActiveChanged(active: Boolean)
  fun onSearchQueryChanged(query: String)

  sealed interface Output {
    data object BackClicked : Output, BackOutput
    data class DishClicked(val dishName: String) : Output
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
    val dishesFlow = if (dishCategory == null) {
      dishRepo.getAllDishes()
    } else {
      dishRepo.getDishesByCategory(dishCategory)
    }.distinctUntilChanged()

    val queryFlow = uiState
      .map { state -> state.searchQuery.takeIf { state.isSearchActive } }
      .distinctUntilChanged()
      .debounce(100)

    combine(dishesFlow, queryFlow) { dishes, query ->
      if (query.isNullOrBlank()) {
        dishes
      } else {
        // TODO: Support name/pinyin fuzzy search
        dishes.filter { it.name.contains(query) }
      }
    }.execute(retainValue = DishListState::dishesAsync) {
      copy(dishesAsync = it)
    }
  }

  override fun onBackClicked() {
    if (uiState.value.isSearchActive) {
      onSearchActiveChanged(false)
    } else {
      onOutput(BackClicked)
    }
  }

  override fun onDishClicked(dish: Dish) = onOutput(DishClicked(dish.name))

  override fun onSearchActiveChanged(active: Boolean) = setState {
    copy(
      isSearchActive = active,
      searchQuery = if (!active) "" else searchQuery
    )
  }

  override fun onSearchQueryChanged(query: String) = setState {
    copy(searchQuery = query)
  }
}
