package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.TipLocalEntity

data class Tip(
  val name: String,
  val type: TipType,
  val content: String,
)

enum class TipType {
  BASIC,
  LEARN,
  ADVANCED,
  UNKNOWN;

  companion object {
    fun fromValue(value: String): TipType =
      when (value) {
        "basic" -> BASIC
        "learn" -> LEARN
        "advanced" -> ADVANCED
        else -> UNKNOWN
      }
  }
}

fun TipLocalEntity.toTip(): Tip =
  Tip(
    name = this.name,
    type = TipType.fromValue(this.type),
    content = this.content,
  )
