package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.cache.db.entity.DishDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.DishSummaryLocalEntity
import com.yueban.compilecook.util.serialName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DISH_DIFFICULTY_MAX_LEVEL = 5

@Serializable
data class DishSummary(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: DishCategory,
  val difficulty: Long,
  val image: String,
  val isFavorite: Boolean,
)

@Serializable
data class DishDetail(
  val name: String,
  val pinyin: String,
  val description: String,
  val category: DishCategory,
  val difficulty: Long,
  val image: String,
  val content: String,
  val isFavorite: Boolean,
)

@Serializable
enum class DishCategory {
  @SerialName("vegetable_dish") VEGETABLE_DISH,
  @SerialName("meat_dish") MEAT_DISH,
  @SerialName("aquatic") AQUATIC,
  @SerialName("breakfast") BREAKFAST,
  @SerialName("staple") STAPLE,
  @SerialName("semi_finished") SEMI_FINISHED,
  @SerialName("soup") SOUP,
  @SerialName("drink") DRINK,
  @SerialName("condiment") CONDIMENT,
  @SerialName("dessert") DESSERT,
  @SerialName("unknown") UNKNOWN;

  companion object {
    fun fromValue(value: String): DishCategory = entries.find { it.serialName() == value } ?: UNKNOWN
  }
}

fun DishSummaryLocalEntity.toDishSummary(): DishSummary =
  DishSummary(
    name = this.name,
    pinyin = this.pinyin,
    description = this.description,
    category = DishCategory.fromValue(this.category),
    difficulty = this.difficulty,
    image = this.image,
    isFavorite = this.isFavorite,
  )

fun DishDetailLocalEntity.toDishDetail(): DishDetail =
  DishDetail(
    name = this.name,
    pinyin = this.pinyin,
    description = this.description,
    category = DishCategory.fromValue(this.category),
    difficulty = this.difficulty,
    image = this.image,
    content = this.content,
    isFavorite = this.isFavorite,
  )
