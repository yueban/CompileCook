package com.yueban.compilecook.util

import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

private val json = Json {
  allowStructuredMapKeys = true
}

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
