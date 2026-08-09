package com.yueban.compilecook.ui.image

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.util.IMAGE_PREVIEW_TRANSITION_DURATION

private const val IMAGE_PREVIEW_OVERLAY_LABEL = "IMAGE_PREVIEW_OVERLAY"

@Composable
internal fun ImagePreviewOverlay(
  imageComponent: ImageComponent?,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    targetState = imageComponent,
    transitionSpec = {
      (
        fadeIn(
          animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
          initialAlpha = 0f,
        ) togetherWith fadeOut(
          animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
        )
        ) using null
    },
    modifier = modifier,
    label = IMAGE_PREVIEW_OVERLAY_LABEL,
  ) { previewComponent ->
    previewComponent?.let {
      ImageContent(
        component = it,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}
