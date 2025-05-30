# Accounts

This package demonstrates how to display the list of enrolled accounts using the Futurae SDK, and how a user can interact with them.

---

## 🛠 Getting Started

- `AccountsScreen` is the main composable responsible for displaying either a blank slate (when no accounts are enrolled yet) or a list of enrolled accounts with the appropriate UI elements based on each account's status.
- `AccountsScreenUIState` and `AccountRowUIState` are data classes representing the current state of the enrolled accounts, allowing the composable to construct the UI accordingly.
- `AccountsViewModel` handles account retrieval and fetching of the respective TOTPs. It also performs an account migration check.

---

## 🔍 Flow Overview

An account displayed in the list can either be active or locked out (`FTAccount.lockedOut`).  
If locked out, a warning icon is shown on the right side of the row; otherwise, the TOTP is displayed in the same position.  
TOTP is fetched using `AuthApi.getTOTP` for each enrolled user.

A countdown bar at the top of the screen indicates the shortest TOTP expiration time among all accounts.  
TOTPs are refreshed automatically when the countdown ends.

Users can:
- **Long-press an account** to delete it. To unenroll a user, trigger `AccountApi.logoutAccount`.
- **Long-press an active account** to copy its HOTP. HOTP is generated using `AuthApi.getNextSynchronousAuthToken`.
- **Tap an account** to navigate to the history screen, where past authentications and transaction confirmations are displayed with their statuses. These can be retrieved from `AccountApi.getAccountHistory`.

Additionally, if accounts eligible for migration are detected, an account migration banner is displayed at the top of the screen.  
See more in the [Account Recovery README](https://github.com/Futurae-Technologies/android-sdk-sample/blob/d4792902f9682c6a15bd0e8be37bfe69aeeb6bd5/futuraeSampleApp/src/main/java/com/futurae/sampleapp/accountsrecovery/README.md).
