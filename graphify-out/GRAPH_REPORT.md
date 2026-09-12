# Graph Report - lumen-launcher  (2026-09-13)

## Corpus Check
- 353 files · ~789,524 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3503 nodes · 9025 edges · 192 communities (129 shown, 57 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 970 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `089df197`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- NextAlarmRepositoryTest
- .setContent
- HomeDrawerRouteTest.kt
- FontWeightOption
- ListContentMode
- Android launcher design planning/support.js
- .setContent
- Screens
- ClockAccessoryIcons.kt
- EnsureActiveFacetUseCase
- FavoritesPickerViewModel
- FavoriteAppRepository
- FacetDockAppRepository
- AppWidgetRepository
- Screens
- Alignment
- FacetEntity
- ManageFacetsViewModel
- FacetCarouselViewModel.kt
- FacetDao
- SystemSettingsRepositoryTest
- 4. Feature Requirements
- SettingsRepository
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerPresentation
- FacetCarouselScreen.kt
- Row
- ContactConnectionsSheet.kt
- .setContent
- WidgetPlacementDao
- FakeFacetDao
- .createViewModel
- ContactRepositoryTest
- .setContent
- ExportBackupUseCase.kt
- ClockStyleGalleryScreen.kt
- .setContent
- AppearanceSettingsScreen.kt
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- SettingsViewModelTest
- BackupRestoreViewModel
- LauncherAppWidgetHost
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- .createViewModel
- .setContent
- HubWidgetPickerScreen.kt
- OnboardingHomeSetupPage.kt
- .refresh
- 4. Feature Requirements
- AppDrawerScreen.kt
- BackupRestoreContent
- PaddingValues
- HomeScreen
- ClockAdjustSheet.kt
- WidgetPlacementRepository
- .setContent
- DrawerViewModel.kt
- Intent
- CalendarSettingsViewModel
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- ContactRepository
- FacetNavHost
- CalendarSettingsScreen.kt
- .setContent
- FakeDockAppDao
- OnboardingViewModel
- HomeWallpaper
- CalendarRepositoryTest
- ClockAlignment
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Fixture
- AppRepository
- .setContent
- PermissionsViewModel
- DockAppEntity
- UsageStatsRepository
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- ObserveFacetPreviewsUseCase.kt
- LauncherViewModel
- HomeViewModel
- Facet Launcher — Built Capabilities
- NotificationAccessRepository
- Play Console — store listing text
- GestureHintOverlay
- .createViewModel
- Alignment
- AppContextMenuTest
- FacetEntityTest
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- DockAppRepository
- Color.kt
- AppearanceSettingsViewModelTest.kt
- StateFlow
- combine
- .setContent
- StickyHeaderLayout
- BackButton
- LauncherSettings
- .setContent
- HubWidgetPickerViewModel
- CalendarRepository
- HubGrid
- FacetDatabaseMigrationTest
- ExportBackupUseCaseTest.kt
- AccentSwatch
- .useCase
- AppDrawerSettingsViewModelTest
- AppIcon
- AppModule
- DashedBorder.kt
- ImportBackupResult
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- OnboardingIntroPage.kt
- GetInstalledAppsUseCase
- WidgetPlacementEntity
- WallpaperRepository
- Facet Launcher — Onboarding Flow
- .setContent
- HubContent
- HubViewModel
- ObserveSettingsScreenStateUseCase.kt
- ClockAdjustMode
- DefaultFavoriteAppRepository
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- NotificationShadeRepository
- SettingsRepositoryTest
- ObserveHubStateUseCase
- PermissionKind
- UsageAccessExplanationViewModel.kt
- Axis
- OnboardingViewModelTest
- Color
- FontWeight
- 2. Design tokens
- AppInfo
- Type.kt
- NotificationSettingsViewModel
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- ViewModel
- HubViewModel
- HubWidgetPickerViewModel
- AlphabetRail
- FacetDatabase
- NestedScrollConnection
- .setContent
- Intent
- ImageVector
- PaddingValues
- AppWidgetHostView
- .setContent
- NestedScrollSource
- Offset
- Dp
- Composable
- AppInfo.kt
- SettingsScreen.kt
- Fixture
- ColorTest
- letterAt
- ObserveHomeScreenStateUseCase.kt
- DockSettingsScreen.kt
- LabeledDropdownRow
- HomeAppsListSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- CardDivider
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 186 edges
2. `FacetEntity` - 183 edges
3. `SettingsRepository` - 145 edges
4. `Row` - 128 edges
5. `FacetRepository` - 124 edges
6. `LauncherSettings` - 104 edges
7. `DefaultFavoriteAppRepository` - 56 edges
8. `HomeScreen()` - 56 edges
9. `FacetDockAppRepository` - 55 edges
10. `FavoriteAppRepository` - 55 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `AppDrawerScreen()` --calls--> `GroupAppsByLetterUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/GroupAppsByLetterUseCase.kt
- `AppDrawerScreen()` --calls--> `RankBySearchRelevanceUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/RankBySearchRelevanceUseCase.kt
- `AppDrawerScreen()` --calls--> `ContactConnectionsSheet()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/drawer/ContactConnectionsSheet.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (192 total, 57 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (10): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, StateFlow (+2 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "NextAlarmRepositoryTest"
Cohesion: 0.08
Nodes (20): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+12 more)

### Community 4 - "HomeDrawerRouteTest.kt"
Cohesion: 0.14
Nodes (10): CatalogEntry, AddAppToDockUseCase, CleanUpUninstalledAppsUseCase, ExportBackupUseCase, ImportBackupUseCase, AppOpsManager, AppWidgetManager, DrawerViewModel (+2 more)

### Community 5 - "FontWeightOption"
Cohesion: 0.07
Nodes (15): Converters, ClockColorOption, ClockFontOption, ClockTemplateId, FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM (+7 more)

### Community 6 - "ListContentMode"
Cohesion: 0.07
Nodes (30): T, resolveOverride(), AppListLimits, BackupAppEntry, BackupFacet, BackupWidgetPlacement, AppListVerticalAlignment, BOTTOM (+22 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "ClockAccessoryIcons.kt"
Cohesion: 0.31
Nodes (10): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+2 more)

### Community 11 - "EnsureActiveFacetUseCase"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow, DataStore, Preferences

### Community 12 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (6): FavoritesPickerUiState, FavoritesPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 13 - "FavoriteAppRepository"
Cohesion: 0.10
Nodes (9): FavoriteAppRepository, AppInfo, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow (+1 more)

### Community 14 - "FacetDockAppRepository"
Cohesion: 0.08
Nodes (11): FacetDockAppRepository, AppInfo, Flow, FacetDockAppDao, Flow, FacetDockAppEntity, toFacetDockAppEntity(), FacetDockAppRepositoryTest (+3 more)

### Community 15 - "AppWidgetRepository"
Cohesion: 0.07
Nodes (13): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+5 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 18 - "FacetEntity"
Cohesion: 0.09
Nodes (8): FacetRepository, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, toCsv(), FacetEntity

### Community 19 - "ManageFacetsViewModel"
Cohesion: 0.13
Nodes (6): StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 20 - "FacetCarouselViewModel.kt"
Cohesion: 0.16
Nodes (5): FacetCarouselUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId

### Community 21 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 22 - "SystemSettingsRepositoryTest"
Cohesion: 0.26
Nodes (3): SettingsSearchEntry, SystemSettingsRepository, SystemSettingsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.05
Nodes (9): Keys, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, IconRenderMode, LauncherFontOption (+1 more)

### Community 25 - ".setContent"
Cohesion: 0.07
Nodes (20): OnboardingScreenTest, DockAppPickerUiState, DockAppPickerViewModel, AppInfo, Flow, StateFlow, ViewModel, Modifier (+12 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerPresentation"
Cohesion: 0.08
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.17
Nodes (21): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), AppInfo (+13 more)

### Community 30 - "Row"
Cohesion: 0.13
Nodes (84): ClockDateStyle, CONDENSED, FULL, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS (+76 more)

### Community 31 - "ContactConnectionsSheet.kt"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 34 - "FakeFacetDao"
Cohesion: 0.12
Nodes (3): FacetRepositoryTest, FakeFacetDao, Flow

### Community 35 - ".createViewModel"
Cohesion: 0.24
Nodes (4): DockSettingsViewModelTest, WallpaperRepository, AppInfo, WallpaperRepository

### Community 38 - "ExportBackupUseCase.kt"
Cohesion: 0.16
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest, ImportBackupUseCaseTest

### Community 39 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.30
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), ClockColorOption (+5 more)

### Community 40 - ".setContent"
Cohesion: 0.24
Nodes (6): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, CalendarRepository, FakeCalendarPermissionRepository

### Community 41 - "AppearanceSettingsScreen.kt"
Cohesion: 0.13
Nodes (24): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY (+16 more)

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.19
Nodes (14): detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, AppInfo, Modifier, SwipeAxisState, FacetCarouselViewModel (+6 more)

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.07
Nodes (17): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+9 more)

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.16
Nodes (8): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost

### Community 47 - "HomeScreen.kt"
Cohesion: 0.18
Nodes (21): DockDisplayMode, ICONS, TEXT, NotificationBadgeStyle, COUNT, DOT, HomeSurfacePreview(), AppInfo (+13 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 52 - "HubWidgetPickerScreen.kt"
Cohesion: 0.38
Nodes (11): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), HubWidgetPickerViewModel, Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+3 more)

### Community 53 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.30
Nodes (16): ConfirmDialog(), Modifier, rememberDragReorderState(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow(), DockSection(), FavoritesClickableRow() (+8 more)

### Community 54 - ".refresh"
Cohesion: 0.09
Nodes (15): Callback, Callback, flattenIcon(), Bitmap, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow (+7 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.16
Nodes (30): ContactInfo, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent() (+22 more)

### Community 57 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 58 - "PaddingValues"
Cohesion: 0.45
Nodes (10): AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier, ManageFacetsContent(), ManageFacetsHeader(), ManageFacetsScreen(), ManageFacetsScreenPreview() (+2 more)

### Community 59 - "HomeScreen"
Cohesion: 0.12
Nodes (9): HomeScreenTest, AppInfo, HomeScreen(), CalendarEvent, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId (+1 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "WidgetPlacementRepository"
Cohesion: 0.20
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 62 - ".setContent"
Cohesion: 0.12
Nodes (10): KeyboardDismissalTest, AddAppToFavoritesUseCase, AppInfo, Flow, ObserveQuickAddStateUseCase, QuickAddState, SeedDefaultDockUseCase, FacetCarouselViewModel (+2 more)

### Community 63 - "DrawerViewModel.kt"
Cohesion: 0.07
Nodes (15): AppInfo, T, RankBySearchRelevanceUseCase, DrawerViewModel, AppInfo, ContactInfo, SettingsSearchEntry, DrawerViewModelTest (+7 more)

### Community 64 - "Intent"
Cohesion: 0.23
Nodes (3): DefaultLauncherRepository, DefaultLauncherRepositoryTest, Intent

### Community 65 - "CalendarSettingsViewModel"
Cohesion: 0.17
Nodes (5): AssignCalendarColorsUseCase, CalendarSettingsUiState, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 69 - "ContactRepository"
Cohesion: 0.30
Nodes (5): ContactRepository, ContactInfo, LabeledValue, ConnectionDetail, ContactConnection

### Community 70 - "FacetNavHost"
Cohesion: 0.26
Nodes (6): FacetDestinations, FacetNavHost(), AppInfo, Modifier, popBackStackSafely(), NavHostController

### Community 71 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (13): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+5 more)

### Community 73 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 74 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 75 - "HomeWallpaper"
Cohesion: 0.08
Nodes (22): HomeWallpaper, Image, Tones, Unavailable, ThemeMode, DARK, LIGHT, SYSTEM (+14 more)

### Community 76 - "CalendarRepositoryTest"
Cohesion: 0.31
Nodes (3): CalendarRepositoryTest, InstanceRow, MatrixCursor

### Community 77 - "ClockAlignment"
Cohesion: 0.15
Nodes (13): ClockAlignment, CENTER, LEFT, RIGHT, FacetSettingsUiState, FacetSettingsViewModel, AppInfo, ClockColorOption (+5 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "AppRepository"
Cohesion: 0.33
Nodes (5): AppRepository, any(), AppRepositoryTest, T, LauncherActivityInfo

### Community 82 - "PermissionsViewModel"
Cohesion: 0.16
Nodes (7): PermissionsScreenGrantedTest, CalendarPermissionRepository, ContactPermissionRepository, UsageAccessRepository, StateFlow, ViewModel, PermissionsViewModel

### Community 83 - "DockAppEntity"
Cohesion: 0.16
Nodes (4): DockAppDao, Flow, DockAppEntity, DockAppDaoTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.40
Nodes (6): FacetPreviewData, Inputs, AppInfo, CalendarEvent, Flow, ObserveFacetPreviewsUseCase

### Community 89 - "LauncherViewModel"
Cohesion: 0.19
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 90 - "HomeViewModel"
Cohesion: 0.22
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "NotificationAccessRepository"
Cohesion: 0.27
Nodes (5): FakeNotificationAccessRepository, NotificationAccessRepository, StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 96 - "Alignment"
Cohesion: 0.21
Nodes (16): Alignment, Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage() (+8 more)

### Community 97 - "AppContextMenuTest"
Cohesion: 0.09
Nodes (11): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), AppInfo (+3 more)

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.11
Nodes (13): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), CalendarEvent, ClockColorOption, ClockDateStyle, ClockFontOption (+5 more)

### Community 101 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview(), Modifier (+4 more)

### Community 102 - "DockAppRepository"
Cohesion: 0.20
Nodes (5): DockSettingsScreenTest, AppRepository, DockAppRepository, LauncherApps, WallpaperManager

### Community 103 - "Color.kt"
Cohesion: 0.24
Nodes (12): Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview(), Color, resolve(), accentTonalExtremes(), homeAppLabelShadow(), Color (+4 more)

### Community 104 - "AppearanceSettingsViewModelTest.kt"
Cohesion: 0.17
Nodes (6): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ConvertersTest

### Community 106 - "combine"
Cohesion: 0.14
Nodes (14): combine(), Flow, DockSettingsUiState, DockSettingsViewModel, AppInfo, StateFlow, ViewModel, T1 (+6 more)

### Community 108 - "StickyHeaderLayout"
Cohesion: 0.38
Nodes (10): Modifier, StickyHeaderLayout(), FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), AppInfo (+2 more)

### Community 109 - "BackButton"
Cohesion: 0.26
Nodes (11): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+3 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.16
Nodes (8): LauncherSettings, HomeUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FacetCarouselUiStateTest, HomeUiStateTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.08
Nodes (21): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, AddFailed, AddFailureReason, HUB_FULL (+13 more)

### Community 114 - "HubGrid"
Cohesion: 0.07
Nodes (35): DragReorderState, Modifier, T, Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+27 more)

### Community 116 - "ExportBackupUseCaseTest.kt"
Cohesion: 0.40
Nodes (3): toDockAppEntity(), ExportBackupUseCaseTest, DockAppEntity

### Community 117 - "AccentSwatch"
Cohesion: 0.14
Nodes (20): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, rememberTickingNow(), AccentSwatch (+12 more)

### Community 118 - ".useCase"
Cohesion: 0.06
Nodes (9): AssignCalendarColorsUseCaseTest, CleanUpUninstalledAppsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest (+1 more)

### Community 120 - "AppIcon"
Cohesion: 0.25
Nodes (17): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel(), Dp, Modifier, NotificationBadge() (+9 more)

### Community 122 - "DashedBorder.kt"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 123 - "ImportBackupResult"
Cohesion: 0.47
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "OnboardingIntroPage.kt"
Cohesion: 0.53
Nodes (8): AppRowBar(), ConceptLine(), HomeDiagram(), Modifier, LeaderRow(), OnboardingIntroPage(), OnboardingIntroPagePreview(), Dp

### Community 132 - "GetInstalledAppsUseCase"
Cohesion: 0.17
Nodes (4): FavoritesPickerScreenTest, GetInstalledAppsUseCase, Flow, LauncherViewModelTest

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.16
Nodes (9): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+1 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "HubContent"
Cohesion: 0.43
Nodes (7): HubContent(), HubScreen(), Context, HubViewModel, Modifier, AppWidgetHostView, HubUiState

### Community 138 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 139 - "ObserveSettingsScreenStateUseCase.kt"
Cohesion: 0.20
Nodes (8): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, SettingsUiState, Intent, StateFlow, ViewModel, SettingsViewModel

### Community 140 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 141 - "DefaultFavoriteAppRepository"
Cohesion: 0.10
Nodes (10): DefaultFavoriteAppRepository, AppInfo, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest (+2 more)

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 147 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 148 - "UsageAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 149 - "Axis"
Cohesion: 0.67
Nodes (3): Axis, HORIZONTAL, VERTICAL

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "AppInfo"
Cohesion: 0.17
Nodes (6): DockAppRepository, Flow, AppInfo, GroupAppsByLetterUseCase, GroupedApps, GetInstalledAppsUseCaseTest

### Community 155 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 156 - "NotificationSettingsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, NotificationSettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 162 - "AlphabetRail"
Cohesion: 0.32
Nodes (5): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale(), LetterJumpZone()

### Community 163 - "FacetDatabase"
Cohesion: 0.08
Nodes (14): DatabaseModule, Context, DefaultFavoriteAppDao, DockAppDao, WidgetPlacementDao, FacetDatabase, DefaultFavoriteAppDao, DockAppDao (+6 more)

### Community 165 - ".setContent"
Cohesion: 0.14
Nodes (14): FacetSettingsScreenTest, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron() (+6 more)

### Community 170 - ".setContent"
Cohesion: 0.06
Nodes (10): AppearanceSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase, DefaultAppRepositoryTest, AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository (+2 more)

### Community 176 - "AppInfo.kt"
Cohesion: 0.18
Nodes (5): AddAppToDockUseCaseTest, Fixture, AddAppToFavoritesUseCaseTest, Fixture, CalendarPermissionRepository

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.26
Nodes (16): appDrawerSummary(), appsListSummary(), ClickableRow(), DrawerPresentation, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+8 more)

### Community 203 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.18
Nodes (6): HomeScreenState, AppInfo, CalendarEvent, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+1 more)

### Community 211 - "LabeledDropdownRow"
Cohesion: 0.32
Nodes (11): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+3 more)

### Community 213 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), AppInfo (+2 more)

### Community 222 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+8 more)

## Knowledge Gaps
- **367 isolated node(s):** `VERTICAL`, `HORIZONTAL`, `ContactInfo`, `Tones`, `Unavailable` (+362 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 681 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **57 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockStyleGalleryViewModel`, `.setContent`, `GetInstalledAppsUseCase`, `WidgetPlacementEntity`, `FontWeightOption`, `OnboardingIntroPage.kt`, `.setContent`, `.setContent`, `.setContent`, `Type.kt`, `FacetCarouselScreen.kt`, `Row`, `ContactConnectionsSheet.kt`, `.setContent`, `AlphabetRail`, `.setContent`, `.setContent`, `ClockStyleGalleryScreen.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `LauncherAppWidgetHost`, `HomeScreen.kt`, `.setContent`, `HubWidgetPickerScreen.kt`, `OnboardingHomeSetupPage.kt`, `AppDrawerScreen.kt`, `BackupRestoreContent`, `PaddingValues`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `.setContent`, `ColorTest`, `CalendarSettingsScreen.kt`, `.setContent`, `HomeWallpaper`, `DockSettingsScreen.kt`, `.setContent`, `PermissionsViewModel`, `LabeledDropdownRow`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `LauncherViewModel`, `GestureHintOverlay`, `CardDivider`, `.setContent`, `AppContextMenuTest`, `Alignment`, `AppDrawerScreen`, `TonalButton`, `DockAppRepository`, `Color.kt`, `.setContent`, `StickyHeaderLayout`, `BackButton`, `.setContent`, `HubGrid`, `AccentSwatch`, `AppDrawerSettingsViewModelTest`, `AppIcon`, `DashedBorder.kt`?**
  _High betweenness centrality (0.179) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `HomeDrawerRouteTest.kt`, `FontWeightOption`, `ListContentMode`, `.setContent`, `EnsureActiveFacetUseCase`, `ObserveSettingsScreenStateUseCase.kt`, `SettingsRepositoryTest`, `ManageFacetsViewModel`, `FacetCarouselViewModel.kt`, `.setContent`, `DrawerPresentation`, `NotificationSettingsViewModel`, `.setContent`, `.createViewModel`, `.setContent`, `ExportBackupUseCase.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `HomeScreen.kt`, `AppInfo.kt`, `.createViewModel`, `.setContent`, `.setContent`, `DrawerViewModel.kt`, `CalendarSettingsViewModel`, `.setContent`, `OnboardingViewModel`, `HomeWallpaper`, `ObserveHomeScreenStateUseCase.kt`, `ClockAlignment`, `PermissionsViewModel`, `ObserveFacetPreviewsUseCase.kt`, `LauncherViewModel`, `HomeViewModel`, `NotificationAccessRepository`, `.createViewModel`, `DockAppRepository`, `AppearanceSettingsViewModelTest.kt`, `combine`, `.setContent`, `LauncherSettings`, `.setContent`, `ExportBackupUseCaseTest.kt`, `.useCase`, `AppDrawerSettingsViewModelTest`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Why does `FacetEntity` connect `FacetEntity` to `ClockStyleGalleryViewModel`, `GetInstalledAppsUseCase`, `ListContentMode`, `EnsureActiveFacetUseCase`, `FavoriteAppRepository`, `FacetDockAppRepository`, `ManageFacetsViewModel`, `FacetCarouselViewModel.kt`, `FacetDao`, `FacetCarouselScreen.kt`, `FakeFacetDao`, `.createViewModel`, `.setContent`, `AppInfo.kt`, `.createViewModel`, `PaddingValues`, `.setContent`, `Fixture`, `CalendarSettingsViewModel`, `ObserveHomeScreenStateUseCase.kt`, `HomeWallpaper`, `ClockAlignment`, `Fixture`, `.setContent`, `ObserveFacetPreviewsUseCase.kt`, `Fixture`, `FacetEntityTest`, `DockAppRepository`, `LauncherSettings`, `ExportBackupUseCaseTest.kt`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Are the 99 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 99 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 123 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 123 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `FacetRepository` (e.g. with `.`addFacet names it Facet N and appends after the last position`()` and `.`deleteFacet removes it`()`) actually correct?**
  _`FacetRepository` has 19 INFERRED edges - model-reasoned connections that need verification._