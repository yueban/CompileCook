package com.yueban.compilecook.data.cache.db.entity

data class DishSummaryLocalEntity(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: String,
  val difficulty: Long,
  val image: String,
  val isFavorite: Boolean,
)

data class DishDetailLocalEntity(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: String,
  val difficulty: Long,
  val image: String,
  val content: String,
  val isFavorite: Boolean,
)
