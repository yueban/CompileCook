package com.yueban.compilecook.data.db

import androidx.test.core.app.ApplicationProvider
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun provideInMemoryDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver =
  AndroidSqliteDriver(
    schema = schema.synchronous(),
    context = ApplicationProvider.getApplicationContext(),
    name = null
  )
