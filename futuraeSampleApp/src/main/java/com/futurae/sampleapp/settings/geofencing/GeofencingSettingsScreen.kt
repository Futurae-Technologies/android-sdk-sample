package com.futurae.sampleapp.settings.geofencing

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.R
import com.futurae.sampleapp.settings.SettingsListItem
import com.futurae.sampleapp.settings.SettingsToggle
import com.futurae.sampleapp.settings.common.SettingsRowComposable
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.utils.UITestTags
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

private enum class PendingGeofencingEnable { AUTH, CONTINUOUS }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeofencingSettingsScreen() {
    val context = LocalContext.current
    val vm: GeofencingSettingsViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var pendingEnable by remember { mutableStateOf<PendingGeofencingEnable?>(null) }
    var showBgPermissionDeniedDialog by remember { mutableStateOf(false) }
    // Flag set from foreground onPermissionsResult to trigger background permission in LaunchedEffect
    var shouldLaunchBgPermission by remember { mutableStateOf(false) }
    // Flag set from background onPermissionResult to check permanent denial in LaunchedEffect
    var bgPermissionJustDenied by remember { mutableStateOf(false) }

    // Declared before foregroundPermissions so its status can be read from that callback
    val bgPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            onPermissionResult = { granted ->
                if (pendingEnable == PendingGeofencingEnable.CONTINUOUS) {
                    if (granted) {
                        vm.enableContinuousGeofencing()
                    } else {
                        // Accompanist refreshes status before invoking this callback, so
                        // shouldShowRationale is already up to date. We use a flag to read it
                        // after recomposition in LaunchedEffect (avoids self-reference on bgPermission val).
                        bgPermissionJustDenied = true
                    }
                    pendingEnable = null
                }
            }
        )
    } else {
        null
    }

    val foregroundPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
        onPermissionsResult = { results ->
            val granted = results.values.any { it }
            when {
                !granted -> pendingEnable = null
                pendingEnable == PendingGeofencingEnable.AUTH -> {
                    vm.enableAuthGeofencing()
                    pendingEnable = null
                }
                pendingEnable == PendingGeofencingEnable.CONTINUOUS -> {
                    val bgAlreadyGranted = bgPermission?.status?.isGranted ?: true
                    if (bgAlreadyGranted) {
                        vm.enableContinuousGeofencing()
                        pendingEnable = null
                    } else {
                        // Defer launching bg permission request to a safe side-effect
                        shouldLaunchBgPermission = true
                    }
                }
            }
        }
    )

    // Launch bg permission from a coroutine context so we're not inside a result callback
    LaunchedEffect(shouldLaunchBgPermission) {
        if (shouldLaunchBgPermission) {
            shouldLaunchBgPermission = false
            bgPermission?.launchPermissionRequest()
        }
    }

    // Detect permanent background location denial after a request was made
    LaunchedEffect(bgPermissionJustDenied) {
        if (bgPermissionJustDenied) {
            bgPermissionJustDenied = false
            val status = bgPermission?.status
            if (status != null && !status.isGranted && !status.shouldShowRationale) {
                showBgPermissionDeniedDialog = true
            }
            // shouldShowRationale == true means first denial; user can retry by toggling again
        }
    }

    val foregroundGranted = foregroundPermissions.permissions.any { it.status.isGranted }

    fun onAuthToggleOn() {
        if (foregroundGranted) {
            vm.enableAuthGeofencing()
        } else {
            pendingEnable = PendingGeofencingEnable.AUTH
            foregroundPermissions.launchMultiplePermissionRequest()
        }
    }

    fun onContinuousToggleOn() {
        val bgGranted = bgPermission?.status?.isGranted ?: true // null = Android < Q, treated as granted
        when {
            bgGranted -> vm.enableContinuousGeofencing()
            foregroundGranted -> {
                // Foreground already granted; request background directly
                pendingEnable = PendingGeofencingEnable.CONTINUOUS
                bgPermission?.launchPermissionRequest()
            }
            else -> {
                // Neither foreground nor background granted; start from foreground
                pendingEnable = PendingGeofencingEnable.CONTINUOUS
                foregroundPermissions.launchMultiplePermissionRequest()
            }
        }
    }

    val items: List<SettingsListItem> = listOf(
        SettingsToggle(
            title = TextWrapper.Resource(R.string.geofencing_on_auth),
            subtitle = TextWrapper.Resource(R.string.geofencing_on_auth_subtitle),
            isEnabled = state.isAuthGeofencingEnabled,
            testTag = UITestTags.ToggleGeofencingOnAuth.tag,
            onToggleChanged = { enabled ->
                if (enabled) onAuthToggleOn() else vm.disableAuthGeofencing()
            }
        ),
        SettingsToggle(
            title = TextWrapper.Resource(R.string.geofencing_continuous),
            subtitle = TextWrapper.Resource(R.string.geofencing_continuous_subtitle),
            isEnabled = state.isContinuousGeofencingEnabled,
            testTag = UITestTags.ToggleContinuousGeofencing.tag,
            onToggleChanged = { enabled ->
                if (enabled) onContinuousToggleOn() else vm.disableContinuousGeofencing()
            }
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                SettingsRowComposable(item)
            }
        }
    }

    if (showBgPermissionDeniedDialog) {
        AlertDialog(
            containerColor = OnPrimaryColor,
            onDismissRequest = { showBgPermissionDeniedDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.geofencing_bg_permission_denied_title),
                    style = FuturaeTypography.titleH4,
                    color = PrimaryColor,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.geofencing_bg_permission_denied_message),
                    style = FuturaeTypography.bodySmallRegular,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBgPermissionDeniedDialog = false
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.go_to_settings),
                        style = FuturaeTypography.button,
                        color = PrimaryColor,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showBgPermissionDeniedDialog = false }) {
                    Text(
                        text = stringResource(R.string.dismiss),
                        style = FuturaeTypography.button,
                        color = PrimaryColor,
                    )
                }
            }
        )
    }
}
