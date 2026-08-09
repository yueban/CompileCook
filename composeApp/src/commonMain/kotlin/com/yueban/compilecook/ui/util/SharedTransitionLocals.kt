package com.yueban.compilecook.ui.util

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

internal const val IMAGE_PREVIEW_TRANSITION_DURATION = 300

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalImagePreviewActive = staticCompositionLocalOf { false }

val LocalImagePreviewSourceBounds =
  staticCompositionLocalOf<SnapshotStateMap<String, Rect>> { mutableStateMapOf() }

private data class ImagePreviewSharedContentKey(val imageUrl: String)

@Composable
internal fun Modifier.imagePreviewSharedElementSource(imageUrl: String): Modifier {
  val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
  val isPreviewActive = LocalImagePreviewActive.current

  return with(sharedTransitionScope) {
    this@imagePreviewSharedElementSource.sharedElementWithCallerManagedVisibility(
      sharedContentState = rememberSharedContentState(ImagePreviewSharedContentKey(imageUrl)),
      visible = !isPreviewActive,
      boundsTransform = { _, _ -> tween(IMAGE_PREVIEW_TRANSITION_DURATION) },
    )
  }
}

@Composable
internal fun Modifier.imagePreviewSharedElementTarget(imageUrl: String): Modifier {
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val isPreviewActive = LocalImagePreviewActive.current

  return if (sharedTransitionScope == null) {
    this
  } else {
    with(sharedTransitionScope) {
      this@imagePreviewSharedElementTarget.sharedElementWithCallerManagedVisibility(
        sharedContentState = rememberSharedContentState(ImagePreviewSharedContentKey(imageUrl)),
        visible = isPreviewActive,
        boundsTransform = { _, _ -> tween(IMAGE_PREVIEW_TRANSITION_DURATION) },
      )
    }
  }
}

@Composable
internal fun Modifier.imagePreviewSourceBounds(imageUrl: String): Modifier {
  val sourceBounds = LocalImagePreviewSourceBounds.current
  return this.onGloballyPositioned { coordinates ->
    sourceBounds[imageUrl] = coordinates.boundsInRoot()
  }
}
