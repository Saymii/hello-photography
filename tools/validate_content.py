"""Validate original catalog before publishing. Run from the project root."""
import json
from pathlib import Path
import re
poses = json.loads(Path('app/src/main/assets/poses.json').read_text())
assert len(poses) >= 40
assert len({p['id'] for p in poses}) == len(poses)
assert len({p['category'] for p in poses}) >= 24
for p in poses:
    for key in ('name','category','steps','hands','feet','face'):
        assert isinstance(p[key], str) and p[key].strip(), (p['id'], key)
    assert p['subject'] in ('Individual','Couple','Family','Group')
    assert p['difficulty'] in ('Beginner','Intermediate','Professional')
    assert isinstance(p['premium'], bool)
print(f"Validated {len(poses)} original poses across {len({p['category'] for p in poses})} categories")

photo_catalog = Path('app/src/main/assets/photo-poses.json')
if photo_catalog.exists():
    from PIL import Image
    photos = json.loads(photo_catalog.read_text())
    assert isinstance(photos, list), 'photo-poses.json must contain an array'
    ids = {p['id'] for p in poses}
    assets = Path('app/src/main/assets')
    for pose in photos:
        assert type(pose['id']) is int and pose['id'] > 0 and pose['id'] not in ids, 'Duplicate/invalid photo pose ID'
        ids.add(pose['id'])
        for key in ('name','category','steps','hands','feet','face','style','location','position'):
            assert isinstance(pose[key], str) and pose[key].strip(), (pose['id'], key)
        assert pose['subject'] in ('Individual','Couple','Family','Group')
        assert pose['difficulty'] in ('Beginner','Intermediate','Professional')
        assert isinstance(pose['premium'], bool)
        photo = pose['photo']
        for key in ('assetPath','altText','source','credit','license','collectionId'):
            assert isinstance(photo[key], str) and photo[key].strip(), (pose['id'], key)
        segments = photo['assetPath'].split('/')
        assert len(segments) >= 2 and segments[0] == 'photos'
        assert all(re.fullmatch(r'[A-Za-z0-9_-][A-Za-z0-9_.-]*', segment) for segment in segments), photo['assetPath']
        path = assets / photo['assetPath']
        assert path.suffix.lower() in ('.jpg','.jpeg','.png','.webp')
        assert path.is_file() and assets.resolve() in path.resolve().parents, path
        with Image.open(path) as image:
            image.verify()
    print(f"Validated {len(photos)} additional bundled photo guides; review rights evidence before publishing")
