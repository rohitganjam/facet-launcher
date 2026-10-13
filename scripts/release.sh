#!/usr/bin/env bash
# Bumps the app's versionName/versionCode in app/build.gradle.kts, then builds a signed
# production release (APK + AAB). See CLAUDE.md's "Production (release) build" section.
#
# Usage:
#   scripts/release.sh                 # bump patch version (x.y.Z -> x.y.(Z+1)), then build
#   scripts/release.sh 0.2.0           # set an explicit version instead of bumping the patch
#   scripts/release.sh --no-build      # only bump the version, skip the release build
#   scripts/release.sh 0.2.0 --no-build
#   scripts/release.sh --notes-only    # bump + draft the in-app What's new, then stop for review
#   scripts/release.sh --resume        # build the version already bumped by --notes-only (or a stopped run)
#
# In-app "What's new": after the bump and BEFORE the build, scripts/gen-whats-new.py drafts
# app/src/main/assets/whats_new/<version>.md (user-facing New/Improved bullets only), so the notes are
# bundled into the APK/AAB. The script then shows them and pauses for sign-off: edit the file if you
# want changes, press Enter to build, or q to stop (re-run with --resume to build later). With no
# terminal attached (e.g. run by a tool) it stops right after drafting; edit, then run --resume. A
# draft that still contains TODO (claude was unreachable) is never built. This is separate from, and
# does not replace, the RELEASE_NOTES.md / release_notes/<version>.md change log described below.
#
# versionCode always increments by exactly 1, regardless of how versionName changes — Play
# Store requires every uploaded build to carry a strictly higher versionCode than the last.
#
# A real release build (i.e. not --no-build) refuses to run unless the working tree is fully
# committed — `git status --porcelain` must be empty. This guarantees the APK/AAB are built from
# exactly one commit, no uncommitted edits mixed in, so the commit id recorded afterward is an
# honest description of what was actually built.
#
# On a successful build, the commit id HEAD pointed to (before this script's own version-bump
# edit) is written to scripts/last-release.json alongside the version — the start point for next
# time's release notes (`git log <that commit>..HEAD`). Not committed automatically; review and
# commit it alongside the version bump, same as always.
#
# RELEASE_NOTES.md is (re)generated automatically right after a successful build, covering every
# commit since the *previous* build recorded in scripts/last-release.json, categorized into New
# Features / Fixes / Improvements / Internal (see scripts/gen-release-notes.py — heuristic, always
# worth a skim before sharing externally). Printed to the terminal and written to RELEASE_NOTES.md
# (always the latest release) AND release_notes/<version>.md (a permanent per-release copy, never
# overwritten — the history RELEASE_NOTES.md alone can't keep since it's replaced every run); not
# committed automatically, same as the version bump.
#
# Alongside the plain app-release.apk/.aab, a version-named copy (app-release-<versionName>.apk/.aab)
# is written next to it, so successive releases in the same output directory don't overwrite each
# other's artifacts.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_GRADLE="$REPO_ROOT/app/build.gradle.kts"
LAST_RELEASE_FILE="$REPO_ROOT/scripts/last-release.json"

do_build=true
make_notes=true
resume=false
explicit_version=""

for arg in "$@"; do
    case "$arg" in
        --no-build)
            do_build=false
            make_notes=false
            ;;
        --notes-only)
            do_build=false
            ;;
        --resume)
            resume=true
            make_notes=false
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

notes_dir_rel="app/src/main/assets/whats_new"
if [[ "$resume" == true ]]; then
    # Resuming a bumped-but-unbuilt release: the tree is dirty on purpose, but only with this release's own files.
    build_commit="$(cd "$REPO_ROOT" && git rev-parse HEAD)"
    previous_commit=""
    if [[ -f "$LAST_RELEASE_FILE" ]]; then
        previous_commit="$(python3 -c "import json,sys; print(json.load(open(sys.argv[1])).get('commit',''))" "$LAST_RELEASE_FILE" 2>/dev/null || true)"
    fi
elif [[ "$do_build" == true || "$make_notes" == true ]]; then
    if [[ -n "$(cd "$REPO_ROOT" && git status --porcelain)" ]]; then
        echo "error: working tree has uncommitted changes — commit or stash everything before building a release." >&2
        echo "       (scripts/release.sh --no-build still works if you just want to bump the version;" >&2
        echo "        use --resume to build a version that --notes-only already bumped.)" >&2
        exit 1
    fi
    build_commit="$(cd "$REPO_ROOT" && git rev-parse HEAD)"
    # Captured before this run's own last-release.json write, below — the previous release's
    # build commit, i.e. the start point for this run's release notes.
    previous_commit=""
    if [[ -f "$LAST_RELEASE_FILE" ]]; then
        previous_commit="$(python3 -c "import json,sys; print(json.load(open(sys.argv[1])).get('commit',''))" "$LAST_RELEASE_FILE" 2>/dev/null || true)"
    fi
fi

current_version="$(grep -m1 'versionName = "' "$BUILD_GRADLE" | sed -E 's/.*versionName = "([^"]+)".*/\1/')"
current_code="$(grep -m1 'versionCode = ' "$BUILD_GRADLE" | sed -E 's/.*versionCode = ([0-9]+).*/\1/')"

if [[ -z "$current_version" || -z "$current_code" ]]; then
    echo "error: couldn't find versionName/versionCode in $BUILD_GRADLE" >&2
    exit 1
fi

validate_notes() {
    if grep -q 'TODO' "$notes_file"; then
        echo "error: $notes_file still contains TODO — finish the in-app notes first." >&2
        return 1
    fi
    if ! grep -qE '^[-*] .+' "$notes_file"; then
        echo "error: $notes_file has no bullet points." >&2
        return 1
    fi
}

if [[ "$resume" == true ]]; then
    new_version="$current_version"
    new_code="$current_code"
    notes_file="$REPO_ROOT/$notes_dir_rel/$new_version.md"
    last_version=""
    if [[ -f "$LAST_RELEASE_FILE" ]]; then
        last_version="$(python3 -c "import json,sys; print(json.load(open(sys.argv[1])).get('version',''))" "$LAST_RELEASE_FILE" 2>/dev/null || true)"
    fi
    if [[ "$new_version" == "$last_version" ]]; then
        echo "error: nothing to resume — $new_version is already the last built release (scripts/last-release.json)." >&2
        exit 1
    fi
    if [[ ! -f "$notes_file" ]]; then
        echo "error: $notes_dir_rel/$new_version.md not found — run scripts/release.sh --notes-only first." >&2
        exit 1
    fi
    unexpected="$(cd "$REPO_ROOT" && git status --porcelain --untracked-files=all | grep -v -E "^( M|\?\?|M | A|A ) (app/build\.gradle\.kts|${notes_dir_rel//./\\.}/${new_version//./\\.}\.md)$" || true)"
    if [[ -n "$unexpected" ]]; then
        echo "error: --resume only expects the version bump and $notes_dir_rel/$new_version.md to be uncommitted, but found:" >&2
        echo "$unexpected" >&2
        exit 1
    fi
    validate_notes
    echo "Resuming release $new_version ($new_code) with the in-app notes in $notes_dir_rel/$new_version.md."
else
    if [[ -n "$explicit_version" ]]; then
        new_version="$explicit_version"
    else
        IFS='.' read -r major minor patch <<< "$current_version"
        new_version="$major.$minor.$((patch + 1))"
    fi
    new_code=$((current_code + 1))
    notes_file="$REPO_ROOT/$notes_dir_rel/$new_version.md"

    echo "Version: $current_version ($current_code) -> $new_version ($new_code)"

    sed -E "s/versionCode = [0-9]+/versionCode = $new_code/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp"
    mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"
    sed -E "s/versionName = \"[^\"]+\"/versionName = \"$new_version\"/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp"
    mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"
fi

if [[ "$make_notes" == true ]]; then
    echo
    echo "Drafting the in-app What's new for $new_version..."
    (cd "$REPO_ROOT" && python3 scripts/gen-whats-new.py --since "$previous_commit" --to "$build_commit" --version "$new_version") > /dev/null || true

    echo
    echo "In-app What's new for $new_version ($notes_dir_rel/$new_version.md):"
    echo
    cat "$notes_file"
    echo

    if [[ "$do_build" == false ]]; then
        echo "Version bumped and notes drafted. Edit $notes_dir_rel/$new_version.md if you want changes, then run: scripts/release.sh --resume"
        exit 0
    fi
    if [[ ! -t 0 ]]; then
        echo "Stopped for sign-off (no terminal to ask). Edit $notes_dir_rel/$new_version.md if you want changes, then run: scripts/release.sh --resume"
        exit 0
    fi
    while true; do
        read -r -p "Edit the notes file if you want changes, then press Enter to build (q to stop; build later with --resume): " answer
        if [[ "$answer" == "q" ]]; then
            echo "Stopped. Version bumped and notes drafted; run scripts/release.sh --resume to build."
            exit 0
        fi
        validate_notes && break
    done
fi

if [[ "$do_build" == true ]]; then
    echo "Building signed release APK + AAB..."
    (cd "$REPO_ROOT" && ./gradlew assembleRelease bundleRelease)

    apk_src="$REPO_ROOT/app/build/outputs/apk/release/app-release.apk"
    aab_src="$REPO_ROOT/app/build/outputs/bundle/release/app-release.aab"
    apk_versioned="$REPO_ROOT/app/build/outputs/apk/release/app-release-$new_version.apk"
    aab_versioned="$REPO_ROOT/app/build/outputs/bundle/release/app-release-$new_version.aab"
    cp "$apk_src" "$apk_versioned"
    cp "$aab_src" "$aab_versioned"

    echo
    echo "APK: app/build/outputs/apk/release/app-release-$new_version.apk"
    echo "AAB: app/build/outputs/bundle/release/app-release-$new_version.aab"

    if [[ -n "$previous_commit" ]]; then
        echo
        echo "Release notes ($previous_commit -> $build_commit):"
        echo
        # Best-effort — a hiccup here (claude CLI missing/not logged in/timed out) shouldn't fail
        # the whole script after a successful build; the APK/AAB already exist regardless.
        if (cd "$REPO_ROOT" && python3 scripts/gen-release-notes.py --since "$previous_commit" --to "$build_commit" --version "$new_version"); then
            echo "Written to RELEASE_NOTES.md and release_notes/$new_version.md."
        else
            echo "warning: release notes generation failed — see error above. Build artifacts are unaffected." >&2
        fi
    else
        echo
        echo "No previous scripts/last-release.json found — skipping release notes (nothing to start from)."
        echo "Run scripts/gen-release-notes.py manually with an explicit --since once you have one."
    fi

    built_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    cat > "$LAST_RELEASE_FILE" <<EOF
{
  "version": "$new_version",
  "versionCode": $new_code,
  "commit": "$build_commit",
  "builtAt": "$built_at"
}
EOF
    echo
    echo "Recorded build commit $build_commit in scripts/last-release.json — next release's notes"
    echo "can start from there: git log $build_commit..HEAD"
fi

echo
if [[ "$do_build" == true ]]; then
    echo "Done — review the version bump in app/build.gradle.kts, scripts/last-release.json, RELEASE_NOTES.md, release_notes/$new_version.md and $notes_dir_rel/$new_version.md (the in-app What's new), and commit them when ready."
else
    echo "Done — review the version bump in app/build.gradle.kts and commit when ready."
fi
