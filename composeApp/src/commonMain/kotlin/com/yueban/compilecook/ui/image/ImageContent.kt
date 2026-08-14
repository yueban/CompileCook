package com.yueban.compilecook.ui.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.yueban.compilecook.ui.util.IMAGE_PREVIEW_TRANSITION_DURATION
import com.yueban.compilecook.ui.util.LocalImagePreviewSourceBounds
import com.yueban.compilecook.ui.util.imagePreviewSharedElementTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DISMISS_THRESHOLD_FACTOR = 4f
private const val SENSITIVITY_FACTOR = 3f
private const val SCALE_FRACTION = 0.5f
private const val MIN_FLYBACK_SCALE = 0.01f
private const val OVERLAY_MAX_ALPHA = 1f

@Composable
fun ImageContent(
  component: ImageComponent,
  cornerRadius: Dp = 0.dp,
  sourceCornerRadius: Dp = 0.dp,
  modifier: Modifier = Modifier,
) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val dragToDismissState = rememberDragToDismissState(onDismiss = component::onBackClicked)
  var imagePainterState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
  var containerBounds by remember { mutableStateOf<Rect?>(null) }
  val sourceBounds = LocalImagePreviewSourceBounds.current[state.imageUrl]

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .drawBehind {
        drawRect(Color.Black.copy(alpha = dragToDismissState.alpha))
      }
      .onGloballyPositioned { coordinates ->
        containerBounds = coordinates.boundsInRoot()
      }
      .pointerInput(sourceBounds, containerBounds) {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            dragToDismissState.onDrag(dragAmount, size)
          },
          onDragEnd = {
            dragToDismissState.onDragEnd(
              containerSize = size,
              containerBounds = containerBounds,
              sourceBounds = sourceBounds,
            )
          }
        )
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {
          if (!dragToDismissState.isDismissing) {
            dragToDismissState.dismissToSource(
              containerBounds = containerBounds,
              sourceBounds = sourceBounds,
            )
          }
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    val manualCornerRadius by animateDpAsState(
      targetValue = if (dragToDismissState.isDismissing) sourceCornerRadius else 0.dp,
      animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
      label = "image_preview_manual_corner_radius",
    )
    val effectiveCornerRadius = if (dragToDismissState.isDismissing) {
      manualCornerRadius
    } else {
      cornerRadius
    }

    val contentSize = calculateFittedSize(imagePainterState, constraints.maxWidth, constraints.maxHeight)
    dragToDismissState.updateContentSize(contentSize)

    FullscreenImage(
      imageUrl = state.imageUrl,
      contentSize = contentSize,
      dragToDismissState = dragToDismissState,
      cornerRadius = effectiveCornerRadius,
      onState = { imagePainterState = it }
    )
  }
}

@Composable
private fun FullscreenImage(
  imageUrl: String,
  contentSize: IntSize?,
  dragToDismissState: DragToDismissState,
  cornerRadius: Dp,
  onState: (AsyncImagePainter.State) -> Unit,
) {
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
        if (dragToDismissState.isDismissing) {
          Modifier
        } else {
          Modifier.imagePreviewSharedElementTarget(imageUrl)
        }
      )
      .clip(RoundedCornerShape(cornerRadius)),
  ) {
    val imageModel = rememberImageModel(imageUrl)
    AsyncImage(
      model = imageModel,
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
  private val _alpha = Animatable(OVERLAY_MAX_ALPHA)
  private var contentSize: IntSize? = null
  private var _isDismissing by mutableStateOf(false)

  val offsetX: Float get() = _offsetX.value
  val offsetY: Float get() = _offsetY.value
  val scale: Float get() = _scale.value
  val alpha: Float get() = _alpha.value
  val isDismissing: Boolean get() = _isDismissing

  fun updateContentSize(contentSize: IntSize?) {
    this.contentSize = contentSize
  }

  fun onDrag(dragAmount: Offset, containerSize: IntSize) {
    if (_isDismissing) return

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
      _alpha.snapTo(((1f - progress) * OVERLAY_MAX_ALPHA).coerceIn(0f, OVERLAY_MAX_ALPHA))
    }
  }

  fun onDragEnd(
    containerSize: IntSize,
    containerBounds: Rect?,
    sourceBounds: Rect?,
  ) {
    if (_isDismissing) return

    scope.launch {
      val shouldDismiss = abs(_offsetY.value) > containerSize.height / DISMISS_THRESHOLD_FACTOR ||
        abs(_offsetX.value) > containerSize.width / DISMISS_THRESHOLD_FACTOR

      if (shouldDismiss) {
        dismissToSource(containerBounds, sourceBounds)
      } else {
        coroutineScope {
          launch { _offsetX.animateTo(0f) }
          launch { _offsetY.animateTo(0f) }
          launch { _scale.animateTo(1f) }
          launch { _alpha.animateTo(OVERLAY_MAX_ALPHA) }
        }
      }
    }
  }

  fun dismissToSource(containerBounds: Rect?, sourceBounds: Rect?) {
    if (_isDismissing) return

    scope.launch {
      val currentContentSize = contentSize
      val flybackTarget = if (currentContentSize != null && containerBounds != null && sourceBounds != null) {
        val targetScale = min(
          sourceBounds.width / currentContentSize.width,
          sourceBounds.height / currentContentSize.height,
        ).coerceAtLeast(MIN_FLYBACK_SCALE)
        FlybackTarget(
          offsetX = sourceBounds.center.x - containerBounds.center.x,
          offsetY = sourceBounds.center.y - containerBounds.center.y,
          scale = targetScale,
        )
      } else {
        null
      }

      if (flybackTarget == null) {
        onDismiss()
        return@launch
      }

      _isDismissing = true
      coroutineScope {
        launch {
          _offsetX.animateTo(
            targetValue = flybackTarget.offsetX,
            animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
          )
        }
        launch {
          _offsetY.animateTo(
            targetValue = flybackTarget.offsetY,
            animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
          )
        }
        launch {
          _scale.animateTo(
            targetValue = flybackTarget.scale,
            animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
          )
        }
        launch {
          _alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(IMAGE_PREVIEW_TRANSITION_DURATION),
          )
        }
      }
      onDismiss()
    }
  }
}

private data class FlybackTarget(
  val offsetX: Float,
  val offsetY: Float,
  val scale: Float,
)

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
