package com.yueban.compilecook.data.net.di

import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineDispatcher
import web.window.window

/**
 * Smart URL Resolver:
 * - If running on Localhost (Wasm Debug), returns just "/api/compilecook"
 *   (The Webpack proxy will pick this up and forward it).
 * - If running in Production, returns "https://static.yueban.site/api/compilecook"
 */
actual fun resolveBaseUrl(baseUrl: String, path: String): String {
  return try {
    val hostname = window.location.hostname
    if (hostname == "localhost" || hostname == "127.0.0.1") {
      path
    } else {
      baseUrl
    }
  } catch (_: Throwable) {
    baseUrl
  }
}

actual fun provideOpenAiEngine(ioDispatcher: CoroutineDispatcher): HttpClientEngine? = null
