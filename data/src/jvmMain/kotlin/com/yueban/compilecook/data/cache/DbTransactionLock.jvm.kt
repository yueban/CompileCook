package com.yueban.compilecook.data.cache

actual class DbTransactionLock actual constructor() {
  actual suspend fun <T> withLock(block: suspend () -> T): T = block()
}
