# [SDK Recovery](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#sdk-initialization-errors--recovery)

This package demonstrates how to perform SDK recovery in case it is needed due to an `FTCorruptedStateException` during initialization.

---

## 🛠 Getting Started

- `SDKRecoveryFlow` is the main composable informing the user about the corrupted state and the result (success or failure) of the recovery attempt.
- `ActivationRecoveryUseCase` attempts to perform SDK recovery via `launchAccountRecovery`.
- `ActivationRecoveryViewModel` handles the connection between UI and use case, considering the selected lock configuration.

Whenever an SDK API throws an `FTCorruptedStateException` or you receive it within an `onError` callback, it means that the SDK is in a corrupted state, such as missing necessary cryptographic material.
In this case, you can either call `FuturaeSDK.reset` to reset your SDK installation (this means all enrolled accounts will be lost and cannot be recovered), or call `FuturaeSDK.launchAccountRecovery` to recover accounts if the SDK's cryptographic material is still present.
To check whether account recovery is possible, inspect the `isRecoverable` attribute of `FTCorruptedStateException`.
---
