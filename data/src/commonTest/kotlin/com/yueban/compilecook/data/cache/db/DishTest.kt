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
      category = 1L,
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
    assertEquals("A classic Italian pasta dish", insertedDish.description)
    assertEquals(1L, insertedDish.category)
    assertEquals(2L, insertedDish.difficulty)
    assertEquals("Black Pepper", insertedDish.addition)

    // Check for a non-existent Name
    val nonExistentDish = dishQueries.getByName("Non Existent Food").awaitAsOneOrNull()
    assertNull(nonExistentDish, "Querying a non-existent Name should return null.")
  }

  @Test
  fun getAll_returnsAllInsertedDishes() = testingDb {
    dishQueries.upsertDish("Pizza", "Cheesy delight", 1L, 3L, "pizza.jpg", "Dough, Sauce, Cheese", "c", "o", "None")
    dishQueries.upsertDish("Salad", "Healthy bowl", 2L, 1L, "salad.jpg", "Lettuce, Tomato", "c", "o", "Dressing")

    val allDishes = dishQueries.getAll().awaitAsList()
    assertEquals(2, allDishes.size, "Should return all 2 inserted dishes.")
    assertTrue(allDishes.any { it.name == "Pizza" }, "Pizza should be in the list.")
    assertTrue(allDishes.any { it.name == "Salad" }, "Salad should be in the list.")
  }

  @Test
  fun upsertDish_modifiesCorrectEntry() = testingDb {
    // 1. Insert initial dish
    dishQueries.upsertDish("Burger", "Juicy beef burger", 1L, 3L, "burger.jpg", "Bun, Patty", "c", "o", "Ketchup")
    val originalDish = dishQueries.getByName("Burger").awaitAsOne()

    // 2. Upsert the SAME dish name with NEW details (this acts as an UPDATE)
    dishQueries.upsertDish(
      name = "Burger", // Keep name same to trigger replace
      description = "Plant-based alternative",
      category = 2L,
      difficulty = 2L,
      image = "veggie.jpg",
      ingredient = "Veggie Patty, Buns",
      calculation = "New Calc",
      operation = "New Op",
      addition = "None"
    )

    val updatedDish = dishQueries.getByName("Burger").awaitAsOne()

    // Verify constraints
    val allDishes = dishQueries.getAll().awaitAsList()
    assertEquals(1, allDishes.size, "Should still be only 1 dish after upsert update.")

    // Verify fields updated
    assertEquals("Burger", updatedDish.name)
    assertEquals("Plant-based alternative", updatedDish.description)
    assertNotEquals(originalDish.description, updatedDish.description, "Description should have changed.")
    assertEquals(2L, updatedDish.difficulty, "Difficulty should have changed.")
    assertEquals("None", updatedDish.addition, "Addition should be updated to 'None'.")
  }

  @Test
  fun deleteByName_removesCorrectEntry() = testingDb {
    dishQueries.upsertDish("Steak", "Ribeye", 3L, 4L, "s.jpg", "Beef", "c", "o", "None")
    dishQueries.upsertDish("Fries", "Crispy", 4L, 1L, "f.jpg", "Potato", "c", "o", "Salt")

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
    repeat(5) { i ->
      dishQueries.upsertDish("Dish $i", "Desc $i", i.toLong(), 1L, "img", "ing", "c", "o", "None")
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
    dishQueries.upsertDish("Spaghetti", "Pasta", 1L, 2L, "img", "ing", "c", "o", "None")
    dishQueries.upsertDish("Lasagna", "Layers", 1L, 3L, "img", "ing", "c", "o", "None")
    // Category 2
    dishQueries.upsertDish("Taco", "Crunchy", 2L, 2L, "img", "ing", "c", "o", "None")

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
    dishQueries.upsertDish("Sushi", "Rolls", 5L, 2L, "img", "ing", "c", "o", "None")
    dishQueries.upsertDish("Ramen", "Noodles", 5L, 2L, "img", "ing", "c", "o", "None")
    // Difficulty 4
    dishQueries.upsertDish("Beef Wellington", "Hard", 6L, 4L, "img", "ing", "c", "o", "None")

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
