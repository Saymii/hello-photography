# Hello Photography 1.0.1 — Play upload files

- **Application ID:** `com.saymii.hellophotography`
- **Version:** `1.0.1` / code `4`
- **Minimum Android:** 8.0 (API 26); target API 36
- **Publisher:** Simon Solomon · simonsolomon436@gmail.com

This update includes **39 supplied AI-generated photos** (12 male, 10 female,
8 couple, 1 group and 8 casual/selfie), with optimized offline copies, simple
instructions and full-screen zoom. The planned 48-photo set is missing female
11–12 and seven group files. The original 48 illustrated guide IDs are retained.

`HelloPhotography-1.0.1.aab` is the signed Google Play upload bundle. An AAB
cannot be opened or installed on a phone. Upload it to the app's internal testing
track in Play Console after enrolling in Play App Signing.

`HelloPhotography-1.0.1-test.apk` is a locally installable APK built from the
same release source and signed with the upload key. Open it using Android Files, allow
installation from that source if requested, then tap Install. It needs Android
8.0 or newer. This new package installs separately from the earlier
`com.posemaster.app` and from the `.debug` package. Their saved data is separate.
The upload-key APK may have a different certificate from a Play-delivered APK;
test production updates through Play. Preserve data before resolving any
certificate conflict; do not uninstall as a first troubleshooting step.

`upload-certificate.pem` is public and safe to share with Play. The private key
and passwords are **outside this repository**. Keep the private backup under the
owner's control in encrypted offline storage. Never upload that backup to GitHub,
Play listing assets, a public release, or a testing group.

Follow [the complete publishing instructions](../docs/PLAY_PUBLISHING_STEPS.md)
for tools, signing, device tests, listing assets, policy hosting, disclosures and
upload steps. See [validation evidence](../docs/VALIDATION.md) for actual checks.
Artwork, English listing text and actual UI renders are in `store-listing/`.
The hostable policy and terms are in `legal/`.

Purchases are disabled. Premium collection samples are previews; no payment or
paid entitlement is simulated. The release excludes advertising SDKs. A public
privacy-policy URL, current Console policy checks, device/16 KB runtime tests,
and any account-specific testing requirements remain before production review.
Nothing here establishes publication, Play acceptance, or working payments.
