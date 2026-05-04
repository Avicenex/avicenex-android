# Avicenex AI Android

Native Jetpack Compose MVP for Avicenex AI mobile workflows.

The repo was empty when scaffolded, so this project provides an Android app structure with:

- Pre-bill review queue
- Claim detail with calculated risk panel
- ICD-10-CM and CPT/HCPCS reference sections
- Workflow assistant entry points
- Full original web tool catalog: Chat, Coding Assistant, Pre-Bill Review, Code Lookup, Compare Codes, Cheat Sheet, Prior Auth Lookup, Denial Analyzer, Modifier Lookup, Appeal Letter, E/M Calculator, Batch Validator, CCI Edit Checker, CARC/RARC Lookup, Prior Auth Tracker, Claim Scrubber, Bookmarks, and Profiles
- Specialty/profile code sets

The bundled demo data is derived from the Avicenex web repo reference files. CPT/HCPCS labels remain Avicenex-authored plain-language content, and the compliance reminder stays visible in the review and assistant workflows.

## Verify

```sh
./gradlew test
./gradlew assembleDebug
```

## Run In Android Studio

1. Install Android Studio.
2. Open this repository folder.
3. Let Android Studio sync Gradle.
4. Select the `app` run configuration.
5. Choose an emulator or connected Android device.
6. Press Run.

For command-line builds on this machine, use JDK 17 and the installed Android SDK:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=/opt/homebrew/share/android-commandlinetools ./gradlew assembleDebug
```
