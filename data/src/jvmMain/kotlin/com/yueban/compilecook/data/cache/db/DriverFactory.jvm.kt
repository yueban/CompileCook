package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.db.QueryResult.AsyncValue
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.yueban.compilecook.BuildKonfig
import com.yueban.compilecook.logger.Logger
import net.harawata.appdirs.AppDirsFactory
import java.io.File

actual fun provideDbDriver(schema: SqlSchema<AsyncValue<Unit>>, dbFileName: String): SqlDriver =
  JdbcSqliteDriver("jdbc:sqlite:${getDbFile(dbFileName).absolutePath}").also { schema.create(it) }

private fun getDbFile(dbFileName: String) =
  AppDirsFactory.getInstance()
    .getUserDataDir(BuildKonfig.APP_NAME, BuildKonfig.APP_VERSION, null)
    .let { File(it) }
    .also {
      if (!it.exists()) it.mkdirs()
      Logger.d("userDataDir: $it")
    }
    .let {
      File(it, dbFileName)
    }
