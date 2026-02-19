package com.yueban.compilecook.ui.about

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.ui.about.AboutComponent.Output
import com.yueban.compilecook.ui.about.AboutComponent.Output.BackClicked
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.BaseComponent

interface AboutComponent {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultAboutComponent(
  componentContext: ComponentContext,
  private val onOutput: (Output) -> Unit,
) : AboutComponent, BaseComponent(componentContext) {
  override fun onBackClicked() = onOutput(BackClicked)
}
