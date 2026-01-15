package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.Uninitialized
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class ListState(
  val loadingAsync: Async<Unit> = Uninitialized,
  val dishesAsync: Async<List<Dish>> = Uninitialized,
)

interface ListComponent : UiStateComponent<ListState> {
  val eventFlow: Flow<Event>
  fun onItemClicked(dishName: String)
  fun onRetry()

  sealed interface Event {
    data class BackFromDetail(val dishName: String) : Event
  }
}

class DefaultListComponent(
  componentContext: ComponentContext,
  private val dishRepo: DishRepo,
  override val eventFlow: Flow<ListComponent.Event>,
  private val onItemSelected: (dishName: String) -> Unit,
) : ListComponent,
  BaseComponent<ListState>(
    componentContext,
    ListState(),
    ListState.serializer()
  ) {
  init {
    dishRepo.getAllDishes()
      .execute(retainValue = ListState::dishesAsync) {
        copy(dishesAsync = it)
      }
    if (uiState.value.dishesAsync.invoke().isNullOrEmpty()) {
      onRetry()
    }
  }

  override fun onItemClicked(dishName: String) = onItemSelected(dishName)

  override fun onRetry() {
    suspend { dishRepo.updateDishes() }
      .execute { copy(loadingAsync = it) }
  }
}
