package com.yueban.compilecook.repo.di

import com.yueban.compilecook.data.cache.AiChatLocalDataSource
import com.yueban.compilecook.data.cache.AiChatLocalDataSourceImpl
import com.yueban.compilecook.data.cache.DishLocalDataSource
import com.yueban.compilecook.data.cache.DishLocalDataSourceImpl
import com.yueban.compilecook.data.cache.db.APP_DATABASE_FILE_NAME
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.createDatabase
import com.yueban.compilecook.data.cache.db.provideDbDriver
import com.yueban.compilecook.di.DispatcherType
import org.koin.core.qualifier.named
import org.koin.dsl.module

val databaseModule = module {
  single { provideDbDriver(AppDatabase.Schema, APP_DATABASE_FILE_NAME) }
  single { createDatabase(get()) }

  // Lazily resolve the database when these are injected
  single { get<AppDatabase>().dishQueries }
  single { get<AppDatabase>().tipQueries }
  single { get<AppDatabase>().aiChatQueries }
  single<DishLocalDataSource> {
    DishLocalDataSourceImpl(
      dishQueries = get(),
      tipQueries = get(),
      defaultDispatcher = get(named(DispatcherType.Default)),
    )
  }
  single<AiChatLocalDataSource> {
    AiChatLocalDataSourceImpl(
      aiChatQueries = get(),
      defaultDispatcher = get(named(DispatcherType.Default)),
    )
  }
}
