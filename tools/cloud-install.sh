#!/usr/bin/env bash
set -euo pipefail
cd /workspace/hello-photography
mkdir -p /workspace/tooling/java-user/.android /workspace/tooling/android-user/cache
if [ ! -x /workspace/tooling/jdk/bin/javac ]; then
  curl -fL https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz -o /workspace/tooling/jdk.tar.gz
  curl -fsSL https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz.sha256 -o /workspace/tooling/jdk.sha256
  python3 - <<'PY'
import hashlib,pathlib
p=pathlib.Path('/workspace/tooling')
assert hashlib.sha256((p/'jdk.tar.gz').read_bytes()).hexdigest()==(p/'jdk.sha256').read_text().strip(), 'JDK checksum mismatch'
PY
  mkdir -p /workspace/tooling/jdk-unpack
  tar -xzf /workspace/tooling/jdk.tar.gz -C /workspace/tooling/jdk-unpack
  pose_jdk_dir=$(find /workspace/tooling/jdk-unpack -mindepth 1 -maxdepth 1 -type d -name 'jdk-*' | head -1)
  ln -s "$pose_jdk_dir" /workspace/tooling/jdk
fi
source tools/cloud-env.sh
if [ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
  curl -fL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o /workspace/tooling/android-tools.zip
  mkdir -p "$ANDROID_HOME/cmdline-tools" /workspace/tooling/sdk-unpack
  unzip -q /workspace/tooling/android-tools.zip -d /workspace/tooling/sdk-unpack
  mv /workspace/tooling/sdk-unpack/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
fi
# sdkmanager can close stdin after accepting all licenses; avoid treating yes SIGPIPE as failure.
set +o pipefail
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --licenses >/tmp/posemaster-sdk-licenses.log
pose_license_status=${PIPESTATUS[1]}
set -o pipefail
[ "$pose_license_status" -eq 0 ]
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
if [ ! -f local.properties ]; then
  printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
fi
./gradlew --no-daemon assembleDebug testDebugUnitTest lintDebug
python3 tools/validate_content.py
(cd backend && npm ci --cache /workspace/tooling/npm-cache && npm test)
mkdir -p artifacts
cp app/build/outputs/apk/debug/app-debug.apk artifacts/HelloPhotography-debug.apk
