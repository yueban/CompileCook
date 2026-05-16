package com.yueban.compilecook.ui.util

import androidx.compose.runtime.Composable
import com.yueban.compilecook.repo.entity.AiContext
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ai_chat_context_general
import compilecook.composeapp.generated.resources.dish_list_difficulty_title_format
import compilecook.composeapp.generated.resources.dish_list_title
import org.jetbrains.compose.resources.stringResource

val AiContext.displayName: String
  @Composable
  get() = when (this) {
    is AiContext.Tip -> name
    is AiContext.Dish -> name
    is AiContext.DishCategory -> category.displayName ?: stringResource(Res.string.dish_list_title)
    is AiContext.DishDifficulty -> stringResource(Res.string.dish_list_difficulty_title_format, level)
    AiContext.DishList -> stringResource(Res.string.dish_list_title)
    AiContext.General -> stringResource(Res.string.ai_chat_context_general)
  }
