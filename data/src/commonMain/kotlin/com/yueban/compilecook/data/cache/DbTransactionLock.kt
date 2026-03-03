package com.yueban.compilecook.data.cache

/**
 * A platform-specific lock to prevent SQLite "Transaction within a transaction"
 * crashes on single-threaded JS/Wasm environments.
 */
expect class DbTransactionLock() {
  suspend fun <T> withLock(block: suspend () -> T): T
}
