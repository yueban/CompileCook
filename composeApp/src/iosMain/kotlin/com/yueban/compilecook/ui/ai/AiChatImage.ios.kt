package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

@Composable
actual fun AiChatImage(
  path: String,
  contentDescription: String?,
  modifier: Modifier,
  contentScale: ContentScale,
  onState: (AsyncImagePainter.State) -> Unit,
) {
  AsyncImage(
    model = path,
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
    onState = onState,
  )
}
