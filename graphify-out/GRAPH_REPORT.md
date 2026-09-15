# Graph Report - lumen-launcher  (2026-09-15)

## Corpus Check
- 409 files · ~837,098 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3946 nodes · 10993 edges · 186 communities (133 shown, 47 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 834 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `93a6298e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModelTest
- design_handoff_minimal_launcher/support.js
- NextAlarmRepositoryTest
- .setContent
- FolderAppPickerViewModelTest
- HomeDrawerRouteTest.kt
- .setContent
- Android launcher design planning/support.js
- .setContent
- Screens
- ManageFacetsViewModel
- ClockFontOption
- DockAppEntity
- ContactRepositoryTest
- SettingsRepository
- ComponentName
- Screens
- FavoritesPickerScreen.kt
- Fixture
- ClockStyleGalleryViewModel
- FacetCarouselUiState
- DockFolderPlacementEntity
- LauncherFontOption
- 4. Feature Requirements
- FavoriteFolderPlacementEntity
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- FakeFacetDao
- HubWidgetTile
- Row
- ClockTemplateId
- ObserveHomeScreenStateUseCase.kt
- AppShortcut
- AccentSwatch
- FacetNavHost
- WidgetPlacementRepository
- .setContent
- AppRepositoryTest.kt
- DockAppPickerViewModel
- SetDefaultLauncherSheetTest
- ContactConnection
- Manrope Font License (SIL OFL 1.1)
- .setContent
- PlacedItem
- DefaultFavoriteFolderPlacementEntity
- .setContent
- FacetCarouselScreen.kt
- Clock Widget Resize — Implementation Spec
- SettingsSearchEntry
- CalendarInfo
- BackupBundle
- DragReorderState
- CardDivider
- NotificationSettingsScreen.kt
- 4. Feature Requirements
- AppDrawerScreen.kt
- AppContextMenu
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- .setContent
- BackupRestoreViewModelTest
- ClockAlignment
- .drawerViewModel
- ResolveWidgetDropUseCaseTest
- AppIcon
- github.md
- HomeDrawerRoute
- ClockAccessoryIcons.kt
- .setContent
- .setContent
- AppDrawerSettingsViewModelTest
- SelectPreviewAppsUseCaseTest
- NotificationBadgeRepository
- SettingsRepository.kt
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
- FacetSettingsUiState
- Play Console — sensitive permission disclosures
- DefaultLauncherRepositoryTest
- FolderContentsSheet
- .setContent
- Facet Launcher — Built Capabilities
- FolderDetailViewModel
- Play Console — store listing text
- DrawerViewModel
- ObserveQuickAddStateUseCaseTest
- HomeScreen.kt
- .createViewModel
- ContactRepository
- AppDrawerScreen
- FacetLauncherTheme
- dashedBorder
- BackupRestoreViewModelTest.kt
- FolderEntity
- NotificationAccessExplanationViewModel.kt
- AppInfo
- .setContent
- ImportBackupResult
- StickyHeaderLayout
- Fixture
- FacetEntity
- HomeViewModel
- HubWidgetPickerViewModel
- .setContent
- Folder
- FacetDatabaseMigrationTest
- HomeWallpaper
- CalendarSettingsScreen.kt
- PlaceWidgetUseCaseTest
- .setContent
- BackButton
- FakeFolderDao
- OnboardingFacetsPage.kt
- WidgetResizeHandle.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DrawerListItemSize
- AppearanceSettingsViewModel
- WidgetPlacementEntity
- AppModule
- Facet Launcher — Onboarding Flow
- FolderAppPickerScreen.kt
- GestureHintOverlay
- HubViewModel
- .setContent
- BackupRestoreViewModel
- AppProfile
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- FacetDockFolderPlacementEntity
- SettingsRepositoryTest
- LauncherActivity.kt
- FavoriteAppEntity
- WallpaperRepositoryTest
- SetDefaultLauncherSheet
- OnboardingViewModelTest
- build_screenshot.py
- OnboardingViewModel
- 2. Design tokens
- HubAddWidgetEvent
- DefaultAppRepositoryTest
- PermissionKind
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- DockSettingsScreen.kt
- FacetScopeBadge
- FoldersSettingsContent
- BackupRestoreContent
- FacetDockAppEntity
- FontWeightSlider
- DrawerTab
- Axis
- TypeTest.kt
- FacetDatabase
- AppearanceSettingsScreen.kt
- HubWidgetPickerScreen.kt
- AppWidgetRepository
- .setContent
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- AppDrawerSettingsScreen.kt
- ClockAccessoryIconsTest
- PermissionsScreen.kt
- .setContent
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
8. `AppProfile` - 103 edges
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

## Communities (186 total, 47 thin omitted)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "NextAlarmRepositoryTest"
Cohesion: 0.08
Nodes (16): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus, BroadcastReceiver (+8 more)

### Community 3 - ".setContent"
Cohesion: 0.10
Nodes (9): HomeDrawerRouteTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 5 - "HomeDrawerRouteTest.kt"
Cohesion: 0.06
Nodes (36): any(), T, KeyboardDismissalTest, FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, BatteryRepository, CalendarPermissionRepository (+28 more)

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

### Community 11 - "ClockFontOption"
Cohesion: 0.06
Nodes (9): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+1 more)

### Community 12 - "DockAppEntity"
Cohesion: 0.12
Nodes (5): DockAppDao, Flow, DockAppEntity, Flow, DockAppDaoTest

### Community 13 - "ContactRepositoryTest"
Cohesion: 0.23
Nodes (3): eq(), ContactRepositoryTest, MatrixCursor

### Community 14 - "SettingsRepository"
Cohesion: 0.03
Nodes (44): BackupRestoreScreenTest, BackupRepository, T, resolveOverride(), ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME (+36 more)

### Community 15 - "ComponentName"
Cohesion: 0.24
Nodes (6): AppWidgetRepositoryTest, AppWidgetProviderInfo, UserHandle, UserManager, ApplicationInfo, ComponentName

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "FavoritesPickerScreen.kt"
Cohesion: 0.31
Nodes (15): FavoritesAppsList(), FavoritesFolderPickerRow(), FavoritesFoldersList(), FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview() (+7 more)

### Community 18 - "Fixture"
Cohesion: 0.21
Nodes (3): Fixture, ObserveHomeScreenStateUseCaseTest, SeedDefaultDockUseCaseTest

### Community 19 - "ClockStyleGalleryViewModel"
Cohesion: 0.13
Nodes (4): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel

### Community 21 - "DockFolderPlacementEntity"
Cohesion: 0.14
Nodes (6): DockFolderPlacementDao, Flow, DockFolderPlacementEntity, DockAppRepositoryTest, FakeDockAppDao, FakeDockFolderPlacementDao

### Community 22 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "FakeFacetDao"
Cohesion: 0.12
Nodes (3): FacetRepositoryTest, FakeFacetDao, Flow

### Community 29 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 30 - "Row"
Cohesion: 0.21
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 32 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.10
Nodes (9): DefaultLauncherRepository, Intent, NotificationShadeRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, ObserveQuickAddStateUseCase, NotificationShadeRepositoryTest (+1 more)

### Community 33 - "AppShortcut"
Cohesion: 0.17
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 34 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 35 - "FacetNavHost"
Cohesion: 0.23
Nodes (7): ClockStyleGalleryRoute(), FacetClockStyleGalleryScreen(), FacetDestinations, FacetNavHost(), Modifier, popBackStackSafely(), NavHostController

### Community 36 - "WidgetPlacementRepository"
Cohesion: 0.10
Nodes (8): Flow, WidgetPlacementDao, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

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

### Community 44 - "PlacedItem"
Cohesion: 0.04
Nodes (37): DockSettingsScreenTest, DefaultFavoriteAppRepository, Flow, DockAppRepository, Flow, FacetDockAppRepository, com, Flow (+29 more)

### Community 45 - "DefaultFavoriteFolderPlacementEntity"
Cohesion: 0.18
Nodes (3): DefaultFavoriteFolderPlacementEntity, FakeDefaultFavoriteFolderPlacementDao, DefaultFavoriteFolderPlacementDaoTest

### Community 46 - ".setContent"
Cohesion: 0.13
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 47 - "FacetCarouselScreen.kt"
Cohesion: 0.06
Nodes (50): Image, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), Alignment (+42 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "SettingsSearchEntry"
Cohesion: 0.22
Nodes (3): SettingsSearchEntry, CatalogEntry, SystemSettingsRepositoryTest

### Community 50 - "CalendarInfo"
Cohesion: 0.06
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel (+2 more)

### Community 51 - "BackupBundle"
Cohesion: 0.15
Nodes (7): Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest, ImportBackupUseCaseTest

### Community 52 - "DragReorderState"
Cohesion: 0.25
Nodes (5): DragReorderState, Modifier, T, detectGrabOrResizeGesture(), detectHomeSwipeGestures()

### Community 53 - "CardDivider"
Cohesion: 0.18
Nodes (26): rememberDragReorderState(), ReorderRowDefaults, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+18 more)

### Community 54 - "NotificationSettingsScreen.kt"
Cohesion: 0.25
Nodes (9): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), StateFlow, ViewModel, NotificationSettingsUiState (+1 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.19
Nodes (24): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip() (+16 more)

### Community 57 - "AppContextMenu"
Cohesion: 0.24
Nodes (13): Add, QuickPlacementAction, Remove, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), FolderCandidateRow(), Composable (+5 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.18
Nodes (21): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+13 more)

### Community 59 - "HomeScreen"
Cohesion: 0.13
Nodes (4): HomeScreenTest, HomeScreen(), HomeScreenTextOnlyPresentationPreview(), stableKey()

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.19
Nodes (10): ClockAdjustSheetTest, AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Composable, Modifier, ClockZoneHandle(), ClockZoneHandlePreview() (+2 more)

### Community 63 - "ClockAlignment"
Cohesion: 0.12
Nodes (20): ClockAlignment, CENTER, LEFT, RIGHT, CalendarEventsBlock(), EventRow(), Color, FontFamily (+12 more)

### Community 66 - "AppIcon"
Cohesion: 0.42
Nodes (9): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), WorkProfileBadge() (+1 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "HomeDrawerRoute"
Cohesion: 0.18
Nodes (12): ClockAdjustMode, ADJUST, MENU, NONE, HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier (+4 more)

### Community 69 - "ClockAccessoryIcons.kt"
Cohesion: 0.29
Nodes (11): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+3 more)

### Community 71 - ".setContent"
Cohesion: 0.14
Nodes (3): AppContextMenuTest, SharedFlow, QuickAddState

### Community 74 - "NotificationBadgeRepository"
Cohesion: 0.07
Nodes (20): BroadcastReceiver, Callback, BroadcastReceiver, Callback, flattenIcon(), Bitmap, BroadcastReceiver, Context (+12 more)

### Community 75 - "SettingsRepository.kt"
Cohesion: 0.08
Nodes (19): DataStoreModule, Context, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK (+11 more)

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
Cohesion: 0.18
Nodes (10): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+2 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "FacetSettingsUiState"
Cohesion: 0.28
Nodes (12): FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron(), SectionHeader(), appsSubtitle(), FacetSettingsContent() (+4 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 89 - "FolderContentsSheet"
Cohesion: 0.40
Nodes (10): AddHere, FolderContentsAppRow(), FolderContentsEmptyState(), FolderContentsGrid(), FolderContentsGridTile(), FolderContentsList(), FolderContentsSheet(), FolderSheetHeaderAction (+2 more)

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
Cohesion: 0.15
Nodes (3): DrawerViewModel, StateFlow, ViewModel

### Community 96 - "HomeScreen.kt"
Cohesion: 0.25
Nodes (20): DrawerPresentation, GRID, LIST, HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow() (+12 more)

### Community 97 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 99 - "AppDrawerScreen"
Cohesion: 0.11
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (6): AlphabetRailTest, ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), FacetLauncherTheme()

### Community 101 - "dashedBorder"
Cohesion: 0.18
Nodes (13): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+5 more)

### Community 102 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.33
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 103 - "FolderEntity"
Cohesion: 0.10
Nodes (8): Flow, FolderAppEntity, FolderDao, FolderWithApps, Flow, FolderEntity, DockFolderPlacementDaoTest, FolderDaoTest

### Community 104 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 105 - "AppInfo"
Cohesion: 0.05
Nodes (16): FavoritesPickerScreenTest, HomeAppsListSettingsScreenTest, AppRepository, DefaultAppRepository, Intent, FolderRepository, AppInfo, GetInstalledAppsUseCase (+8 more)

### Community 107 - "ImportBackupResult"
Cohesion: 0.28
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 108 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (8): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier

### Community 110 - "FacetEntity"
Cohesion: 0.04
Nodes (27): FacetRepository, Flow, toCsv(), FacetDao, Flow, FacetEntity, LauncherSettings, HomeUiState (+19 more)

### Community 111 - "HomeViewModel"
Cohesion: 0.13
Nodes (4): HomeViewModel, Intent, StateFlow, ViewModel

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 116 - "HomeWallpaper"
Cohesion: 0.08
Nodes (13): HomeWallpaper, Tones, Unavailable, Bitmap, WallpaperRepository, DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository (+5 more)

### Community 117 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 120 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 122 - "OnboardingFacetsPage.kt"
Cohesion: 0.54
Nodes (7): FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview(), OnboardingUiState

### Community 123 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "DrawerListItemSize"
Cohesion: 0.08
Nodes (16): ContactInfo, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT (+8 more)

### Community 132 - "AppearanceSettingsViewModel"
Cohesion: 0.25
Nodes (3): AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.11
Nodes (14): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase (+6 more)

### Community 134 - "AppModule"
Cohesion: 0.27
Nodes (4): AppModule, Context, LauncherApps, UserManager

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "FolderAppPickerScreen.kt"
Cohesion: 0.18
Nodes (13): FolderAppPickerScreenTest, AppIconSize, FolderAppPickerContent(), FolderAppPickerHeader(), FolderAppPickerRow(), FolderAppPickerScreen(), FolderAppPickerScreenPreview(), Modifier (+5 more)

### Community 137 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 138 - "HubViewModel"
Cohesion: 0.11
Nodes (18): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+10 more)

### Community 139 - ".setContent"
Cohesion: 0.05
Nodes (18): SettingsScreenTest, BroadcastReceiver, Context, Flow, Intent, WorkProfileRepository, BroadcastReceiver, SharedFlow (+10 more)

### Community 140 - "BackupRestoreViewModel"
Cohesion: 0.22
Nodes (6): BackupRestoreUiState, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 141 - "AppProfile"
Cohesion: 0.06
Nodes (9): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, AppProfile, PERSONAL, WORK, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao (+1 more)

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "FacetDockFolderPlacementEntity"
Cohesion: 0.11
Nodes (5): FacetDockFolderPlacementEntity, FakeFacetDockFolderPlacementDao, FakeFavoriteFolderPlacementDao, Flow, FacetDockFolderPlacementDaoTest

### Community 145 - "SettingsRepositoryTest"
Cohesion: 0.10
Nodes (5): EnsureActiveFacetUseCase, SettingsRepositoryTest, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 146 - "LauncherActivity.kt"
Cohesion: 0.39
Nodes (5): Intent, LauncherApps, LauncherActivity, Bundle, ComponentActivity

### Community 147 - "FavoriteAppEntity"
Cohesion: 0.10
Nodes (7): FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

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

### Community 156 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "DockSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier (+2 more)

### Community 160 - "FacetScopeBadge"
Cohesion: 0.73
Nodes (5): FacetScopeBadge(), Color, Modifier, ScopeBadge(), WorkScopeBadge()

### Community 161 - "FoldersSettingsContent"
Cohesion: 0.29
Nodes (9): FolderRow(), FoldersSettingsContent(), FoldersSettingsScreen(), FoldersSettingsScreenPreview(), Modifier, FoldersSettingsUiState, FoldersSettingsViewModel, StateFlow (+1 more)

### Community 162 - "BackupRestoreContent"
Cohesion: 0.38
Nodes (10): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+2 more)

### Community 163 - "FacetDockAppEntity"
Cohesion: 0.07
Nodes (8): FacetDockAppDao, Flow, FacetDockAppEntity, FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow, FacetDaoTest, FacetDockAppDaoTest

### Community 164 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 165 - "DrawerTab"
Cohesion: 0.50
Nodes (3): DrawerTab, PERSONAL, WORK

### Community 166 - "Axis"
Cohesion: 0.67
Nodes (3): Axis, HORIZONTAL, VERTICAL

### Community 174 - "FacetDatabase"
Cohesion: 0.06
Nodes (12): DatabaseModule, Context, DefaultFavoriteFolderPlacementDao, Flow, FacetDatabase, FacetDockFolderPlacementDao, Flow, FavoriteFolderPlacementDao (+4 more)

### Community 180 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 183 - "HubWidgetPickerScreen.kt"
Cohesion: 0.23
Nodes (15): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Dp, Modifier, WidgetProviderGroupRow() (+7 more)

### Community 185 - "AppWidgetRepository"
Cohesion: 0.10
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.26
Nodes (15): appDrawerSummary(), appsListSummary(), ClickableRow(), dockSummary(), folderCountSummary(), Composable, Modifier, NavigationChevron() (+7 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

## Knowledge Gaps
- **378 isolated node(s):** `Keys`, `CatalogEntry`, `PERSONAL`, `WORK`, `THEME` (+373 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 729 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `DrawerListItemSize`, `.setContent`, `WidgetPlacementEntity`, `HomeDrawerRouteTest.kt`, `.setContent`, `.setContent`, `FolderAppPickerScreen.kt`, `ManageFacetsViewModel`, `.setContent`, `GestureHintOverlay`, `SettingsRepository`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `SetDefaultLauncherSheet`, `LauncherFontOption`, `.setContent`, `DockSettingsScreen.kt`, `FoldersSettingsContent`, `BackupRestoreContent`, `AccentSwatch`, `FontWeightSlider`, `.setContent`, `SetDefaultLauncherSheetTest`, `ContactConnection`, `.setContent`, `PlacedItem`, `.setContent`, `FacetCarouselScreen.kt`, `CalendarInfo`, `AppearanceSettingsScreen.kt`, `CardDivider`, `NotificationSettingsScreen.kt`, `HubWidgetPickerScreen.kt`, `AppDrawerScreen.kt`, `.setContent`, `HomeScreen`, `ClockAdjustSheet.kt`, `.setContent`, `LabeledDropdownRow`, `ClockAlignment`, `SettingsScreen.kt`, `ColorTest`, `.setContent`, `.setContent`, `SettingsRepository.kt`, `DockAppPickerScreen.kt`, `FolderDetailScreen.kt`, `AppDrawerSettingsScreen.kt`, `FacetSettingsUiState`, `.setContent`, `PermissionsScreen.kt`, `.setContent`, `HomeScreen.kt`, `AppDrawerScreen`, `dashedBorder`, `AppInfo`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `Folder`, `HomeWallpaper`, `CalendarSettingsScreen.kt`, `.setContent`, `BackButton`, `OnboardingFacetsPage.kt`, `WidgetResizeHandle.kt`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `DrawerListItemSize`, `.setContent`, `HomeDrawerRouteTest.kt`, `.setContent`, `AppearanceSettingsViewModel`, `FolderAppPickerScreen.kt`, `FolderAppPickerViewModelTest`, `.setContent`, `DockAppEntity`, `AppProfile`, `SettingsRepository`, `FavoritesPickerScreen.kt`, `LauncherActivity.kt`, `FavoriteAppEntity`, `Fixture`, `DockFolderPlacementEntity`, `OnboardingViewModelTest`, `OnboardingViewModel`, `DockSettingsScreen.kt`, `ObserveHomeScreenStateUseCase.kt`, `AppShortcut`, `FoldersSettingsContent`, `FacetNavHost`, `FacetDockAppEntity`, `AppRepositoryTest.kt`, `DockAppPickerViewModel`, `PlacedItem`, `.setContent`, `FacetCarouselScreen.kt`, `AppearanceSettingsScreen.kt`, `CardDivider`, `AppDrawerScreen.kt`, `AppContextMenu`, `HomeScreen`, `SettingsScreen.kt`, `AppIcon`, `HomeDrawerRoute`, `.setContent`, `SelectPreviewAppsUseCaseTest`, `NotificationBadgeRepository`, `DockAppPickerScreen.kt`, `FolderDetailScreen.kt`, `GroupAppsByLetterUseCaseTest`, `FolderContentsSheet`, `FolderDetailViewModel`, `DrawerViewModel`, `ObserveQuickAddStateUseCaseTest`, `HomeScreen.kt`, `AppDrawerScreen`, `FolderEntity`, `.setContent`, `Fixture`, `FacetEntity`, `HomeViewModel`, `Folder`, `HomeWallpaper`, `FakeFolderDao`?**
  _High betweenness centrality (0.130) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModelTest`, `.setContent`, `DrawerListItemSize`, `HomeDrawerRouteTest.kt`, `.setContent`, `ManageFacetsViewModel`, `.setContent`, `ClockFontOption`, `SettingsRepositoryTest`, `Fixture`, `ClockStyleGalleryViewModel`, `LauncherFontOption`, `.setContent`, `Row`, `ClockTemplateId`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `PlacedItem`, `.setContent`, `CalendarInfo`, `NotificationSettingsScreen.kt`, `.setContent`, `.setContent`, `ClockAlignment`, `.drawerViewModel`, `AppDrawerSettingsViewModelTest`, `SettingsRepository.kt`, `HomeScreen.kt`, `.createViewModel`, `NotificationAccessExplanationViewModel.kt`, `AppInfo`, `ImportBackupResult`, `FacetEntity`, `HomeWallpaper`, `.setContent`?**
  _High betweenness centrality (0.067) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.themed()` and `facetTypography()`) actually correct?**
  _`FacetLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 24 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 141 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 141 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `CatalogEntry`, `PERSONAL` to the rest of the system?**
  _378 weakly-connected nodes found - possible documentation gaps or missing edges._