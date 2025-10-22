package com.yueban.compilecook.json

import kotlinx.serialization.json.Json

val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  encodeDefaults = true
}

inline fun <reified T> T.toJson(): String = json.encodeToString(this)

inline fun <reified T> String.toEntity(): T? {
  if (this.isBlank()) {
    return null
  }
  // Use runCatching to handle potential SerializationException and return null on failure
  return runCatching {
    json.decodeFromString<T>(this)
  }.getOrNull()
}
