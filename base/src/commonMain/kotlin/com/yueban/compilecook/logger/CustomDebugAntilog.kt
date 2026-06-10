package com.yueban.compilecook.logger

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

expect class CustomDebugAntilog(minLogLevel: LogLevel) : Antilog {
  override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?)
}
