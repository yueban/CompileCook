package com.yueban.compilecook.di

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.service.DefaultMessageService
import com.yueban.compilecook.service.MessageService
import com.yueban.compilecook.ui.about.AboutComponent
import com.yueban.compilecook.ui.about.DefaultAboutComponent
import com.yueban.compilecook.ui.ai.AiChatComponent
import com.yueban.compilecook.ui.ai.AiChatListComponent
import com.yueban.compilecook.ui.ai.AiComponent
import com.yueban.compilecook.ui.ai.AiComponent.Child.AiChatChild
import com.yueban.compilecook.ui.ai.AiComponent.Child.AiChatListChild
import com.yueban.compilecook.ui.ai.DefaultAiChatComponent
import com.yueban.compilecook.ui.ai.DefaultAiChatListComponent
import com.yueban.compilecook.ui.ai.DefaultAiComponent
import com.yueban.compilecook.ui.dish.DefaultDishComponent
import com.yueban.compilecook.ui.dish.DefaultDishListComponent
import com.yueban.compilecook.ui.dish.DishComponent
import com.yueban.compilecook.ui.dish.DishListComponent
import com.yueban.compilecook.ui.main.DefaultMainComponent
import com.yueban.compilecook.ui.main.MainComponent
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config
import com.yueban.compilecook.ui.root.RootComponent.Child.AboutChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.MainChild
import com.yueban.compilecook.ui.root.RootComponent.Child.TipChild
import com.yueban.compilecook.ui.tip.DefaultTipComponent
import com.yueban.compilecook.ui.tip.TipComponent
import org.koin.dsl.module

val appModule = module {
  single<MessageService> { DefaultMessageService() }
}

val uiModule = module {
  factory { (ctx: ComponentContext, config: Config.Main, onOut: (MainComponent.Output) -> Unit) ->
    MainChild(
      DefaultMainComponent(ctx, config.initialTab, onOut, get())
    )
  }

  factory { (ctx: ComponentContext, config: Config.Tip, onOut: (TipComponent.Output) -> Unit) ->
    TipChild(
      DefaultTipComponent(ctx, config.tipName, onOut, get())
    )
  }

  factory { (ctx: ComponentContext, config: Config.DishList, onOut: (DishListComponent.Output) -> Unit) ->
    DishListChild(
      DefaultDishListComponent(ctx, config.source, onOut, get())
    )
  }

  factory { (ctx: ComponentContext, config: Config.Dish, onOut: (DishComponent.Output) -> Unit) ->
    DishChild(
      DefaultDishComponent(ctx, config.dishName, onOut, get())
    )
  }

  factory { (ctx: ComponentContext, config: Config.About, onOut: (AboutComponent.Output) -> Unit) ->
    AboutChild(
      DefaultAboutComponent(ctx, onOut)
    )
  }

  factory<AiComponent> { (ctx: ComponentContext, onOutput: (AiComponent.Output) -> Unit) ->
    DefaultAiComponent(ctx, onOutput)
  }

  factory { (ctx: ComponentContext, config: AiComponent.Config.Chat, onOutput: (AiChatComponent.Output) -> Unit) ->
    AiChatChild(DefaultAiChatComponent(ctx, get(), onOutput))
  }

  factory {
      (
        ctx: ComponentContext,
        config: AiComponent.Config.ChatList,
        onOutput: (AiChatListComponent.Output) -> Unit,
      ),
    ->
    AiChatListChild(DefaultAiChatListComponent(ctx, get(), onOutput))
  }
}
