# [Automatic Account Recovery](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#automatic-account-recovery)

This package demonstrates how to implement the **Automatic Account Recovery** feature using the Futurae Android SDK. It serves as a reference for integrating the same logic into your own app.

The automatic recovery mechanism allows users to migrate their Futurae account(s) from a previous app installation to a new one — either on a different device or the same device after reinstalling the app. It leverages Android OS's backup and restore capabilities to persist recovery data and enables the user to automatically restore their accounts during a fresh install.

The behavior also depends on whether [Adaptive Account Recovery](https://www.futurae.com/docs/guide/adaptive-account-recovery/) is enabled at the app level and the associated Service. The app delegates the recovery flow accordingly to the SDK.

---

## 🛠 Getting Started

- `AccountsRecoveryViewModel` is responsible for checking whether there are accounts to be recovered.
- `GetMigratableAccountsUseCase` wraps the SDK's `MigrationApi.getMigratableAccounts` response into a Kotlin `Result`.
- `AccountsRecoveryFlow` is the main composable responsible for handling the entire recovery process. The `Flow` suffix indicates it encapsulates a complete logical flow, not just a simple UI element.
- `AccountsRecoveryFlowViewModel` manages the UI state and business logic. It emits states using the sealed class [`ILCEState`](https://github.com/Futurae-Technologies/android-sdk-demo/blob/11b7b3039c1b65f828dbf7ca34d9caf2a819355e/futuraedemoapp/src/main/java/com/futurae/demoapp/utils/ILCEState.kt), which represents loading, content, and error states.
- `MigrateAccountsUseCase` wraps the SDK's `migrationApi.migrateAccounts` response into a Kotlin `Result`.

---

## 🔍 Flow Overview

1. The app verifies whether there are accounts to recover using the SDK's `FuturaeMigrationApi` and specifically the `getMigratableAccounts` endpoint.
2. This call returns a `MigratableAccounts` object. If `MigratableAccounts.migratableAccountInfos` is not empty, accounts are available for recovery.
   1. The user is then prompted to trigger the recovery process.
   2. Once confirmed, the app delegates the account recovery to the SDK using `FuturaeMigrationApi.migrateAccounts`.
      - If the SDK is configured with `SDK_PIN_WITH_BIOMETRICS_OPTIONAL` or if `MigratableAccounts.pinProtected` is `true`, the user will be prompted to enter their SDK PIN.
      - If `MigratableAccounts.adaptiveEnabled` is `true`, you must first enable Adaptive and Adaptive Account Recovery before proceeding; otherwise, the account recovery will fail. This is done by calling `FuturaeAdaptiveApi.enableAdaptive` and `FuturaeAdaptiveApi.enableAdaptiveSubmissionOnAccountMigration`.
3. Upon successful recovery, a confirmation message is displayed.
4. The recovered accounts are shown in the [AccountsScreen](https://github.com/Futurae-Technologies/android-sdk-demo/blob/e27816039ab5e9786913f51c229c4fe661cf0d0b/futuraedemoapp/src/main/java/com/futurae/demoapp/home/accounts/AccountsScreen.kt), using the SDK's `FuturaeAccountApi.activeAccountsFlow` Kotlin `Flow`.

Please **note** that Account Recovery is possible as long as no accounts have been enrolled on this installation.