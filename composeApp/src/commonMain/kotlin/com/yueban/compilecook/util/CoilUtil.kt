package com.yueban.compilecook.util

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.NullRequestDataException
import coil3.request.crossfade
import coil3.util.Logger

object CoilUtil : Logger {
  fun init() {
    SingletonImageLoader.setSafe { context ->
      ImageLoader.Builder(context)
        .crossfade(true)
        .logger(this)
        .build()
    }
  }

  override var minLevel: Logger.Level = Logger.Level.Debug

  override fun log(
    tag: String,
    level: Logger.Level,
    message: String?,
    throwable: Throwable?,
  ) {
    message ?: return

    if (throwable is NullRequestDataException) return

    when (level) {
      Logger.Level.Verbose -> com.yueban.compilecook.logger.Logger.v(message, throwable, tag)
      Logger.Level.Debug -> com.yueban.compilecook.logger.Logger.d(message, throwable, tag)
      Logger.Level.Info -> com.yueban.compilecook.logger.Logger.i(message, throwable, tag)
      Logger.Level.Warn -> com.yueban.compilecook.logger.Logger.w(message, throwable, tag)
      Logger.Level.Error -> com.yueban.compilecook.logger.Logger.e(message, throwable, tag)
    }
  }
}
