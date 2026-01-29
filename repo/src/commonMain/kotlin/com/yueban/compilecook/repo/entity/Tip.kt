package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.TipLocalEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tip(
  val name: String,
  val pinyin: String,
  val type: TipType,
  val content: String,
)

@Serializable
enum class TipType {
  @SerialName("basic")
  BASIC,

  @SerialName("learn")
  LEARN,

  @SerialName("advanced")
  ADVANCED,

  @SerialName("unknown")
  UNKNOWN;

  companion object {
    fun fromValue(value: String): TipType =
      TipType.entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
  }
}

fun TipLocalEntity.toTip(): Tip =
  Tip(
    name = this.name,
    pinyin = this.pinyin,
    type = TipType.fromValue(this.type),
    content = this.content,
  )
