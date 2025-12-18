package com.yueban.compilecook.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.yueban.compilecook.ui.inbox.DefaultDetailComponent
import com.yueban.compilecook.ui.inbox.DefaultListComponent
import com.yueban.compilecook.ui.inbox.DetailComponent
import com.yueban.compilecook.ui.inbox.ListComponent
import com.yueban.compilecook.ui.root.RootComponent.Child.DetailChild
import com.yueban.compilecook.ui.root.RootComponent.Child.ListChild
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable

interface RootComponent : BackHandlerOwner {
  val stack: Value<ChildStack<*, Child>>

  fun onBackClicked()
  sealed class Child {
    class ListChild(val component: ListComponent) : Child()
    class DetailChild(val component: DetailComponent) : Child()
  }
}

class DefaultRootComponent(
  componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {
  private val listEvents = Channel<ListComponent.Event>(Channel.BUFFERED)
  private val navigation = StackNavigation<Config>()
  override val stack: Value<ChildStack<*, RootComponent.Child>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialConfiguration = Config.List,
      handleBackButton = true,
      childFactory = ::child
    )

  private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
    when (config) {
      Config.List -> ListChild(listComponent(componentContext))
      is Config.Detail -> DetailChild(detailComponent(componentContext, config))
    }

  private fun listComponent(componentContext: ComponentContext): ListComponent =
    DefaultListComponent(
      componentContext = componentContext,
      eventFlow = listEvents.receiveAsFlow(),
      onItemSelected = {
        navigation.push(Config.Detail(item = it))
      }
    )

  private fun detailComponent(componentContext: ComponentContext, config: Config.Detail): DetailComponent =
    DefaultDetailComponent(
      componentContext = componentContext,
      item = config.item,
      onFinished = { item ->
        navigation.pop { listEvents.trySend(ListComponent.Event.BackFromDetail(item)) }
      },
    )

  override fun onBackClicked() {
    navigation.pop()
  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data object List : Config

    @Serializable
    data class Detail(val item: String) : Config
  }
}
