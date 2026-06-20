@file:Suppress("MatchingDeclarationName")

package com.yueban.compilecook.ui.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(ab) => Array.from(new Uint8Array(ab))")
private external fun arrayBufferToJsArray(ab: JsAny): JsArray<JsNumber>

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberImagePickerManager(onImagePicked: (ByteArray) -> Unit): ImagePickerManager {
  val currentCallback by rememberUpdatedState(onImagePicked)
  return remember {
    object : ImagePickerManager {
      override fun pickFromGallery() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "image/*"
        input.onchange = { _ ->
          val file = input.files?.item(0)
          if (file != null) {
            val reader = FileReader()
            reader.onload = { _ ->
              @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
              val jsArray = arrayBufferToJsArray(reader.result as JsAny)
              val bytes = ByteArray(jsArray.length) { jsArray[it].toString().toInt().toByte() }
              currentCallback(bytes)
            }
            reader.readAsArrayBuffer(file)
          }
        }
        input.click()
      }

      override fun capturePhoto() = Unit
      override fun isCameraAvailable(): Boolean = false
    }
  }
}
