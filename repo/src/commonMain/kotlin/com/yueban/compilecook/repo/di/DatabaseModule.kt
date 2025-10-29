package com.yueban.compilecook.repo.di

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSourceImpl
import com.yueban.compilecook.data.cache.db.APP_DATABASE_FILE_NAME
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.createDatabase
import com.yueban.compilecook.data.cache.db.provideDbDriver
import org.koin.core.Koin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val initialDatabaseModule = module {
  single { provideDbDriver(AppDatabase.Companion.Schema, APP_DATABASE_FILE_NAME) }
  single { createDatabase(get()) }
}

suspend fun loadDatabaseModule(koin: Koin) {
  AppDatabase.Companion.Schema.awaitCreate(koin.get())
  val databaseModule = databaseModule(koin.get())
  koin.loadModules(listOf(databaseModule))
}

private fun databaseModule(database: AppDatabase) = module {
  single { database }
  single { database.dishQueries }
  singleOf(::DishLocalDataSourceImpl) bind DishLocalDataSource::class
}
