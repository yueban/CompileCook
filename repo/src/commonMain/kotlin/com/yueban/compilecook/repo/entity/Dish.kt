package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.db.entity.DishLocalEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Dish(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: DishCategory,
  val difficulty: Long,
  val image: String,
  val ingredient: String,
  val calculation: String,
  val operation: String,
  val addition: String,
)

@Serializable
enum class DishCategory {
  @SerialName("vegetable_dish")
  VEGETABLE_DISH,

  @SerialName("meat_dish")
  MEAT_DISH,

  @SerialName("aquatic")
  AQUATIC,

  @SerialName("breakfast")
  BREAKFAST,

  @SerialName("staple")
  STAPLE,

  @SerialName("semi_finished")
  SEMI_FINISHED,

  @SerialName("soup")
  SOUP,

  @SerialName("drink")
  DRINK,

  @SerialName("condiment")
  CONDIMENT,

  @SerialName("dessert")
  DESSERT,

  @SerialName("unknown")
  UNKNOWN;

  companion object {
    fun fromValue(value: String): DishCategory =
      entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
  }
}

fun DishLocalEntity.toDish(): Dish =
  Dish(
    name = this.name,
    pinyin = this.pinyin,
    description = this.description,
    category = DishCategory.fromValue(this.category),
    difficulty = this.difficulty,
    image = this.image,
    ingredient = this.ingredient,
    calculation = this.calculation,
    operation = this.operation,
    addition = this.addition
  )
