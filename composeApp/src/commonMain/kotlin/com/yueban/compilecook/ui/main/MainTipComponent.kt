package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.base.UiStateComponent
import kotlinx.serialization.Serializable

@Serializable
data class MainTipState(
  val title: String = "MainTip",
)

interface MainTipComponent : UiStateComponent<MainTipState>

class DefaultMainTipComponent(
  componentContext: ComponentContext,
) : MainTipComponent, BaseComponent<MainTipState>(
  componentContext = componentContext,
  initialState = MainTipState(),
  serializer = MainTipState.serializer(),
)
