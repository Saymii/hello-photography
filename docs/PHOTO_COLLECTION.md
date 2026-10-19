# Adding the new 48-photo collection

The owner plans to provide a new set of 48 pose photographs for Hello Photography. Thirty-five files have arrived: male 01–12, female 01–10, couple 01–08 and the earlier numbered 44–48. This is a new batch: photographs uploaded earlier in the conversation, including the earlier RAR archive, remain excluded from the app and from visual inspiration.

The current app continues to use its 48 original written guides and Compose illustrations. Receipt of a file does not make it part of a release. The new photos need a complete number-to-pose manifest, redistribution permission and reviewed instructions before they are bundled. Keep pending uploads outside the Git repository and release archives.

## Sending the rest of the batch

Keep the existing series names and numbers and send the remaining files in any order. A single ZIP with named images is convenient; several smaller batches also work. Male, female and couple filenames have separate numbering: `male_pose_01.jpg`, `female_pose_01.jpg` and `couple_pose_01.jpg` are different photos. Keep filenames descriptive and do not renumber files after an upload. Keep the highest-quality originals separately. The total collection is planned to contain 48 photos; counts per series have not been specified.

Received filenames establish this proposed mapping, not a replacement for existing guides:

| Series | Photo number | Proposed stable pose ID | Supplied title |
| --- | --- | --- | --- |
| Male | 01 | 1001 | male_pose_01.jpg |
| Male | 02 | 1002 | male_pose_02.jpg |
| Male | 03 | 1003 | male_pose_03.jpg |
| Male | 04 | 1004 | male_pose_04.jpg |
| Male | 05 | 1005 | male_pose_05.jpg |
| Male | 06 | 1006 | male_pose_06.jpg |
| Male | 07 | 1007 | male_pose_07.jpg |
| Male | 08 | 1008 | male_pose_08.jpg |
| Male | 09 | 1009 | male_pose_09.jpg |
| Male | 10 | 1010 | male_pose_10.jpg |
| Male | 11 | 1011 | male_pose_11.jpg |
| Male | 12 | 1012 | male_pose_12.jpg |
| Female | 01 | 2001 | female_pose_01.jpg |
| Female | 02 | 2002 | female_pose_02.jpg |
| Female | 03 | 2003 | female_pose_03.jpg |
| Female | 04 | 2004 | female_pose_04.jpg |
| Female | 05 | 2005 | female_pose_05.jpg |
| Female | 06 | 2006 | female_pose_06.jpg |
| Female | 07 | 2007 | female_pose_07.jpg |
| Female | 08 | 2008 | female_pose_08.jpg |
| Female | 09 | 2009 | female_pose_09.jpg |
| Female | 10 | 2010 | female_pose_10.jpg |
| Couple | 01 | 3001 | couple_pose_01.jpg |
| Couple | 02 | 3002 | couple_pose_02.jpg |
| Couple | 03 | 3003 | couple_pose_03.jpg |
| Couple | 04 | 3004 | couple_pose_04.jpg |
| Couple | 05 | 3005 | couple_pose_05.jpg |
| Couple | 06 | 3006 | couple_pose_06.jpg |
| Couple | 07 | 3007 | couple_pose_07.jpg |
| Couple | 08 | 3008 | couple_pose_08.jpg |
| Numbered | 44 | 1044 | Relaxed Railing Lookout |
| Numbered | 45 | 1045 | Looking up from a book |
| Numbered | 46 | 1046 | Casual tote bag walk, sideways glance |
| Numbered | 47 | 1047 | Relaxed chair pose, chin in hand |
| Numbered | 48 | 1048 | Relaxed three-quarter wave portrait |

Another 13 photos are needed to reach 48. Their exact series and filenames are pending; the old global missing-number range no longer applies. Additional delivery of the same series and number should be treated as an explicit revision, not a second pose. Compare file hashes to catch duplicates and keep a record of which revision was approved.

## Identity and existing saved data

Existing pose IDs 1–48 identify specific original guides. Favorites, recently viewed guides and deep links rely on those IDs. Do not attach a new numbered photo to an existing guide merely because its filename ends with the same number: the movements and meaning can differ.

Male files retain their proposed IDs **1001–1012**. Female files use **2001–2010**, calculated as `2000 + female photo number`. Couple files use **3001–3008**, calculated as `3000 + couple photo number`. Each series keeps its own identity without overwriting another guide with the same filename number. The earlier numbered 44–48 retain IDs **1044–1048**. Use collection ID `owner-photo-set-2026-10`. Assign further IDs without collisions and keep each identity fixed after publication. A revised photo can update the same guide only if it demonstrates the same pose; a different pose receives a new unused ID. A collection ID describes the editorial photo set and is separate from a user's named favorites collection in Room.

If a new image truly matches an existing guide, a reviewed decision can attach it to that existing ID without changing the guide's meaning. Do not do that automatically. Retain the existing guides and favorites while the new collection is prepared.

## Proposed optional photo metadata

Append a nullable, defaulted photo reference to `Pose` so existing constructor calls, original guides and translated copies remain compatible. One possible JSON record is:

```json
{
  "photo": {
    "assetPath": "photos/owner-photo-set-2026-10/1048.webp",
    "altText": "Standing subject with a relaxed hand raised in a wave.",
    "collectionId": "owner-photo-set-2026-10",
    "source": "Owner-supplied batch, file 48",
    "credit": "Exact approved public credit",
    "license": "Exact confirmed redistribution terms"
  }
}
```

This record illustrates the optional schema; it does not state that these files have a confirmed license or have been imported. Keep `photo` absent for illustrated guides. `assetPath` is relative to `app/src/main/assets/` and must point to an existing bundled JPG, PNG or WebP under `photos/`, with no remote URLs or path traversal. Every reference requires nonblank `altText`, `source`, `credit`, `license` and `collectionId`. Use Coil's bundled-asset loader and accessible descriptions. If an asset cannot load, show the original illustration and a clear fallback label rather than a blank card.

`PoseCatalog` loads `poses.json` first, then an optional `photo-poses.json`. It appends new entries, rejects ID collisions and requires a valid photo on each entry in the optional catalog. Only a missing optional file is ignored; malformed published metadata and missing referenced assets fail validation. There is no `photo-poses.json` in the app while the new batch remains pending.

`source` records provenance; it does not by itself establish permission. `credit` must match the rights holder's requested wording. `license` must describe actual confirmed terms rather than an assumed license or a generic “royalty-free” label. Keep private permission correspondence and any releases securely outside the app; retain only appropriate public attribution and evidence references in repository metadata.

## Rights information needed before release

Confirm whether the photos are the owner's originals, commissioned work licensed for redistribution, generated originals with applicable usage rights, or another explicitly licensed source. Confirm that the permission permits distribution inside an Android app and its free/premium guides, and provide the required public credit and any restrictions. Include permission to use images in Play screenshots or marketing only if those uses are intended. Where applicable, retain the necessary subject or property releases. Do not infer ownership from the upload, its filename or the absence of a watermark.

The latest message authorizes preparing this new batch for the app. Explicit rights and credit information have not yet been supplied. The earlier instruction excluding earlier uploads remains in force.

## Import and review workflow

1. Inventory the received files outside Git: series, photo number, original filename, dimensions, SHA-256, revision and permission status. Reconcile all 48 unique photographs and the final series-to-guide mapping before publishing the collection; do not silently fill missing photos with older uploads.
2. Match each approved photo to a guide. Write useful instructions describing the visible action, hands, feet and face. Add camera-angle and lighting guidance where the current model supports it. Keep canonical English filter codes (`Individual`, `Casual`, `Standing`, and so on) unchanged; choose the category from the existing catalog where suitable. Set free/premium status deliberately rather than deriving it from a filename.
3. Preserve the originals privately. Create efficient, visually reviewed bundled copies after approval, stripping unnecessary EXIF/GPS metadata. Maintain aspect ratio and retain important hands and feet in detail views. Choose compression and size from actual phone inspection, not a fixed file-size target alone.
4. Add approved catalog entries and photo metadata in one reviewed change. Keep all existing IDs and data meanings. Bundled images are available offline and do not need photo-library permissions, uploads or account access. Bundled photos cannot be individually removed from the installed APK; do not label them as removable downloads.
5. Add corresponding Amharic text entries by stable ID. `PoseTranslations` falls back to English when a new translation is absent and preserves non-text metadata through `copy`. Do not translate saved filter keys or asset paths. Update catalog tests deliberately if the new collection extends the published catalog; do not weaken checks just to satisfy a new count.
6. Validate the catalog, duplicate IDs, missing files and image metadata; build and run relevant Android tests. Inspect the actual Home, Explore, detail and Favorites screens, including TalkBack descriptions, landscape/small-screen behavior, offline mode, missing-image fallback and saved legacy guides after an update.
7. Rebuild the debug APK and signed release bundle only after the approved content passes review. Increase the version code before a new Play upload. Refresh store screenshots and any claims about guide counts. Never include pending originals, private permission evidence, signing credentials or earlier excluded images in the release package.

The existing validation command is `python3 tools/validate_content.py`. It checks the original catalog and, if present, the optional photo catalog, metadata, duplicate IDs and image files. It cannot establish image rights or validate unimported pending batches. The exact source/translation count assertions in `PoseTranslationsTest` also describe the original 48-guide catalog and must be extended consciously when a new approved catalog is shipped.

## Current pending items

- Another 13 photos, their series/number mapping and confirmation that the complete set has been sent.
- Rights, redistribution terms, required credit and any use restrictions for this new batch.
- Final number-to-guide mapping, original instructions and translation review.
- Asset import, build/device checks and a rebuilt release containing the approved collection.

This document provides a batch workflow and proposed metadata. It does not claim that the pending photographs are already in the installed app.

## Local batch intake helper

Run `python3 tools/stage_photo_batch.py <attachment-directory> /workspace/hello-photography-photo-intake`
to collect the next numbered batch. This helper verifies image files, records
SHA-256/dimensions, distinguishes male/female/couple/unnamed numbered series, preserves
earlier file paths and IDs, and reports the remaining photo count. Its manifest
records received numbers by series rather than an incorrect global missing range.
Conflicting replacements, duplicate identities and pose-ID collisions require review. It
copies originals without pixel edits and never publishes into app assets. It
requires Pillow, also used by the existing store artwork tooling. Keep the
intake directory outside Git. The app collection is imported only after its
rights, instructions and final batch have been reviewed.
