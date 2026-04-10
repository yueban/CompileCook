package com.yueban.compilecook.ui.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.yueban.compilecook.ui.root.LocalNavAnimatedVisibilityScope
import com.yueban.compilecook.ui.root.LocalSharedTransitionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DISMISS_THRESHOLD_FACTOR = 4f
private const val SENSITIVITY_FACTOR = 3f
private const val SCALE_FRACTION = 0.5f
private const val TRANSITION_DURATION = 300

@Composable
fun ImageContent(component: ImageComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val dragToDismissState = rememberDragToDismissState(onDismiss = component::onBackClicked)
  var imagePainterState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .pointerInput(Unit) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            dragToDismissState.onDrag(dragAmount, size)
          },
          onDragEnd = { dragToDismissState.onDragEnd(size) }
        )
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { component.onBackClicked() }
      ),
    contentAlignment = Alignment.Center
  ) {
    val contentSize = calculateFittedSize(imagePainterState, constraints.maxWidth, constraints.maxHeight)

    FullscreenImage(
      imageUrl = state.imageUrl,
      contentSize = contentSize,
      dragToDismissState = dragToDismissState,
      onState = { imagePainterState = it }
    )
  }
}

@Composable
private fun FullscreenImage(
  imageUrl: String,
  contentSize: IntSize?,
  dragToDismissState: DragToDismissState,
  onState: (AsyncImagePainter.State) -> Unit,
) {
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

  Box(
    modifier = Modifier
      .offset { IntOffset(dragToDismissState.offsetX.roundToInt(), dragToDismissState.offsetY.roundToInt()) }
      .then(
        if (contentSize != null) {
          val s = dragToDismissState.scale
          Modifier.requiredSize(
            with(LocalDensity.current) { (contentSize.width * s).toDp() },
            with(LocalDensity.current) { (contentSize.height * s).toDp() }
          )
        } else {
          Modifier.fillMaxSize()
        }
      )
      .then(
        with(sharedTransitionScope) {
          Modifier.sharedElement(
            rememberSharedContentState(key = "image_$imageUrl"),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
          )
        }
      )
  ) {
    AsyncImage(
      model = imageUrl,
      contentDescription = null,
      onState = onState,
      contentScale = ContentScale.Fit,
      modifier = Modifier.fillMaxSize()
    )
  }
}

@Stable
private class DragToDismissState(
  private val scope: CoroutineScope,
  private val onDismiss: () -> Unit,
) {
  private val _offsetX = Animatable(0f)
  private val _offsetY = Animatable(0f)
  private val _scale = Animatable(1f)

  val offsetX: Float get() = _offsetX.value
  val offsetY: Float get() = _offsetY.value
  val scale: Float get() = _scale.value

  fun onDrag(dragAmount: Offset, containerSize: IntSize) {
    scope.launch {
      val newX = _offsetX.value + dragAmount.x
      val newY = _offsetY.value + dragAmount.y
      _offsetX.snapTo(newX)
      _offsetY.snapTo(newY)

      val progress = max(
        abs(newX) / (containerSize.width / SENSITIVITY_FACTOR),
        abs(newY) / (containerSize.height / SENSITIVITY_FACTOR)
      ).coerceIn(0f, 1f)

      _scale.snapTo((1f - progress * SCALE_FRACTION).coerceIn(1f - SCALE_FRACTION, 1f))
    }
  }

  fun onDragEnd(containerSize: IntSize) {
    scope.launch {
      val shouldDismiss = abs(_offsetY.value) > containerSize.height / DISMISS_THRESHOLD_FACTOR ||
        abs(_offsetX.value) > containerSize.width / DISMISS_THRESHOLD_FACTOR

      if (shouldDismiss) {
        onDismiss()
      } else {
        launch { _offsetX.animateTo(0f) }
        launch { _offsetY.animateTo(0f) }
        launch { _scale.animateTo(1f) }
      }
    }
  }
}

@Composable
private fun rememberDragToDismissState(onDismiss: () -> Unit): DragToDismissState {
  val scope = rememberCoroutineScope()
  return remember(onDismiss) { DragToDismissState(scope, onDismiss) }
}

private fun calculateFittedSize(painterState: AsyncImagePainter.State, maxWidth: Int, maxHeight: Int): IntSize? {
  val (width, height) = (painterState as? AsyncImagePainter.State.Success)?.painter?.intrinsicSize
    ?.takeIf { it.width > 0 && it.height > 0 } ?: return null
  val scaleFactor = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
  return IntSize((width * scaleFactor).roundToInt(), (height * scaleFactor).roundToInt())
}
