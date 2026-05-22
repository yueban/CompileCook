package com.yueban.compilecook.ui.root

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.service.UiMessage.Error
import com.yueban.compilecook.service.UiMessage.Resource
import com.yueban.compilecook.service.UiMessage.Text
import com.yueban.compilecook.ui.about.AboutContent
import com.yueban.compilecook.ui.ai.AiChatContent
import com.yueban.compilecook.ui.ai.AiChatDrawerLayout
import com.yueban.compilecook.ui.dish.DishContent
import com.yueban.compilecook.ui.dish.DishListContent
import com.yueban.compilecook.ui.main.MainContent
import com.yueban.compilecook.ui.root.RootComponent.Child.AboutChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishChild
import com.yueban.compilecook.ui.root.RootComponent.Child.DishListChild
import com.yueban.compilecook.ui.root.RootComponent.Child.MainChild
import com.yueban.compilecook.ui.root.RootComponent.Child.TipChild
import com.yueban.compilecook.ui.tip.TipContent
import com.yueban.compilecook.ui.util.stringRes
import org.jetbrains.compose.resources.getString

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
  val state by component.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val aiChatSlot by component.aiChatSlot.subscribeAsState()

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

  // Close AI drawer on back press (Escape on JVM, back gesture on Android/iOS)
  val backCallback = remember {
    object : BackCallback() {
      override fun onBack() {
        if (component.uiState.value.isDrawerOpen) {
          component.closeDrawer()
        }
      }
    }
  }
  var isCallbackRegistered by remember { mutableStateOf(false) }
  LaunchedEffect(state.isDrawerOpen, component.backHandler) {
    if (state.isDrawerOpen && !isCallbackRegistered) {
      component.backHandler.register(backCallback)
      isCallbackRegistered = true
    } else if (!state.isDrawerOpen && isCallbackRegistered) {
      component.backHandler.unregister(backCallback)
      isCallbackRegistered = false
    }
  }

  val systemUriHandler = LocalUriHandler.current
  val customUriHandler = remember(component, systemUriHandler) {
    object : UriHandler {
      override fun openUri(uri: String) {
        if (!component.onUriClicked(uri)) {
          try {
            systemUriHandler.openUri(uri)
          } catch (e: IllegalArgumentException) {
            Logger.e("Failed to open external URI: $uri", e)
          }
        }
      }
    }
  }

  AiChatDrawerLayout(
    isDrawerOpen = state.isDrawerOpen,
    onCloseDrawer = component::closeDrawer,
    mainContent = {
      CompositionLocalProvider(LocalUriHandler provides customUriHandler) {
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
          ) { child ->
            RootChild(child.instance)
          }
        }
      }
    },
    aiContent = {
      aiChatSlot.child?.instance?.let { component ->
        AiChatContent(
          component = component,
          onCameraClick = { /* TODO: Camera integration */ },
        )
      }
    },
  )
}

@Composable
private fun RootChild(child: RootComponent.Child) {
  when (child) {
    is MainChild -> MainContent(child.component)
    is TipChild -> TipContent(child.component)
    is DishListChild -> DishListContent(child.component)
    is DishChild -> DishContent(child.component)
    is AboutChild -> AboutContent(child.component)
  }
}

expect fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T>
