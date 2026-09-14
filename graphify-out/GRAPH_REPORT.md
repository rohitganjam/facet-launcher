# Graph Report - lumen-launcher  (2026-09-14)

## Corpus Check
- 405 files · ~825,397 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3731 nodes · 10007 edges · 196 communities (143 shown, 47 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 1000 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c9a5a23c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- NextAlarmRepositoryTest
- .setContent
- .setContent
- CalendarPermissionRepository
- HomeWallpaper
- Android launcher design planning/support.js
- .setContent
- Screens
- ManageFacetsViewModel
- SettingsRepositoryTest.kt
- NotificationBadgeRepository
- AppShortcut
- FacetDockAppRepository
- AppWidgetRepository
- Screens
- DockAppRepository
- FacetEntity
- ImportBackupUseCase.kt
- FacetCarouselUiState
- DockAppEntity
- SystemSettingsRepositoryTest
- 4. Feature Requirements
- FacetDockFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerGridSize
- FacetCarouselScreen.kt
- Row
- ClockTemplateId
- FavoriteFolderPlacementEntity
- FavoriteAppRepository
- ExportBackupUseCase.kt
- FacetNavHost
- WidgetPlacementRepository
- LauncherAppWidgetHost
- WidgetProviderOption
- OnboardingScreen
- FolderTestFakes.kt
- .setContent
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- FacetDao
- BackupRestoreContent
- AppInfo
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- AccentSwatch
- AppearanceSettingsViewModel
- AppearanceSettingsScreen.kt
- CardDivider
- ContactRepositoryTest
- 4. Feature Requirements
- AppDrawerScreen.kt
- PlaceWidgetUseCase
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- Fixture
- BackupRestoreViewModelTest
- CalendarEvent
- .drawerViewModel
- HubGrid
- dashedBorder
- github.md
- .createViewModel
- .setContent
- LauncherViewModel.kt
- .setContent
- ManageFacetsScreen.kt
- combine
- .setContent
- WallpaperAccentRole
- .setContent
- FolderAppPickerViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- .setContent
- DockAppPickerScreen.kt
- FavoriteAppDaoTest
- AddFolderToDockUseCase
- .repository
- FavoritesPickerScreen.kt
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- AppRepository
- BackupMapping.kt
- .setContent
- Facet Launcher — Built Capabilities
- ClockAccessoryIcons.kt
- Play Console — store listing text
- .setContent
- .homeViewModel
- ClockColorOption
- SetDefaultLauncherSheetTest
- NotificationShadeRepository
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- FolderEntity
- DragReorderState
- PermissionKind
- HomeAppsListSettingsScreen.kt
- DefaultFavoriteAppDao
- Fixture
- BackButton
- LauncherSettings
- DockFolderPlacementEntity
- HubWidgetPickerViewModel
- FacetDockAppDao
- FolderDetailScreen.kt
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
- FacetCarouselViewModel
- HubAddWidgetEvent
- WidgetPlacementEntity
- UsageAccessExplanationViewModel
- Facet Launcher — Onboarding Flow
- LauncherFontOption
- FavoritesPickerViewModel
- HubViewModel
- .setContent
- BackupRestoreViewModel
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- Color.kt
- SettingsRepositoryTest
- LauncherActivity.kt
- FavoriteAppDao
- WallpaperRepositoryTest
- RemoveAppFromDockUseCase
- OnboardingViewModelTest
- SharedFlow
- ObserveHubStateUseCase
- 2. Design tokens
- DefaultAppRepositoryTest
- GestureHintOverlay
- WidgetResizeHandle.kt
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- FacetDockAppDaoTest
- .setContent
- RemoveFolderFromDockUseCase
- SetDefaultLauncherSheet
- FacetDatabase
- BackupRestoreMessage
- .setContent
- build_screenshot.py
- FontWeightSlider
- ClockAdjustMode
- AddAppToFavoritesUseCaseTest
- .createViewModel
- Migrations
- FolderDao
- AppInfo
- .setContent
- Folder
- HomeViewModel
- SettingsScreen.kt
- OnboardingFacetsPage.kt
- ColorTest
- letterAt
- release.sh
- AppIcon
- AddFolderToFavoritesUseCase
- RemoveAppFromFavoritesUseCase
- RemoveFolderFromFavoritesUseCase
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 236 edges
2. `FacetEntity` - 183 edges
3. `AppInfo` - 169 edges
4. `SettingsRepository` - 148 edges
5. `Row` - 138 edges
6. `FacetRepository` - 122 edges
7. `LauncherSettings` - 108 edges
8. `ClockTemplateId` - 73 edges
9. `DockAppRepository` - 69 edges
10. `ClockDateStyle` - 61 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `ClockStyleGalleryViewModel` --calls--> `combine()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/clock/ClockStyleGalleryViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/domain/FlowCombine.kt
- `CalendarRepositoryTest` --calls--> `CalendarRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/CalendarRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/CalendarRepository.kt
- `PermissionsViewModel` --calls--> `PermissionsUiState`  [EXTRACTED]
  app/src/main/kotlin/com/facetlauncher/app/ui/settings/PermissionsViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/ui/settings/PermissionsScreenState.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (196 total, 47 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.09
Nodes (5): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "NextAlarmRepositoryTest"
Cohesion: 0.08
Nodes (20): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+12 more)

### Community 4 - ".setContent"
Cohesion: 0.20
Nodes (9): FolderAppPickerScreenTest, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), PickerSectionHeader(), FolderAppPickerUiState (+1 more)

### Community 5 - "CalendarPermissionRepository"
Cohesion: 0.10
Nodes (12): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 6 - "HomeWallpaper"
Cohesion: 0.07
Nodes (16): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, HomeWallpaper, Tones, Unavailable, Bitmap, WallpaperRepository (+8 more)

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
Cohesion: 0.13
Nodes (6): StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 11 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow, DataStore, Preferences

### Community 12 - "NotificationBadgeRepository"
Cohesion: 0.11
Nodes (11): Callback, Callback, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, Callback (+3 more)

### Community 13 - "AppShortcut"
Cohesion: 0.24
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 14 - "FacetDockAppRepository"
Cohesion: 0.12
Nodes (7): FacetDockAppRepository, Flow, FacetDockAppEntity, toFacetDockAppEntity(), FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "DockAppRepository"
Cohesion: 0.09
Nodes (9): DockAppRepository, AppListLimits, CatalogEntry, CleanUpUninstalledAppsUseCase, CleanUpUninstalledAppsUseCaseTest, AppOpsManager, AppRepository, AppWidgetManager (+1 more)

### Community 18 - "FacetEntity"
Cohesion: 0.05
Nodes (7): FacetRepository, toCsv(), FacetEntity, FacetRepositoryTest, FakeFacetDao, Flow, FacetEntityTest

### Community 19 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 21 - "DockAppEntity"
Cohesion: 0.16
Nodes (5): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppDaoTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FacetDockFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): FacetDockFolderPlacementDao, FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FacetDockFolderPlacementDaoTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerGridSize"
Cohesion: 0.06
Nodes (13): AppDrawerSettingsScreenTest, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, SearchBarPosition, BOTTOM (+5 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier (+6 more)

### Community 30 - "Row"
Cohesion: 0.17
Nodes (75): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+67 more)

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
Cohesion: 0.16
Nodes (9): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest (+1 more)

### Community 35 - "FacetNavHost"
Cohesion: 0.19
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), Modifier (+5 more)

### Community 36 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 37 - "LauncherAppWidgetHost"
Cohesion: 0.24
Nodes (7): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost

### Community 38 - "WidgetProviderOption"
Cohesion: 0.25
Nodes (15): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+7 more)

### Community 39 - "OnboardingScreen"
Cohesion: 0.24
Nodes (11): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO, OnboardingSubScreen (+3 more)

### Community 40 - "FolderTestFakes.kt"
Cohesion: 0.15
Nodes (4): DefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementEntity, FakeDefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementDaoTest

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.18
Nodes (12): Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier (+4 more)

### Community 44 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 45 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 46 - "AppInfo"
Cohesion: 0.20
Nodes (5): DockAppEntity, Flow, AppInfo, DockFolderPlacementDao, PlacedItem

### Community 47 - "HomeScreen.kt"
Cohesion: 0.27
Nodes (15): HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview() (+7 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AccentSwatch"
Cohesion: 0.05
Nodes (31): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent() (+23 more)

### Community 51 - "AppearanceSettingsViewModel"
Cohesion: 0.14
Nodes (7): ThemeMode, DARK, LIGHT, SYSTEM, AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 52 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 53 - "CardDivider"
Cohesion: 0.21
Nodes (19): DrawerPresentation, GRID, LIST, ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard() (+11 more)

### Community 54 - "ContactRepositoryTest"
Cohesion: 0.05
Nodes (31): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+23 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.15
Nodes (28): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT, SettingsSearchEntry (+20 more)

### Community 57 - "PlaceWidgetUseCase"
Cohesion: 0.24
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (16): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+8 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.20
Nodes (9): ClockAdjustSheetTest, AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier (+1 more)

### Community 63 - "CalendarEvent"
Cohesion: 0.19
Nodes (15): CalendarEvent, CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, ClockBlockPreview() (+7 more)

### Community 65 - "HubGrid"
Cohesion: 0.18
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 66 - "dashedBorder"
Cohesion: 0.24
Nodes (10): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier (+2 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".createViewModel"
Cohesion: 0.23
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 70 - "LauncherViewModel.kt"
Cohesion: 0.21
Nodes (6): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 71 - ".setContent"
Cohesion: 0.06
Nodes (34): AppContextMenuTest, Folder, FolderTileContextMenuTest, QuickAddState, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow() (+26 more)

### Community 72 - "ManageFacetsScreen.kt"
Cohesion: 0.45
Nodes (10): AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier, PaddingValues, ManageFacetsContent(), ManageFacetsHeader(), ManageFacetsScreen() (+2 more)

### Community 73 - "combine"
Cohesion: 0.14
Nodes (14): combine(), Flow, FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase, Flow, T1 (+6 more)

### Community 74 - ".setContent"
Cohesion: 0.13
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 75 - "WallpaperAccentRole"
Cohesion: 0.13
Nodes (9): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY (+1 more)

### Community 76 - ".setContent"
Cohesion: 0.18
Nodes (5): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "DockAppPickerScreen.kt"
Cohesion: 0.50
Nodes (8): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader()

### Community 82 - "AddFolderToDockUseCase"
Cohesion: 0.47
Nodes (3): AddFolderToDockUseCase, AddFolderToDockUseCaseTest, Fixture

### Community 83 - ".repository"
Cohesion: 0.16
Nodes (8): DockAppRepositoryTest, FakeDockAppDao, DockAppEntity, Flow, FakeDockFolderPlacementDao, DockAppDao, DockFolderPlacementEntity, FolderDao

### Community 84 - "FavoritesPickerScreen.kt"
Cohesion: 0.61
Nodes (7): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - ".setContent"
Cohesion: 0.24
Nodes (5): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "AppRepository"
Cohesion: 0.08
Nodes (14): FavoritesPickerScreenTest, AppRepository, flattenIcon(), Bitmap, Flow, AppModule, Context, GetInstalledAppsUseCase (+6 more)

### Community 89 - "BackupMapping.kt"
Cohesion: 0.30
Nodes (9): BackupAppEntry, BackupFacet, BackupWidgetPlacement, T, toBackupEntry(), toBackupFacet(), toBackupPlacement(), toEnumOrDefault() (+1 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "ClockAccessoryIcons.kt"
Cohesion: 0.29
Nodes (11): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+3 more)

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - ".setContent"
Cohesion: 0.12
Nodes (12): KeyboardDismissalTest, ContactInfo, SystemSettingsRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, ObserveQuickAddStateUseCase, T, RankBySearchRelevanceUseCase (+4 more)

### Community 95 - ".homeViewModel"
Cohesion: 0.20
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 96 - "ClockColorOption"
Cohesion: 0.03
Nodes (41): Flow, Converters, T, resolveOverride(), ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME (+33 more)

### Community 99 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (4): AlphabetRailTest, ClockBlockTest, ClockBlock(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview() (+4 more)

### Community 103 - "FolderEntity"
Cohesion: 0.28
Nodes (3): FolderAppEntity, FolderEntity, FolderDaoTest

### Community 104 - "DragReorderState"
Cohesion: 0.25
Nodes (5): DragReorderState, Modifier, T, detectGrabOrResizeGesture(), State

### Community 105 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 106 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 109 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.19
Nodes (4): LauncherSettings, HomeUiState, FacetCarouselUiStateTest, HomeUiStateTest

### Community 111 - "DockFolderPlacementEntity"
Cohesion: 0.18
Nodes (3): DockFolderPlacementDao, DockFolderPlacementEntity, DockFolderPlacementDaoTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.14
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 114 - "FolderDetailScreen.kt"
Cohesion: 0.13
Nodes (11): Modifier, RenameDialog(), ReorderRowDefaults, FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview() (+3 more)

### Community 116 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 118 - ".useCase"
Cohesion: 0.06
Nodes (9): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, ExportBackupUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest (+1 more)

### Community 119 - "FontWeightOption"
Cohesion: 0.13
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 120 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 121 - "FolderRepository"
Cohesion: 0.12
Nodes (3): FolderRepository, FolderRepositoryTest, FakeFolderDao

### Community 123 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "FacetCarouselViewModel"
Cohesion: 0.33
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 132 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.16
Nodes (9): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+1 more)

### Community 134 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 137 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 138 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 139 - ".setContent"
Cohesion: 0.07
Nodes (9): SettingsScreenTest, DefaultLauncherRepository, Intent, Intent, StateFlow, ViewModel, SettingsViewModel, DefaultLauncherRepositoryTest (+1 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.16
Nodes (10): BackupRestoreEvent, BackupRestoreUiState, LaunchBindPermission, LaunchConfigure, PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow (+2 more)

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.19
Nodes (5): DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "Color.kt"
Cohesion: 0.39
Nodes (7): Color, resolve(), accentTonalExtremes(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme

### Community 146 - "LauncherActivity.kt"
Cohesion: 0.36
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 149 - "RemoveAppFromDockUseCase"
Cohesion: 0.47
Nodes (3): RemoveAppFromDockUseCase, Fixture, RemoveAppFromDockUseCaseTest

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 156 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 161 - "RemoveFolderFromDockUseCase"
Cohesion: 0.47
Nodes (3): RemoveFolderFromDockUseCase, Fixture, RemoveFolderFromDockUseCaseTest

### Community 162 - "SetDefaultLauncherSheet"
Cohesion: 0.73
Nodes (5): AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 163 - "FacetDatabase"
Cohesion: 0.27
Nodes (4): DatabaseModule, Context, FacetDatabase, RoomDatabase

### Community 164 - "BackupRestoreMessage"
Cohesion: 0.33
Nodes (6): BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded

### Community 165 - ".setContent"
Cohesion: 0.10
Nodes (20): FacetSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), FacetSettingsHeader(), FacetSettingsRow(), Composable (+12 more)

### Community 166 - "build_screenshot.py"
Cohesion: 0.47
Nodes (5): ImageDraw, build(), draw_tracked_text(), Refresh one Play Store marketing screenshot after a raw device capture changes.…, rounded_mask()

### Community 167 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 168 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 174 - "AppInfo"
Cohesion: 0.08
Nodes (13): DefaultFavoriteAppRepository, Flow, AppInfo, ListContentMode, FAVORITES, MOST_USED, RECENTS, Flow (+5 more)

### Community 181 - ".setContent"
Cohesion: 0.19
Nodes (6): DockAppPickerScreenTest, DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 182 - "Folder"
Cohesion: 0.12
Nodes (9): FolderDetailScreenTest, FoldersSettingsScreenTest, Folder, FolderRow(), FoldersSettingsContent(), FoldersSettingsScreen(), FoldersSettingsScreenPreview(), FoldersSettingsUiState (+1 more)

### Community 187 - "HomeViewModel"
Cohesion: 0.18
Nodes (4): HomeViewModel, Intent, StateFlow, ViewModel

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 191 - "OnboardingFacetsPage.kt"
Cohesion: 0.54
Nodes (7): FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview(), OnboardingUiState

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 199 - "AppIcon"
Cohesion: 0.31
Nodes (12): Image, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+4 more)

### Community 201 - "AddFolderToFavoritesUseCase"
Cohesion: 0.47
Nodes (3): AddFolderToFavoritesUseCase, AddFolderToFavoritesUseCaseTest, Fixture

### Community 203 - "RemoveAppFromFavoritesUseCase"
Cohesion: 0.47
Nodes (3): RemoveAppFromFavoritesUseCase, Fixture, RemoveAppFromFavoritesUseCaseTest

### Community 205 - "RemoveFolderFromFavoritesUseCase"
Cohesion: 0.47
Nodes (3): RemoveFolderFromFavoritesUseCase, Fixture, RemoveFolderFromFavoritesUseCaseTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): rememberDragReorderState(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+2 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

## Knowledge Gaps
- **369 isolated node(s):** `AddFailed`, `LaunchBindPermission`, `LaunchConfigure`, `WidgetAdded`, `ExportFailed` (+364 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 736 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `.setContent`, `.setContent`, `WidgetPlacementEntity`, `HomeWallpaper`, `CalendarPermissionRepository`, `.setContent`, `LauncherFontOption`, `.setContent`, `LauncherActivity.kt`, `.setContent`, `GestureHintOverlay`, `DrawerGridSize`, `FacetCarouselScreen.kt`, `WidgetResizeHandle.kt`, `Row`, `.setContent`, `SetDefaultLauncherSheet`, `FacetNavHost`, `.setContent`, `WidgetProviderOption`, `FontWeightSlider`, `.setContent`, `BackupRestoreContent`, `HomeScreen.kt`, `AccentSwatch`, `AppearanceSettingsViewModel`, `AppearanceSettingsScreen.kt`, `.setContent`, `ContactRepositoryTest`, `Folder`, `AppDrawerScreen.kt`, `PlaceWidgetUseCase`, `CardDivider`, `HomeScreen`, `ClockAdjustSheet.kt`, `LabeledDropdownRow`, `SettingsScreen.kt`, `CalendarEvent`, `OnboardingFacetsPage.kt`, `ColorTest`, `dashedBorder`, `.setContent`, `.setContent`, `ManageFacetsScreen.kt`, `.setContent`, `WallpaperAccentRole`, `.setContent`, `DockSettingsScreen.kt`, `.setContent`, `DockAppPickerScreen.kt`, `AppDrawerSettingsScreen.kt`, `FavoritesPickerScreen.kt`, `.setContent`, `AppRepository`, `.setContent`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `SetDefaultLauncherSheetTest`, `AppDrawerScreen`, `TonalButton`, `HomeAppsListSettingsScreen.kt`, `BackButton`, `FolderDetailScreen.kt`, `FontWeightOption`, `Type.kt`?**
  _High betweenness centrality (0.198) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `HomeWallpaper`, `FavoritesPickerViewModel`, `.setContent`, `AppShortcut`, `FacetDockAppRepository`, `DefaultFavoriteAppEntity`, `DockAppRepository`, `LauncherActivity.kt`, `OnboardingViewModelTest`, `FacetCarouselScreen.kt`, `FavoriteAppRepository`, `FacetNavHost`, `.setContent`, `.setContent`, `AddAppToFavoritesUseCaseTest`, `HomeDrawerRoute`, `HomeScreen.kt`, `AppearanceSettingsViewModel`, `AppearanceSettingsScreen.kt`, `.setContent`, `ContactRepositoryTest`, `CardDivider`, `AppDrawerScreen.kt`, `HomeScreen`, `SettingsScreen.kt`, `Fixture`, `CalendarEvent`, `.createViewModel`, `LauncherViewModel.kt`, `.setContent`, `combine`, `.setContent`, `DockSettingsScreen.kt`, `DockAppPickerScreen.kt`, `FavoritesPickerScreen.kt`, `.setContent`, `AppRepository`, `Fixture`, `.setContent`, `.homeViewModel`, `ClockColorOption`, `AppDrawerScreen`, `SettingsRepository`, `HomeAppsListSettingsScreen.kt`, `Fixture`, `.useCase`, `AddAppToDockUseCaseTest`?**
  _High betweenness centrality (0.114) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `CalendarPermissionRepository`, `HomeWallpaper`, `LauncherFontOption`, `ManageFacetsViewModel`, `.setContent`, `SettingsRepositoryTest.kt`, `FacetDockAppRepository`, `DockAppRepository`, `SettingsRepositoryTest`, `ImportBackupUseCase.kt`, `.setContent`, `DrawerGridSize`, `ClockTemplateId`, `.setContent`, `FavoriteAppRepository`, `ExportBackupUseCase.kt`, `.setContent`, `.setContent`, `.createViewModel`, `AppInfo`, `AccentSwatch`, `AppearanceSettingsViewModel`, `CardDivider`, `HomeViewModel`, `.drawerViewModel`, `.createViewModel`, `.setContent`, `LauncherViewModel.kt`, `combine`, `.setContent`, `WallpaperAccentRole`, `.setContent`, `.setContent`, `.setContent`, `AppRepository`, `.setContent`, `.homeViewModel`, `ClockColorOption`, `LauncherSettings`, `.useCase`, `FontWeightOption`, `NotificationAccessExplanationViewModel.kt`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Are the 13 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `SettingsRepository` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`SettingsRepository` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 133 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 133 INFERRED edges - model-reasoned connections that need verification._