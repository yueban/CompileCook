package com.yueban.compilecook.logger

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.LogLevel

actual class CustomDebugAntilog actual constructor(private val minLogLevel: LogLevel) : Antilog() {
  private val delegate = DebugAntilog()

  override fun isEnable(priority: LogLevel, tag: String?) = priority >= minLogLevel

  actual override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
    delegate.log(priority, tag, throwable, message)
  }
}
