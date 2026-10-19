# Build, sign, test, and publish Hello Photography

This prepares a real Android release; it does not publish to Google Play or activate payments. The signed bundle is an **upload artifact**, not proof of Play acceptance, billing operation, privacy-policy hosting, or physical-device acceptance.

## 1. Confirm the app identity and owner

- Google Play app: **Hello Photography**.
- New application ID: **`com.saymii.hellophotography`**.
- Current prepared version: **`1.0.1`**, version code **`4`** (the earlier 1.0.0 bundle used code 3).
- Publisher: **Simon Solomon**.
- Public support/privacy email: **simonsolomon436@gmail.com**.
- Android minimum: API 26 / Android 8.0.
- Target/compile SDK: API 36 / Android 16. Keep the project and uploaded artifact consistent and recheck [current target API policy](https://support.google.com/googleplay/android-developer/answer/11926878) immediately before submission.

This is a **new Play application**, as authorized by the owner. The previously shared debug app used `com.posemaster.app`. The new ID installs as a separate app; Android cannot automatically transfer its private favorites, learning progress, or preferences. Existing debug data remains in the old app. Do not uninstall that app expecting its data to appear in the new package. An explicit export/import feature would be a separate migration task.

The application ID becomes permanent after publication. Increase `versionCode` for every later Play upload, even if you reuse a `versionName`. Do not reuse the previous debug signing certificate as the upload key.

## 2. Set up the build tools

Open the existing repository in a current compatible Android Studio. Install JDK 17+, Android SDK Platform 36, the project's pinned Build Tools, and Platform Tools. Use the checked-in Gradle wrapper and its pinned Android Gradle Plugin. Let Android Studio create an untracked `local.properties` with your local SDK path; never commit private keystore paths or passwords.

On the prepared cloud environment:

```sh
cd /workspace/hello-photography
source tools/cloud-env.sh
```

On another machine, set `JAVA_HOME`/`ANDROID_HOME` appropriately instead of sourcing cloud-specific paths. Run `./gradlew --version` to confirm the toolchain. If an Android SDK license is missing, review and accept the applicable license using Android Studio's SDK Manager or `sdkmanager --licenses`.

## 3. Run source and debug checks

From the repository root:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
python3 tools/validate_content.py
```

If the separate future backend is changed, its policy tests can be run independently:

```sh
npm --prefix backend ci
npm --prefix backend test
```

Backend unit tests do not establish live Google Play purchase verification. The first ads-free release does not depend on deploying that service.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Its application ID is `com.saymii.hellophotography.debug`, so it can coexist with the release package. Install on a test phone with:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Test first-launch role selection for **Photographers**, **Models**, and **Everyone**; personalized Home; search/filter and pose details; favorites/collections; lessons/quizzes/progress; recently viewed guides; themes; English/Amharic; offline restart; sharing; opt-in reminders/denial; confirmed local-data deletion; and all navigation paths. Check small screens, large text/TalkBack, rotation, dark mode, and API 26 plus a recent Android version. Never treat a debug-only test as evidence that a minified/signed release works.

## 4. Protect and configure the upload key

The owner authorized a **new upload key** for the new Play app. Reuse the generated private key for subsequent uploads. Keep at least two encrypted backups under the owner's control. Never commit the key, passwords, recovery details, service-account JSON, or signing-properties files; do not send these to testers. Play App Signing will manage the app-signing key used to sign APKs delivered by Google Play.

The private handoff ZIP contains the PKCS12 keystore, `signing.json` with its
passwords, the public certificate, and private handling instructions. The ZIP
container itself is **not encrypted**. Store it in an encrypted drive or password
manager. Extract it into a private directory outside the checkout; keep the JSON
and keystore together so the relative path remains valid. Do not attach this ZIP
to a GitHub release, Play listing, tester invitation, or support message.

Release signing reads these environment variables:

- `HELLO_UPLOAD_KEYSTORE`: absolute path to the private upload keystore outside the checkout.
- `HELLO_UPLOAD_STORE_PASSWORD`: keystore password.
- `HELLO_UPLOAD_KEY_ALIAS`: key alias inside that keystore.
- `HELLO_UPLOAD_KEY_PASSWORD`: private-key password.

Use the already generated key and private handoff instructions when available. For a future developer generating a key for a genuinely new app only, use Android Studio's **Build → Generate Signed App Bundle / APK → Android App Bundle → Create new**. Store the resulting upload keystore outside the repository. Do not create a different key for every build.

If you received the generated private `signing.json` alongside your keystore, keep both outside the checkout. The file contains the same four field names listed above. Relative keystore paths are resolved relative to that JSON file. The helper loads the credentials only into the child command's environment and does not print their values:

```sh
python3 tools/with-upload-signing.py /secure/private-signing/signing.json ./gradlew --no-daemon testDebugUnitTest lintRelease bundleRelease
```

Replace `/secure/private-signing/signing.json` with the real private path. Do not commit or share that file, include it in a source archive, or attach it to a public release. The helper defaults to `./gradlew --no-daemon bundleRelease` if no command follows the JSON path.

Alternatively, in Bash load existing credentials without putting passwords into shell history:

```sh
read -r -p "Upload keystore absolute path: " HELLO_UPLOAD_KEYSTORE
read -r -p "Upload key alias: " HELLO_UPLOAD_KEY_ALIAS
read -r -s -p "Keystore password: " HELLO_UPLOAD_STORE_PASSWORD
printf '\n'
read -r -s -p "Private key password: " HELLO_UPLOAD_KEY_PASSWORD
printf '\n'
export HELLO_UPLOAD_KEYSTORE HELLO_UPLOAD_KEY_ALIAS
export HELLO_UPLOAD_STORE_PASSWORD HELLO_UPLOAD_KEY_PASSWORD
```

Use a CI secret manager for automated builds. Do not print the environment, enable shell tracing around signing, put passwords in Gradle command-line arguments, or store secrets in tracked files. The `.aab` contains a public signing certificate, not the private signing key. A standalone copy of the public upload certificate is safe for Play registration.

## 5. Build the signed release bundle

With all four signing variables set:

```sh
./gradlew testDebugUnitTest lintRelease bundleRelease
```

Expected output:

```text
app/build/outputs/bundle/release/app-release.aab
```

An AAB is not an installable APK; it is uploaded to Play, which generates device-specific APKs. To produce a locally installable signed release APK for device checks, run:

```sh
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

Debug and release use separate application IDs and can be installed side by side. Their private data is separate. An existing install of the exact release package must have a compatible signing certificate to update in place. Play App Signing usually means the Play-delivered app uses a different app-signing key from a locally signed upload-key APK; test upgrades through Play for the production signature path. Read any signature-conflict error before replacing an installation, and do not uninstall a user's app as a troubleshooting shortcut.

After building, remove password variables from the current shell when no longer needed:

```sh
unset HELLO_UPLOAD_STORE_PASSWORD HELLO_UPLOAD_KEY_PASSWORD
```

Do not overwrite or delete your only copy of the private key.

## 6. Verify the exact upload artifact

Use the JDK tools and record the public certificate fingerprints:

```sh
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
sha256sum app/build/outputs/bundle/release/app-release.aab
```

An upload certificate is normally self-signed, so a certificate-chain warning alone is expected; a missing or invalid bundle signature is not acceptable. Confirm the signer fingerprint matches the intended upload key. Using a trusted downloaded `bundletool` release, set `BUNDLETOOL_JAR` to its absolute path and run:

```sh
java -jar "$BUNDLETOOL_JAR" validate --bundle=app/build/outputs/bundle/release/app-release.aab
java -jar "$BUNDLETOOL_JAR" dump manifest --bundle=app/build/outputs/bundle/release/app-release.aab --module=base
```

Confirm package ID, version code/name, SDKs, app label, and permissions. Audit the release dependency graph and merged manifest as described in [DATA_SAFETY.md](../store-listing/DATA_SAFETY.md); test on a release device before completing disclosures. Test native libraries on a 16 KB page-size Android device/emulator and review Play's [16 KB page-size requirement](https://developer.android.com/guide/practices/page-sizes) where native code is included. Resolve upload/pre-launch warnings rather than assuming a successful local build guarantees compliance.

## 7. Publish the privacy policy and prepare the listing

Owner contact details have been supplied. A **public HTTPS privacy-policy URL is still required**. Review [legal/privacy-policy.md](../legal/privacy-policy.md), its ready-to-host [HTML version](../legal/privacy-policy.html), and [terms of use](../legal/terms-of-use.md). Publish the HTML at your chosen host. GitHub Pages is one possible host, but it must actually be enabled and tested; a suggested Pages address or repository file is not proof of hosting. No external website has been published by this preparation task.

The policy must be publicly reachable without sign-in, a geographic restriction, or a file download/PDF. Check it in an incognito browser and on mobile. It must name Hello Photography and Simon Solomon, show the contact email, and match the actual release. Add the real URL in Play Console and in app configuration where supported; do not leave an example URL.

Use the truthful store copy and art under `store-listing/`. Screenshots must represent the exact submitted application. The original icon/feature artwork remains usable; the seven screenshots under `store-listing/screenshots/phone/` were refreshed from the current Activity rendering. Recapture from the exact Play-delivered release before production submission. The new photo collection contains owner-supplied AI-generated references, labeled with their source; earlier excluded uploads and the RAR remain prohibited. Do not claim the full 48-photo collection is present while only 39 sources are available. Review screenshot text, clipping, localization and unavailable features. Do not advertise live subscriptions, cloud sync or a configured AI recommendation service while they are unavailable. Review any required rights before using a bundled reference separately in marketing.

## 8. Create the app in Play Console

1. Create or finish verification of your Google Play developer account, accept the developer agreement, and complete payment/contact/identity requirements shown for your account. Complete merchant setup only if/when selling paid products.
2. Choose **Create app**. Enter **Hello Photography**, the intended default language, app category, and the initial free distribution choice. A free app can later offer Play subscriptions; do not set a paid upfront price merely because premium previews exist.
3. Complete the main store listing: title, short/full descriptions, category/tags, owner support email, public privacy-policy URL, app icon, feature graphic, and genuine phone screenshots. Check the current asset rules in Console. Do not submit desktop mockups as phone captures.
4. Complete **App content**: privacy policy, ads declaration, app access, target audience/content, IARC content rating, Data Safety, and any other declarations Console requests. Use [the worksheet](../store-listing/DATA_SAFETY.md). The intended release is ads-free and has no login. After choosing a role, reviewers can read all 48 sample posing guides, including four premium-labeled collection previews, and free beginner lessons. Advanced Academy syllabuses are visible but their premium lessons remain locked. Purchases are disabled; do not invent reviewer payment credentials.
5. Select the audience honestly. The role name **Everyone** does not determine a legal age rating or mean the app is a child-directed product. Complete Families requirements if children are actually included.
6. Enroll in **Play App Signing** during first release setup. Register/confirm the public upload certificate for the generated upload key. Keep the private upload keystore securely backed up.

## 9. Upload to internal testing first

1. Open **Testing → Internal testing**, create a release, and upload the signed `app-release.aab`.
2. Review package/version/target API, native-library compatibility, SDK/policy checks, and signing information. If Play rejects the artifact, read the exact error and correct the cause; do not bypass integrity or signing checks.
3. Add factual release notes describing the current offline functionality. Explicitly state that paid subscriptions are not enabled if the premium preview screen is included.
4. Add your tester list and roll out the **internal test**. Share the Play-provided opt-in link with invited testers. This is a test distribution, not a public launch.
5. Install from Google Play and verify the exact release on physical devices, including an update from the previous internal-test version once one exists. Run the functional checks in step 3 again and check the Play pre-launch report, Android vitals, accessibility, ANRs, and crashes.
6. For newer personal developer accounts, follow Console's production-access testing requirements. A previously published requirement for affected new personal accounts was a closed test with at least **12 opted-in testers for 14 consecutive days**. Current policy pages could not be fetched in this environment, so verify the exact current requirement shown for your account in Console and [the official guide](https://support.google.com/googleplay/android-developer/answer/14151465) before planning your release. Internal testing alone does not satisfy a required closed test.

## 10. Prepare subscriptions without pretending they work

The current release provides 39 AI photo guides and all 48 original sample posing guides free, including four premium-labeled collection previews, plus free beginner lessons. Advanced Academy courses expose syllabus previews, while premium lessons remain locked. **Purchases are disabled.** Do not take payment, grant premium through a local flag, or advertise live subscriptions until the following work is complete and tested.

1. Set up the developer merchant/payment profile as required by Play. Under monetization, create monthly and annual subscription products with active base plans, billing periods, regional prices, applicable taxes, and truthful benefit descriptions. Match the client configuration: `HELLO_MONTHLY_PRODUCT` and `HELLO_ANNUAL_PRODUCT` are Gradle properties for two subscription **product IDs**, not secrets. A single product with two base plans requires corresponding client changes; do not interchange a base-plan ID and product ID.
2. Configure real product IDs using private/local Gradle properties or CI properties. Prices must come from Google Play ProductDetails/selected offers. The app must show actual price, period, automatic renewal, any trial/intro terms, cancellation information, and policy links before purchase.
3. Implement and deploy authenticated purchase launch/restore and an HTTPS verification backend. Only the backend may grant access after Google Play Developer API verification. Validate package, product, token, ownership and purchase state; handle pending transactions, idempotency, acknowledgements, expiry, refunds, cancellation, grace period, account hold and revocation. Add authenticated RTDN processing and current entitlement checks. Server credentials never belong in the APK.
4. Wire the Android app to verified entitlements, restore/manage-subscription controls, offline entitlement expiry rules, and accessible errors. Do not infer ownership from a client boolean or a successful Billing callback alone. If accounts are introduced for ownership, add privacy disclosures and in-app/web account deletion first.
5. Configure license testers and use the Play-installed internal-test build with Play test payment methods. Test success, decline, pending, cancellation, renewal, expiration, refund/revocation, restore/reinstall, account switch, duplicate tokens, slow/offline networking, and backend failure. Capture evidence without logging tokens or credentials.
6. Update the privacy policy, Data Safety, terms, and listing for actual purchase/account processing. Enable real purchases only after this integration passes and Console products are available in the intended regions.

See [backend/README.md](../backend/README.md) for the separate reference service and its outstanding production gates. Bundled reference code or unit tests do not constitute a deployed payment backend. The release contains no active ads; monetization through ads requires a separate consent, SDK, policy, and release audit.

## 11. Submit for production review

Complete any required closed test and production-access request. Resolve all release blockers, including the public privacy URL and remaining owner declarations. Review the final assets, actual pricing status, content rights, translations, pre-launch reports, and signed artifact. Create a production release with the same tested AAB (or a newly versioned/retested build), select countries, use a controlled rollout where available, and submit for Google's review.

Google controls approval and availability. No local build, screenshot, signature check, or internal test guarantees approval or makes the app publicly available. Do not claim publishing or payments work until their respective Console/backend setup and acceptance tests are complete.

## 12. Keep each release reproducible

Record the source commit, Gradle/JDK/SDK versions, AAB hash, package/version, upload certificate fingerprint, test evidence, dependency audit, disclosures, and asset sources. Preserve the upload key and passwords outside source control. Increment the version code for updates, test upgrades/persistence, keep SDKs current, and revise disclosures before changing data behavior.
