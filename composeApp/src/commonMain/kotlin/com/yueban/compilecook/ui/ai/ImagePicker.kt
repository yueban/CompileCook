@file:Suppress("MatchingDeclarationName", "Filename")

package com.yueban.compilecook.ui.ai

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.util.ImageSource
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_message
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_settings
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_title
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import io.github.ismoy.imagepickerkmp.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.config.GalleryConfig
import io.github.ismoy.imagepickerkmp.config.PermissionAndConfirmationConfig
import io.github.ismoy.imagepickerkmp.picker.ImagePickerKMPConfig
import io.github.ismoy.imagepickerkmp.picker.ImagePickerResult
import io.github.ismoy.imagepickerkmp.picker.MimeType
import io.github.ismoy.imagepickerkmp.picker.PhotoResult
import io.github.ismoy.imagepickerkmp.picker.rememberImagePickerKMP
import org.jetbrains.compose.resources.stringResource

interface ImagePickerManager {
  fun capturePhoto()
  fun pickFromGallery()
  fun isCameraAvailable(): Boolean
}

@Composable
fun rememberImagePickerManager(onImagePicked: (ImageSource) -> Unit): ImagePickerManager {
  val currentCallback = rememberUpdatedState(onImagePicked)
  val picker = rememberImagePickerKMP(
    config = ImagePickerKMPConfig(
      // disable built-in compression, we rely on our own multi-pass ImageCompressor instead
      // TODO: the built-in compression cannot be disabled on iOS platform.
      cameraCaptureConfig = CameraCaptureConfig(compressionLevel = null),
      galleryConfig = GalleryConfig(
        allowMultiple = false,
        redactGpsData = true,
        mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG, MimeType.IMAGE_WEBP),
      ),
      permissionAndConfirmationConfig = PermissionAndConfirmationConfig(
        customDeniedDialog = { onRetry, onDismiss ->
          AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.ai_chat_camera_permission_title)) },
            text = { Text(stringResource(Res.string.ai_chat_camera_permission_message)) },
            confirmButton = {
              TextButton(onClick = onRetry) {
                Text(stringResource(Res.string.ai_chat_confirm))
              }
            },
            dismissButton = {
              TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ai_chat_cancel))
              }
            },
          )
        },
        customSettingsDialog = { onOpenSettings, onDismiss ->
          AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.ai_chat_camera_permission_title)) },
            text = { Text(stringResource(Res.string.ai_chat_camera_permission_message)) },
            confirmButton = {
              TextButton(onClick = onOpenSettings) {
                Text(stringResource(Res.string.ai_chat_camera_permission_settings))
              }
            },
            dismissButton = {
              TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ai_chat_cancel))
              }
            },
          )
        },
      ),
    ),
  )

  LaunchedEffect(picker.result) {
    val result = picker.result
    if (result is ImagePickerResult.Success) {
      Logger.d("image selected: ${result.first?.uri}")
      val source = result.first?.toImageSource() ?: return@LaunchedEffect
      currentCallback.value(source)
    }
  }

  return remember {
    object : ImagePickerManager {
      override fun capturePhoto() = picker.launchCamera()
      override fun pickFromGallery() = picker.launchGallery()
      override fun isCameraAvailable(): Boolean = isCameraSupported()
    }
  }
}

internal expect fun isCameraSupported(): Boolean
internal expect suspend fun PhotoResult.toImageSource(): ImageSource?
