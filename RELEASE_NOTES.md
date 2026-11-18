# Facet Launcher 0.2.2

## New Features
- **Custom app icons:** Long-press an app and tap its icon (marked with a pencil) to choose a different one. Pick the app's own icon, its version from any installed icon pack, or any icon a pack offers, with search and categories. Manage them in Settings > Icons > Custom icons, with Reset and Reset all. Custom icons are included in backups.
- **App Vault:** Hide apps behind a 4-digit PIN. Open it from the drawer's 3-dot menu, set your PIN, then see your hidden apps and open App info or remove them from the vault. Vault apps stay out of every list, and the vault is free and not included in backups.
- **Dim glass style:** A new dark-leaning counterpart to Bright, with a light blur, a darkened backdrop and a subtle edge. It is available in every glass picker and as an icon background.
- **Frosted panels:** The app drawer, widget hub and facet carousel now appear as frosted glass over your wallpaper (light frost in light themes, dark frost in dark themes). It turns on automatically.
- **Wallpaper capture for foldables and rotation:** Capture your wallpaper once per screen, with a single approval covering both portrait and landscape. Foldables are supported, with a separate copy for each screen. Facet suggests a capture when a screen has none.
- **Backup of automation rules:** Backups now include your automation rules. Restoring skips rules whose facet no longer exists and reports skipped, paused or permission-waiting rules.
- **Play Store search:** When drawer search finds nothing, a "Search Play Store" button appears.
- **Smarter search:** One improved search for the drawer and pickers that handles accents, capitalization, initials, partial words and multiple words, and learns the apps you pick for a query.

## Fixes
- Pressing Home with the drawer's search open no longer makes the search panel flicker back into view over Home.
- Wallpaper previews now match your screen's shape, so unfolded or rotated screens no longer show a tall preview.
- Photos framed on a wide screen are no longer stretched.
- Daily photos that would need too much enlargement are now skipped, so they no longer look blown up.
- The pencil badge on the app menu icon is no longer clipped at its edges.
- Pressing Enter in drawer search now opens the first contact or setting when no app matches.
- A first-frame flash of the widget hub on startup is fixed.

## Improvements
- Wallpaper colours now come from Facet's own wallpaper copy using Material You, so they match what you see, and Android 11 gets wallpaper colours for the first time.
- Home press now slides the search sheet out smoothly together with the drawer.
- Wallpaper glass now works correctly in each screen orientation, and your wallpaper is centred properly in Facet's fit step on newer Android versions.
- The Recently installed icon now follows your icon style, tile, glass and shape settings.
- Light and Dark frost now slightly brighten or darken the wallpaper behind them.
- Both Facet Pro screens now list every Pro feature, and replacing the clock with a widget is no longer listed as a Pro feature.
- Recents, Most used and the drawer's frequent and last-used lists no longer need Usage Access permission.
- Home shows a "Capture wallpaper for glass effects" button, and a rotation now cross-fades smoothly.
- The Private Space and Secure Folder intro text is clearer about hiding and pausing apps and notifications.
- The Settings entry is now called "App Drawer and Search".
- Backups made with the old drawer opacity setting still import. The drawer's opacity slider has been removed.
- Wallpaper notices were reworded in six languages, with a note on the wallpaper page for live wallpapers.

## Internal
- Version bump to 0.2.2 and Release 0.2.1; dropped 0.1.44 What's new notes.
- Room schema bumps: v51 (launch history, facet UUID and unique index), v52 (custom app icons), v53 export.
- Added MediaProjection capture service; glass copies kept per view with originals streamed to disk.
- Replaced manual screenshot flow; stale screens pruned.
- Added WallpaperPaletteRepository (material-color-utilities 6.0.0) and LocalWallpaperPalette.
- Added RankBySearchRelevanceUseCase and LaunchHistoryRepository; removed PACKAGE_USAGE_STATS.
- AppStoreRepository added for Play Store search.
- Fixed detekt/lint findings (App Vault, custom icons, smart search, carousel and hub split into smaller composables), BackupRestoreViewModelTest and migration test fixes (49→50 allowLandscape), pencil badge test using unmerged tree, LaunchHistoryRepositoryTest Mockito stubs.
- App Vault: facet_vault DataStore with Keystore-keyed PIN hash; AppVaultCallbacks moved to its own file.
- Pencil badge now an M3 Badge in BadgedBox.
- Plan/docs updates: verification ticks, rule backup docs, architecture docs, PRD/design spec, test registry.
- Backup strings translated.
