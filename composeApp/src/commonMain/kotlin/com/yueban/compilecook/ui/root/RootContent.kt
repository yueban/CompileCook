package com.yueban.compilecook.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.yueban.compilecook.ui.inbox.DetailContent
import com.yueban.compilecook.ui.inbox.ListContent
import com.yueban.compilecook.ui.root.RootComponent.Child.DetailChild
import com.yueban.compilecook.ui.root.RootComponent.Child.ListChild

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
  Children(
    stack = component.stack,
    modifier = modifier,
    animation = stackAnimation(fade())
  ) {
    when (val child = it.instance) {
      is ListChild -> ListContent(component = child.component)
      is DetailChild -> DetailContent(component = child.component)
    }
  }
}
