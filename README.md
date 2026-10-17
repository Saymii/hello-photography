# PoseMaster – Posing Guide & Photo Ideas

Native Kotlin / Jetpack Compose / Material 3 Android application. Tagline: **Your Perfect Pose, Every Time.** This is a useful offline development release, not a production service launch.

## Install the Android app

Download [PoseMaster debug APK](https://github.com/Saymii/hello-photography/raw/refs/heads/main/downloads/PoseMaster-debug.apk) on your Android phone, then open it to install. Requires Android 8.0 or newer. Allow installation from your browser if prompted. This is a development build with placeholder illustrations and disabled live purchases/ads. The checksum is in `downloads/SHA256SUMS`.

## Run

Open this directory in Android Studio, select JDK 17 or 21, install Android SDK 35 and Build Tools 35.0.0, and let Gradle sync. Use an Android 8.0/API 26 or newer device. SDK path belongs in ignored `local.properties` (`sdk.dir=/your/android/sdk`) or `ANDROID_HOME`.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
python3 tools/validate_content.py
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The generated debug APK is copied into `artifacts/posemaster-debug.apk` when built in this workspace. No Firebase account, API key, camera permission, or login is needed.

## Included

- Five tabs, magazine-inspired charcoal/cream/gold theme, system/light/dark modes, three-step onboarding, guest access, saved interests.
- 48 original pose descriptions across all 24 requested categories; detailed body, head, hand, foot, angle, lighting and coaching guidance; previous/next and system text sharing.
- Search and offline structured subject/style/location/position/experience filters. Exact matching can return zero results with a small catalog; reset controls are supplied. These are deterministic filters, not AI.
- Daily featured guide, interest recommendations, beginner edit and premium previews. Trending data is not fabricated; no analytics service is connected.
- Room favorites, named collections, persistent offline bookmarks. All sample guides and placeholder illustrations are bundled; no photograph download is claimed.
- Eleven photography lessons and a composition illustration. Camera Guide is educational; no camera capture controls or camera permission.
- Billing catalog adapter, verification interface, test AdMob IDs and UMP/adaptive banner helper. No normal-screen ad requests or purchasing are enabled.

## Structure

`domain/`: models and deterministic filtering; `data/`: bundled catalog and Room repository; `ui/`: lifecycle-aware ViewModel; `MainActivity.kt`: Compose navigation and screens; `services/`: optional integration boundaries. Assets are original JSON copy. Reference art is a clearly marked original stick-figure placeholder, not a realistic pose photo. Coil is available for future licensed photo assets, but the sample library needs no remote image service.

## Delivery and service status

Phases 1–2 provide the offline app. Phase 3 supplies service interfaces and draft Firestore authorization rules, a purchase-verification server and privileged catalog CLI, **not implemented cloud sync, login, admin UI, RTDN processing, or a deployed backend**. Phase 4 supplies compile-checked SDK helpers; native/rewarded ad rendering, purchase flow, restoration and verification still require implementation and end-to-end testing. Phase 5 includes local build/test/lint evidence in `docs/VALIDATION.md`; device acceptance and Play release remain required.

See [release and services](docs/RELEASE.md), [content management](docs/CONTENT.md), and [validation](docs/VALIDATION.md). No credentials are requested in chat. Never put service-account keys in the APK.
