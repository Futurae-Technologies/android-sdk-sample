# [Enrollment](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#enroll)

This package demonstrates how to enroll the SDK as a device for a Futurae User.  
There are three enrollment options:
- via an **activation QR code**
- via an **activation shortcode**
- via an **activation URI**

The enrollment flow can either be **user-driven** (e.g., scanning a QR code or manually entering a shortcode), or **silent**, by securely communicating an enrollment code to the app and calling the SDK’s `FuturaeAccountApi.enrollAccount` or `FuturaeAccountApi.enrollAndGetAccount` method behind the scenes.

This package showcases enrollment for all available use cases, depicted by the sealed class `EnrollmentCase`.

---

## 🛠 Getting Started

As with other packages, we follow a consistent structure:
- A `Composable` file
- Optionally a `ComposableUIState` file (data class or sealed class, depending on screen complexity)
- An `/arch` folder containing architectural components (e.g., ViewModels)

In this case:
- `EnrollmentRouteComposable` is the main composable that manages user enrollment, given an `EnrollmentInput`.
- `EnrollmentViewModel` manages the UI state and handles the business logic.

---

## 🔍 Flow Overview

The enrollment flow is triggered from another screen or composable:
- By scanning a QR code, where the associated ViewModel detects it is an enroll-type QR.
- By manually entering a shortcode.
- By opening a URI (e.g., via deeplink or intent), which is detected by the root `FuturaeViewModel`.

In all cases, the flow is delegated to the `EnrollmentRouteComposable` and its `EnrollmentViewModel`.

The `EnrollmentViewModel` is responsible for identifying the necessary steps to complete the enrollment:
- If the associated service requires a **flow binding token**, the user must provide it before proceeding.
- If the SDK is launched with a configuration that requires a lock type like `SDK_PIN_WITH_BIOMETRICS_OPTIONAL`, and this is the first account being enrolled, the user will also be prompted to set up their **SDK PIN**.

The SDK PIN setup is delegated to the `LockScreen` and `LockScreenViewModel`.  
_👉 [See LockScreen README](../lock/README.md)._

Once all necessary input is gathered, enrollment proceeds via `FuturaeAccountApi.enrollAndGetAccount`.

Upon successful enrollment, the UI displays a success message and allows the user to return to the list of enrolled accounts.
