#!/bin/sh
set -eu

if ! command -v gradle >/dev/null 2>&1; then
  echo "ERROR: Gradle is not installed or not available in PATH." >&2
  echo "On GitHub Actions, run gradle/actions/setup-gradle before this script." >&2
  exit 1
fi

exec gradle "$@"
