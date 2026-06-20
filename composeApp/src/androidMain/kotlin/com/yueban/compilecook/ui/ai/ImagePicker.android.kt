@file:Suppress("MatchingDeclarationName")

package com.yueban.compilecook.ui.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_message
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_settings
import compilecook.composeapp.generated.resources.ai_chat_camera_permission_title
import compilecook.composeapp.generated.resources.ai_chat_cancel
import compilecook.composeapp.generated.resources.ai_chat_confirm
import org.jetbrains.compose.resources.stringResource
import java.io.File

private enum class PermissionDialog { NONE, RATIONALE, SETTINGS }

@Composable
actual fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager {
  val context = LocalContext.current
  val currentCallback by rememberUpdatedState(onImagePicked)
  var dialogState by remember { mutableStateOf(PermissionDialog.NONE) }

  val captureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture(),
  ) { success ->
    val tempFile = captureTempFile
    captureTempFile = null
    if (!success) {
      tempFile?.delete()
      return@rememberLauncherForActivityResult
    }
    if (tempFile == null) return@rememberLauncherForActivityResult
    val bytes = readAndDeleteTempFile(tempFile)
    if (bytes != null) currentCallback(bytes)
  }

  fun launchCapture() {
    val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    captureTempFile = file
    captureLauncher.launch(uri)
  }

  val settingsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
      == PackageManager.PERMISSION_GRANTED
    ) {
      launchCapture()
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      launchCapture()
    } else {
      val activity = context as? Activity ?: return@rememberLauncherForActivityResult
      dialogState = if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
        PermissionDialog.RATIONALE
      } else {
        PermissionDialog.SETTINGS
      }
    }
  }

  when (dialogState) {
    PermissionDialog.RATIONALE -> {
      AlertDialog(
        onDismissRequest = { dialogState = PermissionDialog.NONE },
        title = { Text(stringResource(Res.string.ai_chat_camera_permission_title)) },
        text = { Text(stringResource(Res.string.ai_chat_camera_permission_message)) },
        confirmButton = {
          TextButton(onClick = {
            dialogState = PermissionDialog.NONE
            permissionLauncher.launch(Manifest.permission.CAMERA)
          }) {
            Text(stringResource(Res.string.ai_chat_confirm))
          }
        },
        dismissButton = {
          TextButton(onClick = { dialogState = PermissionDialog.NONE }) {
            Text(stringResource(Res.string.ai_chat_cancel))
          }
        },
      )
    }
    PermissionDialog.SETTINGS -> {
      AlertDialog(
        onDismissRequest = { dialogState = PermissionDialog.NONE },
        title = { Text(stringResource(Res.string.ai_chat_camera_permission_title)) },
        text = { Text(stringResource(Res.string.ai_chat_camera_permission_message)) },
        confirmButton = {
          TextButton(onClick = {
            dialogState = PermissionDialog.NONE
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
              data = Uri.fromParts("package", context.packageName, null)
            }
            settingsLauncher.launch(intent)
          }) {
            Text(stringResource(Res.string.ai_chat_camera_permission_settings))
          }
        },
        dismissButton = {
          TextButton(onClick = { dialogState = PermissionDialog.NONE }) {
            Text(stringResource(Res.string.ai_chat_cancel))
          }
        },
      )
    }
    PermissionDialog.NONE -> Unit
  }

  val pickLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
  ) { uri ->
    val bytes = uri?.let { readUriBytes(context, it) }
    if (bytes != null) currentCallback(bytes)
  }

  return remember {
    object : ImagePickerManager {
      override fun capturePhoto() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
          == PackageManager.PERMISSION_GRANTED
        ) {
          launchCapture()
        } else {
          val activity = context as? Activity
          if (activity != null &&
            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
          ) {
            dialogState = PermissionDialog.RATIONALE
          } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
          }
        }
      }

      override fun pickFromGallery() {
        pickLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
      }

      override fun isCameraAvailable(): Boolean = true
    }
  }
}

private var captureTempFile: File? = null

private fun readAndDeleteTempFile(file: File): ByteArray? =
  try {
    if (!file.exists()) return null
    val bytes = file.readBytes()
    file.delete()
    bytes
  } catch (_: Exception) {
    file.delete()
    null
  }

private fun readUriBytes(context: android.content.Context, uri: Uri): ByteArray? =
  try {
    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
  } catch (_: Exception) {
    null
  }
