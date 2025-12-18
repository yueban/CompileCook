package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import kotlin.random.Random

interface DetailComponent {
  val model: Value<Model>

  fun onBackClicked()

  data class Model(
    val item: String,
    val instanceId: String,
  )
}

class DefaultDetailComponent(
  componentContext: ComponentContext,
  item: String,
  private val onFinished: ((Boolean) -> Unit) -> Unit,
) : DetailComponent, ComponentContext by componentContext {
  private val retainedInstance = retainedInstance { RetainedInstance() }
  override val model: Value<DetailComponent.Model> =
    MutableValue(
      DetailComponent.Model(
        item = item,
        instanceId = "ID: ${retainedInstance.id}"
      )
    )

  override fun onBackClicked() = onFinished.invoke { }

  private class RetainedInstance : InstanceKeeper.Instance {
    val id: String = Random.nextInt().toString()

    override fun onDestroy() {
      println("RetainedInstance destroyed: $id")
    }
  }
}
