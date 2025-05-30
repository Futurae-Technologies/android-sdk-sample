package com.futurae.sampleapp.home.qrscanner

sealed interface QRScannerScreenUserInteraction {

    data class OnQRCodeScanned(val code: String): QRScannerScreenUserInteraction
}