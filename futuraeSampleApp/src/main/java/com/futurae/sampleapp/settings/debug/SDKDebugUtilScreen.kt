package com.futurae.sampleapp.settings.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.FuturaeSampleApplication
import com.futurae.sampleapp.settings.common.SettingsRowComposable
import com.futurae.sampleapp.settings.debug.SDKDebugUtilViewModel.Companion.provideFactory
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SDKDebugUtilScreen(
    showSnackbar: suspend (FuturaeSnackbarUIState) -> Unit,
    activateBiometricsRequested: () -> Unit,
    navigateTo: (route: String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val application = context.applicationContext as FuturaeSampleApplication
    val settingsViewModel: SDKDebugUtilViewModel = viewModel(
        factory = provideFactory(
            application = application
        )
    )

    val items by settingsViewModel.debugUtilItems.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        items(items) { item ->
            SettingsRowComposable(item)
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.navigationEvent
            .onEach { navigateTo(it) }
            .launchIn(this)

        settingsViewModel.snackbarUIState
            .onEach { showSnackbar(it) }
            .launchIn(this)

        settingsViewModel.activateBiometricsRequest
            .onEach { activateBiometricsRequested() }
            .launchIn(this)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.refreshItems()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}