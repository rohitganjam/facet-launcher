# Facet Launcher 0.1.43

## New Features
- You can now choose a text colour for the Weather, Agenda and Now Playing widgets: match the theme, always light, or always dark.
- Widgets have two new background options, Theme and Theme inverted. Both are slightly see-through solid backgrounds that follow your light or dark theme. With Theme inverted, the text colour flips to stay readable.

## Fixes
- Fixed a crash with centred app rows in the single-column app list.
- Fixed the launcher sometimes forgetting your wallpaper after a refresh, which could make the wallpaper offer appear when it shouldn't.
- Fixed the wallpaper popup appearing right after you set a wallpaper in Facet. Glass effects also no longer switch off until you leave and come back.

## Improvements
- Glass icons on the dock, list rows and grid tiles now blur the frosted panel behind them too, so layered glass looks more natural.
- The clock now has a single Background setting in Clock Style that covers both the time and its events. Your old calendar background choice carries over.
- Previews now show glass effects using your saved wallpaper, so they match what you'll actually see.
- Glass on the Hub now includes the Hub's dimming, so the blur matches what's on screen.
- The lens effect now works with every glass style.
- In the single-column app list, all rows are now the same width as the widest item and take up at least half the list.
- The wallpaper screen is clearer. "Let Facet use my current wallpaper" is now its own card that's always shown, with a check mark and "Already in use" once it's set. "Remove the copy" is now a simple text button.

## Internal
- GlassPanel records its own layer so nested glass icons sample the panel's frost.
- Calendar background setting removed and migrated into the shared Clock Style background.
- Previews sample their own saved wallpaper instead of the activity's layer.
- Widget text option forced through LocalIsDarkTheme, applied to widget content only.
- Wallpaper-copy staleness check now reads the system wallpaper id fresh.
- Lens depth weight set to 0.8.
- Release 0.1.42 version bump.
