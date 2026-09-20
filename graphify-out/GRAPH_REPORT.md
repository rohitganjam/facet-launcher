# Graph Report - lumen-launcher  (2026-09-20)

## Corpus Check
- 457 files · ~903,535 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 4494 nodes · 12137 edges · 219 communities (157 shown, 54 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 1014 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b893211d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DefaultFavoriteAppRepository
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- FolderEntity
- AppRepository
- FacetEntity
- FacetCarouselScreen.kt
- Android launcher design planning/support.js
- AppRepository.kt
- Screens
- ManageFacetsViewModel
- FacetRepository
- DockAppRepository
- 05 — Architect's Review: Findings & Recommendations
- ClockColorOption
- UserHandle
- Screens
- CalendarPermissionRepository
- NextAlarmRepository.kt
- timeInWords
- .drawerViewModel
- .setContent
- DrawerViewModel
- 4. Feature Requirements
- HomeViewModel
- AppDrawerSettingsViewModelTest
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- FacetDao
- .setContent
- Row
- WorkProfileRepository
- FavoriteAppRepository
- AppShortcutRepository
- ClockStyleGalleryViewModel
- FacetNavHost
- HubViewModel
- AccentSwatch
- AppModule
- AppWidgetRepository
- PrivateSpaceRepository
- ContactRepositoryTest
- Manrope Font License (SIL OFL 1.1)
- .setContent
- .setContent
- homeAppLabelShadow
- .setContent
- .repository
- Clock Widget Resize — Implementation Spec
- .setContent
- BackupRestoreViewModelTest
- BackupMapping.kt
- Folder
- CardDivider
- .createViewModel
- 4. Feature Requirements
- AppDrawerScreen.kt
- .setContent
- FolderDetailScreen.kt
- HomeScreen
- AppearanceSettingsScreen.kt
- CalendarInfo
- FacetSettingsContent
- CalendarEventsBlock.kt
- AppContextMenu
- TonalButton
- GroupAppsByLetterUseCaseTest
- github.md
- OnboardingViewModel
- .setContent
- UsageStatsRepository
- .setContent
- FolderAppPickerViewModelTest
- ComposeHardcodedTextRule
- HubAddWidgetEvent
- .createViewModel
- AppPickerScreen.kt
- CompactWidgetsUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Rule
- FacetCarouselUiState
- DockDisplayMode
- FacetDaoTest
- HomeDrawerRoute
- BackupRestoreViewModel
- Lumen Launcher Implementation Plan
- facetTypography
- Play Console — sensitive permission disclosures
- AppProfile
- HubGrid
- .setContent
- Facet Launcher — Built Capabilities
- FolderDetailViewModel
- Play Console — store listing text
- LongPressReleaseGesture.kt
- ExportBackupUseCase.kt
- OnboardingViewModelTest
- .setContent
- .setContent
- FacetLauncherTheme
- ClockBlock
- 02 — Database & Persistence Architecture
- OnboardingFacetsPage.kt
- FacetDockAppDaoTest
- Instrumented (Compose UI, emulator) — 37 classes
- FontSizeSlider
- .setContent
- LauncherAppWidgetHost
- Unit (JVM) — 91 classes
- HomeScreenTest.kt
- FacetScopeBadge.kt
- .setContent
- HubWidgetPickerViewModel
- SettingsRepository
- .invoke
- FacetDatabaseMigrationTest
- ResolveWidgetDropUseCaseTest
- FolderDao
- PlaceWidgetUseCaseTest
- HomeScreen.kt
- R
- HomeAppsListSettingsScreen.kt
- ManageFacetsScreen.kt
- gradlew
- ClockTemplateId
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ClockAdjustSheet.kt
- .setContent
- WidgetPlacementEntity
- AppInfo
- Facet Launcher — Onboarding Flow
- AppIcon
- NotificationBadgeRepository
- DockSettingsScreen.kt
- .setContent
- HubContent
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- .`a valid backup replaces every facet, dock, and default favorite, then reports what was restored`
- SettingsRepositoryTest
- PlacedItem
- FakeFavoriteAppDao
- ClockAdjustMode
- UserHandle
- .setContent
- build_screenshot.py
- BackupRestoreScreen.kt
- 2. Design tokens
- FolderRepository
- FacetDockAppEntity
- architecture/README.md
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- WidgetResizeHandle.kt
- CoroutineScope
- .setContent
- PermissionsScreen.kt
- androidx
- Intent
- AppWidgetHostView
- Context
- ClockCornerHandle
- .setContent
- HomeDrawerRouteTest.kt
- ResolveWidgetResizeUseCaseTest
- 11 — Flow: Profiles & Spaces (Work Profile, Private Space, Secure Folder)
- Facet Launcher 0.1.10
- ContactRepository
- FacetDatabase
- .setContent
- NotificationAccessExplanationViewModel.kt
- 5.4 Key registry by section
- FolderContentsSheet
- LabeledDropdownRow
- HubWidgetPickerScreen.kt
- NotificationBadgeStyle
- 06 — Testing Strategy & Practice
- pre-commit
- FacetCarouselViewModel.kt
- SettingsScreen.kt
- FavoritesPickerViewModelTest
- SortAppsForPickerUseCaseTest
- DragReorderState
- ColorTest
- 01 — High-Level Architecture & Layer Boundaries
- AlphabetRail.kt
- gen-test-registry.py
- release.sh
- gen-release-notes.py
- 03 — Core Reactive & Data Flow
- 07 — Repository & Use Case Registries
- 08 — Flow: Placements (Favorites, Dock, Folders) — add, remove, reorder, clean up
- 13 — Flows: Facets, Theme resolution, Notification badges, Onboarding
- CalendarRepositoryTest
- 09 — Flow: Backup & Restore
- 10 — Flow: Hub Widgets (host lifecycle, add, move, resize, delete, orphans)
- SelectPreviewAppsUseCaseTest
- 12 — Flow: App Drawer — search, tabs, and per-app actions
- StickyHeaderLayout
- DockAppPickerViewModelTest
- ClockAccessoryIconsTest
- .setContent
- .setContent
- BackupRestoreMessage
- ClockZoneHandle.kt
- ClockAlignment.kt
- ConvertersTest
- WeatherInfo.kt
- Facet Launcher 0.1.10

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 267 edges
2. `AppInfo` - 217 edges
3. `FacetEntity` - 212 edges
4. `SettingsRepository` - 162 edges
5. `Row` - 147 edges
6. `FacetRepository` - 134 edges
7. `LauncherSettings` - 124 edges
8. `AppRepository` - 90 edges
9. `FolderRepository` - 85 edges
10. `Folder` - 83 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `AppDrawerScreen()` --calls--> `GroupAppsByLetterUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/GroupAppsByLetterUseCase.kt
- `AppDrawerScreen()` --calls--> `RankBySearchRelevanceUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/RankBySearchRelevanceUseCase.kt
- `AppDrawerScreen()` --calls--> `ContactConnectionsSheet()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/drawer/ContactConnectionsSheet.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (219 total, 54 thin omitted)

### Community 0 - "DefaultFavoriteAppRepository"
Cohesion: 0.07
Nodes (8): DefaultFavoriteAppRepository, Flow, DefaultFavoriteFolderPlacementDao, Flow, DefaultFavoriteFolderPlacementEntity, DefaultFavoriteFolderPlacementDaoTest, CleanUpUninstalledAppsUseCaseTest, Fixture

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.17
Nodes (9): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus, BatteryRepositoryTest (+1 more)

### Community 3 - "FolderEntity"
Cohesion: 0.12
Nodes (5): FolderEntity, DockFolderPlacementDaoTest, FacetDockFolderPlacementDaoTest, FavoriteFolderPlacementDaoTest, FolderDaoTest

### Community 4 - "AppRepository"
Cohesion: 0.17
Nodes (10): AppRepository, any(), AppRepositoryTest, eq(), Context, LauncherApps, T, UserHandle (+2 more)

### Community 5 - "FacetEntity"
Cohesion: 0.06
Nodes (24): FacetEntity, LauncherSettings, HomeUiState, FacetEntityTest, AddAppToDockUseCaseTest, Fixture, AddAppToFavoritesUseCaseTest, Fixture (+16 more)

### Community 6 - "FacetCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier (+6 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "AppRepository.kt"
Cohesion: 0.18
Nodes (10): BroadcastReceiver, BroadcastReceiver, flattenIcon(), Bitmap, BroadcastReceiver, Context, Flow, Intent (+2 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "ManageFacetsViewModel"
Cohesion: 0.13
Nodes (6): StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 11 - "FacetRepository"
Cohesion: 0.07
Nodes (4): FacetRepository, toCsv(), FacetRepositoryTest, FakeFacetDao

### Community 12 - "DockAppRepository"
Cohesion: 0.06
Nodes (13): DockAppRepository, Flow, DockAppDao, Flow, DockAppEntity, DockFolderPlacementDao, Flow, DockFolderPlacementEntity (+5 more)

### Community 13 - "05 — Architect's Review: Findings & Recommendations"
Cohesion: 0.15
Nodes (13): 05 — Architect's Review: Findings & Recommendations, F10 — One-shot reads on Home (Low, UX), F11 — Backup misses the newer clock/app-list fields (Medium), F12 — Two small convention drifts (Low), F13 — Backup restore leaves placements invisible until restart (High), F2 — Composables instantiating use cases (Medium), F3 — UI reading a repository constant (Low), F4 — `domain/` touching the Android framework (Low–Medium) (+5 more)

### Community 14 - "ClockColorOption"
Cohesion: 0.03
Nodes (52): Flow, Converters, T, resolveOverride(), AppListLimits, ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY (+44 more)

### Community 15 - "UserHandle"
Cohesion: 0.18
Nodes (4): Callback, Callback, UserHandle, Callback

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarPermissionRepository"
Cohesion: 0.13
Nodes (5): FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, Fixture, ObserveHomeScreenStateUseCaseTest

### Community 18 - "NextAlarmRepository.kt"
Cohesion: 0.36
Nodes (5): BroadcastReceiver, Context, Flow, Intent, BroadcastReceiver

### Community 19 - "timeInWords"
Cohesion: 0.12
Nodes (12): numberWordsDe(), numberWordsEn(), numberWordsEs(), numberWordsFr(), numberWordsPt(), timeInWords(), timeInWordsDe(), timeInWordsEn() (+4 more)

### Community 20 - ".drawerViewModel"
Cohesion: 0.09
Nodes (5): SettingsSearchEntry, CatalogEntry, SystemSettingsRepositoryTest, DrawerViewModelTest, Intent

### Community 22 - "DrawerViewModel"
Cohesion: 0.14
Nodes (3): DrawerViewModel, StateFlow, ViewModel

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "HomeViewModel"
Cohesion: 0.12
Nodes (5): ObserveQuickAddStateUseCase, HomeViewModel, Intent, StateFlow, ViewModel

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "FacetDao"
Cohesion: 0.11
Nodes (6): FacetDao, Flow, EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 29 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 30 - "Row"
Cohesion: 0.17
Nodes (75): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+67 more)

### Community 31 - "WorkProfileRepository"
Cohesion: 0.33
Nodes (6): BroadcastReceiver, Context, Flow, Intent, WorkProfileRepository, BroadcastReceiver

### Community 32 - "FavoriteAppRepository"
Cohesion: 0.08
Nodes (7): FavoriteAppRepository, com, Flow, FavoriteAppEntity, FavoriteFolderPlacementEntity, FavoriteAppDaoTest, ExportBackupUseCaseTest

### Community 33 - "AppShortcutRepository"
Cohesion: 0.16
Nodes (5): AppShortcutRepository, UserHandle, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 34 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 35 - "FacetNavHost"
Cohesion: 0.15
Nodes (15): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+7 more)

### Community 36 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 37 - "AccentSwatch"
Cohesion: 0.13
Nodes (21): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState (+13 more)

### Community 38 - "AppModule"
Cohesion: 0.13
Nodes (7): AppModule, Context, CoroutineScope, LauncherApps, UserManager, WallpaperRepositoryTest, WallpaperManager

### Community 39 - "AppWidgetRepository"
Cohesion: 0.06
Nodes (24): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+16 more)

### Community 40 - "PrivateSpaceRepository"
Cohesion: 0.10
Nodes (17): BroadcastReceiver, Context, Flow, Intent, Locked, NotConfigured, PrivateSpaceRepository, BroadcastReceiver (+9 more)

### Community 44 - ".setContent"
Cohesion: 0.08
Nodes (19): FavoritesPickerScreenTest, FavoritesPickerScreen(), Modifier, FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel (+11 more)

### Community 45 - "homeAppLabelShadow"
Cohesion: 0.20
Nodes (14): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+6 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 51 - "BackupMapping.kt"
Cohesion: 0.19
Nodes (19): BackupAppEntry, BackupFacet, BackupFolder, BackupFolderPlacement, BackupWidgetPlacement, com, T, toBackupEntry() (+11 more)

### Community 52 - "Folder"
Cohesion: 0.11
Nodes (3): FolderDetailScreenTest, Folder, ObserveQuickAddStateUseCaseTest

### Community 53 - "CardDivider"
Cohesion: 0.19
Nodes (23): ConfirmDialog(), Modifier, rememberDragReorderState(), CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow() (+15 more)

### Community 54 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.12
Nodes (45): androidx, fakeUserHandle(), ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerFolderRow() (+37 more)

### Community 58 - "FolderDetailScreen.kt"
Cohesion: 0.38
Nodes (9): Modifier, RenameDialog(), FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview(), Modifier (+1 more)

### Community 60 - "AppearanceSettingsScreen.kt"
Cohesion: 0.41
Nodes (12): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview(), Color (+4 more)

### Community 61 - "CalendarInfo"
Cohesion: 0.06
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel (+2 more)

### Community 62 - "FacetSettingsContent"
Cohesion: 0.22
Nodes (15): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier (+7 more)

### Community 63 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 64 - "AppContextMenu"
Cohesion: 0.24
Nodes (13): Add, QuickPlacementAction, Remove, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow(), Composable (+5 more)

### Community 65 - "TonalButton"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 66 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.22
Nodes (4): AppEntry, DrawerItem, FolderEntry, GroupAppsByLetterUseCaseTest

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "OnboardingViewModel"
Cohesion: 0.22
Nodes (3): StateFlow, ViewModel, OnboardingViewModel

### Community 71 - ".setContent"
Cohesion: 0.14
Nodes (3): AppContextMenuTest, SharedFlow, QuickAddState

### Community 73 - "ComposeHardcodedTextRule"
Cohesion: 0.26
Nodes (7): ComposeHardcodedTextRule, FacetRuleSetProvider, KtCallExpression, KtStringTemplateExpression, RuleSet, RuleSetId, RuleSetProvider

### Community 74 - "HubAddWidgetEvent"
Cohesion: 0.22
Nodes (8): AddFailed, AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 76 - "AppPickerScreen.kt"
Cohesion: 0.33
Nodes (13): AppPickerAppsList(), AppPickerFolderRow(), AppPickerFoldersList(), AppPickerHeader(), AppPickerRow(), AppPickerScreen(), AppPickerScreenPreview(), AppPickerSectionHeader() (+5 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "Rule"
Cohesion: 0.12
Nodes (15): PermissionsScreenGrantedTest, AppSortOption, ALPHABETICAL, INSTALL_DATE, LAST_UPDATED, LAST_USED, SortDirection, ASCENDING (+7 more)

### Community 81 - "DockDisplayMode"
Cohesion: 0.07
Nodes (21): DockSettingsScreenTest, HomeWallpaper, Tones, Unavailable, DockDisplayMode, ICONS, TEXT, Bitmap (+13 more)

### Community 83 - "HomeDrawerRoute"
Cohesion: 0.06
Nodes (32): SetDefaultLauncherSheetTest, GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), AlarmClockIcon(), BatteryIcon() (+24 more)

### Community 84 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): BackupRestoreUiState, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "facetTypography"
Cohesion: 0.26
Nodes (7): FacetType, facetTypography(), FontFamily, FontWeight, resolve(), scaledBy(), TypeTest

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "AppProfile"
Cohesion: 0.13
Nodes (10): AppProfile, OTHER, PERSONAL, PRIVATE, WORK, RepairOrphanedProfileRowsUseCase, any(), Fixture (+2 more)

### Community 89 - "HubGrid"
Cohesion: 0.11
Nodes (26): AppSortControl(), Modifier, Color, Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+18 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "FolderDetailViewModel"
Cohesion: 0.22
Nodes (4): FolderDetailViewModel, StateFlow, ViewModel, FolderDetailViewModelTest

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "LongPressReleaseGesture.kt"
Cohesion: 0.50
Nodes (7): detectLongPressReleaseGesture(), emitInteraction(), Modifier, longPressReleaseClickable(), CoroutineScope, MutableInteractionSource, PressInteraction

### Community 95 - "ExportBackupUseCase.kt"
Cohesion: 0.21
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 97 - ".setContent"
Cohesion: 0.05
Nodes (9): AppearanceSettingsScreenTest, HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, DefaultAppRepositoryTest, SeedDefaultDockUseCaseTest, AppearanceSettingsViewModelTest, WallpaperRepository (+1 more)

### Community 98 - ".setContent"
Cohesion: 0.12
Nodes (8): DockAppPickerScreenTest, DockAppPickerScreen(), Modifier, DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 99 - "FacetLauncherTheme"
Cohesion: 0.11
Nodes (6): AlphabetRailTest, AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview(), FacetLauncherTheme(), WorkProfileInfo

### Community 100 - "ClockBlock"
Cohesion: 0.10
Nodes (6): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale()

### Community 101 - "02 — Database & Persistence Architecture"
Cohesion: 0.12
Nodes (17): 02 — Database & Persistence Architecture, 0. Storage map — everything Facet persists, and where, 1. Entity-Relationship diagram, 2. DAO → Repository → Flow mapping, 3. Type converters, 4. Migration policy (as configured), 5.1 Key diagram — every key, and what it overrides in `facets`, 5.2 Read path (+9 more)

### Community 102 - "OnboardingFacetsPage.kt"
Cohesion: 0.35
Nodes (9): Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview() (+1 more)

### Community 104 - "Instrumented (Compose UI, emulator) — 37 classes"
Cohesion: 0.12
Nodes (17): `data/local/`, Instrumented (Compose UI, emulator) — 37 classes, Main-source classes with no mirrored test class, Test Case Registry, `ui/components/`, `ui/dock/`, `ui/drawer/`, `ui/facets/` (+9 more)

### Community 105 - "FontSizeSlider"
Cohesion: 0.83
Nodes (3): FontSizeSlider(), FontSizeSliderAllStopsPreview(), Modifier

### Community 106 - ".setContent"
Cohesion: 0.15
Nodes (7): FolderAppPickerScreenTest, FolderAppPickerScreen(), Modifier, FolderAppPickerUiState, FolderAppPickerViewModel, StateFlow, ViewModel

### Community 107 - "LauncherAppWidgetHost"
Cohesion: 0.22
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 108 - "Unit (JVM) — 91 classes"
Cohesion: 0.12
Nodes (17): `data/`, `data/local/`, `data/widget/`, `domain/`, `ui/dock/`, `ui/drawer/`, `ui/facets/`, `ui/home/` (+9 more)

### Community 109 - "HomeScreenTest.kt"
Cohesion: 0.33
Nodes (5): AppInfo, AppListVerticalAlignment, ClockAlignment, CalendarEvent, ListContentMode

### Community 110 - "FacetScopeBadge.kt"
Cohesion: 0.67
Nodes (6): FacetScopeBadge(), Color, Modifier, OtherScopeBadge(), ScopeBadge(), WorkScopeBadge()

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 113 - "SettingsRepository"
Cohesion: 0.03
Nodes (59): DataStoreModule, Context, FontScaleOption, DEFAULT, EXTRA_LARGE, HUGE, LARGE, SMALL (+51 more)

### Community 114 - ".invoke"
Cohesion: 0.40
Nodes (4): GroupAppsByLetterUseCase, GroupedItems, T, GroupedApps

### Community 117 - "FolderDao"
Cohesion: 0.07
Nodes (7): FolderDao, FolderWithApps, Flow, FakeDefaultFavoriteFolderPlacementDao, FakeFacetDockFolderPlacementDao, FakeFavoriteFolderPlacementDao, Flow

### Community 119 - "HomeScreen.kt"
Cohesion: 0.17
Nodes (31): AppRow(), dashedBorder(), DockIcon(), FolderDockIcon(), FolderRow(), FolderTileGlyph(), FolderTileGlyphSlot(), HomeScreenTextOnlyPresentationPreview() (+23 more)

### Community 120 - "R"
Cohesion: 0.17
Nodes (11): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+3 more)

### Community 122 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+2 more)

### Community 123 - "ManageFacetsScreen.kt"
Cohesion: 0.45
Nodes (10): AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier, PaddingValues, ManageFacetsContent(), ManageFacetsHeader(), ManageFacetsScreen() (+2 more)

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 125 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 131 - "ClockAdjustSheet.kt"
Cohesion: 0.67
Nodes (5): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Composable, Modifier

### Community 132 - ".setContent"
Cohesion: 0.13
Nodes (12): BackupRestoreScreenTest, ExportBackupUseCase, ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion (+4 more)

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.07
Nodes (17): WidgetPlacementEntity, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow (+9 more)

### Community 134 - "AppInfo"
Cohesion: 0.09
Nodes (12): AppInfo, WorkProfileInfo, GetInstalledAppsUseCase, Flow, SelectPreviewAppsUseCase, SharedFlow, StateFlow, ViewModel (+4 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "AppIcon"
Cohesion: 0.23
Nodes (17): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), AppIconSize, badgeLabel(), Dp, Modifier, NotificationBadge() (+9 more)

### Community 137 - "NotificationBadgeRepository"
Cohesion: 0.19
Nodes (7): FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, NotificationListenerService, StatusBarNotification

### Community 138 - "DockSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DockAppsRow(), DockClickableRow(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier, reorderKey() (+1 more)

### Community 139 - ".setContent"
Cohesion: 0.06
Nodes (12): SettingsScreenTest, DefaultLauncherRepository, Intent, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, Intent, StateFlow (+4 more)

### Community 140 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.10
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 146 - "PlacedItem"
Cohesion: 0.06
Nodes (22): FacetDockAppRepository, com, Flow, FacetDockFolderPlacementEntity, FolderItem, PlacedItem, SingleApp, combine() (+14 more)

### Community 147 - "FakeFavoriteAppDao"
Cohesion: 0.12
Nodes (5): FavoriteAppDao, Flow, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 148 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 151 - "build_screenshot.py"
Cohesion: 0.38
Nodes (6): ImageDraw, build(), draw_tracked_text(), Path, Refresh one Play Store marketing screenshot after a raw device capture changes.…, rounded_mask()

### Community 152 - "BackupRestoreScreen.kt"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "FolderRepository"
Cohesion: 0.09
Nodes (5): FolderRepository, Flow, FolderAppEntity, FolderRepositoryTest, FakeFolderDao

### Community 155 - "FacetDockAppEntity"
Cohesion: 0.09
Nodes (5): FacetDockAppDao, Flow, FacetDockAppEntity, FakeFacetDockAppDao, Flow

### Community 156 - "architecture/README.md"
Cohesion: 0.15
Nodes (7): 04 — Directory & Package Structure Map, Non-code directories, Placement rules (derived from the code), Test tree mirror, Facet Launcher — Living Blueprint, Keeping this blueprint alive, Stack at a glance

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 161 - ".setContent"
Cohesion: 0.20
Nodes (5): FoldersSettingsScreenTest, FoldersSettingsUiState, FoldersSettingsViewModel, StateFlow, ViewModel

### Community 162 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

### Community 169 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (26): any(), T, KeyboardDismissalTest, FakeNotificationAccessRepository, BatteryRepository, ContactPermissionRepository, NextAlarmRepository, NotificationAccessRepository (+18 more)

### Community 171 - "11 — Flow: Profiles & Spaces (Work Profile, Private Space, Secure Folder)"
Cohesion: 0.33
Nodes (6): 11 — Flow: Profiles & Spaces (Work Profile, Private Space, Secure Folder), 1. Classification — one function, reused everywhere, 2. Work Profile, 3. Private Space (Android 15+), 4. Samsung Secure Folder, 5. Tear-down and repair (cross-reference)

### Community 172 - "Facet Launcher 0.1.10"
Cohesion: 0.33
Nodes (5): Facet Launcher 0.1.10, Fixes, Improvements, Internal, New Features

### Community 173 - "ContactRepository"
Cohesion: 0.08
Nodes (35): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+27 more)

### Community 174 - "FacetDatabase"
Cohesion: 0.06
Nodes (12): DatabaseModule, Context, FacetDatabase, FacetDockFolderPlacementDao, Flow, FavoriteFolderPlacementDao, Flow, Migrations (+4 more)

### Community 176 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 177 - "5.4 Key registry by section"
Cohesion: 0.17
Nodes (12): 5.4 Key registry by section, App drawer, Calendar selection (global; overridden when `facets.overrideCalendar`), Clock + calendar design (global; overridden per facet when `facets.overrideClock`), Dock (global; overridden when `facets.overrideDock`), Facets, First-run & coach marks, Home app list (global; overridden when `facets.overrideApps` / `overridingFavorites`) (+4 more)

### Community 178 - "FolderContentsSheet"
Cohesion: 0.40
Nodes (10): AddHere, FolderContentsAppRow(), FolderContentsEmptyState(), FolderContentsGrid(), FolderContentsGridTile(), FolderContentsList(), FolderContentsSheet(), FolderSheetHeaderAction (+2 more)

### Community 179 - "LabeledDropdownRow"
Cohesion: 0.32
Nodes (11): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+3 more)

### Community 182 - "HubWidgetPickerScreen.kt"
Cohesion: 0.28
Nodes (12): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+4 more)

### Community 183 - "NotificationBadgeStyle"
Cohesion: 0.14
Nodes (12): NotificationSettingsScreenTest, NotificationBadgeStyle, COUNT, DOT, Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+4 more)

### Community 185 - "06 — Testing Strategy & Practice"
Cohesion: 0.18
Nodes (11): 06 — Testing Strategy & Practice, 1. The two tiers, 2. What each layer's tests look like, 3. Conventions (enforced by review), 4. Running, 5. Known coverage gaps (from the registry's last section), Migrations (`androidTest/data/local/FacetDatabaseMigrationTest`), Repositories (`data/*RepositoryTest`) (+3 more)

### Community 187 - "FacetCarouselViewModel.kt"
Cohesion: 0.21
Nodes (7): FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase, FacetCarouselViewModel, StateFlow, ViewModel

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.26
Nodes (15): appDrawerSummary(), appsListSummary(), ClickableRow(), dockSummary(), folderCountSummary(), Composable, Modifier, NavigationChevron() (+7 more)

### Community 192 - "DragReorderState"
Cohesion: 0.26
Nodes (6): DragReorderState, Modifier, T, itemKey(), detectGrabOrResizeGesture(), detectHomeSwipeGestures()

### Community 194 - "01 — High-Level Architecture & Layer Boundaries"
Cohesion: 0.29
Nodes (7): 01 — High-Level Architecture & Layer Boundaries, 1. The layers, 2. Unidirectional data flow, concretely, 3. Hilt: components, scopes, and modules, 4. Composition root and app lifetime, Scope table, What each layer is allowed to know

### Community 195 - "AlphabetRail.kt"
Cohesion: 0.12
Nodes (16): AlphabetRail(), AlphabetRailFontScalePreview(), AlphabetRailPreviewSurface(), Folders, Modifier, Letter, letterAt(), magnifyScale() (+8 more)

### Community 196 - "gen-test-registry.py"
Cohesion: 0.38
Nodes (6): main_subject(), Path, Regenerates docs/architecture/TEST_REGISTRY.md from the test source sets. Run…, Best-effort path of the class under test, mirrored from the test's package., render(), scan()

### Community 199 - "gen-release-notes.py"
Cohesion: 0.60
Nodes (4): build_notes(), main(), Generates RELEASE_NOTES.md by asking Claude Code to categorize commits since…, run_git()

### Community 200 - "03 — Core Reactive & Data Flow"
Cohesion: 0.33
Nodes (6): 03 — Core Reactive & Data Flow, 1. Installed apps: `LauncherApps` → `Flow<List<AppInfo>>`, 2. Hydrating Room placements against the live list, 3. The Home screen state graph, 4. Process startup, 5. Other reactive sources worth knowing

### Community 201 - "07 — Repository & Use Case Registries"
Cohesion: 0.33
Nodes (5): 07 — Repository & Use Case Registries, 1. Repositories (30) — all `@Singleton`, all constructor-injected, no interfaces, 2. Use cases (31) — unscoped, constructor-injected unless noted, 3. ViewModels (27) — `@HiltViewModel`, one per screen, Repository → repository dependency graph

### Community 202 - "08 — Flow: Placements (Favorites, Dock, Folders) — add, remove, reorder, clean up"
Cohesion: 0.33
Nodes (6): 08 — Flow: Placements (Favorites, Dock, Folders) — add, remove, reorder, clean up, 1. The routing rule — global vs per-facet, 2. Reorder (drag), 3. Folders, 4. Keeping placements honest — three background processes, 5. Invariants this flow guarantees

### Community 204 - "13 — Flows: Facets, Theme resolution, Notification badges, Onboarding"
Cohesion: 0.33
Nodes (5): 13 — Flows: Facets, Theme resolution, Notification badges, Onboarding, 1. Facets — create, switch, preview, override, 2. Theme resolution — from DataStore to `MaterialTheme`, 3. Notification badges, 4. Onboarding & first run

### Community 209 - "09 — Flow: Backup & Restore"
Cohesion: 0.40
Nodes (5): 09 — Flow: Backup & Restore, 1. Export, 2. Import, 3. Widget re-bind after import, 4. Versioning contract

### Community 210 - "10 — Flow: Hub Widgets (host lifecycle, add, move, resize, delete, orphans)"
Cohesion: 0.40
Nodes (5): 10 — Flow: Hub Widgets (host lifecycle, add, move, resize, delete, orphans), 1. Who owns what, 2. Add a widget (`HubWidgetPickerViewModel`), 3. Move, resize, delete (`HubViewModel`), 4. Persistence & recovery matrix

### Community 212 - "12 — Flow: App Drawer — search, tabs, and per-app actions"
Cohesion: 0.33
Nodes (6): 12 — Flow: App Drawer — search, tabs, and per-app actions, 1. Data in, 1a. Folders in the drawer (`DrawerFolderDisplayMode`), 2. Search pipeline, 3. Per-app actions (long-press → `AppContextMenu`), 4. Gestures & routing (why the drawer isn't a nav destination)

### Community 216 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), AboutContent(), AboutHeader(), AboutScreen(), AboutScreenPreview(), ClickableAboutRow(), InfoRow() (+1 more)

### Community 230 - ".setContent"
Cohesion: 0.13
Nodes (10): PermissionsScreenTest, PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState, StateFlow (+2 more)

### Community 231 - "BackupRestoreMessage"
Cohesion: 0.18
Nodes (10): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+2 more)

### Community 239 - "ClockZoneHandle.kt"
Cohesion: 0.83
Nodes (3): ClockZoneHandle(), ClockZoneHandlePreview(), Modifier

### Community 247 - "Facet Launcher 0.1.10"
Cohesion: 0.33
Nodes (5): Facet Launcher 0.1.10, Fixes, Improvements, Internal, New Features

## Knowledge Gaps
- **528 isolated node(s):** `Personal`, `NONE`, `MENU`, `ADJUST`, `START` (+523 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 952 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockAdjustSheet.kt`, `.setContent`, `WidgetPlacementEntity`, `AppInfo`, `FacetCarouselScreen.kt`, `DockSettingsScreen.kt`, `.setContent`, `DockAppRepository`, `ClockColorOption`, `CalendarPermissionRepository`, `.setContent`, `.setContent`, `BackupRestoreScreen.kt`, `.setContent`, `WidgetResizeHandle.kt`, `.setContent`, `ClockStyleGalleryViewModel`, `FacetNavHost`, `PermissionsScreen.kt`, `AccentSwatch`, `AppWidgetRepository`, `.setContent`, `HomeDrawerRouteTest.kt`, `.setContent`, `.setContent`, `ContactRepository`, `.setContent`, `.setContent`, `homeAppLabelShadow`, `.setContent`, `LabeledDropdownRow`, `Folder`, `CardDivider`, `HubWidgetPickerScreen.kt`, `NotificationBadgeStyle`, `.setContent`, `FolderDetailScreen.kt`, `HomeScreen`, `FacetCarouselViewModel.kt`, `CalendarInfo`, `FacetSettingsContent`, `AppearanceSettingsScreen.kt`, `SettingsScreen.kt`, `ColorTest`, `TonalButton`, `AlphabetRail.kt`, `.setContent`, `.setContent`, `AppPickerScreen.kt`, `Rule`, `DockDisplayMode`, `HomeDrawerRoute`, `facetTypography`, `StickyHeaderLayout`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `ClockBlock`, `.setContent`, `OnboardingFacetsPage.kt`, `FontSizeSlider`, `.setContent`, `LauncherAppWidgetHost`, `.setContent`, `ClockZoneHandle.kt`, `SettingsRepository`, `HomeScreen.kt`, `R`, `HomeAppsListSettingsScreen.kt`, `ManageFacetsScreen.kt`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `DefaultFavoriteAppRepository`, `.setContent`, `FacetEntity`, `AppInfo`, `ManageFacetsViewModel`, `.setContent`, `DockAppRepository`, `ClockColorOption`, `CalendarPermissionRepository`, `PlacedItem`, `SettingsRepositoryTest`, `.drawerViewModel`, `.setContent`, `HomeViewModel`, `AppDrawerSettingsViewModelTest`, `FacetDao`, `FavoriteAppRepository`, `ClockStyleGalleryViewModel`, `HomeDrawerRouteTest.kt`, `.setContent`, `.setContent`, `NotificationAccessExplanationViewModel.kt`, `.setContent`, `.createViewModel`, `NotificationBadgeStyle`, `.setContent`, `FacetCarouselViewModel.kt`, `CalendarInfo`, `OnboardingViewModel`, `.setContent`, `.createViewModel`, `Rule`, `DockDisplayMode`, `ExportBackupUseCase.kt`, `.setContent`, `.setContent`, `ClockTemplateId`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `DefaultFavoriteAppRepository`, `AppRepository`, `FacetEntity`, `FacetCarouselScreen.kt`, `AppRepository.kt`, `AppIcon`, `DockSettingsScreen.kt`, `.setContent`, `DockAppRepository`, `DefaultFavoriteAppEntity`, `ClockColorOption`, `UserHandle`, `CalendarPermissionRepository`, `PlacedItem`, `FakeFavoriteAppDao`, `DrawerViewModel`, `HomeViewModel`, `FolderRepository`, `FacetDockAppEntity`, `FavoriteAppRepository`, `.setContent`, `AppShortcutRepository`, `FacetNavHost`, `AppWidgetRepository`, `PrivateSpaceRepository`, `HomeDrawerRouteTest.kt`, `.setContent`, `.repository`, `.setContent`, `FolderContentsSheet`, `Folder`, `CardDivider`, `.createViewModel`, `FolderDetailScreen.kt`, `FacetCarouselViewModel.kt`, `AppearanceSettingsScreen.kt`, `SettingsScreen.kt`, `FavoritesPickerViewModelTest`, `SortAppsForPickerUseCaseTest`, `AppContextMenu`, `GroupAppsByLetterUseCaseTest`, `OnboardingViewModel`, `UsageStatsRepository`, `.setContent`, `FolderAppPickerViewModelTest`, `AppPickerScreen.kt`, `Rule`, `DockDisplayMode`, `HomeDrawerRoute`, `SelectPreviewAppsUseCaseTest`, `DockAppPickerViewModelTest`, `FolderDetailViewModel`, `OnboardingViewModelTest`, `.setContent`, `.setContent`, `.setContent`, `SettingsRepository`, `.invoke`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 74 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.bottomPositionedSearchBarSitsInTheLowerHalfOfTheScreen()` and `.clearingTheSearchQueryRestoresTheFullList()`) actually correct?**
  _`FacetLauncherTheme()` has 74 INFERRED edges - model-reasoned connections that need verification._
- **Are the 24 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 142 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 142 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Personal`, `NONE`, `MENU` to the rest of the system?**
  _528 weakly-connected nodes found - possible documentation gaps or missing edges._