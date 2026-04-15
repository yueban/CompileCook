package com.yueban.compilecook.ui.root

import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.dish.DishListSource
import com.yueban.compilecook.ui.main.MainComponent.MainTab
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.About
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Dish
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.DishList
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Main
import com.yueban.compilecook.ui.root.DefaultRootComponent.Config.Tip
import io.ktor.http.Url

object PathMapper {
  fun configToPath(config: Config): String? =
    when (config) {
      is Main -> "/"
      About -> "/about"
      is Tip -> "/tips/${config.tipName}"
      is DishList -> when (val source = config.source) {
        DishListSource.All -> "/dishes"
        DishListSource.Search -> "/dishes?search=true"
        DishListSource.Favorite -> "/dishes/favorite"
        is DishListSource.Category -> "/dishes/category/${source.category.name.lowercase()}"
        is DishListSource.Difficulty -> "/dishes/difficulty/${source.level}"
      }
      is Dish -> "/dishes/${config.dishName}"
    }

  fun pathToStack(deepLinkUrl: String?): List<Config> {
    val url = deepLinkUrl?.let { Url(it) } ?: return listOf(Main(MainTab.TIPS))
    val segments = url.segments.filter { it.isNotEmpty() }
    val first = segments.firstOrNull()

    return when (first) {
      "about" -> listOf(Main(MainTab.TIPS), About)

      "tips" -> {
        val tipName = segments.getOrNull(1)
        if (tipName != null) {
          listOf(Main(MainTab.TIPS), Tip(tipName))
        } else {
          listOf(Main(MainTab.TIPS))
        }
      }

      "dishes" -> {
        val second = segments.getOrNull(1)
        val mainDishes = Main(MainTab.DISHES)

        when (second) {
          "favorite" -> listOf(mainDishes, DishList(DishListSource.Favorite))

          "category" -> {
            val catName = segments.getOrNull(2)
            val category = DishCategory.entries.find { it.name.lowercase() == catName }
            val source = category?.let { DishListSource.Category(it) } ?: DishListSource.All
            listOf(mainDishes, DishList(source))
          }

          "difficulty" -> {
            val level = segments.getOrNull(2)?.toIntOrNull() ?: 1
            listOf(mainDishes, DishList(DishListSource.Difficulty(level)))
          }

          null -> {
            // Check for ?search=true
            val source = if (url.parameters["search"] == "true") DishListSource.Search else DishListSource.All
            listOf(mainDishes, DishList(source))
          }

          else -> {
            // It's a specific dish name: /dishes/mapodoufu
            listOf(mainDishes, DishList(DishListSource.All), Dish(second))
          }
        }
      }

      else -> listOf(Main(MainTab.TIPS))
    }
  }
}
