package com.yueban.compilecook.data.db

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
  fun insertAndGetById_succeeds() = testingDb {
    // Insert a dish
    dishQueries.insertDish(
      name = "Spaghetti Carbonara",
      category = 1L,
      difficulty = 2L,
      image = "carbonara.jpg",
      ingredient = "Pasta, Eggs, Pancetta",
      calculation = "cal_1",
      operation = "op_1",
      addition = "Black Pepper"
    )

    // Retrieve the dish by its expected ID (1)
    val insertedDish = dishQueries.getById(1L).awaitAsOneOrNull()
    assertNotNull(insertedDish, "Inserted dish should be found by ID 1.")
    assertEquals("Spaghetti Carbonara", insertedDish.name)
    assertEquals(1L, insertedDish.category)
    assertEquals(2L, insertedDish.difficulty)
    assertEquals("Black Pepper", insertedDish.addition)

    // Check for a non-existent ID
    val nonExistentDish = dishQueries.getById(99L).awaitAsOneOrNull()
    assertNull(nonExistentDish, "Querying a non-existent ID should return null.")
  }

  @Test
  fun getAll_returnsAllInsertedDishes() = testingDb {
    dishQueries.insertDish("Pizza", 1L, 3L, "pizza.jpg", "Dough, Sauce, Cheese", "c", "o", null)
    dishQueries.insertDish("Salad", 2L, 1L, "salad.jpg", "Lettuce, Tomato", "c", "o", "Dressing")

    val allDishes = dishQueries.getAll().awaitAsList()
    assertEquals(2, allDishes.size, "Should return all 2 inserted dishes.")
    assertTrue(allDishes.any { it.name == "Pizza" }, "Pizza should be in the list.")
    assertTrue(allDishes.any { it.name == "Salad" }, "Salad should be in the list.")
  }

  @Test
  fun updateDish_modifiesCorrectEntry() = testingDb {
    // Insert initial dish
    dishQueries.insertDish("Burger", 1L, 3L, "burger.jpg", "Bun, Patty", "c", "o", "Ketchup")
    val originalDish = dishQueries.getById(1L).awaitAsOne()

    // Update the dish
    dishQueries.updateDish(
      id = 1L,
      name = "Veggie Burger",
      category = 2L,
      difficulty = 2L,
      image = "veggie.jpg",
      ingredient = "Veggie Patty, Buns",
      calculation = "New Calc",
      operation = "New Op",
      addition = null
    )

    val updatedDish = dishQueries.getById(1L).awaitAsOne()
    assertEquals(1L, updatedDish.id)
    assertEquals("Veggie Burger", updatedDish.name)
    assertNotEquals(originalDish.name, updatedDish.name, "Name should have changed.")
    assertEquals(2L, updatedDish.difficulty, "Difficulty should have changed.")
    assertNull(updatedDish.addition, "Addition should be updated to null.")
  }

  @Test
  fun deleteById_removesCorrectEntry() = testingDb {
    dishQueries.insertDish("Steak", 3L, 4L, "s.jpg", "Beef", "c", "o", null)
    dishQueries.insertDish("Fries", 4L, 1L, "f.jpg", "Potato", "c", "o", "Salt")

    // Delete the first dish (ID 1)
    dishQueries.deleteById(1L)

    val remainingDishes = dishQueries.getAll().awaitAsList()
    assertEquals(1, remainingDishes.size, "There should be only one dish remaining.")
    assertEquals("Fries", remainingDishes.first().name)
    assertEquals(2L, remainingDishes.first().id) // Verify the correct item remains

    val deletedDish = dishQueries.getById(1L).awaitAsOneOrNull()
    assertNull(deletedDish, "The deleted dish should no longer be found.")
  }

  @Test
  fun deleteAll_clearsTheTable() = testingDb {
    // Insert some data
    repeat(5) { i ->
      dishQueries.insertDish("Dish $i", i.toLong(), 1L, "img", "ing", "c", "o", null)
    }
    assertEquals(5, dishQueries.getAll().awaitAsList().size, "5 dishes should exist before delete.")

    // Clear the table
    dishQueries.deleteAll()

    val dishesAfterDelete = dishQueries.getAll().awaitAsList()
    assertTrue(dishesAfterDelete.isEmpty(), "The table should be empty after deleteAll.")
  }

  @Test
  fun getByCategory_returnsMatchingDishes() = testingDb {
    // Category 1
    dishQueries.insertDish("Spaghetti", 1L, 2L, "img", "ing", "c", "o", null)
    dishQueries.insertDish("Lasagna", 1L, 3L, "img", "ing", "c", "o", null)
    // Category 2
    dishQueries.insertDish("Taco", 2L, 2L, "img", "ing", "c", "o", null)

    val category1Dishes = dishQueries.getByCategory(1L).awaitAsList()
    assertEquals(2, category1Dishes.size)
    assertTrue(category1Dishes.all { it.category == 1L }, "All dishes should belong to category 1.")

    val category2Dishes = dishQueries.getByCategory(2L).awaitAsList()
    assertEquals(1, category2Dishes.size)
    assertEquals("Taco", category2Dishes.first().name)

    val emptyCategoryDishes = dishQueries.getByCategory(99L).awaitAsList()
    assertTrue(emptyCategoryDishes.isEmpty(), "Querying a non-existent category should return an empty list.")
  }

  @Test
  fun getByDifficulty_returnsMatchingDishes() = testingDb {
    // Difficulty 2
    dishQueries.insertDish("Sushi", 5L, 2L, "img", "ing", "c", "o", null)
    dishQueries.insertDish("Ramen", 5L, 2L, "img", "ing", "c", "o", null)
    // Difficulty 4
    dishQueries.insertDish("Beef Wellington", 6L, 4L, "img", "ing", "c", "o", null)

    val difficulty2Dishes = dishQueries.getByDifficulty(2L).awaitAsList()
    assertEquals(2, difficulty2Dishes.size)
    assertTrue(difficulty2Dishes.all { it.difficulty == 2L }, "All dishes should have difficulty 2.")

    val difficulty4Dishes = dishQueries.getByDifficulty(4L).awaitAsList()
    assertEquals(1, difficulty4Dishes.size)
    assertEquals("Beef Wellington", difficulty4Dishes.first().name)

    val emptyDifficultyDishes = dishQueries.getByDifficulty(5L).awaitAsList()
    assertTrue(emptyDifficultyDishes.isEmpty(), "Querying a non-existent difficulty should return an empty list.")
  }
}
