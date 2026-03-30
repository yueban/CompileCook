package com.yueban.compilecook.ui.dish

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.DishSummary
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import com.yueban.compilecook.ui.dish.DishListComponent.Output.BackClicked
import com.yueban.compilecook.ui.dish.DishListComponent.Output.DishClicked
import com.yueban.compilecook.ui.util.SmartMatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
data class DishListState(
  val source: DishListSource,
  val isSearchActive: Boolean = source is DishListSource.Search,
  val searchQuery: String = "",
  val dishesAsync: Async<List<DishSummary>> = Uninitialized,
)

@Serializable
sealed interface DishListSource {
  @Serializable data object All : DishListSource

  @Serializable data object Search : DishListSource

  @Serializable data object Favorite : DishListSource

  @Serializable data class Category(val category: DishCategory) : DishListSource

  @Serializable data class Difficulty(val level: Int) : DishListSource
}

interface DishListComponent : UiStateComponent<DishListState> {
  fun onBackClicked()
  fun onDishClicked(dish: DishSummary)
  fun onDishFavoriteClick(dish: DishSummary)
  fun onSearchActiveChanged(active: Boolean)
  fun onSearchQueryChanged(query: String)

  sealed interface Output {
    data object BackClicked : Output, BackOutput
    data class DishClicked(val dishName: String) : Output
  }
}

class DefaultDishListComponent(
  componentContext: ComponentContext,
  source: DishListSource,
  private val onOutput: (DishListComponent.Output) -> Unit,
  private val dishRepo: DishRepo,
) : DishListComponent, UiStateComponentImpl<DishListState>(
  componentContext = componentContext,
  initialState = DishListState(source = source),
  serializer = DishListState.serializer(),
) {
  init {
    val dishesFlow = when (source) {
      DishListSource.All,
      DishListSource.Search,
      -> dishRepo.getAllDishes()
      is DishListSource.Category -> dishRepo.getDishesByCategory(source.category)
      is DishListSource.Difficulty -> dishRepo.getDishesByDifficulty(source.level)
      DishListSource.Favorite -> dishRepo.getAllFavoriteDishes()
    }.distinctUntilChanged()

    val queryFlow = uiState
      .map { state -> state.searchQuery.takeIf { state.isSearchActive } }
      .distinctUntilChanged()
      .debounce(100)

    combine(dishesFlow, queryFlow) { dishes, query ->
      if (query.isNullOrBlank()) {
        dishes
      } else {
        dishes.filter { it.matches(query) }
      }
    }.execute(retainValue = DishListState::dishesAsync) {
      copy(dishesAsync = it)
    }
  }

  override fun onBackClicked() {
    if (uiState.value.source is DishListSource.Search || !uiState.value.isSearchActive) {
      onOutput(BackClicked)
    } else {
      onSearchActiveChanged(false)
    }
  }

  override fun onDishClicked(dish: DishSummary) = onOutput(DishClicked(dish.name))

  override fun onDishFavoriteClick(dish: DishSummary) {
    launch { dishRepo.toggleDishFavorite(dish.name) }
  }

  override fun onSearchActiveChanged(active: Boolean) = setState {
    copy(
      isSearchActive = active,
      searchQuery = if (!active) "" else searchQuery
    )
  }

  override fun onSearchQueryChanged(query: String) = setState {
    copy(searchQuery = query)
  }

  private fun DishSummary.matches(query: String): Boolean {
    return SmartMatcher.matches(this.name, this.pinyin, query)
  }
}
