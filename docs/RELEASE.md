# Release and service setup

## Signed AAB

1. Retain the application ID `com.posemaster.app` for upgrades of existing installs, or plan an explicit new-app migration before any ID change. Own a privacy-policy URL and support address.
2. In Android Studio: Build → Generate Signed App Bundle / APK → Android App Bundle → create/select a private upload keystore → release variant. Keep the keystore and passwords outside Git and use Play App Signing. The output is `app/build/outputs/bundle/release/app-release.aab`.
3. For CI, add an environment-backed release signing configuration, supply secrets through a CI secret manager, then run `./gradlew bundleRelease`. Current Gradle intentionally contains no fabricated signing credentials; a plain CLI bundle is unsigned until signing is configured.
4. Upload to internal testing first. Install via Play and test billing, restoration, consent and offline behavior. Do not distribute the debug key as a release key.

## Google Play Billing

Create monthly and annual subscription products with base plans and regional pricing. Pass their real IDs into `BillingConfiguration`; display only Play ProductDetails/offer pricing, never a hardcoded price. `PlayCatalog` queries products but is not wired to a purchase button. Before adding purchase launch, deploy an authenticated HTTPS backend implementing `PurchaseVerificationBackend`. Verify package, product, token, state and expiry against Google Play Developer API `purchases.subscriptionsv2.get`; handle pending states, idempotency, acknowledgement, cancellations, refunds, grace periods, account hold and Real-time Developer Notifications. Bind ownership to authenticated users. Grant entitlements only after server verification, and recheck expiry; never trust an on-device premium flag. Add restore/manage-subscription UX, disclosures and tests. Supply Google credentials only to the backend, never the app. A reference verification server is supplied in `backend/`; products and deployment are not configured. See backend README for important production gaps.

## AdMob

The manifest uses Google's Android sample application ID. `ConsentGatedTestAds` contains Google's test banner/native/rewarded IDs. Normal screens do not initialize ads. Configure UMP privacy messages, request consent before initializing or loading, and provide privacy-options access when required. Test all regions and child/under-age treatment; do not assume a successful callback implies permission. Integrate banner lifecycle cleanup; add native ad attribution/media/assets and rewarded callbacks before enabling those formats. Never grant paid subscription entitlements from a rewarded callback. Use test devices during development. Before production, move app/unit IDs into per-build manifest placeholders and build configuration populated from CI or private local Gradle properties. Keep ads out of Camera Guide controls and save/share operations. Verify current Google SDK policy and Data Safety disclosures before release.

## Optional Firebase and admin backend

Firebase is intentionally disabled; the offline app does not need it. To implement accounts, register Android app with the final application ID, add your own google-services.json, Firebase BOM/Auth/Firestore dependencies and Google Services plugin. Enable chosen sign-in providers. Add authenticated user-scoped favorites syncing with conflict resolution and deletion propagation; keep guest Room data usable and ask before merging. Provide authenticated account deletion including user documents, uploads and retention handling.

`backend/firestore.rules` is an undeployed starting policy: custom admin claims must be assigned only by a privileged trusted backend. The provided `backend/publish-catalog.js` is a privileged ADC-based draft uploader. Expand validation and image storage authorization before remote publishing. Audit changes, store original license evidence, and publish versioned catalogs. Use an authenticated entitlement-checking endpoint for premium catalog assets. A hidden screen or client boolean is not authorization. Test rules with Emulator Suite before deployment. No admin service account or working upload endpoint is bundled.

## Privacy and Data Safety draft

Current app stores favorites, collection names, interests and theme locally; no account/analytics/photo upload is implemented. INTERNET permission is present for future integrations. Ad SDK is bundled but not initialized by normal screens. Review SDK auto-initialization and network behavior with a release-device traffic audit before answering Data Safety; dependency presence alone is not proof of no collection. If ads/auth/billing are enabled, reassess device identifiers, diagnostics, purchases, email, third-party sharing, encryption, deletion and consent with actual SDK behavior. Publish a real policy and support URL; current profile explains that owner links are pending. Do not submit the draft as a legal policy.

## Play publishing checklist

- Replace placeholder art with licensed and reviewed images; retain licenses and model releases.
- Add adaptive icon, localized resources, store screenshots, age/content rating and accurate listing.
- Verify current Play target API and billing SDK deadlines; update dependencies as needed.
- Add hosted privacy/support and subscription terms, account deletion URL if accounts enabled.
- Confirm Data Safety, ads declaration, content rights, accessibility, RTL, large text and localization.
- Device test API 26–35+, rotation, process death, offline restart, database persistence, slow/error conditions.
- Test internal Play install, server entitlements, cancelled/refunded purchases, restores, consent withdrawal.
- Review crash/ANR, startup, memory and APK size; sign AAB, enable Play App Signing, staged rollout.


## Hello Photography upgrade configuration

Role, recent history, bilingual pose text, academy progress and reminders run locally. `HELLO_MONTHLY_PRODUCT` / `HELLO_ANNUAL_PRODUCT` Gradle properties configure subscription catalog queries; neither is a secret. No purchase button grants access: authenticated purchase/restore wiring, verification and RTDN acceptance remain release gates. Beginner courses are open; premium courses remain locked. The free favorite cap is 20 and legacy saved entries are preserved. Daily notification permission is optional and prompted only on opt-in. The app label is Hello Photography; package and database names retain the previous identity for data compatibility.
