package com.futurae.sampleapp.configuration

import androidx.compose.runtime.Composable
import com.futurae.sampleapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState

@Composable
fun InitialConfigurationScreenRoute(
    onConfigurationComplete: () -> Unit,
    navigateToRecovery: () -> Unit,
    showSnackbar : suspend (FuturaeSnackbarUIState) -> Unit,
) {
    ConfigurationScreenRoute(
        onConfigurationComplete = onConfigurationComplete,
        navigateToRecovery = navigateToRecovery,
        isConfigurationChange = false,
        pinProviderViewModel = null,
        onPinRequested = { },
        showSnackbar = showSnackbar
    )
}