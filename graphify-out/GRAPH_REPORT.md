# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 334 files · ~698,485 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3185 nodes · 8677 edges · 154 communities (110 shown, 38 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 653 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c8506ce9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppEntity
- design_handoff_minimal_launcher/support.js
- HubGrid
- .homeViewModel
- AppDrawerSettingsViewModel
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselViewModel.kt
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- FakeProfileDao
- FakeWidgetPlacementDao
- ClockFontOption
- AppDrawerScreen.kt
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementEntity
- .setContent
- CardDivider
- LumenLauncherTheme
- ClockStyleGalleryViewModel
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- UsageStatsRepository
- NotificationBadgeStyle
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- DefaultFavoriteAppEntity
- dashedBorder
- OnboardingScreen
- ClockTemplateId
- TonalButton
- .setContent
- .setContent
- ObserveHubStateUseCase
- LauncherFontOption
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- HomeDrawerRouteTest.kt
- WidgetPlacementRepository
- CalendarPermissionRepository
- Clock Widget Resize — Implementation Spec
- BackupMapping.kt
- AppDrawerScreen
- FontWeightOption
- .createViewModel
- ManageProfilesScreen.kt
- NotificationBadgeRepository
- 4. Feature Requirements
- ClockColorOption
- EnsureActiveProfileUseCase
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- AppWidgetRepository.kt
- AppRepository
- HubWidgetPickerViewModel
- StickyHeaderLayout
- SettingsScreen.kt
- .setContent
- AccentSwatch
- github.md
- DockAppRepository
- ColorTest
- LumenNavHost
- DockAppEntity
- letterAt
- WidgetPlacementDao
- DockAppPickerScreen.kt
- ProfileDao
- .setContent
- LauncherActivity.kt
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- WidgetProviderOption
- LabeledDropdownRow
- AppInfo
- .createViewModel
- .useCase
- Fixture
- Lumen Launcher Implementation Plan
- OnboardingHomeSetupPage.kt
- DockAppPickerViewModel
- DefaultAppRepositoryTest
- AppearanceSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Lumen Launcher — Built Capabilities
- FavoritesPickerViewModel
- UsageAccessExplanationScreen.kt
- .setContent
- WallpaperRepositoryTest
- SettingsRepositoryTest
- AppContextMenuTest
- PermissionsScreen.kt
- HubAddWidgetEvent
- PlaceWidgetResult
- Fixture
- .setContent
- .setContent
- SettingsViewModelTest
- combine
- AddAppToDockUseCaseTest
- OnboardingViewModel
- DefaultLauncherRepositoryTest
- FavoriteAppEntity
- DrawerViewModelTest
- AddAppToFavoritesUseCaseTest
- .rendersOneEntryPerLetterProvided
- .setContent
- FavoritesPickerScreen.kt
- LumenDatabaseMigrationTest
- .setContent
- HubContent
- HubWidgetTile
- ClockAdjustSheet.kt
- DefaultLauncherRepository
- Migrations
- Modifier
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .createViewModel
- LauncherSettings
- HomeAppsListSettingsScreen.kt
- Lumen Launcher — Onboarding Flow
- .setContent
- DockSettingsScreen.kt
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- Fixture
- .setContent
- SettingsViewModel
- DefaultAppRepository
- 2. Design tokens
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- LumenDatabase
- SettingsRepository
- ProfileEntityTest
- ClockCornerHandle
- ListContentMode

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 224 edges
2. `AppInfo` - 182 edges
3. `ProfileEntity` - 178 edges
4. `SettingsRepository` - 148 edges
5. `ProfileRepository` - 124 edges
6. `Row` - 123 edges
7. `LauncherSettings` - 108 edges
8. `ClockTemplateId` - 73 edges
9. `WidgetPlacementEntity` - 61 edges
10. `ClockDateStyle` - 61 edges

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

## Communities (154 total, 38 thin omitted)

### Community 0 - "ProfileDockAppEntity"
Cohesion: 0.08
Nodes (8): Flow, ProfileDockAppDao, ProfileDockAppEntity, toProfileDockAppEntity(), ProfileDockAppDaoTest, FakeProfileDockAppDao, Flow, ProfileDockAppRepositoryTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "HubGrid"
Cohesion: 0.16
Nodes (14): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+6 more)

### Community 3 - ".homeViewModel"
Cohesion: 0.20
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 4 - "AppDrawerSettingsViewModel"
Cohesion: 0.10
Nodes (5): AppDrawerSettingsScreenTest, AppDrawerSettingsViewModel, StateFlow, ViewModel, AppDrawerSettingsViewModelTest

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ProfileCarouselViewModel.kt"
Cohesion: 0.13
Nodes (4): StateFlow, ViewModel, ProfileCarouselUiState, ProfileCarouselViewModel

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (6): StateFlow, ViewModel, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "FakeProfileDao"
Cohesion: 0.13
Nodes (3): FakeProfileDao, Flow, ProfileRepositoryTest

### Community 13 - "FakeWidgetPlacementDao"
Cohesion: 0.32
Nodes (3): FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockFontOption"
Cohesion: 0.09
Nodes (9): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+1 more)

### Community 15 - "AppDrawerScreen.kt"
Cohesion: 0.05
Nodes (63): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+55 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (8): WidgetPlacementEntity, Flow, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 20 - "CardDivider"
Cohesion: 0.21
Nodes (12): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+4 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "ClockStyleGalleryViewModel"
Cohesion: 0.08
Nodes (5): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.09
Nodes (4): ProfileEntity, Flow, ProfileRepository, toCsv()

### Community 25 - "HomeScreen"
Cohesion: 0.14
Nodes (3): HomeScreenTest, HomeScreen(), HomeScreenTextOnlyPresentationPreview()

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "NotificationBadgeStyle"
Cohesion: 0.06
Nodes (31): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE (+23 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.15
Nodes (22): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground(), AddProfilePage() (+14 more)

### Community 30 - "Row"
Cohesion: 0.14
Nodes (79): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+71 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "DefaultFavoriteAppEntity"
Cohesion: 0.12
Nodes (7): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 34 - "dashedBorder"
Cohesion: 0.24
Nodes (10): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier (+2 more)

### Community 35 - "OnboardingScreen"
Cohesion: 0.22
Nodes (12): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT (+4 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview() (+4 more)

### Community 40 - "ObserveHubStateUseCase"
Cohesion: 0.27
Nodes (4): HubWidgetState, Flow, ObserveHubStateUseCase, ObserveHubStateUseCaseTest

### Community 41 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.21
Nodes (19): HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow(), dashedBorder(), DockIcon(), Color (+11 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 45 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (13): KeyboardDismissalTest, NotificationShadeRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, Flow, ObserveQuickAddStateUseCase, QuickAddState, T (+5 more)

### Community 46 - "WidgetPlacementRepository"
Cohesion: 0.11
Nodes (15): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, WidgetPlacementRepository (+7 more)

### Community 47 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (12): FakeCalendarRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "BackupMapping.kt"
Cohesion: 0.16
Nodes (14): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, T (+6 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 51 - "FontWeightOption"
Cohesion: 0.16
Nodes (11): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+3 more)

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 53 - "ManageProfilesScreen.kt"
Cohesion: 0.45
Nodes (10): AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList() (+2 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.11
Nodes (11): Callback, Callback, LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, Callback (+3 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "ClockColorOption"
Cohesion: 0.10
Nodes (17): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+9 more)

### Community 57 - "EnsureActiveProfileUseCase"
Cohesion: 0.15
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.09
Nodes (29): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen() (+21 more)

### Community 60 - "AppWidgetRepository.kt"
Cohesion: 0.13
Nodes (10): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+2 more)

### Community 61 - "AppRepository"
Cohesion: 0.08
Nodes (17): AppRepository, flattenIcon(), Bitmap, Flow, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, SeedDefaultDockUseCase (+9 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.14
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 63 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (8): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 66 - "AccentSwatch"
Cohesion: 0.05
Nodes (30): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarPermissionRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader() (+22 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "DockAppRepository"
Cohesion: 0.11
Nodes (9): DockSettingsScreenTest, DockAppRepository, Flow, Bitmap, WallpaperRepository, DockSettingsUiState, DockSettingsViewModel, StateFlow (+1 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "DockAppEntity"
Cohesion: 0.10
Nodes (8): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 74 - "DockAppPickerScreen.kt"
Cohesion: 0.50
Nodes (8): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader()

### Community 75 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 77 - "LauncherActivity.kt"
Cohesion: 0.36
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "WidgetProviderOption"
Cohesion: 0.22
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

### Community 80 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 81 - "AppInfo"
Cohesion: 0.11
Nodes (7): DefaultFavoriteAppRepository, Flow, FavoriteAppRepository, Flow, AppInfo, Flow, ProfileDockAppRepository

### Community 82 - ".createViewModel"
Cohesion: 0.26
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 83 - ".useCase"
Cohesion: 0.05
Nodes (10): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, DeleteWidgetUseCaseTest, GroupAppsByLetterUseCaseTest, ImportBackupUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest (+2 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.17
Nodes (19): ConfirmDialog(), Modifier, DragReorderState, Modifier, T, rememberDragReorderState(), detectHomeSwipeGestures(), AppDrawerSection() (+11 more)

### Community 87 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 90 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 93 - "UsageAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 94 - ".setContent"
Cohesion: 0.06
Nodes (26): HomeDrawerRouteTest, GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), ClockAdjustMode, ADJUST (+18 more)

### Community 97 - "AppContextMenuTest"
Cohesion: 0.10
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 98 - "PermissionsScreen.kt"
Cohesion: 0.23
Nodes (13): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionKind, CALENDAR (+5 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 100 - "PlaceWidgetResult"
Cohesion: 0.50
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 105 - "combine"
Cohesion: 0.16
Nodes (13): combine(), Flow, Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, T1, T2 (+5 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 109 - "FavoriteAppEntity"
Cohesion: 0.09
Nodes (8): FavoriteAppDao, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 114 - "FavoritesPickerScreen.kt"
Cohesion: 0.61
Nodes (7): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 117 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 119 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 133 - "LauncherSettings"
Cohesion: 0.17
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 134 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 139 - ".setContent"
Cohesion: 0.12
Nodes (18): ProfileSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable (+10 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+1 more)

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "BackButton"
Cohesion: 0.26
Nodes (9): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+1 more)

### Community 150 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 151 - "DefaultAppRepository"
Cohesion: 0.16
Nodes (4): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 163 - "LumenDatabase"
Cohesion: 0.11
Nodes (10): AppModule, Context, DatabaseModule, Context, LumenDatabase, AppOpsManager, LauncherApps, RoomDatabase (+2 more)

### Community 169 - "SettingsRepository"
Cohesion: 0.04
Nodes (5): Flow, SettingsRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

### Community 187 - "ListContentMode"
Cohesion: 0.08
Nodes (23): T, resolveOverride(), AppListLimits, AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT (+15 more)

## Knowledge Gaps
- **348 isolated node(s):** `HubFull`, `Multiple`, `Single`, `Keys`, `ExportFailed` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 630 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **38 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HubGrid`, `AppDrawerSettingsViewModel`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `AppDrawerScreen.kt`, `BackButton`, `WidgetPlacementEntity`, `.setContent`, `CardDivider`, `.setContent`, `DefaultAppRepository`, `HomeScreen`, `NotificationBadgeStyle`, `ProfileCarouselScreen.kt`, `Row`, `.setContent`, `dashedBorder`, `LumenDatabase`, `TonalButton`, `.setContent`, `.setContent`, `SettingsRepository`, `LauncherFontOption`, `HomeScreen.kt`, `HomeDrawerRouteTest.kt`, `WidgetPlacementRepository`, `CalendarPermissionRepository`, `AppDrawerScreen`, `FontWeightOption`, `ManageProfilesScreen.kt`, `BackupRestoreViewModel`, `ListContentMode`, `StickyHeaderLayout`, `SettingsScreen.kt`, `.setContent`, `AccentSwatch`, `DockAppRepository`, `ColorTest`, `DockAppPickerScreen.kt`, `.setContent`, `LauncherActivity.kt`, `WidgetProviderOption`, `AppInfo`, `OnboardingHomeSetupPage.kt`, `AppearanceSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `UsageAccessExplanationScreen.kt`, `.setContent`, `AppContextMenuTest`, `PermissionsScreen.kt`, `.setContent`, `.setContent`, `.rendersOneEntryPerLetterProvided`, `.setContent`, `FavoritesPickerScreen.kt`, `.setContent`, `ClockAdjustSheet.kt`?**
  _High betweenness centrality (0.150) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ProfileDockAppEntity`, `.homeViewModel`, `.createViewModel`, `HomeAppsListSettingsScreen.kt`, `ProfileCarouselViewModel.kt`, `DockSettingsScreen.kt`, `AppDrawerScreen.kt`, `Fixture`, `DefaultAppRepository`, `HomeScreen`, `UsageStatsRepository`, `NotificationBadgeStyle`, `ProfileCarouselScreen.kt`, `ContactRepositoryTest`, `DefaultFavoriteAppEntity`, `LumenDatabase`, `SettingsRepository`, `HomeScreen.kt`, `OnboardingViewModelTest`, `HomeDrawerRouteTest.kt`, `CalendarPermissionRepository`, `AppDrawerScreen`, `ClockColorOption`, `ListContentMode`, `AppRepository`, `SettingsScreen.kt`, `.setContent`, `DockAppRepository`, `LumenNavHost`, `DockAppEntity`, `DockAppPickerScreen.kt`, `LauncherActivity.kt`, `.createViewModel`, `.useCase`, `Fixture`, `OnboardingHomeSetupPage.kt`, `DockAppPickerViewModel`, `AppearanceSettingsScreen.kt`, `FavoritesPickerViewModel`, `.setContent`, `AppContextMenuTest`, `Fixture`, `SettingsViewModelTest`, `combine`, `AddAppToDockUseCaseTest`, `OnboardingViewModel`, `FavoriteAppEntity`, `AddAppToFavoritesUseCaseTest`, `FavoritesPickerScreen.kt`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `.homeViewModel`, `AppDrawerSettingsViewModel`, `.setContent`, `LauncherSettings`, `.createViewModel`, `ProfileCarouselViewModel.kt`, `.setContent`, `ManageProfilesViewModel`, `AppDrawerScreen.kt`, `BackButton`, `.setContent`, `CardDivider`, `ClockStyleGalleryViewModel`, `DefaultAppRepository`, `NotificationBadgeStyle`, `LumenDatabase`, `ClockTemplateId`, `.setContent`, `.setContent`, `LauncherFontOption`, `HomeDrawerRouteTest.kt`, `WidgetPlacementRepository`, `CalendarPermissionRepository`, `BackupMapping.kt`, `.createViewModel`, `ClockColorOption`, `EnsureActiveProfileUseCase`, `ListContentMode`, `AppRepository`, `.setContent`, `AccentSwatch`, `DockAppRepository`, `.setContent`, `AppInfo`, `.createViewModel`, `.useCase`, `.setContent`, `SettingsRepositoryTest`, `combine`, `OnboardingViewModel`, `.setContent`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `ProfileRepository` (e.g. with `.`addProfile names it Profile N and appends after the last position`()` and `.`deleteProfile removes it`()`) actually correct?**
  _`ProfileRepository` has 19 INFERRED edges - model-reasoned connections that need verification._
- **What connects `HubFull`, `Multiple`, `Single` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._