package com.yueban.compilecook.data.cache

import app.cash.sqldelight.async.coroutines.awaitCreate
import com.yueban.compilecook.data.cache.db.AppDatabase
import com.yueban.compilecook.data.cache.db.provideInMemoryDbDriver
import com.yueban.compilecook.data.db.entity.DishLocalEntity
import com.yueban.compilecook.data.db.entity.TipLocalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DishLocalDataSourceTest {
  private suspend fun createDataSource(): Pair<DishLocalDataSource, () -> Unit> {
    val driver = provideInMemoryDbDriver(AppDatabase.Schema)
    AppDatabase.Schema.awaitCreate(driver)
    val db = AppDatabase(driver)
    val dataSource = DishLocalDataSourceImpl(
      dishQueries = db.dishQueries,
      tipQueries = db.tipQueries,
      defaultDispatcher = UnconfinedTestDispatcher(),
    )
    return dataSource to { driver.close() }
  }

  @Test
  fun getDishSummaries_all() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()

    assertTrue(dataSource.getDishSummaries(null, null, false).first().isEmpty())

    dataSource.upsertDish(
      DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake.")
    )
    val dishes = dataSource.getDishSummaries(null, null, false).first()
    assertEquals(1, dishes.size)
    assertEquals("Pizza", dishes.first().name)
    assertEquals("staple", dishes.first().category)
    assertEquals(false, dishes.first().isFavorite)

    cleanup()
  }

  @Test
  fun getDishSummaries_filterByCategory() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Pancakes", "pancakes", "Fluffy", "breakfast", 2L, "img", "Mix."))
    dataSource.upsertDish(DishLocalEntity("Steak", "steak", "Beef", "meat_dish", 4L, "img", "Grill."))
    dataSource.upsertDish(DishLocalEntity("Omelette", "omelette", "Eggs", "breakfast", 3L, "img", "Cook."))

    val breakfast = dataSource.getDishSummaries("breakfast", null, false).first()
    assertEquals(2, breakfast.size)
    assertTrue(breakfast.all { it.category == "breakfast" })

    val meat = dataSource.getDishSummaries("meat_dish", null, false).first()
    assertEquals(1, meat.size)
    assertEquals("Steak", meat.first().name)

    val empty = dataSource.getDishSummaries("soup", null, false).first()
    assertTrue(empty.isEmpty())

    cleanup()
  }

  @Test
  fun getDishSummaries_filterByDifficulty() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Sushi", "sushi", "Rolls", "aquatic", 2L, "img", "Roll."))
    dataSource.upsertDish(DishLocalEntity("Ramen", "ramen", "Noodles", "staple", 2L, "img", "Boil."))
    dataSource.upsertDish(DishLocalEntity("Wellington", "wellington", "Hard", "meat_dish", 4L, "img", "Wrap."))

    val level2 = dataSource.getDishSummaries(null, 2, false).first()
    assertEquals(2, level2.size)
    assertTrue(level2.all { it.difficulty == 2L })

    val level4 = dataSource.getDishSummaries(null, 4, false).first()
    assertEquals(1, level4.size)
    assertEquals("Wellington", level4.first().name)

    cleanup()
  }

  @Test
  fun getDishSummaries_filterByFavorite() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake."))
    dataSource.upsertDish(DishLocalEntity("Salad", "salad", "Fresh", "vegetable_dish", 1L, "img", "Toss."))

    dataSource.toggleDishFavorite("Pizza")

    val favorites = dataSource.getDishSummaries(null, null, true).first()
    assertEquals(1, favorites.size)
    assertEquals("Pizza", favorites.first().name)
    assertTrue(favorites.first().isFavorite)

    cleanup()
  }

  @Test
  fun getDishByName() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(
      DishLocalEntity("Carbonara", "carbonara", "Pasta dish", "staple", 2L, "img", "Cook pasta. Mix eggs.")
    )

    // First emission is the data after upsert
    val detail = dataSource.getDishByName("Carbonara").first()
    assertNotNull(detail)
    assertEquals("Carbonara", detail.name)
    assertEquals("Cook pasta. Mix eggs.", detail.content)

    // Non-existent name
    val nonExistent = dataSource.getDishByName("NonExistent").first()
    assertNull(nonExistent)

    cleanup()
  }

  @Test
  fun getDishCategories() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake."))
    dataSource.upsertDish(DishLocalEntity("Pasta", "pasta", "Italian", "staple", 2L, "img", "Boil."))
    dataSource.upsertDish(DishLocalEntity("Salad", "salad", "Healthy", "vegetable_dish", 1L, "img", "Toss."))

    val categories = dataSource.getDishCategories().first()
    assertEquals(2, categories.size)
    assertTrue(categories.contains("staple"))
    assertTrue(categories.contains("vegetable_dish"))

    cleanup()
  }

  @Test
  fun getRandomDishName() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Test Dish", "test dish", "A test", "staple", 1L, "img", "Content."))

    val name = dataSource.getRandomDishName()
    assertEquals("Test Dish", name)

    cleanup()
  }

  @Test
  fun upsertDish() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val dish = DishLocalEntity("Burger", "burger", "Beef burger", "meat_dish", 3L, "img", "Grill patty.")
    dataSource.upsertDish(dish)

    val dishes = dataSource.getDishSummaries(null, null, false).first()
    assertEquals(1, dishes.size)
    assertEquals("Burger", dishes.first().name)

    cleanup()
  }

  @Test
  fun updateDishes() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    val dishes = listOf(
      DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake."),
      DishLocalEntity("Salad", "salad", "Fresh", "vegetable_dish", 1L, "img", "Toss."),
      DishLocalEntity("Soup", "soup", "Warm", "soup", 2L, "img", "Simmer."),
    )
    dataSource.updateDishes(dishes)

    val all = dataSource.getDishSummaries(null, null, false).first()
    assertEquals(3, all.size)

    cleanup()
  }

  @Test
  fun deleteDishByName() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Steak", "steak", "Beef", "meat_dish", 4L, "img", "Grill."))
    dataSource.upsertDish(DishLocalEntity("Fries", "fries", "Crispy", "semi_finished", 1L, "img", "Fry."))

    dataSource.deleteDishByName("Steak")

    val remaining = dataSource.getDishSummaries(null, null, false).first()
    assertEquals(1, remaining.size)
    assertEquals("Fries", remaining.first().name)

    cleanup()
  }

  @Test
  fun deleteAllDishes() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("One", "one", "First", "staple", 1L, "img", "A."))
    dataSource.upsertDish(DishLocalEntity("Two", "two", "Second", "meat_dish", 2L, "img", "B."))

    dataSource.deleteAllDishes()

    val all = dataSource.getDishSummaries(null, null, false).first()
    assertTrue(all.isEmpty())

    cleanup()
  }

  @Test
  fun toggleDishFavorite_on() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake."))

    dataSource.toggleDishFavorite("Pizza")
    val dishes = dataSource.getDishSummaries(null, null, true).first()
    assertEquals(1, dishes.size)
    assertTrue(dishes.first().isFavorite)

    cleanup()
  }

  @Test
  fun toggleDishFavorite_off() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertDish(DishLocalEntity("Pizza", "pizza", "Cheesy", "staple", 2L, "img", "Bake."))

    dataSource.toggleDishFavorite("Pizza")
    assertTrue(dataSource.getDishSummaries(null, null, true).first().isNotEmpty())

    dataSource.toggleDishFavorite("Pizza")
    val favorites = dataSource.getDishSummaries(null, null, true).first()
    assertTrue(favorites.isEmpty())

    cleanup()
  }

  // === Tip tests ===

  @Test
  fun getTipSummaries() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Knife Skills", "knife skills", "basic", "Keep fingers curled."))
    dataSource.upsertTip(TipLocalEntity("Searing", "searing", "learn", "Dry the meat first."))

    val tips = dataSource.getTipSummaries().first()
    assertEquals(2, tips.size)
    assertEquals("Knife Skills", tips.first().name)
    assertEquals("basic", tips.first().type)
    assertEquals(false, tips.first().isFavorite)

    cleanup()
  }

  @Test
  fun getTipDetail() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Knife Skills", "knife skills", "basic", "Keep fingers curled."))

    val detail = dataSource.getTipDetail("Knife Skills").first()
    assertNotNull(detail)
    assertEquals("Knife Skills", detail.name)
    assertEquals("Keep fingers curled.", detail.content)

    val nonExistent = dataSource.getTipDetail("NonExistent").first()
    assertNull(nonExistent)

    cleanup()
  }

  @Test
  fun upsertTip() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Test Tip", "test tip", "basic", "Content."))

    val tips = dataSource.getTipSummaries().first()
    assertEquals(1, tips.size)
    assertEquals("Test Tip", tips.first().name)

    cleanup()
  }

  @Test
  fun updateTips() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.updateTips(
      listOf(
        TipLocalEntity("Tip 1", "tip 1", "basic", "Content 1"),
        TipLocalEntity("Tip 2", "tip 2", "learn", "Content 2"),
      )
    )

    val tips = dataSource.getTipSummaries().first()
    assertEquals(2, tips.size)

    cleanup()
  }

  @Test
  fun deleteTipByName() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Keep", "keep", "basic", "Keep."))
    dataSource.upsertTip(TipLocalEntity("Remove", "remove", "learn", "Remove."))

    dataSource.deleteTipByName("Remove")

    val tips = dataSource.getTipSummaries().first()
    assertEquals(1, tips.size)
    assertEquals("Keep", tips.first().name)

    cleanup()
  }

  @Test
  fun deleteAllTips() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("A", "a", "basic", "A."))
    dataSource.upsertTip(TipLocalEntity("B", "b", "learn", "B."))

    dataSource.deleteAllTips()

    assertTrue(dataSource.getTipSummaries().first().isEmpty())

    cleanup()
  }

  @Test
  fun toggleTipFavorite_on() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Knife Skills", "knife skills", "basic", "Keep fingers curled."))

    dataSource.toggleTipFavorite("Knife Skills")
    val tips = dataSource.getTipDetail("Knife Skills").first()
    assertNotNull(tips)
    assertTrue(tips.isFavorite)

    cleanup()
  }

  @Test
  fun toggleTipFavorite_off() = runTest(UnconfinedTestDispatcher()) {
    val (dataSource, cleanup) = createDataSource()
    dataSource.upsertTip(TipLocalEntity("Knife Skills", "knife skills", "basic", "Keep fingers curled."))

    dataSource.toggleTipFavorite("Knife Skills")
    dataSource.toggleTipFavorite("Knife Skills")

    val tips = dataSource.getTipDetail("Knife Skills").first()
    assertNotNull(tips)
    assertEquals(false, tips.isFavorite)

    cleanup()
  }
}
