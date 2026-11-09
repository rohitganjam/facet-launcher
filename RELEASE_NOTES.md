# Facet Launcher 0.1.41

## New Features
- Drag and rearrange items on the Home grid: press and hold to pick one up, then drop it on an empty spot to move it or on another item to swap. The grid scrolls automatically near the edges while you drag.
- Press and hold a Home list row, Dock icon, or open-folder row to pick it up and reorder it. Home swipes pause while you hold an item.
- Swipe between tabs and see the next tab slide in beside the current one. Works in the Drawer, the app picker, and the widget picker.
- New "Adjust widget size & position" option in the Home sheet for the widget currently showing.
- "Switch facet" option in the Home sheet.
- Each facet now has its own Dock and Favorites, and its own "Allow landscape mode" setting.
- New "Set up your app drawer" step in onboarding, with a drawer preview and a "Folders in drawer" option.
- Folder screen in Settings has a three-dot menu with rename, Dock/Favorites, and delete options.
- Gesture options for left, right, and down swipes, plus a swap-left-and-right option.
- Favorites and Apps can now show up to 20 items.
- Your Home grid layout is now included in backups and restored on import.

## Fixes
- Fixed a facet's landscape row position showing as stuck on the left instead of "same as portrait".

## Improvements
- Home fades out smoothly as you pull down the notification shade, and stays hidden until the shade covers the screen.
- Weather locations are more precise, and your current location now shows your neighbourhood and city.
- The drawer preview in onboarding and App drawer settings now shows your real installed apps and icons.
- Onboarding's home step is now titled "Set up your first facet".
- New facets start with your device's default apps in the Dock, an empty Favorites list, and a clock. The Add facet card now describes this.
- The Home grid now spans the full screen like the Dock, with matching column spacing, and its row height matches the App Drawer.
- Dock icons are evenly spaced to line up with the Home grid columns.
- A full Favorites or Dock now shows a "limit reached" row.
- New items are placed in the first free spot on the grid.
- Dragging in the two-column list works across both columns, with sliding rows and automatic scrolling. The item you're holding stays visible past the list edge.
- Layout options on the Apps list screen now sit together at the top of the list content card.
- Dock style and apps list look now live on the Dock and Apps list screens, replacing the separate Dock & Apps layout page.
- The App Drawer grid now defaults to Regular density.
- "Allow landscape mode" now lives with each facet's Apps list settings instead of one setting for the whole launcher.
- Removed the shared launcher-wide Default Dock, Default Apps list, and Favorites reset screens, since every facet now manages its own.

## Internal
- Reset last-release.json to 0.1.40 so 0.1.41 can be rebuilt; bump to 0.1.41 with release notes.
- Room migrations: 47 adds nullable portrait/landscape grid cells on placements; 47->48 deletes shared Dock/Favorites rows; 48->49 adds Facet.allowLandscape, seeded from the old DataStore key.
- Removed LauncherSettings.allowLandscape and the Settings card; LauncherViewModel follows the active facet; BackupFacet carries the flag.
- Removed launcher-scope Apps list route and Copy to facets; HomeAppsListSettingsScreen is facet-only; PlacementTarget has no global list.
- Added SeedFacetDefaultsUseCase; removed launcher-wide seed and defaultsSeeded; onboarding pickers edit the first facet's lists.
- Fixed Converters.toAppRowPosition(null) returning LEFT; covered by a real-Room test.
- Backups no longer write or restore the shared Dock/Favorites; added BackupGridCells for Favorites apps and folder placements.
- Placement repository: observeGridCells/saveGridCells per orientation; reorderItems keeps saved cells; grid drags save cells only, list keeps its own order.
- Added pure HomeGridPlacement logic and HomeAppGrid fixed-height cell layout; QuickPlacementAction.Full; dropped unused compact and reading-order helpers.
- SwipeTabs component; Home list/grid drag popup rendering and edge auto-scroll.
- Stopped tracking graphify-out (post-commit hook rebuilds it; tracked files left the tree dirty and blocked scripts/release.sh).
- Fixed lint errors (NonObservableLocale, unremembered state, VM-in-composable); drag-left HomeDrawerRouteTest now starts on screen; removed picker maxFavorites no-op.
- Weather coordinates use WeatherPlace.COORDINATE_DECIMALS (3) for places and MET requests; reverse lookup returns "subLocality, city"; city picker search keeps plain city name.
- Updated menu tests for removed scope badge; added scroll tests; docs, plans, and test registry updated; knowledge graph updated.
