# Facet Launcher 0.1.29

## New Features
- Landscape mode for Home. When you turn your device sideways, Home puts the clock, your app list and the dock side by side. This works on phones, unfolded foldables and tablets. On short screens the dock sits down the right edge. On taller screens it runs along the bottom in your usual dock style, and the app grid gets wider.
- The Hub has a landscape layout too. Widgets sit in a grid that scrolls sideways. You can drag widgets to reorder them, or use the widget menu to resize or remove them. The landscape order is kept separate, so it doesn't change your portrait layout.
- Adjust mode works in landscape. Drag the divider to set how wide the clock area is, choose top, middle or bottom for the clock, and resize the clock with its corner handles. A custom clock widget can also be resized and moved in landscape.
- Landscape remembers its own clock position, alignment, size, custom clock widget size and app list alignment, separately from portrait. Until you change them, it uses your portrait settings. Dock & Apps appearance now has separate Portrait and Landscape alignment cards.
- A new "Allow landscape mode" switch under Dock & Apps appearance (on by default). Turn it off to keep the launcher in portrait at all times. Your landscape settings come back when you turn it on again.

## Fixes
- Fixed the search field in the app picker missing letters or freezing after you changed the sort order.
- Fixed backups getting corrupted when you saved over an older, larger backup file. Backups damaged this way can now be restored.
- Fixed Launcher settings and the page dots being pushed off screen in the facet carousel on tablets and foldables.
- Fixed a custom clock widget jiggling non-stop in adjust mode.
- Fixed a custom clock widget spilling off screen when its size had been saved on a larger screen.
- Fixed the clock height handle not reaching the empty space above a small clock, and fixed the handle lagging when you reversed a drag.
- Fixed previews showing a facet's clock in the wrong place when that facet has its own clock style.
- Fixed a facet losing its landscape clock alignment when it started using its own clock style.

## Improvements
- Home appears faster when the launcher starts.
- App Drawer settings now have their own screen, opened from the Appearance screen.
- App drawer grid size is now a Grid density setting with Compact, Regular and Airy options. The number of columns and rows adapts to your screen, so one choice works on a phone, foldable or tablet. Your old choice carries over, icons are centered in their columns, and search results use the same grid.
- Custom clock widgets can now be resized down to the size of one Hub grid cell.
- The carousel and Appearance previews now show your real Home screen, scaled down. This includes your custom clock widget and, if you turned on the Home media player, a sample player that shows where it will appear.
- Backups now include your clock and app list settings, your landscape settings and the landscape order of Hub widgets. Older backups still restore.

## Internal
- Release 0.1.28 (29) and 0.1.28 release notes.
- Schema migrations v28→v32: `widget_placements.landscapeOrder`, landscape clock widget size, landscape clock and app-list columns on facets; new `landscape_clock_*` and `landscape_app_*` DataStore keys.
- Landscape building blocks: `homeLayoutModeFor` window-shape rule (width ≥600dp, portrait when taller than wide, compact vs roomy at 480dp), `FlowPackWidgetsUseCase`, `OrderWidgetsForLandscape`, `ReorderLandscapeWidget`, `LandscapeHub` use cases, pure clock geometry functions with unit tests; landscape instrumented tests run in a rotated test activity.
- Landscape twins grouped into `LandscapeOverrides` (`LauncherSettings.landscape`), resolved once in `HomeUiState`; writes go through `updateLandscape`, and view-model commits take a `HomeLayoutMode`.
- `ResolvedFacetStyle` / `resolveFacetStyle` is now the single place facet overrides are resolved (Home, carousel, Appearance, calendar, settings VMs), with per-orientation placement and the hosted clock widget; previews are built via `toPreviewState`.
- Introduced the `Facet` domain model; `FacetEntity` stays in data/local, facet writes take an id and re-read the row; mapper coverage test added.
- HomeScreen refactor: 89 params → state and action groups; layout state moved into `HomeScreenLayout`; clock geometry extracted; adjust overlays split out; frame, slots, portrait/landscape content and effects split; `HomeItemActions`/`HomeItemStyle`; `HomeScreenWiringTest`, `FlatHomeScreen` test shim.
- HomeDrawerRoute split: panels became overlays, `HomeScreenActionsBuilder` added, swipe handling moved into `HomeSwipeGestures.kt` / `HomeSwipeController` with unit tests (1,094 → ~420 lines).
- `ScaledHomeScreen` replaces `HomePreview`; `LocalHomeTagPrefix`; previews use a non-listening `AppWidgetHost`.
- Clock templates take a single `ClockTemplateData`; renderer looked up from a map instead of a `when`. No behavior change.
- `ObservePlacedItemsUseCase` takes over Dock/Favorites hydration from `PlacementRepository`; drawer and calendar use cases are now injected.
- `@IoDispatcher`/`@DefaultDispatcher` qualifiers injected into 14 repositories.
- Backup: export now truncates the file first, import falls back to the first complete JSON object, `BackupCoverageTest` added.
- Startup and memory benchmarks: `reportFullyDrawn` fires once Home has faded in, `ScreenBenchmarks` macrobenchmarks with a seeding receiver and always-Pro override, `scripts/bench-memory.sh` and `scripts/bench-startup.sh`; removed the clock and list settle debounces from the Home reveal.
- Shared `SettingsToggleRow`; App Drawer settings split into smaller composables.
- Detekt baseline trimmed repeatedly (stale entries removed, 177 → 163 in the latest pass).
- Fixed a flaky `NotificationSettingsScreenTest` that asserted before the async state had loaded.
- Docs, plans, test registry and graphify output regenerated or updated.
