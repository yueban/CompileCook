package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.cache.db.entity.DishDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.DishSummaryLocalEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class DishMappingTest {
  @Test
  fun toDishSummary_mapsCorrectly() {
    val local = DishSummaryLocalEntity(
      name = "Mapo Tofu",
      pinyin = "mapo tofu",
      description = "Spicy tofu dish",
      category = "meat_dish",
      difficulty = 3L,
      image = "mapo_tofu.jpg",
      isFavorite = true,
    )

    val result = local.toDishSummary()

    assertEquals("Mapo Tofu", result.name)
    assertEquals("mapo tofu", result.pinyin)
    assertEquals("Spicy tofu dish", result.description)
    assertEquals(DishCategory.MEAT_DISH, result.category)
    assertEquals(3L, result.difficulty)
    assertEquals("mapo_tofu.jpg", result.image)
    assertEquals(true, result.isFavorite)
  }

  @Test
  fun toDishSummary_unknownCategory() {
    val local = DishSummaryLocalEntity(
      name = "Test",
      pinyin = "test",
      description = "Test",
      category = "nonexistent_category",
      difficulty = 1L,
      image = "test.jpg",
      isFavorite = false,
    )

    val result = local.toDishSummary()

    assertEquals(DishCategory.UNKNOWN, result.category)
  }

  @Test
  fun toDishSummary_allCategories() {
    val categoryMappings = listOf(
      "vegetable_dish" to DishCategory.VEGETABLE_DISH,
      "meat_dish" to DishCategory.MEAT_DISH,
      "aquatic" to DishCategory.AQUATIC,
      "breakfast" to DishCategory.BREAKFAST,
      "staple" to DishCategory.STAPLE,
      "semi_finished" to DishCategory.SEMI_FINISHED,
      "soup" to DishCategory.SOUP,
      "drink" to DishCategory.DRINK,
      "condiment" to DishCategory.CONDIMENT,
      "dessert" to DishCategory.DESSERT,
    )

    for ((input, expected) in categoryMappings) {
      val local = DishSummaryLocalEntity("Test", "test", "Test", input, 1L, "test.jpg", false)
      val result = local.toDishSummary()
      assertEquals(expected, result.category, "Expected $expected for input '$input'")
    }
  }

  @Test
  fun toDishDetail_mapsCorrectly() {
    val local = DishDetailLocalEntity(
      name = "Mapo Tofu",
      pinyin = "mapo tofu",
      description = "Spicy tofu dish",
      category = "meat_dish",
      difficulty = 3L,
      image = "mapo_tofu.jpg",
      content = "Cook tofu. Add sauce. Serve.",
      isFavorite = false,
    )

    val result = local.toDishDetail()

    assertEquals("Mapo Tofu", result.name)
    assertEquals("mapo tofu", result.pinyin)
    assertEquals("Spicy tofu dish", result.description)
    assertEquals(DishCategory.MEAT_DISH, result.category)
    assertEquals(3L, result.difficulty)
    assertEquals("mapo_tofu.jpg", result.image)
    assertEquals("Cook tofu. Add sauce. Serve.", result.content)
    assertEquals(false, result.isFavorite)
  }

  @Test
  fun toDishDetail_unknownCategory() {
    val local = DishDetailLocalEntity(
      name = "Test",
      pinyin = "test",
      description = "Test",
      category = "invalid",
      difficulty = 1L,
      image = "img",
      content = "Content",
      isFavorite = false,
    )

    assertEquals(DishCategory.UNKNOWN, local.toDishDetail().category)
  }

  @Test
  fun DishCategory_fromValue_exact() {
    assertEquals(DishCategory.MEAT_DISH, DishCategory.fromValue("meat_dish"))
    assertEquals(DishCategory.STAPLE, DishCategory.fromValue("staple"))
    assertEquals(DishCategory.VEGETABLE_DISH, DishCategory.fromValue("vegetable_dish"))
    assertEquals(DishCategory.AQUATIC, DishCategory.fromValue("aquatic"))
    assertEquals(DishCategory.BREAKFAST, DishCategory.fromValue("breakfast"))
    assertEquals(DishCategory.SEMI_FINISHED, DishCategory.fromValue("semi_finished"))
    assertEquals(DishCategory.SOUP, DishCategory.fromValue("soup"))
    assertEquals(DishCategory.DRINK, DishCategory.fromValue("drink"))
    assertEquals(DishCategory.CONDIMENT, DishCategory.fromValue("condiment"))
    assertEquals(DishCategory.DESSERT, DishCategory.fromValue("dessert"))
  }

  @Test
  fun DishCategory_fromValue_ignoreCase() {
    assertEquals(DishCategory.MEAT_DISH, DishCategory.fromValue("MEAT_DISH"))
    assertEquals(DishCategory.MEAT_DISH, DishCategory.fromValue("Meat_Dish"))
    assertEquals(DishCategory.STAPLE, DishCategory.fromValue("STAPLE"))
    assertEquals(DishCategory.AQUATIC, DishCategory.fromValue("AQUATIC"))
  }

  @Test
  fun DishCategory_fromValue_unknown() {
    assertEquals(DishCategory.UNKNOWN, DishCategory.fromValue("nonexistent"))
    assertEquals(DishCategory.UNKNOWN, DishCategory.fromValue(""))
    assertEquals(DishCategory.UNKNOWN, DishCategory.fromValue("123"))
  }
}
