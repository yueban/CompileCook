package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.serialization.Serializable

interface ListComponent {
  val model: Value<Model>
  fun onItemClicked(item: String)
  fun onAddCount()

  data class Model(
    val items: List<String>,
    val counter: Int,
  )
}

class DefaultListComponent(
  componentContext: ComponentContext,
  private val onItemSelected: (item: String) -> Unit,
) : ListComponent, ComponentContext by componentContext {
  @Serializable
  private data class State(val counter: Int)

  private var state: State by saveable(serializer = State.serializer()) { State(0) }

  override val model: MutableValue<ListComponent.Model> =
    MutableValue(ListComponent.Model(items = List(100) { "Item $it" }, counter = state.counter))

  override fun onItemClicked(item: String) = onItemSelected(item)

  override fun onAddCount() {
    state = State(state.counter + 1)
    model.value = model.value.copy(counter = state.counter)
  }
}
