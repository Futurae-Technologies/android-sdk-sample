package com.futurae.demoapp.settings.sdksettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.settings.common.SettingsRowComposable
import com.futurae.demoapp.ui.theme.Tertiary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navigateTo: (route: String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeDemoApplication
    val settingsViewModel: SDKSettingsViewModel = viewModel(
        factory = SDKSettingsViewModel.provideFactory(application = application)
    )

    val items by settingsViewModel.settingsItems.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary)
    ) {
        LazyColumn {
            items(items) { item ->
                SettingsRowComposable(item)
            }
        }
    }

    LaunchedEffect(Unit) {
        launch {
            settingsViewModel.navigationEvent.collect {
                navigateTo(it)
            }
        }

    }
}