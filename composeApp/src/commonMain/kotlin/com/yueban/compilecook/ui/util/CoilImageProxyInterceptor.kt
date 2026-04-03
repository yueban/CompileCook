package com.yueban.compilecook.ui.util

import coil3.intercept.Interceptor
import coil3.request.ImageResult

private const val PROXY_BASE = "https://gh-usercontent-proxy.yueban.site"
private const val GITHUB_MEDIA_BASE = "https://media.githubusercontent.com"

class CoilImageProxyInterceptor : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val request = chain.request
    val url = request.data.toString()

    val newRequest =
      if (url.contains(GITHUB_MEDIA_BASE)) {
        val newUrl = url.replace(GITHUB_MEDIA_BASE, "$PROXY_BASE/media")
        request.newBuilder().data(newUrl).build()
      } else {
        request
      }

    return chain.withRequest(newRequest).proceed()
  }
}
