package com.yueban.compilecook.logger

import com.aallam.openai.client.LoggingConfig
import com.yueban.compilecook.BuildKonfig
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import org.koin.core.logger.Level
import org.koin.core.logger.MESSAGE
import com.aallam.openai.api.logging.LogLevel as OpenAiLogLevel

typealias KoinLogger = org.koin.core.logger.Logger
typealias OpenAiLogger = com.aallam.openai.api.logging.Logger

object Logger {
  fun init() {
    if (BuildKonfig.IS_DEBUG) {
      Napier.base(CustomDebugAntilog(minLogLevel = LogLevel.DEBUG))
    }
    v("napier init, debug: ${BuildKonfig.IS_DEBUG}")
  }

  fun v(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.v(message, throwable, tag)
  fun d(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.d(message, throwable, tag)
  fun i(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.i(message, throwable, tag)
  fun w(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.w(message, throwable, tag)
  fun e(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.e(message, throwable, tag)
  fun e(throwable: Throwable? = null, tag: String? = null) = Napier.e("", throwable, tag)
  fun wtf(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.wtf(message, throwable, tag)
}

class CustomKoinLogger : KoinLogger(if (BuildKonfig.IS_DEBUG) Level.INFO else Level.NONE) {
  override fun display(level: Level, msg: MESSAGE) {
    when (level) {
      Level.DEBUG -> Logger.d(msg)
      Level.INFO -> Logger.i(msg)
      Level.WARNING -> Logger.w(msg)
      Level.ERROR -> Logger.e(msg)
      Level.NONE -> Unit
    }
  }
}

val openAiLoggingConfig = LoggingConfig(
  logLevel = if (BuildKonfig.IS_DEBUG) OpenAiLogLevel.Headers else OpenAiLogLevel.None,
  logger = OpenAiLogger.Simple,
  sanitize = !BuildKonfig.IS_DEBUG,
)
