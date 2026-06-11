package com.yueban.compilecook.data.cache.db

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TipTest {
  @Test
  fun initialState_tipSummariesEmpty() = testingDb {
    val tips = tipQueries.getTipSummaries().awaitAsList()
    assertTrue(tips.isEmpty(), "Initially, the tip table should be empty.")
  }

  @Test
  fun upsertAndSelectTip() = testingDb {
    tipQueries.upsertTip(
      name = "Knife Skills 101",
      pinyin = "knife skills 101",
      type = "basic",
      content = "Keep your fingers curled when chopping."
    )

    val tips = tipQueries.getTipSummaries().awaitAsList()
    assertEquals(1, tips.size)
    assertEquals("Knife Skills 101", tips.first().name)
    assertEquals("basic", tips.first().type)
    assertEquals(false, tips.first().isFavorite)

    val detail = tipQueries.getTipDetail("Knife Skills 101").awaitAsOneOrNull()
    assertNotNull(detail)
    assertEquals("Knife Skills 101", detail.name)
    assertEquals("basic", detail.type)
    assertEquals("Keep your fingers curled when chopping.", detail.content)
    assertEquals(false, detail.isFavorite)
  }

  @Test
  fun getTipDetail_nonExistent_returnsNull() = testingDb {
    val tip = tipQueries.getTipDetail("Non Existent Tip").awaitAsOneOrNull()
    assertNull(tip)
  }

  @Test
  fun upsertTip_modifiesExisting() = testingDb {
    tipQueries.upsertTip("Seasoning", "seasoning", "basic", "Add salt at the beginning.")
    val original = tipQueries.getTipDetail("Seasoning").awaitAsOne()

    tipQueries.upsertTip("Seasoning", "seasoning", "advanced", "Layer salt throughout cooking.")
    val updated = tipQueries.getTipDetail("Seasoning").awaitAsOne()

    assertEquals("Seasoning", updated.name)
    assertEquals("advanced", updated.type)
    assertEquals("Layer salt throughout cooking.", updated.content)
    assertTrue(original.type != updated.type)
  }

  @Test
  fun deleteAllTips_clearsTable() = testingDb {
    tipQueries.upsertTip("Tip 1", "tip 1", "basic", "Content 1")
    tipQueries.upsertTip("Tip 2", "tip 2", "learn", "Content 2")
    tipQueries.upsertTip("Tip 3", "tip 3", "advanced", "Content 3")

    assertEquals(3, tipQueries.getTipSummaries().awaitAsList().size)

    tipQueries.deleteAllTips()

    assertTrue(tipQueries.getTipSummaries().awaitAsList().isEmpty())
  }

  @Test
  fun insertTipFavoriteAndIsFavorite() = testingDb {
    tipQueries.upsertTip("Knife Skills", "knife skills", "basic", "Keep fingers curled.")
    tipQueries.insertTipFavorite("Knife Skills")

    val isFavorite = tipQueries.isTipFavorite("Knife Skills").awaitAsOne()
    assertEquals(true, isFavorite)
  }

  @Test
  fun deleteTipFavorite() = testingDb {
    tipQueries.upsertTip("Knife Skills", "knife skills", "basic", "Keep fingers curled.")
    tipQueries.insertTipFavorite("Knife Skills")
    assertEquals(true, tipQueries.isTipFavorite("Knife Skills").awaitAsOne())

    tipQueries.deleteTipFavorite("Knife Skills")
    assertEquals(false, tipQueries.isTipFavorite("Knife Skills").awaitAsOne())
  }
}
