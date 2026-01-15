package com.yueban.compilecook.ui.util

import com.yueban.compilecook.logger.Logger
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_network_error
import compilecook.composeapp.generated.resources.common_unknown_error
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import org.jetbrains.compose.resources.StringResource

val Throwable.stringRes: StringResource
  get() {
    Logger.e(this)
    return when (this) {
      is SocketTimeoutException,
      is ConnectTimeoutException,
      -> Res.string.common_network_error

      else -> Res.string.common_unknown_error
    }
  }
