# Futurae - Android SDK Demo App

This repository contains a full demo application showcasing the usage of the [Futurae SDK](https://github.com/Futurae-Technologies/android-sdk) and [Futurae Adaptive SDK](https://github.com/Futurae-Technologies/android-adaptive-sdk). It is an open-source app using all of our available features using the latest Android architectural and UI patterns. 

## Getting Started

1. **Clone the repository:**
    ```bash
    git clone git@github.com:Futurae-Technologies/android-sdk-demo.git
    ```

2. **Configuration:**
    * You need a valid `google-services.json` file from Firebase using the package-name `com.futurae.demoapp`
    * You need to define valid SDK credentials (ref [here](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/#sdk-credentials)). This project reads gradle properties: `SDK_ID`, `SDK_KEY` and `BASE_URL` and creates the respective string resources, read by `futurae.xml`
    * Refer to [Authenticating to Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-to-github-packages) in order to access the published Futurae SDK artifacts, to build the project. You can set `gradle.properties` for `GITHUB_ACTOR` & `GITHUB_TOKEN`.  

3. **Run the app:**
    Build and run the application on your device or emulator.

## Branches
This demo app is also used for development of our new SDK features. You can follow GIT Tags, to see the version of the demo corresponding to the SDK version you want to use. Use branch `/master`: stable & released versions of the demo. Every other branch is WIP. 

## Documentation

For detailed information about the SDK, please refer to our [Official Documentation](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk/).

## Contributing

Contributions are welcome! Please feel free to submit pull requests.

## License

This project is licensed under the [Apache License 2.0](License.txt).
