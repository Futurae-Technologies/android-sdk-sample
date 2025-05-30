package com.futurae.sampleapp.enrollment

import com.futurae.sdk.public_api.account.model.ActivationCode
import com.futurae.sdk.public_api.account.model.EnrollmentInput
import com.futurae.sdk.public_api.account.model.ShortActivationCode
import com.futurae.sdk.public_api.account.model.URI
import kotlinx.serialization.Serializable

@Serializable
sealed class EnrollmentCase {
    abstract fun toEnrollmentInput(): EnrollmentInput

    @Serializable
    data class QRCodeScan(val code: String) : EnrollmentCase() {

        override fun toEnrollmentInput(): EnrollmentInput = ActivationCode(code)
    }

    @Serializable
    data class ManualEntry(val shortCode: String) : EnrollmentCase() {

        override fun toEnrollmentInput(): EnrollmentInput = ShortActivationCode(shortCode)
    }

    @Serializable
    data class URIHandler(val uri: String) : EnrollmentCase() {

        override fun toEnrollmentInput(): EnrollmentInput = URI(uri)
    }
}