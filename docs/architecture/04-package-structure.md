# 04 — Directory & Package Structure Map

Single Gradle module (`:app`), root package `com.facetlauncher.app`. File counts are from the
current tree; the "belongs here if…" column is the placement rule the code actually follows.

```
app/src/main/kotlin/com/facetlauncher/app/
├── FacetApplication.kt            @HiltAndroidApp
├── LauncherActivity.kt            @AndroidEntryPoint — the only Activity; composition root
│
├── data/                          (48)  @Singleton Repositories — the only layer that touches
│   │                                    Room, DataStore, or Android framework services
│   ├── di/                        (7)   Hilt modules: AppModule, DatabaseModule, DataStoreModule, WidgetModule, EntitlementModule + the @AutomationDataStore and @EntitlementDataStore qualifiers
│   ├── local/                     (16)  Room: FacetDatabase, 7 entities, 6 DAOs, Converters, Migrations
│   ├── model/                     (43)  Immutable value types & enums shared by every layer
│   │                                    (AppInfo, PlacedItem, PlacementList, PlacementTarget, LauncherSettings, AppProfile, Clock*Option,
│   │                                    FacetDeepLink's build/parse pair, …)
│   ├── widget/                    (2)   AppWidgetRepository + LauncherAppWidgetHost (AppWidgetHost subclass)
│   └── FacetNotificationListenerService.kt   @AndroidEntryPoint service feeding NotificationBadgeRepository (its component is also what MediaSessionRepository reads sessions through)
│
├── domain/                        (44)  41 *UseCase classes + FlowCombine.kt, HubGridConstants.kt, BackupMapping.kt
│                                        Composes ≥1 repositories, or pure logic (grid placement, ranking, grouping)
│
└── ui/
    ├── theme/                     (11)  Color, Type, Theme, Motion, ClockFonts/Colors, ClockAlignment, AccentSwatch, ThemeLocals,
    │                                    PrivateSpaceTheme, SystemBars (status/nav icon color) — design tokens; FacetLauncherTheme wrapper
    ├── pro/                       (6)   FacetProScreen (before purchase) / FacetProUnlocked (after) / FacetProViewModel, ProComponents (ink panel, PRO mark, facet fans, trigger pills, gradient button), ProSettingsCard (Settings), previews
    ├── components/                (43)  Reusable, screen-agnostic composables (AppIcon, ConfirmDialog,
    │                                    DragReorderState, HomeSurfacePreview, ThemedDropdownMenu, ThemedModalBottomSheet, DismissOnHomePress,
    │                                    AppPickerScreen, FolderContentsSheet, the settings kit — SettingsCard,
    │                                    SettingsSectionHeader, SettingsIconBadge — and the buttons PrimaryButton,
    │                                    TonalButton, SurfaceButton, TextActionButton, …)
    ├── navigation/                (1)   FacetNavHost + FacetDestinations (27 routes) + popBackStackSafely
    ├── launcher/                  (3)   LauncherViewModel (app-lifetime state), HomeDrawerRoute (Home⇄Drawer gesture
    │                                    surface), LauncherLocals (CompositionLocals)
    ├── home/                      (16)  HomeScreen / HomeViewModel / HomeUiState, ClockBlock, clock handles, ClockAdjustSheet /
    │                                    ClockAdjustToolbar, hosted-clock-widget controllers/tile, TimeTick, MediaPlayerRow, shared scaled `HomePreview` + `HomePreviewState`
    │   ├── clock/                 (7)   Clock templates, accessory row/icons, calendar strip, ClockStyleGallery screen+VM
    │   └── widget/                (2)   ClockWidgetPickerScreen / ClockWidgetPickerViewModel (custom clock widget bind flow)
    ├── drawer/                    (6)   AppDrawerScreen / DrawerViewModel, AlphabetRail, ContactConnectionsSheet,
    │                                    PrivateSpaceScreen / PrivateSpaceViewModel
    ├── hub/                       (11)  HubScreen / HubViewModel / HubUiState, HubGrid, widget tiles, resize/grab gestures
    │   └── picker/                (3)   HubWidgetPickerScreen / ViewModel / UiState
    ├── facets/                    (9)   FacetCarousel, FacetSettings, ManageFacets, FavoritesPicker (screen + VM each)
    ├── dock/                      (2)   DockAppPickerScreen / DockAppPickerViewModel
    ├── onboarding/                (8)   OnboardingScreen / ViewModel / UiState + 3 pages, dots, SetDefaultLauncherSheet
    └── settings/                  (36)  17 `*Screen.kt` and 14 `*ViewModel.kt` — most settings screens are an `XScreen.kt` + `XViewModel.kt` pair; Appearance is a hub of three screens (`AppearanceSettingsScreen`, `DockAppsAppearanceScreen`, `FontsThemesIconsScreen`) sharing `AppearanceSettingsViewModel` and the `AppearancePreviewCard` wrapper (`AppearancePreview.kt`) over `ui/home/HomePreview`
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

`app/src/debug/` holds debug-only code (the entitlement override and its Settings row), reached through optional Hilt bindings so release and benchmark builds contain none of it; its tests live in `app/src/testDebug/`.

Settings screens are the main exception to "one package per surface": all of them live flat in
`ui/settings/` (only `backup/` and `automation/` have their own sub-package). Clock styling is split between
`ui/home/clock/` (rendering + gallery) and `data/model/Clock*Option.kt` (choices).

## Test tree mirror

Both test source sets mirror `main`'s packages; the class under test and its test share a package.

```
app/src/test/kotlin/com/facetlauncher/app/          JVM (JUnit4 + Robolectric + coroutines-test)
├── data/          39   one *RepositoryTest per repository + shared fakes (FolderTestFakes.kt — FakeFolderDao, FakeAppPlacementDao, FakeFolderPlacementDao, …)
│   ├── local/      7   Converters, entities, DAO-level behaviour via in-memory Room (AppPlacementDaoTest, FolderPlacementDaoTest, FolderDaoTest, FacetDaoTest, AutomationRuleDaoTest, …)
│   ├── model/      8   FacetLimitsTest, FacetDeepLinkTest (build/parse round-trip + rejection cases), AutomationStateTest, AutomationTriggerTest (schedule windows and battery levels), AutomationRuleValidationTest, AutomationLimitsTest, DeviceStateTest (every trigger's truth)
│   └── widget/     1
├── domain/        38   one test per use case (pure logic — no Android needed for most)
└── ui/            48   ViewModel tests (dock, drawer, facets, home, hub, launcher, onboarding, settings×9, settings/automation, settings/backup, theme) the rule editor state/formatting tests, and `ClockWidgetTouchGateTest` (Robolectric, raw `MotionEvent`s)

app/src/androidTest/kotlin/com/facetlauncher/app/   Instrumented (Compose UI tests, AVD only)
├── data/local/     1   FacetDatabaseMigrationTest (MigrationTestHelper over app/schemas)
└── ui/            59   one *ScreenTest per screen + component/route/theme tests (settings/automation: FacetAutomationScreenTest, RuleEditorSheetTest) and shared fakes
```

Rule of thumb from `CLAUDE.md`, and what the tree shows in practice: everything in `data/` and
`domain/` is covered on the JVM; composables are covered on the emulator; ViewModels are covered on
the JVM by feeding fake repositories through their constructors (no Hilt in tests).

## Non-code directories

| Path | Purpose |
|---|---|
| `app/schemas/com.facetlauncher.app.data.local.FacetDatabase/1..28.json` | Exported Room schemas; the migration test depends on them |
| `app/proguard-rules.pro` | Release minification rules |
| `scripts/release.sh` | The only sanctioned way to build a release (bumps version first) |
| `graphify-out/` | Knowledge graph of the repo (`graphify query`, `graphify path`) |
| `Android launcher design planning/design_handoff_minimal_launcher/` | PRD + design spec the UI is built from |
| `IMPLEMENTATION_PLAN.md` | Task tracker; also the log of bugs found by the instrumented suite |
| `docs/architecture/` | This blueprint |
