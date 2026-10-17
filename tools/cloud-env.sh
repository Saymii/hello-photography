#!/usr/bin/env bash
# Source in the managed cloud machine; leaves user HOME untouched.
export JAVA_HOME=/workspace/tooling/jdk
export ANDROID_HOME=/workspace/tooling/android-sdk
export ANDROID_USER_HOME=/workspace/tooling/android-user
export GRADLE_USER_HOME=/workspace/tooling/gradle-home
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Duser.home=/workspace/tooling/java-user -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
# Java clients require explicit use of this machine's injected HTTPS proxy.
if [ -n "${HTTPS_PROXY:-}" ]; then
  read -r pose_proxy_host pose_proxy_port < <(python3 -c 'import os,urllib.parse; u=urllib.parse.urlparse(os.environ["HTTPS_PROXY"]); print(u.hostname,u.port or 80)')
  export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dhttps.proxyHost=$pose_proxy_host -Dhttps.proxyPort=$pose_proxy_port -Dhttp.proxyHost=$pose_proxy_host -Dhttp.proxyPort=$pose_proxy_port"
fi
