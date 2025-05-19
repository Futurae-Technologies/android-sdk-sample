package com.futurae.demoapp.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.navigation.FuturaeDemoDestinations
import com.futurae.demoapp.R
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeComposableUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenContentUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultInformativeViewModel : ViewModel() {

    private val _navigateToResultInformativeScreen = MutableSharedFlow<Unit>()
    val navigateToResultInformativeScreen = _navigateToResultInformativeScreen.asSharedFlow()

    private val _exitResultScreen = MutableSharedFlow<Unit>()
    val exitResultScreen = _exitResultScreen.asSharedFlow()

    private val _navigateTo = MutableSharedFlow<FuturaeDemoDestinations>()
    val navigateTo = _navigateTo.asSharedFlow()

    private val _resultInformativeRouteUIState = MutableStateFlow<ResultInformativeComposableUIState?>(null)
    val resultInformativeRouteUIState = _resultInformativeRouteUIState.asStateFlow()

    fun onOfflineVerificationCodeReceived(verificationCode: String) {
        viewModelScope.launch {
            _resultInformativeRouteUIState.emit(
                ResultInformativeComposableUIState(
                    resultInformativeScreenUIState = ResultInformativeScreenUIState(
                        title = TextWrapper.Resource(R.string.offline_verification_code),
                        state = ResultState.SUCCESS,
                        actionCta = TextWrapper.Resource(R.string.dismiss)
                    ),
                    contentUIState = ResultInformativeScreenContentUIState.VerificationCodeReceived(
                        code = verificationCode
                    )
                )
            )

            _navigateToResultInformativeScreen.emit(Unit)
        }
    }

    fun onActionClick() {
        viewModelScope.launch {
            val state = _resultInformativeRouteUIState.value
            when (state?.contentUIState) {
                is ResultInformativeScreenContentUIState.NewAccountEnrolled,
                is ResultInformativeScreenContentUIState.VerificationCodeReceived -> {
                    _navigateTo.emit(FuturaeDemoDestinations.ACCOUNTS_ROUTE)
                }
                is ResultInformativeScreenContentUIState.Informative -> {
                    _exitResultScreen.emit(Unit)
                }
                null -> return@launch
            }

            _resultInformativeRouteUIState.emit(null)
        }
    }

    fun onInvalidQRCode() {
        viewModelScope.launch {
            _resultInformativeRouteUIState.emit(
                ResultInformativeComposableUIState(
                    resultInformativeScreenUIState = ResultInformativeScreenUIState(
                        title = TextWrapper.Resource(R.string.account_enrollment_failed),
                        state = ResultState.ERROR,
                        actionCta = TextWrapper.Resource(R.string.try_again_cta)
                    ),
                    contentUIState = ResultInformativeScreenContentUIState.Informative(
                        title = TextWrapper.Resource(R.string.invalid_qr_code),
                        description = TextWrapper.Resource(R.string.scan_a_valid_qr_prompt)
                    )
                )
            )

            _navigateToResultInformativeScreen.emit(Unit)
        }
    }
}