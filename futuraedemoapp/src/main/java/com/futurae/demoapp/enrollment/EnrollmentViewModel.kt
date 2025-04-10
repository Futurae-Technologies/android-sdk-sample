package com.futurae.demoapp.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.LocalStorage
import com.futurae.demoapp.R
import com.futurae.demoapp.home.usecase.EnrollUserUseCase
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeComposableUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenContentUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.demoapp.ui.shared.elements.serviceinfosection.ServiceInfoSectionUIState
import com.futurae.sdk.public_api.account.model.EnrollAccount
import com.futurae.sdk.public_api.account.model.EnrollAccountAndSetupSDKPin
import com.futurae.sdk.public_api.account.model.EnrollmentInput
import com.futurae.sdk.public_api.account.model.EnrollmentParams
import com.futurae.sdk.public_api.common.model.FTAccount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnrollmentViewModel(
    enrollmentInput: EnrollmentInput,
    private val enrollmentUseCase: EnrollUserUseCase
) : ViewModel() {

    companion object {
        fun provideFactory(enrollmentInput: EnrollmentInput): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = EnrollmentViewModel(
                enrollmentInput = enrollmentInput,
                enrollmentUseCase = EnrollUserUseCase()
            ) as T
        }
    }

    private val enrollmentSteps: List<EnrollmentStep> = getEnrollmentSteps()

    private val _pinRequested = MutableStateFlow(false)
    val pinRequestFlow = _pinRequested.asStateFlow()

    private val _activateBiometricsRequested = MutableStateFlow(false)
    val activateBiometricsFlow = _activateBiometricsRequested.asStateFlow()

    private val _exitScreen = MutableSharedFlow<Unit>()
    val exitScreen = _exitScreen.asSharedFlow()

    private val _navigateToAccounts = MutableSharedFlow<Unit>()
    val navigateToAccounts = _navigateToAccounts.asSharedFlow()

    private val _state = MutableStateFlow(
        EnrollmentViewModelState(
            enrollmentInput = enrollmentInput,
            currentStep = enrollmentSteps.first(),
            shouldPromptUserForBiometrics = EnrollmentStep.SDK_PIN_INPUT in enrollmentSteps
        )
    )
    val uiState = _state
        .map(EnrollmentViewModelState::mapToUIState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            EnrollmentViewModelState(
                enrollmentInput = enrollmentInput,
                currentStep = enrollmentSteps.first(),
                shouldPromptUserForBiometrics = EnrollmentStep.SDK_PIN_INPUT in enrollmentSteps
            ).mapToUIState()
        )

    init {
        _state.value.processStep()
    }

    fun onFlowBindingTokenChange(toke: String) {
        viewModelScope.launch {
            _state.update { it.copy(tempFlowBindingToken = toke) }
        }
    }

    fun onFlowBindingTokenSubmitted() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    flowBindingToken = it.tempFlowBindingToken,
                    currentStep = it.currentStep.nextStep()
                )
            }
        }

        _state.value.processStep()
    }

    fun onPinProvided(digits: CharArray?) {
        if (digits == null) {
            return
        }

        _state.update {
            it.copy(
                sdkPin = digits.toList(),
                currentStep = it.currentStep.nextStep()
            )
        }
        _state.value.processStep()
    }

    fun onResultActionClick() {
        val hasEnrollmentCompletedSuccessfully = _state.value.state is ILCEState.Content
        if (hasEnrollmentCompletedSuccessfully) {
            navigateToAccounts()
        } else {
            exitScreen()
        }
    }

    fun pinRequested() {
        viewModelScope.launch {
            _pinRequested.emit(false)
        }
    }

    fun activatingBiometricsRequested() {
        viewModelScope.launch {
            _activateBiometricsRequested.emit(false)
        }
    }

    private fun getEnrollmentSteps(): List<EnrollmentStep> {
        val steps = mutableListOf<EnrollmentStep>()

        if (LocalStorage.isFlowBindingEnabled()) {
            steps.add(EnrollmentStep.FLOW_BINDING_TOKEN_INPUT)
        }

        if (LocalStorage.shouldSetupPin) {
            steps.add(EnrollmentStep.SDK_PIN_INPUT)
        }

        steps.add(EnrollmentStep.PROCESSING)

        return steps
    }

    private fun EnrollmentStep.nextStep(): EnrollmentStep {
        val currentIndex = enrollmentSteps.indexOf(this)
        return if (currentIndex in 0 until enrollmentSteps.lastIndex) {
            enrollmentSteps[currentIndex + 1]
        } else {
            this
        }
    }

    private fun EnrollmentViewModelState.processStep() {
        when (currentStep) {
            EnrollmentStep.FLOW_BINDING_TOKEN_INPUT -> {
                // ignore this since this is being handled on composable
            }
            EnrollmentStep.SDK_PIN_INPUT -> {
                viewModelScope.launch {
                    _pinRequested.emit(true)
                }
            }
            EnrollmentStep.PROCESSING -> {
                enrollUser(
                    enrollmentParams = EnrollmentParams(
                        inputCode = enrollmentInput,
                        enrollmentUseCase = if (sdkPin == null) {
                            EnrollAccount
                        } else {
                            EnrollAccountAndSetupSDKPin(sdkPin = sdkPin.toCharArray())
                        },
                        flowBindingToken =_state.value.flowBindingToken
                    )
                )
            }
        }
    }

    private fun enrollUser(enrollmentParams: EnrollmentParams) {
        viewModelScope.launch {
            enrollmentUseCase(enrollmentParams)
                .onSuccess { account ->
                    _state.update { it.copy(state = ILCEState.Content(account)) }
                }
                .onFailure { error ->
                    _state.update { it.copy(state = ILCEState.Error(error)) }
                }
        }
    }

    private fun exitScreen() {
        viewModelScope.launch {
            _exitScreen.emit(Unit)
        }
    }

    private fun navigateToAccounts() {
        viewModelScope.launch {
            _navigateToAccounts.emit(Unit)
        }
    }

    fun skipEnablingBiometrics() {
        viewModelScope.launch {
            _state.update { it.copy(shouldPromptUserForBiometrics = false) }
        }
    }

    fun enableBiometrics() {
        viewModelScope.launch {
            _state.update { it.copy(shouldPromptUserForBiometrics = false) }
            _activateBiometricsRequested.emit(true)
        }
    }

    data class EnrollmentViewModelState(
        val currentStep: EnrollmentStep,
        val enrollmentInput: EnrollmentInput,
        val tempFlowBindingToken: String = "",
        val flowBindingToken: String? = null,
        val sdkPin: List<Char>? = null,
        val state: ILCEState<FTAccount> = ILCEState.Loading,
        val shouldPromptUserForBiometrics: Boolean
    ) {
        fun mapToUIState(): EnrollmentUIState = when (currentStep) {
            EnrollmentStep.FLOW_BINDING_TOKEN_INPUT -> {
                EnrollmentUIState.FlowBindingTokenInput(tempFlowBindingToken)
            }

            EnrollmentStep.PROCESSING -> {
                when (state) {
                    is ILCEState.Content -> EnrollmentUIState.Result(
                        uiState = ResultInformativeComposableUIState(
                            resultInformativeScreenUIState = ResultInformativeScreenUIState(
                                title = TextWrapper.Resource(R.string.account_added),
                                state = ResultState.SUCCESS,
                                actionCta = TextWrapper.Resource(R.string.dismiss)
                            ),
                            contentUIState = ResultInformativeScreenContentUIState.NewAccountEnrolled(
                                serviceInfoSectionUIState = ServiceInfoSectionUIState(
                                    serviceLogo = state.data.serviceLogo ?: "",
                                    serviceName = state.data.serviceName,
                                    username = state.data.username
                                )
                            )
                        ),
                        shouldPromptUserToEnableBiometrics = shouldPromptUserForBiometrics
                    )
                    is ILCEState.Error -> EnrollmentUIState.Result(
                        ResultInformativeComposableUIState(
                            resultInformativeScreenUIState = ResultInformativeScreenUIState(
                                title = TextWrapper.Resource(R.string.account_enrollment_failed),
                                state = ResultState.ERROR,
                                actionCta = TextWrapper.Resource(R.string.dismiss)
                            ),
                            contentUIState = ResultInformativeScreenContentUIState.Informative(
                                title = TextWrapper.Resource(R.string.account_enrollment_failed_title),
                                description = TextWrapper.Resource(R.string.account_enrollment_failed_description)
                            )
                        )
                    )
                    else -> EnrollmentUIState.Result(
                        ResultInformativeComposableUIState(
                            resultInformativeScreenUIState = ResultInformativeScreenUIState(
                                title = TextWrapper.Resource(R.string.account_enrollment_progress),
                                state = ResultState.LOADING,
                                actionCta = null
                            ),
                            contentUIState = ResultInformativeScreenContentUIState.Informative(
                                title = TextWrapper.Resource(R.string.please_wait),
                                description = TextWrapper.Resource(R.string.account_enrollment_please_wait)
                            )
                        )
                    )
                }
            }

            else -> {
                EnrollmentUIState.Idle
            }
        }
    }

    enum class EnrollmentStep {
        FLOW_BINDING_TOKEN_INPUT,
        SDK_PIN_INPUT,
        PROCESSING;
    }
}
