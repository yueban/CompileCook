package com.yueban.compilecook.data.net.entity

import com.yueban.compilecook.data.db.entity.DishLocalEntity
import kotlinx.serialization.Serializable

@Serializable
data class DishRemoteEntity(
  val name: String,
  val description: String,
  val category: String,
  val difficulty: Long,
  val image: String,
  val ingredient: String,
  val calculation: String,
  val operation: String,
  val addition: String,
)

private val categoryToIdMap = mapOf(
  "aquatic" to 1L,
  "breakfast" to 2L,
  "condiment" to 3L,
  "dessert" to 4L,
  "drink" to 5L,
  "meat_dish" to 6L,
  "semi_finished" to 7L,
  "soup" to 8L,
  "staple" to 9L,
  "vegetable_dish" to 10L
)

fun DishRemoteEntity.toLocalEntity(): DishLocalEntity {
  return DishLocalEntity(
    id = 0,
    name = this.name,
    description = this.description,
    category = categoryToIdMap[this.category] ?: 0,
    difficulty = this.difficulty,
    image = this.image,
    ingredient = this.ingredient,
    calculation = this.calculation,
    operation = this.operation,
    addition = this.addition
  )
}
