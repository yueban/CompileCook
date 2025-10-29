package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import kotlinx.coroutines.test.runTest

expect fun provideInMemoryDbDriver(schema: SqlSchema<QueryResult.AsyncValue<Unit>>): SqlDriver

fun testingDb(block: suspend AppDatabase.() -> Unit) = runTest {
  val driver = provideInMemoryDbDriver(AppDatabase.Companion.Schema)
  AppDatabase.Schema.awaitCreate(driver)
  AppDatabase(driver).block()
  driver.close()
}
