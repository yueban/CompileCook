package com.yueban.compilecook.ui.util

object SmartMatcher {
  private val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'v')

  /**
   * Generic match function.
   * @param name The display name (e.g., "麻婆豆腐")
   * @param pinyin The pinyin string (e.g., "mapodoufu")
   * @param query The user's input
   */
  @Suppress("ReturnCount")
  fun matches(name: String, pinyin: String, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim()

    // 1. Separate query into non-Latin (usually Chinese) and Latin parts
    val (nonLatinPart, latinPart) = splitQuery(q)

    // 2. If there are Chinese characters, they MUST exist in the name
    if (nonLatinPart.isNotEmpty() && !name.contains(nonLatinPart, ignoreCase = true)) {
      return false
    }

    // 3. If there are Latin characters, they must "Smart Match" the pinyin
    if (latinPart.isNotEmpty()) {
      return smartPinyinMatch(query = latinPart, target = pinyin)
    }

    return true
  }

  private fun splitQuery(query: String): Pair<String, String> {
    val nonLatinBuilder = StringBuilder()
    val latinBuilder = StringBuilder()

    query.forEach { char ->
      // treat everything that isn't a basic Latin letter/digit as "name" content
      if (char.lowercaseChar() in 'a'..'z' || char in '0'..'9') {
        latinBuilder.append(char)
      } else {
        nonLatinBuilder.append(char)
      }
    }
    return nonLatinBuilder.toString() to latinBuilder.toString()
  }

  /**
   * Core logic: Context-Aware Consonant Anchoring
   */
  private fun smartPinyinMatch(query: String, target: String): Boolean {
    val qChars = query.lowercase().toCharArray()
    val tChars = target.lowercase().toCharArray()

    // Check every possible starting position in the target
    for (startIndex in tChars.indices) {
      // RULE 1: A match can only START at a "Strong" character.
      // You cannot start a match by skipping the first letter of a word/syllable.
      if (isSkippable(tChars, startIndex)) continue

      var qIdx = 0
      var tIdx = startIndex

      while (tIdx < tChars.size && qIdx < qChars.size) {
        if (qChars[qIdx] == tChars[tIdx]) {
          qIdx++
          tIdx++
        } else if (isSkippable(tChars, tIdx)) {
          // RULE 2: Inside a match, we can skip "Weak" characters (vowels, h/n/g modifiers)
          tIdx++
        } else {
          // RULE 3: We hit a "Hard Initial" that doesn't match the query.
          break
        }
      }

      // If we successfully exhausted the query, we have a match
      if (qIdx == qChars.size) return true
    }

    return false
  }

  /**
   * Determines if a character in the target Pinyin is "Weak" (skippable).
   */
  private fun isSkippable(target: CharArray, index: Int): Boolean {
    val c = target[index]

    // Vowels are always skippable internals
    if (VOWELS.contains(c)) return true

    // Contextual Consonants
    val prev = if (index > 0) target[index - 1] else null
    return when (c) {
      // 'h' is only weak if it turns z, c, s into zh, ch, sh
      'h' -> prev == 'z' || prev == 'c' || prev == 's'
      // 'n' is only weak if it's a nasal coda (like 'an', 'en')
      'n' -> prev != null && VOWELS.contains(prev)
      // 'g' is only weak if it's part of the 'ng' coda
      'g' -> prev == 'n'
      // 'r' is only weak if it's part of 'er'
      'r' -> prev != null && VOWELS.contains(prev)
      else -> false
    }
  }
}
