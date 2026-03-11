package com.yueban.compilecook.data.cache.db.entity

data class TipSummaryLocalEntity(
  val name: String,
  val pinyin: String,
  val type: String,
  val isFavorite: Boolean,
)

data class TipDetailLocalEntity(
  val name: String,
  val pinyin: String,
  val type: String,
  val content: String,
  val isFavorite: Boolean,
)
