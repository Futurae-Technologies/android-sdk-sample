package com.futurae.demoapp.home.qrscanner

sealed interface QRScannerScreenUserInteraction {

    data class OnQRCodeScanned(val code: String): QRScannerScreenUserInteraction
}