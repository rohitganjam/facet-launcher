# Lumen Launcher — Built Capabilities

A snapshot of what the app actually does today, written to support designing a first-run /
onboarding flow. Sourced from [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md), the
[PRD](<Android launcher design planning/design_handoff_minimal_launcher/PRD.md>), the
[design handoff](<Android launcher design planning/design_handoff_minimal_launcher/README.md>),
and the code as of this branch.

- **Stack:** Kotlin + Jetpack Compose, single `:app` module, MVVM (composables → ViewModels →
  Repositories → `domain/` use cases), Hilt, Room, DataStore, Coroutines/Flow.
- **Min SDK 33 (Android 13), target SDK 36.** Primary test device: Samsung Galaxy M15 5G; dev
  emulator `Medium_Phone_API_36.1`.
- **Nav:** one `NavHost` (`ui/navigation/LumenNavHost.kt`). Home + App Drawer are a single route
  with a follow-finger transition; every other screen is a normal destination.
- **Phases 0–10 are essentially complete.** The two remaining unchecked items in the plan are
  **the first-run flow** and **the accessibility pass** (Phase 8), plus a handful of small tracked
  gaps noted per-section below.

---

## 1. What exists at launch today (no onboarding)

`LauncherActivity` (`HOME`/`DEFAULT`/`LAUNCHER` intent filter, `singleTask`) → `LumenNavHost`
starting at `HOME`.

- **`LauncherViewModel`** gates a real `isLoading` state: the screen renders nothing (wallpaper
  shows through the transparent window) until the first settings + installed-apps emission lands,
  with a 3 s timeout safety net.
- **`EnsureActiveProfileUseCase`** runs on every start: if there's no valid active profile, it
  adopts the first existing one or **creates "Profile 1" automatically**. So the app is always in
  a usable one-profile state on first launch — there is no "zero profiles" state to design around.
- **`CleanUpUninstalledAppsUseCase`** runs for the process lifetime, hard-deleting favorites/dock
  rows for uninstalled packages.
- **No first-run detection exists.** There is no "has completed onboarding" flag, no
  `RoleManager`/`ACTION_HOME_SETTINGS` prompt, no gated first-launch screen. First launch drops
  the user straight onto Home. Adding onboarding means adding both the gate (a persisted flag) and
  the entry point.
- **Default-launcher status** is only surfaced in Settings (read via `DefaultLauncherRepository`,
  deep-links out via `ACTION_MANAGE_DEFAULT_APPS_SETTINGS`). Nothing prompts for it proactively.

---

## 2. Home surface

| Capability | Where | Notes |
|---|---|---|
| Custom clock widget | `ui/home/ClockBlock.kt`, `ui/home/clock/ClockTemplates.kt` | ~36 templates (`ClockTemplateId`) — typographic + shape-based. Ticks off `ACTION_TIME_TICK` (no polling). 12/24h. Per-template accent color, meridiem, date style (Full/Condensed). |
| Clock style gallery | `ui/home/clock/ClockStyleGalleryScreen.kt` | Global + per-profile picker. Includes Phase 10 "advanced" templates (Roboto Flex Wide/Narrow/Tech). |
| Clock position + size | `ui/home/ClockZoneHandle.kt`, `ClockCornerHandle.kt`, `ClockAdjustSheet.kt` | Long-press clock → bottom sheet: move handle (drag vertical zone), corner-drag resize (`clockScale` 0.5–2.0, default 0.8), "Edit styles". Global + per-profile. |
| Clock alignment | `ClockAlignment` (LEFT/CENTER/RIGHT) | Independent for clock and calendar strip. Per-profile. |
| Calendar events on clock | `ClockBlock.kt` + `ui/home/clock/CalendarEventsBlock.kt` | Real `CalendarContract` query. Event rows below the date, accent rule on next upcoming, tap → opens calendar app at that event. All-day toggle, per-calendar accent colors, per-calendar selection. **Gated on `READ_CALENDAR`** — shows a dashed permission strip when denied. |
| App list (Favorites / Recents / Most Used) | `ui/home/HomeScreen.kt`, `domain/ObserveHomeScreenStateUseCase.kt` | Per-profile `ListContentMode`. Section heading is dynamic (FAVORITES/RECENTS/MOST USED). |
| — Favorites | `data/FavoriteAppRepository.kt`, `DefaultFavoriteAppRepository.kt` | Up to 8. Global **Default favorites** list + per-profile override (`overridingFavorites`). Uninstall-collapse + hard delete. Reorder in Settings, not the picker. |
| — Recents / Most Used | `data/UsageStatsRepository.kt` | `UsageStatsManager`, 30-day window. **Gated on `PACKAGE_USAGE_STATS`** (special access). Shows a dashed strip + "Open settings" when ungranted; strip dismisses on tap. |
| App list layout | `AppRowPosition` (LEFT/RIGHT), `AppRowPresentation` (ICON_ONLY / ICON_AND_TEXT / TEXT_ONLY), `AppListVerticalAlignment` (TOP/BOTTOM) | Per-profile overridable. |
| Dock | `data/DockAppRepository.kt`, `HomeScreen.kt` | 0–5 apps, **shared across all profiles** (not per-profile). Icons or Text display mode. Omitted entirely when empty. Reorder in Settings. |
| Notification badges | `data/LumenNotificationListenerService.kt`, `NotificationBadgeRepository.kt` | Dot or Count (capped `9+`) on favorites list, dock, and drawer. Respects system silent-channel suppression, filters group summaries. **Gated on notification listener access** (special access). |
| App launch | `LauncherActivity.launchApp` | `ACTION_MAIN`/`CATEGORY_LAUNCHER` component intent. |
| App long-press context menu | `ui/components/AppContextMenu.kt`, `data/AppShortcutRepository.kt` | App info, Uninstall (`REQUEST_DELETE_PACKAGES`), and the app's own published shortcuts (App Shortcuts API — requires Lumen to be default launcher; silently omitted otherwise). Rename is **not** built (deferred to icon-pack work). |
| Home ↔ Drawer gesture | `ui/launcher/HomeDrawerRoute.kt` | Swipe up opens drawer (follow-finger, velocity + distance commit). |
| Swipe down → notification shade | `data/NotificationShadeRepository.kt` | Wired in `HomeDrawerRoute`; `EXPAND_STATUS_BAR`. Not fully verified on-device per the plan. |
| Long-press empty space → profile switcher | `ui/profiles/ProfileCarouselScreen.kt` | Opens the Switch Profiles overlay directly (the old 4-row long-press sheet was **deleted**). |
| Return-to-home on HOME intent | `LauncherActivity.onNewIntent` → `LauncherViewModel.onHomePressed` | Pops nav back to Home. Marked "not yet tested" in the plan (Phase 7). |
| Live wallpaper behind UI | `res/values/themes.xml` | `windowShowWallpaper=true` + transparent window background — OS composites the real wallpaper. No `WallpaperManager` code. |

---

## 3. App Drawer

| Capability | Where | Notes |
|---|---|---|
| List presentation | `ui/drawer/AppDrawerScreen.kt` | Letter section headers, `DrawerListItemSize` (Compact/Regular/Spacious). |
| Grid presentation | same | `LazyVerticalGrid`, `DrawerGridSize` 4×4 / 4×5 / 5×5 / 5×6. Flat grid, no headers. |
| Show icons / show labels | `showDrawerIcons`, `showDrawerLabels` | Conditional on List vs Grid. |
| Alphabet rail (right + left edge) | `ui/drawer/AlphabetRail.kt`, `domain/GroupAppsByLetterUseCase.kt` | Locale-correct bucketing via `android.icu.text.AlphabeticIndex`. Left-edge access always on. Forgiving 90%-height touch zone. |
| Search (apps) | `ui/drawer/DrawerViewModel.kt`, `domain/RankBySearchRelevanceUseCase.kt` | Type-to-filter, starts-with ranks above contains. Search bar Top or Bottom. |
| Search (contacts) + quick actions | `data/ContactRepository.kt`, `ui/drawer/ContactConnectionsSheet.kt` | **Gated on `READ_CONTACTS`** (toggle in App Drawer settings triggers the request; denial reverts). Contacts section omitted when ungranted. Tap a contact → bottom sheet with Call / Message / Email / WhatsApp + dynamically-discovered third-party connections (via `ContactsContract.Data` + `AccountManager`). Multi-number disambiguation page. |
| Overflow menu → Launcher settings | `AppDrawerScreen.kt` | 3-dot menu in the search bar. |
| Drawer opacity | `drawerOpacity` (default 0.6) | Translucent overlay on `SurfaceContainer`. |
| Live app-list refresh | `data/AppRepository.observeInstalledApps()` | `LauncherApps.Callback` — install/uninstall/update reflected without restart. |

---

## 4. Profiles

- **1 profile by default, max 3** (hardcoded constant). At least one must always exist.
- **Switch Profiles** (`ProfileCarouselScreen`, `ProfileCarouselMode.SWITCH`) — Home long-press.
  Translucent overlay over Home. Swipe to browse, **tap any card (centered or peeking) to apply
  immediately**. Live preview cards render each profile's real clock + favorites + shared dock +
  calendar events. Right-aligned "Reorder" button → Manage Profiles.
- **Manage Profiles** (`ManageProfilesScreen`) — Settings → Profiles. Standalone settings screen:
  drag-to-reorder list, per-row overflow (Profile settings / Delete), pinned "Add profile" row.
- **Add profile** — duplicates the previous profile's settings, favorites start empty. Hidden at 3.
- **Delete profile** — `ConfirmDialog` required; blocked at 1 remaining.
- **Rename** — `RenameDialog`, from Per-profile settings.
- **Per-profile settings** (`ProfileSettingsScreen`) — an "APPS LIST" override card (list content
  mode, apps-to-show, favorites + reorder) and a "CLOCK & CALENDAR" override card (clock style,
  24h time, clock font/weight, calendar settings) each behind an Inherit/Override switch
  (`InheritOverrideCard`).

---

## 5. Launcher Hub (widgets)

`ui/hub/` + `data/widget/`. Swipe right from Home.

- Add via system widget picker → allocate/bind/configure flow (`HubWidgetPickerScreen`), handles
  bind-permission dialog and configure activities.
- Fixed 5-column grid (`HUB_COLUMNS`), grows vertically to 50 rows, **20-widget cap** with a real
  at-capacity strip.
- Drag to reposition (displaces neighbors), corner-resize (cascade push-down), remove
  (`deleteAppWidgetId`).
- Orphaned-widget tile (provider uninstalled) with Remove / Keep space.
- Placement persisted in Room (`WidgetPlacementEntity`) — survives process death by construction.
- Sets `OPTION_APPWIDGET_SIZES` + prefers `targetCellWidth/Height` for span (One UI fixes).
- **Known open issues:** `HubGestureTest.movingAWidgetAfterResizingIt...` and
  `HubScreenTest.removingAnOrphanedWidget...` time out (real, pre-existing, unfixed).

---

## 6. Appearance & theming

| Capability | Where | Notes |
|---|---|---|
| Light + dark scheme | `ui/theme/Theme.kt` | `ThemeMode` LIGHT / DARK / SYSTEM (default). |
| Accent color | `accentFromSystem` (default true) | Material You from wallpaper (`WallpaperAccentRole` primary/secondary/tertiary) OR a fixed `AccentSwatch` picker. |
| Icon render mode | `IconRenderMode` | System default OR monochrome overlay (tints via `Icon.getMonochrome()` or a bitmap transform with the accent). Global, not per-app. |
| Launcher font | `LauncherFontOption` | Base font for all text roles except clock/calendar (which have their own picker + a "Default launcher font" option). |
| Font weight | `homeAppsFontWeight`, `calendarFontWeight` | `FontWeightOption` (6 stops). Home-apps weight scopes to app list + dock + drawer labels. Calendar weight is profile-overridable. |
| App label color | `appLabelColorOption` (`ClockColorOption`) | THEME / THEME_INVERTED / ACCENT_PRIMARY / ACCENT_SECONDARY. |
| Live preview card | `AppearanceSettingsScreen.kt` | Wallpaper-backed sample of app rows + dock icon. |
| Per-app label rename | **Not built.** | Deferred to icon-pack work (F11 v2, parked). |
| Third-party icon packs | **Not built / parked** (PRD §3a). | |

---

## 7. Settings map (all built unless noted)

Top-level groups in `SettingsScreen.kt`:

- **PROFILES** → Manage Profiles
- **APPEARANCE** → `AppearanceSettingsScreen` (theme, accent, icons, launcher font, font weight,
  app label color, live preview)
- **CLOCK & CALENDAR** → clock style gallery, `CalendarSettingsScreen` (show all-day events,
  calendars to display — real once granted)
- **HOME & APPS**
  - Dock → `DockSettingsScreen` (display style, pick apps, reorder)
  - Home Apps List → `HomeAppsListSettingsScreen` (position, presentation, default content mode,
    apps to show, default favorites + reorder)
  - App Drawer → `AppDrawerSettingsScreen` (presentation, grid/list size, show icons/labels,
    search contacts, search bar position, drawer opacity)
  - Notifications → `NotificationSettingsScreen` (on/off + Dot/Count style; routes to the access
    explanation screen if not granted)
- **SYSTEM**
  - Permissions → `PermissionsScreen` (Calendar, Contacts, Usage access, Notification access —
    per-row grant status + fix action)
  - Backup & restore → `BackupRestoreScreen` (full-settings JSON export/import via SAF; destructive
    import with confirm; guided widget re-add)
  - Change wallpaper → `ACTION_SET_WALLPAPER` system picker
  - Set as default launcher → status + `ACTION_MANAGE_DEFAULT_APPS_SETTINGS`

Every non-root screen has an on-screen back button (convention).

---

## 8. Permissions — the core of onboarding design

**Principle (PRD §7, implemented): just-in-time, never bundled upfront.** Each feature requests
its own permission when the user enables it, and every gated surface degrades to a dashed strip
("[feature] needs [permission]" + a fix action) rather than breaking or showing empty.

| Permission | Type | Feature | Current request point | Repository / grant check |
|---|---|---|---|---|
| `READ_CALENDAR` | Runtime | Calendar events on clock | Calendar settings "Turn on"; Permissions screen row | `CalendarPermissionRepository` |
| `READ_CONTACTS` | Runtime | Drawer contact search + actions | App Drawer settings "Search contacts" toggle; Permissions row | `ContactPermissionRepository` |
| `PACKAGE_USAGE_STATS` | Special access (Settings redirect) | Recents / Most Used home list | Home strip "Open settings"; Permissions row → `UsageAccessExplanationScreen` | `UsageAccessRepository` (AppOps) |
| Notification listener access | Special access (Settings redirect) | Notification badges | Notification settings toggle → `NotificationAccessExplanationScreen`; Permissions row | `NotificationAccessRepository` |
| `QUERY_ALL_PACKAGES` / `<queries>` | Manifest | App list visibility | build-time, no prompt | — |
| `EXPAND_STATUS_BAR` | Normal (auto) | Swipe-down shade | — | — |
| `REQUEST_DELETE_PACKAGES` | Normal (auto) | Context-menu Uninstall | — | — |

- **Special-access explanation screens already exist** (`UsageAccessExplanationScreen`,
  `NotificationAccessExplanationScreen`) — they explain, redirect to the system Settings screen,
  and auto-dismiss when the grant flips true on resume. An onboarding flow could reuse this
  pattern.
- **"Asked before" tracking:** `calendarPermissionRequested` / `contactsPermissionRequested`
  flags in `LauncherSettings` disambiguate "never asked" from "permanently denied" so a row can
  route to App Info instead of firing a no-op request.
- **Revocation handling** is per-feature: a revoked permission makes the feature hide itself + show
  the strip, re-checked on `ON_RESUME`.
- **Nothing is requested at first launch today.** The default state: badges setting defaults `on`
  but access isn't granted (so no badges show); calendar/contacts/usage all off/denied.

---

## 9. Persistence

- **DataStore** (`SettingsRepository` → `LauncherSettings`): every launcher-wide toggle/enum —
  24h time, dock display, drawer presentation/grid/opacity/item size, badge on/off + style,
  search bar position, active profile id, calendar selection + colors + all-day, contacts search,
  theme mode, accent source + swatch + wallpaper role, icon render mode, launcher font, app label
  color, permission-requested flags, app row position/presentation, list content mode, apps-to-show,
  clock template/font/color/accent/meridiem/date style/alignment/scale/zone height, calendar
  font/weight/color/alignment, home-apps font weight, app list vertical alignment.
- **Room** (`LumenDatabase`, v15): `ProfileEntity` (+ many per-profile override columns),
  `FavoriteAppEntity`, `DefaultFavoriteAppEntity`, `DockAppEntity`, `WidgetPlacementEntity`.
  Real migrations for recent versions; older bumps used destructive fallback (pre-release).
- **In-memory only:** notification badge counts (`NotificationBadgeRepository` — recomputed from
  `activeNotifications` on every listener callback).
- **Backup bundle** excludes: `activeProfileId` (stored as index), `selectedCalendarIds` /
  `calendarColors` (device-specific), permission-requested flags, widget `appWidgetId`s.

---

## 10. Gaps relevant to building onboarding

1. **No first-run gate.** Need a persisted "onboarding complete" flag (DataStore) and a
   conditional start destination or overlay in `LumenNavHost` / `LauncherActivity`.
2. **No set-as-default prompt.** Only a passive Settings row. The design spec (`4h`) wants a
   bottom sheet with "Set as default" / "Later"; consider `RoleManager.createRequestRoleIntent(ROLE_HOME)`
   (API 29+) for an in-place dialog instead of the Settings deep-link.
3. **First-run favorites picker (`4g`)** — no dedicated onboarding variant exists, but
   `FavoritesPickerScreen` / `FavoritesPickerViewModel` (checkbox list, search, cap-8, writes to
   the default favorites list with no `profileId`) is directly reusable.
4. **"What this is" intro screen (`4f`)** — not built at all.
5. **Permissions must stay just-in-time** — onboarding should *not* request `READ_CALENDAR` /
   `READ_CONTACTS` / usage / notification access. The plan's own test bar: "first-run does not
   request any runtime permission."
6. **Accessibility pass still open** (Phase 8) — TalkBack labels on icon-only UI, large font-scale
   layout. Onboarding copy/controls should be built to that bar from the start.
7. **Empty states** for a fresh profile with no favorites already exist as a strip
   ("Nothing here yet — pick up to 8 apps" / "Add apps") — onboarding can lean on these as the
   post-onboarding fallback rather than forcing favorite selection.
8. **Design reference:** screens `4f`–`4h` in the handoff, `screenshots/light-turn4-hub-onboarding-pickers.png`.
