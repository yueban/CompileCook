package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

interface DetailComponent {
  val model: Value<Model>

  fun onBackClicked()

  data class Model(
    val item: String,
  )
}

class DefaultDetailComponent(
  componentContext: ComponentContext,
  item: String,
  private val onFinished: ((Boolean) -> Unit) -> Unit,
) : DetailComponent, ComponentContext by componentContext {
  override val model: Value<DetailComponent.Model> = MutableValue(DetailComponent.Model(item))
  override fun onBackClicked() = onFinished.invoke { }
}
