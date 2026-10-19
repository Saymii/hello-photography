# Validation – Hello Photography

## AI photo library 1.0.1 (2026-10-19)

Built the native Android app with version **1.0.1 / code 4**, the same
`com.saymii.hellophotography` identity and upload certificate, target API 36,
and minimum API 26. This update contains **39 real supplied AI references**:
12 male, 10 female, 8 couple, 1 group and 8 casual/selfie images. Female 11–12
and seven group photos are missing; a successful 48-image check is not claimed.
The original 48 illustrated guide IDs, database and preferences remain intact.

- `python3 tools/import_pose_photos.py --check` and
  `python3 tools/validate_content.py` passed. All 39 original SHA-256 values
  remain unchanged outside Git. Derived WebP copies preserve aspect ratio at
  853 × 1280, strip metadata and total 1,892,984 bytes versus 2,478,888 original
  bytes (23.6% smaller). Repeated import produced identical public outputs.
- The full signed command `assembleDebug testDebugUnitTest lintDebug
  bundleRelease assembleRelease lintRelease` completed in 3m 41s with offline
  Gradle, using private signing settings outside Git. **58 tests passed**,
  zero failures, errors or skips; full-run XML timestamps 16:34:40–16:34:59 UTC.
  Results were retained locally under `artifacts/photo-validation/full-test-results/`.
- New tests decode **every one of the 39 bundled WebPs through real Coil**,
  verify nonblank photographic pixels/dimensions and disk data source, forbid
  network access, exercise category/title search, preserve the original 48
  guides, and check full-screen image accessibility, zoom/reset, next image,
  close and underlying guide context. Existing app, learning and storage tests
  remain passing. Lint reports zero errors and 14 advisory warnings per variant.
- The screenshot test was subsequently rerun successfully in 33s after adding
  asynchronous photo-decode waits. Seven real Activity renders were refreshed
  in `store-listing/screenshots/phone/`, including an actual AI photo guide;
  provenance and checksums accompany them. These are native Robolectric API 34
  renders, distinct from the installed release APK check below.
- Both the signed APK and AAB contain the exact 39 optimized images and catalog,
  verified against `docs/photo-assets.json`. Neither contains private originals
  or signing settings. `apksigner` verified the APK; `jarsigner` verified the
  AAB (expected self-signed upload-certificate warnings); bundletool 1.18.1
  validated the bundle. APK ZIP alignment passed `zipalign -c -P 16 4`.
- The exact signed APK installed as an update on the API 28 Android emulator.
  The saved Photographer role survived, and Home displayed a decoded photo.
  A fresh launch of photo 1001 showed its real image; full-screen 1/39, zoom
  100% → 150% → reset, next photo 2/39, and close back to the original guide
  passed through Android UI automation. Repeated UI dumps waited for the actual
  rendered state on this software emulator. Captures are
  `docs/device-validation/photo-guide-api28.png` and `photo-viewer-api28.png`.
  No app AndroidRuntime crash was logged. System UI showed a boot-time ANR
  before stabilizing; no KVM was available, so this is not a performance test.
- Isolated importer checks reject stale output assets and duplicate originals
  before changing catalog/manifest/image outputs. The private source originals
  were rehashed after verification and remained unchanged.

Artifacts:

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| `release/HelloPhotography-1.0.1-test.apk` | 3,780,149 | `e68a891409f7f2615728f9fad3b46ded5a8ef4bddd507c274118085825cca2b4` |
| `release/HelloPhotography-1.0.1.aab` | 6,038,566 | `40b90055bdfd3356170d18be7eee3a29e809129055af8e1a40f5df0f3439e31c` |
| `artifacts/HelloPhotography-1.0.1-debug.apk` | 23,374,344 | `3a9aa8dd4917c36ca53180cca5de98257339de5cd01e5a9fefef892652c2fcb4` |

The installable release APK is built by `assembleRelease` from the same release
source and signed with the upload key; it is not a Play-delivered APK. The AAB
is an upload artifact, not an installed app or evidence of Play approval.
Purchases and remote AI recommendations remain unconfigured. Physical-device,
16 KB runtime and Play-internal-track checks remain before production submission.

## Photo collection preparation (2026-10-19, not a new release)

The app can now append a reviewed local `photo-poses.json` catalog, with Coil
asset loading, full-frame references, credits, image-rights text and an explicit
illustration fallback. Original IDs 1–48 and Room storage retain their meaning.
The new 48-photo set reserves IDs 1001–1048; five originals numbered 44–48 and
five draft descriptions are staged outside Git. No new owner photo or optional
photo catalog is bundled yet. Remaining files 01–43, rights/credit confirmation
and completed content review are pending. The checked-in 1.0.0 release artifacts
below remain unchanged and do not include this collection.

Validation of the preparation source:

- `./gradlew --offline --no-daemon assembleDebug testDebugUnitTest lintDebug`
  succeeded in 44s. **54 tests passed**, zero failures/errors/skips; current test
  XML timestamps 14:12:50–14:13:06 UTC. This includes the existing 43 tests,
  nine optional-catalog/metadata/path/ID tests and two real Coil image tests.
- Renderer tests decode an original plain-color fixture through the actual Coil
  asset fetcher/decoder and verify bitmap dimensions/pixel, disk data source,
  post-decode success label, accessibility text, missing-image fallback and zero
  network requests. The fixture is debug-only, unrelated to owner photographs.
  Test waits drain Robolectric's paused main looper for asynchronous callbacks.
- Lint reports no errors and 14 advisory warnings. Catalog validation passes all
  48 original guides across 24 categories. CLI photo validation was checked with
  a valid local fixture and rejection of ID collisions, missing rights metadata,
  missing images and remote paths.
- All five staged files are valid 853 × 1280 RGB JPEGs. Their copied bytes match
  recorded SHA-256 values. Repeated intake stays at five unique slots; conflicting
  replacement content and staging inside the Git checkout are rejected.
- No new signed AAB or public photo-containing APK has been produced. Prepare
  those after the complete approved collection and updated release copy are ready.

## Play release 1.0.0 (2026-10-17)

Built the actual native app in the existing checkout with application ID
`com.saymii.hellophotography`, version `1.0.0` / code `3`, minimum API 26,
compile/target API 36, Build Tools 36.0.0, AGP 8.10.1, Gradle 8.11.1,
JDK 21 and Billing 9.1.0. Debug identity is
`com.saymii.hellophotography.debug` / `1.0.0-dev`.

### Passed on the final source

- Signed build: `assembleDebug testDebugUnitTest lintDebug bundleRelease lintRelease`,
  with upload credentials supplied privately through `tools/with-upload-signing.py`.
  Final run completed successfully in 2m 33s.
- **43 Android/JVM tests**, zero failures/errors/skips, current XML timestamps
  20:54:56–20:55:23 UTC. Includes all 42 prior functional/storage tests and the
  six-screen actual Activity screenshot flow. The screenshot harness uses native
  Android Canvas with Robolectric API 34; it does not exercise the minified APK.
- **Five backend entitlement-policy tests** passed. These are policy unit tests,
  without a live Play API, Firebase account, or deployed service.
- Content validation: 48 original guides across 24 categories, all 48 translated
  Amharic records. Four premium-labeled sample guides are free previews; premium
  Academy lessons stay locked. No chat-uploaded photographs are used.
- Debug and release lint: **zero errors, 14 advisory warnings each**. Warnings
  cover a newer debug-only Ads SDK, KSP/ktx recommendations, an unnecessary v26
  resource qualifier and unused legacy icon. None were suppressed. Gradle also
  reports future Gradle 9 deprecations; this project pins Gradle 8.11.1.
- The no-credentials signing guard failed as expected and named the four missing
  signing variables; it did not produce an unsigned release.
- `bundletool` 1.18.1 validated the final AAB. `jarsigner` reports `jar verified`;
  the self-signed upload certificate/absent timestamp warnings are expected for
  this private upload key. The certificate matches the public PEM below.
- AAB manifest: correct package/version/API, non-debuggable, backups disabled,
  Billing version 9.1.0, no ad provider or advertising-ID permission. Final release
  runtime dependency report excludes AdMob, UMP, Firebase Analytics and Crashlytics.
  Billing's shared Google transport/base libraries remain; this is not a network
  traffic audit or a blanket no-data-collection declaration.
- A universal APK was generated **from this final AAB**, signed with the upload
  key, and verified by Android `apksigner` (v2/v3 signatures). `aapt2` confirms
  package/version, min API 26 and target 36, with no debuggable flag.
- AAB config declares `PAGE_ALIGNMENT_16K`; all four native
  `libandroidx.graphics.path.so` ABIs have every ELF LOAD aligned to `0x4000`.
  Libraries are stored uncompressed in the generated APK; `zipalign -c -P 16 -v 4`
  passes. These are structural checks, not a 16 KB runtime test.
- Six reviewed actual UI renders, 824 × 1600 opaque RGB PNG, are in
  `store-listing/screenshots/phone/`; checksums/provenance accompany them. Icon is
  original 512 × 512 RGBA (fully opaque); feature graphic is original 1024 × 500 RGB.

### Final artifacts

| File | Bytes | SHA-256 |
| --- | ---: | --- |
| `release/HelloPhotography-1.0.0.aab` | 3,638,220 | `c394265608f3081c2d3e107aca3923e4593138362630fbc223bb8885d356e984` |
| `release/HelloPhotography-1.0.0-test.apk` | 1,654,814 | `92906822d1202599e9b72e848a601592975158f2aeeafd1a9475613102a75921` |

Build output is also `app/build/outputs/bundle/release/app-release.aab`.
`release/SHA256SUMS` includes the public upload certificate file checksum.
Upload certificate: RSA 3072, SHA256withRSA, expires 2054-03-04.
Public certificate SHA-256:
`25:95:ED:38:96:E3:6C:CD:99:A6:58:B6:32:70:B1:95:7D:F7:8E:F3:EA:F3:0A:37:F8:82:58:A5:AB:FB:A4:53`.
The private key/password backup is outside the repository and must never be added
or uploaded as a public release asset. A source checkout does not contain it.

### Native release smoke test

A real Android 9 / API 28 x86_64 emulator ran without KVM. The installed
`base.apk` was pulled back from the emulator and its SHA-256 matched the final
delivered APK exactly (`92906822…02a75921`). Android `am start -W` reported
`Status: ok` for the release Activity; the welcome screen rendered and the
Photographer selection and Continue responded, and a pose detail (Clean backdrop)
opened from the personalized home. After force-stop/relaunch, the Photographer
role remained selected and its personalized home rendered. No app fatal exception appeared in the
AndroidRuntime log inspected after launch.

Actual release captures are retained in `docs/device-validation/` (welcome, personalized home and
pose detail), copied without pixel edits from ADB screencap.

Both ADB install attempts timed out during the slow software boot; the exact
installed-file hash establishes the outcome instead of claiming a successful
ADB exit. The emulator also displayed a System UI ANR during startup, which was
dismissed before checking the app. This is a limited release runtime smoke test,
not a physical-device performance benchmark, complete device test, or 16 KB test.

### Remaining release gates

No Google Play upload/acceptance, public privacy-policy hosting, real purchase,
cloud account, production ad, physical-device test, modern Android/16 KB runtime
or Play pre-launch report has been completed. Current policy hosts were blocked
by the environment proxy; verify current Console deadlines and account-specific
requirements using [PLAY_REQUIREMENTS.md](PLAY_REQUIREMENTS.md).

The software-only API 28 emulator is slow and is not a performance benchmark.
The limited native release smoke test above remains separate from the JVM
tests and historical debug results. Capture release screenshots from a Play internal-track
installation before production submission. Review large text/TalkBack, rotation,
notification delivery/denial, process death, offline persistence and network/Data
Safety behavior on supported devices. Have Amharic content reviewed by an editor.

New Play/debug packages install separately from legacy `com.posemaster.app`.
Legacy data stays in that installation; no automatic package migration is claimed.
Purchases remain disabled; client purchase/restore and verified entitlement wiring,
backend deployment and RTDN/refund handling remain implementation/release gates.

## Historical validation – Hello Photography 0.2.0 (2026-10-17)

The 0.2.0 upgrade was built and tested in the existing checkout. At that time,
package ID `com.posemaster.app`, Room database/schema, original pose IDs and the
debug signing certificate were retained. The checks and artifacts in this
historical section refer to that version only.

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
