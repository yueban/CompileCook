package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.ext.toValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

interface DetailComponent {
  val model: Value<Model>
  fun onAddCount()
  fun onBackClicked()

  data class Model(
    val dish: Dish?,
    val counter: Int,
  )
}

class DefaultDetailComponent(
  componentContext: ComponentContext,
  dishRepo: DishRepo,
  dishName: String,
  private val onFinished: (dishName: String) -> Unit,
) : DetailComponent, ComponentContext by componentContext {
  private val retainedInstance = retainedInstance { RetainedInstance() }
  override val model: Value<DetailComponent.Model> =
    combine(
      dishRepo.getDishByName(dishName),
      retainedInstance.counterFlow,
    ) { dish, counter ->
      DetailComponent.Model(dish = dish, counter = counter)
    }.toValue(componentContext, DetailComponent.Model(null, 0))

  override fun onAddCount() {
    retainedInstance.counterFlow.update { it + 1 }
  }

  override fun onBackClicked() = onFinished.invoke(model.value.dish?.name ?: "")

  private class RetainedInstance : InstanceKeeper.Instance {
    val counterFlow = MutableStateFlow(0)

    override fun onDestroy() {
      println("RetainedInstance destroyed, counter: ${counterFlow.value}")
    }
  }
}
