# Hello Photography

A native Android photography and posing guide built with **Kotlin, Jetpack Compose, Material 3, MVVM, repositories and Room**. This release improves the existing source project and prepares a new Google Play application, **`com.saymii.hellophotography`**, version **1.0.0 / code 3**. The code namespace, Room database `posemaster.db`, pose IDs and local storage formats are retained.

The earlier distributed app used `com.posemaster.app`. The new Play identity and the development identity `com.saymii.hellophotography.debug` install separately; Android does not automatically copy private favorites or progress between packages. Data remains in the legacy installation. Do not uninstall it expecting an automatic migration. Subsequent updates of the same new package preserve its local data when signed appropriately.

## New photo collection in preparation

The owner is preparing a new set of 48 photos (19 October 2026). Twenty-seven have
been received: male 01–12, female 01–10 and the earlier numbered 44–48. They are
staged outside Git. The app now supports optional
bundled photo references through Coil, full-frame display, credits and illustration
fallbacks. Existing guide IDs and favorites retain their meaning. See
[the collection workflow](docs/PHOTO_COLLECTION.md) for the remaining batch,
rights metadata and import steps. No new photos are included in the checked-in
1.0.0 APK/AAB; rebuild those after the approved collection is complete.

## Install and run

Requires Android 8.0 / API 26 or newer. Open this project in Android Studio with JDK 17 or 21, **SDK 36 and Build Tools 36.0.0**. Compile and target SDK are 36. Use the pinned **Android Gradle Plugin 8.10.1 / Gradle 8.11.1** wrapper. On the managed cloud machine, first run `source tools/cloud-env.sh`.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
python3 tools/validate_content.py
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Debug APK output: **`app/build/outputs/apk/debug/app-debug.apk`**, package `com.saymii.hellophotography.debug`. The checked-in `downloads/HelloPhotography-debug.apk` and its `downloads/SHA256SUMS` may describe the historical 0.2.0 app until explicitly refreshed; consult [validation evidence](docs/VALIDATION.md). On a phone, open the desired APK with Files and allow installation from that source if requested. An **AAB cannot be installed directly**. For updates, use the same package and signing key. If installation fails, retain your data and check the exact Android installer/ADB error, OS version, file size and signing certificate before uninstalling. The app includes arm64-v8a, armeabi-v7a, x86 and x86_64 libraries.

Signed release AAB output: **`app/build/outputs/bundle/release/app-release.aab`**; delivery copy: **`release/HelloPhotography-1.0.0.aab`**. Release builds require the owner's private upload-signing variables and intentionally fail when they are absent. See [build, sign, test and upload instructions](docs/PLAY_PUBLISHING_STEPS.md). A signed upload artifact is not proof of Google Play acceptance. New build/test results are recorded in [docs/VALIDATION.md](docs/VALIDATION.md).

Cloud setup can be refreshed with `bash tools/cloud-install.sh`. New machines restored from the environment snapshot still need the environment published in the product; source commits do not publish it.

## Working features

- Required first-launch role selection: **Photographers, Models or Everyone**. Roles persist locally, personalize recommendations and can be changed in Profile.
- Five tabs: Home, Explore Poses, Learn, Favorites and Profile. Home has personalized greetings, search, daily pose, curated ideas, role categories, courses, continue-learning links, recent history and favorite count. "Trending" is clearly an editorial selection, not invented live analytics.
- 48 original sample pose guides across 24 categories; search and structured filters; detailed body/head/hands/feet/angle/light/mistake advice; original posture-aware illustrations, sharing and previous/next navigation.
- **No photos supplied in chat are used, including as inspiration.** The entire visual library uses clearly labeled original Canvas artwork with seated, walking, standing, close-up, group and mobility-aware compositions. No scraped photos or broken remote placeholders.
- AI Pose Assistant with useful deterministic **offline rules**, transparent explanations, position/location/occasion/style/clothing matching and honest no-match states. No model or remote API is configured.
- Photography Academy: nine courses, original lessons, quizzes, local completion and resume. Advanced premium courses stay locked; free lessons remain usable offline.
- Room favorites and named collections, offline bookmark filter, 20-favorite free limit. Collections above the limit remain intact and editable within the same installation. All bundled sample guides/illustrations are already offline; no photo-download claim.
- English and Amharic navigation, settings, all 48 core pose descriptions and academy content. Some canonical filter choices, rule-match explanations and technical photography vocabulary remain English. Translations should receive a native-speaker editorial review before public release.
- System/light/dark themes, local recent history, opt-in daily local notifications, bilingual privacy/terms pages, feedback/rating via Android sharing, real GitHub support link and confirmed local-data deletion.

## Services and monetization

The subscription page has selectable monthly/yearly plans and a **Google Play Billing 9.1.0** Java-artifact catalog adapter. Optional Gradle properties `HELLO_MONTHLY_PRODUCT` and `HELLO_ANNUAL_PRODUCT` supply Play subscription product IDs; prices come from Google Play ProductDetails, not hardcoded amounts. **Purchasing remains disabled** until authenticated purchase launch/restoration, client/server wiring, verified entitlements and RTDN/refund handling are implemented and accepted. No local boolean unlocks premium content.

The **release variant excludes AdMob and UMP dependencies and the advertising-ID permission**. AdMob/UMP helpers and Google's test ad IDs are confined to debug builds; normal screens do not request ads. Server-side verification references, privileged catalog/image upload tools and draft Firestore/Storage authorization rules remain separate optional work. Firebase accounts/sync and remote catalog delivery are unconfigured. No credential, AI key, live payment or deployed backend is invented. Re-audit privacy and Data Safety before enabling any service.

Read [release and service setup](docs/RELEASE.md), [step-by-step Play publishing](docs/PLAY_PUBLISHING_STEPS.md), [policy requirements and verification limits](docs/PLAY_REQUIREMENTS.md), [store listing](docs/STORE_LISTING.md), [content management](docs/CONTENT.md), [upgrade notes](docs/UPGRADE.md), and [validation evidence](docs/VALIDATION.md). Server instructions are in [backend/README.md](backend/README.md). Publish and review the supplied privacy-policy draft at a real public URL before submission.

## Source layout

- `ui/`: role welcome, personalized dashboard/navigation, reusable components, original pose artwork, assistant, academy, profile and subscription screens.
- `domain/`: pose filtering/recommendations and academy curriculum/quiz policy.
- `data/`: existing Room repository, locally persisted role/preferences/history/progress and cached translations.
- `services/`: optional Billing/AdMob adapters and opt-in local reminder receiver.
- `assets/`: original English and Amharic pose text; no user images.
- `backend/`: separate optional Node/Firebase/Play verification and authorized draft-content tools.
