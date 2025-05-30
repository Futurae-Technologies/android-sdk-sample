package com.futurae.sampleapp.settings.debug.uriutils

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.sampleapp.R
import com.futurae.sdk.public_api.uri.model.FTRUriType
import com.futurae.sdk.utils.FTUriUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DebugURIUtilsViewModel: ViewModel() {

    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = DebugURIUtilsViewModel() as T
        }
    }

    private val _uiState = MutableStateFlow<DebugURIUtilsUIState>(DebugURIUtilsUIState.Input(""))
    val uiState = _uiState.asStateFlow()

    fun onURIChange(uri: String) {
        viewModelScope.launch {
            _uiState.update {
                DebugURIUtilsUIState.Input(uri)
            }
        }
    }

    fun onURISubmit() {
        val uri = (_uiState.value as? DebugURIUtilsUIState.Input)?.uri ?: return

        val oldImplementationResult = uri.handleUsingLegacyImplementation()
        val newImplementationResult = uri.handle()
        val isSameResult = oldImplementationResult.type == newImplementationResult.type &&
                oldImplementationResult.extractedInfo == newImplementationResult.extractedInfo

        viewModelScope.launch {
            _uiState.update {
                DebugURIUtilsUIState.URIResult(
                    uriTypeLegacyImpl = oldImplementationResult.type.label,
                    extractedInfoLegacyImpl = oldImplementationResult.extractedInfo,
                    uriType = newImplementationResult.type.label,
                    extractedInfo = newImplementationResult.extractedInfo,
                    isSameResultInBothImpl = isSameResult
                )
            }
        }
    }

    fun dismissResult() {
        viewModelScope.launch {
            _uiState.update {
                DebugURIUtilsUIState.Input("")
            }
        }
    }

    private fun String.handleUsingLegacyImplementation(): URIHandlingResult {
        val isEnroll = FTUriUtils.isEnrollUri(this)
        val isAuth = FTUriUtils.isAuthUri(this)

        return when {
            isEnroll -> {
                URIHandlingResult(
                    type = URI.ENROLL,
                    extractedInfo = mapOf(
                        "userId" to FTUriUtils.getUserIdFromUri(this)!!
                    )
                )
            }

            isAuth -> {
                URIHandlingResult(
                    type = URI.AUTH,
                    extractedInfo = mapOf(
                        "userId" to FTUriUtils.getUserIdFromUri(this)!!,
                        "sessionToken" to FTUriUtils.getSessionTokenFromUri(this)!!
                    )
                )
            }

            else -> {
                URIHandlingResult(
                    type = URI.UNKNOWN,
                    extractedInfo = emptyMap()
                )
            }
        }
    }

    private fun String.handle(): URIHandlingResult = when (
        val result = FTUriUtils.getFTRUriType(this)
    ) {
        is FTRUriType.Auth -> {
            URIHandlingResult(
                type = URI.AUTH,
                extractedInfo = mapOf(
                    "userId" to result.userId,
                    "sessionToken" to result.sessionToken
                )
            )
        }

        is FTRUriType.Enroll -> {
            URIHandlingResult(
                type = URI.ENROLL,
                extractedInfo = mapOf(
                    "userId" to result.userId
                )
            )
        }

        FTRUriType.Unknown -> {
            URIHandlingResult(
                type = URI.UNKNOWN,
                extractedInfo = emptyMap()
            )
        }
    }

    sealed class DebugURIUtilsUIState {
        data class Input(val uri: String): DebugURIUtilsUIState()

        data class URIResult(
            @StringRes val uriTypeLegacyImpl: Int,
            val extractedInfoLegacyImpl: Map<String, String>,
            @StringRes val uriType: Int,
            val extractedInfo: Map<String, String>,
            val isSameResultInBothImpl: Boolean
        ): DebugURIUtilsUIState()
    }

    data class URIHandlingResult(
        val type: URI,
        val extractedInfo: Map<String, String>,
    )

    enum class URI(@Suppress val label: Int) {
        ENROLL(R.string.debug_uri_utils_enroll_uri),
        AUTH(R.string.debug_uri_utils_auth_uri),
        UNKNOWN(R.string.debug_uri_utils_unknown_uri);
    }
}