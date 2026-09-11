# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 334 files · ~698,355 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3183 nodes · 8677 edges · 154 communities (109 shown, 39 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 653 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b8b1af1f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileDockAppRepository
- design_handoff_minimal_launcher/support.js
- HubGrid
- ObserveHomeScreenStateUseCase.kt
- AppDrawerSettingsViewModelTest
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ProfileCarouselViewModel.kt
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- DrawerPresentation
- WidgetPlacementEntity
- AppRepository
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- ResolveWidgetDropUseCase
- .setContent
- BackupMapping.kt
- LumenLauncherTheme
- ClockFontOption
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
- DefaultFavoriteAppRepository
- .setContent
- ClockStyleGalleryViewModelTest
- ClockTemplateId
- homeAppLabelShadow
- .setContent
- .setContent
- AppShortcut
- LauncherFontOption
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- HomeDrawerRouteTest.kt
- BackupRestoreScreenTest.kt
- SettingsRepository
- Clock Widget Resize — Implementation Spec
- ImportBackupUseCaseTest.kt
- AppDrawerScreen
- FontWeightSlider
- .createViewModel
- ObserveProfilePreviewsUseCase.kt
- .refresh
- 4. Feature Requirements
- DockDisplayMode
- LauncherViewModel
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- AppWidgetRepository.kt
- AppInfo
- HubWidgetPickerViewModel
- AccentSwatch
- SettingsScreen.kt
- .setContent
- CalendarPermissionRepository
- github.md
- WallpaperRepository
- ColorTest
- LumenNavHost
- DockAppRepository
- letterAt
- NotificationBadgeRepository
- ImportBackupUseCase.kt
- ProfileDao
- .setContent
- LauncherActivity.kt
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- WidgetProviderOption
- LabeledDropdownRow
- FavoriteAppRepository
- .createViewModel
- .useCase
- Fixture
- Lumen Launcher Implementation Plan
- CardDivider
- AppModule.kt
- DefaultAppRepositoryTest
- AppearanceSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- UsageAccessExplanationScreen.kt
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- AppContextMenuTest
- StickyHeaderLayout
- HubAddWidgetEvent
- PlaceWidgetResult
- Fixture
- ContactRepository
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
- DeleteWidgetUseCaseTest.kt
- LumenDatabaseMigrationTest
- .setContent
- TypeTest.kt
- ClockAdjustSheet.kt
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .createViewModel
- LauncherSettings
- HomeAppsListSettingsScreen.kt
- Lumen Launcher — Onboarding Flow
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
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- LumenDatabase
- DefaultLauncherRepository
- Type.kt
- ProfileEntityTest
- ClockCornerHandle
- WidgetResizeHandle
- ClockColorOption

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 224 edges
2. `AppInfo` - 184 edges
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

## Communities (154 total, 39 thin omitted)

### Community 0 - "ProfileDockAppRepository"
Cohesion: 0.09
Nodes (8): Flow, ProfileDockAppDao, ProfileDockAppEntity, ProfileDockAppRepository, ProfileDockAppDaoTest, FakeProfileDockAppDao, Flow, ProfileDockAppRepositoryTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "HubGrid"
Cohesion: 0.15
Nodes (16): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+8 more)

### Community 3 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.09
Nodes (9): NotificationShadeRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest (+1 more)

### Community 5 - ".setContent"
Cohesion: 0.05
Nodes (39): OnboardingScreenTest, FavoritesPickerScreenTest, AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow() (+31 more)

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

### Community 12 - "DrawerPresentation"
Cohesion: 0.09
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "AppRepository"
Cohesion: 0.15
Nodes (8): AppRepository, flattenIcon(), Bitmap, AppRepositoryTest, CleanUpUninstalledAppsUseCaseTest, Drawable, LauncherActivityInfo, LauncherApps

### Community 15 - "ContactConnection"
Cohesion: 0.15
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "ResolveWidgetDropUseCase"
Cohesion: 0.11
Nodes (9): DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, ObserveHubStateUseCaseTest (+1 more)

### Community 20 - "BackupMapping.kt"
Cohesion: 0.20
Nodes (13): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+5 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

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
Cohesion: 0.20
Nodes (21): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), DrawerAppRow() (+13 more)

### Community 28 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+6 more)

### Community 30 - "Row"
Cohesion: 0.14
Nodes (79): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+71 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.13
Nodes (7): any(), eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, MatrixCursor

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "DefaultFavoriteAppRepository"
Cohesion: 0.09
Nodes (8): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "homeAppLabelShadow"
Cohesion: 0.07
Nodes (32): dashedBorder(), Color, Dp, Modifier, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector (+24 more)

### Community 39 - ".setContent"
Cohesion: 0.13
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 40 - "AppShortcut"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 41 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.20
Nodes (21): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel() (+13 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 45 - "HomeDrawerRouteTest.kt"
Cohesion: 0.11
Nodes (17): KeyboardDismissalTest, DefaultAppRepository, Intent, AddAppToDockUseCase, AddAppToFavoritesUseCase, CleanUpUninstalledAppsUseCase, CompactWidgetsUseCase, Flow (+9 more)

### Community 46 - "BackupRestoreScreenTest.kt"
Cohesion: 0.21
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 47 - "SettingsRepository"
Cohesion: 0.05
Nodes (12): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, SettingsRepository, UsageAccessRepository, StateFlow (+4 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.17
Nodes (9): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest (+1 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 51 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 53 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.50
Nodes (4): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 54 - ".refresh"
Cohesion: 0.15
Nodes (8): Callback, Callback, Flow, LumenNotificationListenerService, Callback, NotificationListenerService, StatusBarNotification, UserHandle

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "DockDisplayMode"
Cohesion: 0.16
Nodes (10): DockDisplayMode, ICONS, TEXT, HomeSurfacePreview(), Color, FontWeight, Modifier, StateFlow (+2 more)

### Community 57 - "LauncherViewModel"
Cohesion: 0.20
Nodes (7): EnsureActiveProfileUseCase, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, EnsureActiveProfileUseCaseTest

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.10
Nodes (24): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+16 more)

### Community 60 - "AppWidgetRepository.kt"
Cohesion: 0.15
Nodes (10): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+2 more)

### Community 61 - "AppInfo"
Cohesion: 0.10
Nodes (8): AppInfo, Flow, GetInstalledAppsUseCase, Flow, SelectPreviewAppsUseCase, GetInstalledAppsUseCaseTest, LauncherViewModelTest, UsageStatsManager

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 63 - "AccentSwatch"
Cohesion: 0.18
Nodes (11): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+3 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 66 - "CalendarPermissionRepository"
Cohesion: 0.10
Nodes (8): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, CalendarInfo, CalendarSettingsViewModelTest

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "WallpaperRepository"
Cohesion: 0.15
Nodes (8): DockSettingsScreenTest, Bitmap, WallpaperRepository, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, WallpaperManager

### Community 70 - "LumenNavHost"
Cohesion: 0.19
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen() (+5 more)

### Community 71 - "DockAppRepository"
Cohesion: 0.09
Nodes (8): DockAppRepository, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest, ExportBackupUseCaseTest

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 74 - "ImportBackupUseCase.kt"
Cohesion: 0.26
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 75 - "ProfileDao"
Cohesion: 0.11
Nodes (5): Flow, ProfileDao, ProfileDaoTest, FakeProfileDao, Flow

### Community 76 - ".setContent"
Cohesion: 0.21
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

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
Cohesion: 0.19
Nodes (21): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+13 more)

### Community 81 - "FavoriteAppRepository"
Cohesion: 0.15
Nodes (4): FavoriteAppRepository, Flow, FakeFavoriteAppDao, FavoriteAppRepositoryTest

### Community 83 - ".useCase"
Cohesion: 0.06
Nodes (8): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest, SelectPreviewAppsUseCaseTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "CardDivider"
Cohesion: 0.17
Nodes (21): ConfirmDialog(), Modifier, DragReorderState, Modifier, T, rememberDragReorderState(), CardDivider(), Modifier (+13 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 90 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.23
Nodes (9): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground(), WallpaperRepository (+1 more)

### Community 93 - "UsageAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.10
Nodes (23): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), ClockAdjustMode, ADJUST, MENU (+15 more)

### Community 98 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 100 - "PlaceWidgetResult"
Cohesion: 0.50
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 102 - "ContactRepository"
Cohesion: 0.28
Nodes (3): ContactRepository, LabeledValue, ContactInfo

### Community 105 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 109 - "FavoriteAppEntity"
Cohesion: 0.12
Nodes (5): FavoriteAppDao, Flow, FavoriteAppEntity, Flow, FavoriteAppDaoTest

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
Cohesion: 0.19
Nodes (4): LauncherSettings, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 134 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 139 - ".setContent"
Cohesion: 0.06
Nodes (33): ProfileSettingsScreenTest, AssignCalendarColorsUseCase, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog() (+25 more)

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
Cohesion: 0.18
Nodes (15): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+7 more)

### Community 149 - ".setContent"
Cohesion: 0.30
Nodes (6): HubScreenTest, HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 150 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

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
Nodes (8): DatabaseModule, Context, DockAppDao, Flow, LumenDatabase, Migrations, Migration, RoomDatabase

### Community 169 - "DefaultLauncherRepository"
Cohesion: 0.24
Nodes (5): DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

### Community 170 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 183 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 187 - "ClockColorOption"
Cohesion: 0.04
Nodes (42): DataStoreModule, Context, Converters, T, resolveOverride(), AppListLimits, ClockColorOption, ACCENT_PRIMARY (+34 more)

## Knowledge Gaps
- **348 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 629 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `.setContent`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `DrawerPresentation`, `DockSettingsScreen.kt`, `AppRepository`, `ContactConnection`, `BackButton`, `ResolveWidgetDropUseCase`, `.setContent`, `.setContent`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `ProfileCarouselScreen.kt`, `Row`, `WallpaperAccentRole`, `.setContent`, `.setContent`, `homeAppLabelShadow`, `.setContent`, `.setContent`, `DefaultLauncherRepository`, `LauncherFontOption`, `HomeScreen.kt`, `Type.kt`, `HomeDrawerRouteTest.kt`, `BackupRestoreScreenTest.kt`, `SettingsRepository`, `AppDrawerScreen`, `FontWeightSlider`, `WidgetResizeHandle`, `BackupRestoreViewModel`, `ClockColorOption`, `AppInfo`, `AccentSwatch`, `SettingsScreen.kt`, `.setContent`, `CalendarPermissionRepository`, `WallpaperRepository`, `ColorTest`, `LumenNavHost`, `.setContent`, `LauncherActivity.kt`, `WidgetProviderOption`, `LabeledDropdownRow`, `CardDivider`, `AppearanceSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `UsageAccessExplanationScreen.kt`, `HomeDrawerRoute`, `.setContent`, `AppContextMenuTest`, `StickyHeaderLayout`, `.setContent`, `.rendersOneEntryPerLetterProvided`, `.setContent`, `.setContent`, `ClockAdjustSheet.kt`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ProfileDockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `.createViewModel`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `ProfileCarouselViewModel.kt`, `.setContent`, `DrawerPresentation`, `DockSettingsScreen.kt`, `AppRepository`, `Fixture`, `LumenLauncherTheme`, `ProfileEntity`, `HomeScreen`, `AppDrawerScreen.kt`, `WallpaperAccentRole`, `ProfileCarouselScreen.kt`, `DefaultFavoriteAppRepository`, `.setContent`, `.setContent`, `AppShortcut`, `DefaultLauncherRepository`, `HomeScreen.kt`, `OnboardingViewModelTest`, `HomeDrawerRouteTest.kt`, `SettingsRepository`, `AppDrawerScreen`, `ObserveProfilePreviewsUseCase.kt`, `.refresh`, `DockDisplayMode`, `LauncherViewModel`, `ClockColorOption`, `SettingsScreen.kt`, `.setContent`, `WallpaperRepository`, `LumenNavHost`, `DockAppRepository`, `LauncherActivity.kt`, `FavoriteAppRepository`, `.createViewModel`, `.useCase`, `Fixture`, `CardDivider`, `AppearanceSettingsScreen.kt`, `HomeDrawerRoute`, `AppContextMenuTest`, `Fixture`, `SettingsViewModelTest`, `AddAppToDockUseCaseTest`, `OnboardingViewModel`, `FavoriteAppEntity`, `AddAppToFavoritesUseCaseTest`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Why does `ProfileEntity` connect `ProfileEntity` to `ProfileDockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `.createViewModel`, `.setContent`, `LauncherSettings`, `ProfileCarouselViewModel.kt`, `ManageProfilesViewModel`, `.setContent`, `AppRepository`, `Fixture`, `BackupMapping.kt`, `ClockFontOption`, `ProfileCarouselScreen.kt`, `DefaultFavoriteAppRepository`, `ClockStyleGalleryViewModelTest`, `ProfileEntityTest`, `SettingsRepository`, `ImportBackupUseCaseTest.kt`, `ObserveProfilePreviewsUseCase.kt`, `DockDisplayMode`, `ClockColorOption`, `CalendarPermissionRepository`, `WallpaperRepository`, `DockAppRepository`, `ProfileDao`, `LabeledDropdownRow`, `.createViewModel`, `Fixture`, `Fixture`, `AddAppToDockUseCaseTest`, `FavoriteAppEntity`, `AddAppToFavoritesUseCaseTest`, `.setContent`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `ProfileRepository` (e.g. with `.`addProfile names it Profile N and appends after the last position`()` and `.`deleteProfile removes it`()`) actually correct?**
  _`ProfileRepository` has 19 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._