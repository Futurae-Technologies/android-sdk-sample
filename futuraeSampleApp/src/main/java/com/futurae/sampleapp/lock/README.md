# [Lock Configuration](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#lock-configuration-type)

This package demonstrates how to interact with the locking mechanism of the Futurae Android SDK that is aimed to offer an extra layer of security on sensitive SDK-related operations.

The Futurae SDK offers the following lock configuration types: `NONE`, `BIOMETRICS_ONLY`, `BIOMETRICS_OR_DEVICE_CREDENTIALS` and `SDK_PIN_WITH_BIOMETRICS_OPTIONAL`.

In all types apart from `NONE`, the LockScreen is displayed automatically whenever the Futurae SDK gets to the `FuturaeSDKStatus.Locked` status, and blocks the user from interacting with the app in any way unless he/she first unlocks it again.

---

## 🛠 Getting Started

We follow a consistent structure in our feature packages:
- A `Composable` file
- Potentially a `ComposableUIState` file (data or sealed class depending on the screen's complexity)
- An `/arch` folder containing architectural components (e.g., ViewModels)
- Potentially a `/usecase` folder containing the use cases used by this feature

In this case:
- `LockScreen` is the main composable responsible for displaying the appropriate UI elements based on the lock configuration type and allowing the user to proceed with unlocking or setting up the SDK PIN for the first time on `SDK_PIN_WITH_BIOMETRICS_OPTIONAL`.
- `LockScreenViewModel` handles the lock screen UI state and business logic. It exposes events and state flows to determine whether the user unlocked successfully or not and to initiate system authentication (e.g., biometrics).

---

## 🔍 Flow Overview

This screen is displayed when:
- The Futurae SDK transitions to `FuturaeSDKStatus.Locked`
- The user attempts to enroll an account for the first time with `SDK_PIN_WITH_BIOMETRICS_OPTIONAL` configuration
- The user wants to change the SDK PIN they have set up in the past
- The user wants to activate biometrics to unlock the SDK on `SDK_PIN_WITH_BIOMETRICS_OPTIONAL`

Once verification is complete, the screen communicates the result to the caller and returns to the previous screen to continue the original operation or allow the user to interact with the app again after unlocking.

A [set of SDK methods](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#sdk-protected-functions) are considered sensitive and are protected. They cannot be used unless the SDK is in the `FuturaeSDKStatus.Unlocked` state.
Always ensure that the user is prompted to unlock the SDK, based on the selected `lockConfigurationType`. The SDK can be unlocked by calling `LockApi.unlock`. This will prevent `FTUnlockRequiredException` when attempting to call these methods.

---
