package com.yueban.compilecook.data.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

const val APP_DATABASE_FILE_NAME = "AppDatabase.db"

expect fun provideDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>, dbFileName: String): SqlDriver

fun createDatabase(driver: SqlDriver): AppDatabase = AppDatabase(driver)
