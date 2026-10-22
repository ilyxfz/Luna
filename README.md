# Luna

Luna is an Android application built with Kotlin and Jetpack Compose.

## Requirements

- Android Studio with Android SDK Platform 35
- JDK 17

Open the project in Android Studio and allow Gradle to sync, or build from PowerShell with the Gradle wrapper:

```powershell
.\gradlew.bat assembleDebug
```

To build the release APK:

```powershell
.\gradlew.bat assembleRelease
```

The release APK is written to `app/build/outputs/apk/release/`. Release signing is not configured in this project.
