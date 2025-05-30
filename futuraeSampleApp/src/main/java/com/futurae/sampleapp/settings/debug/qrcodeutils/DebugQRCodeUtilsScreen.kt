package com.futurae.sampleapp.settings.debug.qrcodeutils

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.R
import com.futurae.sampleapp.home.qrscanner.CameraPreview
import com.futurae.sampleapp.settings.debug.DebugResultComparison
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialog
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.sampleapp.ui.theme.FuturaeTypography
import com.futurae.sampleapp.ui.theme.PrimaryColor
import com.futurae.sampleapp.ui.theme.SecondaryColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

// Please note this is a debug screen, solely for testing purposes.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DebugQRCodeUtilsScreen() {
    val debugQRCodeUtilsViewModel: DebugQRCodeUtilsViewModel = viewModel(
        factory = DebugQRCodeUtilsViewModel.provideFactory()
    )

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var showGoToSettingsDialog by remember { mutableStateOf(false) }
    val uiState by debugQRCodeUtilsViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is DebugQRCodeUtilsViewModel.DebugQRCodeUtilsUIState.ScanResult -> {
                QRScannedDebugResultComparison(uiState as DebugQRCodeUtilsViewModel.DebugQRCodeUtilsUIState.ScanResult) {
                    debugQRCodeUtilsViewModel.dismissResult()
                }
            }
            DebugQRCodeUtilsViewModel.DebugQRCodeUtilsUIState.Scanning -> {
                CameraPreview { qrCode ->
                    debugQRCodeUtilsViewModel.onQRCodeScanned(qrCode)
                }
            }
        }

        if (showGoToSettingsDialog) {
            FuturaeAlertDialog(
                uiState = FuturaeAlertDialogUIState(
                    title = TextWrapper.Resource(R.string.permission_request_dialog_title),
                    text = TextWrapper.Resource(R.string.permission_request_dialog_message),
                    confirmButtonCta = TextWrapper.Resource(R.string.ok)
                ),
                onConfirm = {
                    showGoToSettingsDialog = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }

        val isPermissionPermanentlyDenied = !cameraPermissionState.status.shouldShowRationale
                && !cameraPermissionState.status.isGranted

        if (isPermissionPermanentlyDenied) {
            showGoToSettingsDialog = true
        }
    }
}

@Composable
private fun QRScannedDebugResultComparison(
    uiState: DebugQRCodeUtilsViewModel.DebugQRCodeUtilsUIState.ScanResult,
    onDismiss: () -> Unit
) {
    DebugResultComparison(
        isSuccess = uiState.isSameResultInBothImpl,
        firstResult = {
            ResultSection(
                version = "Legacy Impl:",
                qrCodeType = uiState.qrCodeTypeLegacyImpl,
                userId = uiState.userIdLegacyImpl,
                sessionToken = uiState.sessionTokenLegacyImpl
            )
        },
        secondResult = {
            ResultSection(
                version = "New Impl:",
                qrCodeType = uiState.qrCodeType,
                userId = uiState.userId,
                sessionToken = uiState.sessionToken
            )
        },
        onDismiss = onDismiss
    )
}

@Composable
fun ResultSection(
    version: String,
    @StringRes qrCodeType: Int,
    userId: String?,
    sessionToken: String?
) {
    Text(
        text = version,
        style = FuturaeTypography.titleH4,
        color = PrimaryColor
    )

    Text(
        text = stringResource(qrCodeType),
        style = FuturaeTypography.titleH4,
        color = PrimaryColor
    )

    userId?.let {
        Text(
            text = stringResource(
                R.string.debug_qr_code_user_id,
                it
            ),
            style = FuturaeTypography.bodyLarge,
            color = SecondaryColor
        )
    }

    sessionToken?.let {
        Text(
            text = stringResource(
                R.string.debug_qr_code_session_token,
                it
            ),
            style = FuturaeTypography.bodyLarge,
            color = SecondaryColor
        )
    }
}