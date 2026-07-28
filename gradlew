#!/usr/bin/env sh
set -eu
GRADLE_VERSION=8.9
if command -v gradle >/dev/null 2>&1; then exec gradle "$@"; fi
BASE="${HOME}/.gradle/petlingo-gradle-${GRADLE_VERSION}"
ZIP="${BASE}/gradle.zip"
DIST="${BASE}/gradle-${GRADLE_VERSION}"
mkdir -p "$BASE"
if [ ! -x "${DIST}/bin/gradle" ]; then
  echo "Downloading Gradle ${GRADLE_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail --retry 3 "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  else
    echo "ERROR: curl or wget is required." >&2; exit 1
  fi
  rm -rf "$DIST"
  unzip -q "$ZIP" -d "$BASE"
fi
exec "${DIST}/bin/gradle" "$@"
