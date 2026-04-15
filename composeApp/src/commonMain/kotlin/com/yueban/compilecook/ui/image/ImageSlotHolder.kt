package com.yueban.compilecook.ui.image

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.yueban.compilecook.ui.base.BackOutput
import kotlinx.serialization.builtins.serializer

class ImageSlotHolder(
  componentContext: ComponentContext,
  key: String,
) {
  private val imageNavigation = SlotNavigation<String>()

  val slot: Value<ChildSlot<String, ImageComponent>> =
    componentContext.childSlot(
      source = imageNavigation,
      serializer = String.serializer(),
      key = key,
      handleBackButton = true,
      childFactory = { imageUrl, childContext ->
        DefaultImageComponent(
          componentContext = childContext,
          imageUrl = imageUrl,
          onOutput = ::onImageOutput,
        )
      }
    )

  fun show(imageUrl: String) = imageNavigation.activate(imageUrl)

  private fun onImageOutput(output: ImageComponent.Output) {
    if (output is BackOutput) {
      imageNavigation.dismiss()
    }
  }
}
