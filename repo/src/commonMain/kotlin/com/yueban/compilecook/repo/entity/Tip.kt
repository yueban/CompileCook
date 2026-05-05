package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.cache.db.entity.TipDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.TipSummaryLocalEntity
import com.yueban.compilecook.util.serialName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TipSummary(
  val name: String,
  val pinyin: String,
  val type: TipType,
  val isFavorite: Boolean,
)

@Serializable
data class TipDetail(
  val name: String,
  val pinyin: String,
  val type: TipType,
  val content: String,
  val isFavorite: Boolean,
)

@Serializable
enum class TipType {
  @SerialName("basic") BASIC,
  @SerialName("learn") LEARN,
  @SerialName("advanced") ADVANCED,
  @SerialName("unknown") UNKNOWN;

  companion object {
    fun fromValue(value: String): TipType = TipType.entries.find { it.serialName() == value } ?: UNKNOWN
  }
}

fun TipSummaryLocalEntity.toTipSummary(): TipSummary =
  TipSummary(
    name = this.name,
    pinyin = this.pinyin,
    type = TipType.fromValue(this.type),
    isFavorite = this.isFavorite,
  )

fun TipDetailLocalEntity.toTipDetail(): TipDetail =
  TipDetail(
    name = this.name,
    pinyin = this.pinyin,
    type = TipType.fromValue(this.type),
    content = this.content,
    isFavorite = this.isFavorite
  )
