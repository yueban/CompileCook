package com.yueban.compilecook.data.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.context.GlobalContext

actual fun provideDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>, dbFileName: String): SqlDriver =
  AndroidSqliteDriver(
    schema = schema.synchronous(),
    context = GlobalContext.get().get<Context>(),
    name = dbFileName
  )
