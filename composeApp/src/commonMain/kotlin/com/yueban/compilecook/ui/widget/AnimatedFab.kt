package com.yueban.compilecook.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalWidgetPreview
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_toc_fab
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnimatedFab(
  isVisible: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) = AnimatedVisibility(
  visible = isVisible,
  enter = scaleIn() + fadeIn(),
  exit = scaleOut() + fadeOut(),
  // compensate for the inner padding to maintain the FAB's original screen position
  modifier = modifier.offset(x = AppTheme.dimens.screenPadding, y = AppTheme.dimens.screenPadding)
) {
  // expand the animation canvas bounds to prevent shadow from being clipped
  Box(modifier = Modifier.padding(AppTheme.dimens.screenPadding)) {
    FloatingActionButton(
      onClick = onClick,
      containerColor = AppTheme.colorScheme.primaryContainer,
      contentColor = AppTheme.colorScheme.onPrimaryContainer,
      shape = CircleShape,
    ) {
      Icon(
        Icons.AutoMirrored.Filled.List,
        contentDescription = stringResource(Res.string.common_des_toc_fab)
      )
    }
  }
}

@UniversalWidgetPreview
@Composable
private fun PreviewAnimatedFab() = PreviewWrapper {
  AnimatedFab(isVisible = true, onClick = {}, modifier = Modifier.padding(AppTheme.dimens.screenPadding))
}
