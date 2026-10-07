# 04 — Directory & Package Structure Map

Single Gradle module (`:app`), root package `com.facetlauncher.app`. File counts are from the
current tree; the "belongs here if…" column is the placement rule the code actually follows.

```
app/src/main/kotlin/com/facetlauncher/app/
├── FacetApplication.kt            @HiltAndroidApp
├── LauncherActivity.kt            @AndroidEntryPoint — the only Activity; composition root
│
├── data/                          (41)  @Singleton Repositories — the only layer that touches
│   │                                    Room, DataStore, or Android framework services
│   ├── di/                        (5)   Hilt modules: AppModule, DatabaseModule, DataStoreModule, WidgetModule + the @AutomationDataStore qualifier
│   ├── local/                     (28)  Room: FacetDatabase, 13 entities, 12 DAOs, Converters, Migrations
│   ├── model/                     (36)  Immutable value types & enums shared by every layer
│   │                                    (AppInfo, PlacedItem, LauncherSettings, AppProfile, Clock*Option,
│   │                                    FacetDeepLink's build/parse pair, …)
│   ├── widget/                    (2)   AppWidgetRepository + LauncherAppWidgetHost (AppWidgetHost subclass)
│   └── FacetNotificationListenerService.kt   @AndroidEntryPoint service feeding NotificationBadgeRepository
│
├── domain/                        (46)  43 *UseCase classes + FlowCombine.kt, HubGridConstants.kt, BackupMapping.kt
│                                        Composes ≥1 repositories, or pure logic (grid placement, ranking, grouping)
│
└── ui/
    ├── theme/                     (11)  Color, Type, Theme, Motion, ClockFonts/Colors, ClockAlignment, AccentSwatch, ThemeLocals,
    │                                    PrivateSpaceTheme, SystemBars (status/nav icon color) — design tokens; FacetLauncherTheme wrapper
    ├── components/                (32)  Reusable, screen-agnostic composables (AppIcon, ConfirmDialog,
    │                                    DragReorderState, ThemedDropdownMenu, ThemedModalBottomSheet, DismissOnHomePress,
    │                                    AppPickerScreen, FolderContentsSheet, …)
    ├── navigation/                (1)   FacetNavHost + FacetDestinations (23 routes) + popBackStackSafely
    ├── launcher/                  (3)   LauncherViewModel (app-lifetime state), HomeDrawerRoute (Home⇄Drawer gesture
    │                                    surface), LauncherLocals (CompositionLocals)
    ├── home/                      (13)  HomeScreen / HomeViewModel / HomeUiState, ClockBlock, clock handles, ClockAdjustSheet /
    │                                    ClockAdjustToolbar, hosted-clock-widget controllers/tile, TimeTick
    │   ├── clock/                 (7)   Clock templates, accessory row/icons, calendar strip, ClockStyleGallery screen+VM
    │   └── widget/                (2)   ClockWidgetPickerScreen / ClockWidgetPickerViewModel (custom clock widget bind flow)
    ├── drawer/                    (6)   AppDrawerScreen / DrawerViewModel, AlphabetRail, ContactConnectionsSheet,
    │                                    PrivateSpaceScreen / PrivateSpaceViewModel
    ├── hub/                       (11)  HubScreen / HubViewModel / HubUiState, HubGrid, widget tiles, resize/grab gestures
    │   └── picker/                (3)   HubWidgetPickerScreen / ViewModel / UiState
    ├── facets/                    (9)   FacetCarousel, FacetSettings, ManageFacets, FavoritesPicker (screen + VM each)
    ├── dock/                      (2)   DockAppPickerScreen / DockAppPickerViewModel
    ├── onboarding/                (8)   OnboardingScreen / ViewModel / UiState + 3 pages, dots, SetDefaultLauncherSheet
    └── settings/                  (29)  13 settings screens, each `XScreen.kt` + `XViewModel.kt`
        ├── automation/            (15)  Facet automation: FacetAutomationScreen / ViewModel (rule list), AutomationStrips, RuleEditorSheet / ViewModel / State (draft rule, permission gate), ScheduleFields, TriggerFields, EditorControls, UpgradeSheet, ProPill, PermissionRequest, formatting, previews
        └── backup/                (3)   BackupRestoreScreen / ViewModel / UiState
```

## Placement rules (derived from the code)

| If the file is… | It goes in | Naming | Example |
|---|---|---|---|
| A class that wraps a Room DAO, `DataStore`, or an Android system service | `data/` | `XRepository` | `CalendarRepository`, `WallpaperRepository` |
| A Room `@Entity`, `@Dao`, `Migration`, or `@TypeConverter` | `data/local/` | `XEntity`, `XDao` | `FavoriteAppEntity`, `FavoriteAppDao` |
| A Hilt `@Module` | `data/di/` | `XModule` | `DatabaseModule` |
| An immutable value type or enum used across layers | `data/model/` | plain noun | `AppInfo`, `PlacedItem`, `ClockDateStyle` |
| Logic that spans more than one repository, or a pure algorithm | `domain/` | `XUseCase` with `operator fun invoke` / `observe()` | `AddAppToDockUseCase`, `ResolveWidgetDropUseCase` |
| Owns screen state and is the composable's only collaborator | `ui/<surface>/` | `XViewModel` (`@HiltViewModel`) | `HubViewModel` |
| The immutable state a screen renders | `ui/<surface>/` | `XUiState` (data class) | `HomeUiState`, `BackupRestoreUiState` |
| A screen-level composable | `ui/<surface>/` | `XScreen` | `AppDrawerScreen` |
| A composable used by ≥2 surfaces | `ui/components/` | descriptive noun | `AppContextMenu`, `ScreenHeader` |
| A colour/typography/motion token | `ui/theme/` | — | `Color.kt`, `Motion.kt` |
| A `NavHost` route | `ui/navigation/FacetNavHost.kt` | `FacetDestinations.X` constant | `FOLDER_DETAIL = "folderDetail/{folderId}"` |

Settings screens are the main exception to "one package per surface": all 13 live flat in
`ui/settings/` (only `backup/` and `automation/` have their own sub-package). Clock styling is split between
`ui/home/clock/` (rendering + gallery) and `data/model/Clock*Option.kt` (choices).

## Test tree mirror

Both test source sets mirror `main`'s packages; the class under test and its test share a package.

```
app/src/test/kotlin/com/facetlauncher/app/          JVM (JUnit4 + Robolectric + coroutines-test)
├── data/          33   one *RepositoryTest per repository + shared fakes (FolderTestFakes.kt, …)
│   ├── local/     12   Converters, entities, DAO-level behaviour via in-memory Room
│   ├── model/      6   FacetDeepLinkTest (build/parse round-trip + rejection cases), AutomationStateTest, AutomationTriggerTest (schedule windows and battery levels), AutomationRuleValidationTest, AutomationLimitsTest, DeviceStateTest (every trigger's truth)
│   └── widget/     1
├── domain/        40   one test per use case (pure logic — no Android needed for most)
└── ui/            38   ViewModel tests (dock, drawer, facets, home, hub, launcher, onboarding, settings×9, settings/automation, settings/backup, theme) and the rule editor state/formatting tests

app/src/androidTest/kotlin/com/facetlauncher/app/   Instrumented (Compose UI tests, AVD only)
├── data/local/     1   FacetDatabaseMigrationTest (MigrationTestHelper over app/schemas)
└── ui/            45   one *ScreenTest per screen + component/route/theme tests (settings/automation: FacetAutomationScreenTest, RuleEditorSheetTest) and shared fakes
```

Rule of thumb from `CLAUDE.md`, and what the tree shows in practice: everything in `data/` and
`domain/` is covered on the JVM; composables are covered on the emulator; ViewModels are covered on
the JVM by feeding fake repositories through their constructors (no Hilt in tests).

## Non-code directories

| Path | Purpose |
|---|---|
| `app/schemas/com.facetlauncher.app.data.local.FacetDatabase/1..20.json` | Exported Room schemas; the migration test depends on them |
| `app/proguard-rules.pro` | Release minification rules |
| `scripts/release.sh` | The only sanctioned way to build a release (bumps version first) |
| `graphify-out/` | Knowledge graph of the repo (`graphify query`, `graphify path`) |
| `Android launcher design planning/design_handoff_minimal_launcher/` | PRD + design spec the UI is built from |
| `IMPLEMENTATION_PLAN.md` | Task tracker; also the log of bugs found by the instrumented suite |
| `docs/architecture/` | This blueprint |
