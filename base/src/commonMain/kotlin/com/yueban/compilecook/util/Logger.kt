package com.yueban.compilecook.util

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object Logger {
  fun init(debug: Boolean) {
    if (debug) {
      Napier.base(DebugAntilog())
    }
    v("napier init, debug: $debug")
  }

  fun v(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.v(message, throwable, tag)
  fun d(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.d(message, throwable, tag)
  fun i(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.i(message, throwable, tag)
  fun w(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.w(message, throwable, tag)
  fun e(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.e(message, throwable, tag)
  fun e(throwable: Throwable? = null, tag: String? = null) = Napier.e("", throwable, tag)
  fun wtf(message: String, throwable: Throwable? = null, tag: String? = null) = Napier.wtf(message, throwable, tag)
}
