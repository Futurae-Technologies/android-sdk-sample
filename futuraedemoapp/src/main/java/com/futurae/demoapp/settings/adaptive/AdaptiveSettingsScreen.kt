package com.futurae.demoapp.settings.adaptive

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.settings.common.SettingsRowComposable
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButton
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.OnSecondaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdaptiveSettingsScreen(
    navigateTo: (route: String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeDemoApplication
    val adaptiveSettingsViewModel: AdaptiveSettingsViewModel = viewModel(
        factory = AdaptiveSettingsViewModel.provideFactory(application = application)
    )

    val adaptivePermissionsState =
        rememberMultiplePermissionsState(permissions = getAdaptivePermissions())

    val state by adaptiveSettingsViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.items) { item ->
                SettingsRowComposable(item)
            }
        }

        AnimatedVisibility(
            visible = state.actions.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight()
                    .padding(bottom = 24.dp)
            ) {
                state.actions.forEach {
                    ActionButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = it.cta,
                        onClick = it.onClick
                    )
                }
            }
        }
    }

    if (state.thresholdUIState.isVisible) {
        AdaptiveThresholdDialog(
            value = state.thresholdUIState.value,
            onDismiss = adaptiveSettingsViewModel::onDismissThresholdDialog,
            onThresholdUpdate = adaptiveSettingsViewModel::onThresholdUpdate
        )
    }

    LaunchedEffect(Unit) {
        adaptiveSettingsViewModel.permissionsNeededFlow
            .onEach {
                if (!adaptivePermissionsState.allPermissionsGranted) {
                    adaptivePermissionsState.launchMultiplePermissionRequest()
                }
            }
            .launchIn(this)

        adaptiveSettingsViewModel.navigationEvent
            .onEach { navigateTo(it) }
            .launchIn(this)
    }
}

@Composable
fun AdaptiveThresholdDialog(value: Int, onDismiss: () -> Unit, onThresholdUpdate: (Int) -> Unit) {
    var currentValue by remember { mutableFloatStateOf(value.toFloat()) }

    AlertDialog(
        containerColor = OnPrimaryColor,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.adaptive_threshold_dialog_title),
                style = FuturaeTypography.titleH4,
                color = PrimaryColor
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.adaptive_threshold_dialog_description),
                    style = FuturaeTypography.bodySmallRegular,
                    color = OnSecondaryColor
                )
                Slider(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    valueRange = 1f..60f,
                    steps = 58 // (60 - 1) - 1
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.adaptive_threshold, currentValue.roundToInt()),
                    style = FuturaeTypography.titleH4,
                    color = PrimaryColor
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onThresholdUpdate(currentValue.roundToInt())
                }
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    style = FuturaeTypography.button,
                    color = PrimaryColor
                )
            }
        }
    )
}

private fun getAdaptivePermissions(): List<String> {
    val permissions = mutableListOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    return permissions
}
