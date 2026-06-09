package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.yueban.compilecook.ui.root.backAnimation

@Composable
fun AiContent(
  component: AiComponent,
  modifier: Modifier = Modifier,
) {
  ChildStack(
    stack = component.stack,
    modifier = modifier,
    animation = backAnimation(
      backHandler = component.backHandler,
      onBack = component::onBackClicked,
    ),
  ) { child ->
    AiChild(child.instance)
  }
}

@Composable
private fun AiChild(child: AiComponent.Child) {
  when (child) {
    is AiComponent.Child.AiChatChild -> AiChatContent(child.component)
  }
}
