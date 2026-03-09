package com.yueban.compilecook.data.net.di

import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.data.net.service.DishRemoteDataSourceImpl
import com.yueban.compilecook.json.json
import com.yueban.compilecook.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal const val PROD_DOMAIN = "https://static.yueban.site"
internal const val API_PATH = "/api/compilecook"
internal const val BASE_URL = "$PROD_DOMAIN$API_PATH"

typealias KtorLogger = io.ktor.client.plugins.logging.Logger

val remoteDataSourceModule = module {
  single {
    HttpClient {
      install(Logging) {
        logger = object : KtorLogger {
          override fun log(message: String) {
            Logger.i(message = message, tag = "HttpClient")
          }
        }
        level = LogLevel.INFO
      }
      install(ContentNegotiation) { json(json) }
    }
  }
  single {
    NetClient(get(), resolveBaseUrl())
  }
  singleOf(::DishRemoteDataSourceImpl) bind DishRemoteDataSource::class
}

expect fun resolveBaseUrl(): String
