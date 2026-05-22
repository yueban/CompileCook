package com.yueban.compilecook.repo.di

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.net.di.remoteDataSourceModule
import com.yueban.compilecook.repo.AiChatRepo
import com.yueban.compilecook.repo.AiChatRepoImpl
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.repo.DishRepoImpl
import org.koin.core.Koin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repoModule = module {
  singleOf(::DishRepoImpl) bind DishRepo::class
  singleOf(::AiChatRepoImpl) bind AiChatRepo::class
}

suspend fun loadDataModules(koin: Koin) {
  AppDatabase.Schema.awaitCreate(koin.get())

  koin.loadModules(listOf(repoModule, remoteDataSourceModule))
}
