package com.futurae.demoapp.accountsrestoration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.R
import com.futurae.demoapp.accountsrestoration.arch.AccountsRestorationFlowViewModel
import com.futurae.demoapp.arch.PinProviderViewModel
import com.futurae.demoapp.navigateToLockScreen
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.decisionmodal.FuturaeFullScreenDecisionModal
import com.futurae.demoapp.ui.shared.elements.decisionmodal.FuturaeFullScreenDecisionModalUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.InformativeContent
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreen
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenContentUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultInformativeScreenUIState
import com.futurae.demoapp.ui.shared.elements.resultinformativescreen.ResultState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun AccountsRestorationFlow(
    pinProviderViewModel: PinProviderViewModel,
    isPinProtected: Boolean,
    navController: NavController
) {
    val accountsRestorationViewModel: AccountsRestorationFlowViewModel = viewModel(
        factory = AccountsRestorationFlowViewModel.provideFactory(isPinProtected)
    )

    val state by accountsRestorationViewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ILCEState.Content -> ResultInformativeScreen(
            uiState = ResultInformativeScreenUIState(
                state = ResultState.SUCCESS,
                title = TextWrapper.Resource(R.string.successful),
                actionCta = TextWrapper.Resource(R.string.dismiss)
            ),
            onAction = {
                navController.navigateUp()
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
                drawableResId = R.drawable.graphic_accounts_restoration,
                titleResId = R.string.restore_accounts_title,
                descriptionResId = R.string.restore_accounts_description,
                noticeResId = R.string.restore_accounts_notice,
                primaryActionResId = R.string.restore_accounts_cta,
                secondaryAction = R.string.restore_accounts_dismiss_cta,
                isDismissible = true
            ),
            onPrimaryActionClick = accountsRestorationViewModel::attemptToRestoreAccounts,
            onSecondaryActionClick = {
                navController.navigateUp()
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
                accountsRestorationViewModel.onPinProvided(it)
                pinProviderViewModel.reset()
            }
            .launchIn(this)

        accountsRestorationViewModel.pinRequestFlow
            .onEach { navController.navigateToLockScreen(it) }
            .launchIn(this)
    }
}