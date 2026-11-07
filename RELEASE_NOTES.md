# Facet Launcher 0.1.39

## New Features
- **Facet widgets**: Weather, Agenda and Now Playing widgets are now built into the launcher. You can place them on the Hub and on Home pages, and resize, move, replace and remove them.
- **Widget settings**: Each Weather and Agenda widget can have its own settings. Weather can use its own location (your current location or a saved city). Agenda can use its own set of calendars. Settings open right after you add a widget, and an Allow button shows up when calendar or location permission is off.
- **Clock is now a widget**: Each clock has its own settings. You can remove the clock and add it back from the widget picker.
- **Widget sheet**: Long-press a widget to open a sheet with its settings, size and position, replace, remove and the widget stack. Long-pressing empty space now opens only Facet settings and Launcher settings. The Hub's long-press menu also has "Configure widget".
- **Calendar settings sheet**: The clock's calendar settings now open as a bottom sheet instead of a full screen.
- **Work Profile calendars**: Agenda and calendar settings can list and select your Work Profile calendars. They are labeled "Work calendar", with a few event titles to help you tell them apart. Tapping a work event opens the work Calendar app.
- **Work Profile widgets**: The widget picker has Personal and Work tabs when you have a Work Profile. Widgets from apps installed only in the Work Profile now show up too.
- **Drawer search sheet**: Tapping the drawer's search bar (or Home, with "Home opens search") opens a dedicated search sheet. With an empty search, it shows recently installed or updated, frequently used and last used apps.
- **Better search results**: Settings and contacts appear as grid tiles. Each setting has its own icon, and contacts show their photo or initial.
- **Weather settings in Settings**: Units and location now have their own row under Settings > System. Before, you could only reach them through the clock's accessories.
- **Facet clock weather location**: The clock's weather can follow its own location.
- **Copy defaults to facets**: The default dock, apps list and look screens have a "Copy to facets" option. It lets you pick which facets get those defaults.
- **Notifications onboarding step**: Setup has a new last page for notification badges, badge style and the Home media player.
- **Clock style screen**: Clock templates appear in a horizontal row at the top, all the same size.
- **Settings shortcut**: Settings now starts with a "<Facet Name> Settings" row for the active facet.

## Fixes
- Long-pressing a widget no longer opens the free-space sheet by mistake.
- Long-pressing the battery or weather accessories on the clock now opens the widget menu.
- The clock no longer shrinks when you switch back to its facet.
- The Weather widget shows a loader while its first forecast loads. It no longer shows "No weather yet" or the location prompt during that time.
- The Weather widget now fetches its own forecast even when the launcher's Weather is off. It shows the Allow prompt when location access is missing.
- Work calendars no longer lose their colors.
- New calendars get a color right away.
- Calendar colors now follow calendar access.
- One failed color assignment no longer stops calendar colors from updating.
- Calendar settings in widgets now refresh after you grant calendar permission. Before, they could keep showing "No calendars to show".
- Agenda sample events in the picker no longer show a date in 2096. They now start tomorrow at 09:00.
- Backups now include the "Home opens search" and double-tap-to-lock settings. Before, a restore reset them.

## Improvements
- Facets now keep their own dock, favorites and look. The launcher defaults only fill in new facets, and "Reset to default" copies them back.
- Dock & Apps appearance is now called "Dock & Apps layout". It has moved under Apps and folders, with a clearer subtitle.
- Settings is reorganized. There is a Customization card with a "Launcher appearance" row, and "Facets" is now "Manage Facets". The dock and apps list rows have clearer names, and App Drawer and Notifications are back in the Appearance card.
- Drawer and Home grids have full-width labels, tighter 2dp gaps, wider cells and bigger labels. Compact and Regular grids are denser.
- Search and drawer grid tiles have a little extra top padding.
- The widget picker shows Facet widgets as bordered tiles under one header, two per row.
- Long Now Playing titles scroll as a marquee in the widget and the media row.
- Now Playing song text matches the calendar text size.
- The Agenda widget header is larger and has more padding.
- Agenda settings show each calendar's color dot.
- The Work badge is redrawn as a small briefcase at the bottom-left of the icon.
- The weather location picker has more spacing and a "Saved cities" heading.
- Clock accessories have more padding in the Location card.
- What's new text now draws **bold** runs in bold.
- The Appearance screen no longer has clock rows, and the clock hints now point at long-pressing the widget.

## Internal
- DB: Room 38 (native widget columns), 41 (facet clock weather location), 42 (widget settings as JSON), 43 (data-only step copying clock settings to pages), 44 (drops clock/calendar columns and clockScale), 45 (inheriting facets get their own values), 46 (drops override flags/LAUNCHER_DEFAULT markers). Backup versions 4, 5 and 6 upgrade older files.
- NativeWidgetConfig moved to JSON, with LegacyNativeWidgetConfig.kt for the old format.
- Removed clockScale/landscapeClockScale (dp box is the only clock size), the launcher-wide clock/calendar keys, MaterializeClockConfigsUseCase and the startup pass.
- Added ClockWidgetConfig, UpdateClockConfigUseCase, ApplyFacetDefaultsUseCase, EnsureCalendarColorsUseCase and a shared WeatherLocationPicker. Calendar selection uses selectsCalendar and one shared label helper.
- Added a ContentObserver for calendar colors, with registration retry. SettingsRepository colon-split bug fixed for work:N colors.
- SettingsViewModel reads notification access. The Appearance route drops its facetId.
- Tests: calendar observer failure/restart, work-event tap routing, saved widget size across facet switch, v37→v44 migration, work-profile-robust instrumented tests, double-tap-lock tests skipped via assumption, Home tests open the Home sheet from free space. Removed obsolete tests for removed rows and facet scope. DrawerGridLayoutTest updated.
- Docs: clock references, persistence/registry/layout/weather docs, clock widget proposal and design questions, CLAUDE.md and doc 18 (weather network exception).
- Portuguese strings use Brazilian forms. Added pt-PT and es-419 strings.
- Graphify output refreshed.
- Release 0.1.38: version bump, What's new, release notes.
