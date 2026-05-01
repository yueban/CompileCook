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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.yueban.compilecook.BuildKonfig
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
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
        onBackClick = component::onBackClicked,
        onAiClick = component::onAiClicked,
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
    modifier = Modifier.fillMaxWidth().padding(AppTheme.dimens.largeGap),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Image(
      painter = painterResource(Res.drawable.app_icon),
      contentDescription = stringResource(Res.string.about_des_app_icon),
      modifier = Modifier.size(AppTheme.dimens.aboutIconSize)
    )
    Spacer(modifier = Modifier.height(AppTheme.dimens.screenPadding))
    Text(
      text = stringResource(Res.string.app_name),
      style = AppTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colors.titleText
    )
    Spacer(modifier = Modifier.height(AppTheme.dimens.tinyGap))
    Text(
      text = BuildKonfig.APP_VERSION,
      style = AppTheme.typography.bodyMedium,
      color = AppTheme.colors.subTitleText
    )
    Spacer(modifier = Modifier.height(AppTheme.dimens.extraLargeGap))
  }
}

private class PreviewAboutComponent : AboutComponent {
  override val uiState = MutableStateFlow(PreviewData.aboutState)
  override fun onBackClicked() = Unit
  override fun onAiClicked() = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewAboutContent() = PreviewWrapper {
  AboutContent(component = PreviewAboutComponent())
}
