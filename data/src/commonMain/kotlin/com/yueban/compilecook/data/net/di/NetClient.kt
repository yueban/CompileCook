package com.yueban.compilecook.data.net.di

import com.yueban.compilecook.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class NetClient(
  private val httpClient: HttpClient,
  private val baseUrl: String,
) {
  @Suppress("TooGenericExceptionCaught")
  suspend fun get(path: String): HttpResponse {
    try {
      Logger.d("Requesting url: $baseUrl$path")
      return httpClient.get("$baseUrl$path")
    } catch (e: Exception) {
      Logger.e("Error requesting url: $baseUrl$path", e)
      throw e
    }
  }
}
