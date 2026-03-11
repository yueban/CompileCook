package com.yueban.compilecook.util

import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.yueban.compilecook.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun SerializableContainer.writeToFile(file: File) {
  file.outputStream().use { output ->
    json.encodeToStream(SerializableContainer.serializer(), this, output)
  }
}

@OptIn(ExperimentalSerializationApi::class)
fun File.readSerializableContainer(): SerializableContainer? =
  takeIf(File::exists)?.inputStream()?.use { input ->
    try {
      json.decodeFromStream(SerializableContainer.serializer(), input)
    } catch (_: Exception) {
      null
    }
  }
