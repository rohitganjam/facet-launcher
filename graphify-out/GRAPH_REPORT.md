# Graph Report - lumen-launcher  (2026-09-14)

## Corpus Check
- 405 files · ~826,347 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3822 nodes · 10097 edges · 198 communities (134 shown, 58 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 1052 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7b7c9199`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModelTest
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- .setContent
- CalendarPermissionRepository
- HomeWallpaper
- Android launcher design planning/support.js
- .setContent
- Screens
- ManageFacetsViewModel
- FakeFacetDao
- .refresh
- AppShortcutRepository
- FakeFacetDockAppDao
- AppWidgetRepository
- Screens
- HomeDrawerRouteTest.kt
- FacetEntity
- LauncherSettings
- FacetCarouselUiState
- DockAppEntity
- SettingsSearchEntry
- 4. Feature Requirements
- FacetDockFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- AppDrawerSettingsViewModelTest
- FontWeightOption
- Row
- ClockTemplateId
- FavoriteFolderPlacementEntity
- FakeFavoriteAppDao
- ExportBackupUseCase.kt
- BackButton
- WidgetPlacementEntity
- LauncherAppWidgetHost
- HubWidgetPickerScreen.kt
- OnboardingScreen
- FolderTestFakes.kt
- ContactConnectionsSheet.kt
- Manrope Font License (SIL OFL 1.1)
- ClockAccessoryIcons.kt
- FacetDockAppEntity
- HomeDrawerRoute
- DockAppRepository.kt
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- CalendarInfo
- DrawerListItemSize
- StickyHeaderLayout
- CardDivider
- ContactRepositoryTest
- 4. Feature Requirements
- AppDrawerScreen.kt
- AppContextMenu
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- .setContent
- BackupRestoreViewModelTest
- CalendarEventsBlock.kt
- .drawerViewModel
- HubWidgetTile
- dashedBorder
- github.md
- .setContent
- .setContent
- GetInstalledAppsUseCase
- .setContent
- CalendarSettingsScreen.kt
- AppInfo
- .setContent
- SettingsRepository.kt
- .setContent
- FolderAppPickerViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- AppIcon
- DockAppPickerViewModel
- FavoriteAppEntity
- .setContent
- .repository
- FavoritesPickerScreen.kt
- Lumen Launcher Implementation Plan
- AccentSwatch
- Play Console — sensitive permission disclosures
- .setContent
- FolderContentsSheet.kt
- .setContent
- Facet Launcher — Built Capabilities
- AppearanceSettingsScreen.kt
- Play Console — store listing text
- DrawerViewModel
- ObserveHomeScreenStateUseCase
- ClockFontOption
- CalendarSettingsViewModel
- NextAlarmRepositoryTest
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- FolderEntity
- .createViewModel
- PermissionKind
- HomeAppsListSettingsScreen.kt
- DefaultFavoriteAppDao
- Fixture
- SetDefaultLauncherSheetTest
- HomeUiState
- FakeDockFolderPlacementDao
- HubWidgetPickerViewModel
- .setContent
- FolderDetailViewModel
- FacetDatabaseMigrationTest
- OnboardingFacetsPage.kt
- .setContent
- .useCase
- ClockStyleGalleryScreen.kt
- NotificationAccessExplanationViewModel.kt
- FolderRepository
- AppModule
- Type.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- OnboardingIntroPage.kt
- NotificationShadeRepository
- ResolveWidgetDropUseCase
- FacetCarouselViewModel.kt
- Facet Launcher — Onboarding Flow
- GestureHintOverlay
- FavoritesPickerViewModel
- HubViewModel
- .setContent
- BackupRestoreViewModel
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- homeAppLabelShadow
- SettingsRepositoryTest
- LauncherActivity.kt
- FavoriteAppDao
- WallpaperRepositoryTest
- RemoveAppFromDockUseCase
- OnboardingViewModelTest
- SharedFlow
- ContactRepository
- 2. Design tokens
- NotificationSettingsViewModel
- DefaultAppRepositoryTest
- WidgetResizeHandle.kt
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- UsageAccessExplanationViewModel
- ClockAdjustMode
- .rendersOneEntryPerLetterProvided
- Migrations
- FacetDatabase
- ClockAlignment.kt
- .setContent
- Flow
- Color
- Dp
- Flow
- .createViewModel
- FontWeight
- Intent
- FolderDao
- DockAppRepository
- androidx
- Modifier
- NestedScrollConnection
- NestedScrollSource
- Offset
- LauncherViewModel
- Folder
- HomeViewModel
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- RemoveAppFromFavoritesUseCase
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- ObserveQuickAddStateUseCaseTest
- ClockAccessoryIconsTest
- PermissionsScreen.kt
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 235 edges
2. `AppInfo` - 146 edges
3. `FacetEntity` - 142 edges
4. `Row` - 141 edges
5. `SettingsRepository` - 135 edges
6. `FacetRepository` - 112 edges
7. `LauncherSettings` - 71 edges
8. `ClockTemplateId` - 69 edges
9. `DockAppRepository` - 63 edges
10. `HomeScreen()` - 63 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `Fixture` --calls--> `ObserveHomeScreenStateUseCase`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/domain/ObserveHomeScreenStateUseCaseTest.kt → app/src/main/kotlin/com/facetlauncher/app/domain/ObserveHomeScreenStateUseCase.kt
- `AppContextMenu()` --calls--> `QuickAddState`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/components/AppContextMenu.kt → app/src/main/kotlin/com/facetlauncher/app/domain/ObserveQuickAddStateUseCase.kt
- `FolderTileContextMenu()` --calls--> `QuickAddState`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/components/FolderTileContextMenu.kt → app/src/main/kotlin/com/facetlauncher/app/domain/ObserveQuickAddStateUseCase.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (198 total, 58 thin omitted)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.10
Nodes (19): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+11 more)

### Community 4 - ".setContent"
Cohesion: 0.20
Nodes (9): FolderAppPickerScreenTest, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), PickerSectionHeader(), FolderAppPickerUiState (+1 more)

### Community 5 - "CalendarPermissionRepository"
Cohesion: 0.10
Nodes (12): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 6 - "HomeWallpaper"
Cohesion: 0.09
Nodes (12): HomeWallpaper, Tones, Unavailable, Bitmap, WallpaperRepository, DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository (+4 more)

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
Cohesion: 0.10
Nodes (6): ManageFacetsScreenTest, StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, ManageFacetsViewModelTest

### Community 11 - "FakeFacetDao"
Cohesion: 0.20
Nodes (4): EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 12 - ".refresh"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 13 - "AppShortcutRepository"
Cohesion: 0.26
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 14 - "FakeFacetDockAppDao"
Cohesion: 0.24
Nodes (3): FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.07
Nodes (13): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+5 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "HomeDrawerRouteTest.kt"
Cohesion: 0.13
Nodes (7): AppOpsManager, AppRepository, FacetRepository, ListContentMode, SystemSettingsRepository, UsageStatsManager, WallpaperManager

### Community 18 - "FacetEntity"
Cohesion: 0.04
Nodes (11): FacetRepository, Flow, toCsv(), FacetDao, Flow, FacetEntity, FacetRepositoryTest, FakeFacetDao (+3 more)

### Community 19 - "LauncherSettings"
Cohesion: 0.08
Nodes (15): ClockAlignment, CENTER, LEFT, RIGHT, LauncherSettings, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+7 more)

### Community 21 - "DockAppEntity"
Cohesion: 0.16
Nodes (5): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppDaoTest

### Community 22 - "SettingsSearchEntry"
Cohesion: 0.22
Nodes (4): SettingsSearchEntry, CatalogEntry, SystemSettingsRepository, SystemSettingsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FacetDockFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): FacetDockFolderPlacementDao, FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FacetDockFolderPlacementDaoTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 29 - "FontWeightOption"
Cohesion: 0.07
Nodes (29): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, DockDisplayMode (+21 more)

### Community 30 - "Row"
Cohesion: 0.19
Nodes (70): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+62 more)

### Community 31 - "ClockTemplateId"
Cohesion: 0.05
Nodes (46): BackupAppEntry, BackupFacet, BackupWidgetPlacement, ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED (+38 more)

### Community 32 - "FavoriteFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): FavoriteFolderPlacementDao, FavoriteFolderPlacementEntity, FakeFavoriteFolderPlacementDao, FavoriteFolderPlacementDaoTest

### Community 33 - "FakeFavoriteAppDao"
Cohesion: 0.24
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 34 - "ExportBackupUseCase.kt"
Cohesion: 0.16
Nodes (9): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest (+1 more)

### Community 35 - "BackButton"
Cohesion: 0.13
Nodes (20): BackButton(), Modifier, FacetDestinations, FacetNavHost(), Modifier, popBackStackSafely(), Modifier, NotificationAccessExplanationContent() (+12 more)

### Community 36 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 37 - "LauncherAppWidgetHost"
Cohesion: 0.20
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 38 - "HubWidgetPickerScreen.kt"
Cohesion: 0.14
Nodes (20): HubWidgetPickerScreenTest, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+12 more)

### Community 39 - "OnboardingScreen"
Cohesion: 0.24
Nodes (11): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO, OnboardingSubScreen (+3 more)

### Community 40 - "FolderTestFakes.kt"
Cohesion: 0.15
Nodes (4): DefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementEntity, FakeDefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementDaoTest

### Community 41 - "ContactConnectionsSheet.kt"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 43 - "ClockAccessoryIcons.kt"
Cohesion: 0.18
Nodes (16): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+8 more)

### Community 44 - "FacetDockAppEntity"
Cohesion: 0.12
Nodes (4): FacetDockAppEntity, toFacetDockAppEntity(), FacetDaoTest, FacetDockAppDaoTest

### Community 45 - "HomeDrawerRoute"
Cohesion: 0.16
Nodes (16): androidx, Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, AppInfo (+8 more)

### Community 46 - "DockAppRepository.kt"
Cohesion: 0.19
Nodes (5): DockAppEntity, Flow, AppInfo, DockFolderPlacementDao, PlacedItem

### Community 47 - "HomeScreen.kt"
Cohesion: 0.29
Nodes (20): DrawerPresentation, GRID, LIST, AppRow(), dashedBorder(), DockIcon(), FolderDockIcon(), FolderRow() (+12 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "CalendarInfo"
Cohesion: 0.20
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo

### Community 51 - "DrawerListItemSize"
Cohesion: 0.08
Nodes (16): ContactInfo, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT (+8 more)

### Community 52 - "StickyHeaderLayout"
Cohesion: 0.27
Nodes (9): ReorderRowDefaults, Modifier, StickyHeaderLayout(), FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview() (+1 more)

### Community 53 - "CardDivider"
Cohesion: 0.14
Nodes (23): ConfirmDialog(), Modifier, DragReorderState, Modifier, T, rememberDragReorderState(), CardDivider(), Modifier (+15 more)

### Community 54 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.20
Nodes (23): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip() (+15 more)

### Community 57 - "AppContextMenu"
Cohesion: 0.20
Nodes (18): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow(), AppInfo, DrawerPresentation, Folder, Modifier (+10 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.19
Nodes (21): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+13 more)

### Community 59 - "HomeScreen"
Cohesion: 0.13
Nodes (3): HomeScreenTest, AppInfo, HomeScreen()

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.20
Nodes (9): ClockAdjustSheetTest, AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier (+1 more)

### Community 63 - "CalendarEventsBlock.kt"
Cohesion: 0.29
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+3 more)

### Community 64 - ".drawerViewModel"
Cohesion: 0.15
Nodes (5): T, RankBySearchRelevanceUseCase, DrawerViewModelTest, SettingsRepository, SettingsSearchEntry

### Community 65 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 66 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 69 - ".setContent"
Cohesion: 0.15
Nodes (12): KeyboardDismissalTest, LauncherViewModel, AddAppToDockUseCase, AddAppToFavoritesUseCase, AddFolderToDockUseCase, AddFolderToFavoritesUseCase, CleanUpUninstalledAppsUseCase, PlaceWidgetUseCase (+4 more)

### Community 70 - "GetInstalledAppsUseCase"
Cohesion: 0.16
Nodes (8): GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 71 - ".setContent"
Cohesion: 0.09
Nodes (6): AppContextMenuTest, Folder, FolderTileContextMenuTest, QuickAddState, AppShortcut, SharedFlow

### Community 72 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 73 - "AppInfo"
Cohesion: 0.06
Nodes (17): Flow, Flow, Flow, AppInfo, combine(), Flow, StateFlow, ViewModel (+9 more)

### Community 74 - ".setContent"
Cohesion: 0.12
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 75 - "SettingsRepository.kt"
Cohesion: 0.06
Nodes (30): DataStoreModule, Context, AppListLimits, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, LauncherFontOption (+22 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "AppIcon"
Cohesion: 0.28
Nodes (13): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), AlreadyDefaultContent() (+5 more)

### Community 80 - "DockAppPickerViewModel"
Cohesion: 0.22
Nodes (13): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+5 more)

### Community 81 - "FavoriteAppEntity"
Cohesion: 0.17
Nodes (3): FavoriteAppEntity, toFavoriteAppEntity(), FavoriteAppDaoTest

### Community 82 - ".setContent"
Cohesion: 0.23
Nodes (3): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent

### Community 83 - ".repository"
Cohesion: 0.17
Nodes (7): DockAppRepositoryTest, FakeDockAppDao, DockAppEntity, Flow, DockAppDao, DockFolderPlacementEntity, FolderDao

### Community 84 - "FavoritesPickerScreen.kt"
Cohesion: 0.61
Nodes (7): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 89 - "FolderContentsSheet.kt"
Cohesion: 0.34
Nodes (13): AddHere, FolderContentsAppRow(), FolderContentsEmptyState(), FolderContentsGrid(), FolderContentsGridTile(), FolderContentsList(), FolderContentsSheet(), FolderSheetHeaderAction (+5 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "DrawerViewModel"
Cohesion: 0.09
Nodes (15): Add, AppInfo, Folder, PlacedItem, ObserveQuickAddStateUseCase, QuickPlacementAction, Remove, DrawerViewModel (+7 more)

### Community 95 - "ObserveHomeScreenStateUseCase"
Cohesion: 0.39
Nodes (5): FacetEntity, LauncherSettings, PlacedItem, ObserveHomeScreenStateUseCase, Flow

### Community 96 - "ClockFontOption"
Cohesion: 0.04
Nodes (29): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+21 more)

### Community 97 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 99 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview() (+4 more)

### Community 102 - "SettingsRepository"
Cohesion: 0.05
Nodes (6): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, SettingsRepository

### Community 103 - "FolderEntity"
Cohesion: 0.40
Nodes (3): FolderAppEntity, FolderEntity, FolderDaoTest

### Community 105 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 106 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.24
Nodes (15): HomeSurfacePreview(), Color, FontWeight, Modifier, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+7 more)

### Community 110 - "HomeUiState"
Cohesion: 0.08
Nodes (16): HomeUiState, FacetEntity, Fixture, ObserveHomeScreenStateUseCaseTest, HomeUiStateTest, AppListVerticalAlignment, AppRowPosition, AppRowPresentation (+8 more)

### Community 111 - "FakeDockFolderPlacementDao"
Cohesion: 0.15
Nodes (4): DockFolderPlacementDao, DockFolderPlacementEntity, FakeDockFolderPlacementDao, DockFolderPlacementDaoTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.12
Nodes (10): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel (+2 more)

### Community 116 - "OnboardingFacetsPage.kt"
Cohesion: 0.35
Nodes (9): Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview() (+1 more)

### Community 118 - ".useCase"
Cohesion: 0.06
Nodes (8): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest, SelectPreviewAppsUseCaseTest

### Community 119 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.31
Nodes (11): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+3 more)

### Community 120 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 121 - "FolderRepository"
Cohesion: 0.09
Nodes (6): flattenIcon(), Bitmap, FolderRepository, FolderRepositoryTest, FakeFolderDao, Drawable

### Community 123 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "OnboardingIntroPage.kt"
Cohesion: 0.31
Nodes (12): Image, Alignment, Modifier, WallpaperBackground(), AppRowBar(), ConceptLine(), HomeDiagram(), Dp (+4 more)

### Community 133 - "ResolveWidgetDropUseCase"
Cohesion: 0.11
Nodes (13): CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+5 more)

### Community 134 - "FacetCarouselViewModel.kt"
Cohesion: 0.19
Nodes (9): T, resolveOverride(), FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase, FacetCarouselViewModel, StateFlow (+1 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 137 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 138 - "HubViewModel"
Cohesion: 0.11
Nodes (18): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+10 more)

### Community 139 - ".setContent"
Cohesion: 0.07
Nodes (9): SettingsScreenTest, DefaultLauncherRepository, Intent, Intent, StateFlow, ViewModel, SettingsViewModel, DefaultLauncherRepositoryTest (+1 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.09
Nodes (29): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen() (+21 more)

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.16
Nodes (5): DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "homeAppLabelShadow"
Cohesion: 0.24
Nodes (12): Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview(), Color, resolve(), accentTonalExtremes(), homeAppLabelShadow(), Color (+4 more)

### Community 146 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 149 - "RemoveAppFromDockUseCase"
Cohesion: 0.47
Nodes (3): RemoveAppFromDockUseCase, Fixture, RemoveAppFromDockUseCaseTest

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "NotificationSettingsViewModel"
Cohesion: 0.36
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 156 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 160 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 163 - "FacetDatabase"
Cohesion: 0.14
Nodes (6): DatabaseModule, Context, FacetDatabase, FacetDockAppDao, Flow, RoomDatabase

### Community 165 - ".setContent"
Cohesion: 0.10
Nodes (19): FacetSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), FacetSettingsHeader(), FacetSettingsRow(), Composable (+11 more)

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 174 - "DockAppRepository"
Cohesion: 0.09
Nodes (12): BackupRestoreScreenTest, DockSettingsScreenTest, AppRepository, DefaultFavoriteAppRepository, DockAppRepository, FacetDockAppRepository, FavoriteAppRepository, ImportBackupUseCase (+4 more)

### Community 182 - "Folder"
Cohesion: 0.06
Nodes (20): FolderDetailScreenTest, FoldersSettingsScreenTest, Folder, FolderItem, PlacedItem, SingleApp, FolderRow(), FoldersSettingsContent() (+12 more)

### Community 187 - "HomeViewModel"
Cohesion: 0.09
Nodes (13): HomeScreenState, HomeViewModel, AppInfo, Folder, StateFlow, ViewModel, HomeViewModelTest, FacetEntity (+5 more)

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.33
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 203 - "RemoveAppFromFavoritesUseCase"
Cohesion: 0.47
Nodes (3): RemoveAppFromFavoritesUseCase, Fixture, RemoveAppFromFavoritesUseCaseTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier (+1 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

## Knowledge Gaps
- **370 isolated node(s):** `VERTICAL`, `HORIZONTAL`, `Tones`, `Unavailable`, `AddFailed` (+365 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 756 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `.setContent`, `.setContent`, `ResolveWidgetDropUseCase`, `CalendarPermissionRepository`, `OnboardingIntroPage.kt`, `.setContent`, `GestureHintOverlay`, `ManageFacetsViewModel`, `.setContent`, `BackupRestoreViewModel`, `homeAppLabelShadow`, `LauncherActivity.kt`, `.setContent`, `WidgetResizeHandle.kt`, `FontWeightOption`, `.rendersOneEntryPerLetterProvided`, `BackButton`, `.setContent`, `LauncherAppWidgetHost`, `HubWidgetPickerScreen.kt`, `ContactConnectionsSheet.kt`, `DockAppRepository`, `HomeScreen.kt`, `CalendarInfo`, `DrawerListItemSize`, `StickyHeaderLayout`, `CardDivider`, `Folder`, `AppDrawerScreen.kt`, `LabeledDropdownRow`, `HomeScreen`, `ClockAdjustSheet.kt`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `dashedBorder`, `.setContent`, `.setContent`, `.setContent`, `CalendarSettingsScreen.kt`, `.setContent`, `SettingsRepository.kt`, `.setContent`, `DockSettingsScreen.kt`, `AppIcon`, `DockAppPickerViewModel`, `.setContent`, `AppDrawerSettingsScreen.kt`, `FavoritesPickerScreen.kt`, `AccentSwatch`, `.setContent`, `.setContent`, `AppearanceSettingsScreen.kt`, `PermissionsScreen.kt`, `.setContent`, `AppDrawerScreen`, `TonalButton`, `HomeAppsListSettingsScreen.kt`, `SetDefaultLauncherSheetTest`, `.setContent`, `OnboardingFacetsPage.kt`, `.setContent`, `ClockStyleGalleryScreen.kt`, `Type.kt`?**
  _High betweenness centrality (0.200) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `FacetCarouselViewModel.kt`, `HomeWallpaper`, `FavoritesPickerViewModel`, `.setContent`, `.refresh`, `DefaultFavoriteAppEntity`, `FakeFacetDockAppDao`, `HomeDrawerRouteTest.kt`, `LauncherActivity.kt`, `LauncherSettings`, `OnboardingViewModelTest`, `FontWeightOption`, `FakeFavoriteAppDao`, `BackButton`, `FacetDockAppEntity`, `DockAppRepository`, `DrawerListItemSize`, `CardDivider`, `ContactRepositoryTest`, `AppDrawerScreen.kt`, `SettingsScreen.kt`, `.setContent`, `GetInstalledAppsUseCase`, `.setContent`, `SettingsRepository.kt`, `DockSettingsScreen.kt`, `AppIcon`, `DockAppPickerViewModel`, `FavoriteAppEntity`, `.setContent`, `FavoritesPickerScreen.kt`, `AppearanceSettingsScreen.kt`, `ClockFontOption`, `AppDrawerScreen`, `HomeAppsListSettingsScreen.kt`, `Fixture`, `.useCase`, `FolderRepository`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModelTest`, `CalendarPermissionRepository`, `FacetCarouselViewModel.kt`, `HomeWallpaper`, `ManageFacetsViewModel`, `.setContent`, `FakeFacetDao`, `HomeDrawerRouteTest.kt`, `SettingsRepositoryTest`, `LauncherSettings`, `FacetEntity`, `.setContent`, `NotificationSettingsViewModel`, `AppDrawerSettingsViewModelTest`, `FontWeightOption`, `ClockTemplateId`, `ExportBackupUseCase.kt`, `.setContent`, `.createViewModel`, `DockAppRepository`, `CalendarInfo`, `DrawerListItemSize`, `.setContent`, `.setContent`, `GetInstalledAppsUseCase`, `AppInfo`, `.setContent`, `SettingsRepository.kt`, `.setContent`, `.setContent`, `ClockFontOption`, `CalendarSettingsViewModel`, `.createViewModel`, `.setContent`, `.useCase`, `NotificationAccessExplanationViewModel.kt`?**
  _High betweenness centrality (0.068) - this node is a cross-community bridge._
- **Are the 44 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 44 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 136 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 136 INFERRED edges - model-reasoned connections that need verification._
- **What connects `VERTICAL`, `HORIZONTAL`, `Tones` to the rest of the system?**
  _370 weakly-connected nodes found - possible documentation gaps or missing edges._