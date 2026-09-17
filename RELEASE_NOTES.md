# Facet Launcher 0.1.9

## New Features
- Added a new "Folders in drawer" setting (Settings → App Drawer) letting you choose how folders appear in the app drawer: hidden (default), mixed in alphabetically with apps, or pinned together at the start or end of the list.

## Fixes
- Fixed installation being blocked on some Android 12/12L devices (like the Oppo Find X3 Pro) that were incorrectly told the app was incompatible.
- Fixed folders disappearing from the Dock and Favorites preview screens in Settings, and from their reorder lists, when a folder had been added to Favorites.

## Improvements
- Redesigned folder icons to show a cleaner preview of the apps inside (a 1x2 row for 1-2 apps, a 2x2 grid for 3-4).
- Adjusted the light wallpaper color to a brighter white and darkened some onboarding icon colors for better contrast.

## Internal
- Lowered minSdk to 31 to match actual device support; replaced an API-33-only call with a backward-compatible fallback; updated Robolectric test config and permissions to match.
- Generalized folder/app grouping and letter-rail logic to support the new drawer item types; added backup/restore support for the new setting; added 11 new automated tests; updated architecture docs and test registry.
- Updated knowledge graph (x4).
- Release 0.1.8 version bump.
