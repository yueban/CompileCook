package com.yueban.compilecook.ui.widget

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.util.IconSource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AnyIcon(
  source: IconSource,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) = when (source) {
  is IconSource.Resource ->
    Icon(
      painter = painterResource(source.res),
      contentDescription = contentDescription,
      modifier = modifier,
      tint = source.tint
    )
  is IconSource.Vector ->
    Icon(
      imageVector = source.imageVector,
      contentDescription = contentDescription,
      modifier = modifier,
      tint = source.tint ?: LocalContentColor.current
    )
}
