package com.futurae.demoapp.recovery

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.FuturaeDemoDestinations
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.lock.LockScreenMode
import com.futurae.demoapp.navigateToLockScreen
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.decisionmodal.FuturaeFullScreenDecisionModal
import com.futurae.demoapp.ui.shared.elements.decisionmodal.FuturaeFullScreenDecisionModalUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.InformativeContent
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreen
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenContentUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import com.futurae.demoapp.utils.UserPresenceVerificationHelper
import com.futurae.sdk.public_api.lock.model.UserPresenceVerificationFactor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun SDKRecoveryFlow(
    pinProviderViewModel: PinProviderViewModel,
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as FuturaeDemoApplication
    val sdkRecoveryViewModel: SDKRecoveryViewModel = viewModel(
        factory = SDKRecoveryViewModel.provideFactory(
            application = application,
            pinProviderViewModel = pinProviderViewModel
        )
    )

    val state by sdkRecoveryViewModel.state.collectAsStateWithLifecycle()
    val context = LocalActivity.current as FragmentActivity

    when (state) {
        is ILCEState.Content -> ResultInformativeScreen(
            uiState = ResultInformativeScreenUIState(
                state = ResultState.SUCCESS,
                title = TextWrapper.Resource(R.string.successful),
                actionCta = TextWrapper.Resource(R.string.dismiss)
            ),
            onAction = {
                navController.popBackStack()
                navController.navigate(FuturaeDemoDestinations.SPLASH_ROUTE.route)
            }
        ) {
            InformativeContent(
                contentUIState = ResultInformativeScreenContentUIState.Informative(
                    title = TextWrapper.Resource(R.string.restore_accounts_successful_title),
                    description = TextWrapper.Resource(R.string.restore_accounts_successful_description)
                )
            )
        }

        is ILCEState.Error -> ResultInformativeScreen(
            uiState = ResultInformativeScreenUIState(
                state = ResultState.ERROR,
                title = TextWrapper.Resource(R.string.failed),
                actionCta = TextWrapper.Resource(R.string.dismiss)
            ),
            onAction = { navController.navigateUp() }
        ) {
            InformativeContent(
                contentUIState = ResultInformativeScreenContentUIState.Informative(
                    title = TextWrapper.Resource(R.string.sdk_generic_error_message_ellipsized),
                    description = TextWrapper.Resource(R.string.restore_accounts_failure_description)
                )
            )
        }

        ILCEState.Idle -> FuturaeFullScreenDecisionModal(
            uiState = FuturaeFullScreenDecisionModalUIState(
                drawableResId = R.drawable.graphic_sdk_recovery,
                titleResId = R.string.sdk_recovery_title,
                descriptionResId = R.string.sdk_recovery_subtitle,
                noticeResId = R.string.sdk_recovery_warning,
                primaryActionResId = R.string.sdk_recovery_action_recover,
                secondaryAction = R.string.sdk_reset,
                isDismissible = true
            ),
            onPrimaryActionClick = sdkRecoveryViewModel::requestSDKRecovery,
            onSecondaryActionClick = {
                navController.popBackStack()
                navController.navigate(FuturaeDemoDestinations.SPLASH_ROUTE.route)
            },
            navController = navController
        )

        ILCEState.Loading -> ResultInformativeScreen(
            uiState = ResultInformativeScreenUIState(
                state = ResultState.LOADING,
                title = TextWrapper.Resource(R.string.restoring_accounts),
                actionCta = null
            ),
            onAction = { navController.navigateUp() }
        ) {
            InformativeContent(
                contentUIState = ResultInformativeScreenContentUIState.Informative(
                    title = TextWrapper.Resource(R.string.please_wait),
                    description = TextWrapper.Resource(R.string.restore_accounts_please_wait)
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        pinProviderViewModel.pinFlow
            .onEach {
                sdkRecoveryViewModel.onPinProvided(it)
                pinProviderViewModel.reset()
            }
            .launchIn(this)

        sdkRecoveryViewModel.upvDependencyFlow
            .onEach {
                when (it) {
                    UserPresenceVerificationFactor.BIOMETRICS -> UserPresenceVerificationHelper.getUPVForSystemAuth(
                        context
                    )

                    UserPresenceVerificationFactor.DEVICE_CREDENTIALS -> UserPresenceVerificationHelper.getUPVForSystemAuth(
                        context
                    )

                    UserPresenceVerificationFactor.APP_PIN -> {
                        navController.navigateToLockScreen(LockScreenMode.GET_PIN)
                    }

                    UserPresenceVerificationFactor.NONE,
                    UserPresenceVerificationFactor.UNKNOWN -> throw IllegalStateException("Unable to support UPV for $it")
                }
            }
            .launchIn(this)
    }
}