# 07 — Repository & Use Case Registries

Complete inventories of the two injectable layers, generated from constructor signatures and
`@Inject` sites in `app/src/main`. If a class is not in these tables it does not exist; if a
dependency is not listed the class does not have it.

## 1. Repositories and data singletons (49) — all `@Singleton`, all constructor-injected, and the weather seams (`WeatherApi`, `WeatherStore`, `PlaceGeocoder`) are the only interfaces

| Repository | Wraps (source of truth) | Injects | Public API shape | Injected by |
|---|---|---|---|---|
| `AppRepository` | `LauncherApps` + `UserManager` — installed activities across every profile | `LauncherApps`, `UserManager`, `Context` | `observeInstalledApps()`, `observeAppsForProfile(p)`, `getInstalledApps()` (one-shot), `observeUninstalledPackages()`, `observeProfileRemoved()`, `profileFor(handle)`, `resolveUserHandle(p)`, `handlesFor(p)`, `openAppDetails(app)` | 5 repositories, 3 use cases, `DrawerViewModel`, `BackupRestoreViewModel` |
| `AppWidgetRepository` (`data/widget`) | `AppWidgetManager` + `LauncherAppWidgetHost` | `Context`, `AppWidgetManager`, `LauncherAppWidgetHost`, `UserManager`, `AppRepository` | provider listing, `allocateAppWidgetId`, `bindAppWidgetIdIfAllowed`, `createBindIntent`, `createConfigureIntentSender`, `createHostView`, `updateWidgetSize`, `start/stopListening`, `observeProviderChanges()`, `deleteAppWidgetId` | `HubViewModel`, `HubWidgetPickerViewModel`, `BackupRestoreViewModel`, `ObserveHubStateUseCase`, `DeleteWidgetUseCase` |
| `AutomationPermissionRepository` (`open`) | `checkSelfPermission` for `BLUETOOTH_CONNECT` and `ACCESS_FINE_LOCATION` | `Context` | `isGranted(permission)`, `isUsable(trigger)` | `RefreshAutomationStateUseCase`, `ObserveFacetAutomationUseCase`, `PermissionsViewModel`, `RuleEditorViewModel` |
| `AutomationRuleRepository` | Room `automation_rules` | `AutomationRuleDao`, `FacetDao` (existence check on save) | `observeRules(): Flow<List<AutomationRule>>` (skips uninterpretable rows), `getRules()`, `getById(id)`, `save(rule): SaveRuleResult` (`Saved(id)`, inserting at the end or updating in place; `Invalid(errors)` or `FacetMissing` with nothing written), `setEnabled`, `delete` | `RefreshAutomationStateUseCase`, `RunFacetAutomationUseCase`, `ObserveFacetAutomationUseCase`, `SaveAutomationRuleUseCase`, `FacetAutomationViewModel`, `RuleEditorViewModel` (list, open, delete), `SettingsViewModel` (rule count) |
| `AutomationStateRepository` | `DataStore<Preferences>` `facet_automation` (`@AutomationDataStore`) | `DataStore<Preferences>` | `state: Flow<AutomationState>`, `get()`, `update(transform)` (atomic read-modify-write) | `ActivateFacetByIdUseCase`, `RefreshAutomationStateUseCase`, `ObserveFacetAutomationUseCase` |
| `BluetoothRepository` | `ACTION_ACL_*` and `ACTION_STATE_CHANGED` broadcasts + A2DP/headset profile proxies + GATT; `BluetoothAdapter.bondedDevices` | `Context` | `observeConnectedAddresses(): Flow<Set<String>?>` (null = unknown), `suspend pairedDevices()` (IO) | `DeviceStateRepository`, `RuleEditorViewModel` |
| `BackupRepository` | SAF `Uri` file I/O + JSON | `Context` | `writeBackup(uri, bundle)`, `readBackup(uri)` (IO) | `ExportBackupUseCase`, `ImportBackupUseCase` |
| `BatteryRepository` | `ACTION_BATTERY_CHANGED` sticky broadcast | `Context` | `observeBatteryStatus(): Flow` | `ObserveClockAccessoriesUseCase` |
| `CalendarPermissionRepository` (`open`) | `checkSelfPermission(READ_CALENDAR)` | `Context` | `isGranted()` | `ObserveHomeScreenStateUseCase`, `ObserveFacetPreviewsUseCase`, `CalendarSettingsViewModel`, `PermissionsViewModel`, `AppearanceSettingsViewModel` |
| `CalendarRepository` | `CalendarContract` via `ContentResolver` | `ContentResolver` | `getUpcomingEvents(ids, includeAllDay)` (IO, one-shot), calendar listing | same three + `CalendarSettingsViewModel`, `AppearanceSettingsViewModel` |
| `ContactPermissionRepository` | `checkSelfPermission(READ_CONTACTS)` | `Context` | `isGranted()` | `DrawerViewModel`, `PermissionsViewModel` |
| `ContactRepository` | `ContactsContract` via `ContentResolver` | `ContentResolver`, `Context` | `searchContacts(q, limit)`, `getConnections(contactId)` (IO) | `DrawerViewModel` |
| `DefaultAppRepository` | `PackageManager.resolveActivity` for browser/SMS/camera/mail/phone intents | `Context` | `getDefaultAppPackages()` (IO) | `SeedDefaultDockUseCase`, `HomeAppsListSettingsViewModel` |
| `DefaultLauncherRepository` | `RoleManager` / `PackageManager` home-role query | `Context` | `isDefaultLauncher()`, `requestDefaultLauncherIntent()` | `HomeViewModel`, `SettingsViewModel` |
| `DeviceStateRepository` | combines battery, headphones, Wi-Fi and Bluetooth into one hot `StateFlow<DeviceState>`, subscribing each source only while an enabled rule reads it | `BatteryRepository`, `HeadphonesRepository`, `WifiRepository`, `AutomationRuleRepository`, `BluetoothRepository`, `@ApplicationScope CoroutineScope` | `deviceState`, `current()`, `onPermissionsChanged()` | `RefreshAutomationStateUseCase`, `RunFacetAutomationUseCase`, `PermissionsViewModel`, `RuleEditorViewModel` |
| `EntitlementRepository` (`open`) | the Pro seam: the base class is "everyone is Pro" (tests, previews); the app injects `PlayEntitlementRepository` via `EntitlementModule` | — | `isPro: StateFlow<Boolean>`, `awaitLoaded()`, `suspend refresh(): ProQueryResult` (what Play said, so Restore can report it) | `RefreshAutomationStateUseCase`, `RunFacetAutomationUseCase`, `ObserveFacetAutomationUseCase`, `SelectableFacetsUseCase`, `AddFacetUseCase`, `RuleEditorViewModel`, `ManageFacetsViewModel`, `FacetCarouselViewModel`, `LauncherActivity` (refresh on resume), `FacetProViewModel`, `SettingsViewModel` |
| `PlacementRepository` | Room `app_placements` + `folder_placements` — every Dock and Favorites list, launcher-wide and per facet; a `PlacementTarget(list, facetId)` names one | `AppPlacementDao`, `FolderPlacementDao` | raw reads `observeAppEntities` / `observeFolderPlacements(target)` and their batched `…ForFacets(list, ids)` forms (hydration lives in `ObservePlacedItemsUseCase`), `addApp`, `removeApp`, `placeFolder`, `removeFolderPlacement`, `replaceItems`, `reorderItems`, `deleteAll(target)`, `removeByPackage/UserId` (every list), raw getters, `restoreApp` / `restoreFolderPlacement`, `getOrphanedRows`, `backfillUserId` | 8 use cases, 5 ViewModels |
| `PlayEntitlementRepository` | Pro from Google Play, cached in the `facet_entitlement` DataStore; only an explicit "owned" or "no purchase" answer changes it (a pending purchase or an unreachable Play keeps the last value) | `BillingRepository`, `@EntitlementDataStore DataStore<Preferences>`, `@ApplicationScope CoroutineScope` | overrides `isPro`, `awaitLoaded()`, `refresh()`; applies an optional debug `EntitlementOverride` on top of Play's answer (the cache keeps Play's answer only) | `EntitlementModule` (bound as `EntitlementRepository`) |
| `BillingRepository` (`open`) | Google Play Billing client over IPC, the one-time `facet_pro` product; no network permission needed | `Context`, `@ApplicationScope CoroutineScope` | `suspend queryPro(): ProQueryResult`, `suspend priceText()`, `suspend launchPurchase(activity)`, `updates: Flow<ProQueryResult>`; acknowledges purchases | `PlayEntitlementRepository`, `FacetProViewModel` (price, purchase flow, pending updates) |
| `FacetRepository` | Room `facets` | `FacetDao` | `observeFacets()`, `getById`, `addFacet()`, `renameFacet`, 28 per-column `setX(facet, value)` setters, 4 `updateOverridingX(...)` block setters (trimmed — `updateOverridingApps`/`updateOverridingDock` no longer take the look-field params, moved to `AppearanceSettingsViewModel`'s own per-column setters), `deleteFacet`, `deleteAllFacets`, `restoreFacet`, `reorderFacets` | 11 use cases, 12 ViewModels |
| `FacetShortcutRepository` | `ShortcutManagerCompat` dynamic shortcuts | `Context` | `syncShortcuts(facets)` — full atomic replace, one `ShortcutInfoCompat` per facet | `SyncFacetShortcutsUseCase` |
| `FolderRepository` | Room `folders` + `folder_apps` | `FolderDao`, `AppRepository` | `observeFolders(): Flow<List<Folder>>` (hydrated), create/rename/delete, add/remove/reorder members, `removeByPackage/UserId`, `getRawFolders`, `restoreFolder`, `deleteAllFolders`, orphan repair | `PlacementRepository`, 5 use cases, 6 ViewModels |
| `HeadphonesRepository` | `AudioManager` output devices + `AudioDeviceCallback` | `Context` | `observeHeadphonesPluggedIn(): Flow<Boolean>` | `DeviceStateRepository` |
| `LocationRepository` | `LocationManager`: one approximate fix (network provider's current location, else the last known), `ACCESS_COARSE_LOCATION` or fine | `Context`, `@IoDispatcher` | `isPermissionGranted()`, `currentLocation(): DeviceLocation?` (10-second timeout; an altitude of exactly 0 counts as unknown). Never continuous and never in the background | `RefreshWeatherUseCase` |
| `MediaSessionRepository` (`open`) | `MediaSessionManager.getActiveSessions` via the notification-listener grant, `MediaController` callbacks and transport controls; sessions only count while `NotificationBadgeRepository.mediaNotificationPackages` has their package; art falls back to a `content:`/`file:` art URI decoded off the main thread | `Context`, `NotificationBadgeRepository` | `observe(): Flow<MediaPlayerState?>` (cold), `skipToPrevious()`, `togglePlayPause()`, `skipToNext()`, `openPlayer()` | `ObserveHomeScreenStateUseCase`, `HomeViewModel` |
| `ReleaseNotesRepository` (`open`) | bundled `assets/whats_new/<version>.md` (user-facing New/Improved bullets per release, drafted and signed off by `scripts/release.sh` before the build); `ReleaseNotesParser` parses each file | `Context` | `releases(): List<ReleaseNotes>` (IO, one-shot, newest first) | `WhatsNewPillViewModel` | the What's new pill over Home (`WhatsNewPillHost`): the version to announce, and `onShown` | — |
| `AppVersion` (`open`) | `BuildConfig.VERSION_NAME`, behind a class so tests can supply their own | — | `name` | `WhatsNewPillViewModel`, `WhatsNewViewModel` |
| `WhatsNewViewModel`, `WhatsNewPillViewModel` |
| `NextAlarmRepository` | `AlarmManager.nextAlarmClock` + `ACTION_NEXT_ALARM_CLOCK_CHANGED` | `Context`, `Clock` | `observeNextAlarmMillis(): Flow<Long?>` | `ObserveClockAccessoriesUseCase` |
| `NotificationActionController` | the connected `FacetNotificationListenerService`, which attaches a `cancelNotifications` lambda while bound | — | `attach(canceller)`, `detach()`, `dismiss(keys): Boolean` | `DrawerViewModel` (callers); `FacetNotificationListenerService` (attach/detach) |
| `NotificationAccessRepository` (`open`) | `NotificationManagerCompat.getEnabledListenerPackages` | `Context` | `isGranted()` | `ObserveHomeScreenStateUseCase`, `DrawerViewModel`, 3 settings ViewModels |
| `NotificationBadgeRepository` | in-memory `MutableStateFlow`s: counts by package, media packages, and the listable notifications | — | `badgeCounts: StateFlow`, `mediaNotificationPackages`, `notificationsFor(pkg, handle): Flow<List<NotificationInfo>>`, `setActiveNotifications(list)` | `FacetNotificationListenerService` (writer), `ObserveHomeScreenStateUseCase`, `DrawerViewModel` |
| `NotificationOpener` | `PendingIntent.send` of another app's notification content intent (with the background-start opt-in on API 34+) | `Context` | `open(contentIntent): Boolean` | `DrawerViewModel` |
| `CityRepository` (`data/places`) | `assets/cities.tsv` read once into a `CityList` (empty when the asset is missing) | `Context`, `@IoDispatcher` | `search(query, limit)`, `nearest(lat, lon, countryCode?, maxKm?)` | `ResolvePlaceUseCase`, `SearchPlacesUseCase` |
| `NotificationShadeRepository` | `StatusBarManager.expandNotificationsPanel` (reflection, `EXPAND_STATUS_BAR`) | `Context` | `expand()` | `HomeViewModel` |
| `PrivateSpaceRepository` | `UserManager` quiet-mode + `ACTION_PROFILE_*` broadcasts | `UserManager`, `AppRepository`, `Context` | `observePrivateSpaceState(): Flow<PrivateSpaceState>`, `observePrivateSpaceApps()`, `requestUnlock()` | `DrawerViewModel`, `PrivateSpaceViewModel` |
| `SecureFolderRepository` | `PackageManager.getLaunchIntentForPackage(Samsung Secure Folder)` | `Context` | `launchIntent(): Intent?` | `DrawerViewModel` |
| `SettingsRepository` | `DataStore<Preferences>` `facet_settings` | `DataStore<Preferences>` | `settings: Flow<LauncherSettings>`, 76 `suspend fun set*/reset*/mark*/update*/choose*/remove*` writers | 14 use cases, 18 ViewModels |
| `SystemSettingsRepository` | Static catalogue of `Settings.ACTION_*` intents + keyword aliases | `Context` | `search(query)` (IO) | `DrawerViewModel` |
| `UsageAccessRepository` | `AppOpsManager.unsafeCheckOpNoThrow(GET_USAGE_STATS)` | `AppOpsManager`, `Context` | `isGranted()` | `ObserveHomeScreenStateUseCase`, `ObserveFacetPreviewsUseCase`, `PermissionsViewModel`, `UsageAccessExplanationViewModel` |
| `UsageStatsRepository` | `UsageStatsManager.queryUsageStats` | `UsageStatsManager`, `AppRepository` | `getRecentApps(limit)`, `getMostUsedApps(limit)` | `ObserveHomeScreenStateUseCase`, `ObserveFacetPreviewsUseCase` |
| `WifiRepository` | `ConnectivityManager` Wi-Fi network callbacks (location info only for a named-network rule) + `WifiManager.scanResults` | `Context` | `observeWifiState(includeSsid): Flow<WifiState>`, `suspend nearbyNetworkNames(current)` (IO) | `DeviceStateRepository`, `RuleEditorViewModel` |
| `WakeEventsRepository` | System broadcasts `ACTION_SCREEN_ON`, `ACTION_USER_PRESENT`, `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED` (`RECEIVER_NOT_EXPORTED`) | `Context` | `observeWakeEvents(): Flow<Unit>` | `RunFacetAutomationUseCase` |
| `WallpaperRepository` (`open`) | `WallpaperManager.peekDrawable` / `getWallpaperColors` | `WallpaperManager` | `currentHomeWallpaper(): HomeWallpaper` (`Image` / `Tones` / `Unavailable`, downscaled to 1080px) | `FacetCarouselViewModel`, `AppearanceSettingsViewModel`, `AppDrawerSettingsViewModel`, `DockSettingsViewModel`, `HomeAppsListSettingsViewModel` |
| `AndroidPlaceGeocoder` (`data/places`, bound as `PlaceGeocoder`) | `android.location.Geocoder` (the listener API from Android 13, the blocking one before); empty answers when it is missing or fails | `Context`, `@IoDispatcher` | `reverse(lat, lon, locale)`, `search(query, locale, max)` | `ResolvePlaceUseCase`, `SearchPlacesUseCase` |
| `WeatherRepository` (`data/weather`) | The forecast: stored hourly rows and the fetch state, refreshed from MET Norway | `WeatherApi`, `WeatherStore`, `Clock` | `observeHours()`, `observeState()`, `refresh(place): RefreshOutcome` (one refresh at a time, through `WeatherRefreshPolicy`: never before `Expires`, never twice within 10 minutes, nothing while a 429 is being waited out; nothing is retried) | `RefreshWeatherUseCase`, `ObserveWeatherUseCase` |
| `MetWeatherApi` (`data/weather`, bound as `WeatherApi`) | HTTPS to `api.met.no` Locationforecast 2.0 `compact` | `Clock`, `@IoDispatcher` | `fetch(place, ifModifiedSince): WeatherApiResult` — the only code in the app that opens a connection | `WeatherRepository` |
| `RoomWeatherStore` (`data/weather`, bound as `WeatherStore`) | Room `weather_hours`, `weather_meta` | `FacetDatabase`, `WeatherDao` | `observeHours()`, `observeState()`, `readHours()`, `readState()`, `replaceHours(list)` (one transaction), `writeState(state)` | `WeatherRepository` |
| `WidgetPlacementRepository` | Room `widget_placements` | `WidgetPlacementDao` | `observeAll()`, `getById`, `upsert`, `deleteById` (thin pass-through) | `HubViewModel`, `HubWidgetPickerViewModel`, `BackupRestoreViewModel`, `ObserveHubStateUseCase`, `DeleteWidgetUseCase`, `ExportBackupUseCase` |
| `WorkProfileRepository` | `UserManager.userProfiles` + `isQuietModeEnabled` + `ACTION_MANAGED_PROFILE_*` | `UserManager`, `AppRepository`, `Context` | `observeWorkProfiles(): Flow<List<WorkProfileInfo>>` | `LauncherViewModel`, `SettingsViewModel` |

Three repositories are `open class` with `open fun` (`CalendarPermissionRepository`,
`NotificationAccessRepository`, `WallpaperRepository`) so instrumented tests can subclass them
(`androidTest/.../settings/FakeNotificationAccessRepository.kt`, `FakeCalendarPermissionRepository.kt`)
where `mockito-android` can't intercept a `final` class; every other repository is either
constructed over a hand-written DAO fake or mocked with Mockito 5 on the JVM.

### Repository → repository dependency graph

```mermaid
flowchart LR
    AR[AppRepository]
    FOR[FolderRepository] --> AR
    USR[UsageStatsRepository] --> AR
    WPR[WorkProfileRepository] --> AR
    PSR[PrivateSpaceRepository] --> AR
    AWR[AppWidgetRepository] --> AR
```

Every other repository depends only on framework services, DAOs, or `DataStore`. There are no
cycles; `AppRepository` is the single root.

## 2. Use cases (51) — unscoped, constructor-injected unless noted

| Use case | Kind | Injects | Injected by |
|---|---|---|---|
| `ActivateFacetByIdUseCase` | write, no-op guard (a facet that is missing or disabled on the free plan), `FacetSwitchSource` (manual re-samples the rules, then records baseline + suppresses active rules) | `SelectableFacetsUseCase`, `SettingsRepository`, `AutomationStateRepository`, `RefreshAutomationStateUseCase` | `LauncherViewModel` (deep link/shortcut ingestion, see [14](14-flow-deep-links-and-shortcuts.md)), `ManageFacetsViewModel`, `FacetCarouselViewModel`, `FacetSettingsViewModel` |
| `ApplyFacetAutomationUseCase` | one automation pass: refresh, then an `AUTOMATION` switch if the rules want a different facet | `RefreshAutomationStateUseCase`, `ActivateFacetByIdUseCase`, `SettingsRepository` | `RunFacetAutomationUseCase` |
| `CanUseTriggerUseCase` | pure Pro gate: `invoke(trigger, isPro)` (schedule free, device triggers Pro) and `entitledRuleIds(rules, isPro)` (free keeps the first two schedule rules in list order) | — | `RefreshAutomationStateUseCase`, `ObserveFacetAutomationUseCase`, `RuleEditorViewModel`, `SaveAutomationRuleUseCase` |
| `AddFacetUseCase` | write: adds a facet unless at the limit (3 free, 10 Pro); returns `AddFacetResult` (`Added` / `LimitReached`) | `FacetRepository`, `EntitlementRepository` | `ManageFacetsViewModel`, `FacetCarouselViewModel` |
| `AddToActiveListUseCase` | write: appends an app or folder to a list for the active facet, at the next position (apps and folders counted together) | `ResolveActivePlacementTargetUseCase`, `PlacementRepository`, `ObservePlacedItemsUseCase` | `DrawerViewModel` |
| `AssignCalendarColorsUseCase` | pure | — | `CalendarSettingsViewModel` |
| `CleanUpUninstalledAppsUseCase` | long-running collector | `AppRepository`, `PlacementRepository`, `FolderRepository` | `LauncherViewModel` |
| `CompactWidgetsUseCase` | pure grid | — | `HubViewModel` |
| `DeleteFacetUseCase` | write (releases the facet's clock widget id first) | `FacetRepository`, `AppWidgetRepository` | `ManageFacetsViewModel`, `FacetCarouselViewModel` |
| `DeleteWidgetUseCase` | write | `WidgetPlacementRepository`, `AppWidgetRepository` | `HubViewModel` |
| `EnsureActiveFacetUseCase` | seeds the first facet, and (`keepUsable()`, a collector) keeps the active facet one the user may use: missing or disabled → the first selectable facet | `FacetRepository`, `SettingsRepository`, `SelectableFacetsUseCase` | `LauncherViewModel` |
| `EvaluateFacetAutomationUseCase` | pure (rules + trigger truth + `AutomationState` → desired facet + new state) | — | `RefreshAutomationStateUseCase` |
| `ExportBackupUseCase` | one-shot read + file write | `SettingsRepository`, `FacetRepository`, `PlacementRepository`, `WidgetPlacementRepository`, `FolderRepository`, `BackupRepository` | `BackupRestoreViewModel` |
| `FlowPackWidgetsUseCase` | pure grid (lays widgets out in a fixed number of rows, in list order) | — | `LandscapeHubUseCase` |
| `GetInstalledAppsUseCase` | read (`invoke()` one-shot / `observe()` live) | `AppRepository` | `LauncherViewModel`, 4 picker/settings ViewModels, `SeedDefaultDockUseCase` |
| `GroupAppsByLetterUseCase` | pure (ICU `AlphabeticIndex`) | — | `DrawerListUseCases` (→ `AppDrawerScreen`) |
| `LandscapeHubUseCase` | pure (composes the three below: lays widgets out in the landscape Hub, and works out the new order after a drop) | `OrderWidgetsForLandscapeUseCase`, `FlowPackWidgetsUseCase`, `ReorderLandscapeWidgetUseCase` | `HubViewModel` |
| `ImportBackupUseCase` | destructive full-replace restore | `BackupRepository`, `SettingsRepository`, `FacetRepository`, `PlacementRepository`, `FolderRepository`, `RepairOrphanedProfileRowsUseCase` | `BackupRestoreViewModel` |
| `ObserveClockAccessoriesUseCase` | observe | `BatteryRepository`, `NextAlarmRepository` | `ObserveHomeScreenStateUseCase` |
| `ObserveFacetAutomationUseCase` | observe (`Flow<FacetAutomationScreenState>`: rule rows with facet names, "why Home looks like this" status, permission and Pro availability, `canAddRule`, `isPro`). Takes a refresh tick that re-reads permission grants | `AutomationRuleRepository`, `AutomationStateRepository`, `FacetRepository`, `SettingsRepository`, `AutomationPermissionRepository`, `EntitlementRepository`, `CanUseTriggerUseCase` | `FacetAutomationViewModel` |
| `ObserveFacetPreviewsUseCase` | observe (`Flow<Map<Long, FacetPreviewData>>`) | `FacetRepository`, `SettingsRepository`, `ObservePlacedItemsUseCase`, `UsageStatsRepository`, `UsageAccessRepository`, `CalendarPermissionRepository`, `CalendarRepository` | `FacetCarouselViewModel` |
| `ObserveWeatherUseCase` | observe (`Flow<WeatherState?>`): stored hours + fetch state + settings + a five-minute tick → now, the next 24 hourly points, local days from today, the rain summary, the place name and zone | `SettingsRepository`, `WeatherRepository`, `Clock` | `WeatherViewModel` |
| `ObserveHomeScreenStateUseCase` | observe (holds `refreshTrigger`, see F8) | 9 repositories + `ObservePlacedItemsUseCase` + `ObserveClockAccessoriesUseCase` | `HomeViewModel` |
| `ObserveHubStateUseCase` | observe (holds `refreshTrigger`) | `WidgetPlacementRepository`, `AppWidgetRepository` | `HubViewModel` |
| `ObserveAppNotificationsUseCase` | read, `flatMapLatest` over the `show_notifications_in_app_menu` setting; `AppNotificationsState(notifications, needsAccess)` | `SettingsRepository`, `NotificationBadgeRepository`, `NotificationAccessRepository` | `DrawerViewModel` |
| `ObservePlacedItemsUseCase` | observe: joins `PlacementRepository`'s raw Dock/Favorites rows against `AppRepository.observeInstalledApps()` and `FolderRepository.observeFolders()` (`apps`, `items`, batched `appsForFacets` / `itemsForFacets`); an uninstalled app drops out live | `PlacementRepository`, `FolderRepository`, `AppRepository` | `AddToActiveListUseCase`, `ObserveHomeScreenStateUseCase`, `ObserveFacetPreviewsUseCase`, `ObserveSettingsScreenStateUseCase`; 8 ViewModels (`DockAppPicker`, `FavoritesPicker`, `Onboarding`, `DockSettings`, `HomeAppsListSettings`, `AppearanceSettings`, `NotificationSettings`, `FacetSettings`) |
| `ObserveQuickAddStateUseCase` | pure (`forApp` / `forFolder`) | — | `HomeViewModel` |
| `ObserveSettingsScreenStateUseCase` | observe | `SettingsRepository`, `ObservePlacedItemsUseCase`, `FolderRepository` | `SettingsViewModel` |
| `OrderWidgetsForLandscapeUseCase` | pure (saved landscape order first, the rest in portrait reading order) | — | `LandscapeHubUseCase` |
| `PlaceWidgetUseCase` | pure grid (`Placed` / `HubFull`) | — | `HubWidgetPickerViewModel`, `BackupRestoreViewModel` |
| `ReorderLandscapeWidgetUseCase` | pure (moves one widget in the order, renumbers all) | — | `LandscapeHubUseCase` |
| `RankBySearchRelevanceUseCase` | pure | — | `DrawerViewModel`, `PrivateSpaceViewModel` (+ `AppDrawerScreen` via `DrawerListUseCases`) |
| `RecentlyInstalledAppsUseCase` | pure (filters `firstInstallTime` within 72h) | — | `AppDrawerScreen` (inline `remember`), `PrivateSpaceViewModel` |
| `RemoveFromActiveListUseCase` | write: removes an app or folder from a list for the active facet | `ResolveActivePlacementTargetUseCase`, `PlacementRepository` | `DrawerViewModel` |
| `ResolveActivePlacementTargetUseCase` | read: which concrete list the active facet reads `DOCK` or `FAVORITES` from (its own when `overrideDock` / `overridingFavorites`, else the launcher-wide one) | `SettingsRepository`, `FacetRepository` | `AddToActiveListUseCase`, `RemoveFromActiveListUseCase` |
| `RefreshWeatherUseCase` | write: one refresh of the weather for the current location (read, with a 5 km hysteresis, then named) or the chosen city, through the repository's gate; returns `WeatherRefreshResult` | `SettingsRepository`, `LocationRepository`, `WeatherRepository`, `ResolvePlaceUseCase` | `WeatherViewModel` (Home resume and the sheet's button) |
| `TurnOffWeatherUseCase` | write: weather off for the launcher default and every facet that shows it | `SettingsRepository`, `FacetRepository` | `WeatherLocationViewModel` (the leave-without-a-city dialog) |
| `ResolvePlaceUseCase` | read: name a position (`Geocoder`, else the nearest listed city as an offline "Near X") and find its timezone | `PlaceGeocoder`, `CityRepository` | `RefreshWeatherUseCase` |
| `RefreshAutomationStateUseCase` | samples every rule against the clock and persists the evaluator's state, without switching | `AutomationRuleRepository`, `AutomationStateRepository`, `SelectableFacetsUseCase`, `SettingsRepository`, `EvaluateFacetAutomationUseCase`, `DeviceStateRepository`, `AutomationPermissionRepository`, `EntitlementRepository`, `CanUseTriggerUseCase`, `Clock` | `ApplyFacetAutomationUseCase`, `ActivateFacetByIdUseCase` |
| `RepairOrphanedProfileRowsUseCase` | one-shot write, at startup and after every backup import | `AppRepository`, `PlacementRepository`, `FolderRepository` | `LauncherViewModel`, `ImportBackupUseCase` |
| `ResolveWidgetDropUseCase` | pure grid | — | `HubViewModel` |
| `ResolveWidgetResizeUseCase` | pure grid | — | `HubViewModel` |
| `RunFacetAutomationUseCase` | long-running collector (wake events, Home presses, rule, active-facet and entitlement changes → one pass each) | `ApplyFacetAutomationUseCase`, `AutomationRuleRepository`, `WakeEventsRepository`, `SettingsRepository`, `DeviceStateRepository`, `EntitlementRepository` | `LauncherViewModel` (see [15](15-flow-facet-automation.md)) |
| `SearchPlacesUseCase` | read: the city picker's search, geocoder results first then listed cities, de-duplicated within 10 km, each with an IANA zone | `PlaceGeocoder`, `CityRepository` | none yet (the location screen, step 6) |
| `SaveAutomationRuleUseCase` | write: Pro gate (trigger type, then the free rule limit for a new rule) before `AutomationRuleRepository.save()`; returns `SaveRuleResult` incl. `ProRequired` | `AutomationRuleRepository`, `CanUseTriggerUseCase` | `RuleEditorViewModel` |
| `SeedDefaultDockUseCase` | startup write (once, `defaults_seeded`) | `SettingsRepository`, `DefaultAppRepository`, `PlacementRepository`, `GetInstalledAppsUseCase` | `LauncherViewModel` |
| `SelectPreviewAppsUseCase` | pure | — | `HomeAppsListSettingsViewModel` |
| `SelectableFacetsUseCase` | the facets the user may switch to: all for Pro, the first 3 in list order for free (`FacetLimits.selectableIds`); `invoke()` ids and `observe()` entities | `FacetRepository`, `EntitlementRepository` | `ActivateFacetByIdUseCase`, `EnsureActiveFacetUseCase`, `RefreshAutomationStateUseCase`, `SyncFacetShortcutsUseCase` |
| `SortAppsForPickerUseCase` | pure + read (`LAST_USED` only) | `UsageStatsRepository` | `FavoritesPickerViewModel`, `DockAppPickerViewModel`, `FolderAppPickerViewModel` |
| `SyncFacetShortcutsUseCase` | long-running collector; publishes shortcuts only for selectable facets | `SelectableFacetsUseCase`, `FacetShortcutRepository` | `LauncherViewModel` (see [14](14-flow-deep-links-and-shortcuts.md)) |
| `SwitchFacetToNativeClockUseCase` | write (releases the clock widget id, then clears it) | `FacetRepository`, `AppWidgetRepository` | `ClockWidgetFacetController` |

Non-use-case files in `domain/`: `FlowCombine.kt` (6/7-ary `combine`), `HubGridConstants.kt`
(`HUB_COLUMNS`, `HUB_MAX_ROWS`, `HUB_MAX_WIDGETS`, `calculateHubCellWidth(context)` — see F4),
`BackupMapping.kt` (entity ⇄ `Backup*` DTO extension functions).

## 3. ViewModels (37) — `@HiltViewModel`, one per screen

| ViewModel | Screen / surface | Nav arg (`SavedStateHandle`) |
|---|---|---|
| `LauncherViewModel` | `LauncherActivity` (app lifetime) | — |
| `HomeViewModel` | `HomeScreen` | — |
| `DrawerViewModel` | `AppDrawerScreen` | — |
| `PrivateSpaceViewModel` | `PrivateSpaceScreen` | — |
| `HubViewModel` | `HubScreen` | — |
| `HubWidgetPickerViewModel` | `HubWidgetPickerScreen` | — |
| `FacetCarouselViewModel` | `FacetCarouselScreen` | — |
| `ManageFacetsViewModel` | `ManageFacetsScreen` | — |
| `FacetAutomationViewModel` | `FacetAutomationScreen` (rule list, status line) | — |
| `RuleEditorViewModel` | the rule editor sheet inside `FacetAutomationScreen`: draft rule, permission gate, device pickers | — |
| `FacetSettingsViewModel` | `FacetSettingsScreen` | `facetId` |
| `FavoritesPickerViewModel` | `FavoritesPickerScreen` | `facetId?` |
| `DockAppPickerViewModel` | `DockAppPickerScreen` | `facetId?` |
| `ClockWidgetPickerViewModel` | `ClockWidgetPickerScreen` | — |
| `ClockStyleGalleryViewModel` | `ClockStyleGalleryScreen` | `facetId?` |
| `WeatherViewModel` | the weather sheet over Home (`WeatherSheet`): `WeatherUiState`, refresh, `onHomeResumed` | — |
| `ClockAccessoriesViewModel` | `ClockAccessoriesScreen` (switches, icon style, location permission for the weather switch) | `facetId?` |
| `UnitsAndLocationViewModel` | `UnitsAndLocationScreen` (the four units, the location row and its permission) | — |
| `WeatherLocationViewModel` | `WeatherLocationScreen` (mode, permission, debounced city search) | — |
| `OnboardingViewModel` | `OnboardingScreen` | — |
| `SettingsViewModel` | `SettingsScreen` | — |
| `FacetProViewModel` | `FacetProScreen` (Settings → Facet Pro, and every Pro limit): Buy, Restore, the Play price and messages | `reason` (`ProReason`) |
| `AppearanceSettingsViewModel` | `AppearanceSettingsScreen`, `DockAppsAppearanceScreen` (both `facetId?`), `ThemesAndColorsScreen`, `FontsScreen`, `IconsScreen` (global) — one instance per back-stack entry | `facetId?` |
| `AppDrawerSettingsViewModel` | `AppDrawerSettingsScreen` (settings, plus the wallpaper behind its preview) | — |
| `DockSettingsViewModel` | `DockSettingsScreen` | `facetId?` |
| `HomeAppsListSettingsViewModel` | `HomeAppsListSettingsScreen` | `facetId?` |
| `CalendarSettingsViewModel` | `CalendarSettingsScreen` | `facetId?` |
| `NotificationSettingsViewModel` | `NotificationSettingsScreen` | — |
| `NotificationAccessExplanationViewModel` | `NotificationAccessExplanationScreen` | — |
| `UsageAccessExplanationViewModel` | `UsageAccessExplanationScreen` | — |
| `PermissionsViewModel` | `PermissionsScreen` | — |
| `FoldersSettingsViewModel` | `FoldersSettingsScreen` | — |
| `FolderDetailViewModel` | `FolderDetailScreen` | `folderId` |
| `FolderAppPickerViewModel` | `FolderAppPickerScreen` | `folderId` |
| `BackupRestoreViewModel` | `BackupRestoreScreen` | — |
| `WhatsNewViewModel` | `WhatsNewScreen` (opening it marks the running version's notes seen) | — |
