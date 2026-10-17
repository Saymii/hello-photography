# Hello Photography

A native Android photography and posing guide built with **Kotlin, Jetpack Compose, Material 3, MVVM, repositories and Room**. This upgrade improves the existing project in place. The application ID `com.posemaster.app`, Room database `posemaster.db`, favorite IDs and original preferences file remain unchanged so existing installations keep their data.

## Install and run

Requires Android 8.0 / API 26 or newer. Open this project in Android Studio with JDK 17 or 21, SDK 35 and Build Tools 35.0.0. On the managed cloud machine, first run `source tools/cloud-env.sh`.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
python3 tools/validate_content.py
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

APK output: **`app/build/outputs/apk/debug/app-debug.apk`**. The checked-in distribution copy is `downloads/HelloPhotography-debug.apk`; its SHA256 is in `downloads/SHA256SUMS`. On a phone, open the APK with Files and allow installation from that source if requested. For updates, use the same signing key. If installation fails, retain your data and check the exact Android installer/ADB error, OS version, file size and signing certificate before uninstalling. The app requires API 26+ and includes arm64-v8a, armeabi-v7a, x86 and x86_64 libraries.

Cloud setup can be refreshed with `bash tools/cloud-install.sh`. New machines restored from the environment snapshot still need the environment published in the product; source commits do not publish it.

## Working features

- Required first-launch role selection: Photographer, Model or Just for Me. Roles persist locally, personalize recommendations and can be changed in Profile. New role onboarding preserves old themes and favorites.
- Five tabs: Home, Explore Poses, Learn, Favorites and Profile. Home has personalized greetings, search, daily pose, curated ideas, role categories, courses, continue-learning links, recent history and favorite count. "Trending" is clearly an editorial selection, not invented live analytics.
- 48 original sample pose guides across 24 categories; search and structured filters; detailed body/head/hands/feet/angle/light/mistake advice; original posture-aware illustrations, sharing and previous/next navigation.
- **No photos supplied in chat are used, including as inspiration.** The entire visual library uses clearly labeled original Canvas artwork with seated, walking, standing, close-up, group and mobility-aware compositions. No scraped photos or broken remote placeholders.
- AI Pose Assistant with useful deterministic **offline rules**, transparent explanations, position/location/occasion/style/clothing matching and honest no-match states. No model or remote API is configured.
- Photography Academy: nine courses, original lessons, quizzes, local completion and resume. Advanced premium courses stay locked; free lessons remain usable offline.
- Room favorites and named collections, offline bookmark filter, 20-favorite free limit. Existing collections above the new limit remain intact and editable. All bundled sample guides/illustrations are already offline; no photo-download claim.
- English and Amharic navigation, settings, all 48 core pose descriptions and academy content. Some canonical filter choices, rule-match explanations and technical photography vocabulary remain English. Translations should receive a native-speaker editorial review before public release.
- System/light/dark themes, local recent history, opt-in daily local notifications, bilingual privacy/terms pages, feedback/rating via Android sharing, real GitHub support link and confirmed local-data deletion.

## Services and monetization

The subscription page has selectable monthly/yearly plans. Optional Gradle properties `HELLO_MONTHLY_PRODUCT` and `HELLO_ANNUAL_PRODUCT` supply Play product IDs; prices come from Google Play ProductDetails, not hardcoded amounts. **Purchasing remains disabled** until authenticated purchase launch/restoration, client/server wiring, verified entitlements and RTDN/refund handling are implemented and accepted. No local boolean unlocks premium content.

Google Play Billing and AdMob/UMP SDK helpers, Google's development test ad IDs, server-side verification reference, privileged catalog/image upload tools, and draft Firestore/Storage authorization rules are retained. Normal screens do not request ads. Firebase accounts/sync and remote catalog delivery remain optional, unconfigured development work. No credential, AI key, live payment or deployed backend is invented.

Read [release and service setup](docs/RELEASE.md), [content management](docs/CONTENT.md), [upgrade notes](docs/UPGRADE.md), and [validation evidence](docs/VALIDATION.md). Server instructions are in [backend/README.md](backend/README.md).

## Source layout

- `ui/`: role welcome, personalized dashboard/navigation, reusable components, original pose artwork, assistant, academy, profile and subscription screens.
- `domain/`: pose filtering/recommendations and academy curriculum/quiz policy.
- `data/`: existing Room repository, locally persisted role/preferences/history/progress and cached translations.
- `services/`: optional Billing/AdMob adapters and opt-in local reminder receiver.
- `assets/`: original English and Amharic pose text; no user images.
- `backend/`: separate optional Node/Firebase/Play verification and authorized draft-content tools.
