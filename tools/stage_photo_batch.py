#!/usr/bin/env python3
"""Collect numbered owner-photo batches outside Git. Does not publish or edit images."""
import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path
from PIL import Image


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
    existing = {row["number"]: row for row in manifest["files"]}
    incoming = {}
    for path in sorted(source.iterdir()):
        if not path.is_file() or path.suffix.lower() not in {".jpg", ".jpeg", ".png", ".webp"}:
            continue
        match = re.search(r"(?:^|[-_ ])(\d+)\.(?:jpe?g|png|webp)$", path.name, re.IGNORECASE)
        if not match or not 1 <= int(match[1]) <= 48:
            raise ValueError(f"Use a final photo number 1–48 in the filename: {path.name}")
        number = int(match[1])
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            width, height = image.size
            if width < 640 or height < 640:
                raise ValueError(f"Photo is too small for a reference guide: {path.name}")
        row = {"number": number, "poseId": 1000 + number, "originalName": path.name,
               "file": f"photos/{number:02d}{path.suffix.lower()}", "sha256": digest,
               "width": width, "height": height, "bytes": path.stat().st_size}
        if number in incoming:
            raise ValueError(f"Duplicate photo number in this batch: {number}")
        if number in existing and existing[number]["sha256"] != digest:
            raise ValueError(f"Photo {number} already exists with different content; review the replacement first")
        incoming[number] = (row, path)
    if not incoming:
        raise ValueError("No numbered images were found in the batch")
    for number, (row, _) in incoming.items():
        destination = target / row["file"]
        if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() != row["sha256"]:
            raise ValueError(f"Staged photo {number} failed its integrity check; review before replacing it")
    # Validate the entire batch before copying any of its files.
    target.mkdir(parents=True, exist_ok=True, mode=0o700)
    (target / "photos").mkdir(exist_ok=True, mode=0o700)
    for number, (row, path) in incoming.items():
        destination = target / row["file"]
        if not destination.exists():
            shutil.copyfile(path, destination)
            destination.chmod(0o600)
        existing[number] = row
    manifest["files"] = [existing[number] for number in sorted(existing)]
    manifest["missingNumbers"] = [n for n in range(1, 49) if n not in existing]
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    manifest_path.chmod(0o600)
    return manifest


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("target", type=Path)
    args = parser.parse_args()
    result = stage(args.source, args.target)
    print(f"Received {len(result['files'])}/48 photos; remaining {len(result['missingNumbers'])}.")
    print("Pending intake only: no app assets, public commits, or release artifacts were changed.")
