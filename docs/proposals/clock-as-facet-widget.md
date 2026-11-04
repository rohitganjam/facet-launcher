# Proposal: the clock as a Facet widget

Status: design agreed in discussion, not started. Branch `clock-as-widget`. Nothing here is built yet; once it is, the
living docs (`docs/architecture/`) replace this file.

## 1. Goal

Today the clock is page 0 of a facet's clock stack (`ClockStackItemKind.CLOCK`) but is special-cased everywhere: its size is a
draw-only scale (`clockScale`), its style and calendar settings are launcher-wide with a per-facet override
(`overrideClock`, `overrideCalendar`), it cannot be removed, and the calendar events are bundled into `ClockBlock`.

Make the clock a Facet widget like Weather, Agenda and Now playing: sized as a dp box, configured per
widget, removable and re-addable, with no launcher-wide default and no inherit/override.

## 2. Decisions

| Topic | Decision |
|---|---|
| Widget kind | A Facet-drawn (Compose) widget, never an `AppWidgetHost` widget. It stays `ClockStackItemKind.CLOCK` on the stack, not a `NativeWidgetType`: that enum is the card widgets (Weather, Agenda, Now playing) that share one backed frame, one tier layout and the Hub; the clock has no backing, scales as one block and is not placeable on the Hub. |
| New facet | Seeded with one clock page set to the built-in defaults (not the user's current settings). |
| Settings model | Per clock: all clock style and accessory settings plus its calendar settings. No launcher default, no override flag. |
| Where | Home clock stack only. Never on the Hub. Hub picker does not list it; the Home picker does ("Facet widgets" section). |
| Empty | A facet may have no widget, free or Pro. The top zone collapses; the media row and then the list start at the top padding. |
| Sizing | dp box, aspect locked. The whole clock scales as one block with the box. System font size does not change the time's size. |
| Calendar | Part of the clock widget. Fixed height, width follows its text, aligned like the clock (same as today). Event count and indicator style move into its settings. A tap on an event behaves as today. |
| Divider | Kept. It still sets the top zone's height (and so where the list starts); dragging it above the clock's bottom shrinks the clock live, as today. Pages of a stack share one zone height. It stays when the zone is empty. |
| Pro | Unchanged: free keeps one page, Pro up to five. A clock is a page. |
| Weather location | Per clock; "Same as Weather settings" stays the default (the only follow-the-launcher setting that survives). |
| Agenda | Loses follow-the-launcher for calendars. Its settings are the same sheet the clock's calendar uses. |

## 3. Data model

- `ClockStackItemKind.CLOCK` stays as the clock's page kind. Size and alignment fields on `ClockStackItem` (`widthDp`, `heightDp`, `alignment`,
  landscape variants) now apply to the clock too; the "null means uses the facet's scale" case is gone. The clock's settings live in the
  page's `nativeWidgetConfig` (`NativeWidgetConfig.clock`).
- `nativeWidgetConfig` (`NativeWidgetConfig`, currently a tab-separated line) gains the clock's fields: template, font, colour, accent
  colour, 24-hour, meridiem, date style, alarm, battery, weather, weather icon style, weather location, time zone (reserved), and the
  calendar block (indicator, item count, all-day, calendar selection). The codec has to outgrow tab-separated fields, so it moves to a
  single new encoding (versioned key/value or JSON). The runtime codec reads only that format: the migration rewrites every stored
  config (`widget_placements.nativeWidgetConfig` and `clock_stack_items.nativeWidgetConfig`) into it, so there are not two ways to read.
- Calendar selection per widget: explicit ids, or an "all calendars" mode that picks up calendars added later. New widgets start in
  "all calendars"; unticking one switches that widget to an explicit set. Applies to Agenda too.
- `Facet` / `FacetEntity` fields retired once migrated: `overrideClock`, `clockTemplateId`, `clockFontOption`, `clockColorOption`,
  `clockAccentColorOption`, `clockShowMeridiem`, `clockDateStyle`, `clockShowAlarm/Battery/Weather`, `clockWeatherIconStyle`,
  `clockWeatherLocation`, `clockScale`, `landscapeClockScale`, `overrideCalendar`, `showCalendarIndicator`, `calendarItemsToShow`,
  `selectedCalendarIdsCsv`, plus the retired hosted-clock columns. Kept on the facet: `clockZoneHeightDp`, `landscapeClockZoneHeightDp`
  (the divider), `clockPlacementOwn`.
- Launcher-wide DataStore keys for clock style, accessories and calendar choices are read by the migration, then left unread for one
  release before deletion so a rollback still has them.
- `ResolvedClockStyle` stops being a facet-wide resolution; the clock widget builds it from its own config. `resolveFacetStyle` loses
  its clock block.

## 4. Migration (schema v42)

Two stages, because a Room migration cannot read DataStore.

1. **Room `MIGRATION_41_42`**: add the columns, copy each facet's `clockScale` and
   landscape scale into a pending field on the page, and rewrite every existing tab-separated `nativeWidgetConfig` (Hub and stack rows)
   into the new encoding. `Migration.migrate` is ordinary Kotlin, so it reads the rows and writes them back; the old format is decoded
   by a one-way converter that lives with the migration, not by the runtime codec.
2. **One-time startup step**, run before Home first draws and recorded by a DataStore done-flag (idempotent): for every facet, resolve
   the clock and calendar settings that are in force today (the facet's own while `overrideClock` / `overrideCalendar`, else the launcher's)
   and write them into each clock's config. Agenda widgets that follow the launcher get the launcher's calendars copied in.
3. **Scale to box**: on a clock's first render the widget converts the pending scale to a dp box using its measured natural width, writes
   it and clears the pending value, so the first draw has exact visual parity. A facet that is never shown converts when it is first shown.

Old backups: they carry the old facet clock fields and old-format widget configs. Import runs them through the same one-way converter
(old format to the new encoding) and the facet-field mapping, so nothing but the importer ever sees the old format; the converter can be
deleted when old backups stop being supported. New backups carry the clock in `nativeStackPages` with its config in the new encoding.

## 5. Sizing and layout

- The clock widget's box is aspect locked. Resize handles scale the clock; minimum is the smallest legible clock, maximum the screen width.
- The calendar block sits under the clock, aligned with it, at its own fixed height and content width. It can be wider than a small clock.
  A long-press anywhere on the clock or calendar belongs to the widget.
- `clockScale`, `ClockFit.maxScale`, the scale re-clamp and the corner scale handles go; `ClockAdjustMode`'s handles become the stack's
  standard dp resize.
- Landscape keeps its pane layout; its scale becomes its own landscape box (`landscapeWidthDp`/`landscapeHeightDp` already exist) with the
  top/middle/bottom alignment.
- Empty zone: the zone's height is whatever the divider sets, with nothing drawn; with no widget the media row and list start at the top
  padding. Any free Home area (above or beside the list, around a grid) is an open area.

## 6. Gestures and sheets

| Gesture | Result |
|---|---|
| Long-press on the clock or its calendar | A small sheet with two rows: **Clock styles** (opens the full-screen clock options for that clock) and **Calendar settings** (the shared calendar sheet). |
| Long-press on free Home space | The Home sheet (today's `ClockAdjustSheet`) with constant rows: **Adjust size & position** (the only way into resize mode), **Replace widget**, **Remove widget** (**Add widget** when there is none), **Widget stack**, then facet and launcher settings. |
| Long-press on Weather / Agenda / Now playing | As today (Home's behaviour through `longPressFallback`). Their on-widget 3-dots stay. |
| 3-dot in the weather forecast sheet opened from a clock | Opens that clock's accessory controls (icons on or off, location, and so on). |
| Tap on the clock text / date | As today (opens the Clock or Calendar app). |

The calendar sheet is `NativeWidgetConfigSheet`'s Agenda body, generalised: all-day events, calendars (with colour dots and work-calendar
hints), event count and indicator style. Roomy spacing. `Appearance > Calendars to display` is removed. All of it is a `ThemedModalBottomSheet`
or a screen that dismisses on Home press.

## 7. Settings changes

- **Launcher-level Appearance**: drop the Clock & calendar section; keep Notifications, Dock & home, Look & feel.
- **Facet settings**: a new **Home** section with **Widgets** (the existing widget stack screen, Configure per page) and **Dock & apps
  appearance** (opens `DockAppsAppearanceScreen` directly). The facet's separate Appearance screen is skipped.
- `ClockStyleGalleryScreen` and `ClockAccessoriesScreen` are scoped to a clock (`itemId`) instead of a facet or the launcher; their launcher
  scope goes.
- Calendar colours stay automatic and launcher-wide for now; editing them (stored per facet) is a later enhancement.
- Weather settings in System is unchanged.

## 8. Phasing (one branch, one schema bump)

1. Data model, config codec, `MIGRATION_41_42`, the startup migration, backup import mapping. Tests: migration, codec, backup coverage.
2. The clock as a native widget with dp sizing and the calendar block; picker entry; replace/remove/add.
3. Gestures and sheets, the empty zone, the divider when empty, the shared calendar sheet, accessory 3-dot.
4. Settings clean-up (Appearance, Facet settings Home section, gallery/accessories routes), hints, living docs, `TEST_REGISTRY.md`.

Merge `master` into the branch regularly.

## 9. Tests and docs

Unit: migration (including the startup step and scale conversion), config codec, stack repository, resolver, backup. Instrumented:
`ClockBlockTest`, Home layout tests, long-press and sheet tests, `FacetSettingsScreenTest`, Appearance screen tests, picker tests, widget
stack screen. Docs to update: `02`, `03`, `07`, `08`, `09`, `13`, `17`, `20`, `README.md`, `IMPLEMENTATION_PLAN.md`, `CLAUDE.md` if conventions change.

## 10. Risks

The clock is Home's most tuned surface (fit maths, landscape pane, calendar following the drawn height); expect layout test churn. The startup
migration must finish before Home's first draw. Hint strings (`gesture_hint_clock_hold`, `clock_style_long_press_hint`) become wrong and are
redesigned later.

## 11. Open

- Hint design (existing hints reworded, to explore later).
- Resize handles: do they cover only the clock, or the clock plus a calendar wider than it?
- Vertical anchor of a page's box inside the shared zone when the box is shorter than the zone.
- Backup: whether `CURRENT_BACKUP_VERSION` must bump so an older app can tell a new backup's config encoding apart (settle in stage 1).
- Per-facet calendar colours and per-clock time zones (later).
