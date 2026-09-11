# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 325 files · ~693,840 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3275 nodes · 8409 edges · 182 communities (124 shown, 53 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 866 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9fbb381f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppRepository
- design_handoff_minimal_launcher/support.js
- PaddingValues
- HomeDrawerRouteTest.kt
- AppDrawerSettingsViewModelTest
- combine
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselUiState
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- FakeProfileDao
- FakeWidgetPlacementDao
- LumenDatabase
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementEntity
- SettingsRepository
- AppRowPresentation
- LumenLauncherTheme
- .setContent
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- AppearanceSettingsViewModel.kt
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- FakeDefaultFavoriteAppDao
- .setContent
- ClockAlignment
- ClockTemplateId
- Color.kt
- .setContent
- SettingsRepository.kt
- ProfileDao
- AppShortcutRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- NotificationShadeRepository
- LauncherAppWidgetHost
- NotificationAccessRepository
- Clock Widget Resize — Implementation Spec
- UsageStatsRepository
- AppDrawerScreen
- ClockStyleGalleryScreen.kt
- .createViewModel
- .setContent
- .setContent
- 4. Feature Requirements
- NotificationSettingsViewModel
- FakeProfileDao
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- DefaultFavoriteAppRepository
- .setContent
- HubWidgetPickerViewModel
- BackupMapping.kt
- SettingsScreen.kt
- DockAppEntity
- CalendarInfo
- github.md
- .setContent
- ColorTest
- LumenNavHost
- .setContent
- letterAt
- CalendarSettingsViewModel
- CardDivider
- DatabaseModule.kt
- .setContent
- NotificationBadgeRepository
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- HubWidgetPickerScreen.kt
- ObserveHomeScreenStateUseCase.kt
- FakeFavoriteAppDao
- .createViewModel
- .useCase
- FavoriteAppRepository
- Lumen Launcher Implementation Plan
- OnboardingHomeSetupPage.kt
- .setContent
- HomeAppsListSettingsViewModel.kt
- AppearanceSettingsScreen.kt
- AccentSwatch
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- WallpaperRepository
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- HubContent
- .createViewModel
- .setContent
- ProfileCarouselViewModel.kt
- Fixture
- FontWeightOption
- DefaultLauncherRepositoryTest
- HomeSurfacePreview
- HubGrid
- FavoritesPickerScreen.kt
- OnboardingViewModel
- AppContextMenuTest
- ContactRepository
- DrawerViewModelTest.kt
- AppRowPosition
- ProfileSettingsViewModel.kt
- ObserveProfilePreviewsUseCase.kt
- HomeAppsListSettingsScreen.kt
- LumenDatabaseMigrationTest
- DefaultLauncherRepository
- BackupRestoreScreen.kt
- CalendarPermissionRepository
- HomeViewModel
- ClockAdjustSheet.kt
- Modifier
- DrawerViewModel.kt
- LauncherActivity.kt
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DefaultAppRepositoryTest
- HomeAppsListSettingsViewModelTest.kt
- LauncherSettings
- .setContent
- Lumen Launcher — Onboarding Flow
- .setContent
- AppDrawerSettingsScreen.kt
- PermissionsViewModel
- ProfileSettingsContent
- DockSettingsScreen.kt
- LauncherFontOption
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- AppearanceSettingsViewModelTest.kt
- Fixture
- .homeViewModel
- .setContent
- CalendarSettingsScreen.kt
- .setContent
- .setContent
- 2. Design tokens
- AppModule.kt
- HubWidgetTile
- BackupRestoreViewModelTest.kt
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- ContactConnectionsSheet.kt
- ProfileEntityTest
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

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 191 edges
2. `ProfileEntity` - 168 edges
3. `SettingsRepository` - 142 edges
4. `Row` - 119 edges
5. `ProfileRepository` - 118 edges
6. `LauncherSettings` - 102 edges
7. `WidgetPlacementEntity` - 58 edges
8. `HomeScreen()` - 57 edges
9. `DefaultFavoriteAppRepository` - 53 edges
10. `ClockBlock()` - 53 edges

## Surprising Connections (you probably didn't know these)
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
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

## Communities (182 total, 53 thin omitted)

### Community 0 - "ProfileDockAppRepository"
Cohesion: 0.08
Nodes (11): Flow, ProfileDockAppDao, ProfileDockAppEntity, AppInfo, Flow, ProfileDockAppRepository, toProfileDockAppEntity(), ProfileDockAppDaoTest (+3 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "PaddingValues"
Cohesion: 0.17
Nodes (22): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+14 more)

### Community 3 - "HomeDrawerRouteTest.kt"
Cohesion: 0.14
Nodes (6): CleanUpUninstalledAppsUseCase, SeedDefaultDockUseCase, AppOpsManager, Context, NotificationShadeRepository, UsageStatsManager

### Community 5 - "combine"
Cohesion: 0.12
Nodes (15): Flow, combine(), Flow, FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel (+7 more)

### Community 6 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ProfileCarouselUiState"
Cohesion: 0.18
Nodes (3): ClockColorOption, ClockFontOption, ProfileCarouselUiState

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "FakeProfileDao"
Cohesion: 0.12
Nodes (4): FakeProfileDao, Flow, ProfileDao, ProfileRepositoryTest

### Community 13 - "FakeWidgetPlacementDao"
Cohesion: 0.17
Nodes (5): Flow, WidgetPlacementDao, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "LumenDatabase"
Cohesion: 0.14
Nodes (10): DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, LumenDatabase, AppRepository, LauncherApps (+2 more)

### Community 15 - "ContactConnection"
Cohesion: 0.17
Nodes (13): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+5 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.11
Nodes (12): WidgetPlacementEntity, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow (+4 more)

### Community 19 - "SettingsRepository"
Cohesion: 0.05
Nodes (9): ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, FontWeightOption, LauncherFontOption, ListContentMode (+1 more)

### Community 20 - "AppRowPresentation"
Cohesion: 0.15
Nodes (13): AppListLimits, AppListVerticalAlignment, BOTTOM, TOP, AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY (+5 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.11
Nodes (10): ClockBlockTest, CalendarEvent, ClockDateStyle, CONDENSED, FULL, ClockBlock(), ClockBlockPreview(), Modifier (+2 more)

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

### Community 28 - "AppearanceSettingsViewModel.kt"
Cohesion: 0.19
Nodes (7): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.15
Nodes (23): AddProfilePage(), AppInfo, CalendarEvent, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption (+15 more)

### Community 30 - "Row"
Cohesion: 0.14
Nodes (80): Alignment, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate(), ClockDisplay(), ClockGlyphText() (+72 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (21): AppRepository, Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, any() (+13 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 35 - "ClockAlignment"
Cohesion: 0.07
Nodes (11): ClockAlignment, CENTER, LEFT, RIGHT, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel (+3 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "Color.kt"
Cohesion: 0.08
Nodes (29): dashedBorder(), Color, Dp, Modifier, ImageVector, Modifier, TonalButton(), HubAtCapacityStrip() (+21 more)

### Community 39 - "SettingsRepository.kt"
Cohesion: 0.07
Nodes (23): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+15 more)

### Community 40 - "ProfileDao"
Cohesion: 0.21
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 41 - "AppShortcutRepository"
Cohesion: 0.20
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 43 - "HomeScreen.kt"
Cohesion: 0.11
Nodes (37): AppContextMenu(), AppInfo, Modifier, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel() (+29 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.18
Nodes (6): Fixture, AppInfo, HomeWallpaper, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.23
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 47 - "NotificationAccessRepository"
Cohesion: 0.19
Nodes (6): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "UsageStatsRepository"
Cohesion: 0.34
Nodes (3): AppInfo, UsageStatsRepository, UsageStatsRepositoryTest

### Community 50 - "AppDrawerScreen"
Cohesion: 0.10
Nodes (8): AppDrawerScreenTest, AppInfo, GroupAppsByLetterUseCase, GroupedApps, AppDrawerScreen(), GetInstalledAppsUseCaseTest, DrawerGridSize, SearchBarPosition

### Community 51 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.33
Nodes (10): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+2 more)

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "NotificationSettingsViewModel"
Cohesion: 0.43
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 57 - "FakeProfileDao"
Cohesion: 0.16
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.16
Nodes (9): BackupRestoreEvent, LaunchBindPermission, LaunchConfigure, PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri (+1 more)

### Community 60 - "DefaultFavoriteAppRepository"
Cohesion: 0.16
Nodes (8): DefaultFavoriteAppRepository, AppInfo, DefaultFavoriteAppEntity, Flow, ExportBackupUseCase, Uri, toBackupSettings(), ExportBackupUseCaseTest

### Community 61 - ".setContent"
Cohesion: 0.14
Nodes (9): KeyboardDismissalTest, GetInstalledAppsUseCase, Flow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest (+1 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.09
Nodes (17): WidgetProviderOption, AddFailed, AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubAddWidgetEvent, HubWidgetPickerUiState, LaunchBindPermission (+9 more)

### Community 63 - "BackupMapping.kt"
Cohesion: 0.11
Nodes (21): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, DefaultFavoriteAppEntity (+13 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appsListSummary(), ClickableRow(), Composable, ListContentMode, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.09
Nodes (8): DockAppRepository, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 66 - "CalendarInfo"
Cohesion: 0.20
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".setContent"
Cohesion: 0.23
Nodes (6): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, AppInfo, StateFlow, ViewModel

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (6): AppInfo, Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 73 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 74 - "CardDivider"
Cohesion: 0.26
Nodes (15): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+7 more)

### Community 75 - "DatabaseModule.kt"
Cohesion: 0.17
Nodes (8): DatabaseModule, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, Migrations, Migration

### Community 77 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "HubWidgetPickerScreen.kt"
Cohesion: 0.38
Nodes (11): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), HubWidgetPickerViewModel, Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+3 more)

### Community 80 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.35
Nodes (5): HomeScreenState, AppInfo, CalendarEvent, Flow, ObserveHomeScreenStateUseCase

### Community 81 - "FakeFavoriteAppDao"
Cohesion: 0.10
Nodes (7): FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 82 - ".createViewModel"
Cohesion: 0.22
Nodes (6): DockSettingsViewModelTest, WallpaperRepository, AppInfo, DockAppRepository, HomeWallpaper, WallpaperRepository

### Community 83 - ".useCase"
Cohesion: 0.05
Nodes (11): AssignCalendarColorsUseCaseTest, CleanUpUninstalledAppsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, AppInfo (+3 more)

### Community 84 - "FavoriteAppRepository"
Cohesion: 0.30
Nodes (4): FavoriteAppRepository, AppInfo, FavoriteAppEntity, Flow

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.19
Nodes (18): DragReorderState, Modifier, T, rememberDragReorderState(), detectHomeSwipeGestures(), DockAppsReorderRow(), DockSection(), FavoritesClickableRow() (+10 more)

### Community 88 - "HomeAppsListSettingsViewModel.kt"
Cohesion: 0.20
Nodes (6): HomeAppsListSettingsViewModel, HomeAppsListUiState, AppInfo, ListContentMode, StateFlow, ViewModel

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.18
Nodes (21): AccentSwatch, WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid() (+13 more)

### Community 90 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.39
Nodes (7): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground()

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.13
Nodes (20): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection (+12 more)

### Community 97 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 99 - ".setContent"
Cohesion: 0.24
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 100 - "ProfileCarouselViewModel.kt"
Cohesion: 0.18
Nodes (9): T, resolveOverride(), ClockDateStyle, ClockTemplateId, FontWeightOption, ListContentMode, StateFlow, ViewModel (+1 more)

### Community 102 - "FontWeightOption"
Cohesion: 0.15
Nodes (11): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+3 more)

### Community 104 - "HomeSurfacePreview"
Cohesion: 0.22
Nodes (9): NotificationBadgeStyle, COUNT, DOT, HomeSurfacePreview(), AppInfo, Color, FontWeight, HomeWallpaper (+1 more)

### Community 105 - "HubGrid"
Cohesion: 0.17
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 106 - "FavoritesPickerScreen.kt"
Cohesion: 0.07
Nodes (37): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), AppInfo, Modifier (+29 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.26
Nodes (6): AppInfo, Intent, ListContentMode, StateFlow, ViewModel, OnboardingViewModel

### Community 110 - "DrawerViewModelTest.kt"
Cohesion: 0.23
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 111 - "AppRowPosition"
Cohesion: 0.06
Nodes (21): Converters, ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockFontOption, LAUNCHER_DEFAULT (+13 more)

### Community 112 - "ProfileSettingsViewModel.kt"
Cohesion: 0.21
Nodes (11): AppInfo, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption, ListContentMode, StateFlow (+3 more)

### Community 113 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.40
Nodes (6): Inputs, AppInfo, CalendarEvent, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 114 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.32
Nodes (11): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), AppInfo (+3 more)

### Community 116 - "DefaultLauncherRepository"
Cohesion: 0.11
Nodes (11): DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, Intent, StateFlow, ViewModel (+3 more)

### Community 117 - "BackupRestoreScreen.kt"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 118 - "CalendarPermissionRepository"
Cohesion: 0.23
Nodes (3): FakeCalendarPermissionRepository, CalendarPermissionRepository, PermissionsViewModelTest

### Community 119 - "HomeViewModel"
Cohesion: 0.22
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 122 - "DrawerViewModel.kt"
Cohesion: 0.29
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

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
Nodes (10): LauncherSettings, HomeUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption, ListContentMode (+2 more)

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 138 - "PermissionsViewModel"
Cohesion: 0.16
Nodes (9): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState, StateFlow, ViewModel (+1 more)

### Community 139 - "ProfileSettingsContent"
Cohesion: 0.23
Nodes (13): Modifier, RenameDialog(), Composable, Modifier, NavigationChevron(), ProfileSettingsHeader(), ProfileSettingsRow(), SectionHeader() (+5 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+4 more)

### Community 141 - "LauncherFontOption"
Cohesion: 0.13
Nodes (13): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+5 more)

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

### Community 150 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

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

### Community 159 - "ContactConnectionsSheet.kt"
Cohesion: 0.50
Nodes (7): ConnectionRow(), ContactConnectionsSheet(), fallbackIcon(), androidx, ImageVector, Modifier, subtitle()

### Community 161 - "AlphabetRail"
Cohesion: 0.38
Nodes (4): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale()

### Community 169 - "BackupRestoreMessage"
Cohesion: 0.33
Nodes (6): BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded

### Community 170 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 172 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

## Knowledge Gaps
- **349 isolated node(s):** `NONE`, `MENU`, `ADJUST`, `LumenType`, `Multiple` (+344 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 628 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `PaddingValues`, `.setContent`, `.setContent`, `AppDrawerSettingsScreen.kt`, `ProfileSettingsContent`, `DockSettingsScreen.kt`, `LauncherFontOption`, `ContactConnection`, `BackButton`, `WidgetPlacementEntity`, `AppearanceSettingsViewModelTest.kt`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `HomeScreen`, `CalendarSettingsScreen.kt`, `AppDrawerScreen.kt`, `AppearanceSettingsViewModel.kt`, `ProfileCarouselScreen.kt`, `Row`, `ContactConnectionsSheet.kt`, `.setContent`, `AlphabetRail`, `.setContent`, `Color.kt`, `.setContent`, `SettingsRepository.kt`, `AppShortcutRepository`, `Type.kt`, `HomeScreen.kt`, `NotificationAccessRepository`, `AppDrawerScreen`, `ClockStyleGalleryScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `SettingsScreen.kt`, `CalendarInfo`, `.setContent`, `ColorTest`, `.setContent`, `CardDivider`, `.setContent`, `HubWidgetPickerScreen.kt`, `OnboardingHomeSetupPage.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `AccentSwatch`, `HomeDrawerRoute`, `.setContent`, `.setContent`, `FontWeightOption`, `HomeSurfacePreview`, `HubGrid`, `FavoritesPickerScreen.kt`, `AppContextMenuTest`, `HomeAppsListSettingsScreen.kt`, `BackupRestoreScreen.kt`, `ClockAdjustSheet.kt`, `LauncherActivity.kt`?**
  _High betweenness centrality (0.181) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `HomeDrawerRouteTest.kt`, `AppDrawerSettingsViewModelTest`, `LauncherSettings`, `.setContent`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `PermissionsViewModel`, `ManageProfilesViewModel`, `LumenDatabase`, `BackButton`, `AppearanceSettingsViewModelTest.kt`, `AppRowPresentation`, `.homeViewModel`, `.setContent`, `.setContent`, `ProfileEntity`, `AppearanceSettingsViewModel.kt`, `.setContent`, `ClockAlignment`, `.setContent`, `SettingsRepository.kt`, `NotificationAccessRepository`, `.createViewModel`, `.setContent`, `.setContent`, `NotificationSettingsViewModel`, `FakeProfileDao`, `DefaultFavoriteAppRepository`, `.setContent`, `BackupMapping.kt`, `CalendarInfo`, `.setContent`, `.setContent`, `CalendarSettingsViewModel`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.createViewModel`, `.useCase`, `.setContent`, `HomeAppsListSettingsViewModel.kt`, `AppearanceSettingsScreen.kt`, `.setContent`, `SettingsRepositoryTest`, `.createViewModel`, `ProfileCarouselViewModel.kt`, `FontWeightOption`, `HomeSurfacePreview`, `OnboardingViewModel`, `DrawerViewModelTest.kt`, `ProfileSettingsViewModel.kt`, `ObserveProfilePreviewsUseCase.kt`, `DefaultLauncherRepository`, `CalendarPermissionRepository`, `HomeViewModel`, `DrawerViewModel.kt`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `ProfileEntity` connect `ProfileEntity` to `ProfileDockAppRepository`, `PaddingValues`, `HomeDrawerRouteTest.kt`, `HomeAppsListSettingsViewModelTest.kt`, `LauncherSettings`, `ManageProfilesViewModel`, `FakeProfileDao`, `LumenDatabase`, `Fixture`, `AppRowPresentation`, `.homeViewModel`, `.setContent`, `ProfileCarouselScreen.kt`, `ProfileEntityTest`, `ClockAlignment`, `ProfileDao`, `FakeProfileDao`, `DefaultFavoriteAppRepository`, `BackupMapping.kt`, `.setContent`, `CalendarSettingsViewModel`, `ObserveHomeScreenStateUseCase.kt`, `FakeFavoriteAppDao`, `.createViewModel`, `.createViewModel`, `ProfileCarouselViewModel.kt`, `Fixture`, `FavoritesPickerScreen.kt`, `ProfileSettingsViewModel.kt`, `ObserveProfilePreviewsUseCase.kt`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Are the 59 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo()`) actually correct?**
  _`LumenLauncherTheme()` has 59 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 114 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 114 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `ProfileRepository` (e.g. with `.`addProfile names it Profile N and appends after the last position`()` and `.`deleteProfile removes it`()`) actually correct?**
  _`ProfileRepository` has 19 INFERRED edges - model-reasoned connections that need verification._