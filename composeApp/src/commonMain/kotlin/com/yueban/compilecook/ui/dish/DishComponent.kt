package com.yueban.compilecook.ui.dish

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.DishDetail
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import com.yueban.compilecook.ui.dish.DishComponent.Output.BackClicked
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DishState(
  val dishName: String,
  val dishAsync: Async<DishDetail?> = Uninitialized,
  @Transient
  val contentAsync: Async<State> = Uninitialized,
)

interface DishComponent : UiStateComponent<DishState> {
  fun onBackClicked()
  fun onFavoriteToggle()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultDishComponent(
  componentContext: ComponentContext,
  private val dishName: String,
  private val onOutput: (DishComponent.Output) -> Unit,
  private val dishRepo: DishRepo,
) : DishComponent, UiStateComponentImpl<DishState>(
  componentContext = componentContext,
  initialState = DishState(dishName = dishName),
  serializer = DishState.serializer(),
) {
  init {
    dishRepo.getDishByName(dishName)
      .execute(retainValue = DishState::dishAsync) {
        copy(dishAsync = it)
      }

    uiState.map { it.dishAsync.value }
      .filterNotNull()
      .map { it.content }
      .distinctUntilChanged()
      .flatMapLatest { parseMarkdownFlow(it) }
      .execute(retainValue = DishState::contentAsync) {
        copy(contentAsync = it)
      }
  }

  override fun onBackClicked() = onOutput(BackClicked)

  override fun onFavoriteToggle() {
    launch { dishRepo.toggleDishFavorite(dishName) }
  }
}
