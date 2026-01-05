package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.ext.saveableFlow
import com.yueban.compilecook.ui.ext.toValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

interface ListComponent {
  val model: Value<Model>
  val eventFlow: Flow<Event>
  fun onItemClicked(dishName: String)
  fun onAddCount()

  data class Model(
    val dishes: List<Dish>,
    val counter: Int,
  )

  sealed interface Event {
    data class BackFromDetail(val dishName: String) : Event
  }
}

class DefaultListComponent(
  componentContext: ComponentContext,
  dishRepo: DishRepo,
  override val eventFlow: Flow<ListComponent.Event>,
  private val onItemSelected: (dishName: String) -> Unit,
) : ListComponent, ComponentContext by componentContext {
  @Serializable
  private data class State(val counter: Int)

  private val stateFlow: MutableStateFlow<State> by saveableFlow(serializer = State.serializer()) { State(0) }

  override val model: Value<ListComponent.Model> =
    combine(dishRepo.getAllDishes(), stateFlow) { dishes, state ->
      ListComponent.Model(dishes = dishes, counter = state.counter)
    }.toValue(componentContext, ListComponent.Model(listOf(), stateFlow.value.counter))

  override fun onItemClicked(dishName: String) = onItemSelected(dishName)

  override fun onAddCount() {
    stateFlow.update { it.copy(counter = it.counter + 1) }
  }
}
