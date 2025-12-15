package com.yueban.compilecook.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler
import com.yueban.compilecook.ui.inbox.DetailContent
import com.yueban.compilecook.ui.inbox.ListContent
import com.yueban.compilecook.ui.root.RootComponent.Child.DetailChild
import com.yueban.compilecook.ui.root.RootComponent.Child.ListChild

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
  ChildStack(
    stack = component.stack,
    modifier = modifier,
    animation = backAnimation(
      backHandler = component.backHandler,
      onBack = component::onBackClicked,
    ),
  ) {
    when (val child = it.instance) {
      is ListChild -> ListContent(component = child.component)
      is DetailChild -> DetailContent(component = child.component)
    }
  }
}

expect fun <C : Any, T : Any> backAnimation(
  backHandler: BackHandler,
  onBack: () -> Unit,
): StackAnimation<C, T>
