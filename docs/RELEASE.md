# Release and service setup

The prepared Play application is **Hello Photography**, application ID
**`com.saymii.hellophotography`**, version **1.0.0**, version code **3**. It targets
and compiles against API **36**, with minimum API **26**, Build Tools **36.0.0**,
Android Gradle Plugin **8.10.1**, Gradle **8.11.1**, and JDK **17 or 21**.
Google Play acceptance and payment operation are separate from building an AAB.

## Identity and existing data

The owner chose a new Play identity. The legacy `com.posemaster.app` app remains
installed separately and retains its own favorites, progress and preferences.
There is no automatic transfer between Android application IDs. Do not uninstall
the legacy app expecting its data to appear in the new one. An export/import
migration is separate work. The new debug variant is
`com.saymii.hellophotography.debug`, so it can coexist with the release.

The Kotlin namespace `com.posemaster.app`, Room database `posemaster.db`, and
existing content IDs remain in the source. Future updates to the same new package
must preserve its database/preferences and use the correct signing identity.
Confirm ownership/availability of the final package in Play Console; a local
application ID does not reserve it. Increase `versionCode` for each Play upload.

## Signed AAB

The [complete build, sign, test and upload guide](PLAY_PUBLISHING_STEPS.md) includes
the owner's release workflow. The [validation report](VALIDATION.md) separates
current results from historical tests. Until a new result is recorded there,
previous test passes must not be applied to this changed release.

1. Use the owner's new private upload key for this Play app. Keep the key and
   passwords outside Git, securely back them up under the owner's control, and
   enroll in Play App Signing. The public certificate under `release/` is safe to
   share; it cannot recreate the private key. Never use the historical debug key
   for publishing.
2. Configure `HELLO_UPLOAD_KEYSTORE`, `HELLO_UPLOAD_STORE_PASSWORD`,
   `HELLO_UPLOAD_KEY_ALIAS`, and `HELLO_UPLOAD_KEY_PASSWORD` securely. The keystore
   is PKCS12; `HELLO_UPLOAD_KEYSTORE` is its absolute file path. Do not put passwords
   in command-line arguments, tracked files, logs, or this document.
3. Run `./gradlew testDebugUnitTest lintRelease bundleRelease` with those
   variables available. Release tasks fail if signing configuration is missing;
   the project must not silently deliver an unsigned upload bundle. The helper
   `tools/with-upload-signing.py` can run a command using an existing private JSON
   configuration without printing its values; keep that JSON outside the checkout.
4. The generated bundle is
   `app/build/outputs/bundle/release/app-release.aab`; the intended delivery copy is
   `release/HelloPhotography-1.0.0.aab`. Confirm the final signature, certificate,
   package, version, target API, release manifest and checksum. An AAB is uploaded
   to Play and cannot be opened on an Android phone as an APK.
5. For local device testing, build `./gradlew assembleRelease` with the same
   signing configuration. Output:
   `app/build/outputs/apk/release/app-release.apk`. Also test the APKs Play generates
   from the AAB, rather than relying only on a debug install.
6. Verify 16 KB ELF and APK ZIP alignment, smoke-test a 16 KB runtime, and inspect
   Play's App Bundle Explorer checks. Upload to **internal testing first**. Test
   physical devices, offline storage, navigation, notification opt-in, large text,
   TalkBack and policy links before production review.

The current Google target-API and Billing deadline pages could not be retrieved
because the cloud network proxy blocked their hosts. API 36 and Billing 9.1.0 are
preparation choices; verify live Console policy rather than claiming approval.
See [policy sources and verification limits](PLAY_REQUIREMENTS.md).

## Google Play Billing

The release includes **`com.android.billingclient:billing:9.1.0`** and a catalog
adapter. Monthly/yearly product IDs are optional Gradle properties
`HELLO_MONTHLY_PRODUCT` and `HELLO_ANNUAL_PRODUCT`. These are non-secret Play
subscription product IDs, not base-plan IDs. Prices must come from Play's returned
`ProductDetails`/offers; configure regional amounts in Play Console.

**Purchases are disabled.** Free guides/lessons work offline; premium-labeled sample
guides are free previews, while advanced premium courses remain locked. The catalog adapter, reference server, and passing
unit tests do not establish a working payment flow. No test button, local boolean
or rewarded-ad callback grants a subscription.

Before enabling purchases:

- Create and activate the correct monthly/yearly products and base plans in the
  final Play application. Match client configuration to the product model.
- Implement purchase launch, restore/recheck, manage-subscription access and clear
  price/renewal/cancellation disclosures. Handle pending states honestly.
- Deploy an authenticated HTTPS backend implementing
  `PurchaseVerificationBackend`. Verify package, product, token, ownership, state
  and expiry using Google Play Developer API `purchases.subscriptionsv2.get`.
  Bind entitlements to authenticated ownership; keep credentials only on servers.
- Make verification and acknowledgement idempotent. Handle expiry, refunds,
  revocations, cancellation, grace periods, account hold, replacement and
  authenticated Real-time Developer Notifications. Enforce offline entitlement
  expiry rather than trusting an indefinite on-device flag.
- Wire client/backend entitlements and test the full flow using a Play-installed
  build, license testers and test payment methods, including restore and failure
  cases. Add account-deletion and privacy changes if accounts are enabled.

The separate `backend/` directory is an undeployed reference with production gaps;
read its README before deployment. Billing credentials, merchant setup, products,
RTDN infrastructure and backend verification are not configured by this release.

## AdMob

The first Play **release variant excludes AdMob and UMP SDK dependencies and the
advertising-ID permission**. Debug-only sources retain the consent-gated test-ad
harness and Google's sample IDs. Normal app screens do not request ads. Verify
the final release dependency graph/merged manifest and network behavior before
completing declarations.

Enabling production advertising is a separate audited release: configure AdMob
app/unit IDs per build configuration, consent messages, UMP privacy options,
region/age treatment, SDK/data disclosures and lifecycle cleanup. Use test devices
and test IDs during development; never present development test delivery as live
revenue. Keep ads out of lessons' critical actions, save/share operations and any
future camera controls. Reassess Data Safety, privacy and the contains-ads
declaration when actual ads are added.

## Optional Firebase and admin backend

The offline release does not enable Firebase accounts or cloud sync. If introduced,
register the **final release application ID** in Firebase, supply owner configuration,
add the relevant Auth/Firestore dependencies/plugin, and test user-scoped rules.
Provide explicit guest-data merge behavior, conflict resolution, deletion
propagation and in-app plus web account-deletion resources. Keep Room usable for
guest users and never expose admin credentials in Android code.

`backend/firestore.rules` and storage rules are undeployed starting policies;
custom admin claims must be assigned only by a trusted privileged backend.
`backend/publish-catalog.js` is a separate privileged ADC-based draft uploader.
Expand validation, authorization, audit logs and licensing evidence before remote
publishing. Test rules in Emulator Suite. No working admin upload endpoint or
service-account credential is bundled. Premium remote assets need server-side
entitlement enforcement.

## Privacy, listing and submission

The release is designed for guest access and local role selection, favorites,
collections, recent history, learning progress, themes and optional reminders. It
does not upload users' photos and uses no photos supplied in chat, including as
inspiration. The bundled library uses labeled original illustrations. The premium
architecture is visible as preparation, not an active subscription service.

Review and publish the supplied policy/terms under the owner's real public HTTPS
URLs and contact details; verify those URLs from a device. Complete Data Safety,
ads, app-access, audience/content and IARC declarations against the actual final
release. Included/unused libraries require an SDK/network audit; do not infer a
no-collection answer solely from app intentions. Reassess every disclosure when
activating billing, authentication, cloud delivery, analytics or advertisements.

Store descriptions and assets are documented in [STORE_LISTING.md](STORE_LISTING.md).
Follow [PLAY_PUBLISHING_STEPS.md](PLAY_PUBLISHING_STEPS.md) for the actual Console
sequence, signing/key backups, testing tracks, account-specific closed-test
requirements and upload checks. Public privacy hosting, owner declarations,
device testing, Play review and production availability remain external gates.
No source commit, local signature or screenshot publishes the app automatically.
