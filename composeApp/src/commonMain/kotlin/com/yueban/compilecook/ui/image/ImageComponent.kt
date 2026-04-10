package com.yueban.compilecook.ui.image

import com.arkivanov.decompose.ComponentContext
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.image.ImageComponent.Output.BackClicked
import kotlinx.serialization.Serializable

@Serializable
data class ImageState(
  val imageUrl: String,
)

interface ImageComponent : UiStateComponent<ImageState> {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultImageComponent(
  componentContext: ComponentContext,
  imageUrl: String,
  private val onOutput: (ImageComponent.Output) -> Unit,
) : ImageComponent, UiStateComponentImpl<ImageState>(
  componentContext = componentContext,
  initialState = ImageState(imageUrl = imageUrl),
  serializer = ImageState.serializer(),
) {
  override fun onBackClicked() = onOutput(BackClicked)
}
