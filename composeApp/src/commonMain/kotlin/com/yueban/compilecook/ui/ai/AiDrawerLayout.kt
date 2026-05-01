package com.yueban.compilecook.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val OVERLAY_AI_WIDTH_RATIO = 0.7f
private const val SIDE_BY_SIDE_AI_WIDTH_DP_TABLET = 380
private const val SIDE_BY_SIDE_AI_WIDTH_DP_DESKTOP = 420
private const val DRAGGABLE_MIN_WIDTH_DP = 280
private const val DRAGGABLE_MAX_WIDTH_DP = 600
private const val SWIPE_CLOSE_THRESHOLD = -10f

@Composable
fun AiDrawerLayout(
  isDrawerOpen: Boolean,
  onCloseDrawer: () -> Unit,
  mainContent: @Composable () -> Unit,
  aiContent: @Composable () -> Unit,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val widthDp = maxWidth
    when {
      widthDp < 600.dp -> AiDrawerOverlay(
        isDrawerOpen = isDrawerOpen,
        onCloseDrawer = onCloseDrawer,
        mainContent = mainContent,
        aiContent = aiContent,
        screenWidthDp = widthDp
      )
      widthDp < 1024.dp -> AiDrawerSideBySide(
        isDrawerOpen = isDrawerOpen,
        mainContent = mainContent,
        aiContent = aiContent,
        aiWidthDp = SIDE_BY_SIDE_AI_WIDTH_DP_TABLET,
      )
      else -> AiDrawerSideBySide(
        isDrawerOpen = isDrawerOpen,
        mainContent = mainContent,
        aiContent = aiContent,
        aiWidthDp = SIDE_BY_SIDE_AI_WIDTH_DP_DESKTOP,
        draggable = true,
      )
    }
  }
}

@Composable
private fun AiDrawerOverlay(
  isDrawerOpen: Boolean,
  onCloseDrawer: () -> Unit,
  mainContent: @Composable () -> Unit,
  aiContent: @Composable () -> Unit,
  screenWidthDp: Dp,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    mainContent()

    // Scrim - tap to close
    AnimatedVisibility(
      visible = isDrawerOpen,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.4f))
          .pointerInput(Unit) {
            detectTapGestures { onCloseDrawer() }
          }
      )
    }

    // AI Panel - aligned to the right
    val panelWidth = screenWidthDp * OVERLAY_AI_WIDTH_RATIO
    AnimatedVisibility(
      visible = isDrawerOpen,
      enter = slideInHorizontally { it },
      exit = slideOutHorizontally { it },
      modifier = Modifier
        .fillMaxHeight()
        .width(panelWidth)
        .align(Alignment.CenterEnd),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.White)
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              if (dragAmount.x < SWIPE_CLOSE_THRESHOLD) {
                onCloseDrawer()
              }
              change.consume()
            }
          }
      ) {
        aiContent()
      }
    }
  }
}

@Composable
private fun AiDrawerSideBySide(
  isDrawerOpen: Boolean,
  mainContent: @Composable () -> Unit,
  aiContent: @Composable () -> Unit,
  aiWidthDp: Int,
  draggable: Boolean = false,
) {
  val density = LocalDensity.current

  if (draggable) {
    val currentWidthDp = remember(aiWidthDp) { mutableFloatStateOf(aiWidthDp.toFloat()) }
    var startWidth by remember { mutableFloatStateOf(aiWidthDp.toFloat()) }
    var accumulatedPx by remember { mutableFloatStateOf(0f) }
    var clampedOffset by remember { mutableFloatStateOf(0f) }
    val targetWidth = if (isDrawerOpen) currentWidthDp.value.dp else 0.dp
    val animatedWidth by animateDpAsState(
      targetValue = targetWidth,
      label = "ai_panel_width",
    )

    Row(modifier = Modifier.fillMaxSize()) {
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        mainContent()
      }

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(4.dp)
          .background(Color.Gray.copy(alpha = 0.3f))
          .dragResizeCursor()
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = {
                startWidth = currentWidthDp.value
                accumulatedPx = 0f
                clampedOffset = 0f
              },
              onDrag = { change, dragAmount ->
                accumulatedPx += dragAmount.x
                val rawWidth = startWidth - (accumulatedPx + clampedOffset) / density.density
                val wasClamped = currentWidthDp.value == DRAGGABLE_MIN_WIDTH_DP.toFloat() ||
                  currentWidthDp.value == DRAGGABLE_MAX_WIDTH_DP.toFloat()
                when {
                  rawWidth < DRAGGABLE_MIN_WIDTH_DP.toFloat() -> {
                    if (!wasClamped) {
                      clampedOffset = (startWidth - DRAGGABLE_MIN_WIDTH_DP.toFloat()) * density.density - accumulatedPx
                    }
                    currentWidthDp.value = DRAGGABLE_MIN_WIDTH_DP.toFloat()
                  }
                  rawWidth > DRAGGABLE_MAX_WIDTH_DP.toFloat() -> {
                    if (!wasClamped) {
                      clampedOffset = (startWidth - DRAGGABLE_MAX_WIDTH_DP.toFloat()) * density.density - accumulatedPx
                    }
                    currentWidthDp.value = DRAGGABLE_MAX_WIDTH_DP.toFloat()
                  }
                  else -> {
                    currentWidthDp.value = rawWidth
                  }
                }
                change.consume()
              },
            )
          }
      )

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(animatedWidth)
          .background(Color.White)
      ) {
        aiContent()
      }
    }
  } else {
    val targetWidthDp = if (isDrawerOpen) aiWidthDp.dp else 0.dp
    val animatedWidth by animateDpAsState(
      targetValue = targetWidthDp,
      label = "ai_panel_width",
    )

    Row(modifier = Modifier.fillMaxSize()) {
      Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
        mainContent()
      }

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(animatedWidth)
          .background(Color.White)
      ) {
        aiContent()
      }
    }
  }
}
