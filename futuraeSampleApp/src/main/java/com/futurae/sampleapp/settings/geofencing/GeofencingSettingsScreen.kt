package com.futurae.sampleapp.settings.geofencing

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.settings.common.SettingsRowComposable
import com.futurae.sampleapp.ui.theme.OnPrimaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GeofencingSettingsScreen() {
    val context = LocalContext.current
    val geofencingSettingsViewModel: GeofencingSettingsViewModel = viewModel()

    val finePermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarsePermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)
    val backgroundPermission = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val state by geofencingSettingsViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnPrimaryColor)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state) { item ->
                SettingsRowComposable(item)
            }
        }
    }


    LaunchedEffect(finePermission.status, coarsePermission.status) {
        when {
            finePermission.status.isGranted || coarsePermission.status.isGranted -> {
                // do nothing
            }

            finePermission.status.shouldShowRationale -> {
                coarsePermission.launchPermissionRequest()
            }

            !finePermission.status.isGranted && coarsePermission.status.isGranted -> {
                finePermission.launchPermissionRequest()
            }
        }
    }

    // Background location can only be requested once foreground (fine/coarse) access is granted.
    LaunchedEffect(finePermission.status, coarsePermission.status, backgroundPermission.status) {
        val hasForegroundPermission = finePermission.status.isGranted || coarsePermission.status.isGranted
        if (hasForegroundPermission && !backgroundPermission.status.isGranted) {
            backgroundPermission.launchPermissionRequest()
        }
    }
}

