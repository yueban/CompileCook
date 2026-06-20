@file:Suppress("MatchingDeclarationName")

package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager {
  val currentCallback by rememberUpdatedState(onImagePicked)
  return remember {
    object : ImagePickerManager {
      override fun pickFromGallery() {
        val chooser = JFileChooser().apply {
          fileFilter = FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "webp")
          isMultiSelectionEnabled = false
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          val bytes = chooser.selectedFile?.readBytes()
          if (bytes != null) currentCallback(bytes)
        }
      }

      override fun capturePhoto() = Unit
      override fun isCameraAvailable(): Boolean = false
    }
  }
}
