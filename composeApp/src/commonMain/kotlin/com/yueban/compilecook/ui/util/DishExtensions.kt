package com.yueban.compilecook.ui.util

import androidx.compose.runtime.Composable
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.DishCategory.AQUATIC
import com.yueban.compilecook.repo.entity.DishCategory.BREAKFAST
import com.yueban.compilecook.repo.entity.DishCategory.CONDIMENT
import com.yueban.compilecook.repo.entity.DishCategory.DESSERT
import com.yueban.compilecook.repo.entity.DishCategory.DRINK
import com.yueban.compilecook.repo.entity.DishCategory.MEAT_DISH
import com.yueban.compilecook.repo.entity.DishCategory.SEMI_FINISHED
import com.yueban.compilecook.repo.entity.DishCategory.SOUP
import com.yueban.compilecook.repo.entity.DishCategory.STAPLE
import com.yueban.compilecook.repo.entity.DishCategory.UNKNOWN
import com.yueban.compilecook.repo.entity.DishCategory.VEGETABLE_DISH
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ic_category_aquatic
import compilecook.composeapp.generated.resources.ic_category_aquatic_monochrome
import compilecook.composeapp.generated.resources.ic_category_breakfast
import compilecook.composeapp.generated.resources.ic_category_breakfast_monochrome
import compilecook.composeapp.generated.resources.ic_category_condiment
import compilecook.composeapp.generated.resources.ic_category_condiment_monochrome
import compilecook.composeapp.generated.resources.ic_category_dessert
import compilecook.composeapp.generated.resources.ic_category_dessert_monochrome
import compilecook.composeapp.generated.resources.ic_category_drink
import compilecook.composeapp.generated.resources.ic_category_drink_monochrome
import compilecook.composeapp.generated.resources.ic_category_meat_dish
import compilecook.composeapp.generated.resources.ic_category_meat_dish_monochrome
import compilecook.composeapp.generated.resources.ic_category_semi_finished
import compilecook.composeapp.generated.resources.ic_category_semi_finished_monochrome
import compilecook.composeapp.generated.resources.ic_category_soup
import compilecook.composeapp.generated.resources.ic_category_soup_monochrome
import compilecook.composeapp.generated.resources.ic_category_staple
import compilecook.composeapp.generated.resources.ic_category_staple_monochrome
import compilecook.composeapp.generated.resources.ic_category_unknown
import compilecook.composeapp.generated.resources.ic_category_unknown_monochrome
import compilecook.composeapp.generated.resources.ic_category_vegetable_dish
import compilecook.composeapp.generated.resources.ic_category_vegetable_dish_monochrome
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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

val DishCategory.icon: DrawableResource
  get() = when (this) {
    VEGETABLE_DISH -> Res.drawable.ic_category_vegetable_dish
    MEAT_DISH -> Res.drawable.ic_category_meat_dish
    AQUATIC -> Res.drawable.ic_category_aquatic
    BREAKFAST -> Res.drawable.ic_category_breakfast
    STAPLE -> Res.drawable.ic_category_staple
    SEMI_FINISHED -> Res.drawable.ic_category_semi_finished
    SOUP -> Res.drawable.ic_category_soup
    DRINK -> Res.drawable.ic_category_drink
    CONDIMENT -> Res.drawable.ic_category_condiment
    DESSERT -> Res.drawable.ic_category_dessert
    UNKNOWN -> Res.drawable.ic_category_unknown
  }

val DishCategory.monochromeIcon: DrawableResource
  get() = when (this) {
    VEGETABLE_DISH -> Res.drawable.ic_category_vegetable_dish_monochrome
    MEAT_DISH -> Res.drawable.ic_category_meat_dish_monochrome
    AQUATIC -> Res.drawable.ic_category_aquatic_monochrome
    BREAKFAST -> Res.drawable.ic_category_breakfast_monochrome
    STAPLE -> Res.drawable.ic_category_staple_monochrome
    SEMI_FINISHED -> Res.drawable.ic_category_semi_finished_monochrome
    SOUP -> Res.drawable.ic_category_soup_monochrome
    DRINK -> Res.drawable.ic_category_drink_monochrome
    CONDIMENT -> Res.drawable.ic_category_condiment_monochrome
    DESSERT -> Res.drawable.ic_category_dessert_monochrome
    UNKNOWN -> Res.drawable.ic_category_unknown_monochrome
  }

val DishCategory.displayName: String?
  @Composable
  get() = when (this) {
    VEGETABLE_DISH -> Res.string.main_dish_category_vegetable_dish
    MEAT_DISH -> Res.string.main_dish_category_meat_dish
    AQUATIC -> Res.string.main_dish_category_aquatic
    BREAKFAST -> Res.string.main_dish_category_breakfast
    STAPLE -> Res.string.main_dish_category_staple
    SEMI_FINISHED -> Res.string.main_dish_category_semi_finished
    SOUP -> Res.string.main_dish_category_soup
    DRINK -> Res.string.main_dish_category_drink
    CONDIMENT -> Res.string.main_dish_category_condiment
    DESSERT -> Res.string.main_dish_category_dessert
    UNKNOWN -> null
  }?.let { stringResource(it) }
