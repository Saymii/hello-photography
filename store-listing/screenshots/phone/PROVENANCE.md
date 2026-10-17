# Screenshot provenance

Captured and visually reviewed on 2026-10-17 during release preparation.

These six PNGs are actual Android app UI rendered from the project's real
`MainActivity`, using `StoreScreenshotTest` on the current debug working tree.
Robolectric runs Android API 34 with native graphics, English, light appearance,
and a 412 × 800 dp phone configuration at xhdpi. Capture draws the actual
Activity decor view through native Android `Canvas`; it does not build a separate
promotional screen. Source files were copied without pixel edits from
`app/build/outputs/store-screenshots/`.

All six files are 824 × 1600, opaque 24-bit RGB PNGs. Each was checked for valid
PNG encoding, nonblank rendering, readable content, and screen state, and visually
inspected at full size. Explore thumbnails have been corrected so a long artwork
label no longer covers their pose illustrations. Original artwork labels remain
on larger references and in accessible descriptions.

| File | Actual screen state |
| --- | --- |
| `01-welcome.png` | First-launch role selection; further cards and Continue are reached by scrolling |
| `02-personalized-home.png` | Photographer home, today's featured original guide, Home selected |
| `03-explore-poses.png` | 48-guide library, search, categories, filters, Explore selected |
| `04-original-pose-guide.png` | Shared horizon original illustration and written pose instructions |
| `05-learning-academy.png` | Academy course list, zero local completion progress, Learn selected |
| `06-profile-settings.png` | Photographer role, English/light settings, optional reminders, Profile selected |

No user photos, external reference photographs, fake reviews, invented prices,
payment confirmations, premium entitlements, or device mockups were added.
`SHA256SUMS` records the hashes of the reviewed copies.

These renders provide screenshot assets and UI evidence. They do not prove
physical-device behavior, a successful release install, Play Billing operation,
or Play review approval. Capture and review the same screens from the exact
signed release installed through an internal Play test track before submitting
the listing for production. Signed-release physical-device captures remain
pending.
