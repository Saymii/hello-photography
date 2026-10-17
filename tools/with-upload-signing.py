#!/usr/bin/env python3
"""Run a build with private upload-signing settings; never print credential values."""
import json,os,subprocess,sys
from pathlib import Path
NAMES=('HELLO_UPLOAD_KEYSTORE','HELLO_UPLOAD_STORE_PASSWORD','HELLO_UPLOAD_KEY_ALIAS','HELLO_UPLOAD_KEY_PASSWORD')
if len(sys.argv)<2:
 raise SystemExit('Usage: python3 tools/with-upload-signing.py /secure/signing.json [command ...]')
config=Path(sys.argv[1]).expanduser().resolve()
settings=json.loads(config.read_text())
missing=[name for name in NAMES if not isinstance(settings.get(name),str) or not settings[name]]
if missing:raise SystemExit('Missing signing configuration fields: '+', '.join(missing))
key=Path(settings['HELLO_UPLOAD_KEYSTORE']).expanduser()
if not key.is_absolute():key=config.parent/key
if not key.is_file():raise SystemExit('The configured upload keystore is unavailable')
settings['HELLO_UPLOAD_KEYSTORE']=str(key.resolve())
environment=os.environ.copy()
for name in NAMES:environment[name]=settings[name]
command=sys.argv[2:] or ['./gradlew','--no-daemon','bundleRelease']
raise SystemExit(subprocess.call(command,env=environment))
