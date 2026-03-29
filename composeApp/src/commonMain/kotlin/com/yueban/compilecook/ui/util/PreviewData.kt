@file:Suppress("MaximumLineLength", "MaxLineLength", "MagicNumber")

package com.yueban.compilecook.ui.util

import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.ui.about.AboutState
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.dish.DishListState
import com.yueban.compilecook.ui.dish.DishState
import com.yueban.compilecook.ui.main.MainDishState
import com.yueban.compilecook.ui.main.MainTipState
import com.yueban.compilecook.ui.tip.TipState
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

object PreviewData {
  val mainTipState by lazy {
    val tip = PreviewConstant.tipSummary
    val tips = List(10) { index ->
      tip.copy(
        name = tip.name + index,
        type = TipType.entries.toTypedArray().let { it[index % it.size] },
        isFavorite = index % 2 == 0
      )
    }
    val groupedTips = tips.asSequence()
      .filter { it.type != TipType.UNKNOWN }
      .groupBy { it.type }
      .toList()
      .sortedBy { it.first.ordinal }.toList()
    MainTipState(
      loadingAsync = Success(Unit),
      groupedTipsAsync = Success(groupedTips)
    )
  }

  val mainDishState by lazy {
    MainDishState(
      loadingAsync = Success(Unit),
      dishCategoriesAsync = Success(DishCategory.entries)
    )
  }

  val dishListState by lazy {
    val dish = PreviewConstant.dishSummary
    val dishes = List(10) { index ->
      dish.copy(
        name = dish.name + index,
        pinyin = dish.pinyin + index,
        isFavorite = index % 2 == 0
      )
    }
    DishListState(
      dishCategory = PreviewConstant.dishSummary.category,
      dishesAsync = Success(dishes),
      startInSearchMode = false,
      isSearchActive = false,
      isFavorite = false,
    )
  }

  val dishListSearchState by lazy {
    dishListState.copy(isSearchActive = true)
  }

  val dishListEmptyState by lazy {
    DishListState(
      dishCategory = PreviewConstant.dishSummary.category,
      dishesAsync = Success(emptyList()),
      startInSearchMode = false,
      isSearchActive = false,
      isFavorite = false,
    )
  }

  val dishListFavoriteEmptyState by lazy {
    DishListState(
      dishCategory = null,
      dishesAsync = Success(emptyList()),
      startInSearchMode = false,
      isSearchActive = false,
      isFavorite = true,
    )
  }

  val tipState by lazy {
    val markdownContent = PreviewConstant.tipDetail.content.trimIndent()
    val rootNode = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(markdownContent)
    val markdownState = State.Success(rootNode, markdownContent, true)
    TipState(
      tipName = PreviewConstant.tipDetail.name,
      contentAsync = Success(markdownState),
    )
  }

  val dishState by lazy {
    val markdownContent = PreviewConstant.dishDetail.content.trimIndent()
    val rootNode = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(markdownContent)
    val markdownState = State.Success(rootNode, markdownContent, true)
    DishState(
      dishName = PreviewConstant.dishDetail.name,
      dishAsync = Success(PreviewConstant.dishDetail),
      contentAsync = Success(markdownState),
    )
  }

  val aboutState by lazy {
    val libsValue = Libs.Builder().withJson(PreviewConstant.ABOUT_LIBRARIES).build()
    AboutState(aboutLibs = Success(libsValue))
  }
}
