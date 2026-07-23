@file:Suppress("MatchingDeclarationName", "Filename")

package com.yueban.compilecook.ui.ai

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_message
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_settings
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_title
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import io.github.ismoy.imagepickerkmp.domain.config.GalleryConfig
import io.github.ismoy.imagepickerkmp.domain.config.PermissionAndConfirmationConfig
import io.github.ismoy.imagepickerkmp.domain.models.MimeType
import io.github.ismoy.imagepickerkmp.domain.models.PhotoResult
import io.github.ismoy.imagepickerkmp.features.imagepicker.config.ImagePickerKMPConfig
import io.github.ismoy.imagepickerkmp.features.imagepicker.model.ImagePickerResult
import io.github.ismoy.imagepickerkmp.features.imagepicker.ui.rememberImagePickerKMP
import org.jetbrains.compose.resources.stringResource

interface ImagePickerManager {
  fun capturePhoto()
  fun pickFromGallery()
  fun isCameraAvailable(): Boolean
}

@Composable
fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager {
  val currentCallback = rememberUpdatedState(onImagePicked)
  val picker = rememberImagePickerKMP(
    config = ImagePickerKMPConfig(
      galleryConfig = GalleryConfig(
        allowMultiple = false,
        redactGpsData = true,
        mimeTypes = listOf(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG, MimeType.IMAGE_WEBP),
      ),
      permissionAndConfirmationConfig = PermissionAndConfirmationConfig(
        skipConfirmation = true,
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
      val bytes = result.first?.loadBytesSuspend()?.takeIf { it.isNotEmpty() } ?: return@LaunchedEffect
      currentCallback.value(bytes)
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
internal expect suspend fun PhotoResult.loadBytesSuspend(): ByteArray
