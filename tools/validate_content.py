"""Validate original catalog before publishing. Run from the project root."""
import json
from pathlib import Path
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
