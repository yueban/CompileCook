package com.yueban.compilecook.repo.di

import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSourceImpl
import com.yueban.compilecook.data.cache.db.APP_DATABASE_FILE_NAME
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.createDatabase
import com.yueban.compilecook.data.cache.db.provideDbDriver
import com.yueban.compilecook.di.DispatcherType
import org.koin.core.qualifier.named
import org.koin.dsl.module

val initialDatabaseModule = module {
  single { provideDbDriver(AppDatabase.Schema, APP_DATABASE_FILE_NAME) }
  single { createDatabase(get()) }
}

internal fun databaseModule(database: AppDatabase) = module {
  single { database }
  single { database.dishQueries }
  single { database.tipQueries }
  single<DishLocalDataSource> {
    DishLocalDataSourceImpl(
      dishQueries = get(),
      tipQueries = get(),
      defaultDispatcher = get(named(DispatcherType.Default)),
    )
  }
}
