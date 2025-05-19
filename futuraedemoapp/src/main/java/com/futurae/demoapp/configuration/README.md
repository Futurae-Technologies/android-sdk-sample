# [SDK Configuration](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#initialize-the-sdk)

This package demonstrates how to configure the **Futurae SDK** based on your app's needs.

In a real-world app, SDK configuration should be handled behind the scenes, using the setup that fits your use case. However, for the purposes of this sample app, we expose a configuration screen to let users select which SDK configuration to apply.

Additionally, this package showcases how an app can **switch SDK configurations**. For instance, if your app initially uses configuration `A` and later needs to switch to configuration `B` in a new release, this package illustrates how to handle that change.

---

## 🛠 Getting Started

- `ConfigurationScreenRoute` and `InitialConfigurationScreenRoute` are the main composables managing SDK configuration.
   - `InitialConfigurationScreenRoute` is used during the initial app launch to allow the user to select the desired SDK configuration.
   - `ConfigurationScreen` is used via the app settings if the user decides to switch configurations later.
   - `InitialConfigurationScreenRoute` is essentially a wrapper around `ConfigurationScreen`.

- `ConfigurationViewModel` manages the UI state and handles the business logic:
   - Refreshes the list of configuration options.
   - Initializes the SDK or switches its configuration depending on where the screen was accessed from.

---

## 🔍 Flow Overview

The UI presented in this package is for demo purposes only. In your own app, SDK configuration and changes should be handled in the background.

Here’s what this screen allows:

- The user can select configuration options for the SDK, including:
  - One of the supported **lock configuration types**
  - SDK **unlock duration**
  - Whether **adding a new biometric credential** invalidates biometric authentication
  - Whether **device unlock** is required for cryptographic operations
  - Whether the SDK should not store cryptographic material on the device’s **StrongBox**.
 - Whether the **SDK PIN** can be changed using biometrics

This is designed to give developers insight into how SDK configuration is handled and how it can be modified dynamically when needed.

To generate the SDK configuration appropriate for your app's needs, use `SDKConfiguration.Builder()` and configure it using the exposed builder functions:
  - `setLockConfigurationType(lockConfigurationType: LockConfigurationType)`
  - `setUnlockDuration(duration: Int)`
  - `setInvalidatedByBiometricChange(invalidatedByBiometricChange: Boolean)`
  - `setUnlockedDeviceRequired(unlockedDeviceRequired: Boolean)`
  - `setSkipHardwareSecurity(skipHardwareSecurity: Boolean)`
  - `setAllowChangePinCodeWithBiometricUnlock(allowChangePinCodeWithBiometricUnlock: Boolean)`

Available `LockConfigurationType` are the following:
  - `NONE`
  - `BIOMETRICS_ONLY`
  - `BIOMETRICS_OR_DEVICE_CREDENTIALS`
  - `SDK_PIN_WITH_BIOMETRICS_OPTIONAL`

After completing the configuration, you can call the builder's `build` method to obtain the resulting `SDKConfiguration`.
Then, launch the Futurae SDK by calling `FuturaeSDK.launch` and passing your generated `SDKConfiguration`.

### [Switch Configuration](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#switch-sdk-configuration)

If you need to switch to a different SDK configuration in a new app version, follow these steps:
- Launch the SDK using the existing `SDKConfiguration` by calling `FuturaeSDK.launch(existingSDKConfiguration)`.
- Switch to the new SDK configuration by leveraging `LockApi.switchToLockConfiguration`, passing the desired configuration wrapped in the appropriate `SwitchTargetLockConfiguration`.
- Subsequent launches should use the new `SDKConfiguration`.