@file:Suppress("MatchingDeclarationName", "UnusedPrivateProperty")

package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager {
  // TODO: Implement with PHPickerViewController / UIImagePickerController
  val currentCallback by rememberUpdatedState(onImagePicked)
  return remember {
    object : ImagePickerManager {
      override fun capturePhoto() = Unit
      override fun pickFromGallery() = Unit

      // TODO: Implement camera support with PHPickerViewController / UIImagePickerController
      override fun isCameraAvailable(): Boolean = false
    }
  }
}
