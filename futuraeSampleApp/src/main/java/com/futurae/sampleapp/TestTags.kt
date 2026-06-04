package com.futurae.sampleapp

object TestTags {

    object SdkConfiguration {
        const val skipHardwareSecurityToggle = "toggle_skip_hw_sec"
        const val requireUnlockedDeviceToggle = "toggle_req_unlocked_device"
        const val biometricInvalidationToggle = "toggle_biometric_invalidation"
        const val changePinWithBiometricsToggle = "toggle_change_pin_with_bio"
    }

    object TopAppBar {
        const val navigateUpButton = "header_back_button"
    }

    object AccountsScreen {
        const val accountsList = "accounts_list"
        const val accountItem = "account_item"
        const val blankSlate = "accounts_blank_slate"
    }

    object AccountHistoryScreen {
        const val historyList = "account_history_list"
        const val historyItem = "account_history_item"
        const val historyItemFailure = "account_history_failure"
        const val historyItemSuccess = "account_history_success"
        const val blankSlate = "account_history_blank_slate"
        const val errorBlankSlate = "account_history_error_blank_slate"
    }

    object ActivationCodeScreen {
        const val activationCodeInput = "manual_entry_input"
    }

    object AdaptiveSettingsScreen {
        const val settingsList = "adaptive_settings_list"
    }

    object AdaptiveCollectionsScreen {
        const val collectionsList = "adaptive_collections_list"
        const val collectionItem = "adaptive_collection_item"
    }

    object MoreScreen {
        const val settingsList = "more_settings_list"
    }

    object SDKSettingsScreen {
        const val settingsList = "sdk_settings_list"
    }

    object AuthenticationConfirmationScreen {
        const val detailItem = "auth_detail_item"
    }

    object FuturaeFullScreenDecisionModal {
        const val closeButton = "decision_modal_close_button"
    }

    object SettingsRow {
        const val settingsRow = "settings_row"
        const val settings_toggle = "settings_toggle"
        const val flowBindingSettingsToggle = "toggle_flow_binding"
        const val unprotectedSessionFetchingSettingsToggle = "toggle_session_fetch_unprotected"
    }
}
