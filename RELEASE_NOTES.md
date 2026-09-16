# Facet Launcher 0.1.7

## New Features
- (none)

## Fixes
- Fixed an issue where adding a new Private Space while the launcher was already running could leave the app list out of date until you restarted the launcher.
- Fixed a rare crash that could occur while loading the list of installed apps if a single app's info failed to load; the launcher now skips that one app instead of crashing.

## Improvements
- (none)

## Internal
- Update knowledge graph (x2).
- Add per-entry guard around AppInfo construction in AppRepository.getInstalledApps(), matching the existing per-profile getActivityList() guard.
- Add scripts/gen-release-notes.py and wire it into release.sh to auto-generate categorized RELEASE_NOTES.md via `claude -p` after a successful build (best-effort, non-blocking).
- Release 0.1.6: bump versionName/versionCode.
- release.sh now requires a clean working tree before building and records the built commit in scripts/last-release.json for future release-notes generation; backfilled last-release.json for the 0.1.6 build.
