package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.DishLocalEntity

data class Dish(
  val name: String,
  val description: String,
  val category: DishCategory,
  val difficulty: Long,
  val image: String,
  val ingredient: String,
  val calculation: String,
  val operation: String,
  val addition: String,
)

enum class DishCategory {
  AQUATIC,
  BREAKFAST,
  CONDIMENT,
  DESSERT,
  DRINK,
  MEAT_DISH,
  SEMI_FINISHED,
  SOUP,
  STAPLE,
  VEGETABLE_DISH,
  UNKNOWN;

  companion object {
    fun fromValue(value: String): DishCategory =
      when (value) {
        "aquatic" -> AQUATIC
        "breakfast" -> BREAKFAST
        "condiment" -> CONDIMENT
        "dessert" -> DESSERT
        "drink" -> DRINK
        "meat_dish" -> MEAT_DISH
        "semi_finished" -> SEMI_FINISHED
        "soup" -> SOUP
        "staple" -> STAPLE
        "vegetable_dish" -> VEGETABLE_DISH
        else -> UNKNOWN
      }
  }
}

fun DishLocalEntity.toDish(): Dish =
  Dish(
    name = this.name,
    description = this.description,
    category = DishCategory.fromValue(this.category),
    difficulty = this.difficulty,
    image = this.image,
    ingredient = this.ingredient,
    calculation = this.calculation,
    operation = this.operation,
    addition = this.addition
  )
