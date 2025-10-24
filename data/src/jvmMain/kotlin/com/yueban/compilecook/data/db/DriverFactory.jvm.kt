package com.yueban.compilecook.data.db

import app.cash.sqldelight.db.QueryResult.AsyncValue
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/**
 * TODO: populate dbFileName based on operating system.
 */
actual fun provideDbDriver(schema: SqlSchema<AsyncValue<Unit>>, dbFileName: String): SqlDriver =
  JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { schema.create(it) }
