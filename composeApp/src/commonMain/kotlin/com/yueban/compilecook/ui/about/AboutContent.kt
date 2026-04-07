package com.yueban.compilecook.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.yueban.compilecook.BuildKonfig
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewData
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.widget.TitleTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.about_des_app_icon
import compilecook.composeapp.generated.resources.about_title
import compilecook.composeapp.generated.resources.app_icon
import compilecook.composeapp.generated.resources.app_name
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutContent(component: AboutComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TitleTopBar(
        title = stringResource(Res.string.about_title),
        enableBack = true,
        onBackClick = component::onBackClicked
      )
    }
  ) { padding ->
    LibrariesContainer(
      libraries = state.aboutLibs.value,
      modifier = Modifier.fillMaxSize().padding(padding),
      header = { item { AboutHeader() } }
    )
  }
}

@Composable
private fun AboutHeader() {
  Column(
    modifier = Modifier.fillMaxWidth().padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(
      painter = painterResource(Res.drawable.app_icon),
      contentDescription = stringResource(Res.string.about_des_app_icon),
      modifier = Modifier.size(100.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = stringResource(Res.string.app_name),
      style = AppTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colors.titleText
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = BuildKonfig.APP_VERSION,
      style = AppTheme.typography.bodyMedium,
      color = AppTheme.colors.subTitleText
    )
    Spacer(modifier = Modifier.height(32.dp))
  }
}

private class PreviewAboutComponent : AboutComponent {
  override val uiState = MutableStateFlow(PreviewData.aboutState)
  override fun onBackClicked() = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewAboutContent() = PreviewWrapper {
  AboutContent(component = PreviewAboutComponent())
}
