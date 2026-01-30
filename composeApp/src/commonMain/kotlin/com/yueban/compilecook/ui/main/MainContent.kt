package com.yueban.compilecook.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.yueban.compilecook.ui.main.MainComponent.Child.Dishes
import com.yueban.compilecook.ui.main.MainComponent.Child.Tips
import com.yueban.compilecook.ui.widget.CommonTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.app_name
import compilecook.composeapp.generated.resources.main_tab_dishes
import compilecook.composeapp.generated.resources.main_tab_tips
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainContent(component: MainComponent) {
  val stack by component.stack.subscribeAsState()
  val activeChild = stack.active.instance

  Scaffold(
    topBar = { CommonTopBar(title = stringResource(Res.string.app_name)) },
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = activeChild is Tips,
          onClick = { component.onTabSelected(MainComponent.MainTab.TIPS) },
          icon = { Icon(Icons.Default.Info, stringResource(Res.string.main_tab_tips)) },
          label = { Text(stringResource(Res.string.main_tab_tips)) }
        )
        NavigationBarItem(
          selected = activeChild is Dishes,
          onClick = { component.onTabSelected(MainComponent.MainTab.DISHES) },
          icon = { Icon(Icons.Default.Fastfood, stringResource(Res.string.main_tab_dishes)) },
          label = { Text(stringResource(Res.string.main_tab_dishes)) }
        )
      }
    }
  ) { padding ->
    Children(
      stack = component.stack,
      modifier = Modifier.padding(padding),
      animation = stackAnimation(fade())
    ) {
      when (val child = it.instance) {
        is Dishes -> MainDishContent(child.component)
        is Tips -> MainTipContent(child.component)
      }
    }
  }
}
