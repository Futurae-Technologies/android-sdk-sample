package com.futurae.sampleapp.lock.arch

import com.futurae.sampleapp.ui.TextWrapper
import kotlinx.serialization.Serializable

sealed class LockScreenUIState {
    abstract val title: TextWrapper
    abstract val error: TextWrapper?
    abstract val supportAlternativeAuthText: TextWrapper?

    data class PinScreen(
        val digitsEntered: Array<Int>,
        override val supportAlternativeAuthText: TextWrapper?,
        override val title: TextWrapper,
        override val error: TextWrapper? = null,
    ) : LockScreenUIState() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as PinScreen

            if (!digitsEntered.contentEquals(other.digitsEntered)) return false
            if (supportAlternativeAuthText != other.supportAlternativeAuthText) return false
            if (title != other.title) return false
            if (error != other.error) return false

            return true
        }

        override fun hashCode(): Int {
            var result = digitsEntered.contentHashCode()
            result = 31 * result + supportAlternativeAuthText.hashCode()
            result = 31 * result + title.hashCode()
            result = 31 * result + (error?.hashCode() ?: 0)
            return result
        }
    }

    data class BioCredsScreen(
        override val supportAlternativeAuthText: TextWrapper? = null,
        override val title: TextWrapper,
        override val error: TextWrapper? = null,
    ) : LockScreenUIState()
}


enum class LockScreenMode {
    UNLOCK,
    GET_PIN,
    CREATE_PIN,
    CHANGE_PIN,
    ACTIVATE_BIO
}

enum class UnlockRequired {
    BIOMETRICS,
    BIOMETRICS_OR_CREDS
}

@Serializable
data class LockScreenConfiguration(
    val maxDigitsAllowed : Int,
    val lockMode: LockScreenMode
)