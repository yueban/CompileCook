package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DishTest {
  @Test
  fun initialState_dishSummariesEmpty() = testingDb {
    val dishes = dishQueries.getDishSummaries(null, null, 0L).awaitAsList()
    assertTrue(dishes.isEmpty(), "Initially, the dish table should be empty.")
  }

  @Test
  fun upsertAndSelectDish() = testingDb {
    dishQueries.upsertDish(
      name = "Spaghetti Carbonara",
      pinyin = "spaghetti carbonara",
      description = "A classic Italian pasta dish",
      category = "staple",
      difficulty = 2L,
      image = "carbonara.jpg",
      content = "Cook pasta. Mix eggs. Serve."
    )

    val dishes = dishQueries.getDishSummaries(null, null, 0L).awaitAsList()
    assertEquals(1, dishes.size)
    assertEquals("Spaghetti Carbonara", dishes.first().name)

    val detail = dishQueries.getDishDetail("Spaghetti Carbonara").awaitAsOneOrNull()
    assertNotNull(detail)
    assertEquals("Spaghetti Carbonara", detail.name)
    assertEquals("staple", detail.category)
    assertEquals(2L, detail.difficulty)
    assertEquals("Cook pasta. Mix eggs. Serve.", detail.content)
    assertEquals(false, detail.isFavorite)
  }

  @Test
  fun getDishDetail_nonExistent_returnsNull() = testingDb {
    val dish = dishQueries.getDishDetail("Non Existent Dish").awaitAsOneOrNull()
    assertNull(dish)
  }

  @Test
  fun upsertDish_modifiesExisting() = testingDb {
    dishQueries.upsertDish(
      "Burger",
      "burger",
      "Juicy beef burger",
      "meat_dish",
      3L,
      "burger.jpg",
      "Grill patty. Assemble."
    )
    val original = dishQueries.getDishDetail("Burger").awaitAsOne()

    dishQueries.upsertDish(
      "Burger",
      "burger",
      "Plant-based alternative",
      "vegetable_dish",
      2L,
      "veggie.jpg",
      "Cook veggie patty."
    )
    val updated = dishQueries.getDishDetail("Burger").awaitAsOne()

    assertEquals("Burger", updated.name)
    assertEquals("vegetable_dish", updated.category)
    assertEquals(2L, updated.difficulty)
    assertEquals("Plant-based alternative", updated.description)
    assertEquals("vegetable_dish", updated.category)
    assertEquals("veggie.jpg", updated.image)
    assertTrue(original != updated || original.category != updated.category)
  }

  @Test
  fun deleteAllDishes_clearsTable() = testingDb {
    val categories = listOf("aquatic", "soup", "drink", "dessert", "condiment")
    repeat(5) { i ->
      dishQueries.upsertDish("Dish $i", "dish $i", "Desc $i", categories[i], 1L, "img", "content $i")
    }
    assertEquals(5, dishQueries.getDishSummaries(null, null, 0L).awaitAsList().size)

    dishQueries.deleteAllDishes()

    val dishesAfter = dishQueries.getDishSummaries(null, null, 0L).awaitAsList()
    assertTrue(dishesAfter.isEmpty())
  }

  @Test
  fun getDishCategories_returnsDistinct() = testingDb {
    dishQueries.upsertDish("Pizza", "pizza", "Cheesy", "staple", 2L, "p.jpg", "Bake.")
    dishQueries.upsertDish("Pasta", "pasta", "Italian", "staple", 2L, "pa.jpg", "Boil.")
    dishQueries.upsertDish("Salad", "salad", "Healthy", "vegetable_dish", 1L, "s.jpg", "Mix.")

    val categories = dishQueries.getDishCategories().awaitAsList()
    assertEquals(2, categories.size)
    assertTrue(categories.contains("staple"))
    assertTrue(categories.contains("vegetable_dish"))
  }

  @Test
  fun getDishSummaries_filterByCategory() = testingDb {
    dishQueries.upsertDish("Pancakes", "pancakes", "Fluffy", "breakfast", 2L, "img", "Mix and fry.")
    dishQueries.upsertDish("Omelette", "omelette", "Egg dish", "breakfast", 3L, "img", "Whisk and cook.")
    dishQueries.upsertDish("Steak", "steak", "Beef", "meat_dish", 4L, "img", "Grill.")

    val breakfastDishes = dishQueries.getDishSummaries("breakfast", null, 0L).awaitAsList()
    assertEquals(2, breakfastDishes.size)
    assertTrue(breakfastDishes.all { it.category == "breakfast" })

    val meatDishes = dishQueries.getDishSummaries("meat_dish", null, 0L).awaitAsList()
    assertEquals(1, meatDishes.size)
    assertEquals("Steak", meatDishes.first().name)

    val emptyCategory = dishQueries.getDishSummaries("soup", null, 0L).awaitAsList()
    assertTrue(emptyCategory.isEmpty())
  }

  @Test
  fun getDishSummaries_filterByDifficulty() = testingDb {
    dishQueries.upsertDish("Sushi", "sushi", "Rolls", "aquatic", 2L, "img", "Roll rice.")
    dishQueries.upsertDish("Ramen", "ramen", "Noodles", "staple", 2L, "img", "Boil broth.")
    dishQueries.upsertDish("Wellington", "wellington", "Hard", "meat_dish", 4L, "img", "Wrap and bake.")

    val level2 = dishQueries.getDishSummaries(null, 2L, 0L).awaitAsList()
    assertEquals(2, level2.size)
    assertTrue(level2.all { it.difficulty == 2L })

    val level4 = dishQueries.getDishSummaries(null, 4L, 0L).awaitAsList()
    assertEquals(1, level4.size)
    assertEquals("Wellington", level4.first().name)
  }

  @Test
  fun getDishSummaries_filterByFavorite() = testingDb {
    dishQueries.upsertDish("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake.")
    dishQueries.upsertDish("Salad", "salad", "Fresh", "vegetable_dish", 1L, "img", "Toss.")
    dishQueries.upsertDish("Soup", "soup", "Warm", "soup", 2L, "img", "Simmer.")

    dishQueries.insertDishFavorite("Pizza")
    dishQueries.insertDishFavorite("Soup")

    val favorites = dishQueries.getDishSummaries(null, null, 1L).awaitAsList()
    assertEquals(2, favorites.size)
    assertTrue(favorites.all { it.isFavorite })
    assertTrue(favorites.any { it.name == "Pizza" })
    assertTrue(favorites.any { it.name == "Soup" })
  }

  @Test
  fun insertDishFavoriteAndIsFavorite() = testingDb {
    dishQueries.upsertDish("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake.")
    dishQueries.insertDishFavorite("Pizza")

    val isFavorite = dishQueries.isDishFavorite("Pizza").awaitAsOne()
    assertEquals(true, isFavorite)
  }

  @Test
  fun deleteDishFavorite() = testingDb {
    dishQueries.upsertDish("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake.")
    dishQueries.insertDishFavorite("Pizza")
    assertEquals(true, dishQueries.isDishFavorite("Pizza").awaitAsOne())

    dishQueries.deleteDishFavorite("Pizza")
    assertEquals(false, dishQueries.isDishFavorite("Pizza").awaitAsOne())
  }

  @Test
  fun getRandomDishName() = testingDb {
    dishQueries.upsertDish("Test Dish", "test dish", "A test", "staple", 1L, "img", "Test content.")

    val name = dishQueries.getRandomDishName().awaitAsOneOrNull()
    assertNotNull(name)
    assertEquals("Test Dish", name)
  }
}
