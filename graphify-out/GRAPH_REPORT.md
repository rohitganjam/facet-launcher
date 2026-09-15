# Graph Report - lumen-launcher  (2026-09-15)

## Corpus Check
- 409 files · ~836,986 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3944 nodes · 10990 edges · 184 communities (127 shown, 51 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 834 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `50494f9d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModelTest
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- FolderAppPickerViewModelTest
- CalendarPermissionRepository
- .createViewModel
- Android launcher design planning/support.js
- .setContent
- Screens
- FakeFacetDao
- EnsureActiveFacetUseCase
- NotificationBadgeRepository
- HomeDrawerRouteTest.kt
- FontWeightOption
- AppWidgetRepository
- Screens
- FavoritesPickerScreen.kt
- Fixture
- ClockColorOption
- FacetCarouselScreen.kt
- DockAppEntity
- LauncherFontOption
- 4. Feature Requirements
- FavoriteFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- FacetRepository
- HubGrid
- Row
- ClockTemplateId
- FolderTestFakes.kt
- AppShortcut
- AccentSwatch
- FacetNavHost
- WidgetPlacementRepository
- LauncherAppWidgetHost
- AppIcon
- DockAppPickerViewModel
- ConvertersTest
- ContactConnection
- Manrope Font License (SIL OFL 1.1)
- FacetDockFolderPlacementEntity
- PlacedItem
- NextAlarmRepositoryTest
- CalendarSettingsViewModel
- .createViewModel
- Clock Widget Resize — Implementation Spec
- SettingsSearchEntry
- CalendarRepository
- SettingsRepository
- FolderDetailScreen.kt
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
- ResolveWidgetDropUseCaseTest
- QuickAddState
- github.md
- DragReorderState
- HomeDrawerRoute
- DefaultLauncherRepositoryTest
- .setContent
- UserHandle
- SelectPreviewAppsUseCaseTest
- .setContent
- CalendarSettingsScreen.kt
- NotificationSettingsScreen.kt
- CompactWidgetsUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- DockAppPickerScreen.kt
- ResolveWidgetResizeUseCaseTest
- BackupMapping.kt
- EnsureActiveFacetUseCaseTest.kt
- NextAlarmRepository
- FacetDao
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- FolderAppPickerScreen.kt
- FolderContentsSheet
- .setContent
- Facet Launcher — Built Capabilities
- FolderDetailViewModel
- Play Console — store listing text
- DrawerViewModel
- ManageFacetsViewModel
- HomeScreen.kt
- .createViewModel
- FacetDaoTest
- AppDrawerScreen
- FacetLauncherTheme
- dashedBorder
- BackupRestoreViewModelTest.kt
- FolderEntity
- GroupAppsByLetterUseCaseTest
- HomeWallpaper
- HomeAppsListSettingsScreen.kt
- AssignCalendarColorsUseCaseTest
- AppRepositoryTest.kt
- .useCase
- FacetEntity
- HomeViewModel
- HubWidgetPickerViewModel
- .setContent
- Folder
- FacetDatabaseMigrationTest
- DockSettingsViewModel.kt
- PermissionKind
- PlaceWidgetUseCaseTest
- ContactRepository
- BackButton
- FakeFolderDao
- FacetCarouselViewModel
- WidgetResizeHandle.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DrawerGridSize
- NotificationAccessExplanationViewModel.kt
- WidgetPlacementEntity
- ImageBitmap
- Facet Launcher — Onboarding Flow
- HubViewModel
- .setContent
- BackupRestoreViewModel
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- .repository
- SettingsRepositoryTest
- LauncherActivity.kt
- FavoriteAppEntity
- WallpaperRepositoryTest
- OnboardingViewModelTest
- OnboardingViewModel
- 2. Design tokens
- DefaultAppRepositoryTest
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- DockSettingsScreen.kt
- .setContent
- FoldersSettingsContent
- BackupRestoreContent
- AppProfile
- FacetDatabase
- CalendarRepositoryTest
- AppearanceSettingsScreen.kt
- DockFolderPlacementEntity
- HubWidgetPickerScreen.kt
- AppWidgetRepository.kt
- .setContent
- .homeViewModel
- SettingsScreen.kt
- ColorTest
- .setContent
- letterAt
- release.sh
- OnboardingFacetsPage.kt
- AppInfo
- AppDrawerSettingsScreen.kt
- ObserveQuickAddStateUseCaseTest
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- FacetEntityTest
- ClockCornerHandle
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 255 edges
2. `AppInfo` - 224 edges
3. `FacetEntity` - 212 edges
4. `SettingsRepository` - 160 edges
5. `Row` - 146 edges
6. `FacetRepository` - 134 edges
7. `LauncherSettings` - 123 edges
8. `AppProfile` - 102 edges
9. `Folder` - 96 edges
10. `FolderRepository` - 76 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `BackupRepositoryTest` --calls--> `BackupRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/BackupRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/BackupRepository.kt
- `BatteryRepositoryTest` --calls--> `BatteryRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/BatteryRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/BatteryRepository.kt
- `CalendarRepositoryTest` --calls--> `CalendarRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/CalendarRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/CalendarRepository.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (184 total, 51 thin omitted)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.17
Nodes (9): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus, BatteryRepositoryTest (+1 more)

### Community 4 - "FolderAppPickerViewModelTest"
Cohesion: 0.16
Nodes (6): FolderAppPickerScreenTest, FolderAppPickerUiState, FolderAppPickerViewModel, StateFlow, ViewModel, FolderAppPickerViewModelTest

### Community 5 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (12): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, UsageAccessRepository, StateFlow, ViewModel, PermissionsViewModel (+4 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "FakeFacetDao"
Cohesion: 0.18
Nodes (3): FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 11 - "EnsureActiveFacetUseCase"
Cohesion: 0.22
Nodes (3): EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao

### Community 12 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 13 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (25): KeyboardDismissalTest, FakeNotificationAccessRepository, BatteryRepository, ContactPermissionRepository, NotificationAccessRepository, Intent, SecureFolderRepository, CatalogEntry (+17 more)

### Community 14 - "FontWeightOption"
Cohesion: 0.03
Nodes (52): Flow, Converters, T, resolveOverride(), ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS (+44 more)

### Community 15 - "AppWidgetRepository"
Cohesion: 0.15
Nodes (7): AppWidgetRepository, AppWidgetRepositoryTest, AppWidgetProviderInfo, UserHandle, UserManager, ApplicationInfo, ComponentName

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "FavoritesPickerScreen.kt"
Cohesion: 0.31
Nodes (15): FavoritesAppsList(), FavoritesFolderPickerRow(), FavoritesFoldersList(), FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview() (+7 more)

### Community 19 - "ClockColorOption"
Cohesion: 0.06
Nodes (15): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+7 more)

### Community 20 - "FacetCarouselScreen.kt"
Cohesion: 0.07
Nodes (28): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), AddFacetPage(), FacetCarouselContent() (+20 more)

### Community 21 - "DockAppEntity"
Cohesion: 0.10
Nodes (8): DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, FakeDockFolderPlacementDao, DockAppDaoTest

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

### Community 28 - "FacetRepository"
Cohesion: 0.07
Nodes (4): FacetRepository, toCsv(), FacetRepositoryTest, FakeFacetDao

### Community 29 - "HubGrid"
Cohesion: 0.18
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 30 - "Row"
Cohesion: 0.19
Nodes (70): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+62 more)

### Community 31 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 32 - "FolderTestFakes.kt"
Cohesion: 0.11
Nodes (6): DefaultFavoriteFolderPlacementEntity, FolderWithApps, Flow, FakeDefaultFavoriteFolderPlacementDao, Flow, DefaultFavoriteFolderPlacementDaoTest

### Community 33 - "AppShortcut"
Cohesion: 0.22
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 34 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 35 - "FacetNavHost"
Cohesion: 0.17
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), Modifier (+5 more)

### Community 36 - "WidgetPlacementRepository"
Cohesion: 0.11
Nodes (7): Flow, WidgetPlacementDao, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 37 - "LauncherAppWidgetHost"
Cohesion: 0.22
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 38 - "AppIcon"
Cohesion: 0.53
Nodes (8): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), WorkProfileBadge()

### Community 39 - "DockAppPickerViewModel"
Cohesion: 0.15
Nodes (14): DockAppPickerViewModel, StateFlow, ViewModel, Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS (+6 more)

### Community 41 - "ContactConnection"
Cohesion: 0.15
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 43 - "FacetDockFolderPlacementEntity"
Cohesion: 0.16
Nodes (3): FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FacetDockFolderPlacementDaoTest

### Community 44 - "PlacedItem"
Cohesion: 0.07
Nodes (23): Flow, com, Flow, FolderItem, PlacedItem, SingleApp, combine(), Flow (+15 more)

### Community 46 - "CalendarSettingsViewModel"
Cohesion: 0.20
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "CalendarRepository"
Cohesion: 0.17
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo

### Community 51 - "SettingsRepository"
Cohesion: 0.04
Nodes (12): BackupRestoreScreenTest, DockSettingsScreenTest, BackupRepository, DockAppRepository, FacetDockAppRepository, BackupSettings, SettingsRepository, ExportBackupUseCase (+4 more)

### Community 52 - "FolderDetailScreen.kt"
Cohesion: 0.29
Nodes (10): Modifier, RenameDialog(), ReorderRowDefaults, FolderAppsReorderList(), FolderDetailContent(), FolderDetailHeader(), FolderDetailScreen(), FolderDetailScreenEmptyPreview() (+2 more)

### Community 53 - "CardDivider"
Cohesion: 0.30
Nodes (16): rememberDragReorderState(), CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow(), DockSection() (+8 more)

### Community 54 - "ContactRepositoryTest"
Cohesion: 0.22
Nodes (3): eq(), ContactRepositoryTest, MatrixCursor

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.12
Nodes (34): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT (+26 more)

### Community 57 - "AppContextMenu"
Cohesion: 0.22
Nodes (13): Add, QuickPlacementAction, Remove, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow(), Composable (+5 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.19
Nodes (10): ClockAdjustSheetTest, AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Composable, Modifier, ClockZoneHandle(), ClockZoneHandlePreview() (+2 more)

### Community 63 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 66 - "QuickAddState"
Cohesion: 0.20
Nodes (4): FolderTileContextMenuTest, SharedFlow, ObserveQuickAddStateUseCase, QuickAddState

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "DragReorderState"
Cohesion: 0.25
Nodes (5): DragReorderState, Modifier, T, detectGrabOrResizeGesture(), detectHomeSwipeGestures()

### Community 69 - "HomeDrawerRoute"
Cohesion: 0.05
Nodes (42): SetDefaultLauncherSheetTest, GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), AlarmClockIcon(), BatteryIcon() (+34 more)

### Community 72 - "UserHandle"
Cohesion: 0.10
Nodes (16): BroadcastReceiver, Callback, BroadcastReceiver, Callback, flattenIcon(), Bitmap, BroadcastReceiver, Context (+8 more)

### Community 74 - ".setContent"
Cohesion: 0.08
Nodes (8): FacetCarouselScreenTest, AppModule, Context, LauncherApps, UserManager, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 75 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 76 - "NotificationSettingsScreen.kt"
Cohesion: 0.57
Nodes (6): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), NotificationSettingsUiState

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "DockAppPickerScreen.kt"
Cohesion: 0.31
Nodes (15): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockAppsList(), DockFolderPickerRow(), DockFoldersList(), DockPickerRow() (+7 more)

### Community 81 - "BackupMapping.kt"
Cohesion: 0.10
Nodes (23): Uri, BackupAppEntry, BackupBundle, BackupFacet, BackupFolder, BackupFolderPlacement, BackupWidgetPlacement, com (+15 more)

### Community 82 - "EnsureActiveFacetUseCaseTest.kt"
Cohesion: 0.33
Nodes (5): DataStoreModule, Context, Flow, DataStore, Preferences

### Community 83 - "NextAlarmRepository"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - ".setContent"
Cohesion: 0.10
Nodes (19): FacetSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), FacetSettingsHeader(), FacetSettingsRow(), Composable (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "FolderAppPickerScreen.kt"
Cohesion: 0.50
Nodes (8): AppIconSize, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), Modifier, PickerSectionHeader()

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

### Community 94 - "DrawerViewModel"
Cohesion: 0.17
Nodes (3): DrawerViewModel, StateFlow, ViewModel

### Community 95 - "ManageFacetsViewModel"
Cohesion: 0.32
Nodes (3): StateFlow, ViewModel, ManageFacetsViewModel

### Community 96 - "HomeScreen.kt"
Cohesion: 0.32
Nodes (18): DrawerPresentation, GRID, LIST, AppRow(), dashedBorder(), DockIcon(), FolderDockIcon(), FolderRow() (+10 more)

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
Cohesion: 0.18
Nodes (13): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+5 more)

### Community 102 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.19
Nodes (8): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion, HubFull, Placed, PlaceWidgetResult

### Community 103 - "FolderEntity"
Cohesion: 0.15
Nodes (5): Flow, FolderAppEntity, FolderDao, FolderEntity, FolderDaoTest

### Community 105 - "HomeWallpaper"
Cohesion: 0.07
Nodes (17): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, HomeWallpaper, Tones, Unavailable, Bitmap, WallpaperRepository (+9 more)

### Community 106 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 108 - "AppRepositoryTest.kt"
Cohesion: 0.20
Nodes (8): any(), AppRepositoryTest, Context, LauncherApps, T, UserHandle, UserManager, LauncherActivityInfo

### Community 110 - "FacetEntity"
Cohesion: 0.06
Nodes (23): FacetEntity, LauncherSettings, HomeUiState, AddAppToDockUseCaseTest, Fixture, AddAppToFavoritesUseCaseTest, Fixture, AddFolderToDockUseCaseTest (+15 more)

### Community 111 - "HomeViewModel"
Cohesion: 0.11
Nodes (6): NotificationShadeRepository, HomeViewModel, Intent, StateFlow, ViewModel, NotificationShadeRepositoryTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.13
Nodes (6): WidgetProviderOption, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 114 - "Folder"
Cohesion: 0.14
Nodes (3): FolderDetailScreenTest, FoldersSettingsScreenTest, Folder

### Community 116 - "DockSettingsViewModel.kt"
Cohesion: 0.16
Nodes (7): DockSettingsViewModel, Flow, StateFlow, ViewModel, DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 117 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 120 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 122 - "FacetCarouselViewModel"
Cohesion: 0.33
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 123 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "DrawerGridSize"
Cohesion: 0.08
Nodes (12): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, SearchBarPosition, BOTTOM, TOP (+4 more)

### Community 132 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (9): WidgetPlacementEntity, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, ObserveHubStateUseCaseTest (+1 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "HubViewModel"
Cohesion: 0.13
Nodes (11): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier, HubUiState, HubWidgetUi, HubViewModel (+3 more)

### Community 139 - ".setContent"
Cohesion: 0.05
Nodes (18): SettingsScreenTest, BroadcastReceiver, Context, Flow, Intent, WorkProfileRepository, BroadcastReceiver, SharedFlow (+10 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.11
Nodes (16): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+8 more)

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.11
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 146 - "LauncherActivity.kt"
Cohesion: 0.39
Nodes (5): Intent, LauncherApps, LauncherActivity, Bundle, ComponentActivity

### Community 147 - "FavoriteAppEntity"
Cohesion: 0.10
Nodes (7): FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 152 - "OnboardingViewModel"
Cohesion: 0.20
Nodes (3): StateFlow, ViewModel, OnboardingViewModel

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "DockSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier (+2 more)

### Community 161 - "FoldersSettingsContent"
Cohesion: 0.29
Nodes (9): FolderRow(), FoldersSettingsContent(), FoldersSettingsScreen(), FoldersSettingsScreenPreview(), Modifier, FoldersSettingsUiState, FoldersSettingsViewModel, StateFlow (+1 more)

### Community 162 - "BackupRestoreContent"
Cohesion: 0.38
Nodes (10): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+2 more)

### Community 163 - "AppProfile"
Cohesion: 0.05
Nodes (9): FacetDockAppDao, Flow, FacetDockAppEntity, AppProfile, PERSONAL, WORK, FakeFacetDockAppDao, Flow (+1 more)

### Community 174 - "FacetDatabase"
Cohesion: 0.09
Nodes (10): DatabaseModule, Context, DefaultFavoriteFolderPlacementDao, Flow, FacetDatabase, FacetDockFolderPlacementDao, Flow, Migrations (+2 more)

### Community 180 - "AppearanceSettingsScreen.kt"
Cohesion: 0.05
Nodes (47): AppearanceSettingsScreenTest, Image, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY (+39 more)

### Community 182 - "DockFolderPlacementEntity"
Cohesion: 0.13
Nodes (4): DockFolderPlacementDao, Flow, DockFolderPlacementEntity, DockFolderPlacementDaoTest

### Community 183 - "HubWidgetPickerScreen.kt"
Cohesion: 0.12
Nodes (25): HubWidgetPickerScreenTest, FacetScopeBadge(), Color, Modifier, ScopeBadge(), WorkScopeBadge(), HubWidgetPickerContent(), HubWidgetPickerHeader() (+17 more)

### Community 185 - "AppWidgetRepository.kt"
Cohesion: 0.13
Nodes (11): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, UserHandle (+3 more)

### Community 186 - ".setContent"
Cohesion: 0.19
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 187 - ".homeViewModel"
Cohesion: 0.15
Nodes (5): DefaultLauncherRepository, Intent, ClockAccessoryState, Flow, HomeViewModelTest

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.26
Nodes (15): appDrawerSummary(), appsListSummary(), ClickableRow(), dockSummary(), folderCountSummary(), Composable, Modifier, NavigationChevron() (+7 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 198 - "OnboardingFacetsPage.kt"
Cohesion: 0.54
Nodes (7): FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview(), OnboardingUiState

### Community 201 - "AppInfo"
Cohesion: 0.05
Nodes (22): FavoritesPickerScreenTest, AppRepository, DefaultFavoriteAppRepository, Flow, FavoriteAppRepository, com, Flow, FolderRepository (+14 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

## Knowledge Gaps
- **378 isolated node(s):** `Keys`, `CatalogEntry`, `PERSONAL`, `WORK`, `THEME` (+373 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 728 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `DrawerGridSize`, `.setContent`, `WidgetPlacementEntity`, `FolderAppPickerViewModelTest`, `CalendarPermissionRepository`, `.setContent`, `.setContent`, `HomeDrawerRouteTest.kt`, `FontWeightOption`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `FacetCarouselScreen.kt`, `LauncherFontOption`, `.setContent`, `DockSettingsScreen.kt`, `.setContent`, `FoldersSettingsContent`, `BackupRestoreContent`, `FacetNavHost`, `AccentSwatch`, `LauncherAppWidgetHost`, `ContactConnection`, `CalendarRepository`, `SettingsRepository`, `AppearanceSettingsScreen.kt`, `CardDivider`, `FolderDetailScreen.kt`, `HubWidgetPickerScreen.kt`, `AppDrawerScreen.kt`, `.setContent`, `HomeScreen`, `ClockAdjustSheet.kt`, `.setContent`, `LabeledDropdownRow`, `SettingsScreen.kt`, `ColorTest`, `QuickAddState`, `.setContent`, `HomeDrawerRoute`, `OnboardingFacetsPage.kt`, `.setContent`, `AppInfo`, `.setContent`, `CalendarSettingsScreen.kt`, `NotificationSettingsScreen.kt`, `DockAppPickerScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `FolderAppPickerScreen.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `HomeScreen.kt`, `AppDrawerScreen`, `dashedBorder`, `HomeWallpaper`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `Folder`, `BackButton`, `WidgetResizeHandle.kt`?**
  _High betweenness centrality (0.197) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `DrawerGridSize`, `.setContent`, `FolderAppPickerViewModelTest`, `ImageBitmap`, `.createViewModel`, `.setContent`, `HomeDrawerRouteTest.kt`, `FontWeightOption`, `DefaultFavoriteAppEntity`, `.repository`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `FavoriteAppEntity`, `FacetCarouselScreen.kt`, `DockAppEntity`, `Fixture`, `OnboardingViewModelTest`, `OnboardingViewModel`, `DockSettingsScreen.kt`, `AppShortcut`, `FoldersSettingsContent`, `FacetNavHost`, `AppProfile`, `DockAppPickerViewModel`, `PlacedItem`, `SettingsRepository`, `AppearanceSettingsScreen.kt`, `CardDivider`, `DockFolderPlacementEntity`, `FolderDetailScreen.kt`, `AppDrawerScreen.kt`, `AppContextMenu`, `HomeScreen`, `.homeViewModel`, `SettingsScreen.kt`, `QuickAddState`, `HomeDrawerRoute`, `.setContent`, `UserHandle`, `SelectPreviewAppsUseCaseTest`, `.setContent`, `DockAppPickerScreen.kt`, `FolderAppPickerScreen.kt`, `FolderContentsSheet`, `ObserveQuickAddStateUseCaseTest`, `FolderDetailViewModel`, `DrawerViewModel`, `HomeScreen.kt`, `AppDrawerScreen`, `FolderEntity`, `GroupAppsByLetterUseCaseTest`, `HomeWallpaper`, `HomeAppsListSettingsScreen.kt`, `AppRepositoryTest.kt`, `.useCase`, `FacetEntity`, `HomeViewModel`, `Folder`, `DockSettingsViewModel.kt`, `FakeFolderDao`?**
  _High betweenness centrality (0.136) - this node is a cross-community bridge._
- **Why does `FacetEntity` connect `FacetEntity` to `ClockStyleGalleryViewModelTest`, `.createViewModel`, `FakeFacetDao`, `EnsureActiveFacetUseCase`, `FontWeightOption`, `Fixture`, `ClockColorOption`, `FacetCarouselScreen.kt`, `FavoriteAppEntity`, `FavoriteFolderPlacementEntity`, `FacetRepository`, `ClockTemplateId`, `AppProfile`, `FacetDockFolderPlacementEntity`, `PlacedItem`, `CalendarSettingsViewModel`, `.createViewModel`, `CalendarRepository`, `SettingsRepository`, `LabeledDropdownRow`, `.homeViewModel`, `AppInfo`, `BackupMapping.kt`, `EnsureActiveFacetUseCaseTest.kt`, `FacetDao`, `ManageFacetsViewModel`, `FacetDaoTest`, `HomeWallpaper`, `FacetEntityTest`, `.setContent`, `DockSettingsViewModel.kt`, `FacetCarouselViewModel`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.themed()` and `facetTypography()`) actually correct?**
  _`FacetLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 24 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 141 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 141 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `CatalogEntry`, `PERSONAL` to the rest of the system?**
  _378 weakly-connected nodes found - possible documentation gaps or missing edges._