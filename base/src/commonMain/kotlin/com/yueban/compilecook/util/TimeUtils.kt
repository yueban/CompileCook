package com.yueban.compilecook.util

import kotlin.time.Clock

val currentTimeMillis: Long
  get() = Clock.System.now().toEpochMilliseconds()
