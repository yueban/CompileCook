package com.yueban.compilecook.ui.inbox

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.ext.toValue
import kotlinx.coroutines.flow.mapLatest
import kotlin.random.Random

interface DetailComponent {
  val model: Value<Model>

  fun onBackClicked()

  data class Model(
    val dish: Dish?,
    val instanceId: String,
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
    dishRepo.getDishByName(dishName).mapLatest {
      DetailComponent.Model(dish = it, instanceId = "ID: ${retainedInstance.id}")
    }.toValue(componentContext, DetailComponent.Model(null, ""))

  override fun onBackClicked() = onFinished.invoke(model.value.dish?.name ?: "")

  private class RetainedInstance : InstanceKeeper.Instance {
    val id: String = Random.nextInt().toString()

    override fun onDestroy() {
      println("RetainedInstance destroyed: $id")
    }
  }
}
