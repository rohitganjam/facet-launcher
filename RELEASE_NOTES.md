# Facet Launcher 0.1.42

## New Features
- Glass backgrounds: the dock, apps list items, the clock's calendar and the media player row can each sit on a frosted panel. Choose from Light frost, Smoked, Bright, Clear glass or Accent in each one's settings.
- Real glass with your wallpaper: Facet can keep its own copy of your wallpaper. Pick a photo, pan and zoom to fit it, and set it as your wallpaper. Glass panels then show your wallpaper through them, with real blur on the dock, lists, grid tiles, calendar, media row and widgets.
- New Dark frost and Dark glass styles.
- Glass icon backgrounds: icons can sit on five glass backgrounds (light frost, smoked, bright, clear glass and accent), alongside the existing tile and reversed tile options.
- The Weather, Agenda and Now playing widgets each have a Background choice in their settings. Now playing now has its own settings on Home and the Hub.
- "Use my current wallpaper": take a screenshot of your wallpaper on a clean capture screen with simple step-by-step instructions, or pick an image you already have.
- Copy from facet: the Dock, Apps list and Widgets screens can copy their content and settings from another facet in one step.

## Fixes
- Wallpaper previews showed the phone's factory wallpaper instead of yours. When your wallpaper can't be read, previews now show a soft wash of its colours.
- The clock sometimes showed a 0% battery reading. It now hides the battery until a real reading arrives.
- Fixed a crash when fitting a photo that already matches the screen size.

## Improvements
- After you change your wallpaper, or the first time you choose a glass background, Facet offers to make a wallpaper copy. If you choose "Not now", it remembers.
- Glass icon backgrounds on Home now show your wallpaper through them.
- Every preview background uses your saved wallpaper copy when it matches your current wallpaper.
- If the wallpaper copy is missing or out of date, glass falls back to a tint.
- Wallpaper images are now chosen through your gallery app.
- Clearer title on the glass backgrounds prompt.
- The Agenda widget now shows a full week of events. Days after Today and Tomorrow are labelled with their date (e.g. "14th Nov, Sat"), with more space between days.
- Even spacing above and below the media player row.
- Landscape settings are combined into one card, with clearer names: "Apps list alignment" and "Apps list position".
- Long setting titles now wrap instead of crowding the switch or dropdown.
- The facet shortcut label is now translated.
- Backups now include your glass background choices.

## Internal
- IconRenderMode now carries style, colour and background directly (30 new stored values, existing unchanged); removed its unused display strings
- GlassStyle.RIM_LIGHT renamed to CLEAR_GLASS (unreleased, no migration)
- Room v50: facets.dockGlass and appsListGlass (TEXT NOT NULL DEFAULT 'OFF'); calendar glass in clock JSON config, media row glass in DataStore
- NativeWidgetConfig gains a per-widget GlassStyle (default Light frost, replacing the fixed 60% surface)
- Agenda uses its own 7-day calendar query instead of the clock's window
- Battery repository emits nothing for broadcasts without level/scale; ClockAccessoryState.batteryPercent nullable
- Dropped the builtInDrawable fallback for wallpaper previews
- Removed leftover icon_render_system_default translation that failed release lint
- Lint fixes: added plural "many" to es, fr, pt, pt-rPT, es-419; DockSettingsScreen modifier parameter order; IconsScreen uses String.toUri
- Settings screens use collectAsStateWithLifecycle; ObserveHomeScreenStateUseCase and ObserveHubStateUseCase are @ViewModelScoped; ObservePlacedItemsUseCase indexes installed apps once per emission
- Removed unused strings from every locale; architecture docs updated
- Fixed stale instrumented tests (ScaledHomeScreenTest, HomeScreenTest, FavoritesPickerScreenTest, AppContextMenuScalingTest)
- Added wallpaper-backed glass plan to IMPLEMENTATION_PLAN.md
- Docs: Pro launch and device verification marked done; stacks listed as a Pro feature
- Refreshed 0.1.41 release notes and last-release record
- Updated Play Store listing for 0.1.41
