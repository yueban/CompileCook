package com.yueban.compilecook.ui.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalWidgetPreview
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_toggle_favorite
import org.jetbrains.compose.resources.stringResource

@Composable
fun FavoriteButton(
  isFavorite: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  IconButton(onClick = onClick, modifier = modifier) {
    Icon(
      imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
      contentDescription = stringResource(Res.string.common_des_toggle_favorite),
      tint = if (isFavorite) AppTheme.colors.favorite else AppTheme.colorScheme.onSurfaceVariant
    )
  }
}

@UniversalWidgetPreview
@Composable
private fun PreviewFavoriteButton() = PreviewWrapper {
  FavoriteButton(
    isFavorite = true,
    onClick = { },
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewFavoriteButton_NotFavorite() = PreviewWrapper {
  FavoriteButton(
    isFavorite = false,
    onClick = { },
  )
}
