#!/usr/bin/env bash
# Bumps the app's versionName/versionCode in app/build.gradle.kts, then builds a signed
# production release (APK + AAB). See CLAUDE.md's "Production (release) build" section.
#
# Usage:
#   scripts/release.sh                 # bump patch version (x.y.Z -> x.y.(Z+1)), then build
#   scripts/release.sh 0.2.0           # set an explicit version instead of bumping the patch
#   scripts/release.sh --no-build      # only bump the version, skip the release build
#   scripts/release.sh 0.2.0 --no-build
#
# versionCode always increments by exactly 1, regardless of how versionName changes — Play
# Store requires every uploaded build to carry a strictly higher versionCode than the last.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_GRADLE="$REPO_ROOT/app/build.gradle.kts"

do_build=true
explicit_version=""

for arg in "$@"; do
    case "$arg" in
        --no-build)
            do_build=false
            ;;
        -h|--help)
            grep '^#' "$0" | sed 's/^#!\?//; s/^ //'
            exit 0
            ;;
        *)
            explicit_version="$arg"
            ;;
    esac
done

if [[ -n "$explicit_version" && ! "$explicit_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "error: version must look like MAJOR.MINOR.PATCH (got '$explicit_version')" >&2
    exit 1
fi

current_version="$(grep -m1 'versionName = "' "$BUILD_GRADLE" | sed -E 's/.*versionName = "([^"]+)".*/\1/')"
current_code="$(grep -m1 'versionCode = ' "$BUILD_GRADLE" | sed -E 's/.*versionCode = ([0-9]+).*/\1/')"

if [[ -z "$current_version" || -z "$current_code" ]]; then
    echo "error: couldn't find versionName/versionCode in $BUILD_GRADLE" >&2
    exit 1
fi

if [[ -n "$explicit_version" ]]; then
    new_version="$explicit_version"
else
    IFS='.' read -r major minor patch <<< "$current_version"
    new_version="$major.$minor.$((patch + 1))"
fi
new_code=$((current_code + 1))

echo "Version: $current_version ($current_code) -> $new_version ($new_code)"

sed -E "s/versionCode = [0-9]+/versionCode = $new_code/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp"
mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"
sed -E "s/versionName = \"[^\"]+\"/versionName = \"$new_version\"/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp"
mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"

if [[ "$do_build" == true ]]; then
    echo "Building signed release APK + AAB..."
    (cd "$REPO_ROOT" && ./gradlew assembleRelease bundleRelease)
    echo
    echo "APK: app/build/outputs/apk/release/app-release.apk"
    echo "AAB: app/build/outputs/bundle/release/app-release.aab"
fi

echo
echo "Done — review the version bump in app/build.gradle.kts and commit when ready."
