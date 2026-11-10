# WiFi Security Toolkit (Android, No Root)

A native Kotlin Android starter app for nearby Wi-Fi scanning, basic advertised-security analysis, and PDF scan reports.

## Requirements
- Android Studio Ladybug (or newer)
- JDK 17
- Android SDK Platform 35
- Android device with Wi-Fi

## Build
1. Extract the ZIP.
2. Open the `WiFiSecurityToolkit` folder in Android Studio.
3. Allow Gradle sync to finish.
4. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Install `app/build/outputs/apk/debug/app-debug.apk` on your phone.

## Runtime notes
- Grant **Precise location** and **Nearby devices** permissions when requested.
- Turn on Android Location for nearby scan results; Android restricts Wi-Fi scan visibility.
- Tap Scan again after a few seconds if the first result is empty. Android throttles scan requests.
- PDF reports are saved in the app-specific external files directory.

## Current scope
- Nearby SSID/BSSID, signal level, frequency, and advertised capabilities.
- Basic WPA/WPA2/WPA3/WEP/WPS capability-string interpretation.
- PDF scan report.

The `NetworkTools` class includes local-network host discovery and user-supplied-host TCP port checks for integration into a future screen. These checks are not wired into the UI yet. Android's `isReachable()` behavior varies by device and network, so a missing response does not prove a device is offline.

## Android limitations
This no-root build uses public Android APIs. It does not enable monitor mode, capture WPA handshakes, deauthenticate clients, or recover Wi-Fi passwords. The security analyzer interprets what the access point advertises; it is not a full router penetration test.


## One-click cloud build with GitHub Actions
1. Create a new empty GitHub repository.
2. Upload the contents of this project folder to the repository root.
3. Open the repository's **Actions** tab and run **Build Android APK** (or push to `main`).
4. Open the completed workflow run and download the `WiFiSecurityToolkit-debug` artifact.
5. Extract the artifact to get `app-debug.apk`.
