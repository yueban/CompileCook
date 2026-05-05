package com.yueban.compilecook.ui.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.main.DefaultMainComponent.Config
import com.yueban.compilecook.ui.main.MainComponent.Output.AboutClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.DishSearchClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.RandomDishClicked
import com.yueban.compilecook.ui.main.MainDishComponent.Output.DishCategoryClicked
import com.yueban.compilecook.ui.main.MainTipComponent.Output.TipClicked
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.get

interface MainComponent {
  val stack: Value<ChildStack<*, Child>>
  fun onTabSelected(tab: MainTab)
  fun onDishSearchClicked()
  fun onRandomDishClicked()
  fun onAboutClicked()

  enum class MainTab {
    DISHES, TIPS;

    fun toConfig(): Config = when (this) {
      DISHES -> Config.Dishes
      TIPS -> Config.Tips
    }
  }

  sealed class Child {
    class Dishes(val component: MainDishComponent) : Child()
    class Tips(val component: MainTipComponent) : Child()
  }

  sealed interface Output {
    data class TipClicked(val tipName: String) : Output
    data class DishCategoryClicked(val dishCategory: DishCategory) : Output
    data object DishFavoriteClicked : Output
    data class DishDifficultyClicked(val level: Int) : Output
    data object DishSearchClicked : Output
    data class RandomDishClicked(val dishName: String) : Output
    data object AboutClicked : Output
  }
}

class DefaultMainComponent(
  componentContext: ComponentContext,
  initialTab: MainComponent.MainTab,
  private val onOutput: (MainComponent.Output) -> Unit,
  private val dishRepo: DishRepo,
) : MainComponent, BaseComponent(componentContext) {
  private val navigation = StackNavigation<Config>()

  override val stack = childStack(
    source = navigation,
    serializer = Config.serializer(),
    initialConfiguration = initialTab.toConfig(),
    handleBackButton = false,
    childFactory = ::createChild
  )

  override fun onTabSelected(tab: MainComponent.MainTab) {
    val config = tab.toConfig()
    navigation.bringToFront(config)
  }

  override fun onDishSearchClicked() = onOutput(DishSearchClicked)

  override fun onRandomDishClicked() {
    componentScope.launch {
      dishRepo.getRandomDishName()?.let {
        onOutput(RandomDishClicked(it))
      }
    }
  }

  override fun onAboutClicked() = onOutput(AboutClicked)

  private fun createChild(config: Config, ctx: ComponentContext): MainComponent.Child {
    return when (config) {
      Config.Tips -> MainComponent.Child.Tips(
        DefaultMainTipComponent(
          componentContext = ctx,
          dishRepo = get(),
          onOutput = { output ->
            when (output) {
              is TipClicked ->
                onOutput(MainComponent.Output.TipClicked(output.tip.name))
            }
          }
        )
      )
      Config.Dishes -> MainComponent.Child.Dishes(
        DefaultMainDishComponent(
          componentContext = ctx,
          dishRepo = get(),
          onOutput = { output ->
            when (output) {
              is DishCategoryClicked ->
                onOutput(MainComponent.Output.DishCategoryClicked(output.dishCategory))
              MainDishComponent.Output.DishFavoriteClicked ->
                onOutput(MainComponent.Output.DishFavoriteClicked)
              is MainDishComponent.Output.DishDifficultyClicked ->
                onOutput(MainComponent.Output.DishDifficultyClicked(output.level))
            }
          }
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
