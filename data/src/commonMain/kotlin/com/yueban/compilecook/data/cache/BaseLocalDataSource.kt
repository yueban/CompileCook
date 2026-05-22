package com.yueban.compilecook.data.cache

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class BaseLocalDataSource(
  protected val defaultDispatcher: CoroutineDispatcher,
) {
  @PublishedApi
  internal val transactionLock = DbTransactionLock()

  /**
   * For single inserts/deletes. Pushes work to background, NO Mutex required.
   */
  protected suspend inline fun <T> write(crossinline block: suspend () -> T): T =
    withContext(defaultDispatcher) { block() }

  /**
   * For bulk operations. Pushes to background AND applies platform-specific transaction lock.
   */
  protected suspend inline fun <T> transactionWrite(crossinline block: suspend () -> T): T =
    withContext(defaultDispatcher) { transactionLock.withLock { block() } }
}
