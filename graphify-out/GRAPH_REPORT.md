# Graph Report - lumen-launcher  (2026-09-12)

## Corpus Check
- 346 files · ~704,330 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3347 nodes · 8911 edges · 183 communities (126 shown, 51 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 834 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b466d0f3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppEntity
- design_handoff_minimal_launcher/support.js
- HubGrid
- HomeViewModel
- AppDrawerSettingsViewModel
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselUiState
- Screens
- AppWidgetRepository
- ProfileDao
- ProfileEntity
- FakeWidgetPlacementDao
- ListContentMode
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementRepository
- .setContent
- NotificationBadgeStyle
- LumenLauncherTheme
- ClockStyleGalleryViewModelTest
- 4. Feature Requirements
- AppDrawerScreen.kt
- HomeScreenTest
- 4. Feature Requirements
- HomeDrawerRoute
- SettingsRepository
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- DefaultFavoriteAppEntity
- DragReorderState
- OnboardingScreen
- ClockTemplateId
- Color.kt
- .setContent
- .setContent
- WidgetPlacementEntity
- LauncherFontOption
- Manrope Font License (SIL OFL 1.1)
- HomeScreen
- OnboardingViewModelTest
- .setContent
- LauncherAppWidgetHost
- CalendarPermissionRepository
- Clock Widget Resize — Implementation Spec
- ExportBackupUseCase.kt
- AppDrawerScreen
- FontWeightOption
- .createViewModel
- ManageProfilesScreen.kt
- NotificationBadgeRepository
- 4. Feature Requirements
- ClockColorOption
- FakeProfileDao
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- AppWidgetProviderInfo
- LauncherViewModel.kt
- HubWidgetPickerViewModel
- ClockStyleGalleryScreen.kt
- SettingsScreen.kt
- .setContent
- CalendarInfo
- github.md
- HomeWallpaper
- ColorTest
- LumenNavHost
- DockAppEntity
- letterAt
- WidgetPlacementDao
- DockAppPickerScreen.kt
- CalendarSettingsViewModel
- .setContent
- ComponentName
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- WidgetProviderOption
- LabeledDropdownRow
- DockAppRepository
- .createViewModel
- .useCase
- Fixture
- Lumen Launcher Implementation Plan
- CardDivider
- DockAppPickerViewModel
- DefaultAppRepositoryTest
- AppearanceSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Lumen Launcher — Built Capabilities
- AppInfo
- HomeUiState
- .setContent
- AccentSwatch
- SettingsRepositoryTest
- AppContextMenuTest
- StickyHeaderLayout
- HubAddWidgetEvent
- PlaceWidgetResult
- Fixture
- BackupWidgetPlacement
- .setContent
- SettingsViewModelTest
- combine
- BatteryStatus
- OnboardingViewModel
- NextAlarmRepositoryTest
- FavoriteAppEntity
- .createViewModel
- ResolveWidgetDropUseCaseTest
- .rendersOneEntryPerLetterProvided
- .setContent
- AppIcon
- LumenDatabaseMigrationTest
- .setContent
- ClockAccessoryIcons.kt
- HubWidgetTile
- ClockAdjustSheet.kt
- BackupRestoreMessage
- SelectPreviewAppsUseCaseTest
- Modifier
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- AppWidgetRepository.kt
- OnboardingProfilesPage.kt
- LauncherSettings
- HomeAppsListSettingsScreen.kt
- Lumen Launcher — Onboarding Flow
- BackupRestoreContent
- CalendarSettingsScreen.kt
- CompactWidgetsUseCaseTest
- .setContent
- DockSettingsScreen.kt
- ResolveWidgetResizeUseCaseTest
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- BatteryRepository.kt
- Fixture
- ContactRepository
- AppModule
- ClockAccessoryIconsTest
- DefaultAppRepository
- NextAlarmRepository.kt
- 2. Design tokens
- GroupAppsByLetterUseCaseTest
- .setContent
- NotificationShadeRepository
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- ObserveProfilePreviewsUseCase
- GestureHintOverlay
- SetDefaultLauncherSheet
- PermissionKind
- LumenDatabase
- Type.kt
- SeedDefaultDockUseCaseTest
- AppContextMenu
- InheritOverrideCard
- BackupRestoreViewModelTest.kt
- ContactInfo
- WeatherInfo.kt
- Alignment
- ClockCornerHandle
- Dp
- StateFlow
- ViewModel
- androidx
- NestedScrollConnection
- NestedScrollSource
- Offset
- TextUnit
- HomeDrawerRouteTest.kt

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 219 edges
2. `AppInfo` - 166 edges
3. `ProfileEntity` - 158 edges
4. `SettingsRepository` - 139 edges
5. `Row` - 126 edges
6. `ProfileRepository` - 115 edges
7. `LauncherSettings` - 97 edges
8. `ClockTemplateId` - 63 edges
9. `WidgetPlacementEntity` - 60 edges
10. `HomeScreen()` - 57 edges

## Surprising Connections (you probably didn't know these)
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (183 total, 51 thin omitted)

### Community 0 - "ProfileDockAppEntity"
Cohesion: 0.08
Nodes (8): Flow, ProfileDockAppDao, ProfileDockAppEntity, toProfileDockAppEntity(), ProfileDockAppDaoTest, FakeProfileDockAppDao, Flow, ProfileDockAppRepositoryTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "HubGrid"
Cohesion: 0.26
Nodes (10): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+2 more)

### Community 3 - "HomeViewModel"
Cohesion: 0.12
Nodes (10): ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase, HomeScreenState, HomeViewModel, HomeViewModelTest, LauncherSettings, ProfileEntity (+2 more)

### Community 4 - "AppDrawerSettingsViewModel"
Cohesion: 0.10
Nodes (5): AppDrawerSettingsScreenTest, AppDrawerSettingsViewModel, StateFlow, ViewModel, AppDrawerSettingsViewModelTest

### Community 6 - "HubViewModel"
Cohesion: 0.20
Nodes (4): HubUiState, HubWidgetUi, HubViewModel, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.11
Nodes (4): AppWidgetRepository, Flow, AppWidgetRepositoryTest, DeleteWidgetUseCaseTest

### Community 11 - "ProfileDao"
Cohesion: 0.08
Nodes (9): Flow, ProfileDao, StateFlow, ViewModel, ManageProfilesViewModel, ProfileDaoTest, FakeProfileDao, Flow (+1 more)

### Community 12 - "ProfileEntity"
Cohesion: 0.07
Nodes (10): ProfileEntity, ProfileRepository, toCsv(), ProfileEntityTest, FakeProfileDao, ProfileRepositoryTest, AddAppToDockUseCaseTest, Fixture (+2 more)

### Community 13 - "FakeWidgetPlacementDao"
Cohesion: 0.36
Nodes (3): FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ListContentMode"
Cohesion: 0.04
Nodes (29): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+21 more)

### Community 15 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementRepository"
Cohesion: 0.14
Nodes (12): HubScreenTest, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow (+4 more)

### Community 20 - "NotificationBadgeStyle"
Cohesion: 0.23
Nodes (7): NotificationBadgeStyle, COUNT, DOT, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.10
Nodes (15): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), CalendarEvent, ClockAlignment, ClockColorOption, ClockDateStyle (+7 more)

### Community 22 - "ClockStyleGalleryViewModelTest"
Cohesion: 0.08
Nodes (5): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "AppDrawerScreen.kt"
Cohesion: 0.19
Nodes (22): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip() (+14 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "HomeDrawerRoute"
Cohesion: 0.14
Nodes (18): androidx, Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, AppInfo (+10 more)

### Community 28 - "SettingsRepository"
Cohesion: 0.04
Nodes (27): DataStoreModule, Context, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, DrawerGridSize, FIVE_BY_FIVE (+19 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+6 more)

### Community 30 - "Row"
Cohesion: 0.18
Nodes (75): Alignment, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily, FontWeight, Modifier (+67 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "DefaultFavoriteAppEntity"
Cohesion: 0.11
Nodes (7): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 34 - "DragReorderState"
Cohesion: 0.20
Nodes (8): DragReorderState, Modifier, T, detectGrabOrResizeGesture(), Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 35 - "OnboardingScreen"
Cohesion: 0.22
Nodes (12): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT (+4 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "Color.kt"
Cohesion: 0.07
Nodes (37): dashedBorder(), Color, Dp, Modifier, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector (+29 more)

### Community 39 - ".setContent"
Cohesion: 0.12
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 41 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 43 - "HomeScreen"
Cohesion: 0.15
Nodes (29): AppRow(), ClockAdjustMode, ADJUST, MENU, NONE, dashedBorder(), DockIcon(), HomeScreen() (+21 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 45 - ".setContent"
Cohesion: 0.17
Nodes (10): KeyboardDismissalTest, LauncherViewModel, CalendarRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, Flow, ObserveQuickAddStateUseCase, QuickAddState (+2 more)

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.20
Nodes (9): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, PlaceWidgetUseCase, AppWidgetHost (+1 more)

### Community 47 - "CalendarPermissionRepository"
Cohesion: 0.11
Nodes (11): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow (+3 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "ExportBackupUseCase.kt"
Cohesion: 0.23
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 51 - "FontWeightOption"
Cohesion: 0.05
Nodes (22): T, resolveOverride(), ClockDateStyle, CONDENSED, FULL, FontWeightOption, EXTRA_LIGHT, LIGHT (+14 more)

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 53 - "ManageProfilesScreen.kt"
Cohesion: 0.37
Nodes (12): rememberDragReorderState(), AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview() (+4 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "ClockColorOption"
Cohesion: 0.10
Nodes (8): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 57 - "FakeProfileDao"
Cohesion: 0.22
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.23
Nodes (5): BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 60 - "AppWidgetProviderInfo"
Cohesion: 0.16
Nodes (6): AppWidgetHostView, AppWidgetProviderInfo, Context, IntentSender, calculateHubCellWidth(), Context

### Community 61 - "LauncherViewModel.kt"
Cohesion: 0.21
Nodes (6): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.16
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 63 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.22
Nodes (16): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+8 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.36
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - ".setContent"
Cohesion: 0.12
Nodes (5): AppearanceSettingsScreenTest, fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 66 - "CalendarInfo"
Cohesion: 0.17
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarInfo

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "HomeWallpaper"
Cohesion: 0.07
Nodes (21): DockSettingsScreenTest, AppListLimits, HomeWallpaper, Image, Tones, Unavailable, DockDisplayMode, ICONS (+13 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.24
Nodes (8): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), ClockStyleGalleryViewModel, NavHostController

### Community 71 - "DockAppEntity"
Cohesion: 0.09
Nodes (8): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 74 - "DockAppPickerScreen.kt"
Cohesion: 0.61
Nodes (7): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader()

### Community 75 - "CalendarSettingsViewModel"
Cohesion: 0.20
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 77 - "ComponentName"
Cohesion: 0.18
Nodes (6): Intent, Intent, LauncherActivity, Bundle, ComponentActivity, ComponentName

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "WidgetProviderOption"
Cohesion: 0.29
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

### Community 80 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 81 - "DockAppRepository"
Cohesion: 0.10
Nodes (13): FavoritesPickerScreenTest, BackupRestoreScreenTest, AppRepository, DefaultFavoriteAppRepository, DockAppRepository, FavoriteAppRepository, ProfileDockAppRepository, CleanUpUninstalledAppsUseCase (+5 more)

### Community 82 - ".createViewModel"
Cohesion: 0.26
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 83 - ".useCase"
Cohesion: 0.17
Nodes (3): AssignCalendarColorsUseCaseTest, ExportBackupUseCaseTest, PlaceWidgetUseCaseTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "CardDivider"
Cohesion: 0.20
Nodes (21): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+13 more)

### Community 87 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.24
Nodes (17): WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel() (+9 more)

### Community 90 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "AppInfo"
Cohesion: 0.11
Nodes (14): AppInfo, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+6 more)

### Community 93 - "HomeUiState"
Cohesion: 0.27
Nodes (13): HomeUiState, AppListVerticalAlignment, AppRowPosition, AppRowPresentation, ClockAlignment, ClockColorOption, ClockDateStyle, ClockFontOption (+5 more)

### Community 95 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 97 - "AppContextMenuTest"
Cohesion: 0.06
Nodes (10): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, DrawerViewModel, StateFlow, ViewModel, AppShortcutRepositoryTest (+2 more)

### Community 98 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 100 - "PlaceWidgetResult"
Cohesion: 0.50
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 102 - "BackupWidgetPlacement"
Cohesion: 0.23
Nodes (7): BackupAppEntry, BackupProfile, BackupWidgetPlacement, toBackupEntry(), toBackupPlacement(), toBackupProfile(), ImportBackupUseCaseTest

### Community 105 - "combine"
Cohesion: 0.11
Nodes (13): Flow, Flow, Flow, Flow, combine(), Flow, T1, T2 (+5 more)

### Community 106 - "BatteryStatus"
Cohesion: 0.28
Nodes (3): BatteryStatus, BatteryRepositoryTest, ObserveClockAccessoriesUseCaseTest

### Community 107 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 109 - "FavoriteAppEntity"
Cohesion: 0.08
Nodes (8): FavoriteAppDao, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 114 - "AppIcon"
Cohesion: 0.38
Nodes (9): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel(), Dp, Modifier, NotificationBadge() (+1 more)

### Community 116 - ".setContent"
Cohesion: 0.08
Nodes (11): SettingsScreenTest, DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, Intent, StateFlow (+3 more)

### Community 117 - "ClockAccessoryIcons.kt"
Cohesion: 0.31
Nodes (10): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+2 more)

### Community 119 - "HubWidgetTile"
Cohesion: 0.33
Nodes (7): AppWidgetHostView, Context, HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 121 - "BackupRestoreMessage"
Cohesion: 0.18
Nodes (10): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+2 more)

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "AppWidgetRepository.kt"
Cohesion: 0.27
Nodes (6): flattenIcon(), Bitmap, Bitmap, toBitmap(), SeedDefaultDockUseCase, Drawable

### Community 132 - "OnboardingProfilesPage.kt"
Cohesion: 0.38
Nodes (8): Modifier, OnboardingDots(), Modifier, MockProfile, MockProfileCard(), OnboardingProfilesPage(), OnboardingProfilesPagePreview(), ProfileSwitchDemo()

### Community 133 - "LauncherSettings"
Cohesion: 0.15
Nodes (3): LauncherSettings, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 134 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+2 more)

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "BackupRestoreContent"
Cohesion: 0.49
Nodes (9): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+1 more)

### Community 137 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 139 - ".setContent"
Cohesion: 0.11
Nodes (17): ProfileSettingsScreenTest, Modifier, RenameDialog(), Composable, Modifier, NavigationChevron(), ProfileSettingsHeader(), ProfileSettingsRow() (+9 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.29
Nodes (13): HomeSurfacePreview(), Color, FontWeight, Modifier, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent() (+5 more)

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "BackButton"
Cohesion: 0.15
Nodes (16): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+8 more)

### Community 146 - "BatteryRepository.kt"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus()

### Community 151 - "DefaultAppRepository"
Cohesion: 0.24
Nodes (3): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent

### Community 152 - "NextAlarmRepository.kt"
Cohesion: 0.36
Nodes (5): BroadcastReceiver, Context, Flow, Intent, BroadcastReceiver

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "ObserveProfilePreviewsUseCase"
Cohesion: 0.52
Nodes (4): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 160 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 161 - "SetDefaultLauncherSheet"
Cohesion: 0.62
Nodes (6): OnboardingUiState, AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 162 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 163 - "LumenDatabase"
Cohesion: 0.19
Nodes (6): DatabaseModule, Context, LumenDatabase, Migrations, Migration, RoomDatabase

### Community 164 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 166 - "AppContextMenu"
Cohesion: 0.80
Nodes (4): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier

### Community 167 - "InheritOverrideCard"
Cohesion: 0.90
Nodes (4): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow()

### Community 169 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.23
Nodes (7): ExportBackupUseCase, ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 187 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (14): BatteryRepository, NextAlarmRepository, AppInfo, CalendarEvent, Flow, LauncherSettings, ProfileEntity, ObserveHomeScreenStateUseCase (+6 more)

## Knowledge Gaps
- **353 isolated node(s):** `WeatherInfo`, `NONE`, `MENU`, `ADJUST`, `CHARGING` (+348 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 652 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `AppDrawerSettingsViewModel`, `.setContent`, `OnboardingProfilesPage.kt`, `HomeAppsListSettingsScreen.kt`, `BackupRestoreContent`, `CalendarSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `ListContentMode`, `ContactConnection`, `BackButton`, `WidgetPlacementRepository`, `.setContent`, `NotificationBadgeStyle`, `DefaultAppRepository`, `AppDrawerScreen.kt`, `HomeScreenTest`, `.setContent`, `SettingsRepository`, `ProfileCarouselScreen.kt`, `Row`, `.setContent`, `GestureHintOverlay`, `DragReorderState`, `SetDefaultLauncherSheet`, `Type.kt`, `Color.kt`, `.setContent`, `.setContent`, `LauncherFontOption`, `HomeScreen`, `.setContent`, `LauncherAppWidgetHost`, `CalendarPermissionRepository`, `AppDrawerScreen`, `FontWeightOption`, `ManageProfilesScreen.kt`, `ClockStyleGalleryScreen.kt`, `SettingsScreen.kt`, `.setContent`, `CalendarInfo`, `HomeWallpaper`, `ColorTest`, `DockAppPickerScreen.kt`, `.setContent`, `ComponentName`, `WidgetProviderOption`, `DockAppRepository`, `CardDivider`, `AppearanceSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `AppInfo`, `.setContent`, `AccentSwatch`, `AppContextMenuTest`, `StickyHeaderLayout`, `.setContent`, `.rendersOneEntryPerLetterProvided`, `.setContent`, `.setContent`, `ClockAdjustSheet.kt`?**
  _High betweenness centrality (0.181) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ProfileDockAppEntity`, `AppWidgetRepository.kt`, `HomeAppsListSettingsScreen.kt`, `DockSettingsScreen.kt`, `ProfileEntity`, `ListContentMode`, `ContactConnection`, `Fixture`, `DefaultAppRepository`, `AppDrawerScreen.kt`, `HomeScreenTest`, `GroupAppsByLetterUseCaseTest`, `ProfileCarouselScreen.kt`, `ObserveProfilePreviewsUseCase`, `ContactRepositoryTest`, `DefaultFavoriteAppEntity`, `SeedDefaultDockUseCaseTest`, `.setContent`, `OnboardingViewModelTest`, `.setContent`, `AppDrawerScreen`, `FontWeightOption`, `NotificationBadgeRepository`, `ClockColorOption`, `HomeDrawerRouteTest.kt`, `LauncherViewModel.kt`, `SettingsScreen.kt`, `.setContent`, `HomeWallpaper`, `LumenNavHost`, `DockAppEntity`, `DockAppPickerScreen.kt`, `ComponentName`, `DockAppRepository`, `.createViewModel`, `.useCase`, `Fixture`, `CardDivider`, `DockAppPickerViewModel`, `AppearanceSettingsScreen.kt`, `AppContextMenuTest`, `SettingsViewModelTest`, `combine`, `OnboardingViewModel`, `FavoriteAppEntity`, `AppIcon`, `.setContent`, `SelectPreviewAppsUseCaseTest`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `AppDrawerSettingsViewModel`, `.setContent`, `LauncherSettings`, `.setContent`, `ProfileDao`, `ProfileEntity`, `ListContentMode`, `BackButton`, `.setContent`, `NotificationBadgeStyle`, `ClockStyleGalleryViewModelTest`, `DefaultAppRepository`, `.setContent`, `.setContent`, `LauncherFontOption`, `BackupRestoreViewModelTest.kt`, `.setContent`, `CalendarPermissionRepository`, `ExportBackupUseCase.kt`, `FontWeightOption`, `.createViewModel`, `ClockColorOption`, `FakeProfileDao`, `HomeDrawerRouteTest.kt`, `LauncherViewModel.kt`, `.setContent`, `CalendarInfo`, `HomeWallpaper`, `CalendarSettingsViewModel`, `.setContent`, `DockAppRepository`, `.createViewModel`, `.useCase`, `AppearanceSettingsScreen.kt`, `SettingsRepositoryTest`, `OnboardingViewModel`, `.createViewModel`, `.setContent`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Are the 9 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`LumenLauncherTheme()` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 121 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 121 INFERRED edges - model-reasoned connections that need verification._
- **What connects `WeatherInfo`, `NONE`, `MENU` to the rest of the system?**
  _353 weakly-connected nodes found - possible documentation gaps or missing edges._