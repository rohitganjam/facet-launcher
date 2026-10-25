# 01 — High-Level Architecture & Layer Boundaries

Facet is a strictly layered MVVM app. The dependency direction is one-way, top to bottom;
state flows back up as `Flow`/`StateFlow`, never as callbacks or shared mutable objects.

## 1. The layers

```mermaid
flowchart TB
    subgraph UI["ui/ — Jetpack Compose (stateless composables)"]
        ACT["LauncherActivity\n@AndroidEntryPoint\ncomposition root"]
        NAV["FacetNavHost\nNavHost + hiltViewModel()"]
        SCR["Screen composables\nHomeScreen, AppDrawerScreen, HubScreen, SettingsScreen ..."]
        ACT --> NAV --> SCR
    end

    subgraph VM["ui/**/*ViewModel — @HiltViewModel (ViewModelComponent)"]
        VMS["ViewModels\nown MutableStateFlow of UiState\nemit one-off UiEvents"]
    end

    subgraph DOM["domain/ — use cases (unscoped, constructor-injected)"]
        UC["*UseCase\ncompose 1..n repositories\npure grid / ranking / grouping logic"]
    end

    subgraph DATA["data/ — @Singleton repositories"]
        REPO["*Repository\nthe only classes that touch DAOs, DataStore,\nor Android framework services"]
    end

    subgraph SRC["Sources"]
        DAO["data/local — Room DAOs\nFacetDatabase v32"]
        DS["DataStore of Preferences\nfacet_settings, facet_automation"]
        FW["Framework services\nLauncherApps, UserManager, AppWidgetManager,\nContentResolver, UsageStatsManager, WallpaperManager ..."]
    end

    SCR -- "events: onAppClick, onReorder, ..." --> VMS
    VMS -- "StateFlow via collectAsStateWithLifecycle()" --> SCR
    VMS --> UC
    VMS --> REPO
    UC --> REPO
    REPO --> DAO
    REPO --> DS
    REPO --> FW
```

### What each layer is allowed to know

| Layer | May depend on | Must not depend on | Verified example |
|---|---|---|---|
| Composables (`ui/**/*Screen.kt`, `ui/components`) | its `UiState`, event lambdas, `data/model` value types, `ui/theme` tokens | Repositories, DAOs, use cases | `HomeScreen(state: HomeScreenState, actions: HomeScreenActions)` |
| ViewModels | Use cases, Repositories | DAOs, `DataStore`, framework services | `HomeViewModel(ObserveHomeScreenStateUseCase, SettingsRepository, FacetRepository, ...)` |
| Use cases (`domain/`) | Repositories, other use cases, `data/model`, entities as value types | Android framework APIs (see finding F4) | `AddToActiveListUseCase(ResolveActivePlacementTargetUseCase, PlacementRepository)` |
| Repositories (`data/`) | DAOs, `DataStore`, framework services, **other repositories** (see F5) | ViewModels, composables | `FolderRepository(FolderDao, AppRepository)`; `PlacementRepository(AppPlacementDao, FolderPlacementDao)` |
| DAOs / entities (`data/local`) | Room, `data/model` enums via `Converters` | anything above | `AppPlacementDao.observe(list, ownerKey): Flow<List<AppPlacementEntity>>` |

Boundary check results (grep over `app/src/main`):

- **0** ViewModels import a DAO. ✅
- **0** composables inject or call a Repository. ✅ (The dock size cap lives in `AppListLimits.MAX_DOCK_APPS`, so no composable imports a repository type for a constant any more.)
- **0** composables construct a use case. ✅ (`AppDrawerScreen` takes its ranking/grouping/recents use cases as a `listUseCases` parameter, passed from `DrawerViewModel.listUseCases`; the parameter default only serves previews and tests.)
- **5** `domain/` files import `android.*` — see F4.

## 2. Unidirectional data flow, concretely

```mermaid
flowchart LR
    U((User)) -- gesture --> C[Composable]
    C -- "onX() lambda" --> V[ViewModel]
    V -- "viewModelScope.launch { useCase() }" --> D[UseCase / Repository]
    D -- "suspend write → Room / DataStore" --> DB[(facet.db / DataStore)]
    DB -- "Flow re-emits" --> D
    D -- "combine / map" --> V
    V -- "_uiState.value = ..." --> C
```

Concrete conventions the code actually follows:

- **State is a single immutable `data class`** per screen (`HomeUiState`, `HubUiState`,
  `SettingsUiState`, `BackupRestoreUiState`, ...), exposed as `StateFlow<XUiState>` from a private
  `MutableStateFlow`. `HomeViewModel` and `LauncherViewModel` build theirs with
  `combine(...).onEach { _uiState.value = it }.launchIn(viewModelScope)`, so their upstream flows stay
  collected for the ViewModel's whole lifetime (see F7); most other ViewModels expose
  `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`, which stops collecting
  when nothing observes the screen.
- **Writes are fire-and-forget suspend calls** launched in `viewModelScope`; the UI never awaits
  them — it sees the result when Room/DataStore re-emits.
- **One-off events** use `MutableSharedFlow(extraBufferCapacity = 1)` (e.g.
  `LauncherViewModel.homePressedEvent`, threaded to Home via `CompositionLocal LocalHomePressedEvent`;
  sheets and dialogs subscribe through `DismissOnHomePress` — see `12 §4`).
- **Threading**: Room and DataStore are main-safe; every blocking framework call is wrapped in
  `withContext(Dispatchers.IO)` (ContentResolver queries, file I/O) or `Dispatchers.Default`
  (`LauncherApps.getActivityList` + parallel icon decode via `async`/`awaitAll`).

## 3. Hilt: components, scopes, and modules

```mermaid
flowchart TB
    subgraph SC["SingletonComponent — lives with FacetApplication (@HiltAndroidApp)"]
        direction TB
        subgraph MODS["@Module @InstallIn(SingletonComponent) — data/di/"]
            AM["AppModule\nClock, LauncherApps, UserManager,\nUsageStatsManager, AppOpsManager,\nWallpaperManager, ContentResolver\n(all @Singleton)"]
            DBM["DatabaseModule\nFacetDatabase @Singleton\n+ 7 DAO @Provides (unscoped)"]
            DSM["DataStoreModule\n3 x DataStore of Preferences @Singleton\n('facet_settings', 'facet_automation' via @AutomationDataStore, 'facet_entitlement' via @EntitlementDataStore)"]
            WM["WidgetModule\nAppWidgetManager, LauncherAppWidgetHost\n(@Singleton)"]
            DPM["DispatcherModule\n@IoDispatcher, @DefaultDispatcher\n(CoroutineDispatcher)"]
            ENM["EntitlementModule\n@Binds PlayEntitlementRepository\n+ 2 @BindsOptionalOf hooks"]
            WTM["WeatherModule\n@Binds MetWeatherApi to WeatherApi\n@Binds RoomWeatherStore to WeatherStore\n@Binds AndroidPlaceGeocoder to PlaceGeocoder"]
        end
        REPOS["47 @Singleton data classes (42 repositories)\n@Singleton class X @Inject constructor(...)\n(no module — constructor injection)"]
        MODS --> REPOS
    end

    subgraph VMC["ViewModelComponent — one per ViewModel instance"]
        UCS["Use cases\nclass X @Inject constructor(...)\nunscoped: new instance per injection"]
        VMS["32 @HiltViewModel ViewModels\nSavedStateHandle for nav args"]
        UCS --> VMS
    end

    subgraph EP["Entry points"]
        ACT["LauncherActivity\n@AndroidEntryPoint\n@Inject lateinit var launcherApps\n@Inject lateinit var clockWidgetHost (previews)"]
        SVC["FacetNotificationListenerService\n@AndroidEntryPoint\n@Inject lateinit var notificationBadgeRepository"]
    end

    REPOS --> UCS
    REPOS --> VMS
    REPOS --> SVC
    SC --> ACT
    VMC -- "hiltViewModel() in FacetNavHost\nby viewModels() in LauncherActivity" --> ACT
```

### Scope table

| What | Scope | How it's bound | Why it matters |
|---|---|---|---|
| Framework services (`LauncherApps`, `UserManager`, `AppWidgetManager`, ...) | `@Singleton` | `@Provides` in `AppModule` / `WidgetModule`, `?: error(...)` if the service is missing | Repositories get real system services injected, so tests swap them with fakes/Robolectric. |
| `CoroutineDispatcher` (`@IoDispatcher`, `@DefaultDispatcher`) | unscoped | `DispatcherModule` provides `Dispatchers.IO` / `Dispatchers.Default`; repositories take them as trailing constructor params defaulting to the same value | Tests pass a `StandardTestDispatcher(testScheduler)` so `withContext` hops run deterministically under `runTest`. |
| `java.time.Clock` | `@Singleton` | `AppModule.provideClock()` | Time-dependent logic (`NextAlarmRepository`) is testable with a fixed clock. |
| `FacetDatabase` | `@Singleton` | `DatabaseModule` — `Room.databaseBuilder(..., "facet.db").addMigrations(*Migrations.ALL).fallbackToDestructiveMigrationOnDowngrade(true)` | One DB instance; **no** destructive fallback on upgrade (a missing migration crashes loudly by design). |
| DAOs (7) | unscoped | `@Provides fun provideXDao(db) = db.xDao()` | Room caches DAO instances internally, so unscoped is free. |
| `DataStore<Preferences>` (3) | `@Singleton` | `DataStoreModule` via `preferencesDataStore(name = "facet_settings")` (unqualified) and `preferencesDataStore(name = "facet_automation")` (`@AutomationDataStore`) and `preferencesDataStore(name = "facet_entitlement")` (`@EntitlementDataStore`) delegates | DataStore requires exactly one instance per file. Automation state lives in its own file so it stays out of `LauncherSettings` re-emissions and backups. |
| `EntitlementRepository` | `@Singleton` | `EntitlementModule` `@Binds` `PlayEntitlementRepository`; two `@BindsOptionalOf` hooks (`EntitlementOverride`, `DebugSettingsEntry`) | The debug source set (`DebugBindingsModule`) fills the optional hooks; a release build leaves them empty, so the override and its Settings row exist only in debug. The benchmark source set provides an `EntitlementOverride` that always forces Pro (a measurement build, never shipped). |
| `WeatherApi`, `WeatherStore`, `PlaceGeocoder` | `@Singleton` | `WeatherModule` `@Binds` `MetWeatherApi`, `RoomWeatherStore` and `AndroidPlaceGeocoder` | The seams the weather code is tested through: tests pass a fake API, an in-memory store and a fake geocoder, so no test touches the network. |
| Repositories (42; with `NotificationOpener`, `NotificationActionController`, `MetWeatherApi`, `RoomWeatherStore` and `AndroidPlaceGeocoder`, 47 `@Singleton` classes in `data/`) | `@Singleton` | Constructor injection, no module | They hold `callbackFlow` registrations and in-memory state (`NotificationBadgeRepository.badgeCounts`); a single instance is required for that state to be shared. |
| Use cases | unscoped | Constructor injection | Stateless by convention; `ObserveHomeScreenStateUseCase` is the one exception (holds a `refreshTrigger` `MutableSharedFlow`) — see F8. |
| ViewModels (36) | `ViewModelComponent` | `@HiltViewModel`, resolved by `hiltViewModel()` per `NavHost` destination or `by viewModels()` in the Activity | Nav-arg-driven screens (`FacetSettingsViewModel`, `FolderDetailViewModel`, ...) read ids from `SavedStateHandle`. |

Apart from `EntitlementModule` and `WeatherModule`, there are **no interface-to-implementation `@Binds` modules**: every repository is a concrete
`@Singleton class`, and tests substitute them with hand-written DAO fakes (`FolderTestFakes.kt`)
or Mockito 5 mocks of the concrete repository (inline mock maker, so `final` Kotlin classes mock
fine) rather than via Hilt test modules — see [06-testing.md](06-testing.md).

## 4. Composition root and app lifetime

`LauncherActivity.onCreate` → `setContent` renders exactly one of three top-level branches from
`LauncherViewModel.uiState`:

1. `isLoading` → an empty `Box` (wallpaper shows through; avoids a default-themed first frame).
2. `!onboardingCompleted` → `OnboardingScreen`, outside the nav graph.
3. otherwise → `FacetNavHost(apps, onAppClick, launcherViewModel)`.

`LauncherViewModel` is the app-lifetime orchestrator. Its `init` block:

- collects `combine(GetInstalledAppsUseCase.observe(), SettingsRepository.settings, WorkProfileRepository.observeWorkProfiles())` into `LauncherUiState` (theme, font, icon mode, onboarding flag, live app list, work profiles);
- runs `EnsureActiveFacetUseCase.keepUsable()` (creates a facet if none exists and keeps `activeFacetId` valid);
- keeps `CleanUpUninstalledAppsUseCase` collecting for the process lifetime (deletes placement rows on real uninstall broadcasts);
- runs `RepairOrphanedProfileRowsUseCase` once (backfills `userId = -1` rows left by pre-profile schemas);
- runs `SeedDefaultDockUseCase` on fresh installs;
- keeps `SyncFacetShortcutsUseCase` and `RunFacetAutomationUseCase` collecting for the process lifetime (dynamic shortcuts; automation rules);
- clears `isLoading` after a 3 s timeout so a slow first app scan can't leave the screen blank.

`FacetNavHost` owns 27 string-route destinations (`FacetDestinations`); `Home ⇄ Drawer` is *not* a
nav transition — `HomeDrawerRoute` handles it as a follow-finger gesture inside the `HOME`
destination, while `Hub`/`Facets`/settings screens are ordinary destinations.
