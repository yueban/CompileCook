package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface MainComponent {
  val stack: Value<ChildStack<*, Child>>
  fun onTabSelected(tab: MainTab)

  enum class MainTab { DISHES, TIPS }

  sealed class Child {
    class Dishes(val component: MainDishComponent) : Child()
    class Tips(val component: MainTipComponent) : Child()
  }
}

class DefaultMainComponent(
  componentContext: ComponentContext,
) : MainComponent, ComponentContext by componentContext, KoinComponent {
  private val navigation = StackNavigation<Config>()

  override val stack = childStack(
    source = navigation,
    serializer = Config.serializer(),
    initialConfiguration = Config.Tips,
    handleBackButton = false,
    childFactory = ::createChild
  )

  override fun onTabSelected(tab: MainComponent.MainTab) {
    val config = when (tab) {
      MainComponent.MainTab.DISHES -> Config.Dishes
      MainComponent.MainTab.TIPS -> Config.Tips
    }
    navigation.bringToFront(config)
  }

  private fun createChild(config: Config, ctx: ComponentContext): MainComponent.Child {
    return when (config) {
      Config.Tips -> MainComponent.Child.Tips(
        DefaultMainTipComponent(
          componentContext = ctx,
          dishRepo = get(),
        )
      )
      Config.Dishes -> MainComponent.Child.Dishes(
        DefaultMainDishComponent(
          componentContext = ctx,
        )
      )
    }
  }

  @Serializable
  sealed interface Config {
    @Serializable data object Tips : Config

    @Serializable data object Dishes : Config
  }
}
