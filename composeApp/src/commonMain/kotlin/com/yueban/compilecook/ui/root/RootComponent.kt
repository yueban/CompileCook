package com.yueban.compilecook.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.childStackWebNavigation
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.webhistory.WebNavigation
import com.arkivanov.decompose.router.webhistory.WebNavigationOwner
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.AiContext
import com.yueban.compilecook.repo.entity.AiContextType
import com.yueban.compilecook.service.MessageService
import com.yueban.compilecook.service.UiMessage
import com.yueban.compilecook.ui.about.AboutComponent
import com.yueban.compilecook.ui.ai.AiChatComponent
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.ToggleAiDrawerOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.dish.DishComponent
import com.yueban.compilecook.ui.dish.DishListComponent
import com.yueban.compilecook.ui.dish.DishListComponent.Output.DishClicked
import com.yueban.compilecook.ui.dish.DishListSource
import com.yueban.compilecook.ui.main.MainComponent
import com.yueban.compilecook.ui.main.MainComponent.MainTab
import com.yueban.compilecook.ui.main.MainComponent.Output.AboutClicked
import com.yueban.compilecook.ui.main.MainComponent.Output.AiClicked
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
import com.yueban.compilecook.ui.tip.TipComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

private const val KEY_ROOT_CHILD_STACK = "ROOT_CHILD_STACK"
private const val KEY_AI_CHAT_SLOT = "AI_CHAT_SLOT"
private const val DRAWER_ANIMATION_DURATION_MS = 300L

data class RootState(
  val isDrawerOpen: Boolean = false,
)

interface RootComponent :
  BackHandlerOwner,
  WebNavigationOwner,
  UiStateComponent<RootState> {
  val stack: Value<ChildStack<Config, Child>>
  val aiChatSlot: Value<ChildSlot<Unit, AiChatComponent>>
  val messages: Flow<UiMessage>
  fun onDeepLink(url: String)
  fun onUriClicked(uri: String): Boolean
  fun onBackClicked()
  fun toggleDrawer()
  fun openDrawer()
  fun closeDrawer()

  sealed class Child {
    class MainChild(val component: MainComponent) : Child()
    class TipChild(val component: TipComponent) : Child()
    class DishListChild(val component: DishListComponent) : Child()
    class DishChild(val component: DishComponent) : Child()
    class AboutChild(val component: AboutComponent) : Child()
  }
}

@Suppress("TooManyFunctions")
class DefaultRootComponent(
  componentContext: ComponentContext,
  deepLinkUrl: String? = null,
) : RootComponent, UiStateComponentImpl<RootState>(
  componentContext = componentContext,
  initialState = RootState(),
) {
  private val deepLinkHandler: DeepLinkHandler = get()
  private val navigation = StackNavigation<Config>()
  private val aiChatNavigation = SlotNavigation<Unit>()
  override val aiChatSlot: Value<ChildSlot<Unit, AiChatComponent>> =
    componentContext.childSlot(
      source = aiChatNavigation,
      serializer = Unit.serializer(),
      key = KEY_AI_CHAT_SLOT,
      handleBackButton = false,
      childFactory = { _, childContext -> get<AiChatComponent> { parametersOf(childContext) } },
    )
  override val stack: Value<ChildStack<Config, RootComponent.Child>> =
    childStack(
      key = KEY_ROOT_CHILD_STACK,
      source = navigation,
      serializer = Config.serializer(),
      initialStack = { PathMapper.pathToStack(deepLinkUrl) },
      handleBackButton = true,
      childFactory = ::child
    )
  override val webNavigation: WebNavigation<*> = childStackWebNavigation(
    navigator = navigation,
    stack = stack,
    serializer = Config.serializer(),
    pathMapper = { (config, child, any) ->
      Logger.d("config: $config, child: $child, any: $any")
      PathMapper.configToPath(config)
    },
  )
  override val messages: Flow<UiMessage> = get<MessageService>().messageFlow

  private var drawerDismissJob: Job? = null

  init {
    stack.subscribe {
      if (uiState.value.isDrawerOpen) {
        val context = deriveAiContext(stack.active.instance)
        aiChatSlot.value.child?.instance?.updateContext(context)
      }
    }
    lifecycle.doOnCreate {
      componentScope.launch {
        deepLinkHandler.deepLinkFlow.collect { url ->
          onDeepLink(url)
        }
      }
    }
  }

  override fun onDeepLink(url: String) {
    navigation.navigate { PathMapper.pathToStack(url) }
  }

  override fun onUriClicked(uri: String): Boolean =
    parseUriToConfig(uri)?.let {
      navigation.push(it)
    } != null

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
      is Main -> get<MainChild> { parametersOf(componentContext, config, ::onMainOutput) }
      is Tip -> get<TipChild> { parametersOf(componentContext, config, ::onTipOutput) }
      is DishList -> get<DishListChild> { parametersOf(componentContext, config, ::onDishListOutput) }
      is Dish -> get<DishChild> { parametersOf(componentContext, config, ::onDishOutput) }
      About -> get<AboutChild> { parametersOf(componentContext, ::onAboutOutput) }
    }

  private fun onMainOutput(output: MainComponent.Output) {
    when (output) {
      is TipClicked -> Tip(output.tipName)
      is DishCategoryClicked -> DishList(DishListSource.Category(output.dishCategory))
      MainComponent.Output.DishFavoriteClicked -> DishList(DishListSource.Favorite)
      is MainComponent.Output.DishDifficultyClicked -> DishList(DishListSource.Difficulty(output.level))
      DishSearchClicked -> DishList(DishListSource.Search)
      is RandomDishClicked -> Dish(output.dishName)
      AboutClicked -> About
      AiClicked -> {
        toggleDrawer()
        return
      }
    }.let { navigation.push(it) }
  }

  private fun onDishListOutput(output: DishListComponent.Output) = navigation.onOutput(output) { output ->
    when (output) {
      is DishClicked -> navigation.push(Dish(output.dishName))
      else -> {}
    }
  }

  private fun onTipOutput(output: TipComponent.Output) = navigation.onOutput(output)

  private fun onDishOutput(output: DishComponent.Output) = navigation.onOutput(output)

  private fun onAboutOutput(output: AboutComponent.Output) = navigation.onOutput(output)

  override fun onBackClicked() {
    navigation.pop()
  }

  override fun toggleDrawer() {
    if (uiState.value.isDrawerOpen) {
      closeDrawer()
    } else {
      openDrawer()
    }
  }

  override fun openDrawer() {
    drawerDismissJob?.cancel()
    drawerDismissJob = null
    if (!uiState.value.isDrawerOpen) {
      aiChatNavigation.activate(Unit)
    }
    setState { copy(isDrawerOpen = true) }
    val context = deriveAiContext(stack.active.instance)
    aiChatSlot.value.child?.instance?.updateContext(context)
  }

  override fun closeDrawer() {
    setState { copy(isDrawerOpen = false) }
    drawerDismissJob?.cancel()
    drawerDismissJob = componentScope.launch {
      // dismiss after drawer closing animation
      delay(DRAWER_ANIMATION_DURATION_MS)
      aiChatNavigation.dismiss()
    }
  }

  @Serializable
  sealed interface Config {
    @Serializable data class Main(val initialTab: MainTab = MainTab.TIPS) : Config
    @Serializable data class Tip(val tipName: String) : Config
    @Serializable data class DishList(val source: DishListSource) : Config
    @Serializable data class Dish(val dishName: String) : Config
    @Serializable data object About : Config
  }

  // TODO: pass actual dish/tip content into AiContext so the system prompt has real data
  private fun deriveAiContext(child: RootComponent.Child): AiContext? = when (child) {
    is DishChild -> AiContext(AiContextType.DISH, child.component.uiState.value.dishName, "")
    is TipChild -> AiContext(AiContextType.TIP, child.component.uiState.value.tipName, "")
    else -> null
  }

  private inline fun <T> StackNavigation<*>.onOutput(
    event: T,
    crossinline handler: (T) -> Unit = {},
  ) {
    when (event) {
      is BackOutput -> pop()
      is ToggleAiDrawerOutput -> toggleDrawer()
      else -> handler(event)
    }
  }
}
