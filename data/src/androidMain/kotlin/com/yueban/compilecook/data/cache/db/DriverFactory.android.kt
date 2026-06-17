package com.yueban.compilecook.data.cache.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
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
    name = dbFileName,
    callback = object : AndroidSqliteDriver.Callback(schema.synchronous()) {
      override fun onConfigure(db: SupportSQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
      }
    },
  )
