package com.yueban.compilecook.repo.entity

import com.yueban.compilecook.data.cache.db.entity.TipDetailLocalEntity
import com.yueban.compilecook.data.cache.db.entity.TipSummaryLocalEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class TipMappingTest {
  @Test
  fun toTipSummary_mapsCorrectly() {
    val local = TipSummaryLocalEntity(
      name = "Knife Skills",
      pinyin = "knife skills",
      type = "basic",
      isFavorite = true,
    )

    val result = local.toTipSummary()

    assertEquals("Knife Skills", result.name)
    assertEquals("knife skills", result.pinyin)
    assertEquals(TipType.BASIC, result.type)
    assertEquals(true, result.isFavorite)
  }

  @Test
  fun toTipSummary_unknownType() {
    val local = TipSummaryLocalEntity("Test", "test", "nonexistent", false)
    val result = local.toTipSummary()
    assertEquals(TipType.UNKNOWN, result.type)
  }

  @Test
  fun toTipSummary_allTypes() {
    val mappings = listOf(
      "basic" to TipType.BASIC,
      "learn" to TipType.LEARN,
      "advanced" to TipType.ADVANCED,
    )

    for ((input, expected) in mappings) {
      val local = TipSummaryLocalEntity("Test", "test", input, false)
      assertEquals(expected, local.toTipSummary().type, "Expected $expected for '$input'")
    }
  }

  @Test
  fun toTipDetail_mapsCorrectly() {
    val local = TipDetailLocalEntity(
      name = "Knife Skills",
      pinyin = "knife skills",
      type = "basic",
      content = "Keep your fingers curled when chopping.",
      isFavorite = false,
    )

    val result = local.toTipDetail()

    assertEquals("Knife Skills", result.name)
    assertEquals("knife skills", result.pinyin)
    assertEquals(TipType.BASIC, result.type)
    assertEquals("Keep your fingers curled when chopping.", result.content)
    assertEquals(false, result.isFavorite)
  }

  @Test
  fun toTipDetail_unknownType() {
    val local = TipDetailLocalEntity("Test", "test", "invalid", "Content", false)
    assertEquals(TipType.UNKNOWN, local.toTipDetail().type)
  }

  @Test
  fun TipType_fromValue_exact() {
    assertEquals(TipType.BASIC, TipType.fromValue("basic"))
    assertEquals(TipType.LEARN, TipType.fromValue("learn"))
    assertEquals(TipType.ADVANCED, TipType.fromValue("advanced"))
  }

  @Test
  fun TipType_fromValue_ignoreCase() {
    assertEquals(TipType.BASIC, TipType.fromValue("BASIC"))
    assertEquals(TipType.LEARN, TipType.fromValue("Learn"))
    assertEquals(TipType.ADVANCED, TipType.fromValue("ADVANCED"))
  }

  @Test
  fun TipType_fromValue_unknown() {
    assertEquals(TipType.UNKNOWN, TipType.fromValue("nonexistent"))
    assertEquals(TipType.UNKNOWN, TipType.fromValue(""))
  }
}
