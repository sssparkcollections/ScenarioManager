# ScenarioOS Android project

This project wraps the existing ScenarioOS trading journal HTML in an Android WebView app.

## Build an APK
1. Install Android Studio on a computer.
2. Open this folder (`ScenarioOS_Android`) in Android Studio.
3. Allow Gradle sync to finish and install Android SDK Platform 35 if prompted.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio will show the APK location, normally `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer that APK to your Android phone and install it. You may need to allow installation from that file manager.

## Included
- Existing ScenarioOS HTML and its login screen.
- WebView app shell with local browser storage enabled.
- File picker support for importing backups and attaching images.
- Native Save dialog for JSON/CSV exports.
- No internet permission is requested; app data is stored locally on the device.

## Important
This is the Android Studio source project, not a precompiled APK. An Android SDK/Gradle build environment is required to produce the APK. Keep exporting backups regularly because local app data can be lost if the app is uninstalled or app storage is cleared.
