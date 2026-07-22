package com.yueban.compilecook.data.net.di

import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.yueban.compilecook.APIKonfig
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSource
import com.yueban.compilecook.data.net.service.AiChatRemoteDataSourceImpl
import com.yueban.compilecook.data.net.service.DishRemoteDataSource
import com.yueban.compilecook.data.net.service.DishRemoteDataSourceImpl
import com.yueban.compilecook.di.DispatcherType
import com.yueban.compilecook.json.json
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.logger.openAiLoggingConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
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
      config = OpenAIConfig(
        token = APIKonfig.OPEN_AI_API_TOKEN,
        host = OpenAIHost(
          baseUrl = resolveBaseUrl(
            baseUrl = "${APIKonfig.OPEN_AI_API_DOMAIN}${APIKonfig.OPEN_AI_API_PATH}",
            path = APIKonfig.OPEN_AI_API_PATH
          )
        ),
        logging = openAiLoggingConfig,
        engine = provideOpenAiEngine(ioDispatcher = get(named(DispatcherType.IO))),
      )
    )
  }
  single { ModelId(APIKonfig.OPEN_AI_MODEL) }
  single {
    AiChatRemoteDataSourceImpl(
      openAi = get(),
      modelId = get(),
      defaultDispatcher = get(named(DispatcherType.Default)),
      ioDispatcher = get(named(DispatcherType.IO)),
    )
  } bind AiChatRemoteDataSource::class
}

expect fun resolveBaseUrl(baseUrl: String, path: String): String

/**
 * Provides a platform-specific Ktor engine for the OpenAI HTTP client.
 *
 * On iOS, Ktor 3.4.1+ defaults the Darwin engine dispatcher to [Dispatchers.IO] for
 * [io.ktor.client.statement.HttpStatement.execute] blocks. The OpenAI streaming flow
 * emits from inside this block, so its emissions carry the engine dispatcher context.
 * When the AI chat pipeline collects this flow via [kotlinx.coroutines.flow.flowOn]
 * with the DI-managed IO dispatcher, the Flow invariant on Darwin compares dispatcher
 * object identity. Without overriding the engine dispatcher here, the engine creates
 * its own internal instance that differs from the DI singleton, causing a violation.
 *
 * The iOS actual sets the engine dispatcher to the same DI singleton passed here.
 * On JVM/Android/Wasm returns `null` (no override needed).
 *
 * @param ioDispatcher The IO dispatcher from DI. The iOS actual uses this as the
 *   engine dispatcher so emission and [kotlinx.coroutines.flow.flowOn] collection
 *   share the identical instance, satisfying the Flow invariant on Darwin.
 */
expect fun provideOpenAiEngine(ioDispatcher: CoroutineDispatcher): HttpClientEngine?
