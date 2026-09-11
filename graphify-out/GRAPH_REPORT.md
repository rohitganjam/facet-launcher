# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 327 files · ~695,651 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3137 nodes · 8528 edges · 159 communities (117 shown, 37 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 650 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `26efac00`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppRepository
- design_handoff_minimal_launcher/support.js
- HubGrid
- HomeViewModel
- AppDrawerSettingsViewModelTest
- FavoritesPickerViewModel
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselUiState
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- ImportBackupUseCase.kt
- WidgetPlacementRepository
- AppRepository
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementEntity
- SettingsRepository
- BackupMapping.kt
- ClockBlock
- ClockFontOption
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
- DefaultFavoriteAppRepository
- .setContent
- ClockStyleGalleryViewModelTest
- ClockTemplateId
- homeAppLabelShadow
- .setContent
- .setContent
- AppShortcutRepository
- DockAppPickerViewModel
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- .setContent
- LauncherAppWidgetHost
- HomeDrawerRouteTest.kt
- Clock Widget Resize — Implementation Spec
- ImportBackupUseCaseTest.kt
- LumenLauncherTheme
- ClockStyleGalleryScreen.kt
- .setContent
- ProfileCarouselViewModel.kt
- NotificationBadgeRepository
- 4. Feature Requirements
- CalendarSettingsViewModel
- SettingsRepositoryTest.kt
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- SetDefaultLauncherSheet
- GetInstalledAppsUseCase
- HubWidgetPickerViewModel
- AccentSwatch
- SettingsScreen.kt
- DockAppEntity
- CalendarRepository
- github.md
- .setContent
- ColorTest
- LumenNavHost
- FakeDockAppDao
- letterAt
- .createViewModel
- DockAppRepository
- ProfileDao
- .setContent
- DrawerViewModel
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- HubWidgetPickerScreen.kt
- ManageProfilesScreen.kt
- AppInfo
- .createViewModel
- .useCase
- BackupRestoreMessage
- Lumen Launcher Implementation Plan
- CardDivider
- .setContent
- CalendarSettingsScreen.kt
- AppearanceSettingsScreen.kt
- LabeledDropdownRow
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- UsageAccessExplanationScreen.kt
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- AppContextMenuTest
- StickyHeaderLayout
- HubAddWidgetEvent
- DockAppPickerScreen.kt
- Fixture
- ContactRepository
- .setContent
- SettingsViewModelTest
- combine
- OnboardingScreen
- OnboardingViewModel
- DefaultLauncherRepositoryTest
- FavoriteAppDaoTest
- DrawerViewModelTest
- .`a valid backup replaces every profile, dock, and default favorite, then reports what was restored`
- dashedBorder
- .setContent
- InheritOverrideCard
- LumenDatabaseMigrationTest
- .setContent
- BackupRestoreContent
- HubAtCapacityStrip.kt
- ClockAdjustSheet.kt
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- HomeAppsListSettingsViewModelTest.kt
- LauncherSettings
- HomeAppsListSettingsScreen.kt
- Lumen Launcher — Onboarding Flow
- WallpaperRepositoryTest
- PermissionKind
- .setContent
- DockSettingsScreen.kt
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- Fixture
- .setContent
- SettingsViewModel
- .setContent
- 2. Design tokens
- HubWidgetTile
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- LumenDatabase
- DefaultLauncherRepository
- Type.kt
- ProfileEntityTest
- ClockAdjustMode
- ClockCornerHandle
- WidgetResizeHandle
- GestureHintOverlay
- FontWeightOption
- WallpaperRepository

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 224 edges
2. `AppInfo` - 172 edges
3. `ProfileEntity` - 168 edges
4. `SettingsRepository` - 142 edges
5. `Row` - 122 edges
6. `ProfileRepository` - 118 edges
7. `LauncherSettings` - 102 edges
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

## Communities (159 total, 37 thin omitted)

### Community 0 - "ProfileDockAppRepository"
Cohesion: 0.08
Nodes (10): Flow, ProfileDockAppDao, ProfileDockAppEntity, Flow, ProfileDockAppRepository, toProfileDockAppEntity(), ProfileDockAppDaoTest, FakeProfileDockAppDao (+2 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "HubGrid"
Cohesion: 0.18
Nodes (17): Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), HubGrid(), AppWidgetHostView (+9 more)

### Community 3 - "HomeViewModel"
Cohesion: 0.14
Nodes (4): HomeViewModel, StateFlow, ViewModel, HomeViewModelTest

### Community 5 - "FavoritesPickerViewModel"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (6): StateFlow, ViewModel, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "ImportBackupUseCase.kt"
Cohesion: 0.08
Nodes (19): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerPresentation, GRID, LIST (+11 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.13
Nodes (7): Flow, WidgetPlacementRepository, DeleteWidgetUseCase, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 14 - "AppRepository"
Cohesion: 0.09
Nodes (14): AppRepository, flattenIcon(), Bitmap, AppModule, Context, Bitmap, WallpaperRepository, AppRepositoryTest (+6 more)

### Community 15 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.10
Nodes (14): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, HubFull, Placed (+6 more)

### Community 19 - "SettingsRepository"
Cohesion: 0.05
Nodes (7): ManageProfilesScreenTest, ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, SettingsRepository

### Community 20 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 21 - "ClockBlock"
Cohesion: 0.09
Nodes (6): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale()

### Community 22 - "ClockFontOption"
Cohesion: 0.06
Nodes (17): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockAlignment (+9 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.06
Nodes (6): ProfileEntity, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.17
Nodes (25): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase (+17 more)

### Community 28 - "LauncherFontOption"
Cohesion: 0.06
Nodes (23): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, LauncherFontOption, MANROPE, NOTO_SANS, POPPINS (+15 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+6 more)

### Community 30 - "Row"
Cohesion: 0.16
Nodes (72): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+64 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.13
Nodes (7): any(), eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, MatrixCursor

### Community 32 - ".setContent"
Cohesion: 0.13
Nodes (20): HubGestureTest, Offset, T, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier (+12 more)

### Community 33 - "DefaultFavoriteAppRepository"
Cohesion: 0.09
Nodes (9): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow (+1 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "homeAppLabelShadow"
Cohesion: 0.24
Nodes (12): Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview(), Color, resolve(), accentTonalExtremes(), homeAppLabelShadow(), Color (+4 more)

### Community 39 - ".setContent"
Cohesion: 0.12
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 40 - "AppShortcutRepository"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 41 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 43 - "HomeScreen.kt"
Cohesion: 0.19
Nodes (20): AppContextMenu(), Modifier, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel(), Dp (+12 more)

### Community 45 - ".setContent"
Cohesion: 0.24
Nodes (3): KeyboardDismissalTest, NotificationShadeRepository, NotificationShadeRepositoryTest

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.20
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 47 - "HomeDrawerRouteTest.kt"
Cohesion: 0.08
Nodes (16): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, CleanUpUninstalledAppsUseCase (+8 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.23
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 50 - "LumenLauncherTheme"
Cohesion: 0.16
Nodes (5): AlphabetRailTest, AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview(), LumenLauncherTheme()

### Community 51 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.36
Nodes (9): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+1 more)

### Community 52 - ".setContent"
Cohesion: 0.07
Nodes (8): AppearanceSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase, DefaultAppRepositoryTest, AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 53 - "ProfileCarouselViewModel.kt"
Cohesion: 0.21
Nodes (7): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "CalendarSettingsViewModel"
Cohesion: 0.20
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 57 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 60 - "SetDefaultLauncherSheet"
Cohesion: 0.31
Nodes (12): Modifier, MockProfile, MockProfileCard(), OnboardingProfilesPage(), OnboardingProfilesPagePreview(), ProfileSwitchDemo(), OnboardingUiState, AlreadyDefaultContent() (+4 more)

### Community 61 - "GetInstalledAppsUseCase"
Cohesion: 0.10
Nodes (13): GetInstalledAppsUseCase, Flow, Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState (+5 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.13
Nodes (7): WidgetProviderOption, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 63 - "AccentSwatch"
Cohesion: 0.14
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.17
Nodes (4): DockAppDao, Flow, DockAppEntity, DockAppDaoTest

### Community 66 - "CalendarRepository"
Cohesion: 0.16
Nodes (6): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".setContent"
Cohesion: 0.24
Nodes (5): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 75 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 76 - ".setContent"
Cohesion: 0.15
Nodes (10): NotificationSettingsScreenTest, displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), StateFlow, ViewModel (+2 more)

### Community 77 - "DrawerViewModel"
Cohesion: 0.29
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "HubWidgetPickerScreen.kt"
Cohesion: 0.29
Nodes (12): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile(), AddFailureReason (+4 more)

### Community 80 - "ManageProfilesScreen.kt"
Cohesion: 0.45
Nodes (10): AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList() (+2 more)

### Community 81 - "AppInfo"
Cohesion: 0.12
Nodes (7): FavoriteAppRepository, Flow, FavoriteAppEntity, AppInfo, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 82 - ".createViewModel"
Cohesion: 0.26
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 83 - ".useCase"
Cohesion: 0.06
Nodes (8): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest, SelectPreviewAppsUseCaseTest

### Community 84 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "CardDivider"
Cohesion: 0.27
Nodes (16): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+8 more)

### Community 88 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 90 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+4 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.24
Nodes (11): HomeWallpaper, Image, Tones, Unavailable, HomeSurfacePreview(), Color, FontWeight, Modifier (+3 more)

### Community 93 - "UsageAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.17
Nodes (13): Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier, NestedScrollConnection (+5 more)

### Community 98 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 100 - "DockAppPickerScreen.kt"
Cohesion: 0.61
Nodes (7): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader()

### Community 105 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 106 - "OnboardingScreen"
Cohesion: 0.22
Nodes (12): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT (+4 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 112 - "dashedBorder"
Cohesion: 0.70
Nodes (4): dashedBorder(), Color, Dp, Modifier

### Community 114 - "InheritOverrideCard"
Cohesion: 0.90
Nodes (4): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow()

### Community 117 - "BackupRestoreContent"
Cohesion: 0.49
Nodes (9): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+1 more)

### Community 118 - "HubAtCapacityStrip.kt"
Cohesion: 0.83
Nodes (3): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "HomeAppsListSettingsViewModelTest.kt"
Cohesion: 0.21
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 133 - "LauncherSettings"
Cohesion: 0.14
Nodes (7): LauncherSettings, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 134 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.18
Nodes (15): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), detectHomeSwipeGestures(), DefaultFavoritesReorderList(), HomeAppsListClickableRow() (+7 more)

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 139 - ".setContent"
Cohesion: 0.14
Nodes (15): ProfileSettingsScreenTest, Modifier, RenameDialog(), Composable, Modifier, NavigationChevron(), ProfileSettingsHeader(), ProfileSettingsRow() (+7 more)

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

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 156 - ".setContent"
Cohesion: 0.26
Nodes (3): BackupRestoreScreenTest, ExportBackupUseCase, ImportBackupUseCase

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 163 - "LumenDatabase"
Cohesion: 0.08
Nodes (10): DatabaseModule, Context, FavoriteAppDao, Flow, LumenDatabase, Migrations, Flow, WidgetPlacementDao (+2 more)

### Community 169 - "DefaultLauncherRepository"
Cohesion: 0.24
Nodes (5): DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

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
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 187 - "FontWeightOption"
Cohesion: 0.04
Nodes (40): Converters, T, resolveOverride(), AppListLimits, FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM (+32 more)

## Knowledge Gaps
- **348 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 628 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `FavoritesPickerViewModel`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `AppRepository`, `ContactConnection`, `BackButton`, `WidgetPlacementEntity`, `SettingsRepository`, `ClockBlock`, `.setContent`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `ProfileCarouselScreen.kt`, `Row`, `LauncherFontOption`, `.setContent`, `.setContent`, `homeAppLabelShadow`, `.setContent`, `.setContent`, `AppShortcutRepository`, `DefaultLauncherRepository`, `Type.kt`, `HomeScreen.kt`, `.setContent`, `LauncherAppWidgetHost`, `HomeDrawerRouteTest.kt`, `ClockStyleGalleryScreen.kt`, `.setContent`, `ProfileCarouselViewModel.kt`, `WidgetResizeHandle`, `GestureHintOverlay`, `FontWeightOption`, `SetDefaultLauncherSheet`, `GetInstalledAppsUseCase`, `AccentSwatch`, `SettingsScreen.kt`, `CalendarRepository`, `.setContent`, `ColorTest`, `.setContent`, `HubWidgetPickerScreen.kt`, `ManageProfilesScreen.kt`, `CardDivider`, `.setContent`, `CalendarSettingsScreen.kt`, `AppearanceSettingsScreen.kt`, `LabeledDropdownRow`, `UsageAccessExplanationScreen.kt`, `.setContent`, `AppContextMenuTest`, `StickyHeaderLayout`, `DockAppPickerScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `BackupRestoreContent`, `HubAtCapacityStrip.kt`, `ClockAdjustSheet.kt`?**
  _High betweenness centrality (0.134) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ProfileDockAppRepository`, `HomeAppsListSettingsViewModelTest.kt`, `LauncherSettings`, `FavoritesPickerViewModel`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `ImportBackupUseCase.kt`, `AppRepository`, `ContactConnection`, `Fixture`, `ClockBlock`, `ProfileEntity`, `HomeScreen`, `AppDrawerScreen.kt`, `LauncherFontOption`, `ProfileCarouselScreen.kt`, `DefaultFavoriteAppRepository`, `.setContent`, `.setContent`, `AppShortcutRepository`, `DefaultLauncherRepository`, `DockAppPickerViewModel`, `HomeScreen.kt`, `OnboardingViewModelTest`, `.setContent`, `HomeDrawerRouteTest.kt`, `LumenLauncherTheme`, `.setContent`, `ProfileCarouselViewModel.kt`, `NotificationBadgeRepository`, `FontWeightOption`, `GetInstalledAppsUseCase`, `SettingsScreen.kt`, `.setContent`, `LumenNavHost`, `FakeDockAppDao`, `DockAppRepository`, `DrawerViewModel`, `.createViewModel`, `.useCase`, `CardDivider`, `AppearanceSettingsScreen.kt`, `HomeWallpaper`, `HomeDrawerRoute`, `AppContextMenuTest`, `DockAppPickerScreen.kt`, `Fixture`, `SettingsViewModelTest`, `OnboardingViewModel`?**
  _High betweenness centrality (0.128) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `HomeViewModel`, `AppDrawerSettingsViewModelTest`, `LauncherSettings`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `ImportBackupUseCase.kt`, `ManageProfilesViewModel`, `AppRepository`, `BackButton`, `ClockFontOption`, `.setContent`, `ProfileEntity`, `.setContent`, `LauncherFontOption`, `DefaultFavoriteAppRepository`, `.setContent`, `ClockStyleGalleryViewModelTest`, `.setContent`, `.setContent`, `DefaultLauncherRepository`, `.setContent`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCaseTest.kt`, `.setContent`, `ProfileCarouselViewModel.kt`, `CalendarSettingsViewModel`, `SettingsRepositoryTest.kt`, `FontWeightOption`, `GetInstalledAppsUseCase`, `CalendarRepository`, `.setContent`, `.createViewModel`, `.setContent`, `DrawerViewModel`, `AppInfo`, `.createViewModel`, `.useCase`, `.setContent`, `.setContent`, `SettingsRepositoryTest`, `OnboardingViewModel`, `.setContent`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 117 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 117 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._