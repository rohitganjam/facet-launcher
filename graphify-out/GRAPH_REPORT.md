# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 327 files · ~695,651 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3319 nodes · 8497 edges · 206 communities (125 shown, 76 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 908 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `551f6bb0`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppEntity
- design_handoff_minimal_launcher/support.js
- LabeledDropdownRow
- HomeViewModel
- AppDrawerSettingsViewModelTest
- FavoritesPickerViewModel.kt
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselViewModel.kt
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- FakeProfileDao
- WidgetPlacementEntity
- HomeDrawerRouteTest.kt
- ContactRepository
- Screens
- CalendarEventsBlock.kt
- ResolveWidgetDropUseCase
- SettingsRepository
- AppRowPosition
- LumenLauncherTheme
- ClockStyleGalleryViewModel
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- LauncherFontOption
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
- .setContent
- Converters
- DockAppPickerViewModel.kt
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModel
- NotificationShadeRepository
- LauncherAppWidgetHost
- NotificationAccessRepository
- Clock Widget Resize — Implementation Spec
- UsageStatsRepository
- AppDrawerScreen
- ClockStyleGalleryScreen.kt
- .createViewModel
- .setContent
- .refresh
- 4. Feature Requirements
- ClockFontOption
- FakeProfileDao
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- .setContent
- GetInstalledAppsUseCase
- HubWidgetPickerViewModel
- AccentSwatch
- SettingsScreen.kt
- DockAppEntity
- CalendarInfo
- github.md
- ProfileDockAppRepository
- ColorTest
- LumenNavHost
- eq
- letterAt
- DefaultFavoriteAppRepository
- CardDivider
- ProfileDao
- .setContent
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
- AppDrawerSettingsScreen.kt
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- WallpaperRepository
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- HubContent
- StickyHeaderLayout
- HubAddWidgetEvent
- DockAppPickerScreen.kt
- Fixture
- FontWeightOption
- .setContent
- SettingsViewModelTest
- combine
- OnboardingScreen
- Intent
- AppIcon
- CalendarRepositoryTest
- DrawerViewModelTest.kt
- ConvertersTest
- FavoritesPickerScreen.kt
- .setContent
- HomeAppsListSettingsScreen.kt
- LumenDatabaseMigrationTest
- .setContent
- BackupRestoreScreen.kt
- .setContent
- HomeSurfacePreview
- ClockAdjustSheet.kt
- Modifier
- NotificationSettingsViewModel
- LauncherActivity.kt
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DefaultAppRepositoryTest
- .createViewModel
- LauncherSettings
- rememberDragReorderState
- Lumen Launcher — Onboarding Flow
- .setContent
- WallpaperRepositoryTest
- PermissionKind
- ProfileSettingsContent
- DockSettingsScreen.kt
- ClockFonts.kt
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- AppearanceSettingsViewModelTest.kt
- Fixture
- .setContent
- .setContent
- SettingsViewModel
- .setContent
- ComponentName
- 2. Design tokens
- UsageStatsManager
- HubViewModel.kt
- .setContent
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
- ObserveSettingsScreenStateUseCase.kt
- Type.kt
- ProfileEntityTest
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
- ClockDateStyle
- WidgetResizeHandle
- DeleteWidgetUseCaseTest.kt
- GestureHintOverlay
- Dp
- ClockAlignment
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
- StateFlow
- ViewModel
- HomeWallpaper
- WallpaperRepository
- OnboardingUiState

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 190 edges
2. `ProfileEntity` - 165 edges
3. `SettingsRepository` - 138 edges
4. `Row` - 122 edges
5. `ProfileRepository` - 117 edges
6. `LauncherSettings` - 95 edges
7. `WidgetPlacementEntity` - 58 edges
8. `HomeScreen()` - 57 edges
9. `ClockBlock()` - 53 edges
10. `ClockTemplateId` - 50 edges

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

## Communities (206 total, 76 thin omitted)

### Community 0 - "ProfileDockAppEntity"
Cohesion: 0.06
Nodes (16): DatabaseModule, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, Migrations, Flow (+8 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 3 - "HomeViewModel"
Cohesion: 0.10
Nodes (10): HomeScreenState, AppInfo, CalendarEvent, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel (+2 more)

### Community 5 - "FavoritesPickerViewModel.kt"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 6 - "HubViewModel"
Cohesion: 0.12
Nodes (13): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+5 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ProfileCarouselViewModel.kt"
Cohesion: 0.08
Nodes (18): T, resolveOverride(), Inputs, AppInfo, CalendarEvent, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData (+10 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (9): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Context, Flow, IntentSender, calculateHubCellWidth(), Context (+1 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "FakeProfileDao"
Cohesion: 0.12
Nodes (4): FakeProfileDao, Flow, ProfileDao, ProfileRepositoryTest

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.14
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "HomeDrawerRouteTest.kt"
Cohesion: 0.08
Nodes (16): DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, LumenDatabase, Bitmap, toBitmap() (+8 more)

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
Cohesion: 0.16
Nodes (8): CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - "SettingsRepository"
Cohesion: 0.04
Nodes (31): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+23 more)

### Community 20 - "AppRowPosition"
Cohesion: 0.07
Nodes (31): BackupRepository, Uri, AppListLimits, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement (+23 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "ClockStyleGalleryViewModel"
Cohesion: 0.13
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.08
Nodes (9): ProfileEntity, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, FontWeightOption, ProfileRepository (+1 more)

### Community 25 - "HomeScreen"
Cohesion: 0.14
Nodes (3): HomeScreenTest, AppInfo, HomeScreen()

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.24
Nodes (23): androidx, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile() (+15 more)

### Community 28 - "LauncherFontOption"
Cohesion: 0.07
Nodes (22): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, LauncherFontOption, MANROPE, NOTO_SANS, POPPINS (+14 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.14
Nodes (27): AddProfilePage(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), NestedScrollConnection, ProfileCarouselHeader(), ProfileCarouselScreen() (+19 more)

### Community 30 - "Row"
Cohesion: 0.13
Nodes (81): Alignment, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate(), ClockDisplay(), ClockGlyphText() (+73 more)

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
Cohesion: 0.09
Nodes (31): dashedBorder(), Color, Dp, Modifier, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector (+23 more)

### Community 40 - "Converters"
Cohesion: 0.13
Nodes (5): Converters, ListContentMode, FAVORITES, MOST_USED, RECENTS

### Community 41 - "DockAppPickerViewModel.kt"
Cohesion: 0.33
Nodes (6): DockAppPickerUiState, DockAppPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 43 - "HomeScreen.kt"
Cohesion: 0.24
Nodes (17): AppContextMenu(), AppInfo, Modifier, AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview(), AppInfo (+9 more)

### Community 44 - "OnboardingViewModel"
Cohesion: 0.07
Nodes (15): DefaultLauncherRepository, Intent, AppInfo, DrawerPresentation, ListContentMode, OnboardingViewModel, DefaultLauncherRepositoryTest, Fixture (+7 more)

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.24
Nodes (7): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost

### Community 47 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (12): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "UsageStatsRepository"
Cohesion: 0.34
Nodes (3): AppInfo, UsageStatsRepository, UsageStatsRepositoryTest

### Community 50 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (5): AppDrawerScreenTest, AppInfo, GroupAppsByLetterUseCase, GroupedApps, AppDrawerScreen()

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
Cohesion: 0.14
Nodes (8): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 57 - "FakeProfileDao"
Cohesion: 0.16
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.19
Nodes (8): BackupRestoreEvent, LaunchBindPermission, LaunchConfigure, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 61 - "GetInstalledAppsUseCase"
Cohesion: 0.12
Nodes (9): GetInstalledAppsUseCase, Flow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, GetInstalledAppsUseCaseTest, LauncherViewModelTest (+1 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.13
Nodes (6): WidgetProviderOption, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 63 - "AccentSwatch"
Cohesion: 0.14
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appsListSummary(), ClickableRow(), Composable, ListContentMode, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.09
Nodes (9): DockAppRepository, Flow, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow (+1 more)

### Community 66 - "CalendarInfo"
Cohesion: 0.07
Nodes (18): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent() (+10 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "ProfileDockAppRepository"
Cohesion: 0.13
Nodes (11): DockSettingsScreenTest, AppInfo, Flow, ProfileDockAppRepository, DockSettingsScreen(), DockSettingsUiState, DockSettingsViewModel, AppInfo (+3 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (6): AppInfo, Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "eq"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 73 - "DefaultFavoriteAppRepository"
Cohesion: 0.24
Nodes (4): DefaultFavoriteAppRepository, AppInfo, DefaultFavoriteAppEntity, Flow

### Community 74 - "CardDivider"
Cohesion: 0.38
Nodes (9): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+1 more)

### Community 75 - "ProfileDao"
Cohesion: 0.21
Nodes (3): Flow, ProfileDao, ProfileDaoTest

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

### Community 83 - ".useCase"
Cohesion: 0.06
Nodes (10): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, AppInfo, DockAppRepository (+2 more)

### Community 84 - "FavoriteAppRepository"
Cohesion: 0.30
Nodes (4): FavoriteAppRepository, AppInfo, FavoriteAppEntity, Flow

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.30
Nodes (16): ConfirmDialog(), Modifier, AppDrawerSection(), DockAppsReorderRow(), DockClickableRow(), DockSection(), FavoritesClickableRow(), FavoritesReorderList() (+8 more)

### Community 88 - "HomeAppsListSettingsViewModel.kt"
Cohesion: 0.17
Nodes (10): HomeAppsListSettingsViewModel, HomeAppsListUiState, AppInfo, ListContentMode, StateFlow, ViewModel, fakeWallpaperRepository(), WallpaperRepository (+2 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): AccentSwatch, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+9 more)

### Community 90 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), LauncherSettings (+4 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.22
Nodes (9): HomeWallpaper, Image, Tones, Unavailable, Bitmap, WallpaperRepository, Alignment, Modifier (+1 more)

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.14
Nodes (18): Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, AppInfo (+10 more)

### Community 97 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 98 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.18
Nodes (10): AddFailed, AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubAddWidgetEvent, HubWidgetPickerUiState, LaunchBindPermission, LaunchConfigure (+2 more)

### Community 100 - "DockAppPickerScreen.kt"
Cohesion: 0.49
Nodes (9): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), AppInfo, Modifier, PickerSectionHeader() (+1 more)

### Community 102 - "FontWeightOption"
Cohesion: 0.15
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 103 - ".setContent"
Cohesion: 0.26
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 105 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 106 - "OnboardingScreen"
Cohesion: 0.20
Nodes (14): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT (+6 more)

### Community 108 - "AppIcon"
Cohesion: 0.49
Nodes (9): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadgeStyle, NotificationBadge() (+1 more)

### Community 110 - "DrawerViewModelTest.kt"
Cohesion: 0.23
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 112 - "FavoritesPickerScreen.kt"
Cohesion: 0.49
Nodes (9): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), AppInfo, Modifier, PickerSectionHeader() (+1 more)

### Community 114 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.26
Nodes (13): AppIconSize, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+5 more)

### Community 117 - "BackupRestoreScreen.kt"
Cohesion: 0.21
Nodes (16): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+8 more)

### Community 119 - "HomeSurfacePreview"
Cohesion: 0.52
Nodes (6): HomeSurfacePreview(), AppInfo, Color, FontWeight, HomeWallpaper, Modifier

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 122 - "NotificationSettingsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, NotificationSettingsViewModel

### Community 123 - "LauncherActivity.kt"
Cohesion: 0.33
Nodes (5): AppInfo, Intent, LauncherActivity, Bundle, ComponentActivity

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "LauncherSettings"
Cohesion: 0.13
Nodes (10): LauncherSettings, HomeUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption, ListContentMode (+2 more)

### Community 134 - "rememberDragReorderState"
Cohesion: 0.27
Nodes (6): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), Orientation

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 139 - "ProfileSettingsContent"
Cohesion: 0.18
Nodes (17): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable, Modifier (+9 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreenPreview(), AppInfo (+2 more)

### Community 141 - "ClockFonts.kt"
Cohesion: 0.53
Nodes (5): FontFamily, resolveFontFamily(), variableWeightInstances(), Font, FontStyle

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

### Community 150 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "UsageStatsManager"
Cohesion: 0.31
Nodes (4): AppModule, Context, AppOpsManager, UsageStatsManager

### Community 155 - "HubViewModel.kt"
Cohesion: 0.27
Nodes (8): AppWidgetHostView, Context, ViewModel, HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 156 - ".setContent"
Cohesion: 0.16
Nodes (10): BackupRestoreScreenTest, ExportBackupUseCase, Uri, toBackupSettings(), ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri (+2 more)

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

### Community 169 - "ObserveSettingsScreenStateUseCase.kt"
Cohesion: 0.60
Nodes (3): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

### Community 170 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 172 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 182 - "ClockDateStyle"
Cohesion: 0.50
Nodes (3): ClockDateStyle, CONDENSED, FULL

### Community 183 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 185 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 187 - "ClockAlignment"
Cohesion: 0.07
Nodes (24): AppListVerticalAlignment, BOTTOM, TOP, ClockAlignment, CENTER, LEFT, RIGHT, DockDisplayMode (+16 more)

## Knowledge Gaps
- **349 isolated node(s):** `INTRO`, `HOME_SETUP`, `PROFILES`, `SET_DEFAULT`, `DOCK_PICKER` (+344 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 650 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **76 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `.setContent`, `ProfileSettingsContent`, `DockSettingsScreen.kt`, `HomeDrawerRouteTest.kt`, `ContactRepository`, `BackButton`, `ResolveWidgetDropUseCase`, `AppearanceSettingsViewModelTest.kt`, `.setContent`, `.setContent`, `SettingsRepository`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `ProfileCarouselScreen.kt`, `Row`, `LauncherFontOption`, `.setContent`, `AlphabetRail`, `.setContent`, `Color.kt`, `.setContent`, `.setContent`, `Type.kt`, `HomeScreen.kt`, `NotificationAccessRepository`, `AppDrawerScreen`, `ClockStyleGalleryScreen.kt`, `.setContent`, `WidgetResizeHandle`, `GestureHintOverlay`, `.setContent`, `AccentSwatch`, `SettingsScreen.kt`, `CalendarInfo`, `ProfileDockAppRepository`, `ColorTest`, `CardDivider`, `.setContent`, `HubWidgetPickerScreen.kt`, `PaddingValues`, `OnboardingHomeSetupPage.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `StickyHeaderLayout`, `DockAppPickerScreen.kt`, `FontWeightOption`, `.setContent`, `FavoritesPickerScreen.kt`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `BackupRestoreScreen.kt`, `.setContent`, `ClockAdjustSheet.kt`, `LauncherActivity.kt`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `HomeViewModel`, `AppDrawerSettingsViewModelTest`, `LauncherSettings`, `.createViewModel`, `.setContent`, `ProfileCarouselViewModel.kt`, `ManageProfilesViewModel`, `HomeDrawerRouteTest.kt`, `ContactRepository`, `BackButton`, `AppearanceSettingsViewModelTest.kt`, `.setContent`, `AppRowPosition`, `ClockStyleGalleryViewModel`, `.setContent`, `ProfileEntity`, `.setContent`, `LauncherFontOption`, `.setContent`, `ClockStyleGalleryViewModelTest`, `.setContent`, `.setContent`, `ObserveSettingsScreenStateUseCase.kt`, `NotificationAccessRepository`, `.createViewModel`, `.setContent`, `FakeProfileDao`, `ClockAlignment`, `.setContent`, `CalendarInfo`, `ProfileDockAppRepository`, `DefaultFavoriteAppRepository`, `.setContent`, `.createViewModel`, `.useCase`, `.setContent`, `HomeAppsListSettingsViewModel.kt`, `.setContent`, `SettingsRepositoryTest`, `FontWeightOption`, `DrawerViewModelTest.kt`, `.setContent`, `NotificationSettingsViewModel`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Why does `ProfileEntity` connect `ProfileEntity` to `ProfileDockAppEntity`, `HomeViewModel`, `.createViewModel`, `LauncherSettings`, `ProfileCarouselViewModel.kt`, `ManageProfilesViewModel`, `FakeProfileDao`, `HomeDrawerRouteTest.kt`, `Fixture`, `AppRowPosition`, `ClockStyleGalleryViewModel`, `ClockStyleGalleryViewModelTest`, `ProfileEntityTest`, `FakeProfileDao`, `ClockAlignment`, `CalendarInfo`, `ProfileDockAppRepository`, `ProfileDao`, `PaddingValues`, `FakeFavoriteAppDao`, `.createViewModel`, `HomeAppsListSettingsViewModel.kt`, `Fixture`, `.setContent`, `.setContent`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 62 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo()`) actually correct?**
  _`LumenLauncherTheme()` has 62 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `SettingsRepository` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`SettingsRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 117 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 117 INFERRED edges - model-reasoned connections that need verification._