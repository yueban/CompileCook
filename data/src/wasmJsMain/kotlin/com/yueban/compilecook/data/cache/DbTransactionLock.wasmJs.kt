package com.yueban.compilecook.data.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

actual class DbTransactionLock actual constructor() {
  private val mutex = Mutex()

  actual suspend fun <T> withLock(block: suspend () -> T): T = mutex.withLock { block() }
}
