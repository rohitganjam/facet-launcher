# Facet Launcher 0.1.23

## New Features
- Added a setting to toggle the calendar colour indicator bar on Home screen events, and to choose how many upcoming events are shown (1-3) — available app-wide and per-facet.

## Fixes
- Fixed calendar colour dots still showing in the calendar picker even when the colour indicator setting was turned off.

## Improvements
- Upcoming events on the Home screen now show through noon tomorrow instead of just today, with a weekday label (e.g. "Tue 9:00") on events that aren't happening today.
- The calendar preview strip now shows your real upcoming events once calendar access is granted, and sample events when there are none.

## Internal
- Released 0.1.22 (code 23) version bump.
- Added Room database v27 migration, DataStore keys, and backup fields for the new calendar settings.
- Extracted calendar-pairing logic out of ObserveHomeScreenStateUseCase to satisfy detekt's LongMethod rule.
- Fixed a French translation string that was breaking resource merging.
- Updated ViewModel and UI tests for the new wallpaper repository dependency and calendar indicator feature.
- Fixed an intermittent failure in the calendar row zero-count test by waiting for real state instead of asserting immediately.
- Fixed API 30 test flakiness: scroll to the Done button before tapping in FacetProContentTest, and use @SdkSuppress instead of assumeTrue for wallpaper-accent tests.
- Refreshed the graphify knowledge graph.
