package com.yueban.compilecook.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun provideDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>, dbFileName: String): SqlDriver {
  return NativeSqliteDriver(schema.synchronous(), dbFileName)
}
