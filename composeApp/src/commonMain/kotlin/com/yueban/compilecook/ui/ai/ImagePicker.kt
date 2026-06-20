@file:Suppress("MatchingDeclarationName", "Filename")

package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable

interface ImagePickerManager {
  fun capturePhoto()
  fun pickFromGallery()
  fun isCameraAvailable(): Boolean
}

@Composable
expect fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager
