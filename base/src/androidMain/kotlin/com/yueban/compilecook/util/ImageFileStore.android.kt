package com.yueban.compilecook.util

import android.content.Context
import java.io.File
import java.util.UUID

actual object ImageFileStore {
  @Volatile
  private lateinit var storageDir: File

  fun init(context: Context) {
    storageDir = File(context.filesDir, "ai_chat_images").apply { mkdirs() }
  }

  actual suspend fun save(bytes: ByteArray, prefix: String): String {
    val file = File(storageDir, "${prefix}_${UUID.randomUUID()}.jpg")
    file.writeBytes(bytes)
    return "$LOCAL_IMAGE_SCHEME${file.name}"
  }

  actual suspend fun saveFromPath(path: String, prefix: String): String {
    val file = File(storageDir, "${prefix}_${UUID.randomUUID()}.jpg")
    File(path).copyTo(file)
    return "$LOCAL_IMAGE_SCHEME${file.name}"
  }

  actual fun resolve(imageRef: String): String = if (imageRef.isRemoteImageRef()) {
    imageRef
  } else {
    fileFor(imageRef).absolutePath
  }

  actual fun readBytes(imageRef: String): ByteArray {
    val file = fileFor(imageRef)
    return if (file.exists()) file.readBytes() else ByteArray(0)
  }

  actual fun delete(imageRef: String) {
    val file = fileFor(imageRef)
    if (file.exists()) file.delete()
  }

  private fun fileFor(imageRef: String): File = if (imageRef.startsWith(LOCAL_IMAGE_SCHEME)) {
    File(storageDir, imageRef.removePrefix(LOCAL_IMAGE_SCHEME))
  } else {
    File(imageRef)
  }

  private fun String.isRemoteImageRef(): Boolean =
    startsWith("http://") || startsWith("https://") || startsWith("data:")
}
