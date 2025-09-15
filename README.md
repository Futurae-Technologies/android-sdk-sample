# 🚀 Futurae - Android SDK Sample App

This repository contains a full sample android application showcasing the usage of both [Futurae SDK](https://github.com/Futurae-Technologies/android-sdk) and [Futurae Adaptive SDK](https://github.com/Futurae-Technologies/android-adaptive-sdk). It is an open-source app using all of our available features using the latest Android architectural and UI patterns. 

## 📢 Disclaimer
The SDK Sample App in this repository is provided as is and is intended solely as an example implementation to assist customers in integrating Futurae’s SDKs. This SDK Sample App is not designed for production use, and Futurae does not offer support or maintenance for it. Futurae makes no representations or warranties, express or implied, including but not limited to, any warranties of merchantability or suitability, or fitness for a particular purpose, or non-infringement, regarding the SDK Sample App. Futurae does not warrant that the SDK Sample App will be uninterrupted or error free or without delay.

## 🛠 Getting Started

1. **Clone the repository:**
    ```bash
    git clone git@github.com:Futurae-Technologies/android-sdk-sample.git
    ```

2. **Configuration:**
    * You need a valid `google-services.json` file from Firebase using the package-name `com.futurae.sampleapp`
    * You need to define valid SDK credentials (ref [here](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/#sdk-credentials)). This project reads gradle properties: `SDK_ID`, `SDK_KEY` and `BASE_URL` and creates the respective string resources, read by `futurae.xml`
    * If you are using _Application Integrity Check Embedded into Enrollment and Authentication flow_, you need to enable the Integrity API on your Google Cloud project, and include your Cloud Project Number found on your Google Cloud Console as `CLOUD_PROJECT_NUMBER` gradle property to be able to use your quota during those embedded Integrity Verdict requests. This will also create a string resource, read by `futurae.xml`.
    * Refer to [Authenticating to Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) in order to access the published Futurae SDK artifacts, to build the project. You can set `gradle.properties` for `GITHUB_ACTOR` & `GITHUB_TOKEN`.  

3. **Run the app:**
    Build and run the application on your device or emulator.

## 🌿 Branches
This Sample app is also used for development of our new SDK features. The master branch represents the latest stable version. Development and feature branches may be unstable or WIP. 

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

| Feature / Flow             | Description                                     | README                                                                                            |
|----------------------------|-------------------------------------------------|---------------------------------------------------------------------------------------------------|
| SDK Configuration          | Overview and usage of SDK configuration options | [SDK Configuration](futuraeSampleApp/src/main/java/com/futurae/sampleapp/configuration/README.md) |
| Enrollment                 | Flows for enrolling the device                  | [Enrollment](futuraeSampleApp/src/main/java/com/futurae/sampleapp/enrollment/README.md)               |
| Account Management         | Active account list                             | [Account List](futuraeSampleApp/src/main/java/com/futurae/sampleapp/home/accounts/README.md)          |
| Manual Entry               | Enrolling using ShortCode                       | [Manual Entry](futuraeSampleApp/src/main/java/com/futurae/sampleapp/home/activationcode/README.md)    |
| QR Code Scanning           | Flows initiated via QR code scanning            | [QR Code Scanning](futuraeSampleApp/src/main/java/com/futurae/sampleapp/home/qrscanner/README.md)     |
| Lock SDK                   | Handling locking and unlocking of the SDK       | [Lock SDK](futuraeSampleApp/src/main/java/com/futurae/sampleapp/lock/README.md)                       |
| Automatic Account Recovery | Account recovery from previous installments     | [Account Recovery](futuraeSampleApp/src/main/java/com/futurae/sampleapp/accountsrecovery/README.md)   |
| SDK Recovery               | Recover SDK from corrupt state                  | [SDK Recovery](futuraeSampleApp/src/main/java/com/futurae/sampleapp/recovery/README.md)               |

## 📄 Documentation

For detailed information about the SDK, please refer to our [Official Documentation](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/).

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests.

## 📜 License

This project is licensed under the [Apache License 2.0](License.txt).
