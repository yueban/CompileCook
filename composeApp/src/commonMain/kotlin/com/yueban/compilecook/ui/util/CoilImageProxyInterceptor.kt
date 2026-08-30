package com.yueban.compilecook.ui.util

import coil3.intercept.Interceptor
import coil3.request.ImageResult

private const val GITHUB_PROXY_BASE = "https://gh-proxy.com/"
private const val GITHUB_RAW_BASE = "https://raw.githubusercontent.com/"

class CoilImageProxyInterceptor : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val request = chain.request
    val url = request.data.toString()

    val newRequest =
      if (url.contains(GITHUB_RAW_BASE)) {
        val newUrl = "$GITHUB_PROXY_BASE$url"
        request.newBuilder().data(newUrl).build()
      } else {
        request
      }

    return chain.withRequest(newRequest).proceed()
  }
}
