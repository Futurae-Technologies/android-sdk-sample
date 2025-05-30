# [QR Codes](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#qr-codes) & [QRCodeApi](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#qrcode-api)

This package demonstrates how to leverage the Futurae SDK capabilities to handle scanned QR codes issued by Futurae.

---

## 🛠 Getting Started

We follow a consistent structure in our feature packages:
- A `Composable` file.
- Potentially a `ComposableUIState` file (data or sealed class depending on the screen's complexity).
- An `/arch` folder containing architectural components (e.g., ViewModels).
- Potentially a `/usecase` folder containing the use cases used by this feature.

In this case:
- `QRScannedScreen` is the main composable responsible for allowing the user to scan a QR code and request the necessary camera permissions if needed.
- `QRScannerViewModel` is responsible for delegating the scanned QR code type detection to the Futurae SDK and triggering the appropriate flow based on the result.

---

## 🔍 Flow Overview

Futurae uses QR codes for different operations:
- **Enroll a device** using the SDK's `AccountApi` with the appropriate `EnrollmentParams`. In this case, the `ActivationCode` that is extracted from the parsed `QRCode`. (`QRCode.Enroll`)
- **Authenticate a user or session** using the SDK's `AuthApi.approve` with the appropriate `SessionIdentificationOption`. In this case, use `OnlineQR(qrCodeContent = qrCode.rawCode)`. (`QRCode.Online`)
  If you need to fetch the session details to display additional information to the user, use either `SessionApi.getSessionInfoWithoutUnlock` or `SessionApi.getSessionInfo` (depending on the respective feature flag), passing the `SessionInfoQuery` with the appropriate `SessionIdentifier` (in this case, `ByToken(QRCode.sessionToken)`).
- **Authenticate a user or session using offline QR codes** by calling `AuthApi.getOfflineQRVerificationCode` (`QRCode.Offline`) to get the verification code. (`QRCode.Offline`)
- **Authenticate a session not attached to a specific user** (`QRCode.Usernameless`). In this case, you must prompt the user to select which of the active accounts should be used for authentication, and then call `AuthApi.approve` with the appropriate `SessionIdentificationOption`, in this case `UsernamelessQR`. (`QRCode.Usernameless`)

The app allows users to scan QR codes using `androidx.camera`'s `PreviewView`.  
Once scanned, the QR code type detection is delegated to the Futurae SDK.  
Based on the detected QR code type, a different flow is initiated:

- If an **enrollment QR code** is scanned, the enrollment flow is initiated via the `EnrollmentRoute`.
- If an **authentication QR code** is scanned, the authentication flow is initiated via the `AuthenticationRoute`.
- If a **usernameless QR code** is scanned, the authentication flow is initiated after prompting the user to select one of the enrolled accounts.

---