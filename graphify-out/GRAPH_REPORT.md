# Graph Report - lumen-launcher  (2026-09-14)

## Corpus Check
- 405 files · ~825,587 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3758 nodes · 10078 edges · 192 communities (136 shown, 50 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 1070 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `21fcc922`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- NextAlarmRepositoryTest
- .setContent
- .setContent
- NotificationAccessRepository
- .createViewModel
- Android launcher design planning/support.js
- .setContent
- Screens
- ManageFacetsViewModel
- FakeFacetDao
- NotificationBadgeRepository
- AppShortcut
- FacetDockAppRepository
- AppWidgetRepository
- Screens
- HomeDrawerRouteTest.kt
- FacetEntity
- ClockFontOption
- FacetCarouselUiState
- ExportBackupUseCaseTest.kt
- SettingsSearchEntry
- 4. Feature Requirements
- FacetDockFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- AppDrawerSettingsViewModel
- FacetCarouselScreen.kt
- Row
- ClockTemplateId
- FavoriteFolderPlacementEntity
- FavoriteAppRepository
- ExportBackupUseCase.kt
- FacetNavHost
- WidgetPlacementRepository
- BackupRestoreScreenTest.kt
- WidgetProviderOption
- OnboardingScreen
- FolderTestFakes.kt
- ContactConnection
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- FacetDao
- BackupRestoreContent
- DockAppRepository
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- CalendarPermissionRepository
- FacetEntity.kt
- FolderDetailScreen.kt
- CardDivider
- AppRepository
- 4. Feature Requirements
- AppDrawerScreen.kt
- PlaceWidgetUseCase
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- Fixture
- BackupRestoreViewModelTest
- CalendarEventsBlock.kt
- .drawerViewModel
- HubWidgetTile
- dashedBorder
- github.md
- .createViewModel
- .setContent
- GetInstalledAppsUseCase
- .setContent
- CalendarSettingsScreen.kt
- combine
- .setContent
- AppearanceSettingsScreen.kt
- .setContent
- FolderAppPickerViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- .setContent
- AppIcon
- FavoriteAppDaoTest
- FacetSettingsContent
- .repository
- FavoritesPickerScreen.kt
- Lumen Launcher Implementation Plan
- AccentSwatch
- Play Console — sensitive permission disclosures
- .setContent
- ImportBackupUseCase.kt
- .setContent
- Facet Launcher — Built Capabilities
- AppWidgetRepository.kt
- Play Console — store listing text
- DrawerViewModel.kt
- ObserveHomeScreenStateUseCase.kt
- ListContentMode
- AppInfo
- DefaultLauncherRepositoryTest
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- FolderEntity
- SettingsViewModelTest
- PermissionKind
- HomeAppsListSettingsScreen.kt
- DefaultFavoriteAppDao
- Fixture
- BackButton
- LauncherSettings
- DockFolderPlacementEntity
- HubWidgetPickerViewModel
- .setContent
- FolderDetailViewModel
- FacetDatabaseMigrationTest
- HubContent
- WidgetPlacementDao
- .useCase
- FontWeightOption
- NotificationAccessExplanationViewModel.kt
- FolderRepository
- AddAppToDockUseCaseTest
- Type.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- HomeWallpaper
- HubAddWidgetEvent
- WidgetPlacementEntity
- ObserveFacetPreviewsUseCase.kt
- Facet Launcher — Onboarding Flow
- .setContent
- FavoritesPickerViewModel
- HubViewModel
- .setContent
- BackupRestoreViewModel
- DefaultFavoriteAppRepository
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- Color.kt
- SettingsRepositoryTest
- ComponentName
- FavoriteAppDao
- WallpaperRepositoryTest
- RemoveAppFromDockUseCase.kt
- OnboardingViewModel
- SharedFlow
- ContactRepository
- 2. Design tokens
- NotificationSettingsScreen.kt
- .setContent
- WidgetResizeHandle.kt
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- FacetDockAppDaoTest
- .setContent
- ObserveClockAccessoriesUseCase.kt
- SettingsViewModel
- FacetDatabase
- FacetEntityTest
- .setContent
- calculateHubCellWidth
- Color
- Dp
- AddAppToFavoritesUseCaseTest
- .setContent
- FontWeight
- FolderDao
- DefaultLauncherRepository
- OnboardingScreenTest.kt
- Folder
- HomeViewModel
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- RemoveAppFromFavoritesUseCase.kt
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 235 edges
2. `FacetEntity` - 183 edges
3. `AppInfo` - 159 edges
4. `SettingsRepository` - 147 edges
5. `Row` - 141 edges
6. `FacetRepository` - 121 edges
7. `LauncherSettings` - 108 edges
8. `ClockTemplateId` - 71 edges
9. `DockAppRepository` - 69 edges
10. `HomeScreen()` - 64 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `HomeScreen()` --calls--> `ClockAdjustSheet()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/home/ClockAdjustSheet.kt
- `HomeScreen()` --calls--> `ClockBlock()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/home/ClockBlock.kt
- `HomeScreen()` --calls--> `ClockCornerHandle()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/HomeScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/home/ClockCornerHandle.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (192 total, 50 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "NextAlarmRepositoryTest"
Cohesion: 0.09
Nodes (17): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+9 more)

### Community 4 - ".setContent"
Cohesion: 0.20
Nodes (9): FolderAppPickerScreenTest, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), PickerSectionHeader(), FolderAppPickerUiState (+1 more)

### Community 5 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (10): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow, ViewModel (+2 more)

### Community 6 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

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
Cohesion: 0.12
Nodes (7): StateFlow, ViewModel, ManageFacetsUiState, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 11 - "FakeFacetDao"
Cohesion: 0.20
Nodes (4): EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 12 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 13 - "AppShortcut"
Cohesion: 0.24
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 14 - "FacetDockAppRepository"
Cohesion: 0.12
Nodes (7): FacetDockAppRepository, Flow, FacetDockAppEntity, toFacetDockAppEntity(), FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.10
Nodes (7): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Context, Flow, IntentSender, AppWidgetRepositoryTest

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (11): AppModule, Context, CatalogEntry, AddAppToDockUseCase, AddAppToFavoritesUseCase, SeedDefaultDockUseCase, AppOpsManager, AppRepository (+3 more)

### Community 18 - "FacetEntity"
Cohesion: 0.06
Nodes (7): FacetRepository, Flow, toCsv(), FacetEntity, FacetRepositoryTest, FakeFacetDao, Flow

### Community 19 - "ClockFontOption"
Cohesion: 0.08
Nodes (19): T, resolveOverride(), ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX (+11 more)

### Community 21 - "ExportBackupUseCaseTest.kt"
Cohesion: 0.13
Nodes (6): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppDaoTest, ExportBackupUseCaseTest

### Community 22 - "SettingsSearchEntry"
Cohesion: 0.26
Nodes (3): SettingsSearchEntry, SystemSettingsRepository, SystemSettingsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FacetDockFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): FacetDockFolderPlacementDao, FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FacetDockFolderPlacementDaoTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "AppDrawerSettingsViewModel"
Cohesion: 0.09
Nodes (5): AppDrawerSettingsScreenTest, AppDrawerSettingsViewModel, StateFlow, ViewModel, AppDrawerSettingsViewModelTest

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.23
Nodes (15): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier (+7 more)

### Community 30 - "Row"
Cohesion: 0.12
Nodes (87): ClockDateStyle, CONDENSED, FULL, LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX (+79 more)

### Community 31 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 32 - "FavoriteFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): FavoriteFolderPlacementDao, FavoriteFolderPlacementEntity, FakeFavoriteFolderPlacementDao, FavoriteFolderPlacementDaoTest

### Community 33 - "FavoriteAppRepository"
Cohesion: 0.12
Nodes (7): FavoriteAppRepository, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 34 - "ExportBackupUseCase.kt"
Cohesion: 0.20
Nodes (7): BackupRepository, Uri, BackupBundle, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 35 - "FacetNavHost"
Cohesion: 0.17
Nodes (15): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), Modifier (+7 more)

### Community 36 - "WidgetPlacementRepository"
Cohesion: 0.16
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 37 - "BackupRestoreScreenTest.kt"
Cohesion: 0.21
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 38 - "WidgetProviderOption"
Cohesion: 0.25
Nodes (15): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+7 more)

### Community 39 - "OnboardingScreen"
Cohesion: 0.24
Nodes (11): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO, OnboardingSubScreen (+3 more)

### Community 40 - "FolderTestFakes.kt"
Cohesion: 0.15
Nodes (4): DefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementEntity, FakeDefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementDaoTest

### Community 41 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.05
Nodes (43): SetDefaultLauncherSheetTest, GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), AlarmClockIcon(), BatteryIcon() (+35 more)

### Community 44 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 45 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 46 - "DockAppRepository"
Cohesion: 0.18
Nodes (6): DockAppRepository, DockAppEntity, Flow, AppInfo, DockFolderPlacementEntity, PlacedItem

### Community 47 - "HomeScreen.kt"
Cohesion: 0.17
Nodes (30): DockDisplayMode, ICONS, TEXT, DrawerPresentation, GRID, LIST, NotificationBadgeStyle, COUNT (+22 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "CalendarPermissionRepository"
Cohesion: 0.07
Nodes (12): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase (+4 more)

### Community 51 - "FacetEntity.kt"
Cohesion: 0.10
Nodes (11): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, SearchBarPosition, BOTTOM, TOP (+3 more)

### Community 52 - "FolderDetailScreen.kt"
Cohesion: 0.16
Nodes (14): Modifier, RenameDialog(), ReorderRowDefaults, FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview() (+6 more)

### Community 53 - "CardDivider"
Cohesion: 0.27
Nodes (16): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+8 more)

### Community 54 - "AppRepository"
Cohesion: 0.08
Nodes (14): DockSettingsScreenTest, AppRepository, DockSettingsViewModel, StateFlow, ViewModel, any(), AppRepositoryTest, eq() (+6 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.21
Nodes (23): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupedApps, ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip() (+15 more)

### Community 57 - "PlaceWidgetUseCase"
Cohesion: 0.39
Nodes (4): HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 59 - "HomeScreen"
Cohesion: 0.10
Nodes (11): HomeScreenTest, AppInfo, HomeScreen(), stableKey(), CalendarEvent, ClockColorOption, ClockDateStyle, ClockFontOption (+3 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.20
Nodes (9): ClockAdjustSheetTest, AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier (+1 more)

### Community 63 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 65 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 66 - "dashedBorder"
Cohesion: 0.24
Nodes (10): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier (+2 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".createViewModel"
Cohesion: 0.26
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 69 - ".setContent"
Cohesion: 0.21
Nodes (6): KeyboardDismissalTest, CleanUpUninstalledAppsUseCase, Flow, ObserveQuickAddStateUseCase, CleanUpUninstalledAppsUseCaseTest, LauncherViewModel

### Community 70 - "GetInstalledAppsUseCase"
Cohesion: 0.16
Nodes (8): GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 71 - ".setContent"
Cohesion: 0.06
Nodes (34): AppContextMenuTest, Folder, FolderTileContextMenuTest, QuickAddState, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow() (+26 more)

### Community 72 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 73 - "combine"
Cohesion: 0.18
Nodes (10): Flow, combine(), Flow, T1, T2, T3, T4, T5 (+2 more)

### Community 74 - ".setContent"
Cohesion: 0.12
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 75 - "AppearanceSettingsScreen.kt"
Cohesion: 0.08
Nodes (29): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+21 more)

### Community 76 - ".setContent"
Cohesion: 0.19
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "AppIcon"
Cohesion: 0.26
Nodes (16): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel(), Dp, Modifier, NotificationBadge() (+8 more)

### Community 82 - "FacetSettingsContent"
Cohesion: 0.29
Nodes (11): FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron(), SectionHeader(), appsSubtitle(), FacetSettingsContent() (+3 more)

### Community 83 - ".repository"
Cohesion: 0.17
Nodes (8): DockAppRepositoryTest, FakeDockAppDao, DockAppEntity, Flow, FakeDockFolderPlacementDao, DockAppDao, DockFolderPlacementDao, FolderDao

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

### Community 89 - "ImportBackupUseCase.kt"
Cohesion: 0.14
Nodes (16): BackupAppEntry, BackupFacet, BackupSettings, BackupWidgetPlacement, T, toBackupEntry(), toBackupFacet(), toBackupPlacement() (+8 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "AppWidgetRepository.kt"
Cohesion: 0.24
Nodes (5): flattenIcon(), Bitmap, Bitmap, toBitmap(), Drawable

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "DrawerViewModel.kt"
Cohesion: 0.19
Nodes (6): ContactInfo, T, RankBySearchRelevanceUseCase, DrawerViewModel, StateFlow, ViewModel

### Community 95 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.20
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 96 - "ListContentMode"
Cohesion: 0.05
Nodes (28): DataStoreModule, Context, Converters, AppListLimits, AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition (+20 more)

### Community 97 - "AppInfo"
Cohesion: 0.24
Nodes (4): AppInfo, GroupAppsByLetterUseCase, AppDrawerScreenGridPreview(), GetInstalledAppsUseCaseTest

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.12
Nodes (8): AlphabetRailTest, ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview() (+4 more)

### Community 102 - "SettingsRepository"
Cohesion: 0.06
Nodes (6): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, SettingsRepository

### Community 103 - "FolderEntity"
Cohesion: 0.40
Nodes (3): FolderAppEntity, FolderEntity, FolderDaoTest

### Community 105 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 106 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreenPreview(), AppInfo, Modifier (+1 more)

### Community 109 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.20
Nodes (4): LauncherSettings, HomeUiState, FacetCarouselUiStateTest, HomeUiStateTest

### Community 111 - "DockFolderPlacementEntity"
Cohesion: 0.18
Nodes (3): DockFolderPlacementDao, DockFolderPlacementEntity, DockFolderPlacementDaoTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 114 - "FolderDetailViewModel"
Cohesion: 0.23
Nodes (3): FolderDetailUiState, FolderDetailViewModel, FolderDetailViewModelTest

### Community 116 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 118 - ".useCase"
Cohesion: 0.06
Nodes (9): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, ImportBackupUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest (+1 more)

### Community 119 - "FontWeightOption"
Cohesion: 0.14
Nodes (11): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+3 more)

### Community 120 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 121 - "FolderRepository"
Cohesion: 0.11
Nodes (3): FolderRepository, FolderRepositoryTest, FakeFolderDao

### Community 123 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "HomeWallpaper"
Cohesion: 0.13
Nodes (12): HomeWallpaper, Image, Tones, Unavailable, Bitmap, WallpaperRepository, Alignment, Modifier (+4 more)

### Community 132 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.11
Nodes (14): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase (+6 more)

### Community 134 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.42
Nodes (4): FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 138 - "HubViewModel"
Cohesion: 0.13
Nodes (12): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+4 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.11
Nodes (16): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+8 more)

### Community 141 - "DefaultFavoriteAppRepository"
Cohesion: 0.15
Nodes (6): DefaultFavoriteAppRepository, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "Color.kt"
Cohesion: 0.33
Nodes (8): Color, resolve(), accentTonalExtremes(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme, Shadow

### Community 146 - "ComponentName"
Cohesion: 0.22
Nodes (6): Intent, Intent, LauncherActivity, Bundle, ComponentActivity, ComponentName

### Community 149 - "RemoveAppFromDockUseCase.kt"
Cohesion: 0.38
Nodes (3): RemoveAppFromDockUseCase, Fixture, RemoveAppFromDockUseCaseTest

### Community 150 - "OnboardingViewModel"
Cohesion: 0.10
Nodes (12): FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview(), OnboardingUiState, StateFlow (+4 more)

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "NotificationSettingsScreen.kt"
Cohesion: 0.57
Nodes (6): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), NotificationSettingsUiState

### Community 156 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 161 - "ObserveClockAccessoriesUseCase.kt"
Cohesion: 0.47
Nodes (3): ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase

### Community 162 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 163 - "FacetDatabase"
Cohesion: 0.11
Nodes (8): DatabaseModule, Context, FacetDatabase, FacetDockAppDao, Flow, Migrations, Migration, RoomDatabase

### Community 170 - ".setContent"
Cohesion: 0.06
Nodes (9): AppearanceSettingsScreenTest, HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase, DefaultAppRepositoryTest, AppearanceSettingsViewModelTest, WallpaperRepository (+1 more)

### Community 174 - "DefaultLauncherRepository"
Cohesion: 0.24
Nodes (5): DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

### Community 181 - "OnboardingScreenTest.kt"
Cohesion: 0.29
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 182 - "Folder"
Cohesion: 0.08
Nodes (14): FolderDetailScreenTest, Folder, AddFolderToDockUseCase, AddFolderToFavoritesUseCase, RemoveFolderFromDockUseCase, RemoveFolderFromFavoritesUseCase, AddFolderToDockUseCaseTest, Fixture (+6 more)

### Community 187 - "HomeViewModel"
Cohesion: 0.11
Nodes (6): NotificationShadeRepository, HomeViewModel, Intent, StateFlow, ViewModel, NotificationShadeRepositoryTest

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 203 - "RemoveAppFromFavoritesUseCase.kt"
Cohesion: 0.38
Nodes (3): RemoveAppFromFavoritesUseCase, Fixture, RemoveAppFromFavoritesUseCaseTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.18
Nodes (15): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DockAppsRow(), DockClickableRow(), dockDisplayLabel() (+7 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

## Knowledge Gaps
- **370 isolated node(s):** `ICONS`, `TEXT`, `LIST`, `GRID`, `FOUR_BY_FOUR` (+365 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 740 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **50 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockStyleGalleryViewModel`, `.setContent`, `.setContent`, `WidgetPlacementEntity`, `ObserveFacetPreviewsUseCase.kt`, `NotificationAccessRepository`, `.setContent`, `.setContent`, `.setContent`, `HomeDrawerRouteTest.kt`, `ComponentName`, `OnboardingViewModel`, `.setContent`, `NotificationSettingsScreen.kt`, `.setContent`, `AppDrawerSettingsViewModel`, `FacetCarouselScreen.kt`, `WidgetResizeHandle.kt`, `Row`, `.setContent`, `FacetDatabase`, `FacetNavHost`, `.setContent`, `BackupRestoreScreenTest.kt`, `WidgetProviderOption`, `ContactConnection`, `.setContent`, `HomeDrawerRoute`, `BackupRestoreContent`, `DefaultLauncherRepository`, `HomeScreen.kt`, `CalendarPermissionRepository`, `FolderDetailScreen.kt`, `OnboardingScreenTest.kt`, `AppRepository`, `Folder`, `AppDrawerScreen.kt`, `CardDivider`, `LabeledDropdownRow`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `ColorTest`, `dashedBorder`, `.setContent`, `.setContent`, `CalendarSettingsScreen.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `.setContent`, `AppIcon`, `FacetSettingsContent`, `AppDrawerSettingsScreen.kt`, `FavoritesPickerScreen.kt`, `AccentSwatch`, `.setContent`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `AppInfo`, `AppDrawerScreen`, `TonalButton`, `HomeAppsListSettingsScreen.kt`, `BackButton`, `.setContent`, `FontWeightOption`, `Type.kt`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `HomeWallpaper`, `NotificationAccessRepository`, `ObserveFacetPreviewsUseCase.kt`, `.createViewModel`, `FavoritesPickerViewModel`, `NotificationBadgeRepository`, `DefaultFavoriteAppRepository`, `FacetDockAppRepository`, `AppShortcut`, `HomeDrawerRouteTest.kt`, `ComponentName`, `ClockFontOption`, `FacetEntity`, `OnboardingViewModel`, `FacetCarouselScreen.kt`, `FavoriteAppRepository`, `FacetNavHost`, `ContactConnection`, `.setContent`, `HomeDrawerRoute`, `AddAppToFavoritesUseCaseTest`, `DefaultLauncherRepository`, `HomeScreen.kt`, `OnboardingScreenTest.kt`, `CardDivider`, `AppRepository`, `AppDrawerScreen.kt`, `SettingsScreen.kt`, `Fixture`, `.createViewModel`, `GetInstalledAppsUseCase`, `.setContent`, `combine`, `.setContent`, `AppearanceSettingsScreen.kt`, `DockSettingsScreen.kt`, `AppIcon`, `FavoritesPickerScreen.kt`, `.setContent`, `Fixture`, `AppWidgetRepository.kt`, `DrawerViewModel.kt`, `ObserveHomeScreenStateUseCase.kt`, `ListContentMode`, `AppDrawerScreen`, `SettingsViewModelTest`, `Fixture`, `.setContent`, `.useCase`, `AddAppToDockUseCaseTest`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `HomeWallpaper`, `NotificationAccessRepository`, `ObserveFacetPreviewsUseCase.kt`, `.createViewModel`, `ManageFacetsViewModel`, `.setContent`, `FakeFacetDao`, `HomeDrawerRouteTest.kt`, `SettingsRepositoryTest`, `ClockFontOption`, `FacetEntity`, `ExportBackupUseCaseTest.kt`, `OnboardingViewModel`, `.setContent`, `AppDrawerSettingsViewModel`, `.setContent`, `ExportBackupUseCase.kt`, `FacetDatabase`, `.setContent`, `BackupRestoreScreenTest.kt`, `.setContent`, `DefaultLauncherRepository`, `HomeScreen.kt`, `CalendarPermissionRepository`, `FacetEntity.kt`, `OnboardingScreenTest.kt`, `AppRepository`, `AppDrawerScreen.kt`, `HomeViewModel`, `.drawerViewModel`, `.createViewModel`, `.setContent`, `GetInstalledAppsUseCase`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `.setContent`, `ImportBackupUseCase.kt`, `DrawerViewModel.kt`, `ObserveHomeScreenStateUseCase.kt`, `ListContentMode`, `LauncherSettings`, `.useCase`, `FontWeightOption`, `NotificationAccessExplanationViewModel.kt`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Are the 44 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 44 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 3 inferred relationships involving `SettingsRepository` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`SettingsRepository` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 136 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 136 INFERRED edges - model-reasoned connections that need verification._