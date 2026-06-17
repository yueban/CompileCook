package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver

actual fun provideDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>, dbFileName: String): SqlDriver =
  // TODO: manually specify sql.js worker
  createDefaultWebWorkerDriver().also {
    it.execute(null, "PRAGMA foreign_keys = ON", 0)
  }
