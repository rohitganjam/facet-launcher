# 04 — Directory & Package Structure Map

Single Gradle module (`:app`), root package `com.facetlauncher.app`. File counts are from the
current tree; the "belongs here if…" column is the placement rule the code actually follows.

```
app/src/main/kotlin/com/facetlauncher/app/
├── FacetApplication.kt            @HiltAndroidApp
├── LauncherActivity.kt            @AndroidEntryPoint — the only Activity; composition root
│
├── data/                          (63)  @Singleton Repositories — the only layer that touches
│   │                                    Room, DataStore, or Android framework services
│   ├── di/                        (9)   Hilt modules: AppModule (also the @ApplicationScope qualifier), DatabaseModule, DataStoreModule, DispatcherModule (@IoDispatcher / @DefaultDispatcher), WidgetModule, EntitlementModule, WeatherModule + the @AutomationDataStore and @EntitlementDataStore qualifiers
│   ├── icon/                      (4)   The glyph-only and outline icon pictures: IconMasks and IconStyler (pure pixel code), IconLayers (reads an app's themed monochrome layer), StyledIconCache (makes them on demand, off the main thread)
│   ├── iconpack/                  (4)   Installed icon packs: AppFilter (reads appfilter.xml, asset or resource), IconPackIcons + ResourceIconPack (a pack's icons and its themed fallback, ≤256px), IconPackRepository (finds packs, loads and caches them, and one app's icon or a pack's catalog for custom icons), PackCatalog (reads drawable.xml for browsing)
│   ├── local/                     (28)  Room: FacetDatabase, 14 entities in 11 files (WeatherEntities.kt holds two, LaunchHistoryEntities.kt three), 10 DAOs, Converters, Migrations (+ ClockConfigMigration and FacetDefaultsMigration, the DataStore reads three migrations need), FacetMapping, PlacementCells
│   ├── places/                    (4)   Place names and cities: CityRepository over the bundled CityList (assets/cities.tsv), PlaceGeocoder + AndroidPlaceGeocoder (the device Geocoder), GeoDistance
│   ├── weather/                   (7)   The weather forecast: MetWeatherApi (the only network code), MetForecastParser, WeatherRefreshPolicy (the refresh gate), WeatherRepository, WidgetWeatherRepository (a Weather widget's own forecast), WeatherStore + RoomWeatherStore (see 18)
│   ├── model/                     (64)  Immutable value types & enums shared by every layer
│   │                                    (AppInfo, PlacedItem, PlacementList, PlacementTarget, LauncherSettings, AppProfile, Clock*Option,
│   │                                    FacetDeepLink's build/parse pair, …)
│   ├── widget/                    (3)   AppWidgetRepository + LauncherAppWidgetHost (AppWidgetHost subclass) + WidgetConfigureActivity (invisible trampoline that opens a widget's configure activity)
│   └── FacetNotificationListenerService.kt   @AndroidEntryPoint service feeding NotificationBadgeRepository (its component is also what MediaSessionRepository reads sessions through)
│
├── domain/                        (98)  82 *UseCase classes in 80 files (NativeWidgetConfigUseCases.kt and LandscapeWidgetOrder.kt hold two each) + 18 helper files: FlowCombine, ScreenCover, HubGridConstants, NativeWidgetSizing, NativeWidgetOverrideResolver, BackupMapping, BackupUpgrade, AutomationRuleBackupMapping, ClockConfigResolution, ResolvedFacetStyle, HomePlacement, HomeGridPlacement, WeatherUnits, WeatherDay, WeatherCondition, RainSummary, PortraitCrop, DailyWallpaperCoordinator
│                                        Composes ≥1 repositories, or pure logic (grid placement, ranking, grouping)
│
└── ui/
    ├── theme/                     (12)  Color, Type, Theme, SettingsTypography, Motion, ClockFonts/Colors, ClockAlignment, AccentSwatch, ThemeLocals,
    │                                    PrivateSpaceTheme, SystemBars (status/nav icon color) — design tokens; FacetLauncherTheme wrapper
    ├── pro/                       (7)   FacetProScreen (before purchase) / FacetProUnlocked (after) / FacetProViewModel, ProFeature (the rows both screens list), ProComponents (ink panel, PRO mark, facet fans, trigger pills, gradient button), ProSettingsCard (Settings), previews
    ├── appicon/                   (7)   Custom app icons ([22](22-flow-custom-icons.md)): AppIconEditor (+ LocalAppIconEditor) / AppIconEditorViewModel, ChangeIconPage and BrowsePackPage (pages of the app menu), CustomIconsScreen / CustomIconsViewModel (Settings → Icons → Custom icons)
    ├── components/                (63)  Reusable, screen-agnostic composables (AppIcon, ConfirmDialog,
    │                                    DragReorderState, HomeSurfacePreview, ThemedDropdownMenu, ThemedModalBottomSheet, DismissOnHomePress,
    │                                    AppPickerScreen, FolderContentsSheet, the settings kit — SettingsCard,
    │                                    SettingsSectionHeader, SettingsIconBadge — and the buttons PrimaryButton,
    │                                    TonalButton, SurfaceButton, TextActionButton, …)
    ├── navigation/                (1)   FacetNavHost + FacetDestinations (34 routes) + popBackStackSafely
    ├── launcher/                  (11)  CalendarEventOpener, LauncherViewModel (app-lifetime state), HomeDrawerRoute (wires Home and the panels over it), HomeSwipeController +
    │                                    HomeSwipeGestures (the three swipe axes, settle rules, drag detector), HomeOverlays / HomeAppDrawerOverlay / HomeOverlayState
    │                                    (the Hub, drawer, Private Space, pickers, carousel and one-time prompts), HomeScreenActionsBuilder, LauncherLocals
    ├── home/                      (44)  HomeScreen (thin dispatcher) / HomeFrame / HomeContent / HomeSlots / HomeScreenEffects / HomeClockGeometry / HomeScreenLayout (measurements) / ClockAdjustControls / ClockResizeOverlays / HomeScreenState (its state and action groups) / HomeViewModel / HomeUiState, ClockBlock, clock handles, HomeSheets /
    │                                    ClockAdjustToolbar / `HomeMenuMode`, hosted-clock-widget controllers/tile, `ClockStackPager` (`ClockPagerState` and the page dots), `ClockWidgetHostCache`, TimeTick, MediaPlayerRow, `ScaledHomeScreen` (the real Home scaled down, for previews), `HomeScreenStateBuilder` and `HomeTags`;
    │                                    the list and tiles `HomeAppList` (two-column list, column, grid), `AppRow`, `FolderRow`, `DockIcon`, with `HomeItemSpec`
    │                                    (`HomeItemActions` / `HomeItemStyle` / `HomeAppListContent`, the one parameter group they share);
    │                                    landscape: `HomeLandscapeLayout`, `HomeLandscapeGeometry`, `HomeClockHandleGeometry`, `HomeLayoutMode`; `ClockPlacement`/`ListAlignment` (now in `domain/HomePlacement.kt`)
    │   ├── clock/                 (9)   Clock templates, accessory row/icons, ClockWeatherAccessory (the weather item and `LocalClockWeather`), calendar strip, ClockStyleGallery screen+VM
    │   ├── weather/               (11)  The weather sheet over Home: WeatherSheet (+ WeatherSheetParts, RainSummaryText), WeatherViewModel, ClockWeatherAccessoryState, and the formatting and icon helpers (WeatherFormat, WeatherConditionText, WeatherIconKind, WeatherIconColors, WeatherCompass, WindArrow)
    │   ├── stack/                 (2)   ClockStackScreen / ClockStackViewModel (the clock stack: reorder, Add, remove, per-page size)
    │   └── widget/                (3)   ClockWidgetPickerScreen / ClockWidgetPickerViewModel / ClockWidgetPickerTarget (custom clock widget bind flow; Replace a page or Add one)
    ├── vault/                     (2)   AppVaultScreen (stateless: intro, PIN pad, list, Add apps picker), AppVaultViewModel — the App Vault; its overlay is `launcher/HomeAppVaultOverlay`
    ├── drawer/                    (14)  AppDrawerScreen / DrawerViewModel, AlphabetRail, ContactConnectionsSheet,
    │                                    PrivateSpaceScreen / PrivateSpaceViewModel; the drawer's own parts: DrawerBrowseModel (the browse list,
    │                                    built in composition), DrawerListUseCases (the use cases it is handed), DrawerParams, DrawerTab,
    │                                    DrawerGridLayout, DrawerRailTarget (rail selection → list index), DrawerSearchSheet, SearchResultTiles
    ├── nativewidget/              (9)   The widgets Facet draws itself ([20](20-flow-native-widgets.md)): `NativeWidget` (size → tier → content), `WeatherNativeWidget`, `AgendaNativeWidget`, `NowPlayingNativeWidget`, `NativeWidgetState` (state, actions, Hub environment), `NativeWidgetAccess` (+ `NativeWidgetAccessViewModel`), `NativeWidgetPickerSection`, `LongPressFallback`, previews
    │   └── config/                (2)   NativeWidgetConfigSheet / NativeWidgetConfigViewModel (a widget's own settings)
    ├── hub/                       (13)  HubScreen / HubViewModel / HubUiState, HubGrid, widget tiles, resize/grab gestures
    │   └── picker/                (3)   HubWidgetPickerScreen / ViewModel / UiState
    ├── facets/                    (9)   FacetCarousel, FacetSettings, ManageFacets, FavoritesPicker (screen + VM each)
    ├── dock/                      (2)   DockAppPickerScreen / DockAppPickerViewModel
    ├── onboarding/                (10)  OnboardingScreen / ViewModel / UiState + 5 pages (Intro, HomeSetup, DrawerSetup, Facets, Notifications), dots, SetDefaultLauncherSheet
    └── settings/                  (67)  25 `*Screen.kt` and 23 `*ViewModel.kt` — most settings screens are an `XScreen.kt` + `XViewModel.kt` pair; Appearance is a hub of four screens (`AppearanceSettingsScreen`, `ThemesAndColorsScreen`, `FontsScreen`, `IconsScreen`) sharing `AppearanceSettingsViewModel` (`IconsScreen` adds `IconsViewModel` for its style and icon pack cards) and the `AppearancePreviewCard` wrapper (`AppearancePreview.kt`) over `ui/home/ScaledHomeScreen`
        ├── automation/            (13)  Facet automation: FacetAutomationScreen / ViewModel (rule list), AutomationStrips, RuleEditorSheet / ViewModel / State (draft rule, permission gate), ScheduleFields, TriggerFields, EditorControls, UpgradeSheet, ProPill, PermissionRequest, formatting, previews (`ProPill` lives in `components/`; the Facet Pro screen they open is in `pro/`)
        └── backup/                (3)   BackupRestoreScreen / ViewModel / UiState
```

## Placement rules (derived from the code)

| If the file is… | It goes in | Naming | Example |
|---|---|---|---|
| A class that wraps a Room DAO, `DataStore`, or an Android system service | `data/` | `XRepository` | `CalendarRepository`, `WallpaperRepository` |
| A Room `@Entity`, `@Dao`, `Migration`, or `@TypeConverter` | `data/local/` | `XEntity`, `XDao` | `AppPlacementEntity`, `AppPlacementDao` |
| A Hilt `@Module` | `data/di/` | `XModule` | `DatabaseModule` |
| An immutable value type or enum used across layers | `data/model/` | plain noun | `AppInfo`, `PlacedItem`, `ClockDateStyle` |
| Logic that spans more than one repository, or a pure algorithm | `domain/` | `XUseCase` with `operator fun invoke` / `observe()` | `AddToActiveListUseCase`, `ResolveWidgetDropUseCase` |
| Owns screen state and is the composable's only collaborator | `ui/<surface>/` | `XViewModel` (`@HiltViewModel`) | `HubViewModel` |
| The immutable state a screen renders | `ui/<surface>/` | `XUiState` (data class) | `HomeUiState`, `BackupRestoreUiState` |
| A screen-level composable | `ui/<surface>/` | `XScreen` | `AppDrawerScreen` |
| A composable used by ≥2 surfaces | `ui/components/` | descriptive noun | `AppContextMenu`, `ScreenHeader` |
| A colour/typography/motion token | `ui/theme/` | — | `Color.kt`, `Motion.kt` |
| A `NavHost` route | `ui/navigation/FacetNavHost.kt` | `FacetDestinations.X` constant | `FOLDER_DETAIL = "folderDetail/{folderId}"` |

`app/src/debug/` holds debug-only code (the entitlement override and its Settings row), reached through optional Hilt bindings so a release build contains none of it; its tests live in `app/src/testDebug/`. `app/src/benchmark/` is the benchmark build's own: a Pro override (so a run can have more than three facets) and `BenchmarkSeedReceiver`, which the `:benchmark` module broadcasts to finish onboarding and set up N facets; it is never in a release build.

Settings screens are the main exception to "one package per surface": all of them live flat in
`ui/settings/` (only `backup/` and `automation/` have their own sub-package). Clock styling is split between
`ui/home/clock/` (rendering + gallery) and `data/model/Clock*Option.kt` (choices).

## Test tree mirror

Both test source sets mirror `main`'s packages; the class under test and its test share a package.

```
app/src/test/kotlin/com/facetlauncher/app/          JVM (JUnit4 + Robolectric + coroutines-test)
├── data/          52   one *RepositoryTest for most repositories (46 test classes) + shared fakes (FolderTestFakes.kt — FakeFolderDao, FakeAppPlacementDao, FakeFolderPlacementDao, …)
│   ├── icon/       3   IconMasksTest, IconStylerTest (JVM, no Android), StyledIconCacheTest (Robolectric)
│   ├── iconpack/   3   AppFilterTest (pull parser and text scan must agree), IconPackRepositoryTest (pack discovery, clearing an uninstalled pack), PackCatalogTest (both drawable.xml readers, search, appfilter fallback)
│   ├── local/      8   Converters, entities, DAO-level behaviour via in-memory Room (AppPlacementDaoTest, FolderPlacementDaoTest, FolderDaoTest, FacetDaoTest, AutomationRuleDaoTest, …)
│   ├── model/     20   IconOverrideTest (matching custom icons to apps), FacetLimitsTest, FacetDeepLinkTest (build/parse round-trip + rejection cases), AutomationStateTest, AutomationTriggerTest (schedule windows and battery levels), AutomationRuleValidationTest, AutomationLimitsTest, DeviceStateTest (every trigger's truth)
│   ├── places/     3
│   ├── weather/    6
│   └── widget/     2
├── domain/        76   one test class per use case or helper, for most (pure logic — no Android needed for most; the ones without are listed in 06 §5)
└── ui/            98   ViewModel tests (dock, drawer, facets, home, hub, launcher, onboarding, pro, settings×9, settings/automation, settings/backup, theme) the rule editor state/formatting tests, and `ClockWidgetTouchGateTest` (Robolectric, raw `MotionEvent`s)

app/src/androidTest/kotlin/com/facetlauncher/app/   Instrumented (Compose UI tests, AVD only)
├── data/           2   MediaSessionRepositoryTest + InstalledAppsTestSupport
├── data/local/     1   FacetDatabaseMigrationTest (MigrationTestHelper over app/schemas)
└── ui/           105   one *ScreenTest per screen (appicon: CustomIconsScreenTest) + component/route/theme tests (settings/automation: FacetAutomationScreenTest, RuleEditorSheetTest) and shared fakes
```

Rule of thumb from `CLAUDE.md`, and what the tree shows in practice: everything in `data/` and
`domain/` is covered on the JVM; composables are covered on the emulator; ViewModels are covered on
the JVM by feeding fake repositories through their constructors (no Hilt in tests).

## Non-code directories

| Path | Purpose |
|---|---|
| `app/schemas/com.facetlauncher.app.data.local.FacetDatabase/1..49.json` | Exported Room schemas; the migration test depends on them |
| `app/proguard-rules.pro` | Release minification rules |
| `scripts/release.sh` | The only sanctioned way to build a release (bumps version first) |
| `graphify-out/` | Knowledge graph of the repo (`graphify query`, `graphify path`) |
| `Android launcher design planning/design_handoff_minimal_launcher/` | PRD + design spec the UI is built from |
| `IMPLEMENTATION_PLAN.md` | Task tracker; also the log of bugs found by the instrumented suite |
| `docs/architecture/` | This blueprint |
