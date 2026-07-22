package com.yueban.compilecook.data.net.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineDispatcher

actual fun resolveBaseUrl(baseUrl: String, path: String): String = baseUrl

/**
 * Ktor 3.4.1+ defaults the Darwin engine dispatcher to [Dispatchers.IO] for
 * [io.ktor.client.statement.HttpStatement.execute] blocks, so the OpenAI streaming
 * flow emits from that dispatcher. The caller passes the DI-managed [DispatcherType.IO]
 * singleton, which we use as the engine dispatcher so emission and
 * [kotlinx.coroutines.flow.flowOn] collection share the identical instance.
 */
actual fun provideOpenAiEngine(ioDispatcher: CoroutineDispatcher): HttpClientEngine? = Darwin.create {
  dispatcher = ioDispatcher
}
