# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 326 files · ~694,253 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3290 nodes · 8425 edges · 201 communities (127 shown, 69 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 880 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ca815a46`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppRepository
- design_handoff_minimal_launcher/support.js
- LabeledDropdownRow
- HomeDrawerRouteTest.kt
- AppDrawerSettingsViewModelTest
- AppInfo
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselViewModel.kt
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- FakeProfileDao
- WidgetPlacementEntity
- DefaultFavoriteAppRepository
- ContactRepository
- Screens
- CalendarEventsBlock.kt
- ResolveWidgetDropUseCase
- SettingsRepository
- ClockAlignment
- LumenLauncherTheme
- ClockStyleGalleryViewModel
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- WallpaperAccentRole
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- FakeDefaultFavoriteAppDao
- .setContent
- ClockStyleGalleryViewModelTest
- ClockTemplateId
- Color.kt
- .setContent
- AppDrawerSettingsViewModel
- ListContentMode
- .setContent
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- NotificationShadeRepository
- .setContent
- .setContent
- Clock Widget Resize — Implementation Spec
- UsageStatsRepository
- AppDrawerScreen
- ClockStyleGalleryScreen.kt
- .createViewModel
- .setContent
- .refresh
- 4. Feature Requirements
- ClockFontOption
- ProfileDao
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- ExportBackupUseCaseTest.kt
- GetInstalledAppsUseCase
- HubWidgetPickerViewModel
- BackupBundle
- SettingsScreen.kt
- .setContent
- AccentSwatch
- github.md
- .setContent
- ColorTest
- LumenNavHost
- eq
- letterAt
- ScreenHeader
- CardDivider
- DatabaseModule.kt
- NotificationBadgeStyle
- NotificationBadgeRepository
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- HubWidgetPickerScreen.kt
- PaddingValues
- FakeFavoriteAppDao
- .createViewModel
- .useCase
- FavoriteAppRepository
- Lumen Launcher Implementation Plan
- OnboardingHomeSetupPage.kt
- .setContent
- HomeAppsListSettingsViewModel.kt
- AppearanceSettingsScreen.kt
- ResolveWidgetDropUseCaseTest
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- WallpaperRepository
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- HubContent
- StickyHeaderLayout
- .setContent
- DockAppPickerScreen.kt
- Fixture
- FontWeightOption
- DefaultLauncherRepository
- SelectPreviewAppsUseCaseTest
- HubGrid
- FavoritesPickerScreen.kt
- OnboardingViewModel
- AppIcon
- CalendarRepositoryTest
- DrawerViewModelTest.kt
- Converters
- CompactWidgetsUseCaseTest
- ObserveProfilePreviewsUseCase.kt
- HomeAppsListSettingsScreen.kt
- LumenDatabaseMigrationTest
- .setContent
- BackupRestoreScreen.kt
- .createViewModel
- HomeViewModel
- ClockAdjustSheet.kt
- Modifier
- ResolveWidgetResizeUseCaseTest
- LauncherActivity.kt
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DefaultAppRepositoryTest
- HomeAppsListSettingsViewModelTest.kt
- LauncherSettings
- DragReorderState
- Lumen Launcher — Onboarding Flow
- .setContent
- WallpaperRepositoryTest
- PermissionsViewModel
- .setContent
- DockSettingsScreen.kt
- LauncherFontOption
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- AppearanceSettingsViewModelTest.kt
- Fixture
- .homeViewModel
- .setContent
- SeedDefaultDockUseCaseTest
- .setContent
- GroupAppsByLetterUseCaseTest
- 2. Design tokens
- AppModule.kt
- HubWidgetTile
- BackupRestoreViewModelTest.kt
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- LumenNotificationListenerService
- AppRepository.kt
- AlphabetRail
- ObserveHubStateUseCase
- Context
- Alignment
- FontFamily
- SharedFlow
- PaddingValues
- BackupRestoreMessage
- Type.kt
- WallpaperRepository
- ClockAdjustMode
- ClockCornerHandle
- androidx
- CalendarEvent
- ClockColorOption
- ClockDateStyle
- ClockFontOption
- ClockTemplateId
- FontWeightOption
- LauncherFontOption
- NotificationAccessRepository
- WidgetResizeHandle
- calculateHubCellWidth
- GestureHintOverlay
- Axis
- ClockAlignment.kt
- AppInfo
- CalendarEvent
- ClockColorOption
- ClockDateStyle
- ClockFontOption
- ClockTemplateId
- FontWeightOption
- HomeWallpaper
- LauncherFontOption
- ListContentMode
- NestedScrollConnection
- NestedScrollSource
- Offset

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 191 edges
2. `ProfileEntity` - 165 edges
3. `SettingsRepository` - 141 edges
4. `Row` - 119 edges
5. `ProfileRepository` - 117 edges
6. `LauncherSettings` - 102 edges
7. `WidgetPlacementEntity` - 58 edges
8. `HomeScreen()` - 57 edges
9. `ClockBlock()` - 53 edges
10. `DefaultFavoriteAppRepository` - 52 edges

## Surprising Connections (you probably didn't know these)
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `HubWidgetPickerViewModel` --calls--> `WidgetProviderGroup`  [INFERRED]
  app/src/main/kotlin/com/lumenlauncher/app/ui/hub/picker/HubWidgetPickerViewModel.kt → app/src/main/kotlin/com/lumenlauncher/app/ui/hub/picker/HubWidgetPickerUiState.kt
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (201 total, 69 thin omitted)

### Community 0 - "ProfileDockAppRepository"
Cohesion: 0.08
Nodes (11): Flow, ProfileDockAppDao, ProfileDockAppEntity, AppInfo, Flow, ProfileDockAppRepository, toProfileDockAppEntity(), ProfileDockAppDaoTest (+3 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "LabeledDropdownRow"
Cohesion: 0.22
Nodes (14): AppContextMenu(), AppInfo, Modifier, Modifier, T, LabeledDropdownRow(), Composable, Modifier (+6 more)

### Community 3 - "HomeDrawerRouteTest.kt"
Cohesion: 0.08
Nodes (19): KeyboardDismissalTest, Bitmap, toBitmap(), CleanUpUninstalledAppsUseCase, HomeScreenState, AppInfo, CalendarEvent, Flow (+11 more)

### Community 5 - "AppInfo"
Cohesion: 0.18
Nodes (9): AppInfo, GroupAppsByLetterUseCase, GroupedApps, FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel (+1 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ProfileCarouselViewModel.kt"
Cohesion: 0.12
Nodes (10): ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption, ListContentMode, StateFlow, ViewModel (+2 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.09
Nodes (8): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Context, Flow, Intent, IntentSender, AppWidgetRepositoryTest

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "FakeProfileDao"
Cohesion: 0.12
Nodes (4): FakeProfileDao, Flow, ProfileDao, ProfileRepositoryTest

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.14
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "DefaultFavoriteAppRepository"
Cohesion: 0.10
Nodes (14): DefaultFavoriteAppRepository, AppInfo, DefaultFavoriteAppEntity, Flow, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao (+6 more)

### Community 15 - "ContactRepository"
Cohesion: 0.05
Nodes (31): AppContextMenuTest, ContactConnectionsSheetTest, AppShortcutRepository, ContactRepository, LabeledValue, AppShortcut, ConnectionDetail, ConnectionOption (+23 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "ResolveWidgetDropUseCase"
Cohesion: 0.17
Nodes (7): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - "SettingsRepository"
Cohesion: 0.05
Nodes (9): ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, FontWeightOption, LauncherFontOption, ListContentMode (+1 more)

### Community 20 - "ClockAlignment"
Cohesion: 0.07
Nodes (40): DataStoreModule, Context, AppListLimits, BackupAppEntry, BackupProfile, BackupSettings, BackupWidgetPlacement, AppListVerticalAlignment (+32 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.11
Nodes (10): ClockBlockTest, CalendarEvent, ClockDateStyle, CONDENSED, FULL, ClockBlock(), ClockBlockPreview(), Modifier (+2 more)

### Community 22 - "ClockStyleGalleryViewModel"
Cohesion: 0.11
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.07
Nodes (11): ProfileEntity, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, FontWeightOption, ListContentMode (+3 more)

### Community 25 - "HomeScreen"
Cohesion: 0.14
Nodes (3): HomeScreenTest, AppInfo, HomeScreen()

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.24
Nodes (23): androidx, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile() (+15 more)

### Community 28 - "WallpaperAccentRole"
Cohesion: 0.09
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.15
Nodes (26): AddProfilePage(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), NestedScrollConnection, ProfileCarouselHeader(), ProfileCarouselScreen() (+18 more)

### Community 30 - "Row"
Cohesion: 0.14
Nodes (80): Alignment, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate(), ClockDisplay(), ClockGlyphText() (+72 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "Color.kt"
Cohesion: 0.13
Nodes (22): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+14 more)

### Community 39 - "AppDrawerSettingsViewModel"
Cohesion: 0.08
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 40 - "ListContentMode"
Cohesion: 0.21
Nodes (5): ListContentMode, FAVORITES, MOST_USED, RECENTS, ProfileDaoTest

### Community 41 - ".setContent"
Cohesion: 0.19
Nodes (7): DockAppPickerScreenTest, DockAppPickerUiState, DockAppPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 43 - "HomeScreen.kt"
Cohesion: 0.20
Nodes (21): HomeSurfacePreview(), AppInfo, Color, FontWeight, HomeWallpaper, Modifier, AppRow(), dashedBorder() (+13 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.18
Nodes (6): Fixture, AppInfo, HomeWallpaper, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 46 - ".setContent"
Cohesion: 0.14
Nodes (10): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, PlaceWidgetUseCase (+2 more)

### Community 47 - ".setContent"
Cohesion: 0.19
Nodes (6): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, UsageAccessRepository

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "UsageStatsRepository"
Cohesion: 0.29
Nodes (4): AppInfo, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (4): AppDrawerScreenTest, AppDrawerScreen(), DrawerGridSize, SearchBarPosition

### Community 51 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.31
Nodes (11): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+3 more)

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 54 - ".refresh"
Cohesion: 0.21
Nodes (5): Callback, Callback, Flow, Callback, UserHandle

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "ClockFontOption"
Cohesion: 0.13
Nodes (8): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 57 - "ProfileDao"
Cohesion: 0.13
Nodes (6): Flow, ProfileDao, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 60 - "ExportBackupUseCaseTest.kt"
Cohesion: 0.31
Nodes (4): ExportBackupUseCase, Uri, toBackupSettings(), ExportBackupUseCaseTest

### Community 61 - "GetInstalledAppsUseCase"
Cohesion: 0.17
Nodes (4): FavoritesPickerScreenTest, GetInstalledAppsUseCase, Flow, LauncherViewModelTest

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.11
Nodes (10): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel (+2 more)

### Community 63 - "BackupBundle"
Cohesion: 0.21
Nodes (5): BackupRepository, Uri, BackupBundle, BackupRepositoryTest, ImportBackupUseCaseTest

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appsListSummary(), ClickableRow(), Composable, ListContentMode, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 65 - ".setContent"
Cohesion: 0.06
Nodes (10): ProfileCarouselScreenTest, DockAppRepository, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow (+2 more)

### Community 66 - "AccentSwatch"
Cohesion: 0.05
Nodes (31): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent() (+23 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (6): AppInfo, Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "eq"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 73 - "ScreenHeader"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 74 - "CardDivider"
Cohesion: 0.23
Nodes (17): CardDivider(), Modifier, SettingsCard(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+9 more)

### Community 75 - "DatabaseModule.kt"
Cohesion: 0.19
Nodes (8): DatabaseModule, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, Migrations, Migration

### Community 76 - "NotificationBadgeStyle"
Cohesion: 0.15
Nodes (7): NotificationSettingsScreenTest, NotificationBadgeStyle, COUNT, DOT, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 77 - "NotificationBadgeRepository"
Cohesion: 0.33
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "HubWidgetPickerScreen.kt"
Cohesion: 0.38
Nodes (11): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), HubWidgetPickerViewModel, Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+3 more)

### Community 80 - "PaddingValues"
Cohesion: 0.39
Nodes (11): AddProfileRow(), Modifier, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList(), ProfileReorderRow() (+3 more)

### Community 81 - "FakeFavoriteAppDao"
Cohesion: 0.10
Nodes (7): FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 82 - ".createViewModel"
Cohesion: 0.22
Nodes (6): DockSettingsViewModelTest, WallpaperRepository, AppInfo, DockAppRepository, HomeWallpaper, WallpaperRepository

### Community 84 - "FavoriteAppRepository"
Cohesion: 0.13
Nodes (14): Flow, FavoriteAppRepository, AppInfo, FavoriteAppEntity, Flow, combine(), Flow, T1 (+6 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.32
Nodes (14): rememberDragReorderState(), DockAppsReorderRow(), DockSection(), FavoritesClickableRow(), FavoritesReorderList(), HomeAppsSection(), AppInfo, ListContentMode (+6 more)

### Community 88 - "HomeAppsListSettingsViewModel.kt"
Cohesion: 0.13
Nodes (11): DockSettingsUiState, DockSettingsViewModel, AppInfo, StateFlow, ViewModel, HomeAppsListSettingsViewModel, HomeAppsListUiState, AppInfo (+3 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): AccentSwatch, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+9 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.39
Nodes (7): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground()

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.19
Nodes (13): detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, AppInfo, HubWidgetPickerViewModel, Modifier, NestedScrollConnection (+5 more)

### Community 97 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 98 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 99 - ".setContent"
Cohesion: 0.15
Nodes (11): HubWidgetPickerScreenTest, AddFailed, AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubAddWidgetEvent, HubWidgetPickerUiState, LaunchBindPermission (+3 more)

### Community 100 - "DockAppPickerScreen.kt"
Cohesion: 0.42
Nodes (10): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), AppInfo, Modifier, PickerSectionHeader() (+2 more)

### Community 102 - "FontWeightOption"
Cohesion: 0.15
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 103 - "DefaultLauncherRepository"
Cohesion: 0.22
Nodes (4): DefaultLauncherRepository, Intent, DefaultLauncherRepositoryTest, Context

### Community 105 - "HubGrid"
Cohesion: 0.26
Nodes (10): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+2 more)

### Community 106 - "FavoritesPickerScreen.kt"
Cohesion: 0.16
Nodes (20): Modifier, OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT, OnboardingSubScreen (+12 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.26
Nodes (6): AppInfo, Intent, ListContentMode, StateFlow, ViewModel, OnboardingViewModel

### Community 108 - "AppIcon"
Cohesion: 0.49
Nodes (9): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadgeStyle, NotificationBadge() (+1 more)

### Community 110 - "DrawerViewModelTest.kt"
Cohesion: 0.23
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 113 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.40
Nodes (6): Inputs, AppInfo, CalendarEvent, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 114 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.26
Nodes (13): AppIconSize, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+5 more)

### Community 116 - ".setContent"
Cohesion: 0.09
Nodes (10): SettingsScreenTest, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, Intent, StateFlow, ViewModel, SettingsViewModel (+2 more)

### Community 117 - "BackupRestoreScreen.kt"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 119 - "HomeViewModel"
Cohesion: 0.22
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 123 - "LauncherActivity.kt"
Cohesion: 0.33
Nodes (5): AppInfo, Intent, LauncherActivity, Bundle, ComponentActivity

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "HomeAppsListSettingsViewModelTest.kt"
Cohesion: 0.19
Nodes (6): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, AppInfo, HomeWallpaper, WallpaperRepository

### Community 133 - "LauncherSettings"
Cohesion: 0.13
Nodes (12): T, resolveOverride(), LauncherSettings, HomeUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId (+4 more)

### Community 134 - "DragReorderState"
Cohesion: 0.31
Nodes (4): DragReorderState, Modifier, T, detectGrabOrResizeGesture()

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "PermissionsViewModel"
Cohesion: 0.16
Nodes (9): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState, StateFlow, ViewModel (+1 more)

### Community 139 - ".setContent"
Cohesion: 0.08
Nodes (29): ProfileSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable (+21 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+4 more)

### Community 141 - "LauncherFontOption"
Cohesion: 0.23
Nodes (11): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+3 more)

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "BackButton"
Cohesion: 0.15
Nodes (16): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+8 more)

### Community 146 - "AppearanceSettingsViewModelTest.kt"
Cohesion: 0.31
Nodes (3): DefaultAppRepository, Intent, SelectPreviewAppsUseCase

### Community 149 - ".setContent"
Cohesion: 0.25
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 150 - "SeedDefaultDockUseCaseTest"
Cohesion: 0.36
Nodes (3): AppInfo, DockAppRepository, SeedDefaultDockUseCaseTest

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "AppModule.kt"
Cohesion: 0.39
Nodes (3): AppModule, Context, AppOpsManager

### Community 155 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 156 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.36
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "LumenNotificationListenerService"
Cohesion: 0.43
Nodes (3): LumenNotificationListenerService, NotificationListenerService, StatusBarNotification

### Community 160 - "AppRepository.kt"
Cohesion: 0.47
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 161 - "AlphabetRail"
Cohesion: 0.38
Nodes (4): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale()

### Community 169 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 170 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 172 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 183 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 185 - "GestureHintOverlay"
Cohesion: 0.83
Nodes (3): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier

### Community 186 - "Axis"
Cohesion: 0.67
Nodes (3): Axis, HORIZONTAL, VERTICAL

## Knowledge Gaps
- **349 isolated node(s):** `Multiple`, `Single`, `InvalidFile`, `ExportFailed`, `ExportSucceeded` (+344 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 642 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **69 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HomeDrawerRouteTest.kt`, `.setContent`, `.setContent`, `DockSettingsScreen.kt`, `LauncherFontOption`, `ContactRepository`, `BackButton`, `ResolveWidgetDropUseCase`, `AppearanceSettingsViewModelTest.kt`, `.setContent`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `WallpaperAccentRole`, `ProfileCarouselScreen.kt`, `Row`, `.setContent`, `AlphabetRail`, `.setContent`, `Color.kt`, `.setContent`, `.setContent`, `Type.kt`, `HomeScreen.kt`, `.setContent`, `.setContent`, `AppDrawerScreen`, `ClockStyleGalleryScreen.kt`, `.setContent`, `WidgetResizeHandle`, `GestureHintOverlay`, `GetInstalledAppsUseCase`, `SettingsScreen.kt`, `.setContent`, `AccentSwatch`, `.setContent`, `ColorTest`, `ScreenHeader`, `CardDivider`, `NotificationBadgeStyle`, `HubWidgetPickerScreen.kt`, `PaddingValues`, `OnboardingHomeSetupPage.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `DockAppPickerScreen.kt`, `FontWeightOption`, `FavoritesPickerScreen.kt`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `BackupRestoreScreen.kt`, `ClockAdjustSheet.kt`, `LauncherActivity.kt`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `HomeDrawerRouteTest.kt`, `AppDrawerSettingsViewModelTest`, `LauncherSettings`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `ProfileCarouselViewModel.kt`, `PermissionsViewModel`, `.setContent`, `ManageProfilesViewModel`, `DefaultFavoriteAppRepository`, `ContactRepository`, `BackButton`, `AppearanceSettingsViewModelTest.kt`, `ClockAlignment`, `.homeViewModel`, `ClockStyleGalleryViewModel`, `.setContent`, `SeedDefaultDockUseCaseTest`, `ProfileEntity`, `WallpaperAccentRole`, `.setContent`, `ClockStyleGalleryViewModelTest`, `.setContent`, `AppDrawerSettingsViewModel`, `OnboardingViewModelTest`, `.setContent`, `.setContent`, `.createViewModel`, `.setContent`, `ProfileDao`, `ExportBackupUseCaseTest.kt`, `.setContent`, `AccentSwatch`, `.setContent`, `NotificationBadgeStyle`, `.createViewModel`, `.useCase`, `.setContent`, `HomeAppsListSettingsViewModel.kt`, `.setContent`, `SettingsRepositoryTest`, `FontWeightOption`, `OnboardingViewModel`, `DrawerViewModelTest.kt`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `.createViewModel`, `HomeViewModel`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `LauncherSettings` connect `LauncherSettings` to `HomeDrawerRouteTest.kt`, `AppDrawerSettingsViewModelTest`, `HomeAppsListSettingsViewModelTest.kt`, `ProfileCarouselViewModel.kt`, `ManageProfilesViewModel`, `DefaultFavoriteAppRepository`, `ContactRepository`, `AppearanceSettingsViewModelTest.kt`, `SettingsRepository`, `ClockAlignment`, `Fixture`, `ClockStyleGalleryViewModel`, `SeedDefaultDockUseCaseTest`, `ProfileEntity`, `.homeViewModel`, `WallpaperAccentRole`, `ClockStyleGalleryViewModelTest`, `AppDrawerSettingsViewModel`, `OnboardingViewModelTest`, `.setContent`, `.createViewModel`, `ExportBackupUseCaseTest.kt`, `GetInstalledAppsUseCase`, `SettingsScreen.kt`, `AccentSwatch`, `CardDivider`, `NotificationBadgeStyle`, `.createViewModel`, `.useCase`, `AppearanceSettingsScreen.kt`, `Fixture`, `DrawerViewModelTest.kt`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `.createViewModel`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Are the 61 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo()`) actually correct?**
  _`LumenLauncherTheme()` has 61 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 114 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 114 INFERRED edges - model-reasoned connections that need verification._
- **Are the 20 inferred relationships involving `ProfileRepository` (e.g. with `.setContent()` and `.`addProfile names it Profile N and appends after the last position`()`) actually correct?**
  _`ProfileRepository` has 20 INFERRED edges - model-reasoned connections that need verification._