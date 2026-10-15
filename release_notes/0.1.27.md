# Facet Launcher 0.1.27

## New Features
- Ongoing notifications, such as music playback or active downloads, now show when you long-press an app. You can tap them to open the app. They can't be swiped away, "Clear all" skips them, and "Clear all" is greyed out when only ongoing notifications are left.

## Fixes
- Fixed a crash when opening an app picker on a phone with a Work Profile, if the same app was installed in both your personal and work profiles.
- If an app is installed in both your personal and work profiles, you can now add each copy to the Dock or Favorites separately. Before, adding one copy made the other look like it was already added.
- Fixed apps that showed a notification badge but opened an empty notification list on long-press.
- The preview on the Appearance screen now matches your Home screen, including the clock position and whether the app list sits at the top or bottom.
- The setup screen no longer flashes or reappears by mistake when the launcher starts. If loading takes too long, the launcher now goes to your Home screen instead of setup.

## Improvements
- Work apps now show the Work badge on your Home screen favorites, Home grid and Dock, so you can tell them apart from your personal apps.
- The Work badge now also appears in app pickers, folder lists, Dock and Home app settings, and during setup.
- The launcher's home screen previews now match your actual Home screen more closely.
- Album art for media now uses less memory.
- The launcher now loads faster at startup because it no longer waits for your full app list before deciding whether to show setup.

## Internal
- Onboarding gate: `onboardingCompleted` is now nullable and collected separately from the app list and work profiles. The loading timeout now falls back to "done".
- `decodeArt` reads image bounds first and decodes with a power-of-two `inSampleSize` that keeps both edges at or above 512px. This addresses a Play Console memory warning.
- Added `AppInfo.listKey` (package + activity + user). It replaces the package + activity keys in pickers, folder detail, onboarding reorder lists, the long-press Add/Remove check and the settings reorder keys.
- Home favorites, grid tiles and dock icons now use `AppIcon(app = ...)`, which also passes the notification count through.
- Pickers, folder lists, Dock/Home-apps settings and onboarding lists now use `AppIcon(app = ...)`.
- Carousel and Appearance now share a single scaled `HomePreview`.
- Merged the eight Dock/Favorites tables into `app_placements` and `folder_placements` (schema v28, `MIGRATION_27_28`). `ownerKey` carries the unique keys.
- Merged the four placement repositories into `PlacementRepository` (addressed by `PlacementTarget`), and the eight Add/Remove use cases into `AddToActiveListUseCase`, `RemoveFromActiveListUseCase` and `ResolveActivePlacementTargetUseCase`.
- Dock and favorites caps moved to `AppListLimits`, which resolves finding F3. The uninstall and orphan queries now cover every list.
- Tests: `HomeScreenTest` Work badge coverage, `AppPickerScreenTest` crash reproduction, `AppInfoTest`, picker key and membership tests, `PlacementRepositoryTest`, the v27→v28 migration test, real-Room DAO tests, and persistent-notification tests. 1115 unit and 732 instrumented tests pass.
- Regenerated `TEST_REGISTRY.md` for the selection and picker key tests.
- Docs: CLAUDE.md now checks for a test run that's already going (fixed the running-Gradle pattern) and runs one class per instrumented invocation. Clarified the F6 findings. Updated docs 01–14, the README and the plan.
- Release 0.1.26 (code 27).
