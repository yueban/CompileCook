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
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.ui.inbox.DefaultDetailComponent
import com.yueban.compilecook.ui.inbox.DefaultListComponent
import com.yueban.compilecook.ui.inbox.DetailComponent
import com.yueban.compilecook.ui.inbox.ListComponent
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config
import com.yueban.compilecook.ui.root.RootComponent.Child.DetailChild
import com.yueban.compilecook.ui.root.RootComponent.Child.ListChild
import io.ktor.http.Url
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface RootComponent : BackHandlerOwner, WebNavigationOwner {
  val stack: Value<ChildStack<Config, Child>>
  fun onDeepLink(url: String)

  fun onBackClicked()

  sealed class Child {
    class ListChild(val component: ListComponent) : Child()
    class DetailChild(val component: DetailComponent) : Child()
  }
}

class DefaultRootComponent(
  componentContext: ComponentContext,
  deepLinkUrl: String? = null,
) : RootComponent, ComponentContext by componentContext, KoinComponent {
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
        Config.List -> "/"
        is Config.Detail -> "/${config.dishName}"
      }
    }
  )

  override fun onDeepLink(url: String) {
    navigation.navigate { getInitialStack(url) }
  }

  private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
    when (config) {
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
          navigation.pop { listEvents.trySend(ListComponent.Event.BackFromDetail(dishName)) }
        },
      ).let { DetailChild(it) }
    }

  private fun getInitialStack(deepLinkUrl: String?): List<Config> {
    Logger.d("deepLinkUrl: $deepLinkUrl")
    val url = deepLinkUrl?.let { Url(it) }
    return when (val dishName = url?.segments?.firstOrNull()) {
      null -> listOf(Config.List)
      else -> listOf(Config.List, Config.Detail(dishName = dishName))
    }
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  @Serializable
  sealed interface Config {
    @Serializable
    data object List : Config

    @Serializable
    data class Detail(val dishName: String) : Config
  }
}
