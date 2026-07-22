package com.yueban.compilecook.data.net.di

import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher

actual fun resolveBaseUrl(baseUrl: String, path: String): String = baseUrl

actual fun provideOpenAiEngine(ioDispatcher: CoroutineDispatcher): HttpClientEngine? = null
