package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver

actual fun provideInMemoryDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
  createDefaultWebWorkerDriver()
