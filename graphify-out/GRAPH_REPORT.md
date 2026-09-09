# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 288 files · ~457,725 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2727 nodes · 7434 edges · 153 communities (102 shown, 48 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 525 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dbe26e8c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- homeAppLabelShadow
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- AppInfo
- ClockFontOption
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ContactRepository
- ProfileEntity
- ProfileDao
- SettingsRepository
- AppWidgetRepository
- WidgetPlacementRepository
- ClockColorOption
- AppDrawerSettingsViewModel
- Screens
- LauncherFontOption
- WidgetPlacementEntity
- .setContent
- LauncherSettings
- LumenLauncherTheme
- DrawerViewModel
- 4. Feature Requirements
- FakeProfileDao
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- ProfileCarouselScreen.kt
- ProfileCarouselUiState
- Row
- ContactRepositoryTest
- .setContent
- .setContent
- .refresh
- .setContent
- ClockTemplateId
- ProfileCarouselScreenTest.kt
- ProfileSettingsScreen.kt
- .setContent
- ExportBackupUseCase.kt
- DockAppRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- ManageProfilesViewModel
- BackupMapping.kt
- HubGrid
- CalendarEventsBlock.kt
- Clock Widget Resize — Implementation Spec
- .setContent
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- BackupRestoreViewModelTest
- DefaultFavoriteAppRepository
- eq
- CalendarRepositoryTest
- DockAppPickerScreen.kt
- HomeDrawerRouteTest.kt
- LauncherAppWidgetHost
- ImportBackupUseCase
- AppWidgetRepository.kt
- ListContentMode
- RankBySearchRelevanceUseCase
- .setContent
- SettingsScreen.kt
- FakeDockAppDao
- ObserveHomeScreenStateUseCase
- github.md
- BackupRestoreViewModel
- ColorTest
- LumenNavHost
- LumenDatabase
- letterAt
- HomeDrawerRoute
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- DragReorderState
- .setContent
- .setContent
- WidgetProviderOption
- .createViewModel
- ComponentName
- PermissionKind
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- HubContent
- BackupRestoreContent
- CalendarInfo
- AppearanceSettingsScreen.kt
- dashedBorder
- EnsureActiveProfileUseCase
- AppRepository
- ClockAlignment
- HubAtCapacityStrip.kt
- HubEmptyState.kt
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- HubHeader
- OrphanedWidgetTile.kt
- Fixture
- NotificationBadgeRepository
- PlaceWidgetUseCaseTest
- HubWidgetPickerViewModel
- .setContent
- UsageStatsRepositoryTest
- .setContent
- ClockColors.kt
- ClockAdjustSheet.kt
- WidgetResizeHandle
- DockAppEntity
- StickyHeaderLayout
- LabeledDropdownRow
- ProfileSettingsViewModel.kt
- LumenDatabaseMigrationTest
- AppModule
- BackButton
- combine
- NotificationAccessExplanationViewModel.kt
- .rendersOneEntryPerLetterProvided
- BackupRestoreMessage
- .setContent
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .setContent
- .createViewModel
- DefaultFavoriteAppDao
- DockAppDao
- FontWeightOption
- WidgetPlacementDao
- GroupAppsByLetterUseCaseTest
- CalendarSettingsScreen.kt
- CardDivider
- .setContent
- DockSettingsScreen.kt
- .setContent
- LauncherActivity.kt
- NotificationShadeRepository
- ProfileEntityTest
- ObserveHubStateUseCase
- ProfileCarouselViewModel.kt
- HubWidgetTile
- DockSettingsViewModel
- FavoriteAppDaoTest
- .setContent
- HubAddWidgetEvent

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 207 edges
2. `ProfileEntity` - 151 edges
3. `AppInfo` - 146 edges
4. `SettingsRepository` - 130 edges
5. `Row` - 106 edges
6. `ProfileRepository` - 105 edges
7. `LauncherSettings` - 90 edges
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
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `CalendarRepositoryTest` --calls--> `CalendarRepository`  [INFERRED]
  app/src/test/kotlin/com/lumenlauncher/app/data/CalendarRepositoryTest.kt → app/src/main/kotlin/com/lumenlauncher/app/data/CalendarRepository.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (153 total, 48 thin omitted)

### Community 0 - "homeAppLabelShadow"
Cohesion: 0.50
Nodes (7): accentTonalExtremes(), homeAppLabelShadow(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme, Shadow

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.18
Nodes (6): DefaultLauncherRepository, ObserveSettingsScreenStateUseCase, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "AppInfo"
Cohesion: 0.08
Nodes (10): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, AppInfo, FakeFavoriteAppDao, FavoriteAppRepositoryTest (+2 more)

### Community 4 - "ClockFontOption"
Cohesion: 0.12
Nodes (8): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 5 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactRepository"
Cohesion: 0.10
Nodes (23): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+15 more)

### Community 9 - "ProfileEntity"
Cohesion: 0.14
Nodes (3): ProfileEntity, ProfileRepository, toCsv()

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "SettingsRepository"
Cohesion: 0.05
Nodes (21): DockDisplayMode, ICONS, TEXT, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR (+13 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.12
Nodes (3): AppWidgetRepository, AppWidgetRepositoryTest, DeleteWidgetUseCaseTest

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockColorOption"
Cohesion: 0.08
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 15 - "AppDrawerSettingsViewModel"
Cohesion: 0.15
Nodes (4): AppDrawerSettingsScreenTest, AppDrawerSettingsViewModel, StateFlow, ViewModel

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "LauncherFontOption"
Cohesion: 0.05
Nodes (29): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, LauncherFontOption, MANROPE, NOTO_SANS, POPPINS (+21 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.15
Nodes (8): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 20 - "LauncherSettings"
Cohesion: 0.16
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "DrawerViewModel"
Cohesion: 0.11
Nodes (8): AppContextMenuTest, AppShortcutRepository, AppShortcut, DrawerViewModel, StateFlow, ViewModel, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FakeProfileDao"
Cohesion: 0.12
Nodes (3): FakeProfileDao, Flow, ProfileRepositoryTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.17
Nodes (25): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase (+17 more)

### Community 28 - "ProfileCarouselScreen.kt"
Cohesion: 0.49
Nodes (9): AddProfilePage(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselScreen(), ProfileCarouselScreenPreview(), ProfilePreviewPage() (+1 more)

### Community 30 - "Row"
Cohesion: 0.21
Nodes (62): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+54 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 34 - ".refresh"
Cohesion: 0.17
Nodes (7): Callback, Callback, LumenNotificationListenerService, Callback, NotificationListenerService, StatusBarNotification, UserHandle

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "ProfileCarouselScreenTest.kt"
Cohesion: 0.23
Nodes (5): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 40 - "ExportBackupUseCase.kt"
Cohesion: 0.23
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 43 - "HomeScreen.kt"
Cohesion: 0.21
Nodes (18): AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+10 more)

### Community 44 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 45 - "BackupMapping.kt"
Cohesion: 0.17
Nodes (13): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+5 more)

### Community 46 - "HubGrid"
Cohesion: 0.26
Nodes (10): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+2 more)

### Community 47 - "CalendarEventsBlock.kt"
Cohesion: 0.24
Nodes (13): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+5 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 53 - "DefaultFavoriteAppRepository"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "eq"
Cohesion: 0.36
Nodes (5): any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "HomeDrawerRouteTest.kt"
Cohesion: 0.07
Nodes (17): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, ContactPermissionRepository, NotificationAccessRepository (+9 more)

### Community 58 - "LauncherAppWidgetHost"
Cohesion: 0.20
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 59 - "ImportBackupUseCase"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "AppWidgetRepository.kt"
Cohesion: 0.15
Nodes (9): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, IntentSender, toBitmap(), calculateHubCellWidth() (+1 more)

### Community 61 - "ListContentMode"
Cohesion: 0.05
Nodes (22): Converters, T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT (+14 more)

### Community 62 - "RankBySearchRelevanceUseCase"
Cohesion: 0.21
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 63 - ".setContent"
Cohesion: 0.06
Nodes (7): AppearanceSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase, DefaultAppRepositoryTest, SelectPreviewAppsUseCaseTest, AppearanceSettingsViewModelTest

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 66 - "ObserveHomeScreenStateUseCase"
Cohesion: 0.21
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 70 - "LumenNavHost"
Cohesion: 0.32
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "LumenDatabase"
Cohesion: 0.20
Nodes (6): DatabaseModule, Context, LumenDatabase, Migrations, Migration, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "HomeDrawerRoute"
Cohesion: 0.09
Nodes (19): ClockAdjustMode, ADJUST, MENU, NONE, HomeViewModel, StateFlow, ViewModel, Axis (+11 more)

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 75 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 76 - "DragReorderState"
Cohesion: 0.31
Nodes (4): DragReorderState, Modifier, T, detectGrabOrResizeGesture()

### Community 79 - "WidgetProviderOption"
Cohesion: 0.29
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

### Community 81 - "ComponentName"
Cohesion: 0.22
Nodes (6): Intent, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, ComponentName

### Community 82 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.08
Nodes (31): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+23 more)

### Community 86 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 87 - "BackupRestoreContent"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 88 - "CalendarInfo"
Cohesion: 0.07
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel (+2 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

### Community 90 - "dashedBorder"
Cohesion: 0.70
Nodes (4): dashedBorder(), Color, Dp, Modifier

### Community 91 - "EnsureActiveProfileUseCase"
Cohesion: 0.15
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 92 - "AppRepository"
Cohesion: 0.27
Nodes (6): AppRepository, flattenIcon(), Bitmap, Flow, Drawable, LauncherApps

### Community 93 - "ClockAlignment"
Cohesion: 0.18
Nodes (6): ClockAlignment, CENTER, LEFT, RIGHT, Alignment, resolve()

### Community 94 - "HubAtCapacityStrip.kt"
Cohesion: 0.83
Nodes (3): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 95 - "HubEmptyState.kt"
Cohesion: 0.83
Nodes (3): HubEmptyState(), HubEmptyStatePreview(), Modifier

### Community 99 - "HubHeader"
Cohesion: 0.83
Nodes (3): HubHeader(), HubHeaderAtCapacityPreview(), Modifier

### Community 100 - "OrphanedWidgetTile.kt"
Cohesion: 0.83
Nodes (3): Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview()

### Community 102 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 107 - ".setContent"
Cohesion: 0.11
Nodes (11): KeyboardDismissalTest, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState (+3 more)

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 112 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (8): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier

### Community 113 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 114 - "ProfileSettingsViewModel.kt"
Cohesion: 0.20
Nodes (3): StateFlow, ViewModel, ProfileSettingsViewModel

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "combine"
Cohesion: 0.16
Nodes (11): combine(), Flow, Flow, SettingsScreenState, T1, T2, T3, T4 (+3 more)

### Community 119 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 121 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 122 - ".setContent"
Cohesion: 0.18
Nodes (5): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 135 - "FontWeightOption"
Cohesion: 0.10
Nodes (16): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+8 more)

### Community 138 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 139 - "CardDivider"
Cohesion: 0.26
Nodes (15): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+7 more)

### Community 141 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (10): rememberDragReorderState(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+2 more)

### Community 143 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 147 - "ProfileCarouselViewModel.kt"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 148 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 149 - "DockSettingsViewModel"
Cohesion: 0.43
Nodes (4): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

### Community 152 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

## Knowledge Gaps
- **247 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+242 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 504 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `SettingsScreenTest.kt`, `AppInfo`, `.setContent`, `.setContent`, `FontWeightOption`, `ContactRepository`, `CalendarSettingsScreen.kt`, `CardDivider`, `.setContent`, `DockSettingsScreen.kt`, `.setContent`, `AppDrawerSettingsViewModel`, `LauncherActivity.kt`, `LauncherFontOption`, `WidgetPlacementEntity`, `.setContent`, `SettingsRepository`, `DrawerViewModel`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `ProfileCarouselScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `ProfileCarouselScreenTest.kt`, `ProfileSettingsScreen.kt`, `.setContent`, `HomeScreen.kt`, `.setContent`, `AppDrawerScreen`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `LauncherAppWidgetHost`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `LumenDatabase`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `.setContent`, `WidgetProviderOption`, `BackupRestoreContent`, `CalendarInfo`, `AppearanceSettingsScreen.kt`, `AppRepository`, `HubAtCapacityStrip.kt`, `HubEmptyState.kt`, `HubHeader`, `OrphanedWidgetTile.kt`, `.setContent`, `.setContent`, `ClockAdjustSheet.kt`, `WidgetResizeHandle`, `StickyHeaderLayout`, `LabeledDropdownRow`, `BackButton`, `.rendersOneEntryPerLetterProvided`, `.setContent`?**
  _High betweenness centrality (0.182) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `SettingsScreenTest.kt`, `.createViewModel`, `.setContent`, `ContactRepository`, `GroupAppsByLetterUseCaseTest`, `SettingsRepository`, `DockSettingsScreen.kt`, `LauncherActivity.kt`, `LauncherFontOption`, `ProfileCarouselViewModel.kt`, `LauncherSettings`, `LumenLauncherTheme`, `DrawerViewModel`, `DockSettingsViewModel`, `HomeScreen`, `AppDrawerScreen.kt`, `ProfileCarouselScreen.kt`, `.setContent`, `.refresh`, `ProfileCarouselScreenTest.kt`, `ProfileSettingsScreen.kt`, `DockAppRepository`, `HomeScreen.kt`, `AppDrawerScreen`, `DefaultFavoriteAppRepository`, `eq`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `ListContentMode`, `.setContent`, `SettingsScreen.kt`, `FakeDockAppDao`, `ObserveHomeScreenStateUseCase`, `LumenNavHost`, `HomeDrawerRoute`, `HomeAppsListSettingsScreen.kt`, `.createViewModel`, `AppearanceSettingsScreen.kt`, `AppRepository`, `Fixture`, `UsageStatsRepositoryTest`, `.setContent`, `ProfileSettingsViewModel.kt`, `combine`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `.setContent`, `ClockFontOption`, `.createViewModel`, `FontWeightOption`, `ProfileEntity`, `.setContent`, `.setContent`, `AppDrawerSettingsViewModel`, `ClockColorOption`, `LauncherFontOption`, `.setContent`, `LauncherSettings`, `ProfileCarouselViewModel.kt`, `DrawerViewModel`, `DockSettingsViewModel`, `AppDrawerScreen.kt`, `.setContent`, `.setContent`, `ClockTemplateId`, `ProfileCarouselScreenTest.kt`, `.setContent`, `ExportBackupUseCase.kt`, `DockAppRepository`, `ManageProfilesViewModel`, `BackupMapping.kt`, `.setContent`, `ClockStyleGalleryViewModelTest`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase`, `ListContentMode`, `RankBySearchRelevanceUseCase`, `.setContent`, `ObserveHomeScreenStateUseCase`, `LumenDatabase`, `HomeDrawerRoute`, `.setContent`, `.createViewModel`, `AppDrawerSettingsViewModelTest`, `CalendarInfo`, `EnsureActiveProfileUseCase`, `AppRepository`, `ClockAlignment`, `SettingsRepositoryTest`, `.setContent`, `ProfileSettingsViewModel.kt`, `combine`, `NotificationAccessExplanationViewModel.kt`, `.setContent`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _247 weakly-connected nodes found - possible documentation gaps or missing edges._