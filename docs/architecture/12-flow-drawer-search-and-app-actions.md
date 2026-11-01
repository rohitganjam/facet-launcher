# 12 — Flow: App Drawer — search, tabs, and per-app actions

The drawer is the one surface that fans out to the most repositories (`DrawerViewModel` injects
10 repositories, 4 use cases and the two notification helpers `NotificationActionController` and `NotificationOpener`). Its app list comes from *outside* (`FacetNavHost` passes
`LauncherUiState.apps`); the ViewModel owns search, tabs' supporting data, and every action.

## 1. Data in

```mermaid
flowchart TB
    LVM["LauncherViewModel.uiState.apps\n(GetInstalledAppsUseCase.observe(): live, minus PRIVATE and Facet itself)"] --> NAV[FacetNavHost] --> R[HomeDrawerRoute] --> S[AppDrawerScreen]
    LVM -- workProfiles --> S
    S --> TAB["DrawerTab.Personal → profile ∈ {PERSONAL, OTHER}\nDrawerTab.Work(handle) → userHandle == handle"]
    TAB --> GRP["GroupAppsByLetterUseCase(items, locale) { displayName }\nICU AlphabeticIndex buckets → GroupedItems&lt;DrawerItem&gt;\n(headers + AlphabetRail index maps)\nitems = apps only, or apps+folders merged when INLINE"]
    DVM[DrawerViewModel] -- "badgeCounts, contactResults, settingsResults,\nshowContactsPermissionPrompt, showContactsSettingPrompt,\nprivateSpaceState, secureFolderIntent, folders" --> S
    SET["SettingsRepository.settings"] -- "drawerPresentation LIST/GRID, gridDensity,\nlistItemSize, opacity, showIcons/Labels,\nsearchBarPosition, notificationBadgeStyle,\ndrawerFolderDisplayMode, recentlyInstalledPosition" --> S
```

### 1a. Folders in the drawer (`DrawerFolderDisplayMode`)

`AppDrawerScreen` wraps every app as `DrawerItem.AppEntry` regardless of mode, so `DrawerListContent`/`DrawerGridContent` render one item type throughout; only `groupedItems`' construction and an optional pinned section vary by mode:

- `DO_NOT_SHOW` (default): folders never render as drawer entries — `groupedItems` is apps-only, exactly the original behavior. Folders stay reachable via Dock/Favorites or `AppContextMenu`'s "Add to folder" row (`folderCandidates`, unaffected by this setting).
- `INLINE`: apps and `folderCandidates` are merged into one list, sorted by name (folders don't arrive pre-sorted the way `apps` does — see chat history for why a naive concatenation before grouping silently breaks interleaving), then grouped together — a folder tile/row sorts under its own leading letter, indistinguishable in position from an app.
- `SHOW_FIRST`/`SHOW_LAST`: `groupedItems` stays apps-only; every folder instead renders in its own pinned section (`drawerFolderSection`/`drawerFolderTiles`) before or after the lettered content — its own "Folders" header in List (Grid renders no headers at all, same as the lettered content).

The alphabet rail (`AlphabetRail`, `LetterJumpZone`) mirrors this: `RailFolderPosition.NONE` for `DO_NOT_SHOW`/`INLINE` (a folder is reachable by its own letter in `INLINE`, same as any app), `TOP`/`BOTTOM` for `SHOW_FIRST`/`SHOW_LAST` — which renders the Folder icon `RailGlyph` as one more rail entry and makes `railSelectionAt` resolve a drag/touch there to `RailSelection.Folders` instead of a letter, scrolling to that section's own start index. `DrawerFolderRow`/`DrawerFolderTile` reuse `FolderTileGlyph`/`FolderContentsSheet`/`FolderTileContextMenu` (rename + Add to Favorites/Dock), mirroring `ui/home/HomeScreen.kt`'s `FolderRow`/`FolderDockIcon`. Search results never include folders — this is browse-mode only.

### 1b. "Recently installed" category (`RecentlyInstalledPosition`)

A single virtual item — not a real `Folder`/`FolderEntity` — positioned by
`LauncherSettings.recentlyInstalledPosition` (global-only `RecentlyInstalledPosition` enum:
`SHOW_FIRST` default, `SHOW_LAST`, or `DO_NOT_SHOW`; Settings → Appearance → App Drawer → "Recently installed",
a `LabeledDropdownRow` mirroring `DrawerFolderDisplayMode`'s own dropdown). `SHOW_FIRST` places it
above everything, even a `SHOW_FIRST` folder section; `SHOW_LAST` places it below everything, even
a `SHOW_LAST` folder section — each independent of the other (a folder section and the category can
sit at opposite ends, or the same end, with the category always the outermost item at that end).
**List-only**: it has no grid-tile counterpart, so it disappears entirely when
`DrawerPresentation.GRID` is selected (`recentlyInstalledOffset`/`showRecentlyInstalledTrailing` are
both forced false whenever `presentation == GRID`). Computed live via `RecentlyInstalledAppsUseCase`
(`domain/`, pure/stateless — filters `AppInfo.firstInstallTime` within the last 72 hours, sorted
newest-first) over `tabScopedApps`, the *same* tab-filtered source the lettered list uses —
deliberately unlike pinned folders (which are **not** tab-filtered): the Personal tab shows only
Personal recently-installed apps, a Work tab only that profile's. Hidden entirely (not just an empty
state) whenever the computed list is empty or the position is `DO_NOT_SHOW`, so it never occupies a
dead tap target.

**Section header**: an icon-only header (`DrawerRecentlyInstalledHeader`, `RecencyIcon` tinted
`DrawerHeaderTextColor` at `labelSmall`'s font size, no text label) renders directly above the row
— same `header_$letter` treatment as a letter group's own header, minus the label — for both
`SHOW_FIRST` (leading) and `SHOW_LAST` (trailing) placement, in both `DrawerListContent` and
`PrivateSpaceScreen` (`PrivateSpaceRecentlyInstalledHeader`, `onSurfaceVariant` tint). Adds its own
lazy-list slot, so `recentlyInstalledOffset` is `2` (header + row), not `1`.

**Category glyph**: `DrawerRecentlyInstalledRow`'s leading icon is `RecentlyInstalledGlyph` — the
same 12dp-rounded-tile-with-inset-vignette container shape as `FolderTileGlyph`, but with its own
background (`Ink`, not `FolderGlyphBackground`) and icon tint (`InkInverted`) — deliberately
themed opposite to `FolderTileGlyph`'s fixed-dark tile, since `Ink`/`InkInverted` are exact
inverses of each other in both themes so this stays high-contrast in light and dark alike, unlike
a literal-white icon on a theme-invariant dark tile. `RecencyIcon` stands in for `FolderTileGlyph`'s
own folder icon (not a star — see chat history: a star read as generic/unrelated, so the category
glyph instead reuses the same "history" icon as each contained app's own badge, below).

Tapping it opens `FolderContentsSheet` via a synthetic `Folder(id = -1L, name = "Recently
installed", apps = recentlyInstalledApps)` — reused as-is since `Folder`/`FolderTileGlyph`/
`FolderContentsSheet` are plain-data/Room-decoupled at the UI layer (see `13-flow-facets-…md`'s
own note on this same reuse for Home's "look" preview cards). `FolderSheetHeaderAction` gained a
third case, `None`, so the sheet renders with no "Rename" trailing action on this non-editable
virtual folder. `DrawerRecentlyInstalledRow` has no long-press context menu (nothing to
rename/favorite/dock for a virtual item) — just tap-to-open. Each contained app also gets a small
recency badge (`RecencyBadge`, `FolderContentsSheet.kt`) at its icon's bottom-end corner —
`showRecencyBadge: Boolean` on `FolderContentsSheet`/`FolderContentsList`/`FolderContentsAppRow`,
`false` by default so real folders' contents are unaffected. The badge's (and category's) glyph is
a custom `ImageVector` built from a supplied Material Symbols "history" SVG path (`RecencyIcon` in
`FolderContentsSheet.kt`, `internal` for cross-file reuse) rather than
`androidx.compose.material.icons`' own (older, visually different) `History` icon — parsed via
`PathParser().parsePathString(...)`, then shifted into Compose's `[0, viewportHeight]` coordinate
space with a `group(translationY = 960f)` wrapper, since the source SVG's
`viewBox="0 -960 960 960"` has no direct Compose equivalent. The badge renders it as literal white
(`Color.White`, not a theme token) — its background (`IconTile`) is dark in both light and dark
theme, so a theme-aware tint would go dark-on-dark; the category glyph itself uses `InkInverted`
on `Ink` instead (see above), which stays high-contrast without needing a literal color.

**Alphabet rail entry**: `AlphabetRail`/`railSelectionAt` gained a `recentlyInstalledPosition:
RailFolderPosition` param (reusing the same `NONE`/`TOP`/`BOTTOM` type already used for the folder
glyph — mapped from the data-layer `RecentlyInstalledPosition` at the call site, and forced `NONE`
in Grid) and a third `RailSelection` case, `RecentlyInstalled`. Slot ordering in both the rail's
`Layout` and `railSelectionAt`'s touch-to-selection mapping mirrors the list's own render order:
Recently Installed then a `TOP` folder glyph at the top end, a `BOTTOM` folder glyph then Recently
Installed at the bottom end — so the category is always the outermost item at whichever end it's
on. Tapping/dragging onto it scrolls to index `0` when `SHOW_FIRST`, or to the very last index
(after any `SHOW_LAST` folder section) when `SHOW_LAST` — `onRailSelectionChanged`'s
`RailSelection.RecentlyInstalled` branch and `RailSelection.Folders`' `SHOW_LAST` branch now share
a local `afterLetteredContentIndex()` helper for that shared "index right after all lettered
content" computation.

A leading header+row shifts every absolute lazy-list index after it by 2 — `letterIndexOffset`
(used by the alphabet rail's letter-jump) and `onRailSelectionChanged`'s `RailSelection.Folders`
case (`SHOW_FIRST`'s fixed offset and `SHOW_LAST`'s summed index) add a `recentlyInstalledOffset`
(0 or 2, and always 0 in Grid or when the position is `SHOW_LAST`) to stay correct. A trailing
header+row needs no such offset — `DrawerListContent` renders it as the very last two `item`s
instead, gated on `recentlyInstalledPosition == SHOW_LAST`.

**Private Space's own copy**: `PrivateSpaceScreen`/`PrivateSpaceViewModel` get an independent
instance of the same feature — same `RecentlyInstalledAppsUseCase` class and the same global
`recentlyInstalledPosition` setting (including `SHOW_LAST` placement and its own icon-only
`PrivateSpaceRecentlyInstalledHeader`, via a `recentlyInstalledPosition` param on
`PrivateSpaceScreen` deciding whether its own leading/trailing header+row pair renders), but
computed separately over Private Space's own already-isolated app list
(`PrivateSpaceViewModel.recentlyInstalledApps`, a `combine(apps, query, settings)` `StateFlow`,
hidden while `query` is non-blank — mirroring the main Drawer's own search-hides-browse-mode-content
precedent). Not shared state with the main Drawer's version — no alphabet rail there to place it on
either, just the row's own position in its flat `LazyColumn`. The row there
(`PrivateSpaceRecentlyInstalledRow`) is styled through `PrivateSpaceTheme`'s own
`MaterialTheme.colorScheme.*` tokens, like every other Private Space row, not this app's usual
`Accent`/`Ink`, and also gets the sheet's recency badge (`showRecencyBadge = true`).

### 1c. Grid density (`DrawerGridDensity`)

Grid presentation only. `COMPACT`/`REGULAR`/`SPACIOUS` pick a target cell width and row pitch
(`ui/drawer/DrawerGridLayout.kt`); the column and row counts are computed from the viewport, so one
setting fits a phone, a foldable and a tablet (a typical phone gets 5/4/4 columns and 6/5/4 rows).
`DrawerGridContent` derives both counts from its own constraints and divides the height into equal
rows; search results use the same column count, laid out as equal-weight cells so tiles sit on the
same grid. The setting persists under the old `drawer_grid_size` key; the retired `FIVE_BY_SIX`-style
names still load (`DrawerGridDensity.fromStored`) and map to the nearest density. Home's own grid
(`AppListGridColumns`) is a separate setting and still fixed-column.

### 1d. Preview on the App drawer settings screen

`AppDrawerSettingsScreen` opens with `AppDrawerSettingsPreview` (`ui/settings/`): the real `AppDrawerScreen` on eight sample
apps (null icons, so the tile placeholder; labels from `R.array.drawer_preview_app_labels`) and one sample folder, in a
card over the system wallpaper. The drawer is laid out at the real screen width and 400dp of height, then scaled down to the
card's width (about 350dp tall on a phone), so its columns and rows are the ones a phone gets (`DrawerGridLayout`'s
two-row minimum lets a short viewport show Compact 3, Regular 2, Spacious 2 rows). The wallpaper is
`AppDrawerSettingsViewModel.homeWallpaper`, so the opacity slider shows. It is fed the
current `LauncherSettings`, so presentation, grid density, list size, icons, labels, folders in drawer, recently installed,
search bar position and opacity all show as they change. `drawerPreviewSample` builds the sample (two apps installed an hour
ago for the "Recently installed" row). Taps do nothing (an overlay takes them, and its vertical drags scroll the drawer's own list or grid state, passing what they can't consume on to the settings
list), `consumeWindowInsets(systemBars)` stops the drawer padding for the status bar, and
`clearAndSetSemantics` keeps the sample apps out of the accessibility tree.

## 2. Search pipeline

```mermaid
flowchart LR
    Q["searchQuery: MutableStateFlow&lt;String&gt;\n(onQueryChanged)"]
    Q --> A["Apps: filtered in the composable\nRankBySearchRelevanceUseCase(apps, query) { label }\nprefix match ranks above substring"]
    Q --> C{"settings.searchContactsEnabled\n&amp;&amp; ContactPermissionRepository.isGranted()"}
    C -- yes --> CR["ContactRepository.searchContacts(query, MAX_CONTACT_RESULTS)\nContactsContract via ContentResolver, Dispatchers.IO\n→ rankBySearchRelevance { displayName }"]
    C -- "enabled but not granted" --> P1["showContactsPermissionPrompt"]
    C -- "granted but disabled" --> P2["showContactsSettingPrompt → enableContactSearch()"]
    Q --> SS{"settings.searchSettingsEnabled"}
    SS -- yes --> SR["SystemSettingsRepository.search(query)\nstatic catalogue of Settings.ACTION_* entries\nwith keyword aliases (no relevance re-rank: alias hits keep catalogue order)"]
    CR --> OUT["contactResults: StateFlow"]
    SR --> OUT2["settingsResults: StateFlow"]
    A --> S[AppDrawerScreen sections: Apps / Contacts / Settings]
    OUT --> S
    OUT2 --> S
```

- Contacts and settings searches are `flatMapLatest` over `(query, enabled)` — a new keystroke
  cancels the in-flight `ContentResolver` query.
- Both prompts are gated by a `MutableStateFlow` "dismissed" flag so they show once per drawer
  session; `contacts_permission_requested` in DataStore records that the system dialog was
  shown at least once.
- Tapping a contact opens `ContactConnectionsSheet`; connections (phone/email/messaging apps
  registered for that contact) are fetched on open via `ContactRepository.getConnections(id)`.

## 3. Per-app actions (long-press → `AppContextMenu`)

| Action | Path | Notes |
|---|---|---|
| Launch | `onAppClick` → `LauncherActivity.launchApp` | other-profile apps via `LauncherApps.startMainActivity`; also fired by tapping the sheet header's icon or name (`onOpenApp`, sheet closes first) |
| Notifications zone | `DrawerViewModel.observeAppNotifications(app)` → `ObserveAppNotificationsUseCase` (setting `show_notifications_in_app_menu` + `NotificationAccessRepository.isGranted()` + `NotificationBadgeRepository.notificationsFor(pkg, handle)`) → `AppNotificationsState(notifications, needsAccess)` | live while the sheet is open; only this app in this profile; see below |
| Open / dismiss a notification | `DrawerViewModel.openNotification(n)` → `NotificationOpener.open(contentIntent)`, then `NotificationActionController.dismiss` if the notification asked for `autoCancel`; swipe and "Clear all" → `dismissNotifications(keys)` → the listener's `cancelNotifications` | a dismiss with no connected listener is a no-op that returns false |
| Add/remove to Favorites / Dock | `QuickAddState` from `ObserveQuickAddStateUseCase` decides which action is offered; `DrawerViewModel.onFavoritesAction / onDockAction` → the 4 app use cases | routing in [08 §1](08-flow-placements.md) |
| Add to folder / create folder | `DrawerViewModel.createFolder(app, name)`, `addToFolder(app, folderId)` → `FolderRepository` | `folders` observed for the submenu |
| Notification dot/count | `badgeCounts` = `NotificationBadgeRepository.badgeCounts` gated by `notification_dots_enabled` | style from `notification_badge_style` |
| App info | `DrawerViewModel.openAppInfo(app)` → `AppRepository.openAppDetails` (`LauncherApps.startAppDetailsActivity`, profile-aware) | header icon button |
| Uninstall | composable fires `Intent(ACTION_DELETE, package:…)` (`REQUEST_DELETE_PACKAGES`) | header icon button; the system dialog uninstalls; `CleanUpUninstalledAppsUseCase` then deletes any placements |

App shortcuts (`LauncherApps.getShortcuts`) are no longer shown in the menu.

**Sheet layout.** `AppContextMenu` is one scrolling page with three zones, each behind a 1dp
`CardDivider`: a header (icon and name, one tap target that opens the app, and App info and
Uninstall as 48dp icon buttons), the notifications zone (left out only when the setting is off), and the
placement actions. It opens at its full
content height (`skipPartiallyExpanded`) and scrolls when the zones don't fit.

- **Notifications zone.** Present whenever "Show notifications in app menu" is on, and only the setting
  hides it. A header ("Notifications · n" or "Notifications") carries "Clear all", greyed out unless there
  is something to clear. Below it, one of: the access row, an empty state ("No notifications", access
  granted but nothing posted), or the list. The list is a `LazyColumn` window sized from the measured first
  card to two cards plus a 40% peek of the next, and never over a third of the screen. Each card shows the
  title, a compact age (`1s`, `2m`, `3h`, `4d`; `ShortAge.kt`, localised unit strings) and up to two lines
  of text. Cards open on tap (the sheet closes first) and swipe away singly; "Clear all" swipes every visible clearable card off to the right together (each asks the listener once its swipe ends), dismisses the off-screen ones in one batch, and leaves the sheet open. Rows animate out (`animateItem`) and the zone's height animates (`animateContentSize`). A persistent (ongoing, non-clearable) notification is listed
  and tappable but has no swipe, is left out of "Clear all" (which greys out when only persistent ones remain),
  and is never dismissed after opening. With the setting on but no notification access the body is one
  "Enable notifications" row that closes the sheet and calls
  `HomeDrawerRoute.onNavigateToNotificationAccessExplanation` (→ the access explanation route). It is fed to
  the sheet as one `AppMenuNotifications` holder threaded through `HomeScreen`, `AppDrawerScreen` and
  `FolderContentsSheet`.
- **Placement actions** (`PlacementActions`): Favorites, Dock and Folder (or "Remove from folder") as
  `PlacementActionUi`. Each is a tile with its scope pill (`FacetScopeBadge`: "Global" or the facet name)
  on top, an icon with a +/− badge, and the label ("Add to X" / "Remove from X"); an action the app is
  already in is filled with an accent wash. Folder has no pill but its tile keeps an invisible pill's
  height so the three icons align. The tiles become full-width 56dp rows when a measured fit fails:
  any label needs more than two lines, any word is wider than the tile's text width, or any pill is
  wider than it. The check uses `TextMeasurer` at the tile width, so it holds for any language, facet
  name, system font scale or in-app Text size, and all actions switch together.
- **Text scaling.** Text follows both the system font scale and the in-app Text size. Header and
  placement icons grow with them up to `MAX_CHROME_ICON_SCALE` (1.3x, `ChromeScale.kt`) and never shrink
  below their base size; the +/− badge keeps a fixed 16dp.

Folder tiles get the mirror menu (`FolderTileContextMenu`): the same header (folder glyph, name, app
count, Rename as an icon button) over Favorites/Dock tiles. A folder has no notifications, no Uninstall
and no Delete there — a folder is only deleted from Settings → Folders.

**Long-press timing:** the menu opens at the long-press threshold itself, not on release. Every
long-press row/tile across Home, Dock, and Drawer (`AppRow`, `SingleAppDockIcon`,
`FolderDockIcon`, `FolderRow`, `DrawerAppRow`, `DrawerFolderRow`, `DrawerGridTile`,
`DrawerFolderTile`) uses `Modifier.longPressReleaseClickable`
(`ui/components/LongPressReleaseGesture.kt`) in place of `combinedClickable` — a
`HapticFeedbackType.LongPress` tick and `menuExpanded = true` fire together the instant the
threshold (the platform default, `ViewConfiguration.getLongPressTimeout()` — 500ms) is met. The
tick is also wired into the Widget Hub's own long-press grab gesture, `WidgetGrabGesture.kt`, so
it's consistent across every long-press surface in the app. A prior version of this gating opened
the menu only on release, as deliberate prep for a future long-press-drag (reorder/merge-to-folder)
feature on these same rows; that feature never landed, and immediate-open-on-threshold is simpler,
so the release gating was removed. If that drag feature is picked up later, this timing will need
revisiting.

## 4. Gestures & routing (why the drawer isn't a nav destination)

`HomeDrawerRoute` owns every follow-finger drag that starts on Home, on one axis at a time
(`Axis` resolution on the first frame). The logic is `HomeSwipeController` (`launcher/`): three
`SwipeAxisState`s (Drawer, Hub, carousel), the axis lock, the shade-expand rule, and the two
nested-scroll bridges (a hosted clock widget through `WidgetScrollBridge`, and the app list's leftover
scroll). `swipeSettleTarget` is the pure open/closed decision. `HomeSwipeControllerTest` and
`HomeSwipeGesturesTest` cover it:

| Drag on Home | Opens | Reverse gesture closes |
|---|---|---|
| up | Drawer | drag down on the drawer's list, only once scrolled to the top |
| right | Hub | drag left on the Hub |
| left | Facet carousel | drag right on the carousel (empty space or first card) |

In landscape the whole clock pane is treated like the app list for this: it scrolls its own content, so a
vertical drag that starts there is not Home's (swiping up opens the Drawer from the apps side only), while a
horizontal one still opens the Hub or carousel. `HomeDrawerRoute` gets the pane's bounds through
`HomeScreenActions.onClockPaneBoundsChange`.

**The clock stack's pager comes first for a horizontal drag in the clock zone.** The zone is everything above the
divider in portrait (`HomeScreenActions.onClockZoneBoundsChange`, from the clock block's bottom) and the clock pane in
landscape. When such a drag moves, `HomeSwipeController` asks `ClockPagerState.canMove(dx)`: left needs a next page,
right a previous one. If there is one, the pager owns the whole drag, including a reversal; if not (the first or
last page, a single page, an empty stack) the drag goes to Hub or the carousel as above. A hosted widget's handed-over
scroll (from its own nested scrolling, or taken by `ClockWidgetTouchGate` itself for a horizontal-dominant drag the widget did not claim, so static widgets can be swiped away) counts as starting in the zone, and its fling velocity is negated for the pager (a view's is in scroll
direction). A drag past `PAGE_COMMIT_FRACTION` (20%) or a fling over `PAGE_FLING_VELOCITY_PX` turns the page,
otherwise it springs back.
The pages form a carousel: while the drag moves the pages by the pager's `offset`, the pages beside the visible one (`HomeClockStackState.next` / `previous`, built by `HomeScreenStateBuilder`) are composed and attached all the time, in one keyed list in `PageStack` (`HomeSlots.kt`): the one the drag is turning towards slides in from the other side, the other waits parked far to the side. A page's shift is its distance in pages from the page the swipe began on (`ClockPagerState.anchorIndex`) plus the offset, in zones, so the page that arrives keeps its place when it becomes the visible one. A committed swipe carries the pages to the edge, changes the page, waits for the change to reach Home (`ClockPagerState.awaitPage`) and resets the offset, so the arrived page, keyed by its stack item, is the same node and widget view with no second slide-in. In landscape the stack fills the clock pane and each page rests where its own alignment puts it (top, middle or bottom), so a page that is taller or aligned differently is neither clipped nor repositioned on landing. The clock slot is placed from its height measured in the same layout pass (not last frame's), so a page of another height never shows a frame at the old one's position. A swipe on a hosted widget is taken by `ClockWidgetTouchGate` itself when the widget does not claim it, measured in screen coordinates because the page moves under the finger. `ClockPagerStateTest`, `HomeSwipeControllerTest` and four `HomeDrawerRouteTest` cases cover it.

In clock adjust mode (`ClockAdjustMode.ADJUST`) the screen-wide detector is replaced by a horizontal-only one (`onAdjustDrag*` on the controller): a drag in the clock zone turns the pages and nothing else (no Hub, carousel or drawer, no hand-off at the ends, adjust mode kept), so each page can be sized and placed in turn. It sits after the handles in the pointer chain, so a handle keeps its own drags; the dots are held visible.

A drag locks to an axis only after 16dp of travel (`HOME_SWIPE_SLOP`), and counts as vertical only
if its vertical movement exceeds `VERTICAL_DOMINANCE` (1.2x) the horizontal — anything else is
horizontal. A touch that starts inside the app list's own region is still watched, but only a
horizontal-dominant drag is claimed (so Hub/carousel work over the list); a vertical one is left to the
list's own scroll, which hands leftover drag back via `appListNestedScrollConnection`. The list's bounds reach the route through a `snapshotFlow` in `HomeScreen`, not a `SideEffect` (which would report stale bounds — the layout state is written after layout and read nowhere in composition — so the drawer would claim drags that should scroll the list).

Commit happens when the drag has travelled `COMMIT_TRAVEL_FRACTION` (20%) of the container *from where
that drag started*, or released above `VELOCITY_THRESHOLD_PX` (1500 px/s); anything short springs back.
A downward drag from a closed drawer expands the notification shade on the same terms
(`SWIPE_DOWN_SHADE_FRACTION`, 20% of the height).

Every Home action menu — `AppContextMenu`, `FolderTileContextMenu`, the clock adjust menu and the one-time
"make Facet your home screen" prompt (`SetDefaultLauncherSheet`) — is a `ThemedModalBottomSheet` (`components/`): one themed M3 `ModalBottomSheet`, so the surface runs behind the
system bars and swipe-down and Back dismiss it the same way everywhere. The surface is `Surface` unless the caller passes `containerColor`: the clock adjust menu passes the dimmer `SurfaceContainer` because its content (`ClockAdjustSheet`) is two tonal `SettingsCard`s of icon-badge rows — Adjust clock size & position, Clock & calendar settings (with the Global / facet-name badge) and, by the visible page, Replace with custom widget (clock page) or Replace custom widget and Replace with clock (widget page; the latter hidden while the clock is on another page); then the Widget stack row, Facet settings and Launcher settings. While one is showing, Home's swipes
are off — otherwise the still-down finger that opened the sheet could go on to open the carousel or Hub
behind it. The wrapper calls `BlockHomeSwipesWhileShown()` (`HomeSwipeGate`, provided by
`LauncherActivity` via `LocalHomeSwipeGate`); `HomeDrawerRoute` skips its swipe detector and the app
list's scroll handoff while the gate is non-zero (clock *adjust mode*, with its drag handles and no sheet,
is gated separately on `clockAdjustMode`).
System back is unaffected.

**Adjust mode** (`ClockAdjustMode.ADJUST`, entered from the clock menu) has no sheet: the clock or hosted widget
gets corner resize handles, a full-width height handle (`ClockZoneHandle`, 48dp touch strip), and the
`ClockAdjustToolbar` — an "Alignment" pill with left / center / right. The toolbar sits *below* the height
handle (strip + `CLOCK_ADJUST_TOOLBAR_GAP`), not under the clock: the clock block is pinned only 24dp
(`HOME_CLOCK_MIN_GAP`) above the handle, too little room for it. It writes through
`HomeViewModel.onClockAlignmentCommit` to the facet: the clock page's alignment, scale and divider height are the facet's own (`CommitClockPlacementUseCase`, copy-on-first-adjust, see [17](17-flow-home-layout.md)), and a widget page saves its own alignment against its stack item. The Clock Style screen has no alignment row any more. Changing alignment re-clamps an oversized clock through the existing `clockAlignment`-keyed effect. The
toolbar dims to 25% and its options are disabled while any handle or resize drag is active, and becomes usable again
~200ms after release, so a thumb coming off the handle can't tap it (it stays composed, so it doesn't pop in and out).
It swallows taps on its own surface even when disabled, because Home's root exits adjust mode on any tap that reaches
it unconsumed (empty space); there is no Done button — tapping the clock or empty space, Back and Home all exit.

**Media player row** (`MediaPlayerRow`, see [13 §3](13-flow-facets-theme-notifications-onboarding.md)) sits just under the
clock block, at `handlePx - HOME_CLOCK_MIN_GAP + HOME_MEDIA_ROW_TOP_GAP`, and while visible pushes a top-aligned app list
down by its own measured height (`HomeMediaLayout.listTopPx` replaces `handlePx` for the list's offset, max height and the compact-spacing
check). A bottom-aligned list stays put and the row fills the gap. Its height is kept after it hides, and it is not composed
in clock adjust mode (so the handles own that space), so neither shifts the list. Its taps are scoped to the art and the
title, so Home's own long-press still works around it.

**Pressing Home** (`LauncherActivity.onNewIntent` → `LauncherViewModel.homePressedEvent`) collapses
everything back to bare Home, where Back only peels off the top layer. `HomeDrawerRoute`'s collector clears
focus, resets the clock adjust sheet and the clock widget picker, and closes the Drawer, Hub and carousel
axes — each close in its own `launch`, never inline: a drag landing on the same `Animatable` throws a
`CancellationException`, and inline that escaped `collect` and unsubscribed the collector for good, so
Home silently stopped working until the app restarted (`HomeDrawerRouteTest`). Every sheet, dialog and
overlay that composes its own state subscribes with `DismissOnHomePress` — `ThemedModalBottomSheet` does it
for every action menu and for the facet automation rule editor (a settings sheet, so a Home press while it is
open closes it and drops the draft), and `FolderContentsSheet` and the contact connections sheet call it
directly; a new one must do the same.
A Home press while the launcher is already in front and bare (Drawer, Hub, carousel, clock adjust, pickers, Private Space and sheets all closed) instead opens the Drawer and focuses its search field. `LauncherActivity.onNewIntent` passes `whileResumed` (lifecycle at least STARTED: onNewIntent runs after a pause) to `LauncherViewModel.onHomePressed`, which `HomeDrawerRoute`'s collector reads as `homePressedWhileResumed` before it closes anything, then bumps `DrawerSearchBarConfig.focusRequest`. A press from another app, or with anything open, only returns to bare Home (`HomeDrawerRouteTest`).
Samsung's swipe-up gesture injects `KEYCODE_HOME` itself, so it reaches this path exactly like the button.

Because the transition tracks the finger, none
of these can be `NavHost` transitions — Home, Drawer, Hub and Carousel are composed together
under the single `HOME` destination, and the keyboard is dismissed on drawer close
(`KeyboardDismissalTest`). Everything else (settings, pickers, facet settings) is a real
`FacetDestinations` route.
