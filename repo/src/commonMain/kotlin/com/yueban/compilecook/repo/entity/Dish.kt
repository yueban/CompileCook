package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.DishLocalEntity

data class Dish(
  val id: Long,
  val name: String,
  val description: String,
  val category: Long,
  val difficulty: Long,
  val image: String,
  val ingredient: String,
  val calculation: String,
  val operation: String,
  val addition: String?,
)

fun DishLocalEntity.toDish(): Dish {
  return Dish(
    id = this.id,
    name = this.name,
    description = this.description,
    category = this.category,
    difficulty = this.difficulty,
    image = this.image,
    ingredient = this.ingredient,
    calculation = this.calculation,
    operation = this.operation,
    addition = this.addition
  )
}

fun Dish.toLocalEntity(): DishLocalEntity {
  return DishLocalEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    category = this.category,
    difficulty = this.difficulty,
    image = this.image,
    ingredient = this.ingredient,
    calculation = this.calculation,
    operation = this.operation,
    addition = this.addition
  )
}
