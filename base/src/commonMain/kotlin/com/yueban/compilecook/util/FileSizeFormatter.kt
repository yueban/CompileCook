package com.yueban.compilecook.util

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

object FileSizeFormatter {
  private val units = arrayOf("B", "KB", "MB", "GB", "TB")

  @Suppress("MagicNumber")
  fun format(bytes: Long): String {
    if (bytes <= 0) return "0B"
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)
    val value = bytes / 1024.0.pow(digitGroups.toDouble())
    val roundedValue = round(value * 10) / 10
    return if (roundedValue % 1.0 == 0.0) {
      "${roundedValue.toInt()}${units[digitGroups]}"
    } else {
      "$roundedValue${units[digitGroups]}"
    }
  }
}
