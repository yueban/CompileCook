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

class TipTest {

  @Test
  fun initialState_isEmpty() = testingDb {
    val tips = tipQueries.getAll().awaitAsList()
    assertTrue(tips.isEmpty(), "Initially, the tip table should be empty.")
  }

  @Test
  fun upsertAndGetByName_succeeds() = testingDb {
    // Insert a tip using upsert
    tipQueries.upsertTip(
      name = "Knife Skills 101",
      type = "basic",
      content = "Keep your fingers curled when chopping."
    )

    // Retrieve the tip by its Name (Primary Key)
    val insertedTip = tipQueries.getByName("Knife Skills 101").awaitAsOneOrNull()

    assertNotNull(insertedTip, "Inserted tip should be found by Name.")
    assertEquals("Knife Skills 101", insertedTip.name)
    assertEquals("basic", insertedTip.type)
    assertEquals("Keep your fingers curled when chopping.", insertedTip.content)

    // Check for a non-existent Name
    val nonExistentTip = tipQueries.getByName("Rocket Science").awaitAsOneOrNull()
    assertNull(nonExistentTip, "Querying a non-existent Name should return null.")
  }

  @Test
  fun getAll_returnsAllInsertedTips() = testingDb {
    tipQueries.upsertTip("Boiling Water", "basic", "Use a lid to boil faster.")
    tipQueries.upsertTip("Searing Meat", "learn", "Dry the meat first.")

    val allTips = tipQueries.getAll().awaitAsList()
    assertEquals(2, allTips.size, "Should return all 2 inserted tips.")
    assertTrue(allTips.any { it.name == "Boiling Water" })
    assertTrue(allTips.any { it.name == "Searing Meat" })
  }

  @Test
  fun upsertTip_modifiesCorrectEntry() = testingDb {
    // 1. Insert initial tip
    tipQueries.upsertTip(
      name = "Seasoning",
      type = "basic",
      content = "Add salt at the beginning."
    )
    val originalTip = tipQueries.getByName("Seasoning").awaitAsOne()

    // 2. Upsert the SAME name with NEW details (acts as UPDATE)
    tipQueries.upsertTip(
      name = "Seasoning", // Keep name same to trigger replace
      type = "advanced", // Changed type
      content = "Layer salt throughout the cooking process." // Changed content
    )

    val updatedTip = tipQueries.getByName("Seasoning").awaitAsOne()

    // Verify fields updated
    assertEquals("Seasoning", updatedTip.name)
    assertEquals("advanced", updatedTip.type)
    assertEquals("Layer salt throughout the cooking process.", updatedTip.content)

    assertNotEquals(originalTip.type, updatedTip.type, "Type should have changed.")
    assertNotEquals(originalTip.content, updatedTip.content, "Content should have changed.")
  }

  @Test
  fun deleteByName_removesCorrectEntry() = testingDb {
    tipQueries.upsertTip("Mise en place", "basic", "Prepare everything before cooking.")
    tipQueries.upsertTip("Sous Vide", "advanced", "Vacuum seal the food.")

    // Delete the first tip by Name
    tipQueries.deleteByName("Mise en place")

    val remainingTips = tipQueries.getAll().awaitAsList()
    assertEquals(1, remainingTips.size, "There should be only one tip remaining.")
    assertEquals("Sous Vide", remainingTips.first().name)

    val deletedTip = tipQueries.getByName("Mise en place").awaitAsOneOrNull()
    assertNull(deletedTip, "The deleted tip should no longer be found.")
  }

  @Test
  fun deleteAll_clearsTheTable() = testingDb {
    // Insert some data
    tipQueries.upsertTip("Tip 1", "basic", "Content 1")
    tipQueries.upsertTip("Tip 2", "learn", "Content 2")
    tipQueries.upsertTip("Tip 3", "advanced", "Content 3")

    assertEquals(3, tipQueries.getAll().awaitAsList().size, "3 tips should exist before delete.")

    // Clear the table
    tipQueries.deleteAll()

    val tipsAfterDelete = tipQueries.getAll().awaitAsList()
    assertTrue(tipsAfterDelete.isEmpty(), "The table should be empty after deleteAll.")
  }

  @Test
  fun getByType_returnsMatchingTips() = testingDb {
    // Insert mixed types
    tipQueries.upsertTip("Knife Care", "basic", "Don't wash in dishwasher.")
    tipQueries.upsertTip("Pot Selection", "basic", "Use heavy bottom pots.")

    tipQueries.upsertTip("Sauce Making", "learn", "Make a roux first.")

    tipQueries.upsertTip("Molecular Gastronomy", "advanced", "Use agar agar.")

    // Test querying "basic"
    val basicTips = tipQueries.getByType("basic").awaitAsList()
    assertEquals(2, basicTips.size)
    assertTrue(basicTips.all { it.type == "basic" })
    assertTrue(basicTips.any { it.name == "Knife Care" })

    // Test querying "learn"
    val learnTips = tipQueries.getByType("learn").awaitAsList()
    assertEquals(1, learnTips.size)
    assertEquals("Sauce Making", learnTips.first().name)

    // Test querying "advanced"
    val advancedTips = tipQueries.getByType("advanced").awaitAsList()
    assertEquals(1, advancedTips.size)
    assertEquals("Molecular Gastronomy", advancedTips.first().name)

    // Test querying non-existent type
    val emptyTips = tipQueries.getByType("random_type").awaitAsList()
    assertTrue(emptyTips.isEmpty(), "Querying a non-existent type should return an empty list.")
  }
}
