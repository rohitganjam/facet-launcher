# Facet Launcher 0.1.41

## New Features
- Edit Home in place: long-press a Home list row, Dock icon, or open-folder row to pick it up and drag it to reorder; releasing in place opens the options sheet.
- Drag items around Home's grid: drop on an empty cell to move an item, or on another item to swap. Empty slot guides appear and the grid scrolls at the edges while you drag. Positions are saved separately for portrait and landscape.
- Two-column Home list supports dragging across both columns, and lists auto-scroll when you drag a row near the top or bottom.
- Adjust widget size and position directly from the Home sheet (hidden when there are no widgets).
- Switch facet from the Home sheet.
- Swipeable tabs in the Drawer, app picker, and widget picker; the neighbouring tab slides into view as you swipe, and tapped tabs slide in.
- Folder settings screen has a menu for renaming, adding to Dock/Favorites, or deleting.
- New gesture options: left/right/down switches, and swapping left and right.
- Onboarding has a separate "Set up your app drawer" step with a preview and a "Folders in drawer" option.
- Each facet now has its own Dock and Favorites. New facets start with your device's default apps in the Dock and empty Favorites.
- Backups now include Home grid positions.
- Weather shows your neighbourhood name along with the city when available.

## Fixes
- Home now fades out smoothly as you pull down the notification shade, and stays hidden until the shade covers it so it doesn't flash back.
- A held item stays visible when dragged past the edge of the list.
- Home swipe gestures no longer trigger while you're holding an item.
- Dock icons are evenly spaced and line up with the Home grid columns.

## Improvements
- Home grid spans the full screen width and spaces its columns like the Dock.
- Home grid rows now match the regular row height of the App Drawer.
- The drawer preview in onboarding and settings shows your own installed apps with their real icons.
- Onboarding's home step is now titled "Set up your first facet".
- The "Add facet" card explains what a new facet starts with: a clock, your default apps in the Dock, and the launcher's look.
- Dock style now lives on the Dock screen, and the apps list look on the Apps list screen. The separate Dock & Apps layout page is gone.
- App list layout options sit at the top of the list content card.
- Default drawer grid density is now Regular.
- Favorites and Apps can show up to 20 items; single-column rows are 70% of the width.
- A full Dock or Favorites shows a "limit reached" row, and new items take the first free grid cell.
- Weather locations are more precisely rounded.
- Removed the shared Default Dock screen, the Favorites reset, and the Reset button on the Apps list screen (the Dock keeps its own).

## Internal
- Room DB 46→47: nullable portrait/landscape grid cells on placements; 47→48 deletes shared launcher-wide Dock/Favorites rows. Backups no longer write or restore them; BackupGridCells added for Favorites apps and folder placements.
- Removed launcher-wide seed and `defaultsSeeded`; added SeedFacetDefaultsUseCase; onboarding pickers edit the first facet's lists; PlacementTarget has no global list.
- Placement repository: observeGridCells/saveGridCells per orientation; reorderItems keeps saved cells. Grid drags save cells only; list order is separate.
- HomeAppGrid draws from saved cells (fixed-height cells, first free cell from anchor, top/bottom mirroring); pure HomeGridPlacement logic; QuickPlacementAction.Full; removed unused compact and reading-order helpers.
- SwipeTabs component; held row/tile drawn in a popup.
- Weather: coordinates use WeatherPlace.COORDINATE_DECIMALS (3) for places and the MET request; reverse lookup returns "subLocality, city"; city picker search keeps the plain city name.
- Fixed lint errors (NonObservableLocale, unremembered state, VM-in-composable); fixed HomeDrawerRouteTest drag-left start; removed picker maxFavorites no-op.
- Added scroll and menu tests (removed scope badge); updated docs, plans, and test registry.
- Updated knowledge graph; stopped tracking graphify-out (post-commit hook rebuilds it, and a dirty tree blocked scripts/release.sh).
- Release 0.1.40 version bump commit (previous release).
