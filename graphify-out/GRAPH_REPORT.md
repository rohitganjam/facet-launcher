# Graph Report - lumen-launcher  (2026-09-15)

## Corpus Check
- 409 files · ~837,098 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3947 nodes · 10991 edges · 195 communities (137 shown, 52 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 894 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5ac0c11a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- FacetDockAppRepository
- SettingsRepository
- .setContent
- Android launcher design planning/support.js
- .setContent
- Screens
- ManageFacetsViewModel
- ConvertersTest
- DockAppEntity
- ContactRepositoryTest
- ClockColorOption
- AppWidgetRepository
- Screens
- FavoritesPickerScreen.kt
- Fixture
- LauncherSettings
- FacetCarouselUiState
- .createViewModel
- LauncherFontOption
- 4. Feature Requirements
- FavoriteFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- .setContent
- HubGrid
- Row
- ClockTemplateId
- HomeViewModel
- AppShortcut
- AccentSwatch
- FacetNavHost
- WidgetPlacementRepository
- LauncherViewModel
- AppRepositoryTest.kt
- DockAppPickerViewModel
- SetDefaultLauncherSheetTest
- ContactConnection
- Manrope Font License (SIL OFL 1.1)
- .setContent
- FavoriteAppRepository
- FolderDao
- .setContent
- FacetCarouselScreen.kt
- Clock Widget Resize — Implementation Spec
- SettingsSearchEntry
- CalendarInfo
- BackupBundle
- DockSettingsViewModel.kt
- OnboardingHomeSetupPage.kt
- LauncherAppWidgetHost
- 4. Feature Requirements
- AppDrawerScreen.kt
- AppContextMenu
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- NextAlarmRepositoryTest
- BackupRestoreViewModelTest
- CalendarEventsBlock.kt
- .drawerViewModel
- ResolveWidgetDropUseCaseTest
- AppIcon
- github.md
- HomeDrawerRoute
- ClockAccessoryIcons.kt
- WorkProfileRepository
- .setContent
- HomeAppsListSettingsScreen.kt
- SelectPreviewAppsUseCaseTest
- NotificationBadgeRepository
- WallpaperAccentRole
- CalendarRepositoryTest
- CompactWidgetsUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- DockAppPickerScreen.kt
- ResolveWidgetResizeUseCaseTest
- BackupMapping.kt
- FolderDetailScreen.kt
- BackupRestoreMessage
- GroupAppsByLetterUseCaseTest
- Lumen Launcher Implementation Plan
- FacetSettingsContent
- Play Console — sensitive permission disclosures
- DefaultLauncherRepositoryTest
- FolderContentsSheet
- .setContent
- Facet Launcher — Built Capabilities
- FolderDetailViewModel
- Play Console — store listing text
- OnboardingIntroPage.kt
- ObserveQuickAddStateUseCaseTest
- HomeScreen.kt
- .createViewModel
- ContactRepository
- AppDrawerScreen
- FacetLauncherTheme
- dashedBorder
- PlaceWidgetUseCase
- FolderEntity
- .repository
- AppInfo
- SettingsViewModelTest
- ImportBackupResult
- NextAlarmRepository
- Fixture
- FacetEntity
- FolderAppPickerScreen.kt
- HubWidgetPickerViewModel
- .setContent
- Folder
- FacetDatabaseMigrationTest
- HomeWallpaper
- CalendarSettingsScreen.kt
- PlaceWidgetUseCaseTest
- Color.kt
- BackButton
- FakeFolderDao
- OnboardingFacetsPage.kt
- FacetDaoTest
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DrawerGridSize
- NotificationShadeRepository
- WidgetPlacementEntity
- AppModule
- Facet Launcher — Onboarding Flow
- FolderAppPickerViewModelTest
- GestureHintOverlay
- HubViewModel
- .setContent
- BackupRestoreViewModel
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- FacetDockFolderPlacementEntity
- SettingsRepositoryTest
- LauncherActivity.kt
- FakeFavoriteAppDao
- WallpaperRepositoryTest
- SetDefaultLauncherSheet
- OnboardingViewModelTest
- build_screenshot.py
- OnboardingViewModel
- 2. Design tokens
- HubAddWidgetEvent
- DefaultAppRepositoryTest
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- DockSettingsScreen.kt
- SettingsViewModel
- FoldersSettingsContent
- BackupRestoreContent
- AppProfile
- FontWeightSlider
- .useCase
- Axis
- TypeTest.kt
- .setContent
- ClockAccessoryState
- FacetCarouselViewModel
- Type.kt
- DefaultLauncherRepository
- FacetEntityTest
- FacetDatabase
- ClockAdjustMode
- ClockAlignment.kt
- T
- AppearanceSettingsScreen.kt
- HubWidgetPickerScreen.kt
- AppWidgetRepository.kt
- .setContent
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- CardDivider
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- ClockCornerHandle
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 253 edges
2. `AppInfo` - 223 edges
3. `FacetEntity` - 212 edges
4. `SettingsRepository` - 159 edges
5. `Row` - 146 edges
6. `FacetRepository` - 133 edges
7. `LauncherSettings` - 123 edges
8. `AppProfile` - 102 edges
9. `Folder` - 96 edges
10. `FolderRepository` - 75 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `HomeDrawerRouteTest` --calls--> `AppInfo`  [INFERRED]
  app/src/androidTest/kotlin/com/facetlauncher/app/ui/launcher/HomeDrawerRouteTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/model/AppInfo.kt
- `ManageFacetsViewModel` --calls--> `combine()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/facets/ManageFacetsViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/domain/FlowCombine.kt
- `DefaultAppRepositoryTest` --calls--> `DefaultAppRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/DefaultAppRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/DefaultAppRepository.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (195 total, 52 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.06
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.17
Nodes (10): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+2 more)

### Community 4 - "FacetDockAppRepository"
Cohesion: 0.09
Nodes (10): BackupRestoreScreenTest, BackupRepository, FacetDockAppRepository, com, Flow, FacetDockAppEntity, ExportBackupUseCase, Flow (+2 more)

### Community 5 - "SettingsRepository"
Cohesion: 0.03
Nodes (30): any(), KeyboardDismissalTest, FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, CalendarPermissionRepository, CalendarRepository, ContactPermissionRepository, NotificationAccessRepository (+22 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "ManageFacetsViewModel"
Cohesion: 0.09
Nodes (8): ManageFacetsScreenTest, StateFlow, ViewModel, ManageFacetsUiState, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 12 - "DockAppEntity"
Cohesion: 0.09
Nodes (9): DockAppDao, Flow, DockAppEntity, DockFolderPlacementEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, FakeDockFolderPlacementDao (+1 more)

### Community 13 - "ContactRepositoryTest"
Cohesion: 0.23
Nodes (3): eq(), ContactRepositoryTest, MatrixCursor

### Community 14 - "ClockColorOption"
Cohesion: 0.03
Nodes (62): DataStoreModule, Context, Converters, T, resolveOverride(), ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY (+54 more)

### Community 15 - "AppWidgetRepository"
Cohesion: 0.16
Nodes (7): AppWidgetRepository, AppWidgetRepositoryTest, AppWidgetProviderInfo, UserHandle, UserManager, ApplicationInfo, ComponentName

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "FavoritesPickerScreen.kt"
Cohesion: 0.31
Nodes (15): FavoritesAppsList(), FavoritesFolderPickerRow(), FavoritesFoldersList(), FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview() (+7 more)

### Community 19 - "LauncherSettings"
Cohesion: 0.11
Nodes (6): LauncherSettings, HomeUiState, AddAppToDockUseCaseTest, Fixture, FacetCarouselUiStateTest, HomeUiStateTest

### Community 21 - ".createViewModel"
Cohesion: 0.15
Nodes (4): HomeAppsListSettingsViewModel, StateFlow, ViewModel, HomeAppsListSettingsViewModelTest

### Community 22 - "LauncherFontOption"
Cohesion: 0.14
Nodes (8): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, facetColorScheme(), ClockFontsTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FavoriteFolderPlacementEntity"
Cohesion: 0.11
Nodes (5): FavoriteFolderPlacementDao, Flow, FavoriteFolderPlacementEntity, FakeFavoriteFolderPlacementDao, FavoriteFolderPlacementDaoTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - ".setContent"
Cohesion: 0.17
Nodes (5): FavoritesPickerScreenTest, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 29 - "HubGrid"
Cohesion: 0.13
Nodes (20): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+12 more)

### Community 30 - "Row"
Cohesion: 0.18
Nodes (70): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+62 more)

### Community 31 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 32 - "HomeViewModel"
Cohesion: 0.10
Nodes (6): ObserveQuickAddStateUseCase, HomeViewModel, Intent, StateFlow, ViewModel, HomeViewModelTest

### Community 33 - "AppShortcut"
Cohesion: 0.20
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 34 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 35 - "FacetNavHost"
Cohesion: 0.17
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), Modifier (+5 more)

### Community 36 - "WidgetPlacementRepository"
Cohesion: 0.12
Nodes (7): Flow, WidgetPlacementDao, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 37 - "LauncherViewModel"
Cohesion: 0.22
Nodes (6): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 38 - "AppRepositoryTest.kt"
Cohesion: 0.20
Nodes (8): any(), AppRepositoryTest, Context, LauncherApps, T, UserHandle, UserManager, LauncherActivityInfo

### Community 39 - "DockAppPickerViewModel"
Cohesion: 0.13
Nodes (15): DockAppPickerViewModel, Flow, StateFlow, ViewModel, Modifier, nextStep(), OnboardingScreen(), OnboardingStep (+7 more)

### Community 41 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 43 - ".setContent"
Cohesion: 0.19
Nodes (4): FacetSettingsScreenTest, FacetSettingsViewModel, StateFlow, ViewModel

### Community 44 - "FavoriteAppRepository"
Cohesion: 0.10
Nodes (14): FavoriteAppRepository, com, Flow, FavoriteAppEntity, combine(), Flow, FavoriteAppDaoTest, T1 (+6 more)

### Community 45 - "FolderDao"
Cohesion: 0.08
Nodes (7): DefaultFavoriteFolderPlacementEntity, FolderDao, FolderWithApps, Flow, FakeDefaultFavoriteFolderPlacementDao, Flow, DefaultFavoriteFolderPlacementDaoTest

### Community 46 - ".setContent"
Cohesion: 0.13
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 47 - "FacetCarouselScreen.kt"
Cohesion: 0.12
Nodes (25): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), AddFacetPage(), FacetCarouselContent() (+17 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "CalendarInfo"
Cohesion: 0.07
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel (+2 more)

### Community 51 - "BackupBundle"
Cohesion: 0.15
Nodes (7): Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest, ImportBackupUseCaseTest

### Community 52 - "DockSettingsViewModel.kt"
Cohesion: 0.22
Nodes (5): DockSettingsViewModel, Flow, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 53 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.17
Nodes (19): ConfirmDialog(), Modifier, DragReorderState, Modifier, T, rememberDragReorderState(), detectHomeSwipeGestures(), AppDrawerSection() (+11 more)

### Community 54 - "LauncherAppWidgetHost"
Cohesion: 0.23
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.12
Nodes (32): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT (+24 more)

### Community 57 - "AppContextMenu"
Cohesion: 0.12
Nodes (16): Add, QuickPlacementAction, Remove, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow(), Composable (+8 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.18
Nodes (21): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+13 more)

### Community 59 - "HomeScreen"
Cohesion: 0.13
Nodes (4): HomeScreenTest, HomeScreen(), HomeScreenTextOnlyPresentationPreview(), stableKey()

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.22
Nodes (14): FacetScopeBadge(), Color, Modifier, ScopeBadge(), WorkScopeBadge(), AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview() (+6 more)

### Community 63 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 66 - "AppIcon"
Cohesion: 0.49
Nodes (9): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), WorkProfileBadge() (+1 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "HomeDrawerRoute"
Cohesion: 0.25
Nodes (8): HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier, NestedScrollConnection, NestedScrollSource, Offset, SwipeAxisState

### Community 69 - "ClockAccessoryIcons.kt"
Cohesion: 0.29
Nodes (11): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+3 more)

### Community 70 - "WorkProfileRepository"
Cohesion: 0.32
Nodes (6): BroadcastReceiver, Context, Flow, Intent, WorkProfileRepository, BroadcastReceiver

### Community 71 - ".setContent"
Cohesion: 0.09
Nodes (5): AppContextMenuTest, SharedFlow, FolderTileContextMenuTest, SharedFlow, QuickAddState

### Community 72 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+2 more)

### Community 74 - "NotificationBadgeRepository"
Cohesion: 0.07
Nodes (20): BroadcastReceiver, Callback, BroadcastReceiver, Callback, flattenIcon(), Bitmap, BroadcastReceiver, Context (+12 more)

### Community 75 - "WallpaperAccentRole"
Cohesion: 0.10
Nodes (11): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY (+3 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "DockAppPickerScreen.kt"
Cohesion: 0.31
Nodes (15): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockAppsList(), DockFolderPickerRow(), DockFoldersList(), DockPickerRow() (+7 more)

### Community 81 - "BackupMapping.kt"
Cohesion: 0.19
Nodes (19): BackupAppEntry, BackupFacet, BackupFolder, BackupFolderPlacement, BackupWidgetPlacement, com, T, toBackupEntry() (+11 more)

### Community 82 - "FolderDetailScreen.kt"
Cohesion: 0.38
Nodes (9): Modifier, RenameDialog(), FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview(), Modifier (+1 more)

### Community 83 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "FacetSettingsContent"
Cohesion: 0.29
Nodes (11): FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron(), SectionHeader(), appsSubtitle(), FacetSettingsContent() (+3 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 89 - "FolderContentsSheet"
Cohesion: 0.40
Nodes (10): AddHere, FolderContentsAppRow(), FolderContentsEmptyState(), FolderContentsGrid(), FolderContentsGridTile(), FolderContentsList(), FolderContentsSheet(), FolderSheetHeaderAction (+2 more)

### Community 90 - ".setContent"
Cohesion: 0.24
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "FolderDetailViewModel"
Cohesion: 0.22
Nodes (4): FolderDetailViewModel, StateFlow, ViewModel, FolderDetailViewModelTest

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "OnboardingIntroPage.kt"
Cohesion: 0.35
Nodes (10): Modifier, OnboardingDots(), AppRowBar(), ConceptLine(), HomeDiagram(), Dp, Modifier, LeaderRow() (+2 more)

### Community 96 - "HomeScreen.kt"
Cohesion: 0.24
Nodes (21): DrawerPresentation, GRID, LIST, HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow() (+13 more)

### Community 97 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 99 - "AppDrawerScreen"
Cohesion: 0.11
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.12
Nodes (8): AlphabetRailTest, ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "dashedBorder"
Cohesion: 0.24
Nodes (10): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier (+2 more)

### Community 102 - "PlaceWidgetUseCase"
Cohesion: 0.22
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 103 - "FolderEntity"
Cohesion: 0.22
Nodes (4): FolderAppEntity, FolderEntity, DockFolderPlacementDaoTest, FolderDaoTest

### Community 105 - "AppInfo"
Cohesion: 0.04
Nodes (31): DockSettingsScreenTest, HomeAppsListSettingsScreenTest, AppRepository, DefaultAppRepository, Intent, DefaultFavoriteAppRepository, Flow, DockAppRepository (+23 more)

### Community 107 - "ImportBackupResult"
Cohesion: 0.28
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 108 - "NextAlarmRepository"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver

### Community 110 - "FacetEntity"
Cohesion: 0.03
Nodes (24): FacetRepository, Flow, toCsv(), FacetDao, Flow, FacetEntity, EnsureActiveFacetUseCase, FacetRepositoryTest (+16 more)

### Community 111 - "FolderAppPickerScreen.kt"
Cohesion: 0.50
Nodes (8): AppIconSize, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 114 - "Folder"
Cohesion: 0.14
Nodes (3): FolderDetailScreenTest, FoldersSettingsScreenTest, Folder

### Community 116 - "HomeWallpaper"
Cohesion: 0.12
Nodes (13): HomeWallpaper, Image, Tones, Unavailable, Bitmap, Alignment, Modifier, WallpaperBackground() (+5 more)

### Community 117 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 119 - "Color.kt"
Cohesion: 0.39
Nodes (7): Color, resolve(), accentTonalExtremes(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme

### Community 120 - "BackButton"
Cohesion: 0.15
Nodes (16): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+8 more)

### Community 122 - "OnboardingFacetsPage.kt"
Cohesion: 0.54
Nodes (7): FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview(), OnboardingUiState

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "DrawerGridSize"
Cohesion: 0.06
Nodes (13): AppDrawerSettingsScreenTest, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, SearchBarPosition, BOTTOM (+5 more)

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 134 - "AppModule"
Cohesion: 0.24
Nodes (5): AppModule, Context, LauncherApps, UserManager, WallpaperManager

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "FolderAppPickerViewModelTest"
Cohesion: 0.16
Nodes (6): FolderAppPickerScreenTest, FolderAppPickerUiState, FolderAppPickerViewModel, StateFlow, ViewModel, FolderAppPickerViewModelTest

### Community 137 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 138 - "HubViewModel"
Cohesion: 0.12
Nodes (11): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier, HubUiState, HubWidgetUi, HubViewModel (+3 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.11
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "FacetDockFolderPlacementEntity"
Cohesion: 0.11
Nodes (5): FacetDockFolderPlacementDao, Flow, FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FacetDockFolderPlacementDaoTest

### Community 146 - "LauncherActivity.kt"
Cohesion: 0.39
Nodes (5): Intent, LauncherApps, LauncherActivity, Bundle, ComponentActivity

### Community 147 - "FakeFavoriteAppDao"
Cohesion: 0.13
Nodes (5): FavoriteAppDao, Flow, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 149 - "SetDefaultLauncherSheet"
Cohesion: 0.73
Nodes (5): AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 151 - "build_screenshot.py"
Cohesion: 0.47
Nodes (5): ImageDraw, build(), draw_tracked_text(), Refresh one Play Store marketing screenshot after a raw device capture changes.…, rounded_mask()

### Community 152 - "OnboardingViewModel"
Cohesion: 0.20
Nodes (3): StateFlow, ViewModel, OnboardingViewModel

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 156 - ".setContent"
Cohesion: 0.13
Nodes (10): PermissionsScreenTest, PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState, StateFlow (+2 more)

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "DockSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier (+2 more)

### Community 160 - "SettingsViewModel"
Cohesion: 0.48
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 161 - "FoldersSettingsContent"
Cohesion: 0.29
Nodes (9): FolderRow(), FoldersSettingsContent(), FoldersSettingsScreen(), FoldersSettingsScreenPreview(), Modifier, FoldersSettingsUiState, FoldersSettingsViewModel, StateFlow (+1 more)

### Community 162 - "BackupRestoreContent"
Cohesion: 0.49
Nodes (9): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+1 more)

### Community 163 - "AppProfile"
Cohesion: 0.05
Nodes (7): FacetDockAppDao, Flow, AppProfile, PERSONAL, WORK, UserHandle, FakeFacetDockAppDao

### Community 164 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 166 - "Axis"
Cohesion: 0.67
Nodes (3): Axis, HORIZONTAL, VERTICAL

### Community 169 - "ClockAccessoryState"
Cohesion: 0.47
Nodes (3): ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase

### Community 170 - "FacetCarouselViewModel"
Cohesion: 0.33
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 171 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 174 - "FacetDatabase"
Cohesion: 0.09
Nodes (10): DatabaseModule, Context, DefaultFavoriteFolderPlacementDao, Flow, DockFolderPlacementDao, Flow, FacetDatabase, Migrations (+2 more)

### Community 175 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 180 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 183 - "HubWidgetPickerScreen.kt"
Cohesion: 0.23
Nodes (15): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+7 more)

### Community 185 - "AppWidgetRepository.kt"
Cohesion: 0.14
Nodes (10): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+2 more)

### Community 186 - ".setContent"
Cohesion: 0.15
Nodes (6): FakeNotificationAccessRepository, NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.28
Nodes (15): appDrawerSummary(), appsListSummary(), ClickableRow(), dockSummary(), folderCountSummary(), Composable, Modifier, NavigationChevron() (+7 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 211 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+8 more)

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

## Knowledge Gaps
- **378 isolated node(s):** `HubFull`, `InvalidFile`, `Tones`, `Unavailable`, `AddFailed` (+373 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 725 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AppInfo` connect `AppInfo` to `.setContent`, `FacetDockAppRepository`, `SettingsRepository`, `.setContent`, `FolderAppPickerViewModelTest`, `DockAppEntity`, `DefaultFavoriteAppEntity`, `ClockColorOption`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `FakeFavoriteAppDao`, `LauncherSettings`, `.createViewModel`, `Fixture`, `OnboardingViewModelTest`, `OnboardingViewModel`, `.setContent`, `DockSettingsScreen.kt`, `HomeViewModel`, `AppShortcut`, `FoldersSettingsContent`, `FacetNavHost`, `LauncherViewModel`, `AppRepositoryTest.kt`, `DockAppPickerViewModel`, `.useCase`, `FavoriteAppRepository`, `.setContent`, `FacetCarouselScreen.kt`, `AppearanceSettingsScreen.kt`, `OnboardingHomeSetupPage.kt`, `DockSettingsViewModel.kt`, `AppDrawerScreen.kt`, `AppContextMenu`, `HomeScreen`, `SettingsScreen.kt`, `HomeDrawerRoute`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `NotificationBadgeRepository`, `WallpaperAccentRole`, `DockAppPickerScreen.kt`, `FolderDetailScreen.kt`, `GroupAppsByLetterUseCaseTest`, `FolderContentsSheet`, `FolderDetailViewModel`, `ObserveQuickAddStateUseCaseTest`, `HomeScreen.kt`, `AppDrawerScreen`, `.repository`, `SettingsViewModelTest`, `Fixture`, `FacetEntity`, `FolderAppPickerScreen.kt`, `Folder`, `FakeFolderDao`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockStyleGalleryViewModel`, `.setContent`, `DrawerGridSize`, `SettingsRepository`, `WidgetPlacementEntity`, `.setContent`, `.setContent`, `FacetDockAppRepository`, `ManageFacetsViewModel`, `FolderAppPickerViewModelTest`, `.setContent`, `GestureHintOverlay`, `ClockColorOption`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `SetDefaultLauncherSheet`, `LauncherFontOption`, `.setContent`, `.setContent`, `.setContent`, `HubGrid`, `DockSettingsScreen.kt`, `FoldersSettingsContent`, `BackupRestoreContent`, `FacetNavHost`, `FontWeightSlider`, `AccentSwatch`, `.setContent`, `ContactConnection`, `SetDefaultLauncherSheetTest`, `.setContent`, `Type.kt`, `.setContent`, `FacetCarouselScreen.kt`, `CalendarInfo`, `AppearanceSettingsScreen.kt`, `OnboardingHomeSetupPage.kt`, `HubWidgetPickerScreen.kt`, `AppDrawerScreen.kt`, `.setContent`, `HomeScreen`, `LabeledDropdownRow`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `ColorTest`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `WallpaperAccentRole`, `DockAppPickerScreen.kt`, `FolderDetailScreen.kt`, `CardDivider`, `FacetSettingsContent`, `.setContent`, `OnboardingIntroPage.kt`, `StickyHeaderLayout`, `.setContent`, `HomeScreen.kt`, `AppDrawerScreen`, `dashedBorder`, `PlaceWidgetUseCase`, `AppInfo`, `FolderAppPickerScreen.kt`, `.setContent`, `Folder`, `CalendarSettingsScreen.kt`, `BackButton`, `OnboardingFacetsPage.kt`?**
  _High betweenness centrality (0.152) - this node is a cross-community bridge._
- **Why does `FacetEntity` connect `FacetEntity` to `ClockStyleGalleryViewModel`, `FacetDockAppRepository`, `SettingsRepository`, `ManageFacetsViewModel`, `ClockColorOption`, `FacetDockFolderPlacementEntity`, `Fixture`, `LauncherSettings`, `.createViewModel`, `FavoriteFolderPlacementEntity`, `.setContent`, `HomeViewModel`, `FacetCarouselViewModel`, `FavoriteAppRepository`, `FacetEntityTest`, `FacetCarouselScreen.kt`, `CalendarInfo`, `BackupBundle`, `DockSettingsViewModel.kt`, `LabeledDropdownRow`, `BackupMapping.kt`, `AppInfo`, `Fixture`, `.setContent`, `FacetDaoTest`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Are the 24 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 141 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 141 INFERRED edges - model-reasoned connections that need verification._
- **What connects `HubFull`, `InvalidFile`, `Tones` to the rest of the system?**
  _378 weakly-connected nodes found - possible documentation gaps or missing edges._