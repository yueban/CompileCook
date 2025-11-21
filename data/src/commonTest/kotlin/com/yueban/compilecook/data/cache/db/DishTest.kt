package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DishTest {
  @Test
  fun initialState_isEmpty() = testingDb {
    val dishes = dishQueries.getAll().awaitAsList()
    assertTrue(dishes.isEmpty(), "Initially, the database should be empty.")
  }

  @Test
  fun upsertAndGetByName_succeeds() = testingDb {
    // Insert a dish using upsert
    dishQueries.upsertDish(
      name = "Spaghetti Carbonara",
      description = "A classic Italian pasta dish",
      category = "staple",
      difficulty = 2L,
      image = "carbonara.jpg",
      ingredient = "Pasta, Eggs, Pancetta",
      calculation = "cal_1",
      operation = "op_1",
      addition = "Black Pepper"
    )

    // Retrieve the dish by its Name (Primary Key)
    val insertedDish = dishQueries.getByName("Spaghetti Carbonara").awaitAsOneOrNull()

    assertNotNull(insertedDish, "Inserted dish should be found by Name.")
    assertEquals("Spaghetti Carbonara", insertedDish.name)
    assertEquals("staple", insertedDish.category)
    assertEquals(2L, insertedDish.difficulty)
    assertEquals("Black Pepper", insertedDish.addition)

    // Check for a non-existent Name
    val nonExistentDish = dishQueries.getByName("Non Existent Food").awaitAsOneOrNull()
    assertNull(nonExistentDish, "Querying a non-existent Name should return null.")
  }

  @Test
  fun getAll_returnsAllInsertedDishes() = testingDb {
    dishQueries.upsertDish(
      "Pizza",
      "Cheesy delight",
      "staple",
      3L,
      "pizza.jpg",
      "Dough, Sauce, Cheese",
      "c",
      "o",
      "None"
    )
    dishQueries.upsertDish(
      "Salad",
      "Healthy bowl",
      "vegetable_dish",
      1L,
      "salad.jpg",
      "Lettuce, Tomato",
      "c",
      "o",
      "Dressing"
    )

    val allDishes = dishQueries.getAll().awaitAsList()
    assertEquals(2, allDishes.size, "Should return all 2 inserted dishes.")
    assertTrue(allDishes.any { it.name == "Pizza" }, "Pizza should be in the list.")
    assertTrue(allDishes.any { it.name == "Salad" }, "Salad should be in the list.")
  }

  @Test
  fun upsertDish_modifiesCorrectEntry() = testingDb {
    // 1. Insert initial dish
    dishQueries.upsertDish(
      "Burger",
      "Juicy beef burger",
      "meat_dish",
      3L,
      "burger.jpg",
      "Bun, Patty",
      "c",
      "o",
      "Ketchup"
    )
    val originalDish = dishQueries.getByName("Burger").awaitAsOne()

    // 2. Upsert the SAME dish name with NEW details (acts as UPDATE)
    dishQueries.upsertDish(
      name = "Burger", // Keep name same
      description = "Plant-based alternative",
      category = "vegetable_dish",
      difficulty = 2L,
      image = "veggie.jpg",
      ingredient = "Veggie Patty, Buns",
      calculation = "New Calc",
      operation = "New Op",
      addition = "None"
    )

    val updatedDish = dishQueries.getByName("Burger").awaitAsOne()

    // Verify fields updated
    assertEquals("Burger", updatedDish.name)
    assertEquals("vegetable_dish", updatedDish.category)
    assertNotEquals(originalDish.category, updatedDish.category, "Category should have changed.")
    assertEquals("Plant-based alternative", updatedDish.description)
  }

  @Test
  fun deleteByName_removesCorrectEntry() = testingDb {
    dishQueries.upsertDish("Steak", "Ribeye", "meat_dish", 4L, "s.jpg", "Beef", "c", "o", "None")
    dishQueries.upsertDish("Fries", "Crispy", "semi_finished", 1L, "f.jpg", "Potato", "c", "o", "Salt")

    // Delete the first dish by Name
    dishQueries.deleteByName("Steak")

    val remainingDishes = dishQueries.getAll().awaitAsList()
    assertEquals(1, remainingDishes.size, "There should be only one dish remaining.")
    assertEquals("Fries", remainingDishes.first().name)

    val deletedDish = dishQueries.getByName("Steak").awaitAsOneOrNull()
    assertNull(deletedDish, "The deleted dish should no longer be found.")
  }

  @Test
  fun deleteAll_clearsTheTable() = testingDb {
    // Insert some data
    val categories = listOf("aquatic", "soup", "drink", "dessert", "condiment")
    repeat(5) { i ->
      dishQueries.upsertDish("Dish $i", "Desc $i", categories[i], 1L, "img", "ing", "c", "o", "None")
    }
    assertEquals(5, dishQueries.getAll().awaitAsList().size, "5 dishes should exist before delete.")

    // Clear the table
    dishQueries.deleteAll()

    val dishesAfterDelete = dishQueries.getAll().awaitAsList()
    assertTrue(dishesAfterDelete.isEmpty(), "The table should be empty after deleteAll.")
  }

  @Test
  fun getByCategory_returnsMatchingDishes() = testingDb {
    // Category "staple"
    dishQueries.upsertDish("Spaghetti", "Pasta", "staple", 2L, "img", "ing", "c", "o", "None")
    dishQueries.upsertDish("Lasagna", "Layers", "staple", 3L, "img", "ing", "c", "o", "None")
    // Category "breakfast"
    dishQueries.upsertDish("Pancakes", "Fluffy", "breakfast", 2L, "img", "ing", "c", "o", "None")

    // Test querying "staple"
    val stapleDishes = dishQueries.getByCategory("staple").awaitAsList()
    assertEquals(2, stapleDishes.size)
    assertTrue(stapleDishes.all { it.category == "staple" }, "All dishes should belong to category 'staple'.")

    // Test querying "breakfast"
    val breakfastDishes = dishQueries.getByCategory("breakfast").awaitAsList()
    assertEquals(1, breakfastDishes.size)
    assertEquals("Pancakes", breakfastDishes.first().name)

    // Test non-existent category
    val emptyCategoryDishes = dishQueries.getByCategory("space_food").awaitAsList()
    assertTrue(emptyCategoryDishes.isEmpty(), "Querying a non-existent category should return an empty list.")
  }

  @Test
  fun getByDifficulty_returnsMatchingDishes() = testingDb {
    // Difficulty 2
    dishQueries.upsertDish("Sushi", "Rolls", "aquatic", 2L, "img", "ing", "c", "o", "None")
    dishQueries.upsertDish("Ramen", "Noodles", "staple", 2L, "img", "ing", "c", "o", "None")
    // Difficulty 4
    dishQueries.upsertDish("Beef Wellington", "Hard", "meat_dish", 4L, "img", "ing", "c", "o", "None")

    val difficulty2Dishes = dishQueries.getByDifficulty(2L).awaitAsList()
    assertEquals(2, difficulty2Dishes.size)
    assertTrue(difficulty2Dishes.all { it.difficulty == 2L }, "All dishes should have difficulty 2.")

    val difficulty4Dishes = dishQueries.getByDifficulty(4L).awaitAsList()
    assertEquals(1, difficulty4Dishes.size)
    assertEquals("Beef Wellington", difficulty4Dishes.first().name)
  }
}
