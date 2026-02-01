package com.yueban.compilecook.data.net.entity

import com.yueban.compilecook.data.db.entity.DishLocalEntity
import kotlinx.serialization.Serializable

@Serializable
data class DishRemoteEntity(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: String,
  val difficulty: Long,
  val image: String,
  val content: String,
)

fun DishRemoteEntity.toLocalEntity(): DishLocalEntity =
  DishLocalEntity(
    name = this.name,
    pinyin = this.pinyin,
    description = this.description,
    category = category,
    difficulty = this.difficulty,
    image = this.image,
    content = this.content,
  )
