package com.futurae.sampleapp.utils

enum class UITestTags(val tag : String) {
    ManualEntryInput("manual_entry_input"),
    AccountsList("accounts_list"),
    ToggleSkipHWSec("toggle_skip_hw_sec"),
    ToggleReqUnlockedDevice("toggle_req_unlocked_device"),
    ToggleChangePinWithBio("toggle_change_pin_with_bio"),
    ToggleBiometricInvalidation("toggle_biometric_invalidation"),
    ToggleFlowBinding("toggle_flow_binding"),
    ToggleSessionFetchUnprotected("toggle_session_fetch_unprotected"),
    ToggleGeofencing("toggle_geofencing"),
    HeaderBackButton("header_back_button"),
    AccountsHistoryList("accounts_history_list"),
    AccountsHistorySuccess("account_history_success"),
    AccountsHistoryFail("account_history_failure"),
}