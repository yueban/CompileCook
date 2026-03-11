@file:Suppress("unused")

package com.yueban.compilecook.util

import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.yueban.compilecook.json.json
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

@Suppress("unused") // Used in Swift
fun save(coder: NSCoder, state: SerializableContainer) {
  coder.encodeObject(`object` = json.encodeToString(SerializableContainer.serializer(), state), forKey = "state")
}

@Suppress("unused") // Used in Swift
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun restore(coder: NSCoder): SerializableContainer? =
  (coder.decodeTopLevelObjectOfClass(aClass = NSString, forKey = "state", error = null) as String?)?.let {
    try {
      json.decodeFromString(SerializableContainer.serializer(), it)
    } catch (_: Exception) {
      null
    }
  }
