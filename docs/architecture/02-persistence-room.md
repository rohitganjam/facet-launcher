# 02 — Database & Persistence Architecture

## 0. Storage map — everything Facet persists, and where

| Store | Location | What it holds | Written by | Survives |
|---|---|---|---|---|
| Room `FacetDatabase` | `facet.db` (schema **v35**, `exportSchema = true` → `app/schemas/…/1.json … 35.json`) | Facets + every *placement*: favorites, dock, folders, folder membership, widget grid positions, plus the facet automation rules (`automation_rules`) the weather cache (`weather_hours`, `weather_meta`) and each facet's clock stack (`clock_stack_items`) | 7 repositories (§2): `FacetRepository`, `PlacementRepository`, `FolderRepository`, `WidgetPlacementRepository`, `AutomationRuleRepository`, `WeatherRepository` (through `RoomWeatherStore`), `ClockStackRepository` | Reinstall-over-upgrade (migrations); **not** downgrade (dropped) |
| `DataStore<Preferences>` | `facet_settings` (`datastore/facet_settings.preferences_pb`) | **All launcher-wide settings and defaults** — clock/app-list/dock design defaults (the calendar events strip has no design defaults of its own — see below), theme, drawer, search, permission-prompt flags, onboarding/seed/coach-mark flags, active facet id, the clock accessory switches, weather units and location — 79 keys (§5) | `SettingsRepository` only | Any upgrade (missing keys fall back to `LauncherSettings()` defaults) |
| `DataStore<Preferences>` | `facet_entitlement` (`datastore/facet_entitlement.preferences_pb`) | The cached Pro state (`pro_purchased`; excluded from Android backups and device transfer by `res/xml/data_extraction_rules.xml`), so Pro works offline and at cold start; debug builds also keep `debug_entitlement_mode` here | `PlayEntitlementRepository`, only from an explicit Google Play answer | Same as `facet_settings`; **not** in the backup file, so a restore can never grant Pro |
| `DataStore<Preferences>` | `facet_automation` (`datastore/facet_automation.preferences_pb`) | Facet-automation bookkeeping: the baseline facet, the ordered active rule ids, the suppressed rule ids (§5.7) | `AutomationStateRepository` (via `ActivateFacetByIdUseCase`) | Same as `facet_settings`; **not** in the backup file |
| System `AppWidgetService` | Android framework, keyed by `HUB_APP_WIDGET_HOST_ID = 1024` | Which `appWidgetId`s are bound to which providers for this host | `LauncherAppWidgetHost` via `AppWidgetRepository` (allocate/bind/delete) | App data clear **does not** clear it → orphan detection in `ObserveHubStateUseCase` |
| Backup file | User-picked SAF `Uri` (`CreateDocument`/`OpenDocument`), JSON via `kotlinx-serialization` | `BackupBundle` v3: settings + facets + placements + folders + widget placements (raw rows, unhydrated) | `BackupRepository` (`ExportBackupUseCase` / `ImportBackupUseCase`) | Whatever the user does with the file — Facet keeps no copy |
| In-memory only | `NotificationBadgeRepository.badgeCounts` (`MutableStateFlow`) | Per-package non-silent notification counts | `FacetNotificationListenerService` | Process lifetime only |

Nothing else is persisted: no `SharedPreferences`, no files in `filesDir`/`cacheDir`, no network.

**Global defaults vs per-facet overrides.** The clock, app-list and dock *defaults* the
user sets in Settings are DataStore keys (`clock_template_id`, `clock_alignment`,
`list_content_mode`, `dock_display_mode`, …). The `facets` table carries the same fields again
as *overrides*, each block gated by a flag (`overrideClock`, `overrideApps`, `overrideDock`,
`overrideCalendar`, `overridingFavorites`); `Facet.resolveOverride(...)` picks the facet
value when its flag is set and the DataStore value otherwise. The ER diagram in §1 is Room only —
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
        boolean overrideClock "gates the whole clock+calendar design block"
        string clockTemplateId "enum via Converters"
        string clockFontOption
        string clockColorOption
        string clockAccentColorOption
        boolean use24HourTime
        boolean clockShowMeridiem
        string clockDateStyle
        boolean clockShowAlarm "default true; used while overrideClock"
        boolean clockShowBattery "default true"
        boolean clockShowWeather "default false"
        string clockWeatherIconStyle "default COLORED; used while overrideClock"
        string clockAlignment "also positions the calendar events strip"
        float clockZoneHeightDp "nullable"
        float clockScale
        boolean overrideApps
        string appRowPosition
        string appRowPresentation
        string listContentMode
        int appsToShowCount
        string appListVerticalAlignment
        string appListLayout "single column / two column / grid"
        string appListColumnAlignment "two-column only"
        string appListGridColumns "grid only"
        string appListGridDisplayMode "grid only"
        boolean overridingFavorites
        boolean overrideDock
        string dockDisplayMode
        boolean overrideCalendar
        boolean showAllDayEvents
        boolean showCalendarIndicator
        int calendarItemsToShow
        string selectedCalendarIdsCsv "nullable"
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
    }

    facets ||--o{ clock_stack_items : "has (CASCADE)"
    clock_stack_items {
        long id PK
        long facetId FK
        int position "swipe order"
        string kind "CLOCK or WIDGET"
        int appWidgetId "null for CLOCK"
        int widthDp "nullable until resized"
        int heightDp
        string alignment "nullable"
        int landscapeWidthDp "null falls back to portrait"
        int landscapeHeightDp
        string landscapeAlignment
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
  is the *global default* list and a facet id is that facet's *per-facet override*. Which one is live
  for a facet is decided by its `overrideDock` / `overridingFavorites` flags via
  `Facet.placementTarget(list)`; `AddToActiveListUseCase` / `RemoveFromActiveListUseCase` ask
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
| `ClockStackItemDao` | `observe(facetId)`, `observeAll()` (`ORDER BY position, id`) | `insert` | `delete(id)`, `deleteAll(facetId)` | `getAll(facetId)`, `getById(id)`; updates `replace` (swaps the kind and widget id, resets the sizes), `setPosition`, `setSize`, `setLandscapeSize`, `setAlignment`, `setLandscapeAlignment`. `ClockStackRepository` wraps add, remove, reorder and clear in `database.withTransaction`, caps a stack at `MAX_CLOCK_STACK_ITEMS` (5) and allows one `CLOCK` item |
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
comma-joined `selectedCalendarIdsCsv`. The override helpers (`resolveOverride`, `resolveSentinel`,
`landscape`, `placementTarget`, …) are extensions on `Facet`.

Every facet write takes a facet id (`setClockScale(facetId, …)`). The repository re-reads the row and
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

The landscape twins are handled as one `LandscapeOverrides` group (clock alignment, divider, scale, widget size, row position, vertical and column alignment; every field nullable). Settings hold it as `LauncherSettings.landscape`, read from the six `landscape_*` keys and written only through `updateLandscape`. A facet keeps the flat columns above (so the schema is unchanged) and exposes the group as `FacetEntity.landscape` / `withLandscape(...)`, written through `FacetRepository.updateLandscape`. `resolveLandscape` is the one resolution rule (see [17](17-flow-home-layout.md) §1.0).

## 5. DataStore — `facet_settings` (`SettingsRepository`)

The second store. One `DataStore<Preferences>`, provided by `DataStoreModule`
(`preferencesDataStore(name = "facet_settings")` → `files/datastore/facet_settings.preferences_pb`),
read and written **only** by `SettingsRepository`. It is a flat key–value file: no schema version,
no migrations — evolution is "add a key with a default", and removed keys are simply ignored.

### 5.1 Key diagram — every key, and what it overrides in `facets`

The diagram uses ER notation so it reads alongside §1. `facet_settings` is one logical record
(there is exactly one); each dotted "overridden by" edge means the `facets` column of the same
name replaces the global value whenever the named gate flag on that facet is `true`
(`Facet.resolveOverride`).

```mermaid
erDiagram
    facet_settings {
        string clock_template_id "ClockTemplateId, default LIGHT_STACK"
        string clock_font_option "ClockFontOption, default LAUNCHER_DEFAULT"
        string clock_color_option "ClockColorOption, default THEME"
        string clock_accent_color_option "ClockColorOption, default ACCENT_PRIMARY"
        boolean use_24_hour_time "default false"
        boolean clock_show_meridiem "default false"
        string clock_date_style "ClockDateStyle, default FULL"
        string clock_alignment "ClockAlignment, default LEFT; also positions the calendar events strip"
        float clock_zone_height_dp "nullable, absent = template height"
        float clock_scale "default 0.8"
        boolean allow_landscape "default true; off = portrait only"
        string landscape_clock_alignment "nullable, absent = use clock_alignment"
        float landscape_clock_zone_height_dp "nullable, absent = use clock_zone_height_dp"
        float landscape_clock_scale "nullable, absent = use clock_scale"
        boolean show_all_day_events "default true"
        boolean show_calendar_indicator "default true"
        int calendar_items_to_show "1..3, default 3"
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
        string notification_badge_style "NotificationBadgeStyle, default DOT"
        boolean show_media_player_on_home "default false"
        boolean show_media_skip_buttons "default true"
        boolean show_notifications_in_app_menu "default true"
        boolean calendar_permission_requested "default false"
        boolean contacts_permission_requested "default false"
        boolean bluetooth_permission_requested "default false"
        boolean location_permission_requested "default false"
        boolean onboarding_completed "default false"
        boolean defaults_seeded "default false"
        stringset coach_marks_seen "CoachMarkIds, default empty"
    }

    facets {
        long id PK
        boolean overrideClock "gate: 10 clock design columns (also positions the calendar strip)"
        boolean overrideApps "gate: listContentMode, appsToShowCount (content only now)"
        boolean overridingFavorites "gate: this facet's Favorites vs the global default list"
        boolean overrideDock "gate: this facet's Dock vs the global default list (content only now)"
        boolean overrideCalendar "gate: showAllDayEvents, showCalendarIndicator, calendarItemsToShow, selectedCalendarIdsCsv"
    }

    facet_settings ||--o{ facets : "active_facet_id selects one"
    facet_settings }|..|| facets : "clock keys overridden when overrideClock"
    facet_settings }|..|| facets : "list content mode/count overridden when overrideApps"
    facet_settings }|..|| facets : "calendar keys overridden when overrideCalendar"
```

Keys with **no** facet counterpart (always global): `home_apps_font_weight`,
`app_label_color_option`, `launcher_font_option` (all three of which the calendar events strip
reads directly — see §0), `calendar_colors`, everything under Theme, Drawer, Search,
Notifications, Permission bookkeeping, and First-run.

**`appRowPosition`/`appRowPresentation`/`appListVerticalAlignment`/`dockDisplayMode`/`appListLayout`/
`appListColumnAlignment`/`appListGridColumns`/`appListGridDisplayMode` are a third
resolution shape**, distinct from both the boolean-gated fields above and the always-global list —
edited from Settings → Appearance now (moved out of Dock's/Home Apps List's own screens, see chat
history), each resolved via its own `LAUNCHER_DEFAULT` sentinel value (mirroring
`ClockFontOption`'s existing one) rather than a shared override flag:
`facetValue == LAUNCHER_DEFAULT ? globalValue : facetValue`
([`resolveSentinel`](../../app/src/main/kotlin/com/facetlauncher/app/data/model/Facet.kt)),
independently of `overrideApps`/`overrideDock`. `LAUNCHER_DEFAULT` is filtered out of the option
list at global scope (nothing to inherit from there) and is each `FacetEntity` column's own
default value for a newly-created facet.

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

### 5.3 Write path — the complete writer API (64 functions)

Every writer is `suspend`, wraps a single `dataStore.edit { }` and touches exactly one key.
DataStore serialises writes and is main-safe; callers `viewModelScope.launch { }` them.

| Writer | Key | Op | Called from |
|---|---|---|---|
| `setClockTemplateId(ClockTemplateId)` | `clock_template_id` | set | `ClockStyleGalleryViewModel` |
| `setClockFontOption(ClockFontOption)` | `clock_font_option` | set | `ClockStyleGalleryViewModel` |
| `setClockColorOption(ClockColorOption)` | `clock_color_option` | set | `ClockStyleGalleryViewModel` |
| `setClockAccentColorOption(ClockColorOption)` | `clock_accent_color_option` | set | `ClockStyleGalleryViewModel` |
| `setUse24HourTime(Boolean)` | `use_24_hour_time` | set | `ClockStyleGalleryViewModel` |
| `setClockShowMeridiem(Boolean)` | `clock_show_meridiem` | set | `ClockStyleGalleryViewModel` |
| `setClockDateStyle(ClockDateStyle)` | `clock_date_style` | set | `ClockStyleGalleryViewModel` |
| `setShowNextAlarm(Boolean)`, `setShowBatteryPercentage(Boolean)`, `setShowWeather(Boolean)` | `show_next_alarm`, `show_battery_percentage`, `show_weather` | set | `ClockAccessoriesViewModel` |
| `setTemperatureUnit`, `setWindSpeedUnit`, `setRainUnit`, `setPressureUnit` | `temperature_unit`, `wind_speed_unit`, `rain_unit`, `pressure_unit` | set | `UnitsAndLocationViewModel` |
| `setWeatherIconStyle(WeatherIconStyle)` | `weather_icon_style` | set | `ClockAccessoriesViewModel` (default screen; a facet uses `FacetRepository.setClockWeatherIconStyle`) |
| `setWeatherLocationMode(WeatherLocationMode)` | `weather_location_mode` | set | none yet |
| `setWeatherCity(WeatherCity?)` | `weather_city_name`, `weather_city_latitude`, `weather_city_longitude`, `weather_city_timezone` | set the four together, or remove all four on `null` | none yet |
| `chooseWeatherCity(WeatherCity): Boolean` | `weather_saved_cities`, the four city keys, `weather_location_mode` | add the city to the saved list unless already there (refused, returning false, past 10), make it the chosen city and switch to city mode | `WeatherLocationViewModel` |
| `removeWeatherCity(WeatherCity)` | `weather_saved_cities`, the four city keys | drop it from the list; clear the four keys when it was the chosen one | `WeatherLocationViewModel` |
| `setSavedWeatherCities(List<WeatherCity>)` | `weather_saved_cities` | replace the list (restore), at most 10 | `ImportBackupUseCase` |
| `setAllowLandscape(Boolean)` | `allow_landscape` | set | `AppearanceSettingsViewModel` |
| `setClockAlignment(ClockAlignment)` | `clock_alignment` | set | `ClockStyleGalleryViewModel`, `HomeViewModel` (clock adjust toolbar) |
| `setClockZoneHeight(Float)` | `clock_zone_height_dp` | set | `HomeViewModel` (zone drag handle) |
| `resetClockZoneHeight()` | `clock_zone_height_dp` and `landscape_clock_zone_height_dp` | **remove** | `ClockStyleGalleryViewModel` |
| `setClockScale(Float)` | `clock_scale` | set | `HomeViewModel` (corner handle) |
| `resetClockScale()` | `clock_scale` and `landscape_clock_scale` | **remove** |
| `updateLandscape((LandscapeOverrides) -> LandscapeOverrides)` | all six `landscape_*` keys | set each field, or **remove** it when `null` | `HomeViewModel` (landscape adjust commits), `AppearanceSettingsViewModel` (Landscape alignment rows), `ClockStyleGalleryViewModel` (Reset position), `ImportBackupUseCase` |
| `setShowAllDayEvents(Boolean)` | `show_all_day_events` | set | `CalendarSettingsViewModel` |
| `setShowCalendarIndicator(Boolean)` | `show_calendar_indicator` | set | `CalendarSettingsViewModel` |
| `setCalendarItemsToShow(Int)` | `calendar_items_to_show` | set (clamped 1..3) | `CalendarSettingsViewModel` |
| `setSelectedCalendarIds(Set<String>)` | `selected_calendar_ids` | set | `CalendarSettingsViewModel` |
| `setCalendarColors(Map<String,String>)` | `calendar_colors` | set (encoded `id:color`) | `CalendarSettingsViewModel` after `AssignCalendarColorsUseCase` |
| `setListContentMode(ListContentMode)` | `list_content_mode` | set | `HomeAppsListSettingsViewModel`, `OnboardingViewModel` |
| `setAppsToShowCount(Int)` | `apps_to_show_count` | set | `HomeAppsListSettingsViewModel`, `OnboardingViewModel` |
| `setAppRowPosition(AppRowPosition)` | `app_row_position` | set | `AppearanceSettingsViewModel` |
| `setAppRowPresentation(AppRowPresentation)` | `app_row_presentation` | set | `AppearanceSettingsViewModel` |
| `setAppListVerticalAlignment(AppListVerticalAlignment)` | `app_list_vertical_alignment` | set | `AppearanceSettingsViewModel` |
| `setAppListLayout(AppListLayout)` | `app_list_layout` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListColumnAlignment(AppListColumnAlignment)` | `app_list_column_alignment` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListGridColumns(AppListGridColumns)` | `app_list_grid_columns` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setAppListGridDisplayMode(AppListGridDisplayMode)` | `app_list_grid_display_mode` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setHomeAppsFontWeight(FontWeightOption)` | `home_apps_font_weight` | set | `AppearanceSettingsViewModel` |
| `setAppLabelColorOption(ClockColorOption)` | `app_label_color_option` | set | `AppearanceSettingsViewModel` |
| `setDockDisplayMode(DockDisplayMode)` | `dock_display_mode` | set | `AppearanceSettingsViewModel` |
| `setActiveFacetId(Long)` | `active_facet_id` | set | `FacetCarouselViewModel`, `ManageFacetsViewModel`, `EnsureActiveFacetUseCase`, `ActivateFacetByIdUseCase`, `ImportBackupUseCase` |
| `setThemeMode(ThemeMode)` | `theme_mode` | set | `AppearanceSettingsViewModel` |
| `setSystemBarIconStyle(SystemBarIconStyle)` | `system_bar_icon_style` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
| `setAccentFromSystem(Boolean)` | `accent_from_system` | set | `AppearanceSettingsViewModel` |
| `setCustomAccentSwatch(String)` | `custom_accent_swatch` | set | `AppearanceSettingsViewModel` |
| `setWallpaperAccentRole(WallpaperAccentRole)` | `wallpaper_accent_role` | set | `AppearanceSettingsViewModel` |
| `setIconRenderMode(IconRenderMode)` | `icon_render_mode` | set | `AppearanceSettingsViewModel` |
| `setIconShape(IconShape)` | `icon_shape` | set | `AppearanceSettingsViewModel`, `ImportBackupUseCase` |
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
| `setNotificationBadgeStyle(NotificationBadgeStyle)` | `notification_badge_style` | set | `NotificationSettingsViewModel` |
| `setShowMediaPlayerOnHome(Boolean)` | `show_media_player_on_home` | set | `NotificationSettingsViewModel` |
| `setShowMediaSkipButtons(Boolean)` | `show_media_skip_buttons` | set | `NotificationSettingsViewModel` |
| `setShowNotificationsInAppMenu(Boolean)` | `show_notifications_in_app_menu` | set | `NotificationSettingsViewModel` |
| `setCalendarPermissionRequested(Boolean)` | `calendar_permission_requested` | set | `PermissionsViewModel` |
| `setContactsPermissionRequested(Boolean)` | `contacts_permission_requested` | set | `PermissionsViewModel` |
| `setBluetoothPermissionRequested(Boolean)` | `bluetooth_permission_requested` | set | `PermissionsViewModel`, `RuleEditorViewModel` |
| `setLocationPermissionRequested(Boolean)` | `location_permission_requested` | set | `PermissionsViewModel`, `RuleEditorViewModel` |
| `setOnboardingCompleted(Boolean)` | `onboarding_completed` | set | `LauncherViewModel.completeOnboarding()` |
| `setDefaultsSeeded(Boolean)` | `defaults_seeded` | set | `SeedDefaultDockUseCase` |
| `markCoachMarkSeen(String)` | `coach_marks_seen` | set (union with existing) | `HomeViewModel` |

`ImportBackupUseCase.applySettings()` restores settings by calling these same setters one at a time (42 calls, plus `setActiveFacetId` after facets are re-inserted) —
there is no bulk "replace all preferences" entry point, so a restore is not atomic at the
DataStore level either.

### 5.4 Key registry by section

The same 67 keys, grouped the way Settings screens present them, with the `LauncherSettings` field
each maps to. Defaults are `LauncherSettings()`'s constructor defaults.

#### Clock design (global; overridden per facet when `facets.overrideClock`)

The calendar events strip has no design keys of its own here any more — it reads the global-only
`home_apps_font_weight`/`app_label_color_option` ("Home app list", below) and `launcher_font_option`
("Theme & appearance", below) keys for font/weight/color, and `clock_alignment` in this section for
its position (see chat history: calendar/appearance styling consolidation).

| Key | Type | `LauncherSettings` field | Default | Notes |
|---|---|---|---|---|
| `clock_template_id` | String (enum `ClockTemplateId`) | `clockTemplateId` | `LIGHT_STACK` | |
| `clock_font_option` | String (`ClockFontOption`) | `clockFontOption` | `LAUNCHER_DEFAULT` | |
| `clock_color_option` | String (`ClockColorOption`) | `clockColorOption` | `THEME` | |
| `clock_accent_color_option` | String (`ClockColorOption`) | `clockAccentColorOption` | `ACCENT_PRIMARY` | |
| `use_24_hour_time` | Boolean | `use24HourTime` | `false` | |
| `clock_show_meridiem` | Boolean | `clockShowMeridiem` | `false` | |
| `clock_date_style` | String (`ClockDateStyle`) | `clockDateStyle` | `FULL` | |
| `show_next_alarm` | Boolean | `showNextAlarm` | `true` | launcher default of the clock accessories; a facet overriding the clock uses its own `clockShowAlarm` |
| `show_battery_percentage` | Boolean | `showBatteryPercentage` | `true` | likewise `clockShowBattery` |
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
| `clock_scale` | Float | `clockScale` | `0.8` | `resetClockScale()` removes the key |
| `allow_landscape` | Boolean | `allowLandscape` | `true` | launcher-wide: off keeps Home in portrait, locks the screen to portrait and hides the landscape settings |
| `landscape_clock_alignment` | String (`ClockAlignment`) | `landscapeClockAlignment` | `null` (= `clock_alignment`) | landscape's own placement, written from Home's adjust mode in landscape |
| `landscape_clock_zone_height_dp` | Float | `landscapeClockZoneHeightDp` | `null` (= `clock_zone_height_dp`) | landscape's divider position (the clock pane width) |
| `landscape_clock_scale` | Float | `landscapeClockScale` | `null` (= `clock_scale`) | removed by `resetClockScale()` too |

#### Calendar selection (global; overridden when `facets.overrideCalendar`)

| Key | Type | Field | Default | Notes |
|---|---|---|---|---|
| `show_all_day_events` | Boolean | `showAllDayEvents` | `true` | |
| `show_calendar_indicator` | Boolean | `showCalendarIndicator` | `true` | Calendar-colour bar on Home event rows; facet-overridable via `overrideCalendar` |
| `calendar_items_to_show` | Int | `calendarItemsToShow` | `3` | Events Home shows, clamped to 1..3 on read and write; facet-overridable via `overrideCalendar` |
| `selected_calendar_ids` | Set<String> | `selectedCalendarIds` | `null` (= all calendars) | nullable — absent key means "every calendar" |
| `calendar_colors` | Set<String> encoded `"<calendarId>:<colorName>"` | `calendarColors: Map<String,String>` | `{}` | Assigned by `AssignCalendarColorsUseCase`; entries without a `:` are dropped on read; not facet-overridable |

#### Home app list (global; overridden when `facets.overrideApps` / `overridingFavorites`)

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
| `app_list_grid_display_mode` | String (`AppListGridDisplayMode`) | `appListGridDisplayMode` | `ICONS` | grid only — icons or text, never both |
| `home_apps_font_weight` | String (`FontWeightOption`) | `homeAppsFontWeight` | `REGULAR` | not facet-overridable; also styles the calendar events strip |
| `app_label_color_option` | String (`ClockColorOption`) | `appLabelColorOption` | `THEME` | not facet-overridable; also styles the calendar events strip |

#### Dock (global; overridden when `facets.overrideDock`)

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
| `launcher_font_option` | String (`LauncherFontOption`) | `launcherFontOption` | `SYSTEM` | also styles the calendar events strip |
| `font_scale_option` | String (`FontScaleOption`) | `fontScaleOption` | `DEFAULT` | multiplies every `MaterialTheme.typography` role's `fontSize`/`lineHeight` app-wide except the clock |

#### App drawer

| Key | Type | Field | Default |
|---|---|---|---|
| `drawer_presentation` | String (`DrawerPresentation`) | `drawerPresentation` | `LIST` |
| `drawer_grid_size` | String (`DrawerGridDensity`) | `drawerGridDensity` | `COMPACT` |
| `drawer_list_item_size` | String (`DrawerListItemSize`) | `drawerListItemSize` | `REGULAR` |
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
| `defaults_seeded` | Boolean | `defaultsSeeded` | `false` | Set by `SeedDefaultDockUseCase` so the dock is seeded exactly once |
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

`CURRENT_BACKUP_VERSION = 3`; `kotlinx-serialization` JSON written/read by `BackupRepository`
through a user-chosen SAF `Uri`. Import refuses `backupVersion > CURRENT_BACKUP_VERSION`, accepts
older (fields added since carry defaults). Contents: `settings: BackupSettings` — every DataStore key except
the ones below, with `activeFacetIndex` instead of `active_facet_id` and the `landscape_*` keys as flat
`landscape*` fields. **Not backed up** (all deliberate): `active_facet_id` (a row id), `selected_calendar_ids`
and `calendar_colors` (keyed by this device's calendar row ids), `allow_landscape` (a per-device choice),
all four `*_permission_requested` flags and `onboarding_completed`, `defaults_seeded`, `coach_marks_seen`
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
