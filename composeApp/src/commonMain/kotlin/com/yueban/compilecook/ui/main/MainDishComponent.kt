package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import kotlinx.serialization.Serializable

@Serializable
data class MainDishState(
  val title: String = "MainDish",
)

interface MainDishComponent : UiStateComponent<MainDishState>

class DefaultMainDishComponent(
  componentContext: ComponentContext,
) : MainDishComponent, BaseComponent<MainDishState>(
  componentContext = componentContext,
  initialState = MainDishState(),
  serializer = MainDishState.serializer(),
)
