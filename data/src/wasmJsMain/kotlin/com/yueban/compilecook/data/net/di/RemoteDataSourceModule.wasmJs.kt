package com.yueban.compilecook.data.net.di

import web.window.window

/**
 * Smart URL Resolver:
 * - If running on Localhost (Wasm Debug), returns just "/api/compilecook"
 *   (The Webpack proxy will pick this up and forward it).
 * - If running in Production, returns "https://static.yueban.site/api/compilecook"
 */
actual fun resolveBaseUrl(): String {
  return try {
    val hostname = window.location.hostname
    if (hostname == "localhost" || hostname == "127.0.0.1") {
      API_PATH
    } else {
      BASE_URL
    }
  } catch (_: Throwable) {
    BASE_URL
  }
}
