# 02 — Database & Persistence Architecture

## 0. Storage map — everything Facet persists, and where

| Store | Location | What it holds | Written by | Survives |
|---|---|---|---|---|
| Room `FacetDatabase` | `facet.db` (schema **v47**, `exportSchema = true` → `app/schemas/…/1.json … 47.json`) | Facets + every *placement*: favorites, dock, folders, folder membership, widget grid positions, plus the facet automation rules (`automation_rules`) the weather cache (`weather_hours`, `weather_meta`) and each facet's clock stack (`clock_stack_items`) | 7 repositories (§2): `FacetRepository`, `PlacementRepository`, `FolderRepository`, `WidgetPlacementRepository`, `AutomationRuleRepository`, `WeatherRepository` (through `RoomWeatherStore`), `ClockStackRepository` | Reinstall-over-upgrade (migrations); **not** downgrade (dropped) |
| `DataStore<Preferences>` | `facet_settings` (`datastore/facet_settings.preferences_pb`) | **All launcher-wide settings and defaults** — clock/app-list/dock design defaults (the calendar events strip has no design defaults of its own — see below), theme, drawer, search, permission-prompt flags, onboarding/seed/coach-mark flags, active facet id, the clock accessory switches, weather units and location, the icon pack and size — 86 keys (§5) | `SettingsRepository` only | Any upgrade (missing keys fall back to `LauncherSettings()` defaults) |
| `DataStore<Preferences>` | `facet_entitlement` (`datastore/facet_entitlement.preferences_pb`) | The cached Pro state (`pro_purchased`; excluded from Android backups and device transfer by `res/xml/data_extraction_rules.xml`), so Pro works offline and at cold start; debug builds also keep `debug_entitlement_mode` here | `PlayEntitlementRepository`, only from an explicit Google Play answer | Same as `facet_settings`; **not** in the backup file, so a restore can never grant Pro |
| `DataStore<Preferences>` | `facet_automation` (`datastore/facet_automation.preferences_pb`) | Facet-automation bookkeeping: the baseline facet, the ordered active rule ids, the suppressed rule ids (§5.7) | `AutomationStateRepository` (via `ActivateFacetByIdUseCase`) | Same as `facet_settings`; **not** in the backup file |
| System `AppWidgetService` | Android framework, keyed by `HUB_APP_WIDGET_HOST_ID = 1024` | Which `appWidgetId`s are bound to which providers for this host | `LauncherAppWidgetHost` via `AppWidgetRepository` (allocate/bind/delete) | App data clear **does not** clear it → orphan detection in `ObserveHubStateUseCase` |
| Backup file | User-picked SAF `Uri` (`CreateDocument`/`OpenDocument`), JSON via `kotlinx-serialization` | `BackupBundle` v6: settings + facets + placements + folders + widget placements (raw rows, unhydrated) | `BackupRepository` (`ExportBackupUseCase` / `ImportBackupUseCase`) | Whatever the user does with the file — Facet keeps no copy |
| In-memory only | `NotificationBadgeRepository.badgeCounts` (`MutableStateFlow`) | Per-package non-silent notification counts | `FacetNotificationListenerService` | Process lifetime only |

Nothing else is persisted: no `SharedPreferences`, no files in `filesDir`/`cacheDir`, no network.

**Launcher defaults seed a facet; a facet owns its values.** The app-list and dock *defaults* the
user sets in Settings are DataStore keys (`list_content_mode`, `dock_display_mode`, …) plus the launcher-wide
Dock and Favorites rows (`ownerKey = 0`). The `facets` table carries the same fields again, and a facet
*owns* them: there is no override flag and no inheritance. `ApplyFacetDefaultsUseCase` copies the defaults into a
facet once, when it is created, and again when the user chooses *Reset to default* on one of its screens; later
edits to the defaults never reach an existing facet. A clock's style, accessories and calendar live on its
page, see [13](13-flow-facets-theme-notifications-onboarding.md). The ER diagram in §1 is Room only —
the defaults are in §5. The calendar events strip is the one exception to this pattern — it has no
design fields of its own anywhere (Room or DataStore); it reads the global-only
`home_apps_font_weight`/`app_label_color_option` (§5.4 "Home app list") and `launcher_font_option`
(§5.4 "Theme & appearance") keys for font/weight/color, and `clockAlignment` (Room,
facet-overridable) for its position — see chat history: calendar/appearance styling consolidation.

What is **not** persisted: the installed-app list. Room rows reference apps only by
`(packageName, activityName, userId)`; labels, icons and `UserHandle`s are re-resolved live from
`LauncherApps` on every emission (see [03](03-reactive-data-flow.md)).

## 1. Entity-Relationship diagram

```mermaid
erDiagram
    facets {
        long id PK "autoGenerate"
        string name
        int position
        boolean clockPlacementOwn "false until first adjusted on Home: then alignment, zone height, scale and their landscape twins are the facet's own"
        string clockAlignment "own placement, used while clockPlacementOwn"
        float clockZoneHeightDp "nullable"
        string appRowPosition
        string appRowPresentation
        string listContentMode
        int appsToShowCount
        string appListVerticalAlignment
        string appListLayout "single column / two column / grid"
        string appListColumnAlignment "two-column only"
        string appListGridColumns "grid only"
        string appListGridDisplayMode "grid only"
        string appListShrinkToFit "list only"
        string dockDisplayMode
    }

    app_placements {
        long id PK
        string list "DOCK or FAVORITES"
        long facetId FK "nullable, CASCADE — null = launcher-wide default list"
        long ownerKey "facetId or 0, unique key column"
        string packageName
        string activityName
        int position
        string profile
        int userId
        int portraitRow "nullable Home grid cell from the anchor edge, null until moved"
        int portraitColumn
        int landscapeRow
        int landscapeColumn
    }

    folders {
        long id PK
        string name
    }

    folder_apps {
        long id PK
        long folderId FK "CASCADE"
        string packageName
        string activityName
        int position
        string profile
        int userId
    }

    folder_placements {
        long id PK
        string list "DOCK or FAVORITES"
        long facetId FK "nullable, CASCADE — null = launcher-wide default list"
        long ownerKey "facetId or 0, unique key column"
        long folderId FK "CASCADE"
        int position
        int portraitRow "nullable Home grid cell from the anchor edge, null until moved"
        int portraitColumn
        int landscapeRow
        int landscapeColumn
    }

    widget_placements {
        int appWidgetId PK "host-allocated id"
        string providerPackageName
        string providerClassName
        int row
        int col
        int colSpan
        int rowSpan
        string profile
        int userId
        int landscapeOrder "nullable; landscape Hub flow order"
        string nativeWidgetType "nullable; set for a widget Facet draws itself, appWidgetId then negative"
        string nativeWidgetConfig "nullable; the widget's own calendars or weather location, null follows the launcher"
    }

    facets ||--o{ clock_stack_items : "has (CASCADE)"
    clock_stack_items {
        long id PK
        long facetId FK
        int position "swipe order"
        string kind "CLOCK, WIDGET or NATIVE_WIDGET"
        int appWidgetId "null for CLOCK and NATIVE_WIDGET"
        string nativeWidgetType "nullable; set for NATIVE_WIDGET"
        int widthDp "nullable until resized"
        int heightDp
        string alignment "nullable"
        int landscapeWidthDp "null falls back to portrait"
        int landscapeHeightDp
        string landscapeAlignment
        string nativeWidgetConfig "nullable; the page's own calendars or weather location, null follows the launcher"
    }

    weather_hours {
        long timeEpochSecond PK "the forecast hour, UTC"
        double temperatureC
        double humidityPercent "nullable, as are the next six fields"
        double pressureHpa
        double cloudPercent
        double windSpeedMps
        double windFromDegrees
        double precipitationMm "null on a 6-hourly point"
        string symbolCode
    }

    weather_meta {
        int id PK "always 1"
        double latitude "the place the hours are for; nullable"
        double longitude
        int altitudeM
        long fetchedAtEpochSecond
        long expiresAtEpochSecond "MET Expires"
        string lastModified
        long lastAttemptAtEpochSecond
        long blockedUntilEpochSecond "after a 429"
        string placeName
        string placeNameSource "GEOCODER or OFFLINE"
    }

    facets ||--o{ automation_rules : "rule target (CASCADE)"
    facets |o--o{ automation_rules : "switch-to end facet (SET NULL)"
    facets |o--o{ app_placements : "per-facet dock and favorites"
    facets |o--o{ folder_placements : "folder in a facet's dock or favorites"
    folders ||--o{ folder_placements : "placed in dock or favorites"
```

### Reading the diagram

- **One table per kind, two lists, two scopes.** `app_placements` and `folder_placements` hold every
  Dock and Favorites entry. `list` (`PlacementList.DOCK | FAVORITES`) picks the list; a null `facetId`
  is the *launcher-wide default* list (the seed a new facet is copied from) and a facet id is that facet's own
  list, always the live one for that facet via `Facet.placementTarget(list)`;
  `AddToActiveListUseCase` / `RemoveFromActiveListUseCase` ask
  `ResolveActivePlacementTargetUseCase` for the right `PlacementTarget(list, facetId)` and write it
  through `PlacementRepository`, the one repository over both tables.
- **`ownerKey` exists because SQLite treats NULLs as distinct in a unique index.** It stores `facetId`,
  or `GLOBAL_OWNER_KEY` (`0`) for the global lists, so the unique keys below also hold for global rows.
  The entity derives it from `facetId` at construction; never set it independently.
- **Apps and folders share a position axis.** `PlacedItem` (`SingleApp` | `FolderItem`) is the
  in-memory union; app rows and folder-placement rows in the same scope are merged and sorted by
  `position` at read time (no cross-table constraint enforces uniqueness of `position`).
- **Uniqueness indices** (all `unique = true`):
  `(list, ownerKey, packageName, activityName, userId)` on `app_placements`,
  `(list, ownerKey, folderId)` on `folder_placements`, and
  `(folderId, packageName, activityName, userId)` on `folder_apps`. `userId` is part of every key so the
  same component can be placed once per profile (personal + work).
- **Profile identity.** `profile` (`AppProfile`: `PERSONAL | WORK | PRIVATE | OTHER`) is a display
  classification; `userId` (`UserHandle.hashCode()`) is the identity. `-1` marks rows written before
  v20 that `RepairOrphanedProfileRowsUseCase` backfills once at startup (`getOrphaned()` DAO queries).
- **Cascades.** Deleting a facet or folder removes all its placement rows in SQLite; the
  repositories never have to clean up children manually.

## 2. DAO → Repository → Flow mapping

```mermaid
flowchart LR
    subgraph DB["FacetDatabase (Room)"]
        FD[FacetDao]
        APD[AppPlacementDao]
        FPD[FolderPlacementDao]
        FOD["FolderDao\n(@Transaction FolderWithApps)"]
        WPD[WidgetPlacementDao]
        ARD[AutomationRuleDao]
        WTD[WeatherDao]
        CSD[ClockStackItemDao]
    end

    subgraph REPO["data/ repositories"]
        FR[FacetRepository]
        PR["PlacementRepository\n(every Dock and Favorites list)"]
        FOR[FolderRepository]
        WPR[WidgetPlacementRepository]
        ARR["AutomationRuleRepository\n(maps rows to AutomationRule)"]
        WTR["WeatherRepository\n(via RoomWeatherStore, plus the MET API)"]
        CSR["ClockStackRepository\n(a facet's clock stack)"]
        AR["AppRepository\n(LauncherApps, not Room)"]
    end

    FD --> FR
    APD --> PR
    FPD --> PR
    FOD --> FOR
    WPD --> WPR
    ARD --> ARR
    WTD --> WTR
    CSD --> CSR
    FD -. "facet exists check on save" .-> ARR

    AR -. "observeInstalledApps()" .-> FOR
    AR -. "observeInstalledApps()" .-> PR
    FOR -. "observeFolders()" .-> PR

    FR -- "Flow of List Facet" --> OUT((use cases / ViewModels))
    PR -- "Flow of List PlacedItem, or Map facetId to List PlacedItem" --> OUT
    FOR -- "Flow of List Folder" --> OUT
    WPR -- "Flow of List WidgetPlacementEntity" --> OUT
    ARR -- "Flow of List AutomationRule" --> OUT
    WTR -- "Flow of List WeatherHour, Flow of WeatherFetchState" --> OUT
```

### The DAO contract

Every DAO follows the same shape, which is what makes the reactive layer uniform:

| Kind | Signature pattern | Room behaviour |
|---|---|---|
| Observe | `fun observe(list, ownerKey): Flow<List<Entity>>` (placements) / `observeAll()` — always `ORDER BY position ASC` | Cold `Flow`; re-emits after any write to the queried table(s). Main-safe. |
| Write | `suspend fun upsert(entity): Long` with `OnConflictStrategy.REPLACE`; `suspend fun delete…` | Runs on Room's own I/O executor; the caller just `suspend`s. |
| Scoped delete | `deleteByComponent(list, ownerKey, pkg, activity, userId)`, `deleteByPackage(list, global, pkg, userId)`, `deleteByUserId(list, global, userId)`, `deleteAllForOwner(list, ownerKey)` | Used by `CleanUpUninstalledAppsUseCase` (uninstall), profile-removal handling, facet deletion, and backup restore. |
| Repair | `suspend fun getOrphaned(list, global): List<Entity>` (`WHERE userId = -1`) | One-shot; only `RepairOrphanedProfileRowsUseCase` calls it. |
| Raw export | (repository) `getRawXxx() = dao.observe(list, owner).first()` | Backup export reads *unhydrated* rows so an app that is currently uninstalled still gets backed up. |

### DAO method matrix (every method, every DAO)

| DAO | Observe | Write | Delete | Read one-shot |
|---|---|---|---|---|
| `FacetDao` | `observeAll()` | `insert(facet): Long` (plain `@Insert`, **no** REPLACE — ids are never reused), `update(facet)` | `delete(facet)`, `deleteAll()` | `getById(id)` |
| `AppPlacementDao` | `observe(list, ownerKey)` | `upsert(entity): Long` (REPLACE) | `deleteByComponent(list, ownerKey, pkg, activity, userId)`, `deleteByPackage(list, global, pkg, userId)`, `deleteByUserId(list, global, userId)`, `deleteAllForOwner(list, ownerKey)` | `getOrphaned(list, global)` (`userId = -1`) |
| `FolderPlacementDao` | `observe(list, ownerKey)` | `upsert` (REPLACE) | `deleteByFolderId(list, ownerKey, folderId)`, `deleteAllForOwner(list, ownerKey)` | — |
| `FolderDao` | `observeAllWithApps()` (`@Transaction`, `FolderWithApps`) | `insertFolder(folder): Long`, `renameFolder(id, name)`, `upsertFolderApp(app): Long` | `deleteFolder(id)`, `deleteAllFolders()`, `deleteFolderApp(folderId, pkg, activity, userId)`, `deleteFolderAppsByPackage(pkg, userId)`, `deleteFolderAppsByUserId(userId)` | `getOrphanedFolderApps()` |
| `AutomationRuleDao` | `observeAll()` (`ORDER BY position ASC, id ASC`) | `insert(rule): Long`, `update(rule)`, `setEnabled(id, enabled)` | `deleteById(id)` | `getById(id)`, `maxPosition()` (`-1` when empty) |
| `ClockStackItemDao` | `observe(facetId)`, `observeAll()` (`ORDER BY position, id`) | `insert` | `delete(id)`, `deleteAll(facetId)` | `getAll(facetId)`, `getById(id)`; updates `clearClockBoxes(facetId)` (a facet's clock pages only, both orientations), `replace` (swaps the kind, widget id and native type, resets the sizes), `setPosition`, `setSize`, `setLandscapeSize`, `setAlignment`, `setLandscapeAlignment`. `ClockStackRepository` wraps add (clock, hosted widget, native widget), replace, remove, reorder, clear and a backup's `restoreNativeWidgets` in `database.withTransaction`, caps a stack at `MAX_CLOCK_STACK_ITEMS` (5) and allows one `CLOCK` item |
| `WeatherDao` | `observeHours()` (`ORDER BY timeEpochSecond ASC`), `observeMeta()` (the one row, or `null`) | `insertHours(list)` (REPLACE), `upsertMeta(meta)` (REPLACE) | `clearHours()` | `getHours()`, `getMeta()`. `RoomWeatherStore.replaceHours` clears and inserts inside `database.withTransaction`, so a reader never sees half a forecast; `writeState` stores `placeName` and `placeNameSource` with the rest of the fetch state, and `WeatherRepository` drops them when the place changes |
| `WidgetPlacementDao` | `observeAll()` (no ORDER BY — grid position is `row`/`col`, not `position`) | `upsert(placement)` (REPLACE on `appWidgetId`) | `deleteById(appWidgetId)`, `delete(placement)` | `getById(appWidgetId)` |

Exceptions to the common shape, all deliberate:

- **`FacetDao.insert` is not an upsert.** Facet ids are referenced by `activeFacetId` in DataStore and by every per-facet placement FK; `restoreFacet` in backup import explicitly copies with `id = 0` to force a fresh id.
- **`FacetDao` has no `deleteByUserId`/`getOrphaned`** — facets are not profile-scoped.
- **`FolderPlacementDao` has no `getOrphaned`** — they reference folders by id, not apps by component; profile repair happens on `folder_apps` instead.
- **`WidgetPlacementDao` has no profile-scoped deletes.** A removed Work Profile's widgets are handled by `ObserveHubStateUseCase` flagging them `isOrphaned` (provider info gone) rather than a DAO sweep.
- **`AutomationRuleDao` rows are addressed by `id` all the way up.** Rule ids are public: `AutomationState` stores them (`activeRuleIds`, `suppressedRuleIds`) and the evaluator matches on them. `AutomationRuleRepository` also skips rows it can't interpret (unknown `triggerType`, or a missing required parameter) instead of failing the whole list.
- **Every `delete*` for a placement is by component/user, never by row `id`** — repositories always resolve the row from an `AppInfo`, so `id` never leaks above `data/`.

`FolderDao` is the one relational DAO: `observeAllWithApps()` is a `@Transaction` query returning
`FolderWithApps(@Embedded folder, @Relation apps)`.

### Room → Flow → UI, step by step

1. A ViewModel launches a write, e.g. `viewModelScope.launch { addToActiveList(PlacementList.DOCK, PlacedItem.SingleApp(app)) }`.
2. The use case resolves the active facet's `PlacementTarget` (`settings.first()` + `facetRepository.getById(...)`), then
   calls `placementRepository.addApp(target, app, position)` → `appPlacementDao.upsert(AppPlacementEntity(list = DOCK, ...))`.
3. Room commits and invalidates the `app_placements` table tracker.
4. Every live `appPlacementDao.observe(DOCK, GLOBAL_OWNER_KEY)` collector re-queries and emits the new `List<AppPlacementEntity>`.
5. `ObservePlacedItemsUseCase.items(target)` is a `combine(placementRepository.observeAppEntities(target), placementRepository.observeFolderPlacements(target), folderRepository.observeFolders(), appRepository.observeInstalledApps())` (the first two are `PlacementRepository`'s raw `appPlacementDao.observe` / `folderPlacementDao.observe` flows); the new entity list is joined against the *latest cached* installed-app list (no `LauncherApps` re-query happens on a Room write) and sorted into `List<PlacedItem>`.
6. `ObserveHomeScreenStateUseCase` combines that with settings/facets/calendar/badges → `HomeScreenState`.
7. `HomeViewModel` maps it into `HomeUiState` and sets `_uiState.value`.
8. `HomeScreen` recomposes from `collectAsStateWithLifecycle()`.

### `Facet` and `FacetEntity`

`FacetEntity` is the Room row and stays inside `data/local`. `FacetRepository` maps it to `Facet`
(`data/model/Facet.kt`) with `FacetMapping.kt`, so ViewModels, use cases and composables never see the
entity: `observeFacets()` and `getById()` return `Facet`, and `restoreFacet()` takes one. `Facet` has the
same fields as the row except `selectedCalendarIds`, which is a real `Set<String>?` where the row keeps a
comma-joined `selectedCalendarIdsCsv`. The helpers (`resolveLandscape`,
`landscape`, `placementTarget`, …) are extensions on `Facet`.

Every facet write takes a facet id (`setClockPlacement(facetId, …)`). The repository re-reads the row and
writes back the change, so a caller holding an older `Facet` cannot overwrite a newer edit.
`FacetMappingTest` fails if a column is added to `FacetEntity` without a matching `Facet` field.

## 3. Type converters

`Converters` maps every enum column on `FacetEntity` (and `profile` on placement rows) to its
`name` string. Each `to*` converter is **lenient**: an unknown stored string falls back to the
enum's default (`ListContentMode.FAVORITES`, `ClockTemplateId.LIGHT_STACK`, `AppProfile.PERSONAL`, …)
instead of returning `null`, because Room's generated code for a `NOT NULL` column crashes on a
null converter result. Renaming an enum constant therefore never needs a migration.

## 4. Migration policy (as configured)

```kotlin
Room.databaseBuilder(context, FacetDatabase::class.java, "facet.db")
    .addMigrations(*Migrations.ALL)                       // 10→11 … 32→33, explicit SQL
    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
    .build()
```

- **Upgrade without a migration = crash**, deliberately (`IllegalStateException` at open). The
  process documented in `Migrations.kt` is: bump `FacetDatabase.VERSION` → add
  `MIGRATION_<old>_<new>` to `ALL` → add a `FacetDatabaseMigrationTest` case
  (`app/src/androidTest/.../data/local/FacetDatabaseMigrationTest.kt`, built on `MigrationTestHelper`).
- **Downgrade** drops all tables (dev-only scenario).
- Versions 1–9 have exported schemas but no shipped migrations — they predate any production install.

| Step | Change |
|---|---|
| 10→11 | `profiles.selectedCalendarIdsCsv` |
| 11→12 | `profiles.calendarFontWeight` |
| 12→13 | `profiles.appListVerticalAlignment` |
| 13→14 | `profiles.clockAlignment`, `calendarAlignment`, `clockZoneHeightDp` |
| 14→15 | `profiles.clockAccentColorOption`, `clockDateStyle`, `clockScale` |
| 15→16 | `profiles.overrideDock`, `dockDisplayMode`; new `profile_dock_apps` table |
| 16→17 | **Rename** `profiles` → `facets`; rebuild `favorite_apps` / `profile_dock_apps` → `facet_dock_apps` with `facetId` FKs (copy-table pattern) |
| 17→18 | Folders: `folders`, `folder_apps` + the four `*_folder_placements` tables (folded into the single `folder_placements` table by 27→28) |
| 18→19 | `profile` column on all six app/widget tables (the four app placement tables, `folder_apps`, `widget_placements`) |
| 19→20 | `userId` column on the same six tables (backfilled from personal user), unique indices rebuilt to include it |
| 20→21 | `facets.clockWidgetAppWidgetId` (PRD F15) |
| 21→22 | `facets.clockWidgetWidthDp`, `clockWidgetHeightDp` (PRD F15) |
| 22→23 | Recreate `facets` (create-copy-drop-rename, same pattern as 16→17) dropping `calendarFontOption`, `calendarColorOption`, `calendarFontWeight`, `calendarAlignment` — calendar/appearance styling consolidation |
| 23→24 | No column change (`LAUNCHER_DEFAULT` is just a new valid string for the already-`TEXT NOT NULL` `appRowPosition`/`appRowPresentation`/`appListVerticalAlignment`/`dockDisplayMode` columns) — data-only `UPDATE` resetting those four columns to `'LAUNCHER_DEFAULT'` on every facet row where the matching `overrideApps`/`overrideDock` flag is `0` (a stale, previously-unread value would otherwise start "overriding" silently); a row where the flag is `1` keeps its real value untouched |
| 24→25 | `facets.appListLayout`, `appListColumnAlignment`, `appListGridColumns`, `appListGridDisplayMode` — Home app list two-column/grid layouts; brand-new columns (`DEFAULT 'LAUNCHER_DEFAULT'`), no data-reset step needed |
| 25→26 | New table `automation_rules` (facet automation rules) with two foreign keys to `facets` (`targetFacetId` CASCADE, `endFacetId` SET NULL) and an index on each; DDL copied from the exported `26.json`, no data to carry over. Tested by `FacetDatabaseMigrationTest.migration25To26…` (schema validation plus both FK actions) |
| 26→27 | `ALTER TABLE facets ADD COLUMN` `showCalendarIndicator INTEGER NOT NULL DEFAULT 1` and `calendarItemsToShow INTEGER NOT NULL DEFAULT 3` (per-facet calendar display overrides, gated by `overrideCalendar`); existing rows keep today's look |
| 27→28 | **F6 consolidation.** Creates `app_placements` and `folder_placements` (DDL copied from the exported `28.json`), copies every row from the eight old tables in `id` order (`list` = `DOCK`/`FAVORITES`, `facetId` null and `ownerKey` 0 for the former global tables), then drops the eight old tables (`dock_apps`, `facet_dock_apps`, `favorite_apps`, `default_favorite_apps` and the four `*_folder_placements`). Tested by `FacetDatabaseMigrationTest.migration27To28…` (seeded rows in all eight tables, work-profile `userId`, orphan `-1`, facet cascade, old tables gone) |
| 28→29 | `landscape_order` for the landscape Hub: `widget_placements.landscapeOrder INTEGER` (nullable, no default). Existing rows stay `NULL`, which makes landscape follow portrait reading order, so nothing is rewritten. `FacetDatabaseMigrationTest.migration28To29…` seeds two widgets and checks both survive with a `NULL` order. |
| 29→30 | A hosted clock widget's own landscape size: `facets.landscapeClockWidgetWidthDp`, `landscapeClockWidgetHeightDp` (nullable). `NULL` makes landscape use the portrait size (`clockWidgetWidthDp`/`HeightDp`), clamped to its pane on screen, so nothing is rewritten. Binding another widget clears both pairs. Not in the backup (widget ids aren't portable). `FacetDatabaseMigrationTest.migration29To30…` checks the portrait size survives and the new pair is `NULL`. |
| 30→31 | Landscape's own clock placement: `facets.landscapeClockAlignment` (TEXT), `landscapeClockZoneHeightDp`, `landscapeClockScale` (REAL), all nullable. `NULL` uses the facet's portrait value, so nothing is rewritten. "Reset clock position" clears them. Not in `BackupFacet` (the same gap as the portrait clock placement). `FacetDatabaseMigrationTest.migration30To31…` checks the portrait placement survives and the new columns are `NULL`. |
| 31→32 | Landscape's own list alignment: `facets.landscapeAppRowPosition`, `landscapeAppListVerticalAlignment`, `landscapeAppListColumnAlignment` (nullable TEXT). `NULL` is "Same as portrait": a facet's landscape value, else its explicit portrait value, else the launcher's landscape value, else its portrait value (`resolveLandscapeSentinel`). Not in `BackupFacet`. `FacetDatabaseMigrationTest.migration31To32…` checks the portrait alignment survives and the new columns are `NULL`. |
| 32→33 | The clock accessory switches and the weather cache: `facets.clockShowAlarm` and `clockShowBattery` (`INTEGER NOT NULL DEFAULT 1`) and `clockShowWeather` (`DEFAULT 0`), so every facet looks as it did, and the new tables `weather_hours` and `weather_meta` (DDL from the exported `33.json`, empty). The switches follow `overrideClock`; the facet fields are in `BackupFacet`, the cache is not backed up. `FacetDatabaseMigrationTest.migration32To33…` checks the three defaults and that both tables exist and are empty. |
| 33→34 | `facets.clockWeatherIconStyle` (`TEXT NOT NULL DEFAULT 'COLORED'`): the per-facet weather picture style, used while the facet overrides the clock (`BackupFacet.clockWeatherIconStyle`). `FacetDatabaseMigrationTest.migration33To34…` runs 32→33→34 and checks the default. |
| 34→35 | New table `clock_stack_items` (DDL from the exported `35.json`, index on `facetId, position`) and every facet gets its first page at position 0: a facet with a hosted clock widget gets that `WIDGET` row (size, alignment and landscape twins carried over), every other facet gets a `CLOCK` row. An empty stack draws nothing, so no facet may be left without a row; `FacetRepository.addFacet` and `restoreFacet` insert the `CLOCK` row too. The `facets.clockWidget*` columns stay (SQLite on minSdk 30 cannot drop a column) but are retired: `FacetMapping` neither reads nor writes them, and `Facet` has no such fields. `FacetDatabaseMigrationTest.migration34To35…` runs 32→33→34→35 and checks the moved widget row, that a clock-only facet gets a `CLOCK` row, and that no facet is left without one. |
| 35→36 | `facets.clockPlacementOwn` (`INTEGER NOT NULL DEFAULT 0`), then `UPDATE facets SET clockPlacementOwn = overrideClock`: a facet that overrode the clock already owned its placement, the others keep showing the launcher fallback until first adjusted on Home. `BackupFacet.clockPlacementOwn` is nullable: a backup from before it reads as `overrideClock`. `FacetDatabaseMigrationTest.migration35To36…` runs 32→…→36 and checks both facets. |
| 36→37 | `facets.appListShrinkToFit` (`TEXT NOT NULL DEFAULT 'LAUNCHER_DEFAULT'`): Home's "Shrink list to fit", per facet; every existing facet follows the launcher-wide value (`app_list_shrink_to_fit`, off by default). `FacetDatabaseMigrationTest.migration36To37…` runs 32→…→37 and checks the default. |
| 37→38 | `widget_placements.nativeWidgetType` and `clock_stack_items.nativeWidgetType` (both `TEXT`, nullable, no default): the widgets Facet draws itself ([20](20-flow-native-widgets.md)). Every existing row stays `NULL`, a hosted widget or the clock. `FacetDatabaseMigrationTest.migration37To38…` seeds a placement and checks it survives with a `NULL` type. |
| 38→39 | `widget_placements.nativeWidgetConfig` (`TEXT`, nullable, no default): what one Hub Weather or Agenda widget is set to instead of the launcher-wide setting ([20 §3a](20-flow-native-widgets.md)). Every existing row stays `NULL`, which follows the launcher. `FacetDatabaseMigrationTest.migration38To39…` seeds a native placement and checks it survives with a `NULL` config. |
| 39→40 | `clock_stack_items.nativeWidgetConfig` (`TEXT`, nullable, no default): the same per-widget setting for a native widget page on Home's clock stack ([20 §3a](20-flow-native-widgets.md)). Existing rows stay `NULL`. `FacetDatabaseMigrationTest.migration39To40…` seeds a native page and checks it survives with a `NULL` config. |
| 40→41 | `facets.clockWeatherLocation` (`TEXT`, nullable, no default): where the facet's clock shows the weather for, in the widget-location encoding (`encodeWeatherLocation`), used while the facet overrides the clock; `NULL` follows Weather settings. Existing rows stay `NULL`. `FacetDatabaseMigrationTest.migration40To41…` checks a facet survives with a `NULL` location |
| 41→42 | No schema change. A widget's setting (`widget_placements.nativeWidgetConfig`, `clock_stack_items.nativeWidgetConfig`) and a facet's clock weather location (`facets.clockWeatherLocation`) are rewritten from the tab-separated lines to JSON (`NativeWidgetConfig.encode`), through `upgradeLegacyNativeWidgetConfig` (`LegacyNativeWidgetConfig.kt`), which only this migration and the importer of an older backup read. `FacetDatabaseMigrationTest.migration41To42…` seeds old-form values and reads them back |
| 42→43 | No schema change. `Migrations.migration42To43` (`materializeClockConfigs`, `ClockConfigMigration.kt`) gives every `clock_stack_items` CLOCK row without a clock config the settings it showed: the facet's own `facets` clock and calendar columns while it overrides the clock or the calendar, the launcher-wide values otherwise. The launcher-wide values come from the settings DataStore, which a migration cannot reach, so `DatabaseModule` passes a reader (`Preferences.legacyLauncherClock`, the one place the retired keys are still read) that runs only when this step does. A page that already has a config is left alone. `FacetDatabaseMigrationTest.migration42To43GivesEachClockPage…`, `…LeavesAClockPageThatAlreadyHasSettingsAlone`, `LegacyLauncherClockTest`. |
| 43→44 | **Auto-migration** (`@AutoMigration(from = 43, to = 44, spec = DropFacetClockColumns)`): `facets` loses `overrideClock`, `clockTemplateId`, `clockFontOption`, `clockColorOption`, `clockAccentColorOption`, `use24HourTime`, `clockShowMeridiem`, `clockDateStyle`, `clockShowAlarm`, `clockShowBattery`, `clockShowWeather`, `clockWeatherIconStyle`, `clockWeatherLocation`, `overrideCalendar`, `showAllDayEvents`, `showCalendarIndicator`, `calendarItemsToShow`, `selectedCalendarIdsCsv`, the five retired hosted-clock columns and `clockScale` / `landscapeClockScale`. The clock settings are not lost: 42→43 has already copied them onto each clock page, and the two steps run in one upgrade, so a jump from any older version is safe. The scale is: a clock's size is the box on its page (`clock_stack_items.widthDp`/`heightDp`), which cannot be worked out in a migration (it needs the drawn size of the clock's template and font), so a clock that was never resized goes back to the default size (`DEFAULT_CLOCK_SCALE`, 0.8). The launcher-wide `clock_scale` / `landscape_clock_scale` DataStore keys are no longer read or written. Room rebuilds the table because SQLite on minSdk 30 cannot drop a column. `FacetDatabaseMigrationTest.migration43To44DropsTheFacetClockColumnsAndKeepsEverythingElse`. |
| 44→45 | No schema change. `Migrations.migration44To45` (`materializeFacetDefaults`, `FacetDefaultsMigration.kt`) writes today's launcher-wide values into every facet that inherited them, so the flags can go without anything changing on screen. For each facet with `overrideDock = 0` / `overridingFavorites = 0` it deletes the facet's stale own Dock / Favorites rows and copies the launcher-wide `app_placements` and `folder_placements` rows under its id; with `overrideApps = 0` it sets `listContentMode` and `appsToShowCount` from the settings; every `LAUNCHER_DEFAULT` look column becomes the launcher's value. A null landscape twin on a facet whose portrait value was inherited takes the launcher's landscape value, if it has one, since that is what it showed. A facet that overrode a list or the content is untouched. The launcher-wide values come from the settings DataStore, which a migration cannot reach, so `DatabaseModule` passes a reader (`SettingsRepository(...).settings.first()`) that runs only when this step does. `FacetDatabaseMigrationTest.migration44To45GivesInheritingFacets…`, `…LeavesAnOverridingFacetAndAnExplicitLandscapeValueAlone`, `…HandlesAnEmptyLauncherDock`. |
| 45→46 | **Auto-migration** (`@AutoMigration(from = 45, to = 46, spec = DropFacetOverrideFlags)`): `facets` loses `overrideApps`, `overridingFavorites` and `overrideDock`. Nothing is lost: 44→45 has already copied the launcher's values into every facet that inherited them. `FacetDatabaseMigrationTest.migration45To46DropsTheOverrideFlagsAndKeepsTheFacetsOwnValues`. |
| 46→47 | **Auto-migration** (`@AutoMigration(from = 46, to = 47)`): `app_placements` and `folder_placements` each gain four nullable columns, `portraitRow`, `portraitColumn`, `landscapeRow` and `landscapeColumn` (a Home grid cell counted from the anchor edge). All null until an item is first moved on a grid, when its place still follows `position`. Nothing reads them yet. `FacetDatabaseMigrationTest.migration46To47AddsEmptyHomeCellsAndKeepsEveryPlacement`. |

The landscape twins are handled as one `LandscapeOverrides` group (clock alignment, divider, scale, widget size, row position, vertical and column alignment; every field nullable). Settings hold it as `LauncherSettings.landscape`, read from the six `landscape_*` keys and written only through `updateLandscape`. A facet keeps the flat columns above (so the schema is unchanged) and exposes the group as `FacetEntity.landscape` / `withLandscape(...)`, written through `FacetRepository.updateLandscape`. `resolveLandscape` is the one resolution rule (see [17](17-flow-home-layout.md) §1.0).

## 5. DataStore — `facet_settings` (`SettingsRepository`)

The second store. One `DataStore<Preferences>`, provided by `DataStoreModule`
(`preferencesDataStore(name = "facet_settings")` → `files/datastore/facet_settings.preferences_pb`),
read and written **only** by `SettingsRepository`. It is a flat key–value file: no schema version,
no migrations — evolution is "add a key with a default", and removed keys are simply ignored.

### 5.1 Key diagram — every key, and which seed a facet

The diagram uses ER notation so it reads alongside §1. `facet_settings` is one logical record
(there is exactly one). The app-list and dock keys are *defaults*: `ApplyFacetDefaultsUseCase` copies them into
the `facets` column of the same name when a facet is created or reset, and the facet owns the copy from then on.

```mermaid
erDiagram
    facet_settings {
        boolean use_24_hour_time "default false"
        string clock_alignment "ClockAlignment, default LEFT; also positions the calendar events strip"
        float clock_zone_height_dp "nullable, absent = template height"
        boolean allow_landscape "default true; off = portrait only"
        boolean hide_status_bar "default false; hides the status bar over Home, Hub and Drawer"
        string landscape_clock_alignment "nullable, absent = use clock_alignment"
        float landscape_clock_zone_height_dp "nullable, absent = use clock_zone_height_dp"
        boolean show_all_day_events "default true"
        stringset selected_calendar_ids "nullable, absent = all calendars"
        stringset calendar_colors "entries calendarId:colorName"
        string list_content_mode "ListContentMode, default FAVORITES"
        int apps_to_show_count "default AppListLimits.DEFAULT_APPS_TO_SHOW"
        string app_row_position "AppRowPosition, default LEFT"
        string landscape_app_row_position "nullable, absent = same as portrait"
        string landscape_app_list_vertical_alignment "nullable, absent = same as portrait"
        string landscape_app_list_column_alignment "nullable, absent = same as portrait"
        string app_row_presentation "AppRowPresentation, default ICON_AND_TEXT"
        string app_list_vertical_alignment "AppListVerticalAlignment, default BOTTOM"
        string home_apps_font_weight "FontWeightOption, default REGULAR"
        string app_label_color_option "ClockColorOption, default THEME"
        string dock_display_mode "DockDisplayMode, default ICONS"
        long active_facet_id "default 0 = NO_ACTIVE_FACET_ID"
        string theme_mode "ThemeMode, default SYSTEM"
        string system_bar_icon_style "SystemBarIconStyle, default MATCH_THEME"
        boolean accent_from_system "default true"
        string custom_accent_swatch "AccentSwatch name, nullable"
        string wallpaper_accent_role "WallpaperAccentRole, default PRIMARY"
        string icon_render_mode "IconRenderMode, default SYSTEM_DEFAULT"
        string icon_shape "IconShape, default SQUIRCLE"
        int icon_scale_percent "Icon size, 80..120, default 100"
        string icon_pack_package "installed icon pack, absent = built-in styles"
        string icon_pack_fallback "IconPackFallback, default MATCH_PACK"
        bool force_icon_shape "clip the pack's icons to icon_shape, default false"
        string launcher_font_option "LauncherFontOption, default SYSTEM"
        string font_scale_option "FontScaleOption, default DEFAULT"
        string drawer_presentation "DrawerPresentation, default LIST"
        string drawer_grid_size "DrawerGridDensity, default COMPACT (legacy FIVE_BY_SIX etc. still read)"
        string drawer_list_item_size "DrawerListItemSize, default REGULAR"
        float drawer_opacity "default 0.6"
        boolean show_drawer_icons "default true"
        boolean show_drawer_labels "default true"
        string search_bar_position "SearchBarPosition, default TOP"
        string drawer_folder_display_mode "DrawerFolderDisplayMode, default DO_NOT_SHOW"
        string recently_installed_position "RecentlyInstalledPosition, default SHOW_FIRST"
        boolean search_contacts_enabled "default false"
        boolean search_settings_enabled "default false"
        boolean notification_dots_enabled "default true"
        boolean home_opens_search "default false"
        boolean double_tap_to_lock "default false"
        boolean swipe_left_enabled "default true"
        boolean swipe_right_enabled "default true"
        boolean swipe_down_enabled "default true"
        boolean swap_swipe_sides "default false"
        string notification_badge_style "NotificationBadgeStyle, default DOT"
        boolean show_media_player_on_home "default false"
        boolean show_media_skip_buttons "default true"
        boolean show_notifications_in_app_menu "default true"
        boolean calendar_permission_requested "default false"
        boolean contacts_permission_requested "default false"
        boolean bluetooth_permission_requested "default false"
        boolean location_permission_requested "default false"
        boolean onboarding_completed "default false"
        stringset coach_marks_seen "CoachMarkIds, default empty"
    }

    facets {
        long id PK
    }

    facet_settings ||--o{ facets : "active_facet_id selects one"
```

Keys with **no** facet counterpart (always global): `home_apps_font_weight`,
`app_label_color_option`, `launcher_font_option` (all three of which the calendar events strip
reads directly — see §0), `calendar_colors`, everything under Theme, Drawer, Search,
Notifications, Permission bookkeeping, and First-run.

**The Home look keys follow the same rule.** `appRowPosition`/`appRowPresentation`/`appListVerticalAlignment`/
`appListLayout`/`appListColumnAlignment`/`appListGridColumns`/`appListGridDisplayMode`/
`appListShrinkToFit` and the three landscape twins are edited on the Apps list screen (Settings → Default Apps list,
and each facet's own copy of the screen). The launcher-wide values only seed a new or reset facet
(`ApplyFacetDefaultsUseCase`, scope `LOOK`, run with `APPS_LIST` by the Apps list screen's reset and copy); a facet's value is always its own, with no `LAUNCHER_DEFAULT`
marker. `dockDisplayMode` is the same kind of default but is edited on the Dock screen and copied with the dock (scope `DOCK`). A facet's landscape twin is its own too, `null` meaning "same as portrait".

### 5.2 Read path

```kotlin
val settings: Flow<LauncherSettings> = dataStore.data.map { preferences ->
    val defaults = LauncherSettings()
    LauncherSettings(
        use24HourTime = preferences[Keys.USE_24_HOUR_TIME] ?: defaults.use24HourTime,
        clockTemplateId = preferences[Keys.CLOCK_TEMPLATE_ID]
            ?.let { runCatching { ClockTemplateId.valueOf(it) }.getOrNull() } ?: defaults.clockTemplateId,
        calendarColors = preferences[Keys.CALENDAR_COLORS].orEmpty()
            .mapNotNull { it.split(":", limit = 2).takeIf { p -> p.size == 2 }?.let { p -> p[0] to p[1] } }.toMap(),
        // ... one line per key, always `?: default`
    )
}
```

- One `Flow<LauncherSettings>`; every consumer (`ObserveHomeScreenStateUseCase`,
  `LauncherViewModel`, 20 ViewModels, 11 use cases) `combine`s or `.first()`s it. There is no
  per-key flow.
- Every emission is the **whole** immutable `LauncherSettings` (50 fields); DataStore emits on any
  key change, so a coach-mark write re-emits theme/clock/drawer settings too — consumers rely on
  `combine`/`distinctUntilChanged` in their own graphs to avoid recomposing.
- Defaults come from `LauncherSettings()`'s constructor defaults, which are the single source of
  truth (the table in 5.4 is transcribed from them).
- Enum keys are stored as `Enum.name` strings and parsed leniently — a renamed constant degrades to
  the default instead of crashing, mirroring Room's `Converters`.
- Nullable fields (`selectedCalendarIds`, `customAccentSwatch`, `clockZoneHeightDp`) have no
  default: an absent key **means** null, and null is meaningful ("all calendars", "no custom
  swatch", "template's natural height").

### 5.3 Write path — the complete writer API

Every writer is `suspend`, wraps a single `dataStore.edit { }` and touches exactly one key.
DataStore serialises writes and is main-safe; callers `viewModelScope.launch { }` them.

| Writer | Key | Op | Called from |
|---|---|---|---|
| `setUse24HourTime(Boolean)` | `use_24_hour_time` | set | `ClockStyleGalleryViewModel` |
| `setTemperatureUnit`, `setWindSpeedUnit`, `setRainUnit`, `setPressureUnit` | `temperature_unit`, `wind_speed_unit`, `rain_unit`, `pressure_unit` | set | `UnitsAndLocationViewModel` |
| `setWeatherIconStyle(WeatherIconStyle)` | `weather_icon_style` | set | `ClockAccessoriesViewModel` (default screen; a facet uses `FacetRepository.setClockWeatherIconStyle`) |
| `setWeatherLocationMode(WeatherLocationMode)` | `weather_location_mode` | set | none yet |
| `setWeatherCity(WeatherCity?)` | `weather_city_name`, `weather_city_latitude`, `weather_city_longitude`, `weather_city_timezone` | set the four together, or remove all four on `null` | none yet |
| `chooseWeatherCity(WeatherCity): Boolean` | `weather_saved_cities`, the four city keys, `weather_location_mode` | add the city to the saved list unless already there (refused, returning false, past 10), make it the chosen city and switch to city mode | `WeatherLocationViewModel` |
| `removeWeatherCity(WeatherCity)` | `weather_saved_cities`, the four city keys | drop it from the list; clear the four keys when it was the chosen one | `WeatherLocationViewModel` |
| `setSavedWeatherCities(List<WeatherCity>)` | `weather_saved_cities` | replace the list (restore), at most 10 | `ImportBackupUseCase` |
| `setAllowLandscape(Boolean)` | `allow_landscape` | set | `HomeAppsListSettingsViewModel` |
| `setHideStatusBar(Boolean)` | `hide_status_bar` | set | `AppearanceSettingsViewModel` |
| `setClockAlignment(ClockAlignment)` | `clock_alignment` | set | `ClockStyleGalleryViewModel`, `HomeViewModel` (clock adjust toolbar) |
| `setShowWeather(Boolean)` | `show_weather` | set | `TurnOffWeatherUseCase`, weather settings |
| `setClockZoneHeight(Float)` | `clock_zone_height_dp` | set | `HomeViewModel` (zone drag handle) |
| `resetClockZoneHeight()` | `clock_zone_height_dp` and `landscape_clock_zone_height_dp` | **remove** | `ClockStyleGalleryViewModel` |
| `updateLandscape((LandscapeOverrides) -> LandscapeOverrides)` | all six `landscape_*` keys | set each field, or **remove** it when `null` | `HomeViewModel` (landscape adjust commits), `HomeAppsListSettingsViewModel` (Landscape alignment rows), `ClockStyleGalleryViewModel` (Reset position), `ImportBackupUseCase` |
| `setShowAllDayEvents(Boolean)` | `show_all_day_events` | set | `CalendarSettingsViewModel` |
| `setSelectedCalendarIds(Set<String>)` | `selected_calendar_ids` | set | `CalendarSettingsViewModel` |
| `setCalendarColors(Map<String,String>)` | `calendar_colors` | set (encoded `id:color`) | `EnsureCalendarColorsUseCase` (app-wide, on every calendar change), `CalendarSettingsViewModel` after `AssignCalendarColorsUseCase` |
| `setListContentMode(ListContentMode)` | `list_content_mode` | set | `HomeAppsListSettingsViewModel`, `OnboardingViewModel` |
| `setAppsToShowCount(Int)` | `apps_to_show_count` | set | `HomeAppsListSettingsViewModel`, `OnboardingViewModel` |
| `setAppRowPosition(AppRowPosition)` | `app_row_position` | set | `HomeAppsListSettingsViewModel` |
| `setAppRowPresentation(AppRowPresentation)` | `app_row_presentation` | set | `HomeAppsListSettingsViewModel` |
| `setAppListVerticalAlignment(AppListVerticalAlignment)` | `app_list_vertical_alignment` | set | `HomeAppsListSettingsViewModel` |
| `setAppListLayout(AppListLayout)` | `app_list_layout` | set | `HomeAppsListSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListColumnAlignment(AppListColumnAlignment)` | `app_list_column_alignment` | set | `HomeAppsListSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListGridColumns(AppListGridColumns)` | `app_list_grid_columns` | set | `HomeAppsListSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListGridDisplayMode(AppListGridDisplayMode)` | `app_list_grid_display_mode` | set | `HomeAppsListSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListShrinkToFit(AppListShrinkToFit)` | `app_list_shrink_to_fit` | set | `HomeAppsListSettingsViewModel`, `ImportBackupUseCase` |
| `setHomeAppsFontWeight(FontWeightOption)` | `home_apps_font_weight` | set | `AppearanceSettingsViewModel` |
| `setAppLabelColorOption(ClockColorOption)` | `app_label_color_option` | set | `AppearanceSettingsViewModel` |
| `setDockDisplayMode(DockDisplayMode)` | `dock_display_mode` | set | `DockSettingsViewModel` |
| `setActiveFacetId(Long)` | `active_facet_id` | set | `FacetCarouselViewModel`, `ManageFacetsViewModel`, `EnsureActiveFacetUseCase`, `ActivateFacetByIdUseCase`, `ImportBackupUseCase` |
| `setThemeMode(ThemeMode)` | `theme_mode` | set | `AppearanceSettingsViewModel` |
| `setSystemBarIconStyle(SystemBarIconStyle)` | `system_bar_icon_style` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setAccentFromSystem(Boolean)` | `accent_from_system` | set | `AppearanceSettingsViewModel` |
| `setCustomAccentSwatch(String)` | `custom_accent_swatch` | set | `AppearanceSettingsViewModel` |
| `setWallpaperAccentRole(WallpaperAccentRole)` | `wallpaper_accent_role` | set | `AppearanceSettingsViewModel` |
| `setIconRenderMode(IconRenderMode)` | `icon_render_mode` | set | `AppearanceSettingsViewModel`, `IconsViewModel` |
| `setIconShape(IconShape)` | `icon_shape` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setIconScalePercent(Int)` | `icon_scale_percent` | set (clamped 80..120) | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setIconPack(String?)` | `icon_pack_package` | set / remove (null) | `IconsViewModel`, `IconPackRepository` (clears an uninstalled pack), `ImportBackupUseCase` |
| `setIconPackFallback(IconPackFallback)` | `icon_pack_fallback` | set | `IconsViewModel`, `ImportBackupUseCase` |
| `setForceIconShape(Boolean)` | `force_icon_shape` | set | `IconsViewModel`, `ImportBackupUseCase` |
| `setLauncherFontOption(LauncherFontOption)` | `launcher_font_option` | set | `AppearanceSettingsViewModel` |
| `setFontScaleOption(FontScaleOption)` | `font_scale_option` | set | `AppearanceSettingsViewModel` |
| `setDrawerPresentation(DrawerPresentation)` | `drawer_presentation` | set | `AppDrawerSettingsViewModel`, `OnboardingViewModel` |
| `setDrawerGridDensity(DrawerGridDensity)` | `drawer_grid_size` | set | `AppDrawerSettingsViewModel` |
| `setDrawerListItemSize(DrawerListItemSize)` | `drawer_list_item_size` | set | `AppDrawerSettingsViewModel` |
| `setDrawerOpacity(Float)` | `drawer_opacity` | set | `AppDrawerSettingsViewModel` |
| `setShowDrawerIcons(Boolean)` | `show_drawer_icons` | set | `AppDrawerSettingsViewModel` |
| `setShowDrawerLabels(Boolean)` | `show_drawer_labels` | set | `AppDrawerSettingsViewModel` |
| `setSearchBarPosition(SearchBarPosition)` | `search_bar_position` | set | `AppDrawerSettingsViewModel` |
| `setDrawerFolderDisplayMode(DrawerFolderDisplayMode)` | `drawer_folder_display_mode` | set | `AppDrawerSettingsViewModel` |
| `setRecentlyInstalledPosition(RecentlyInstalledPosition)` | `recently_installed_position` | set | `AppDrawerSettingsViewModel` |
| `setSearchContactsEnabled(Boolean)` | `search_contacts_enabled` | set | `AppDrawerSettingsViewModel`, `DrawerViewModel` (inline prompt) |
| `setSearchSettingsEnabled(Boolean)` | `search_settings_enabled` | set | `AppDrawerSettingsViewModel` |
| `setNotificationDotsEnabled(Boolean)` | `notification_dots_enabled` | set | `NotificationSettingsViewModel`, `NotificationAccessExplanationViewModel` |
| `setHomeOpensSearch(Boolean)` | `home_opens_search` | set | `GesturesViewModel` |
| `setDoubleTapToLock(Boolean)` | `double_tap_to_lock` | set | `GesturesViewModel` |
| `setSwipeLeftEnabled(Boolean)` | `swipe_left_enabled` | set | `GesturesViewModel` |
| `setSwipeRightEnabled(Boolean)` | `swipe_right_enabled` | set | `GesturesViewModel` |
| `setSwipeDownEnabled(Boolean)` | `swipe_down_enabled` | set | `GesturesViewModel` |
| `setSwapSwipeSides(Boolean)` | `swap_swipe_sides` | set | `GesturesViewModel` |
| `setNotificationBadgeStyle(NotificationBadgeStyle)` | `notification_badge_style` | set | `NotificationSettingsViewModel` |
| `setShowMediaPlayerOnHome(Boolean)` | `show_media_player_on_home` | set | `NotificationSettingsViewModel` |
| `setShowMediaSkipButtons(Boolean)` | `show_media_skip_buttons` | set | `NotificationSettingsViewModel` |
| `setShowNotificationsInAppMenu(Boolean)` | `show_notifications_in_app_menu` | set | `NotificationSettingsViewModel` |
| `setCalendarPermissionRequested(Boolean)` | `calendar_permission_requested` | set | `PermissionsViewModel` |
| `setContactsPermissionRequested(Boolean)` | `contacts_permission_requested` | set | `PermissionsViewModel` |
| `setBluetoothPermissionRequested(Boolean)` | `bluetooth_permission_requested` | set | `PermissionsViewModel`, `RuleEditorViewModel` |
| `setLocationPermissionRequested(Boolean)` | `location_permission_requested` | set | `PermissionsViewModel`, `RuleEditorViewModel` |
| `setOnboardingCompleted(Boolean)` | `onboarding_completed` | set | `LauncherViewModel.completeOnboarding()` |
| `markCoachMarkSeen(String)` | `coach_marks_seen` | set (union with existing) | `HomeViewModel` |

`ImportBackupUseCase.applySettings()` restores settings by calling these same setters one at a time (42 calls, plus `setActiveFacetId` after facets are re-inserted) —
there is no bulk "replace all preferences" entry point, so a restore is not atomic at the
DataStore level either.

### 5.4 Key registry by section

The same 73 keys, grouped the way Settings screens present them, with the `LauncherSettings` field
each maps to. Defaults are `LauncherSettings()`'s constructor defaults.

#### Clock (a clock holds its own style, accessories and calendar in `ClockWidgetConfig`; only these launcher-wide keys remain)

The placement keys here (`clock_alignment`, `clock_zone_height_dp` and the landscape twins) are no longer
edited anywhere: they are the launcher's built-in fallback for a facet that has not adjusted its own placement
(`facets.clockPlacementOwn` false). The first adjustment on Home copies what the facet shows into the facet.

The calendar events strip has no design keys of its own here any more — it reads the global-only
`home_apps_font_weight`/`app_label_color_option` ("Home app list", below) and `launcher_font_option`
("Theme & appearance", below) keys for font/weight/color, and `clock_alignment` in this section for
its position (see chat history: calendar/appearance styling consolidation).

| Key | Type | `LauncherSettings` field | Default | Notes |
|---|---|---|---|---|
| `use_24_hour_time` | Boolean | `use24HourTime` | `false` | |
| `show_weather` | Boolean | `showWeather` | `false` | likewise `clockShowWeather`; while off nothing uses the network, location or geocoder |
| `temperature_unit` | String (`TemperatureUnit`) | `temperatureUnit` | `AUTO` | launcher-wide, never per facet; an unknown name reads as `AUTO`. Same for the next three |
| `wind_speed_unit` | String (`WindSpeedUnit`) | `windSpeedUnit` | `AUTO` | |
| `rain_unit` | String (`RainUnit`) | `rainUnit` | `AUTO` | |
| `pressure_unit` | String (`PressureUnit`) | `pressureUnit` | `AUTO` | |
| `weather_location_mode` | String (`WeatherLocationMode`) | `weatherLocationMode` | `CURRENT` | launcher-wide |
| `weather_city_name`, `weather_city_latitude` (Double), `weather_city_longitude` (Double), `weather_city_timezone` | String / Double | `weatherCity` | `null` | the chosen city; it reads as `null` unless all four keys are present |
| `weather_saved_cities` | String | `savedWeatherCities` | empty | the cities kept for switching, one per line with tab-separated name, latitude, longitude and timezone; the chosen city is added to the read list when an older install stored it without a list |
| `weather_icon_style` | String | `weatherIconStyle` | `COLORED` | `MONOCHROME` or `COLORED` weather pictures; the launcher default, a facet overriding the clock uses its own `clockWeatherIconStyle` |
| `clock_alignment` | String (`ClockAlignment`) | `clockAlignment` | `LEFT` | also positions the calendar events strip |
| `clock_zone_height_dp` | Float | `clockZoneHeightDp` | `null` (= template's natural height) | nullable — `resetClockZoneHeight()` removes the key |
| `allow_landscape` | Boolean | `allowLandscape` | `true` | launcher-wide: off keeps Home in portrait, locks the screen to portrait and hides the landscape settings |
| `hide_status_bar` | Boolean | `hideStatusBar` | `false` | launcher-wide: hides the status bar over Home, Hub and Drawer; a swipe-down shows it for 3 s ([13](13-flow-facets-theme-notifications-onboarding.md) §2a) |
| `landscape_clock_alignment` | String (`ClockAlignment`) | `landscapeClockAlignment` | `null` (= `clock_alignment`) | landscape's own placement, written from Home's adjust mode in landscape |
| `landscape_clock_zone_height_dp` | Float | `landscapeClockZoneHeightDp` | `null` (= `clock_zone_height_dp`) | landscape's divider position (the clock pane width) |

#### Calendar selection (global; the Agenda widget and the clock keep their own)

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `show_all_day_events` | Boolean | `showAllDayEvents` | `true` | |
| `selected_calendar_ids` | Set<String> | `selectedCalendarIds` | `null` (= all calendars) | nullable — absent key means "every calendar" |
| `calendar_colors` | Set<String> encoded `"<calendarId>:<colorName>"` | `calendarColors: Map<String,String>` | `{}` | Assigned by `AssignCalendarColorsUseCase`; split at the last `:` on read (a Work Profile id is `work:<n>`), entries without one are dropped; not facet-overridable |

#### Home app list (launcher defaults: they seed a new or reset facet)

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `list_content_mode` | String (`ListContentMode`) | `listContentMode` | `FAVORITES` | `FAVORITES` / `RECENTS` / `MOST_USED` |
| `apps_to_show_count` | Int | `appsToShowCount` | `AppListLimits.DEFAULT_APPS_TO_SHOW` | |
| `app_row_position` | String (`AppRowPosition`) | `appRowPosition` | `LEFT` | |
| `landscape_app_row_position` | String (`AppRowPosition`) | `landscapeAppRowPosition` | `null` (= `app_row_position`) | landscape's own; the key is removed for "Same as portrait" |
| `landscape_app_list_vertical_alignment` | String (`AppListVerticalAlignment`) | `landscapeAppListVerticalAlignment` | `null` (= `app_list_vertical_alignment`) | same |
| `landscape_app_list_column_alignment` | String (`AppListColumnAlignment`) | `landscapeAppListColumnAlignment` | `null` (= `app_list_column_alignment`) | two-column only; same |
| `app_row_presentation` | String (`AppRowPresentation`) | `appRowPresentation` | `ICON_AND_TEXT` | |
| `app_list_vertical_alignment` | String (`AppListVerticalAlignment`) | `appListVerticalAlignment` | `BOTTOM` | |
| `app_list_layout` | String (`AppListLayout`) | `appListLayout` | `SINGLE_COLUMN` | single column / two column / grid |
| `app_list_column_alignment` | String (`AppListColumnAlignment`) | `appListColumnAlignment` | `BOTH_LEFT` | two-column only |
| `app_list_grid_columns` | String (`AppListGridColumns`) | `appListGridColumns` | `FOUR` | grid only — 3/4/5/6 |
| `app_list_grid_display_mode` | String (`AppListGridDisplayMode`) | `appListGridDisplayMode` | `ICONS` | grid only — icons, text, or icons and text |
| `app_list_shrink_to_fit` | String (`AppListShrinkToFit`) | `appListShrinkToFit` | `OFF` | list layouts only — Compact rows when Regular ones wouldn't fit; off, the list scrolls; facet-overridable (`facets.appListShrinkToFit`) |
| `home_apps_font_weight` | String (`FontWeightOption`) | `homeAppsFontWeight` | `REGULAR` | not facet-overridable; also styles the calendar events strip |
| `app_label_color_option` | String (`ClockColorOption`) | `appLabelColorOption` | `THEME` | not facet-overridable; also styles the calendar events strip |

#### Dock (launcher default: seeds a new or reset facet)

| Key | Type | Field | Default |
|---|---|---|---|
| `dock_display_mode` | String (`DockDisplayMode`) | `dockDisplayMode` | `ICONS` |

#### Facets

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `active_facet_id` | Long | `activeFacetId` | `NO_ACTIVE_FACET_ID` (`0L`) | Reconciled against the `facets` table by `EnsureActiveFacetUseCase` at startup; backup stores it as an *index* (`activeFacetIndex`) because ids change on restore |

#### Theme & appearance

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `theme_mode` | String (`ThemeMode`) | `themeMode` | `SYSTEM` | |
| `system_bar_icon_style` | String (`SystemBarIconStyle`) | `systemBarIconStyle` | `MATCH_THEME` | status + 3-button nav icon color over the wallpaper; global, not facet-overridable; see 13 §2 |
| `accent_from_system` | Boolean | `accentFromSystem` | `true` | Material You / wallpaper accent when true |
| `custom_accent_swatch` | String (`AccentSwatch.name`) | `customAccentSwatch` | `null` | nullable; parsed to enum in `LauncherActivity` |
| `wallpaper_accent_role` | String (`WallpaperAccentRole`) | `wallpaperAccentRole` | `PRIMARY` | only meaningful when `accent_from_system` |
| `icon_render_mode` | String (`IconRenderMode`) | `iconRenderMode` | `SYSTEM_DEFAULT` | |
| `icon_shape` | String (`IconShape`) | `iconShape` | `SQUIRCLE` | icon outline (Squircle n=4 / Rounded n=2.6 / Circle / Square); global, not facet-overridable; backed up |
| `icon_scale_percent` | Int | `iconScalePercent` | `100` | Icon size, 80..120 (clamped on read and write); scales every app icon outside settings (`LocalIconScale`); global; backed up |
| `icon_pack_package` | String | `iconPackPackage` | `null` (absent) | installed icon pack applied launcher-wide; while set, `effectiveIconRenderMode` is `SYSTEM_DEFAULT` and `icon_render_mode` is kept for when it's cleared; cleared by `IconPackRepository.loadApplied` when the pack isn't installed; backed up |
| `icon_pack_fallback` | String (`IconPackFallback`) | `iconPackFallback` | `MATCH_PACK` | apps the pack has no icon for: the pack's back/mask/upon art, or their own icon; backed up |
| `force_icon_shape` | Boolean | `forceIconShape` | `false` | clips the pack's own icons to `icon_shape` as well (`IconOutline.clipsPackIcons`, carried by `LocalIconShape`); only applies while a pack is set; backed up |
| `launcher_font_option` | String (`LauncherFontOption`) | `launcherFontOption` | `SYSTEM` | also styles the calendar events strip |
| `font_scale_option` | String (`FontScaleOption`) | `fontScaleOption` | `DEFAULT` | multiplies every `MaterialTheme.typography` role's `fontSize`/`lineHeight` app-wide except the clock |

#### App drawer

| Key | Type | Field | Default |
|---|---|---|---|
| `drawer_presentation` | String (`DrawerPresentation`) | `drawerPresentation` | `LIST` |
| `drawer_grid_size` | String (`DrawerGridDensity`) | `drawerGridDensity` | `COMPACT` |
| `drawer_list_item_size` | String (`DrawerListItemSize`) | `drawerListItemSize` | `REGULAR` | a row is icon + padding each side: Compact 36 + 4, Regular 44 + 12, Spacious 44 + 16 (icons × Icon size); also sizes Private Space's rows |
| `drawer_opacity` | Float | `drawerOpacity` | `0.6` |
| `show_drawer_icons` | Boolean | `showDrawerIcons` | `true` |
| `show_drawer_labels` | Boolean | `showDrawerLabels` | `true` |
| `search_bar_position` | String (`SearchBarPosition`) | `searchBarPosition` | `TOP` |
| `drawer_folder_display_mode` | String (`DrawerFolderDisplayMode`) | `drawerFolderDisplayMode` | `DO_NOT_SHOW` |
| `recently_installed_position` | String (`RecentlyInstalledPosition`) | `recentlyInstalledPosition` | `SHOW_FIRST` — the "Recently installed" category's position, also used independently by `PrivateSpaceViewModel` for its own copy |

#### Search

| Key | Type | Field | Default |
|---|---|---|---|
| `search_contacts_enabled` | Boolean | `searchContactsEnabled` | `false` |
| `search_settings_enabled` | Boolean | `searchSettingsEnabled` | `false` |

#### Gestures

Both are opt-in switches on Settings → Gestures. Neither is in `BackupBundle`: double-tap to lock depends on a per-device
accessibility grant, and the pair is small enough to set again after a restore.

| Key | Type | Field | Default |
|---|---|---|---|
| `home_opens_search` | Boolean | `homeOpensSearch` | `false` — a Home press on bare Home opens the Drawer with search focused. In `BackupSettings` (defaulted, no version bump) |
| `double_tap_to_lock` | Boolean | `doubleTapToLock` | `false` — the switch only reads on while the accessibility service is also enabled. In `BackupSettings` (defaulted, no version bump) |
| `swipe_left_enabled` | Boolean | `swipeLeftEnabled` | `true`. In `BackupSettings` (defaulted, no version bump) |
| `swipe_right_enabled` | Boolean | `swipeRightEnabled` | `true`. In `BackupSettings` (defaulted, no version bump) |
| `swipe_down_enabled` | Boolean | `swipeDownEnabled` | `true`. In `BackupSettings` (defaulted, no version bump) |
| `swap_swipe_sides` | Boolean | `swapSwipeSides` | `false`. In `BackupSettings` (defaulted, no version bump) |

#### Notifications

| Key | Type | Field | Default |
|---|---|---|---|
| `notification_dots_enabled` | Boolean | `notificationDotsEnabled` | `true` |
| `notification_badge_style` | String (`NotificationBadgeStyle`) | `notificationBadgeStyle` | `DOT` |
| `show_media_player_on_home` | Boolean | `showMediaPlayerOnHome` | `false` — opt-in; the row also needs notification access, so it only shows when both hold |
| `show_media_skip_buttons` | Boolean | `showMediaSkipButtons` | `true` |
| `show_notifications_in_app_menu` | Boolean | `showNotificationsInAppMenu` | `true` — lists an app's notifications in its long-press menu; also needs notification access. In `BackupSettings` (defaulted, no version bump) |

#### Permission-prompt bookkeeping

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `calendar_permission_requested` | Boolean | `calendarPermissionRequested` | `false` | "we already asked once" — the grant itself is read live from `CalendarPermissionRepository` |
| `contacts_permission_requested` | Boolean | `contactsPermissionRequested` | `false` | same, for `ContactPermissionRepository` |
| `bluetooth_permission_requested` | Boolean | `bluetoothPermissionRequested` | `false` | same, for `BLUETOOTH_CONNECT` (facet automation); grant read live from `AutomationPermissionRepository` |
| `location_permission_requested` | Boolean | `locationPermissionRequested` | `false` | same, for `ACCESS_FINE_LOCATION` (named Wi-Fi networks) |

#### First-run & coach marks

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `onboarding_completed` | Boolean | `onboardingCompleted` | `false` | Gates `LauncherActivity`'s onboarding branch |
| `coach_marks_seen` | Set<String> | `coachMarksSeen` | `{}` | Ids from `ui/components/CoachMarkIds.kt`: `HOME_GESTURES`, `HOME_SET_DEFAULT_PROMPT`; written via `markCoachMarkSeen(id)` |

### 5.5 What is deliberately *not* in DataStore

Anything per-facet (Room `facets`), anything per-app placement (Room), permission *grants* (read
live from the OS on every check), the installed-app list, and notification counts (in-memory).

### 5.6 Adding a key — the checklist

1. Add the `Keys.X = xPreferencesKey("snake_case")` constant and the `LauncherSettings` field with its default.
2. Add the read line in `settings` (`?: defaults.x`, lenient enum parse) and the `setX()` writer.
3. Decide whether it is facet-overridable; if so, add the `facets` column (+ migration, §4) and the gate.
4. Decide whether it belongs in `BackupSettings` (+ `BackupMapping` both ways). Bump `CURRENT_BACKUP_VERSION` only for a breaking change — a defaulted additive field doesn't need one ([09 §4](09-flow-backup-restore.md)).
5. Add a `SettingsRepositoryTest` case for default + round-trip, and a row in 5.3 and 5.4 here.

### 5.7 `facet_automation` (`AutomationStateRepository`)

A separate DataStore file, provided by `DataStoreModule` under the `@AutomationDataStore` qualifier. It
is not part of `LauncherSettings` (so its writes don't re-emit every settings collector) and not in
`BackupBundle` (automation state is runtime bookkeeping, and facet ids change on import). Read through
`state: Flow<AutomationState>`; written only through `update { }`, an atomic read-modify-write.

| Key | Type | `AutomationState` field | Meaning |
|---|---|---|---|
| `baseline_facet_id` | Long (absent = null) | `baselineFacetId` | The facet the user last chose by hand; what a rule returns to |
| `active_rule_ids` | String (`"10,11"`, activation order) | `activeRuleIds` | The last evaluated set of true rules; the last one wins |
| `suppressed_rule_ids` | Set<String> | `suppressedRuleIds` | Active rules the user overrode; ignored until they stop being true |

Written by `ActivateFacetByIdUseCase` on every manual switch (`AutomationState.afterManualSwitch`), and
by the automation evaluator once it is wired in (see `IMPLEMENTATION_PLAN.md`, "Facet automation rules").
Stale ids are tolerated: the evaluator drops rule ids that no longer exist and falls back when the
baseline facet was deleted.

## 6. Backup file format (`BackupBundle`, `data/model/BackupBundle.kt`)

`CURRENT_BACKUP_VERSION = 5`; `kotlinx-serialization` JSON written/read by `BackupRepository`
through a user-chosen SAF `Uri`. Import refuses `backupVersion > CURRENT_BACKUP_VERSION`, accepts
older (fields added since carry defaults). Contents: `settings: BackupSettings` — every DataStore key except
the ones below, with `activeFacetIndex` instead of `active_facet_id` and the `landscape_*` keys as flat
`landscape*` fields. **Not backed up** (all deliberate): `active_facet_id` (a row id), `selected_calendar_ids`
and `calendar_colors` (keyed by this device's calendar row ids), `allow_landscape` and `hide_status_bar` (per-device choices),
all four `*_permission_requested` flags and `onboarding_completed`, `coach_marks_seen`
(this install's own history). `BackupFacet` carries every `Facet` field except the id and the hosted
clock stack (`Facet.clockStack`, the rows of `clock_stack_items`): an `AppWidgetHost` id is not
portable, so the widgets are re-added by hand. `BackupCoverageTest` fails when a `Facet` or `LauncherSettings`
field is neither exported nor in its list of exclusions, so a new column can't be left out of a backup silently.
The clock look and placement, the app list's vertical alignment, `recently_installed_position`,
`search_settings_enabled` and the landscape clock and list values were added later as defaulted fields,
without a version bump.
Then
`facets: List<BackupFacet>` (each with its own favorites, dock apps, folder placements),
`dockApps`, `defaultFavoriteApps`, `folders` (with members), `dockFolderPlacements`,
`defaultFavoriteFolderPlacements`, and `widgetPlacements` (never auto-restored — re-bound
one-by-one through the picker flow, see [09-flow-backup-restore.md](09-flow-backup-restore.md)).
Folder references are by *index into `folders`*, facet references by *index into `facets`*, because
Room ids are regenerated on restore.
Their landscape twins `landscapeAppRowPosition` and `landscapeAppListColumnAlignment` are exported too, in both, as optional additive fields (absent means "same as portrait", no version bump). The portrait clock placement isn't backed up, so landscape's isn't either.

**Automation rules are not exported.** `automation_rules` has no field in `BackupBundle`, and import
calls `deleteAllFacets()` first, so the foreign-key cascade deletes every rule on restore. The
`facet_automation` state is not exported either ([§5.7](#57-facet_automation-automationstaterepository));
it holds stale ids after a restore, which the evaluator tolerates. Backing rules up would need
`BackupBundle` fields (facet references by index, like placements) and a version decision.
