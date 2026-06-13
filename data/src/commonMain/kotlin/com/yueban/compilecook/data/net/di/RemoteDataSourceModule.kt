package com.yueban.compilecook.data.net.di

import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSourceImpl
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
import io.ktor.client.plugins.logging.Logger as KtorLogger

private const val PROD_DOMAIN = "https://static.yueban.site"
private const val API_PATH = "/api/compilecook"
private const val BASE_URL = "$PROD_DOMAIN$API_PATH"

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
    NetClient(get(), resolveBaseUrl(BASE_URL, API_PATH))
  }
  singleOf(::DishRemoteDataSourceImpl) bind DishRemoteDataSource::class
  singleOf(::AiChatRemoteDataSourceImpl) bind AiChatRemoteDataSource::class
}

expect fun resolveBaseUrl(baseUrl: String, path: String): String
