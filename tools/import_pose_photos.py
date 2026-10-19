#!/usr/bin/env python3
"""Import authorized AI pose references, or verify the checked-in mobile copies.

Import: python3 tools/import_pose_photos.py /path/to/private-photo-intake
Verify: python3 tools/import_pose_photos.py --check

Requires Pillow with WebP support. Intake originals are read only and stay outside Git.
The intake must contain intake.json, pose-descriptions-draft.json and its image files.
"""

import argparse
import hashlib
import io
import json
import re
from collections import Counter
from pathlib import Path

import PIL
from PIL import Image, ImageOps, features


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
COLLECTION = "owner-photo-set-2026-10"
CATALOG = ASSETS / "photo-poses.json"
MANIFEST = ROOT / "docs/photo-assets.json"
IMAGE_DIRECTORY = ASSETS / "photos" / COLLECTION
MAX_EDGE = 1280
QUALITY = 84
METHOD = 6
EXPECTED = {
    "Male poses": 12,
    "Female poses": 12,
    "Couple photography": 8,
    "Group photography": 8,
    "Casual & selfie poses": 8,
}
AI_SOURCE = "AI-generated reference supplied by the app owner"
AI_CREDIT = "AI-generated image supplied by Simon Solomon"
AUTHORIZATION = "Owner authorized inclusion in Hello Photography on 2026-10-19; AI-generated reference."
AUTHORIZATION_STATUS = "owner_authorized_ai_generated_inclusion_2026-10-19"
TEXT_FIELDS = ("name", "category", "subject", "style", "location", "position", "difficulty", "steps", "hands", "feet", "face")


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def json_bytes(data) -> bytes:
    return (json.dumps(data, ensure_ascii=False, indent=2) + "\n").encode("utf-8")


def write_file(path: Path, data: bytes) -> None:
    """Replace an output atomically and avoid touching unchanged files."""
    if path.exists() and path.read_bytes() == data:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    pending = path.with_name(path.name + ".importing")
    pending.write_bytes(data)
    pending.replace(path)


def local_path(base: Path, relative: str) -> Path:
    path = (base / relative).resolve()
    if path == base.resolve() or base.resolve() not in path.parents:
        raise ValueError("An image path escapes its expected directory")
    return path


def category_for(row: dict) -> str:
    series, number = row["series"], row["number"]
    if series == "male" and 1 <= number <= 12:
        return "Male poses"
    if series == "female" and 1 <= number <= 12:
        return "Female poses"
    if series == "couple" and 1 <= number <= 8:
        return "Couple photography"
    if series == "group" and 1 <= number <= 8:
        return "Group photography"
    if series in {"casual", "selfie"} and 1 <= number <= 8:
        return "Casual & selfie poses"
    # The numbered intake predates the per-category filenames. Retain its stable IDs.
    if series == "numbered" and 33 <= number <= 40:
        return "Group photography"
    if series == "numbered" and 41 <= number <= 48:
        return "Casual & selfie poses"
    raise ValueError(f"Unrecognized photo identity: {series} {number}")


def validate_guide(guide: dict) -> None:
    if not isinstance(guide.get("id"), int) or guide["id"] <= 48:
        raise ValueError("Photo guides must retain separate IDs above the original 1–48 catalog")
    for field in TEXT_FIELDS:
        if not isinstance(guide.get(field), str) or not guide[field].strip():
            raise ValueError(f"Pose {guide['id']} requires nonempty {field}")
    steps = guide["steps"].splitlines()
    if not 3 <= len(steps) <= 4 or not all(re.match(r"^[1-4]\. \S", step) for step in steps):
        raise ValueError(f"Pose {guide['id']} needs three or four numbered instructions")
    if guide.get("premium") is not False:
        raise ValueError("These owner-supplied references must be freely accessible")
    photo = guide.get("photo", {})
    if photo.get("collectionId") != COLLECTION or not photo.get("altText", "").strip():
        raise ValueError(f"Pose {guide['id']} requires collection metadata and an accessible description")


def import_photos(intake: Path) -> dict:
    intake = intake.resolve()
    if intake == ROOT or ROOT in intake.parents:
        raise ValueError("Preserved originals must remain outside the Git checkout")
    received = json.loads((intake / "intake.json").read_text())
    if received.get("rightsStatus") != AUTHORIZATION_STATUS:
        raise ValueError("The intake must record the owner's authorization before importing")
    guides = json.loads((intake / "pose-descriptions-draft.json").read_text())
    by_id = {row["id"]: row for row in guides}
    originals = {row["poseId"]: row for row in received["files"]}
    if len(by_id) != len(guides) or len(originals) != len(received["files"]) or set(by_id) != set(originals):
        raise ValueError("Every received original must have exactly one matching guide and stable pose ID")
    catalog, assets, outputs = [], [], []
    original_digests = {}
    for pose_id in sorted(originals):
        row = originals[pose_id]
        guide = json.loads(json.dumps(by_id[pose_id]))
        guide["category"] = category_for(row)
        validate_guide(guide)
        source = local_path(intake, row["file"])
        original = source.read_bytes()
        if sha256(original) != row["sha256"] or len(original) != row["bytes"]:
            raise ValueError(f"Original {pose_id} failed its recorded integrity check")
        if row["sha256"] in original_digests:
            raise ValueError(f"Poses {original_digests[row['sha256']]} and {pose_id} reuse the same original")
        original_digests[row["sha256"]] = pose_id
        with Image.open(io.BytesIO(original)) as image:
            image.load()
            if image.size != (row["width"], row["height"]):
                raise ValueError(f"Original {pose_id} dimensions changed")
            mobile = ImageOps.exif_transpose(image).convert("RGB")
            mobile.thumbnail((MAX_EDGE, MAX_EDGE), Image.Resampling.LANCZOS)
            width, height = mobile.size
            encoded = io.BytesIO()
            mobile.save(encoded, format="WEBP", quality=QUALITY, method=METHOD, exact=True)
        data = encoded.getvalue()
        with Image.open(io.BytesIO(data)) as decoded:
            decoded.load()
            if decoded.format != "WEBP" or decoded.size != (width, height):
                raise ValueError(f"Optimized {pose_id} failed decoding")
        asset_path = f"photos/{COLLECTION}/{pose_id}.webp"
        guide["photo"].update(assetPath=asset_path, source=AI_SOURCE, credit=AI_CREDIT, license=AUTHORIZATION)
        catalog.append(guide)
        outputs.append((ASSETS / asset_path, data))
        assets.append({
            "poseId": pose_id,
            "title": guide["name"],
            "category": guide["category"],
            "original": {"fileName": Path(row["originalName"]).name, "sha256": row["sha256"], "width": row["width"], "height": row["height"], "bytes": row["bytes"]},
            "optimized": {"assetPath": asset_path, "sha256": sha256(data), "width": width, "height": height, "bytes": len(data)},
        })
    counts = Counter(row["category"] for row in catalog)
    if any(counts[category] > expected for category, expected in EXPECTED.items()):
        raise ValueError("Received photos exceed one of the owner's category totals")
    manifest = {
        "schemaVersion": 1,
        "collectionId": COLLECTION,
        "source": AI_SOURCE,
        "credit": AI_CREDIT,
        "authorization": AUTHORIZATION,
        "expectedCount": sum(EXPECTED.values()),
        "actualCount": len(assets),
        "missingCount": sum(EXPECTED.values()) - len(assets),
        "status": "complete" if len(assets) == sum(EXPECTED.values()) else "partial_upload",
        "categories": [{"category": category, "expectedCount": expected, "actualCount": counts[category], "missingCount": expected - counts[category]} for category, expected in EXPECTED.items()],
        "optimization": {"format": "WebP", "maxEdge": MAX_EDGE, "upscale": False, "quality": QUALITY, "method": METHOD, "pillowVersion": PIL.__version__, "webpVersion": features.version("webp"), "originalsModified": False, "originalBytes": sum(row["original"]["bytes"] for row in assets), "optimizedBytes": sum(row["optimized"]["bytes"] for row in assets)},
        "assets": assets,
    }
    # A reduced inventory must not replace the catalog before stale files are discovered.
    planned_paths = {path.resolve() for path, _ in outputs}
    existing_paths = {path.resolve() for path in IMAGE_DIRECTORY.glob("*") if path.is_file()}
    if existing_paths - planned_paths:
        raise ValueError("Existing mobile photos are absent from this intake; review removals before importing")
    # Finish validation before writing any public output. Never write to intake originals.
    for path, data in outputs:
        write_file(path, data)
    write_file(CATALOG, json_bytes(catalog))
    write_file(MANIFEST, json_bytes(manifest))
    for pose_id, row in originals.items():
        if sha256(local_path(intake, row["file"]).read_bytes()) != row["sha256"]:
            raise ValueError(f"Original {pose_id} no longer matches its preserved hash")
    return verify()


def verify() -> dict:
    """Decode every public asset and verify hashes, categories, counts and guide metadata."""
    manifest = json.loads(MANIFEST.read_text())
    catalog = json.loads(CATALOG.read_text())
    entries = {row["poseId"]: row for row in manifest["assets"]}
    if len(entries) != len(manifest["assets"]) or {row["id"] for row in catalog} != set(entries) or len(catalog) != len(entries):
        raise ValueError("The public manifest and photo catalog do not match one to one")
    if manifest["actualCount"] != len(entries) or manifest["missingCount"] != 48 - len(entries):
        raise ValueError("Photo counts do not match the actual bundled assets")
    counts = Counter(row["category"] for row in catalog)
    for summary in manifest["categories"]:
        expected = EXPECTED[summary["category"]]
        if (summary["actualCount"], summary["expectedCount"], summary["missingCount"]) != (counts[summary["category"]], expected, expected - counts[summary["category"]]):
            raise ValueError("A category summary is inconsistent with the photo catalog")
    expected_paths = set()
    for guide in catalog:
        validate_guide(guide)
        row = entries[guide["id"]]
        optimized, photo = row["optimized"], guide["photo"]
        relative = f"photos/{COLLECTION}/{guide['id']}.webp"
        if row["category"] != guide["category"] or row["title"] != guide["name"] or optimized["assetPath"] != relative or photo["assetPath"] != relative:
            raise ValueError(f"Pose {guide['id']} has a mismatched image or category")
        if (photo["source"], photo["credit"], photo["license"]) != (AI_SOURCE, AI_CREDIT, AUTHORIZATION):
            raise ValueError("Every photograph must disclose its AI-generated source and owner authorization")
        path = local_path(ASSETS, relative)
        data = path.read_bytes()
        expected_paths.add(path)
        if (sha256(data), len(data)) != (optimized["sha256"], optimized["bytes"]):
            raise ValueError(f"Pose {guide['id']} image hash or size does not match")
        with Image.open(path) as image:
            image.load()
            if image.format != "WEBP" or image.size != (optimized["width"], optimized["height"]) or max(image.size) > MAX_EDGE:
                raise ValueError(f"Pose {guide['id']} failed WebP decoding or dimensions")
            if max(image.size) > max(row["original"]["width"], row["original"]["height"]):
                raise ValueError(f"Pose {guide['id']} was enlarged beyond its original dimensions")
    if {path.resolve() for path in IMAGE_DIRECTORY.glob("*") if path.is_file()} != expected_paths:
        raise ValueError("The mobile photo directory contains unlisted assets")
    if sum(row["optimized"]["bytes"] for row in entries.values()) != manifest["optimization"]["optimizedBytes"]:
        raise ValueError("The optimized size summary is inconsistent")
    return manifest


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("intake", type=Path, nargs="?", help="Private directory holding the preserved originals and draft descriptions")
    parser.add_argument("--check", action="store_true", help="Verify checked-in assets without needing private originals")
    args = parser.parse_args()
    if not args.check and args.intake is None:
        parser.error("provide an intake directory or use --check")
    result = verify() if args.check else import_photos(args.intake)
    sizes = result["optimization"]
    print(f"Verified {result['actualCount']}/{result['expectedCount']} supplied photos; {result['missingCount']} have not been received.")
    print(f"Originals preserved: {sizes['originalBytes']:,} bytes. Mobile WebP copies: {sizes['optimizedBytes']:,} bytes.")
