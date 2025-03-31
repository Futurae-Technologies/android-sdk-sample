package com.futurae.demoapp.home.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futurae.demoapp.R
import com.futurae.demoapp.arch.FuturaeViewModel
import com.futurae.demoapp.home.accounts.arch.AccountsViewModel
import com.futurae.demoapp.home.accounts.restoreaccountsbanner.RestoreAccountsBanner
import com.futurae.demoapp.home.accounts.restoreaccountsbanner.RestoreAccountsBannerUIState
import com.futurae.demoapp.migration.arch.MigrationViewModel
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialog
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogUIState
import com.futurae.demoapp.ui.shared.elements.servicelogo.ServiceLogo
import com.futurae.demoapp.ui.shared.elements.timeoutIndicator.TimeoutIndicator
import com.futurae.demoapp.ui.theme.DisableColor
import com.futurae.demoapp.ui.theme.FuturaeTypography
import com.futurae.demoapp.ui.theme.Gray75
import com.futurae.demoapp.ui.theme.OnPrimaryColor
import com.futurae.demoapp.ui.theme.PrimaryColor
import com.futurae.demoapp.ui.theme.WarningColor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun AccountsScreen(
    futuraeViewModel: FuturaeViewModel,
    migrationViewModel: MigrationViewModel,
    onAccountClick: (String) -> Unit,
    onAccountRestorationClick: (Boolean) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current

    val accountsViewModel: AccountsViewModel = viewModel(
        factory = AccountsViewModel.provideFactory()
    )

    val uiState by accountsViewModel.uiState.collectAsStateWithLifecycle()
    val restoreAccountsBannerUIState by accountsViewModel.restorationBannerUIState.collectAsStateWithLifecycle()
    val timeoutProgress by accountsViewModel.timeoutCountdownProgress.collectAsStateWithLifecycle()

    AccountsScreen(
        uiState = uiState,
        timeoutProgress = timeoutProgress,
        restoreAccountsBannerUIState = restoreAccountsBannerUIState,
        onAccountClick = onAccountClick,
        onHOTPRequest = accountsViewModel::getHOTP,
        onDeleteAccountRequest = accountsViewModel::deleteAccount,
        onAccountRestorationBannerDismiss = accountsViewModel::accountsRestorationBannerDismissed,
        onRestoreAccountsClick = {
            accountsViewModel.userHasBeenInformed()
            onAccountRestorationClick(it)
        }
    )

    LaunchedEffect(Unit) {
        accountsViewModel.onHOTPGenerated
            .onEach { clipboardManager.setText(AnnotatedString(it)) }
            .launchIn(this)

        migrationViewModel.migrationInfo
            .onEach { accountsViewModel.onMigrationInfoChanges(it) }
            .launchIn(this)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                futuraeViewModel.fetchAccountsStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun AccountsScreen(
    uiState: AccountsScreenUIState,
    timeoutProgress: Float,
    restoreAccountsBannerUIState: RestoreAccountsBannerUIState,
    onAccountClick: (String) -> Unit,
    onHOTPRequest: (String) -> Unit,
    onDeleteAccountRequest: (String) -> Unit,
    onAccountRestorationBannerDismiss: () -> Unit,
    onRestoreAccountsClick: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (uiState.hasEnrolledAccounts)  {
            AccountList(
                accountUIStates = uiState.accountRowUIStates,
                timeoutProgress = timeoutProgress,
                onAccountClick = onAccountClick,
                onHOTPRequest = onHOTPRequest,
                onDeleteAccountRequest = onDeleteAccountRequest
            )
        } else {
            NoneEnrolledAccountScreen(
                restoreAccountsBannerUIState = restoreAccountsBannerUIState,
                onAccountRestorationBannerDismiss = onAccountRestorationBannerDismiss,
                onRestoreAccountsClick = onRestoreAccountsClick
            )
        }
    }
}

@Composable
private fun NoneEnrolledAccountScreen(
    restoreAccountsBannerUIState: RestoreAccountsBannerUIState,
    onAccountRestorationBannerDismiss: () -> Unit,
    onRestoreAccountsClick: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AccountsBlankSlate()
        RestoreAccountsBanner(
            restoreAccountsBannerUIState,
            onAccountRestorationBannerDismiss,
            onRestoreAccountsClick
        )
    }
}

@Composable
private fun AccountList(
    accountUIStates: List<AccountRowUIState>,
    timeoutProgress: Float,
    onAccountClick: (String) -> Unit,
    onHOTPRequest: (String) -> Unit,
    onDeleteAccountRequest: (String) -> Unit
) {
    var lockedAccountInformativeDialog by remember {
        mutableStateOf<FuturaeAlertDialogUIState?>(null)
    }

    TimeoutIndicator(progress = timeoutProgress)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = OnPrimaryColor)
    ) {
        items(
            items = accountUIStates,
            key = { it.userId }
        ) {
            AccountItem(
                uiState = it,
                onClick = onAccountClick,
                onHOTPRequest = onHOTPRequest,
                onDeleteAccountRequest = onDeleteAccountRequest,
                onLockedAccountIconClicked = { dialogUIState ->
                    lockedAccountInformativeDialog = dialogUIState
                }
            )
        }
    }

    lockedAccountInformativeDialog?.let {
        FuturaeAlertDialog(
            uiState = it,
            onConfirm = {
                lockedAccountInformativeDialog = null
            },
            onDismiss = {
                lockedAccountInformativeDialog = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountItem(
    uiState: AccountRowUIState,
    onClick: (String) -> Unit,
    onHOTPRequest: (String) -> Unit,
    onDeleteAccountRequest: (String) -> Unit,
    onLockedAccountIconClicked: (FuturaeAlertDialogUIState) -> Unit
) {
    var showAccountActions by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .background(OnPrimaryColor)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick(uiState.userId) },
                    onLongClick = { showAccountActions = true }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ServiceLogo(
                modifier = Modifier.size(56.dp),
                url = uiState.serviceLogo
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.serviceName,
                    style = FuturaeTypography.titleH4,
                    color = PrimaryColor
                )
                Text(
                    text = uiState.username,
                    style = FuturaeTypography.bodyLarge,
                    color = Gray75
                )
            }

            Column(
                modifier = Modifier.height(56.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (uiState.isLocked) {
                    IconButton(
                        onClick = {
                            onLockedAccountIconClicked(
                                uiState.getLockedAccountInformativeDialogUIState()
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_alert),
                            contentDescription = "Locked account",
                            tint = WarningColor
                        )
                    }
                    if (showAccountActions) {
                        AccountActionsPopup(
                            isAccountLocked = true,
                            onDismissPopup = { showAccountActions = false },
                            onHOTPRequest = {
                                onHOTPRequest(uiState.userId)
                                showAccountActions = false
                            },
                            onDeleteAccountRequest = {
                                onDeleteAccountRequest(uiState.userId)
                                showAccountActions = false
                            }
                        )
                    }
                } else {
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(top = 6.dp)
                    )
                    Box {
                        Text(
                            text = uiState.code,
                            style = FuturaeTypography.titleH2,
                            color = PrimaryColor
                        )
                        if (showAccountActions) {
                            AccountActionsPopup(
                                isAccountLocked = false,
                                onDismissPopup = { showAccountActions = false },
                                onHOTPRequest = {
                                    onHOTPRequest(uiState.userId)
                                    showAccountActions = false
                                },
                                onDeleteAccountRequest = {
                                    onDeleteAccountRequest(uiState.userId)
                                    showAccountActions = false
                                }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = DisableColor)
    }
}

@Composable
private fun AccountActionsPopup(
    isAccountLocked: Boolean,
    onHOTPRequest: () -> Unit,
    onDeleteAccountRequest: () -> Unit,
    onDismissPopup: () -> Unit
) {
    val context = LocalContext.current
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissPopup,
        modifier = Modifier.background(OnPrimaryColor),
        offset = DpOffset(x = (-16).dp, y = (-16).dp),
    ) {
        if (!isAccountLocked) {
            DropdownMenuItem(
                contentPadding = PaddingValues(vertical = 2.dp, horizontal = 8.dp),
                text = {
                    Text(
                        text = TextWrapper.Resource(R.string.generate_hotp).value(context),
                        color = PrimaryColor
                    )
                },
                onClick = onHOTPRequest
            )
        }
        DropdownMenuItem(
            contentPadding = PaddingValues(vertical = 2.dp, horizontal = 8.dp),
            text = {
                Text(
                    text = TextWrapper.Resource(R.string.delete_account).value(context),
                    color = PrimaryColor
                )
            },
            onClick = onDeleteAccountRequest
        )
    }
}

@Composable
private fun AccountsBlankSlate() {
    Column (
        modifier = Modifier
            .background(color = OnPrimaryColor)
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.graphic_empty_account),
            contentDescription = "Empty Accounts"
        )

        Text(
            text = stringResource(R.string.accounts_list_is_empty),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryColor
        )

        Text(
            text = stringResource(R.string.accounts_list_is_empty_description),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryColor
        )
    }
}