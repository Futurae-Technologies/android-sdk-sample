package com.futurae.sampleapp.settings.sdksettings

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
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.arch.PinProviderViewModel
import com.futurae.sampleapp.settings.common.SettingsRowComposable
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sampleapp.ui.theme.Tertiary
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SettingsScreen(
    navigateTo: (route: String) -> Unit,
    onPinRequested: () -> Unit,
    pinProviderViewModel: PinProviderViewModel,
    showSnackbar: suspend (FuturaeSnackbarUIState) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeSampleApplication
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
        settingsViewModel.navigationEvent
            .onEach { navigateTo(it) }
            .launchIn(this)

        settingsViewModel.requestSDKPinChange
            .onEach { onPinRequested() }
            .launchIn(this)

        pinProviderViewModel
            .pinFlow
            .onEach {
                settingsViewModel.onPinProvided(it)
                pinProviderViewModel.reset()
            }
            .launchIn(this)

        settingsViewModel
            .notifyUser
            .onEach {
                showSnackbar(it)
            }
            .launchIn(this)
    }
}