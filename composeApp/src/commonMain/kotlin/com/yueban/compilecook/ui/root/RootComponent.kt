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
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.yueban.compilecook.di.DispatcherType
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.service.MessageService
import com.yueban.compilecook.service.UiMessage
import com.yueban.compilecook.ui.dish.DefaultDishListComponent
import com.yueban.compilecook.ui.dish.DishListComponent
import com.yueban.compilecook.ui.inbox.DefaultDetailComponent
import com.yueban.compilecook.ui.inbox.DefaultListComponent
import com.yueban.compilecook.ui.inbox.DetailComponent
import com.yueban.compilecook.ui.inbox.ListComponent
import com.yueban.compilecook.ui.inbox.ListComponent.Event.BackFromDetail
import com.yueban.compilecook.ui.main.DefaultMainComponent
import com.yueban.compilecook.ui.main.MainComponent
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config
import com.yueban.compilecook.ui.root.RootComponent.Child.DetailChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.ListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.MainChild
import com.yueban.compilecook.ui.root.RootComponent.Child.TipChild
import com.yueban.compilecook.ui.service.DeepLinkHandler
import com.yueban.compilecook.ui.tip.DefaultTipComponent
import com.yueban.compilecook.ui.tip.TipComponent
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

interface RootComponent : BackHandlerOwner, WebNavigationOwner {
  val stack: Value<ChildStack<Config, Child>>
  val messages: Flow<UiMessage>
  fun onDeepLink(url: String)
  fun onBackClicked()

  sealed class Child {
    class MainChild(val component: MainComponent) : Child()
    class TipChild(val component: TipComponent) : Child()
    class DishListChild(val component: DishListComponent) : Child()
    class ListChild(val component: ListComponent) : Child()
    class DetailChild(val component: DetailComponent) : Child()
  }
}

class DefaultRootComponent(
  componentContext: ComponentContext,
  deepLinkUrl: String? = null,
) : RootComponent, ComponentContext by componentContext, KoinComponent {
  private val scope: CoroutineScope = coroutineScope()
  private val deepLinkHandler: DeepLinkHandler = get()
  private val listEvents = Channel<ListComponent.Event>(Channel.BUFFERED)
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
        Config.Main -> "/"
        is Config.Tip -> "/tips/${config.tipName}"
        is Config.DishList ->
          if (config.dishCategory == null) {
            "/dishes"
          } else {
            "/dishes/category=${config.dishCategory.name.lowercase()}"
          }

        Config.List -> "/dishes"
        is Config.Detail -> "/dishes/${config.dishName}"
      }
    }
  )
  override val messages: Flow<UiMessage> = get<MessageService>().messageFlow

  init {
    lifecycle.doOnCreate {
      scope.launch {
        deepLinkHandler.deepLinkFlow.collect { url ->
          onDeepLink(url)
        }
      }
    }
  }

  override fun onDeepLink(url: String) {
    navigation.navigate { getInitialStack(url) }
  }

  private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
    when (config) {
      Config.Main -> DefaultMainComponent(
        componentContext = componentContext,
        onOutput = { output ->
          when (output) {
            is MainComponent.Output.TipClicked -> navigation.push(Config.Tip(output.tipName))
            is MainComponent.Output.DishCategoryClicked -> navigation.push(Config.DishList(output.dishCategory))
          }
        }
      ).let { MainChild(it) }

      is Config.Tip -> DefaultTipComponent(
        componentContext = componentContext,
        tipName = config.tipName,
        onOutput = { output ->
          when (output) {
            TipComponent.Output.BackClicked -> onBackClicked()
          }
        },
        dishRepo = get(),
        defaultDispatcher = get(named(DispatcherType.Default)),
      ).let { TipChild(it) }

      is Config.DishList -> DefaultDishListComponent(
        componentContext = componentContext,
        dishCategory = config.dishCategory,
        dishRepo = get(),
        onOutput = { output ->
          when (output) {
            DishListComponent.Output.BackClicked -> onBackClicked()
          }
        }
      ).let { DishListChild(it) }

      Config.List -> DefaultListComponent(
        componentContext = componentContext,
        dishRepo = get(),
        eventFlow = listEvents.receiveAsFlow(),
        onItemSelected = {
          navigation.push(Config.Detail(dishName = it))
        }
      ).let { ListChild(it) }

      is Config.Detail -> DefaultDetailComponent(
        componentContext = componentContext,
        dishRepo = get(),
        dishName = config.dishName,
        onFinished = { dishName ->
          navigation.pop { listEvents.trySend(BackFromDetail(dishName)) }
        },
      ).let { DetailChild(it) }
    }

  private fun getInitialStack(deepLinkUrl: String?): List<Config> {
    Logger.d("deepLinkUrl: $deepLinkUrl")
    val url = deepLinkUrl?.let { Url(it) }
    return when (val dishName = url?.segments?.firstOrNull()) {
      null -> listOf(Config.Main)
      else -> listOf(Config.Main, Config.Detail(dishName = dishName))
    }
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  @Serializable
  sealed interface Config {
    @Serializable data object Main : Config

    @Serializable data class Tip(val tipName: String) : Config

    @Serializable data class DishList(val dishCategory: DishCategory?) : Config

    @Serializable data object List : Config

    @Serializable data class Detail(val dishName: String) : Config
  }
}

private fun ComponentContext.coroutineScope(): CoroutineScope {
  val scope = CoroutineScope(
    Dispatchers.Main.immediate +
      SupervisorJob() +
      CoroutineExceptionHandler { _, throwable ->
        Logger.e(throwable)
        throw throwable
      }
  )
  lifecycle.doOnDestroy { scope.cancel() }
  return scope
}
