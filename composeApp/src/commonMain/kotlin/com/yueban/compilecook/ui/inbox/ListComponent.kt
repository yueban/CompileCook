package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.saveable
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.ext.toValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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

  private var state: State by saveable(serializer = State.serializer()) { State(0) }
  private val counterFlow = MutableStateFlow(0)

  override val model: Value<ListComponent.Model> =
    combine(dishRepo.getAllDishes(), counterFlow) { dishes, counter ->
      ListComponent.Model(dishes = dishes, counter = counter)
    }.toValue(componentContext, ListComponent.Model(listOf(), state.counter))

  override fun onItemClicked(dishName: String) = onItemSelected(dishName)

  override fun onAddCount() {
    (state.counter + 1).let {
      state = State(it)
      counterFlow.value = it
    }
  }
}
