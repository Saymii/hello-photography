# AI-generated pose photo collection

The owner identified the new collection as AI-generated and requested its inclusion in Hello Photography on 19 October 2026. The intended set is **48 photos**: 12 male, 12 female, 8 couple, 8 group and 8 casual/selfie poses. **39 source files are currently available**. The app must not claim that all 48 have been imported or verified.

| Category | Available | Planned | Outstanding |
| --- | ---: | ---: | --- |
| Male | 12 | 12 | None |
| Female | 10 | 12 | Female poses 11–12 |
| Couple | 8 | 8 | None |
| Group | 1 | 8 | Seven additional group photos |
| Casual/selfie | 8 | 8 | None |
| **Total** | **39** | **48** | **9 photos** |

The one supplied group image uses the owner's global number 40. Casual/selfie images use global numbers 41–48. Do not infer the filenames or numbering of the seven missing group images, duplicate a supplied photo to fill a gap, or substitute unrelated older uploads. The earlier unapproved image batches and RAR archive remain excluded, including as visual inspiration.

## What is bundled

The available collection is recorded in `app/src/main/assets/photo-poses.json`, with mobile WebP copies under `app/src/main/assets/photos/owner-photo-set-2026-10/`. Each photo guide has a title, simple body-positioning instructions, hand/foot/face guidance, a category and filter values, accessible image text and visible source/authorization metadata. The photos are bundled, so reading them does not require a connection, camera access, photo-library access or an account.

The original 48 written guides and their Compose illustrations remain available. Their stable IDs preserve existing favorites and recently viewed guides. New photo guides use their own IDs because a similarly numbered file does not necessarily show the same movement as an existing illustrated guide. Cards and details for the new guides use the corresponding photo. The full-screen viewer preserves the image's aspect ratio and supports closer inspection. A failed image load falls back to a clearly labeled illustration rather than leaving a blank card.

This document describes the source content and import workflow. Check [VALIDATION.md](VALIDATION.md) and the release notes for the exact artifacts and checks completed; changing source files does not update an already installed APK.

## Identity and saved data

| Photo series | Stable guide IDs | Mapping |
| --- | --- | --- |
| Male 01–12 | 1001–1012 | `1000 + male number` |
| Female 01–10 | 2001–2010 | `2000 + female number` |
| Couple 01–08 | 3001–3008 | `3000 + couple number` |
| Group, global 40 | 1040 | Existing intake identity retained |
| Casual/selfie, global 41–48 | 1041–1048 | Existing intake identity retained |

Female 11–12 can use 2011–2012 after delivery and review. Assign the remaining group files unused IDs after checking their actual filenames and inventory; do not collide with the published IDs above. Keep each identity fixed after publication. A revised photo can replace the image for an existing guide when it demonstrates the same pose; a different movement needs a new ID. Collection ID `owner-photo-set-2026-10` identifies the editorial photo set and is separate from a user's favorites collection in Room.

## Originals, mobile copies and provenance

Keep the full-quality uploaded originals in `/workspace/hello-photography-photo-intake`, outside Git and release archives. The intake manifest records each original filename, series/number, dimensions, byte size and SHA-256. Treat these originals as read-only. Mobile optimization creates separate WebP copies with Pillow (quality 84, method 6), a maximum edge of 1,280 pixels, no upscaling and metadata stripped from the derived files. It preserves aspect ratio and important hands, feet and props. Do not overwrite the originals or use optimized files as the only archive. The 39 originals total 2,478,888 bytes; the 39 bundled WebP copies total 1,892,984 bytes, a 23.6% reduction.

`docs/photo-assets.json` is the public provenance and integrity manifest for bundled copies. It records the intended and available counts and connects source hashes to optimized assets. Check this manifest when adding revisions or reconciling duplicate filenames. A hash establishes file identity; it does not establish copyright, a model release or the accuracy of an AI-generated depiction.

The public attribution is **“AI-generated image supplied by Simon Solomon.”** The authorization record describes the owner's request to include this AI-generated collection in the app on **2026-10-19**. This is not a Creative Commons license or a claim that the developer independently verified the generating service's terms, subject releases or third-party rights. Keep any applicable generation-service usage terms, provenance evidence and permission records privately. Do not infer permission to redistribute an image separately from the app or to use it in marketing from the bundled reference metadata alone.

AI-generated reference images can contain anatomical or object errors. Review hands, limbs, contact points and props; write instructions that are comfortable and physically achievable. The image is an educational reference, not proof of a real photograph or a real person's endorsement. The offline pose recommendation assistant continues to use structured rules; the source of these images does not make that assistant a configured AI service.

## Catalog schema

`PoseCatalog` loads `poses.json`, then appends `photo-poses.json`. The nullable `photo` field keeps illustrated guides compatible. Example:

```json
{
  "photo": {
    "assetPath": "photos/owner-photo-set-2026-10/1048.webp",
    "altText": "Standing subject with one relaxed hand raised in a wave.",
    "collectionId": "owner-photo-set-2026-10",
    "source": "AI-generated reference supplied by the app owner",
    "credit": "AI-generated image supplied by Simon Solomon",
    "license": "Owner authorized inclusion in Hello Photography on 2026-10-19; AI-generated reference."
  }
}
```

Use the actual public metadata from the imported catalog when editing an existing entry. Every reference requires nonblank `altText`, `source`, `credit`, `license` and `collectionId`. Asset paths are relative to `app/src/main/assets/`, must resolve to a bundled image under `photos/`, and must not contain remote URLs or path traversal. Keep `photo` absent for illustrated guides. `license` records the supplied authorization without inventing a broader license.

## Adding the remaining photos or revising a guide

1. Receive the actual missing files. Inventory series, number, filename, dimensions and SHA-256 outside Git. Reconcile duplicates or changed files before replacing anything. Use `python3 tools/stage_photo_batch.py <attachment-directory> /workspace/hello-photography-photo-intake` for male/female/couple/group/casual/selfie and globally numbered filename formats. New named group files use IDs 4001–4008; the existing global group image retains ID 1040. Review identities and duplicate hashes before importing. This helper copies originals unchanged and never publishes app assets.
2. Review each source image and match it to its own guide. Write a clear title and simple instructions for the visible action, with hands, feet and face guidance. Keep canonical English filter codes unchanged and choose the correct category. Choose free/premium status explicitly. The supplied new photo guides are free; adding a premium label must not bypass verified purchase requirements.
3. Import with `python3 tools/import_pose_photos.py /workspace/hello-photography-photo-intake` from the project root, after reviewing the inventory and instructions. The importer preserves originals and produces mobile copies. Review their sharpness on a phone. Update `photo-poses.json`, corresponding optimized assets and `docs/photo-assets.json` together. Check source and output hashes. Do not publish originals or private evidence. Bundled photos cannot be individually removed from an installed APK; do not present them as removable downloads.
4. Add reviewed Amharic text by stable ID in `poses-am.json` where available. `PoseTranslations` falls back to English for absent translations and retains image metadata through `copy`. Do not translate asset paths, IDs or saved filter keys. Translate category display labels without changing canonical stored values.
5. Run `python3 tools/import_pose_photos.py --check`, `python3 tools/validate_content.py` and relevant Android catalog/image/viewer tests. Verify every available photo decodes, has a unique guide ID, matches its category and opens from Home/Explore/Favorites/detail/full-screen. Test offline access, zoom controls, small screens, large text/TalkBack, light/dark mode, fallback behavior and legacy favorites after an update. These checks validate content handling; they cannot establish image rights.
6. Rebuild and test the installable APK and signed release bundle. Increase the version code for each Play upload and refresh screenshots from the artifact being submitted. Update counts only to match the actual imported files. Keep billing availability, privacy disclosures and image source labels accurate.

## Before completing the 48-photo collection

Nine source photos remain to be supplied: female 11–12 and seven additional group images. After they arrive, import and validate those actual files, then reconcile the intended 12/12/8/8/8 category counts. Report a successful 48-image check only after all 48 are present and decode successfully. Until then, clearly report the available count and missing files.
