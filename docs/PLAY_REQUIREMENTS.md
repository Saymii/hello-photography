# Google Play requirements and policy verification

Review date: **17 October 2026** (Africa/Nairobi). This file is a release checklist,
not a statement that Google has accepted the app or approved payments.

## Verification limits

Live requests to the authoritative Google policy pages could not be completed in
this cloud environment. Requests to both `support.google.com` and
`developer.android.com` failed with **`CONNECT tunnel failed, response 403`** at
the configured network proxy. Neither host was present in the effective network
allowlist. No alternate route, mirror, or TLS bypass was used.

Consequently, the current 2026 target-API deadline and Billing-version deadline
remain **unverified against live official policy**. The official links below
must be checked in a browser and the actual Play Console before uploading. Do not
interpret SDK installation, successful builds, or this document as policy approval.

One official artifact source **was** reachable: [Google Maven's Billing metadata](https://dl.google.com/dl/android/maven2/com/android/billingclient/billing/maven-metadata.xml).
It lists 8.3.0, 9.0.0, and 9.1.0, with `latest`/`release` 9.1.0 and
`lastUpdated` 20260618171108. This confirms artifact availability only; Maven
metadata does not state the Play submission support/deprecation policy.

## Required checks before upload

| Check | Preparation and remaining verification | Official source |
| --- | --- | --- |
| Target Android API | The prepared release compiles/targets API 36. Confirm it meets the current new-app/update deadline. API 35 was the published 2025 baseline; it must not be assumed sufficient for October 2026. API 36 is a conservative preparation target, but the current deadline was not retrievable here. | [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en), [Android target API guidance](https://developer.android.com/google/play/requirements/target-sdk) |
| Billing library | The release configuration replaces 7.1.1 with the Java artifact `com.android.billingclient:billing:9.1.0`. The previously published version-7 deprecation schedule was 31 August 2026, with a possible extension to 1 November 2026; do not rely on an extension. Verify the live schedule and dependency used in the final release. | [Billing deprecation FAQ](https://developer.android.com/google/play/billing/deprecation-faq), [Billing release notes](https://developer.android.com/google/play/billing/release-notes) |
| 16 KB memory pages | Previously published policy requires Android 15+ submissions with native code to support 16 KB pages from 1 November 2025. Inspect transitive native libraries, AAB page-alignment configuration, generated APK ZIP alignment, and runtime behavior on a 16 KB device/emulator. | [16 KB page-size guidance](https://developer.android.com/guide/practices/page-sizes) |
| Signing and identity | Pick the final application ID before the first Play upload; after publication it identifies that app permanently. Preserve existing identity if upgrading the same published app. Use a private upload key and Play App Signing; increase `versionCode` for every uploaded release. | [App signing](https://developer.android.com/studio/publish/app-signing), [App versioning](https://developer.android.com/studio/publish/versioning) |
| Developer account | Complete Play developer verification and any Console identity/contact/device checks. Personal accounts created after 13 November 2023 have previously been required to run a closed test with at least 12 continuously opted-in testers for 14 days before applying for production access. Check the account's current requirements in Console. | [Testing requirements for new personal accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) |
| Pre-release validation | Upload the signed AAB to internal testing, install through Play, inspect App Bundle Explorer and the pre-launch report, and test supported devices. An internal upload is not a production publication. | [Testing tracks](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en) |

The previously built debug APK contains `libandroidx.graphics.path.so` for
`arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`. Inspection with `readelf -lW`
found `0x4000` alignment on every LOAD segment. This is useful evidence for those
specific ELF files only; it does not establish ZIP alignment, final release
contents, or runtime compatibility. Repeat checks on the final release artifact.

## Store listing assets

Prepare the following conservative formats and confirm the current specifications
in [Google's preview-asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en)
when uploading:

| Asset | Preparation format |
| --- | --- |
| Store icon | 512 × 512 px, 32-bit PNG, at most 1,024 KB. Keep important artwork away from the outer edge; Google applies its own shape/shadow treatment. |
| Feature graphic | 1,024 × 500 px, JPEG or 24-bit PNG without alpha. |
| Phone screenshots | At least two actual app screenshots. JPEG or 24-bit PNG without alpha; each dimension 320–3,840 px, with the longest dimension at most twice the shortest. Prefer four or more 1,080 × 1,920 portrait images showing real core features. |
| Listing text | App title at most 30 characters; short description at most 80; full description at most 4,000. Avoid unsupported ranking, price, AI, purchase, or performance claims. |

Screenshots must show the shipping release's behavior and actual content. Use only
the project's original illustrations or separately licensed assets with recorded
rights. **Do not use any photos uploaded by the user**, including as inspiration.
Keep feature-graphic artwork separate from screenshots; do not present artwork as
an in-app feature. Localized Amharic screenshots should show the actual Amharic UI.

## App content, privacy, and disclosures

Complete the App content section in Play Console against the **actual release
variant**, not planned functionality:

1. **Privacy policy:** host the reviewed policy at an active, publicly accessible
   HTTPS URL, available without signing in, and link it in the app and listing.
   Identify the app and responsible developer, explain local storage, relevant
   third-party SDK collection/sharing, retention, deletion, and a real support
   contact. A local policy file or inaccessible draft URL is not sufficient.
2. **Data Safety:** audit the merged manifest, enabled SDKs, actual network
   traffic, server processing, backups, and retention. Local-only favorites are
   not automatically data collection; an included ad SDK is not automatically
   proof of no collection either. Account, advertising, billing, analytics, and
   crash-reporting changes require a new assessment. Internal-only testing has
   historically had an exemption, but do not assume closed/open/production tracks
   are exempt.
3. **Ads:** answer the contains-ads declaration based on what the submitted app
   actually displays. An unused development harness is different from live
   advertising. Enable production ads only after UMP/consent, region/age handling,
   privacy options, SDK review, and accurate disclosures are tested. Test ad IDs
   are for development testing, not a reason to claim live monetization.
4. **Content rating and audience:** complete the IARC questionnaire accurately,
   select the intended age groups, and assess Families rules if children are
   included. Do not select a rating or child-directed treatment solely to improve
   store reach. Review every illustration and educational item in the release.
5. **App access:** explain guest access and supply reviewer credentials only if
   enabled restricted functionality requires them. Do not invent an account
   backend or credentials. Describe unavailable premium purchasing honestly.
6. **Permissions:** request notifications only after the user opts into reminders.
   The guide does not need broad photo-library or camera permission merely to
   display its bundled assets. Reassess permissions if camera/photo features are
   later added.
7. **Account deletion:** the offline release has no cloud account. If accounts
   are introduced, supply in-app initiation plus a functional web deletion
   resource and disclose any lawful retention. Deleting local guides alone does
   not delete a server account.
8. **Other declarations:** answer financial features, health, government, news,
   advertising ID, and any Console-specific forms accurately. A photography guide
   must not claim regulated services it does not provide.

Official references:

- [User Data policy and privacy policy](https://support.google.com/googleplay/android-developer/answer/10144311?hl=en)
- [Data Safety form](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [Account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en)
- [Preparing your app for review](https://support.google.com/googleplay/android-developer/answer/9859455?hl=en)
- [Target audience and content](https://support.google.com/googleplay/android-developer/answer/9285070?hl=en)
- [Content ratings](https://support.google.com/googleplay/android-developer/answer/9859655?hl=en)
- [AdMob UMP privacy](https://developers.google.com/admob/android/privacy)

## Premium subscriptions: release gates

Digital premium guides distributed through Play should use Google Play Billing
unless the developer independently qualifies and enrolls in an applicable
alternative-billing program. No such program is configured here.

- Create the actual subscription products and monthly/yearly base plans in the
  correct Play Console application. Configure regional prices there.
- Show price, currency, billing period, trial/introductory conditions if any,
  renewal behavior, cancellation information, and terms/privacy links before
  purchase. Use returned `ProductDetails` offers, not sample amounts.
- Deploy an authenticated HTTPS purchase-verification and entitlement service.
  Validate package, product, purchase token, purchase state, expiry and ownership
  through Google Play Developer APIs. Keep service credentials on the server.
- Grant premium access only after verified purchase state. Handle pending
  purchases without granting access, and acknowledge completed purchases within
  Google's required period. Make retries and ownership binding idempotent.
- Handle restoration, app reinstall, cancellation, expiry, refund/revocation,
  grace period, account hold, subscription replacement and RTDN deduplication.
- Provide restore/recheck and manage-subscription paths. A custom local flag,
  reward callback, test button, or catalog response must never grant a paid
  subscription entitlement.
- Validate the entire flow using Play-distributed test builds, license testers
  and the backend, including cancellation and refund scenarios. A locally signed
  APK or successful SDK connection alone does not demonstrate working purchases.

Official references:

- [Subscriptions policy](https://support.google.com/googleplay/android-developer/answer/9900533?hl=en)
- [Billing integration](https://developer.android.com/google/play/billing/integrate)
- [Billing security](https://developer.android.com/google/play/billing/security)
- [Billing testing](https://developer.android.com/google/play/billing/test)
- [Subscription lifecycle](https://developer.android.com/google/play/billing/lifecycle/subscriptions)

Before production, record the actual Console acceptance results and test evidence
in the release report. Requirements marked unverified here remain release gates.
