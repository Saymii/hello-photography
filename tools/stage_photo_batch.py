#!/usr/bin/env python3
"""Collect numbered owner-photo batches outside Git. Does not publish or edit images."""
import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path
from PIL import Image


def series_for(filename: str) -> str:
    match = re.search(r"(?:^|[-_ ])(male|female)_pose_\d+\.", filename, re.IGNORECASE)
    return match[1].lower() if match else "numbered"


def stage(source: Path, target: Path) -> dict:
    root = Path(__file__).resolve().parents[1]
    source, target = source.resolve(), target.resolve()
    if target == root or root in target.parents:
        raise ValueError("Pending photos must be staged outside the Git checkout")
    if not source.is_dir():
        raise ValueError("Batch source must be an existing directory")
    manifest_path = target / "intake.json"
    manifest = json.loads(manifest_path.read_text()) if manifest_path.exists() else {
        "collectionId": "owner-photo-set-2026-10",
        "expectedCount": 48,
        "rightsStatus": "pending_owner_confirmation",
        "files": [],
    }
    existing = {}
    pose_ids = {}
    for original in manifest["files"]:
        row = dict(original)
        row.setdefault("series", series_for(row["originalName"]))
        key = (row["series"], row["number"])
        if key in existing or row["poseId"] in pose_ids:
            raise ValueError("The staged manifest has duplicate photo identities or pose IDs")
        if hashlib.sha256((target / row["file"]).read_bytes()).hexdigest() != row["sha256"]:
            raise ValueError(f"Staged photo {key} failed its integrity check")
        existing[key] = row
        pose_ids[row["poseId"]] = key
    incoming = {}
    for path in sorted(source.iterdir()):
        if not path.is_file() or path.suffix.lower() not in {".jpg", ".jpeg", ".png", ".webp"}:
            continue
        match = re.search(r"(?:^|[-_ ])(\d+)\.(?:jpe?g|png|webp)$", path.name, re.IGNORECASE)
        if not match or not 1 <= int(match[1]) <= 48:
            raise ValueError(f"Use a final photo number 1–48 in the filename: {path.name}")
        number = int(match[1])
        series = series_for(path.name)
        key = (series, number)
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            width, height = image.size
            if width < 640 or height < 640:
                raise ValueError(f"Photo is too small for a reference guide: {path.name}")
        previous = existing.get(key)
        pose_id = previous["poseId"] if previous else (2000 if series == "female" else 1000) + number
        if pose_id in pose_ids and pose_ids[pose_id] != key:
            raise ValueError(f"Photo {key} would reuse an existing pose ID; review the mapping first")
        row = {"series": series, "number": number, "poseId": pose_id, "originalName": path.name,
               "file": previous["file"] if previous else f"photos/{series}/{number:02d}{path.suffix.lower()}", "sha256": digest,
               "width": width, "height": height, "bytes": path.stat().st_size}
        if key in incoming:
            raise ValueError(f"Duplicate photo identity in this batch: {key}")
        if previous and previous["sha256"] != digest:
            raise ValueError(f"Photo {key} already exists with different content; review the replacement first")
        incoming[key] = (row, path)
        pose_ids[pose_id] = key
    if not incoming:
        raise ValueError("No numbered images were found in the batch")
    if len(set(existing) | set(incoming)) > manifest["expectedCount"]:
        raise ValueError("This batch exceeds the planned collection size; review the expected count first")
    for key, (row, _) in incoming.items():
        destination = target / row["file"]
        if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() != row["sha256"]:
            raise ValueError(f"Staged photo {key} failed its integrity check; review before replacing it")
    # Validate the entire batch before copying any of its files.
    target.mkdir(parents=True, exist_ok=True, mode=0o700)
    (target / "photos").mkdir(exist_ok=True, mode=0o700)
    for key, (row, path) in incoming.items():
        destination = target / row["file"]
        destination.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
        if not destination.exists():
            shutil.copyfile(path, destination)
            destination.chmod(0o600)
        existing[key] = row
    manifest["schemaVersion"] = 2
    manifest["files"] = [existing[key] for key in sorted(existing)]
    manifest.pop("missingNumbers", None)
    manifest["receivedBySeries"] = {
        series: sorted(row["number"] for row in existing.values() if row["series"] == series)
        for series in sorted({key[0] for key in existing})
    }
    manifest["remainingCount"] = manifest["expectedCount"] - len(existing)
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    manifest_path.chmod(0o600)
    return manifest


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("target", type=Path)
    args = parser.parse_args()
    result = stage(args.source, args.target)
    print(f"Received {len(result['files'])}/{result['expectedCount']} photos; remaining {result['remainingCount']}.")
    print("Pending intake only: no app assets, public commits, or release artifacts were changed.")
