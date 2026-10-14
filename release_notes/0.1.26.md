# Facet Launcher 0.1.26

## New Features
- Added a "What's new" screen under Settings → About, listing what's new and improved in each release.
- Added a 3-column option for the Home screen app grid, alongside the existing 4, 5, and 6 column choices.
- The long-press menu for an app now shows its notifications directly, with a setting to turn this on or off (on by default). You can tap a notification to open it, or swipe/use "Clear all" to dismiss it.

## Fixes
- Fixed the media player's progress wave so it correctly flattens out when playback is paused or stopped, and animates again on resume.

## Improvements
- Redesigned the app long-press menu into clearer sections: app info/uninstall, notifications, and placement actions (favorites, dock, folder).
- Placement actions (add to favorites/dock/folder) in the long-press menu are now clearer buttons that show whether the app is already placed there, and adapt their layout for long app names or larger text sizes.
- Removed app shortcuts from the long-press menu to keep it simpler.
- Removed the separate flat/wavy progress style setting for the media player, since the wave now always animates appropriately on its own.
- Updated the Play Store listing and permission explanations to better describe the app's features and data usage.

## Internal
- Refreshed the graphify knowledge graph (multiple commits).
- Fixed a flaky test by measuring the media progress wave's ink by comparing pixel colors against the background instead of counting any pixel with alpha.
- Added a test for the media progress wave flattening on playback stop.
- Added tests for the new notification zone, placement layout at various text scales, and gesture handling in the long-press sheet; updated docs, plan, and test registry.
- Added tests for the 3-column grid option (enum, backup round trip, per-facet persistence, UI).
- Added `scripts/gen-whats-new.py` and wired release note drafting into `scripts/release.sh`, with `--notes-only` and `--resume` flags and a check preventing release with unfinished notes.
- Backfilled release notes content for versions 0.1.10–0.1.25.
- Released version 0.1.25 (code 26).
