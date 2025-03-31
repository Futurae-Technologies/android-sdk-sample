package com.futurae.demoapp.settings.more

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futurae.demoapp.FuturaeDemoApplication
import com.futurae.demoapp.FuturaeDemoDestinations
import com.futurae.demoapp.ILCEState
import com.futurae.demoapp.LocalStorage
import com.futurae.demoapp.NavigationArguments
import com.futurae.demoapp.R
import com.futurae.demoapp.home.usecase.LogoutUseCase
import com.futurae.demoapp.migration.arch.MigrationViewModel
import com.futurae.demoapp.settings.SettingsItem
import com.futurae.demoapp.settings.SettingsListItem
import com.futurae.demoapp.settings.SettingsSpacer
import com.futurae.demoapp.ui.TextWrapper
import com.futurae.demoapp.ui.shared.elements.alertdialog.FuturaeAlertDialogWithFilledButtonsUIState
import com.futurae.demoapp.ui.shared.elements.buttons.ActionButtonType
import com.futurae.sdk.FuturaeSDK
import com.futurae.sdk.public_api.migration.model.MigratableAccountInfo
import com.futurae.sdk.public_api.migration.model.MigratableAccounts
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoreViewModel(
    private val migrationInfo: StateFlow<ILCEState<MigratableAccounts>>,
    private val logoutUseCase: LogoutUseCase,
    application: FuturaeDemoApplication
) : AndroidViewModel(application) {

    companion object {
        fun provideFactory(
            application: FuturaeDemoApplication,
            migrationViewModel: MigrationViewModel
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T = MoreViewModel(
                logoutUseCase = LogoutUseCase(),
                migrationInfo = migrationViewModel.migrationInfo,
                application = application
            ) as T
        }
    }

    private var migratableAccountInfos: List<MigratableAccountInfo> = emptyList()
    private var isAccountMigrationPinProtected: Boolean = false

    private val _moreItems = MutableStateFlow(
        generateMoreItems()
    )
    val moreItems = _moreItems.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Pair<String, Boolean>>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _accountsDeletionResult = MutableSharedFlow<Result<Unit>>()
    val accountsDeletionResult = _accountsDeletionResult.asSharedFlow()

    private val _accountsDeletionDialogUIState = MutableStateFlow<FuturaeAlertDialogWithFilledButtonsUIState?>(null)
    val accountsDeletionDialogUIState = _accountsDeletionDialogUIState.asStateFlow()

    init {
        viewModelScope.launch {
            migrationInfo.collect {
                if (it is ILCEState.Content) {
                    migratableAccountInfos = it.data.migratableAccountInfos
                    isAccountMigrationPinProtected = it.data.pinProtected

                    refreshItems()
                }
            }
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            _moreItems.emit(generateMoreItems())
        }
    }

    fun onAccountsDeletionConfirmed() {
        viewModelScope.launch {
            _accountsDeletionDialogUIState.emit(null)

            logoutUseCase
                .invoke()
                .also { _accountsDeletionResult.emit(it) }

            refreshItems()
        }
    }

    fun onAccountsDeletionCanceled() {
        viewModelScope.launch {
            _accountsDeletionDialogUIState.emit(null)
        }
    }

    private fun generateMoreItems(): List<SettingsListItem> {
        return listOf(
            SettingsItem(
                title = TextWrapper.Resource(R.string.learn_more),
                subtitle = TextWrapper.Resource(R.string.futurae_homepage),
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                actionCallback = {
                    // TODO move out of ViewModel
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.futurae.com")).apply {
                            flags = FLAG_ACTIVITY_NEW_TASK
                        }
                    getApplication<Application>().startActivity(intent)
                }
            ),
            SettingsItem(
                title = TextWrapper.Resource(R.string.settings),
                subtitle = TextWrapper.Resource(R.string.configuration),
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                actionCallback = {
                    viewModelScope.launch {
                        _navigationEvent.emit(
                            FuturaeDemoDestinations.SETTINGS_ROUTE.route to false
                        )
                    }
                }
            ),
            SettingsSpacer,
            createRestoreAccountsItem(),
            SettingsItem(
                title = TextWrapper.Resource(R.string.delete_all_accounts),
                subtitle = TextWrapper.Resource(R.string.delete_all_accounts_subtitle),
                isItemWithWarning = FuturaeSDK.client.accountApi.getActiveAccounts().isNotEmpty(),
                isItemClickable = FuturaeSDK.client.accountApi.getActiveAccounts().isNotEmpty(),
                actionCallback = {
                    viewModelScope.launch {
                        _accountsDeletionDialogUIState.emit(
                            FuturaeAlertDialogWithFilledButtonsUIState(
                                drawableRes = R.drawable.graphic_warning_shield,
                                title = TextWrapper.Resource(R.string.delete_all_accounts_confirmation_dialog_title),
                                text = TextWrapper.Resource(R.string.delete_all_accounts_confirmation_dialog_text),
                                confirmButtonCta = TextWrapper.Resource(R.string.delete_all_accounts_confirmation_dialog_delete_cta),
                                dismissButtonCta = TextWrapper.Resource(R.string.delete_all_accounts_confirmation_dialog_cancel_cta),
                                confirmButtonType = ActionButtonType.Warning
                            )
                        )
                    }
                }
            ),
        )
    }

    private fun createRestoreAccountsItem(): SettingsItem {
        val info = migratableAccountInfos.toSettingsItemInfo()
        return SettingsItem(
            title = TextWrapper.Resource(info.title),
            subtitle = TextWrapper.Resource(info.description),
            isItemClickable = info.isRestorationAvailable,
            isItemWithWarning = info.isRestorationAvailable,
            actionCallback = {
                viewModelScope.launch {
                    _navigationEvent.emit(
                        FuturaeDemoDestinations.ACCOUNTS_RESTORATION_ROUTE.route  +
                                "?${NavigationArguments.AccountRestorationRoute.IS_PIN_PROTECTED}" +
                                "=$isAccountMigrationPinProtected" to true
                    )
                }
            }
        )
    }

    private fun List<MigratableAccountInfo>.toSettingsItemInfo(): MigrationInfo {
        val hasBackUpAvailable = isNotEmpty()
        val hasEnrolledAccounts = FuturaeSDK.client.accountApi.getActiveAccounts().isNotEmpty()
        val haveAccountsBeenRestored = LocalStorage.haveAccountsBeenRestored
        val hasUserEnrolledWithoutRestoration = hasBackUpAvailable && hasEnrolledAccounts &&
                !haveAccountsBeenRestored
        val isRestorationAvailable = hasBackUpAvailable && !hasEnrolledAccounts
        val (title, description) = when {
            isRestorationAvailable -> {
                R.string.restore_accounts to R.string.restore_accounts_subtitle
            }
            hasUserEnrolledWithoutRestoration -> {
                R.string.restore_accounts_disabled to
                        R.string.already_enrolled_account_restore_subtitle
            }
            hasEnrolledAccounts -> {
                R.string.restore_accounts_disabled to
                        R.string.restore_accounts_on_next_install_subtitle
            }
            else -> {
                R.string.restore_accounts_disabled to R.string.no_accounts_to_restore_subtitle
            }
        }

        return MigrationInfo(
            isRestorationAvailable = isRestorationAvailable,
            title = title,
            description = description
        )
    }
}

private data class MigrationInfo(
    val isRestorationAvailable: Boolean,
    @StringRes val title: Int,
    @StringRes val description: Int
)