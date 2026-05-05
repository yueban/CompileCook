package com.yueban.compilecook.util

import kotlinx.serialization.serializer

/**
 * Gets the @SerialName of an enum constant generically.
 */
inline fun <reified T : Enum<T>> T.serialName(): String {
  val serializer = serializer<T>()
  return serializer.descriptor.getElementName(this.ordinal)
}
