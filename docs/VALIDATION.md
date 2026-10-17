# Validation – 2026-10-17

## Passed in this cloud instance

- Native Android debug APK assembled with Gradle 8.10.2, Kotlin 1.9.25, AGP 8.7.3, JDK 21, SDK/Build Tools 35.
- Seven Android tests executed and passed: five structured-filter tests; one Robolectric Compose flow (scrollable onboarding, guest Explore, search, open details, save favorite, Favorites); one Room database reopen/persistence/removal test. No skipped tests.
- Android lint completed: zero errors, two warnings (explicit Android 12 data-extraction policy recommended; Room supports faster KSP instead of kapt). Warnings were not suppressed.
- Content validator: 48 original descriptions, 24 categories, stable unique IDs and required fields.
- Five Node entitlement-policy tests passed; no skipped tests.
- Backend started locally for a health request and rejected an unauthenticated purchase verification request with HTTP 401; the smoke-test process was stopped. No Firebase or Play API calls were validated.
- Backend production dependency audit reported zero known vulnerabilities after updating firebase-admin to 14.5.0. This is the audit snapshot, not a guarantee of future advisories.
- APK package inspected: com.posemaster.app, version 0.1.0, min API 26, target API 35. Android apksigner verified its v2 debug signature.
- tools/cloud-install.sh completed successfully, including repeat installation, build/check commands and artifact copy. Gradle reused valid outputs on repeat execution. Bootstrap downloads were tested during setup; a brand-new published cloud task has not been independently tested.

APK SHA256: `d89388a60084642fb990b3beeb012150afbd7a8b7642356513c88842588f17e7`

## Limits and outstanding work

Robolectric exercises the real Compose Activity and Room stack on the JVM; no physical Android device or emulator installation was tested. This host has no /dev/kvm. Complete rotation/process-death, Android 8–current devices, large text, TalkBack, visual/RTL, and performance acceptance before release.

Reference art is original, explicitly labeled placeholder illustration. It does not demonstrate each exact pose; replace it with licensed reference images for publication. Camera Guide is education, not camera capture.

Firebase account syncing and remote catalog consumption are not implemented. Admin catalog/image CLIs and authorization-rule drafts are supplied but not live-tested or deployed. No owner Firebase project, storage bucket, Google credentials, Play products or hosted policy/support URLs were supplied.

Play catalog queries, a purchase-verification backend, entitlement-policy tests, and AdMob/UMP test helpers are supplied. Android purchase launch/restoration, RTDN/refund refresh, native/rewarded rendering, client/server wiring and live service acceptance remain required. Ads and purchases are disabled in the normal app. AI is explicitly unconfigured. See RELEASE.md and backend/README.md for exact configuration and implementation gates; adding credentials alone does not complete those features.

Saved cloud scripts are draft configuration. Publication and restoration in a fresh task are separate actions and have not been verified here.
