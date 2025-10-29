package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.inMemoryDriver

actual fun provideInMemoryDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
  inMemoryDriver(schema = schema.synchronous())
