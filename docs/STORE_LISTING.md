# Hello Photography store listing

These files prepare a reviewable Google Play listing. They do not create a Play
Console app, upload a release, activate subscriptions, or establish ownership of a
publisher account. Use the exact behavior of the release you submit when reviewing
the listing and App content declarations.

## Ready-to-upload artwork and English text

| Play Console field | File | Prepared format |
| --- | --- | --- |
| App name | `store-listing/en-US/title.txt` | 17 characters; maximum 30 |
| Short description | `store-listing/en-US/short-description.txt` | 80 characters; maximum 80 |
| Full description | `store-listing/en-US/full-description.txt` | Under 4,000 characters |
| High-resolution icon | `store-listing/app-icon-512.png` | 512 × 512, 32-bit RGBA PNG; fully opaque |
| Feature graphic | `store-listing/feature-graphic-1024x500.png` | 1024 × 500, opaque RGB PNG |
| Phone screenshots | `store-listing/screenshots/phone/*.png` | Actual app renders; see below |

All listing artwork is original geometric/vector artwork created for this project.
No user-uploaded photos, third-party photographs, stock imagery, creator marks, or
social-media screenshots were used. The square icon has no baked rounded-corner
mask. Google Play applies the display shape. Editable SVG sources and an asset
renderer are in `store-listing/source/`.

Reproduce the icon and feature graphic with Python 3 and Pillow:

```sh
python3 store-listing/source/generate_assets.py
```

This renderer uses DejaVu Sans from the standard Linux font path. Install Pillow
and DejaVu fonts in your own development environment if necessary; no photo or
network service is involved in rendering.

## Actual app screenshots

`StoreScreenshotTest` renders the existing `MainActivity` and real Compose screens
using native Robolectric graphics. It never constructs promotional fake UI and
does not seed a premium purchase or entitlement. The test captures welcome,
personalized home, Explore, a pose guide, Learn, and Profile in English/light mode.

```sh
./gradlew :app:testDebugUnitTest --tests '*StoreScreenshotTest'
```

Generated output: `app/build/outputs/store-screenshots/*.png`.
Review every file before copying it to `store-listing/screenshots/phone/`.
Screenshot qualifiers use a phone layout of 412 × 800 dp at xhdpi so the images
stay within Play's screenshot dimensions and aspect-ratio limit. Files contain
unframed UI without added claims, fake payments, device mockups, or misleading
prices. The screenshot test validates rendering; it cannot prove hardware,
accessibility, Play Billing, or notification behavior on a physical device.

After visually reviewing the generated screenshots, copy them from the project
root with:

```sh
mkdir -p store-listing/screenshots/phone
cp app/build/outputs/store-screenshots/*.png store-listing/screenshots/phone/
```

Before publishing, capture the same screens from the exact signed release on a
physical device or Android emulator. This avoids differences from debug-only
configuration or from changes after the test captures. Capture a clean portrait
screen with no personal notification information:

```sh
adb shell screencap -p /sdcard/hello-photography.png
adb pull /sdcard/hello-photography.png
adb shell rm /sdcard/hello-photography.png
```

Google Play requires at least two phone screenshots. Upload the strongest actual
screens first: personalized home, illustrated pose guide, Explore, then academy.
Use the welcome and Profile captures if they clearly explain supported features.
Keep the screenshots truthful; the pose library uses original illustrations,
not photographic reference images. A development-mode purchase screen should not
be advertised as a working subscription checkout.

## Create the listing in Play Console

1. Create or open your app in Play Console. Select English (United States) as the
   default language, the app name above, and App rather than Game.
2. Under Grow users → Store presence → Main store listing, paste the text files.
   Review it against the app you are actually uploading. Do not claim premium
   availability, live ads, syncing, or cloud AI unless configured and tested.
3. Upload the PNG icon, feature graphic, and at least two actual phone screenshots.
   App icon artwork and launcher artwork should use a consistent brand.
4. Choose Photography as the app category. Add appropriate available tags, such
   as Photography and Education, if Play Console offers them for your category.
5. Use publisher name **Simon Solomon** and support email
   **simonsolomon436@gmail.com**, and enter your public privacy-policy URL.
   A repository Markdown source is not a substitute for a public, readable policy
   matching this app and your actual contact information.
6. Complete App content, Data safety, Target audience, content rating, ads,
   app access, and financial/subscription declarations as applicable. Reconcile
   these with the release configuration before sending for review.
7. Add an Amharic listing when you have reviewed its translation. Future language
   plans are not shipped language support and should not be advertised as such.

## Final copy review

- The bundled guides are original illustrated sample content. Do not describe
  them as licensed photographs or imply that a photo gallery exists.
- The recommendation service is rule based and works offline. No cloud AI is
  claimed.
- Free favorites have a limit of 20; existing local collections are preserved.
- The premium syllabus and subscription preparation are not evidence of a live
  product. Review purchase wording once products and the secure backend have
  passed internal-track tests.
- Disclose the exact advertising, billing, analytics, and account behavior of the
  uploaded release. Do not infer data practices from a future roadmap.

Official guidance (check again when submitting):

- [Create and set up your app](https://support.google.com/googleplay/android-developer/answer/9859152)
- [Add preview assets](https://support.google.com/googleplay/android-developer/answer/9866151)
- [Google Play icon design](https://developer.android.com/distribute/google-play/resources/icon-design-specifications)
- [Store listing policy](https://support.google.com/googleplay/android-developer/answer/9898842)
