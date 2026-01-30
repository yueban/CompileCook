package com.yueban.compilecook.ui.util

import androidx.compose.runtime.Composable
import com.yueban.compilecook.repo.entity.DishCategory
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.main_dish_category_aquatic
import compilecook.composeapp.generated.resources.main_dish_category_breakfast
import compilecook.composeapp.generated.resources.main_dish_category_condiment
import compilecook.composeapp.generated.resources.main_dish_category_dessert
import compilecook.composeapp.generated.resources.main_dish_category_drink
import compilecook.composeapp.generated.resources.main_dish_category_meat_dish
import compilecook.composeapp.generated.resources.main_dish_category_semi_finished
import compilecook.composeapp.generated.resources.main_dish_category_soup
import compilecook.composeapp.generated.resources.main_dish_category_staple
import compilecook.composeapp.generated.resources.main_dish_category_vegetable_dish
import org.jetbrains.compose.resources.stringResource

val DishCategory.emoji: String
  get() = when (this) {
    DishCategory.VEGETABLE_DISH -> "🥬"
    DishCategory.MEAT_DISH -> "🥩"
    DishCategory.AQUATIC -> "🐟"
    DishCategory.BREAKFAST -> "🍳"
    DishCategory.STAPLE -> "🍚"
    DishCategory.SEMI_FINISHED -> "🍱"
    DishCategory.SOUP -> "🥣"
    DishCategory.DRINK -> "🍹"
    DishCategory.CONDIMENT -> "🧂"
    DishCategory.DESSERT -> "🍰"
    DishCategory.UNKNOWN -> "❓"
  }

val DishCategory.displayName: String?
  @Composable
  get() = when (this) {
    DishCategory.VEGETABLE_DISH -> Res.string.main_dish_category_vegetable_dish
    DishCategory.MEAT_DISH -> Res.string.main_dish_category_meat_dish
    DishCategory.AQUATIC -> Res.string.main_dish_category_aquatic
    DishCategory.BREAKFAST -> Res.string.main_dish_category_breakfast
    DishCategory.STAPLE -> Res.string.main_dish_category_staple
    DishCategory.SEMI_FINISHED -> Res.string.main_dish_category_semi_finished
    DishCategory.SOUP -> Res.string.main_dish_category_soup
    DishCategory.DRINK -> Res.string.main_dish_category_drink
    DishCategory.CONDIMENT -> Res.string.main_dish_category_condiment
    DishCategory.DESSERT -> Res.string.main_dish_category_dessert
    DishCategory.UNKNOWN -> null
  }?.let { stringResource(it) }
