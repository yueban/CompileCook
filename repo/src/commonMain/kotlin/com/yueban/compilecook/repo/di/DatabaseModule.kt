package com.yueban.compilecook.repo.di

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSourceImpl
import com.yueban.compilecook.data.cache.db.APP_DATABASE_FILE_NAME
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.createDatabase
import com.yueban.compilecook.data.cache.db.provideDbDriver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val initialDatabaseModule = module {
  single { provideDbDriver(AppDatabase.Schema, APP_DATABASE_FILE_NAME) }
  single { createDatabase(get()) }
}

internal fun databaseModule(database: AppDatabase) = module {
  single { database }
  single { database.dishQueries }
  singleOf(::DishLocalDataSourceImpl) bind DishLocalDataSource::class
}
