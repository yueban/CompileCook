package com.yueban.compilecook.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.childStackWebNavigation
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.webhistory.WebNavigation
import com.arkivanov.decompose.router.webhistory.WebNavigationOwner
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.service.MessageService
import com.yueban.compilecook.service.UiMessage
import com.yueban.compilecook.ui.about.AboutComponent
import com.yueban.compilecook.ui.about.DefaultAboutComponent
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.BaseComponent
import com.yueban.compilecook.ui.dish.DefaultDishComponent
import com.yueban.compilecook.ui.dish.DefaultDishListComponent
import com.yueban.compilecook.ui.dish.DishComponent
import com.yueban.compilecook.ui.dish.DishListComponent
import com.yueban.compilecook.ui.dish.DishListComponent.Output.DishClicked
import com.yueban.compilecook.ui.main.DefaultMainComponent
import com.yueban.compilecook.ui.main.MainComponent
import com.yueban.compilecook.ui.main.MainComponent.MainTab
import com.yueban.compilecook.ui.main.MainComponent.Output.AboutClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.DishCategoryClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.DishSearchClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.RandomDishClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.TipClicked
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.About
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Dish
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.DishList
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Main
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Tip
import com.yueban.compilecook.ui.root.RootComponent.Child.AboutChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.MainChild
import com.yueban.compilecook.ui.root.RootComponent.Child.TipChild
import com.yueban.compilecook.ui.service.DeepLinkHandler
import com.yueban.compilecook.ui.tip.DefaultTipComponent
import com.yueban.compilecook.ui.tip.TipComponent
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.get

interface RootComponent : BackHandlerOwner, WebNavigationOwner {
  val stack: Value<ChildStack<Config, Child>>
  val messages: Flow<UiMessage>
  fun onDeepLink(url: String)
  fun onUriClicked(uri: String): Boolean
  fun onBackClicked()

  sealed class Child {
    class MainChild(val component: MainComponent) : Child()
    class TipChild(val component: TipComponent) : Child()
    class DishListChild(val component: DishListComponent) : Child()
    class DishChild(val component: DishComponent) : Child()
    class AboutChild(val component: AboutComponent) : Child()
  }
}

class DefaultRootComponent(
  componentContext: ComponentContext,
  deepLinkUrl: String? = null,
) : RootComponent, BaseComponent(componentContext) {
  private val deepLinkHandler: DeepLinkHandler = get()
  private val navigation = StackNavigation<Config>()
  override val stack: Value<ChildStack<Config, RootComponent.Child>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialStack = { getInitialStack(deepLinkUrl) },
      handleBackButton = true,
      childFactory = ::child
    )
  override val webNavigation: WebNavigation<*> = childStackWebNavigation(
    navigator = navigation,
    stack = stack,
    serializer = Config.serializer(),
    pathMapper = { (config, child, any) ->
      Logger.d("config: $config, child: $child, any: $any")
      when (config) {
        is Main -> "/"
        About -> "/about"
        is Tip -> "/tips/${config.tipName}"
        is DishList -> {
          val cat = config.dishCategory?.name?.lowercase()
          if (cat == null) "/dishes" else "/dishes?category=$cat"
        }
        is Dish -> "/dishes/${config.dishName}"
      }
    }
  )
  override val messages: Flow<UiMessage> = get<MessageService>().messageFlow

  init {
    lifecycle.doOnCreate {
      componentScope.launch {
        deepLinkHandler.deepLinkFlow.collect { url ->
          onDeepLink(url)
        }
      }
    }
  }

  override fun onDeepLink(url: String) {
    navigation.navigate { getInitialStack(url) }
  }

  override fun onUriClicked(uri: String): Boolean =
    parseUriToConfig(uri)?.let { navigation.push(it) } != null

  private fun parseUriToConfig(uri: String): Config? {
    val segments = uri.split("/")
    val fileName = segments.lastOrNull()?.removeSuffix(".md") ?: return null
    return when {
      uri.contains("/dishes/") -> Dish(fileName)
      uri.contains("/tips/") -> Tip(fileName)
      else -> null
    }
  }

  private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
    when (config) {
      is Main -> DefaultMainComponent(
        componentContext = componentContext,
        initialTab = config.initialTab,
        onOutput = { output ->
          when (output) {
            is TipClicked -> navigation.push(Tip(output.tipName))
            is DishCategoryClicked -> navigation.push(DishList(output.dishCategory))
            MainComponent.Output.DishFavoriteClicked -> navigation.push(DishList(null, isFavorite = true))
            DishSearchClicked -> navigation.push(DishList(null, startInSearchMode = true))
            is RandomDishClicked -> navigation.push(Dish(output.dishName))
            AboutClicked -> navigation.push(About)
          }
        },
        dishRepo = get(),
      ).let { MainChild(it) }

      is Tip -> DefaultTipComponent(
        componentContext = componentContext,
        tipName = config.tipName,
        onOutput = navigation.onOutput(),
        dishRepo = get(),
      ).let { TipChild(it) }

      is DishList -> DefaultDishListComponent(
        componentContext = componentContext,
        dishCategory = config.dishCategory,
        startInSearchMode = config.startInSearchMode,
        isFavorite = config.isFavorite,
        dishRepo = get(),
        onOutput = navigation.onOutput { output ->
          when (output) {
            is DishClicked -> navigation.push(Dish(output.dishName))
            else -> {}
          }
        }
      ).let { DishListChild(it) }

      is Dish -> DefaultDishComponent(
        componentContext = componentContext,
        dishName = config.dishName,
        dishRepo = get(),
        onOutput = navigation.onOutput()
      ).let { DishChild(it) }

      About -> DefaultAboutComponent(
        componentContext = componentContext,
        onOutput = navigation.onOutput()
      ).let { AboutChild(it) }
    }

  private fun getInitialStack(deepLinkUrl: String?): List<Config> {
    Logger.d("deepLinkUrl: $deepLinkUrl")
    val url = deepLinkUrl?.let { Url(it) } ?: return listOf(Main(MainTab.TIPS))
    val segments = url.segments.filter { it.isNotEmpty() }

    return when {
      // /about
      segments.firstOrNull() == "about" -> listOf(Main(MainTab.TIPS), About)

      // /tips/{name}
      segments.firstOrNull() == "tips" -> {
        val tipName = segments.getOrNull(1)
        if (tipName != null) listOf(Main(MainTab.TIPS), Tip(tipName)) else listOf(Main(MainTab.TIPS))
      }

      // /dishes
      segments.firstOrNull() == "dishes" -> {
        val nextSegment = segments.getOrNull(1)
        if (nextSegment == null) {
          // /dishes?category=?
          val categoryName = url.parameters["category"]
          val category = DishCategory.entries.find { it.name.lowercase() == categoryName }
          listOf(Main(MainTab.DISHES), DishList(category))
        } else {
          // /dishes/{dishName}
          listOf(Main(MainTab.DISHES), DishList(null), Dish(nextSegment))
        }
      }

      else -> listOf(Main(MainTab.TIPS))
    }
  }

  override fun onBackClicked() = navigation.pop()

  @Serializable
  sealed interface Config {
    @Serializable data class Main(val initialTab: MainTab = MainTab.TIPS) : Config

    @Serializable data class Tip(val tipName: String) : Config

    @Serializable data class DishList(
      val dishCategory: DishCategory?,
      val startInSearchMode: Boolean = false,
      val isFavorite: Boolean = false,
    ) : Config

    @Serializable data class Dish(val dishName: String) : Config

    @Serializable data object About : Config
  }
}

private inline fun <T> StackNavigation<*>.onOutput(
  crossinline handler: (T) -> Unit = {},
): (T) -> Unit = { event ->
  if (event is BackOutput) {
    pop()
  } else {
    handler(event)
  }
}
