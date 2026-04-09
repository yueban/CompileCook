package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.yueban.compilecook.ui.theme.AppTheme

object Coil3ImageTransformerImpl : ImageTransformer {
  @Composable
  override fun transform(link: String): ImageData {
    return rememberAsyncImagePainter(
      model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(link)
        .size(coil3.size.Size.ORIGINAL)
        .build()
    ).let { ImageData(it) }
  }

  @Composable
  override fun intrinsicSize(painter: Painter): Size {
    val density = LocalDensity.current
    val fixedHeightPx = with(density) { AppTheme.dimens.markdownImageHeight.toPx() }

    var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
    if (painter is AsyncImagePainter) {
      val painterState = painter.state.collectAsState()
      val intrinsicSize = painterState.value.painter?.intrinsicSize
      intrinsicSize?.also { size = it }
    }

    if (size.height > 0 && size.width > 0) {
      val aspect = size.width / size.height
      return Size(fixedHeightPx * aspect, fixedHeightPx)
    }

    // Default size while loading to prevent zero-size placeholders and overlapping
    // Use a reasonable aspect ratio like 1:1
    return Size(fixedHeightPx, fixedHeightPx)
  }
}
