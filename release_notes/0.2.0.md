# Facet Launcher 0.2.0

Covers everything since 0.1.43, including the 0.1.44 closed beta.

## New Features
- **Daily wallpaper:** Facet can now change your wallpaper once a day using a photo from Bing or Windows Spotlight. Turn it on from the switch in Daily photos. It only checks for a new photo when you return to the home screen. It follows your Wi-Fi-only and Battery Saver settings, and it swaps in the new photo the next time your screen turns off so the change doesn't interrupt you.
- Daily wallpaper is a Facet Pro feature. Without Pro, the switch shows a Pro badge and opens Facet Pro. If your Pro subscription ends, your daily wallpaper settings are kept, but the wallpaper stops changing until you renew.
- **Browse daily photos when setting a wallpaper:** "Set a wallpaper with Facet" now opens a grid of this week's Bing and Spotlight photos, 7 from each. You can still use "Choose your own image" at the bottom. While you position a photo, the photographer's credit appears.

## Fixes
- On the clock stack, swiping left or right on one of Facet's own widgets, such as the Agenda, now moves to the next page. The Agenda still scrolls up and down as before.
- With the glass effect on, the image Facet used to create the blur could cover your real wallpaper. This happened if that image was a screenshot or one you picked with "I have an image already". Your actual wallpaper now always shows, and glass panels still blur correctly.
- Dragging to pan and zoom a wallpaper while positioning it now follows your finger smoothly. Before, it missed much of the movement.
- Resized clocks now fit their glass background. A clock enlarged above 100% no longer spills past the right edge of its panel, and a shrunken clock no longer leaves empty glass beside it.
- Colored text on the Home screen no longer shows a glowing halo around it. All Home screen text and icon shadows are now dark.

## Improvements
- In the two-column Home list with glass, there is now the same small gap between the columns as between the rows. Dragging an app onto the gap still drops it in the right place, and the Appearance preview shows the new spacing.
- A progress indicator now shows while a wallpaper is being set, saved or opened.
- The tabs for choosing a photo source and for choosing Home or Home & Lock are now the same height, and longer labels stay centred.
- Black and dark grey text and icons on the Home screen now have a thin light outline, so they stay easy to read on dark wallpapers.
- The outline is thinner on app labels and small text, and slightly thicker on clock digits, so text looks crisp without looking heavy.
- Text and icons inside filled clock pills and blocks no longer get a shadow or outline, so they look clean on their solid background.

## Internal
- Fixed master after merging the daily wallpaper and agenda swipe PRs:
  - Daily wallpaper unit tests now run the coordinator and the fake DataStores on a test scope that `advanceUntilIdle()` waits for, instead of `backgroundScope` or real IO.
  - Detekt: split `ApplyPendingWallpaperUseCase` and `RunDailyWallpaperUseCase` (ReturnCount), simplified two conditions, renamed `PortraitCentreCrop.kt` to `PortraitCrop.kt`, and moved `DailyWallpaperActions` next to its UiState.
  - Instrumented tests now pass `DailyWallpaperCoordinator` to `LauncherViewModel`. Removed commas from two test names because they fail to dex.
  - The Agenda swipe test now counts only the on-screen Agenda, since an offscreen copy is also composed.
  - CLAUDE.md now notes that commas also break androidTest names.
- Home's swipe detector now claims horizontal drags inside Facet's own widget pages, the same way the app list does. Hosted widgets still rely on their touch gate.
- Daily wallpaper is gated on Pro entitlement. After a lapse, settings are kept, but runs and applies are skipped.
- Daily wallpaper runs are triggered when the launcher comes to the foreground and applied on screen-off.
- Glass: the wallpaper copy is recorded only into the backdrop layer and is no longer drawn full screen behind Home.
- Wallpaper picker uses Peapix feeds for Bing and Spotlight, cached for the calendar day. The original image is downloaded on tap.
- Pan and zoom in the fit step keeps the latest frame for each touch event, because touch events were arriving faster than recomposition.
- `PillTabPair` is now used for the source tabs and the Home / Home & Lock selector. Its tabs share a height and centre wrapped text.
- Documented the second network use (the photo feeds) in CLAUDE.md and the architecture docs.
- Scaled clock footprint now follows the drawn width as well as the height, growing away from the clock's aligned edge. Added ScaledClockTest for the horizontal offset. ClockBlockTest now resizes the accent-hour clock to 1.4x, 1x, 0.6x and 1.2x, with and without calendar events.
- Added the HomeText composable, which draws the edge over the shadow and outside the glyph for homeTextOutlined colours. homeIconShadow applies the same rule as an unblurred ring. Added OnSolidFill for content inside filled pills and blocks.
- homeTextShadow() and homeAppLabelShadow() no longer take the text colour.
- Set Home text edge widths to 0.5dp for labels and small text and 0.75dp for clock digits.
- Docs: Home text edge widths.
