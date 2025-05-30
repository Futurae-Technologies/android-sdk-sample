package com.futurae.sampleapp.home.qrscanner

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.sampleapp.R
import com.futurae.sampleapp.arch.AuthRequestData
import com.futurae.sampleapp.enrollment.EnrollmentCase
import com.futurae.sampleapp.home.qrscanner.arch.QRScannerViewModel
import com.futurae.sampleapp.ui.TextWrapper
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialog
import com.futurae.sampleapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onInvalidQRCode: () -> Unit,
    onEnrollmentRequest: (EnrollmentCase.QRCodeScan) -> Unit,
    onAuthRequest: (AuthRequestData) -> Unit
) {
    val qrScannerViewModel: QRScannerViewModel = viewModel(
        factory = QRScannerViewModel.provideFactory()
    )

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showGoToSettingsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CameraPreview { qrCode ->
            qrScannerViewModel.handleUserInteraction(
                userInteraction = QRScannerScreenUserInteraction.OnQRCodeScanned(qrCode)
            )
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

        val isPermissionPermanentlyDenied = !cameraPermissionState.status.shouldShowRationale &&
                !cameraPermissionState.status.isGranted
        if (isPermissionPermanentlyDenied) {
            showGoToSettingsDialog = true
        }

        qrScannerViewModel.onAuthRequest
            .onEach { onAuthRequest(it) }
            .launchIn(this)

        qrScannerViewModel.onEnrollmentFlowRequest
            .onEach { onEnrollmentRequest(it) }
            .launchIn(this)

        qrScannerViewModel.onFailure
            .onEach { onInvalidQRCode() }
            .launchIn(this)
    }
}

@Composable
fun CameraPreview(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val debounceDelay = 300L
    val scope = rememberCoroutineScope()
    var lastScanTime by remember { mutableLongStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalyzer = ImageAnalysis.Builder().build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(ctx), QRCodeAnalyzer { qrCode ->
                        val currentTime = System.currentTimeMillis()

                        if (currentTime - lastScanTime > debounceDelay) {
                            lastScanTime = currentTime

                            scope.launch {
                                delay(debounceDelay)
                                onQRCodeScanned(qrCode)
                            }
                        }
                    })
                }
                cameraProvider.apply {
                    unbindAll()
                    bindToLifecycle(
                        ctx as LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Image(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(R.drawable.graphic_qr_frame),
            contentDescription = "QR frame"
        )
    }
}

class QRCodeAnalyzer(
    private val onQRCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner
            .process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()
                    ?.rawValue
                    ?.let { qrCode ->
                        onQRCodeScanned(qrCode)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("QRCodeAnalyzer", "QR code scanning failed", exception)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
