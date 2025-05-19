package com.futurae.demoapp.settings.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.R
import com.futurae.demoapp.accountsrecovery.check.arch.AccountsRecoveryCheckViewModel
import com.futurae.demoapp.settings.common.SettingsRowComposable
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogWithFilledButtons
import com.futurae.demoapp.ui.shared.elements.snackbar.FuturaeSnackbarUIState
import com.futurae.demoapp.ui.theme.Tertiary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    accountsRecoveryCheckViewModel: AccountsRecoveryCheckViewModel,
    showSnackBar: suspend (FuturaeSnackbarUIState) -> Unit,
    navigateTo: (route: String, launchSingleTop: Boolean) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as FuturaeDemoApplication
    val moreViewModel: MoreViewModel = viewModel(
        factory = MoreViewModel.provideFactory(
            application = application,
            accountsRecoveryCheckViewModel = accountsRecoveryCheckViewModel
        )
    )

    val items by moreViewModel.moreItems.collectAsStateWithLifecycle()
    val accountsDeletionDialogUIState by moreViewModel.accountsDeletionDialogUIState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Tertiary)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                SettingsRowComposable(item)
            }
        }

        accountsDeletionDialogUIState?.let {
            FuturaeAlertDialogWithFilledButtons(
                uiState = it,
                onConfirm = {
                    moreViewModel.onAccountsDeletionConfirmed()
                },
                onDeny = {
                    moreViewModel.onAccountsDeletionCanceled()
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        launch {
            moreViewModel.navigationEvent.collectLatest {
                navigateTo(it.first, it.second)
            }
        }

        launch {
            moreViewModel.accountsDeletionResult.collect {
                it
                    .onSuccess {
                        showSnackBar(
                            FuturaeSnackbarUIState.Success(
                                TextWrapper.Resource(R.string.accounts_deleted_successfully)
                            )
                        )
                    }
                    .onFailure {
                        showSnackBar(
                            FuturaeSnackbarUIState.Error(
                                TextWrapper.Resource(R.string.accounts_failed_to_be_deleted)
                            )
                        )
                    }

            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                moreViewModel.refreshItems()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}