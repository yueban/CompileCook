package com.yueban.compilecook.ui.util

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.NullRequestDataException
import coil3.request.crossfade
import coil3.util.Logger.Level.Debug
import coil3.util.Logger.Level.Error
import coil3.util.Logger.Level.Info
import coil3.util.Logger.Level.Verbose
import coil3.util.Logger.Level.Warn
import com.yueban.compilecook.logger.Logger

typealias CoilLogger = coil3.util.Logger
typealias CoilLoggerLevel = coil3.util.Logger.Level

object CoilUtil : CoilLogger {
  fun init() {
    SingletonImageLoader.setSafe { context ->
      ImageLoader.Builder(context)
        .crossfade(true)
        .logger(this)
        .build()
    }
  }

  override var minLevel: CoilLoggerLevel = Debug

  override fun log(
    tag: String,
    level: CoilLoggerLevel,
    message: String?,
    throwable: Throwable?,
  ) {
    message ?: return

    if (throwable is NullRequestDataException) return

    when (level) {
      Verbose -> Logger.v(message, throwable, tag)
      Debug -> Logger.d(message, throwable, tag)
      Info -> Logger.i(message, throwable, tag)
      Warn -> Logger.w(message, throwable, tag)
      Error -> Logger.e(message, throwable, tag)
    }
  }
}
