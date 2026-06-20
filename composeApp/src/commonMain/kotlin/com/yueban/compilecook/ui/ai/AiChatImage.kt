package com.yueban.compilecook.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_choose_gallery
import compilecook.composeapp.generated.resources.ai_chat_des_remove_image
import compilecook.composeapp.generated.resources.ai_chat_take_photo
import org.jetbrains.compose.resources.stringResource

/**
 * Displays an image from [ImageFileCache] path.
 *
 * On most platforms this delegates to Coil's [AsyncImage].
 * On wasmJS, where cache paths use a custom `mem://` scheme that Coil cannot resolve,
 * bytes are converted to `data:` URIs that Coil's [DataUriFetcher][coil3.decode.DataUriFetcher] handles natively.
 */
@Composable
expect fun AiChatImage(
  path: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
)

@Composable
internal fun ImagePickerSheet(
  isCameraAvailable: Boolean,
  onTakePhoto: () -> Unit,
  onPickFromGallery: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState()
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(modifier = Modifier.padding(AppTheme.dimens.screenPadding)) {
      if (isCameraAvailable) {
        ImagePickerOption(
          icon = Icons.Default.PhotoCamera,
          text = stringResource(Res.string.ai_chat_take_photo),
          onClick = {
            onDismiss()
            onTakePhoto()
          },
        )
      }
      ImagePickerOption(
        icon = Icons.Default.PhotoLibrary,
        text = stringResource(Res.string.ai_chat_choose_gallery),
        onClick = {
          onDismiss()
          onPickFromGallery()
        },
      )
      Spacer(modifier = Modifier.height(AppTheme.dimens.screenPadding))
    }
  }
}

@Composable
private fun ImagePickerOption(
  icon: ImageVector,
  text: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = AppTheme.dimens.screenPadding, vertical = AppTheme.dimens.mediumGap),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.mediumGap),
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
    Text(text = text, style = AppTheme.typography.bodyLarge)
  }
}

@Composable
internal fun PendingImageThumbnail(imagePath: String, onRemove: () -> Unit) {
  Box(modifier = Modifier.size(AppTheme.dimens.aiChatImageThumbnailSize)) {
    AiChatImage(
      path = imagePath,
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(AppTheme.dimens.radiusSmall)),
    )
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .size(AppTheme.dimens.iconMedium)
        .clip(CircleShape)
        .background(AppTheme.colorScheme.scrim)
        .clickable(onClick = onRemove),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(Res.string.ai_chat_des_remove_image),
        tint = Color.White,
        modifier = Modifier.size(AppTheme.dimens.iconSmall),
      )
    }
  }
}

@Composable
internal fun CompressingImagePlaceholder() {
  Box(
    modifier = Modifier
      .size(AppTheme.dimens.aiChatImageThumbnailSize)
      .clip(RoundedCornerShape(AppTheme.dimens.radiusSmall))
      .background(AppTheme.colorScheme.surfaceVariant),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(AppTheme.dimens.iconMedium),
      strokeWidth = AppTheme.dimens.aiChatLoadingStroke,
    )
  }
}
