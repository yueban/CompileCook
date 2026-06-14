package com.yueban.compilecook.data.net.di

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import com.yueban.compilecook.APIKonfig
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSourceImpl
import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.data.net.service.DishRemoteDataSourceImpl
import com.yueban.compilecook.json.json
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.logger.openAiLoggingConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import io.ktor.client.plugins.logging.Logger as KtorLogger

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
    NetClient(
      get(),
      resolveBaseUrl(
        baseUrl = "${APIKonfig.API_DOMAIN}${APIKonfig.API_PATH}",
        path = APIKonfig.API_PATH
      )
    )
  }
  singleOf(::DishRemoteDataSourceImpl) bind DishRemoteDataSource::class
  single {
    OpenAI(
      host = OpenAIHost(
        baseUrl = resolveBaseUrl(
          baseUrl = "${APIKonfig.OPEN_AI_API_DOMAIN}${APIKonfig.OPEN_AI_API_PATH}",
          path = APIKonfig.OPEN_AI_API_PATH
        )
      ),
      token = APIKonfig.OPEN_AI_API_TOKEN,
      logging = openAiLoggingConfig,
    )
  }
  single { ModelId(APIKonfig.OPEN_AI_MODEL) }
  singleOf(::AiChatRemoteDataSourceImpl) bind AiChatRemoteDataSource::class
}

expect fun resolveBaseUrl(baseUrl: String, path: String): String
