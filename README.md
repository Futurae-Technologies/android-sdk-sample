# 🚀 Futurae - Android SDK Demo App

This repository contains a full sample android application showcasing the usage of both [Futurae SDK](https://github.com/Futurae-Technologies/android-sdk) and [Futurae Adaptive SDK](https://github.com/Futurae-Technologies/android-adaptive-sdk). It is an open-source app using all of our available features using the latest Android architectural and UI patterns. 

## 🛠 Getting Started

1. **Clone the repository:**
    ```bash
    git clone git@github.com:Futurae-Technologies/android-sdk-sample.git
    ```

2. **Configuration:**
    * You need a valid `google-services.json` file from Firebase using the package-name `com.futurae.demoapp`
    * You need to define valid SDK credentials (ref [here](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/#sdk-credentials)). This project reads gradle properties: `SDK_ID`, `SDK_KEY` and `BASE_URL` and creates the respective string resources, read by `futurae.xml`
    * Refer to [Authenticating to Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) in order to access the published Futurae SDK artifacts, to build the project. You can set `gradle.properties` for `GITHUB_ACTOR` & `GITHUB_TOKEN`.  

3. **Run the app:**
    Build and run the application on your device or emulator.

## 🌿 Branches
This demo app is also used for development of our new SDK features. You can follow GIT Tags, to see the version of the sample app corresponding to the SDK version you want to use. Use branch `/master`: stable & released versions of the demo. Every other branch is WIP. 

## 🧩 Architecture

The project is organized primarily using a feature-package structure.  
Additionally, there are common `utils` and `ui` packages containing shared utilities and UI components used across different screens.

The structure within each feature package typically follows this approach:
- A `Composable` file.
- Optionally, a `ComposableUIState` file (either a data class or sealed class, depending on the screen's complexity).
- An `/arch` folder containing architectural components (e.g., ViewModels).
- Optionally, a `/usecase` folder containing the use cases relevant to this feature.

## 📚 Table of Contents

Throughout the project's packages, there are dedicated README.md files to assist navigation and provide clarifications for each feature or flow.

| Feature / Flow             | Description                                     | README                                                                                          |
|----------------------------|-------------------------------------------------|-------------------------------------------------------------------------------------------------|
| SDK Configuration          | Overview and usage of SDK configuration options | [SDK Configuration](futuraedemoapp/src/main/java/com/futurae/demoapp/configuration/README.md)   |
| Enrollment                 | Flows for enrolling the device                  | [Enrollment](futuraedemoapp/src/main/java/com/futurae/demoapp/enrollment/README.md)             |
| Account Management         | Active account list                             | [Account List](futuraedemoapp/src/main/java/com/futurae/demoapp/home/accounts/README.md)        |
| Manual Entry               | Enrolling using ShortCode                       | [Manual Entry](futuraedemoapp/src/main/java/com/futurae/demoapp/home/activationcode/README.md)  |
| QR Code Scanning           | Flows initiated via QR code scanning            | [QR Code Scanning](futuraedemoapp/src/main/java/com/futurae/demoapp/home/qrscanner/README.md)   |
| Lock SDK                   | Handling locking and unlocking of the SDK       | [Lock SDK](futuraedemoapp/src/main/java/com/futurae/demoapp/lock/README.md)                     |
| Automatic Account Recovery | Account recovery from previous installments     | [Account Recovery](futuraedemoapp/src/main/java/com/futurae/demoapp/accountsrecovery/README.md) |
| SDK Recovery               | Recover SDK from corrupt state                  | [SDK Recovery](futuraedemoapp/src/main/java/com/futurae/demoapp/recovery/README.md)             |

## 📄 Documentation

For detailed information about the SDK, please refer to our [Official Documentation](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📜 License

This project is licensed under the [Apache License 2.0](License.txt).
