package com.yueban.compilecook.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.yueban.compilecook.ui.inbox.ListComponent.Event.BackFromDetail

@Composable
fun ListContent(component: ListComponent, modifier: Modifier = Modifier) {
  val model by component.model.subscribeAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(component) {
    component.eventFlow.collect { event ->
      when (event) {
        is BackFromDetail -> snackbarHostState.showSnackbar("Back From Detail: ${event.dishName}")
      }
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) {
    LazyColumn(modifier.fillMaxSize().background(Color.Red)) {
      item {
        Text(
          text = "Counter: ${model.counter}",
          modifier = Modifier.clickable { component.onAddCount() }
        )
      }
      items(items = model.dishes) {
        Text(
          text = it.name,
          modifier = Modifier.clickable { component.onItemClicked(dishName = it.name) },
        )
      }
    }
  }
}
