package com.futurae.demoapp.configuration

import androidx.compose.runtime.Composable
import com.futurae.demoapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState

@Composable
fun InitialConfigurationScreenRoute(
    onConfigurationComplete: () -> Unit,
    showSnackbar : suspend (FuturaeSnackbarUIState) -> Unit,
) {
    ConfigurationScreenRoute(
        onConfigurationComplete = onConfigurationComplete,
        isConfigurationChange = false,
        pinProviderViewModel = null,
        onPinRequested = { },
        showSnackbar = showSnackbar
    )
}