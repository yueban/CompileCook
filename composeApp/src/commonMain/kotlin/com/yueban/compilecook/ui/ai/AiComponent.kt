package com.yueban.compilecook.ui.ai

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.yueban.compilecook.repo.entity.AiChatContext
import com.yueban.compilecook.ui.ai.AiComponent.Child
import com.yueban.compilecook.ui.ai.AiComponent.Child.AiChatChild
import com.yueban.compilecook.ui.ai.AiComponent.Child.AiChatListChild
import com.yueban.compilecook.ui.ai.AiComponent.Config
import com.yueban.compilecook.ui.base.BaseComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

interface AiComponent : BackHandlerOwner {
  val stack: Value<ChildStack<Config, Child>>
  fun updateContext(context: AiChatContext)
  fun onBackClicked()

  @Serializable
  sealed interface Config {
    @Serializable data object Chat : Config
    @Serializable data object ChatList : Config
  }

  sealed class Child {
    class AiChatChild(val component: AiChatComponent) : Child()
    class AiChatListChild(val component: AiChatListComponent) : Child()
  }
}

class DefaultAiComponent(
  componentContext: ComponentContext,
) : AiComponent, BaseComponent(componentContext) {
  private val navigation = StackNavigation<Config>()

  override val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = Config.serializer(),
      initialConfiguration = Config.Chat,
      handleBackButton = true,
      childFactory = ::child,
    )

  private fun child(config: Config, componentContext: ComponentContext): Child =
    when (config) {
      is Config.Chat -> get<AiChatChild> { parametersOf(componentContext, config, ::onChatOutput) }
      is Config.ChatList -> get<AiChatListChild> { parametersOf(componentContext, config, ::onChatListOutput) }
    }

  private fun onChatOutput(output: AiChatComponent.Output) = when (output) {
    is AiChatComponent.Output.HistoryClicked -> navigation.push(Config.ChatList)
  }

  private fun onChatListOutput(output: AiChatListComponent.Output) = when (output) {
    is AiChatListComponent.Output.BackClicked -> navigation.pop()
    is AiChatListComponent.Output.ConversationSelected -> {
      navigation.pop()
      chatComponent?.selectConversation(output.conversationId)
    }
  }

  override fun onBackClicked() {
    navigation.pop()
  }

  override fun updateContext(context: AiChatContext) {
    chatComponent?.updateContext(context)
  }

  private val chatComponent: AiChatComponent?
    get() = (stack.active.instance as? AiChatChild)?.component
}
