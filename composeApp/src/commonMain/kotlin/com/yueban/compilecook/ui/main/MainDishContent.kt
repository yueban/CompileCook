package com.yueban.compilecook.ui.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainDishContent(component: MainDishComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()
  Text(state.title)
}
