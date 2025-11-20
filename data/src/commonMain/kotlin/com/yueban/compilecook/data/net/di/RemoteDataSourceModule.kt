package com.yueban.compilecook.data.net.di

import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.data.net.service.DishRemoteDataSourceImpl
import com.yueban.compilecook.json.json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val remoteDataSourceModule = module {
  single {
    HttpClient {
      install(ContentNegotiation) { json(json) }
    }
  }
  single { NetClient(get(), BASE_URL) }
  singleOf(::DishRemoteDataSourceImpl) bind DishRemoteDataSource::class
}

private const val BASE_URL = "https://static.yueban.site/api/compilecook"
