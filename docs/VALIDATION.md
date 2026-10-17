# Validation – Hello Photography 0.2.0 (2026-10-17)

The upgrade was built and tested in the existing checkout. Package ID, Room database/schema, original pose IDs and debug signing certificate are retained.

## Passed

- `./gradlew --offline --no-daemon assembleDebug testDebugUnitTest lintDebug` completed successfully after the final palette and manifest changes.
- 42 Android/JVM tests executed, zero failures/errors/skips. Coverage includes real Compose Activity navigation, mandatory role selection, persisted role after recreation, search/detail/favorite flow, Profile role changes and confirmed deletion, Amharic welcome, Room reopen/legacy data preservation, transactional free favorite limits, assistant matching, locale catalog compatibility, notification permissions/channel blocking/deep-link content, academy quiz gating and persistent progress.
- Five Node entitlement-policy tests passed. No real Google Play purchase or Firebase credential was used.
- Android lint: zero errors and one unsuppressed warning recommending KSP instead of kapt for Room.
- Catalog validation passed: 48 original pose descriptions across 24 categories. All 48 IDs have five translated Amharic text fields. Academy contains 9 courses and 27 bilingual lessons with valid quizzes.
- Android apksigner verified the debug signature; its public certificate matches the previously delivered APK, preserving update compatibility.
- APK metadata: com.posemaster.app, versionCode 2 / versionName 0.2.0, min API 26, target API 35, four native ABIs.
- A real headless Android 9 / API 28 x86_64 emulator accepted the APK (`adb install -r`: Success). The native welcome/role screen rendered, including Ethiopic text. A second `adb install -r` of the final APK also succeeded, and the previously selected photographer role remained persisted. Initial software-emulator boot produced System UI restarts/timeouts without KVM; this is not a physical-device performance benchmark. JVM UI flows completed separately.

## Test classes

- `com.posemaster.app.AcademyFlowTest`: 2 passed
- `com.posemaster.app.AcademyPersistenceTest`: 3 passed
- `com.posemaster.app.AppFlowTest`: 4 passed
- `com.posemaster.app.RoomPersistenceTest`: 1 passed
- `com.posemaster.app.UpgradeStorageTest`: 5 passed
- `com.posemaster.app.data.PoseTranslationsTest`: 4 passed
- `com.posemaster.app.domain.AcademyCatalogTest`: 2 passed
- `com.posemaster.app.domain.PoseAssistantTest`: 12 passed
- `com.posemaster.app.domain.PoseFilterTest`: 5 passed
- `com.posemaster.app.services.DailyInspirationTest`: 4 passed

## Artifact

`app/build/outputs/apk/debug/app-debug.apk` (22,176,099 bytes)

SHA256: `1c8df58ba6daf682bf9a1af28ac470e2a7ba12e3b59c0df63745d446976126f1`

Distribution copies: `downloads/HelloPhotography-debug.apk` and the backward-compatible `downloads/PoseMaster-debug.apk` URL. The source archive excludes build caches, credentials and private local configuration.

## Remaining production checks

No physical phone, Google Play internal-test installation, purchase/restore, live AdMob consent/ad delivery, Firebase account sync, remote content publishing or hosted owner policy URLs were validated. Subscription purchase launch/restoration, client/backend entitlement wiring and RTDN/refund refresh remain implementation/release gates; adding credentials alone does not finish them. AdMob/UMP test helpers are prepared, normal-screen ad requests are disabled and measurement initialization is delayed. AI recommendations are explicitly offline rules.

Complete release-device testing on supported Android versions, TalkBack/large text, background notification delivery, lifecycle/process death, performance and a network/Data Safety audit before public release. Have bilingual content reviewed by a native Amharic editor. See RELEASE.md and backend/README.md. No uploaded photos are used.
