package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.db.QueryResult.AsyncValue
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yueban.compilecook.util.FileUtils
import java.io.File

actual fun provideDbDriver(schema: SqlSchema<AsyncValue<Unit>>, dbFileName: String): SqlDriver =
  JdbcSqliteDriver("jdbc:sqlite:${getDbFile(dbFileName).absolutePath}").also {
    schema.create(it)
    it.execute(null, "PRAGMA foreign_keys = ON", 0)
  }

private fun getDbFile(dbFileName: String) = File(FileUtils.getUserDataDir(), dbFileName)
