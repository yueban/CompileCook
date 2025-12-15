package com.yueban.compilecook.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun DetailContent(component: DetailComponent, modifier: Modifier = Modifier) {
  val model by component.model.subscribeAsState()

  Column(modifier) {
    IconButton(onClick = { component.onBackClicked() }) {
      Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
    }
    Text(text = model.item)
  }
}
