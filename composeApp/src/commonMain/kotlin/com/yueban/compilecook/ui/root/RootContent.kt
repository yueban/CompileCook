package com.yueban.compilecook.ui.root

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.yueban.compilecook.service.UiMessage.Error
import com.yueban.compilecook.service.UiMessage.Resource
import com.yueban.compilecook.service.UiMessage.Text
import com.yueban.compilecook.ui.dish.DishContent
import com.yueban.compilecook.ui.dish.DishListContent
import com.yueban.compilecook.ui.main.MainContent
import com.yueban.compilecook.ui.root.RootComponent.Child.DishChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.MainChild
import com.yueban.compilecook.ui.root.RootComponent.Child.TipChild
import com.yueban.compilecook.ui.tip.TipContent
import com.yueban.compilecook.ui.util.stringRes
import org.jetbrains.compose.resources.getString

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(component) {
    component.messages.collect { message ->
      val text = when (message) {
        is Text -> message.value

        is Resource -> {
          @Suppress("SpreadOperator")
          getString(message.res, *message.args.toTypedArray())
        }
        is Error -> getString(message.error.stringRes)
      }
      snackbarHostState.showSnackbar(text)
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { _ ->
    ChildStack(
      stack = component.stack,
      modifier = modifier,
      animation = backAnimation(
        backHandler = component.backHandler,
        onBack = component::onBackClicked,
      ),
    ) {
      when (val child = it.instance) {
        is MainChild -> MainContent(component = child.component)
        is TipChild -> TipContent(component = child.component)
        is DishListChild -> DishListContent(component = child.component)
        is DishChild -> DishContent(component = child.component)
      }
    }
  }
}

expect fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T>
