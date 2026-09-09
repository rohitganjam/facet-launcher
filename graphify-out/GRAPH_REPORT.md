# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 289 files · ~459,742 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2758 nodes · 7430 edges · 145 communities (94 shown, 48 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 543 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4779737c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ProfileCarouselViewModel.kt
- design_handoff_minimal_launcher/support.js
- ClockFontsTest.kt
- FavoriteAppRepository
- ClockFonts.kt
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- WidgetProviderOption
- AccentSwatch
- ProfileDaoTest
- AppDrawerSettingsViewModel
- AppWidgetRepository
- WidgetPlacementRepository
- ClockStyleGalleryViewModel
- ContactConnection
- Screens
- LauncherFontOption
- WidgetPlacementEntity
- .setContent
- LauncherSettings
- LumenLauncherTheme
- .setContent
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- BackupRestoreViewModelTest
- ProfileCarouselUiState
- Row
- ContactRepositoryTest
- .setContent
- SettingsScreenTest.kt
- .refresh
- .setContent
- ClockTemplateId
- AppWidgetRepository.kt
- ProfileSettingsScreen.kt
- .setContent
- ImportBackupUseCaseTest.kt
- AppShortcutRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- SettingsRepository.kt
- DefaultFavoriteAppRepository
- ManageProfilesScreen.kt
- CalendarEventsBlock.kt
- Clock Widget Resize — Implementation Spec
- combine
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- .createViewModel
- StickyHeaderLayout
- AppRepository
- .setContent
- DockAppPickerScreen.kt
- CalendarPermissionRepository
- HomeDrawerRouteTest.kt
- ImportBackupUseCase.kt
- DockAppRepository
- ClockFontOption
- ComponentName
- .setContent
- SettingsScreen.kt
- FakeDockAppDao
- ObserveHomeScreenStateUseCase.kt
- github.md
- BackupRestoreViewModel
- ColorTest
- LumenNavHost
- LumenDatabase
- letterAt
- homeAppLabelShadow
- HomeAppsListSettingsScreen.kt
- LabeledDropdownRow
- SelectPreviewAppsUseCaseTest
- DefaultFavoriteAppDao
- .setContent
- ProfileCarouselScreen.kt
- DockSettingsViewModel
- NotificationBadgeRepository
- PermissionKind
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- DrawerViewModel
- PlaceWidgetResult
- CalendarRepository
- AppearanceSettingsScreen.kt
- DefaultAppRepositoryTest
- CalendarRepositoryTest
- AssignCalendarColorsUseCaseTest
- DockAppDao
- DockAppEntity
- FavoriteAppDaoTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- HubAddWidgetEvent
- UsageAccessExplanationViewModel
- Fixture
- AppContextMenuTest
- PlaceWidgetUseCaseTest
- HubWidgetPickerViewModel
- .setContent
- ContactRepository
- AppInfo
- BackupRestoreContent
- ClockAdjustSheet.kt
- DrawerViewModelTest
- BackupMapping.kt
- PermissionsScreen.kt
- Fixture
- AppRepository.kt
- LumenDatabaseMigrationTest
- .setContent
- BackButton
- ContactConnectionType
- SettingsRepository
- AlphabetRail
- Type.kt
- .`a valid backup replaces every profile, dock, and default favorite, then reports what was restored`
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- NotificationAccessExplanationViewModel.kt
- .createViewModel
- ExportBackupUseCaseTest.kt
- ClockAlignment.kt
- FontWeightOption
- ImageVector
- GroupAppsByLetterUseCaseTest
- CalendarSettingsScreen.kt
- CardDivider
- androidx
- DockSettingsScreen.kt
- .setContent
- Color
- FontWeight

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 203 edges
2. `ProfileEntity` - 148 edges
3. `AppInfo` - 138 edges
4. `SettingsRepository` - 130 edges
5. `Row` - 106 edges
6. `ProfileRepository` - 105 edges
7. `LauncherSettings` - 90 edges
8. `ClockTemplateId` - 71 edges
9. `WidgetPlacementEntity` - 61 edges
10. `ClockDateStyle` - 59 edges

## Surprising Connections (you probably didn't know these)
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `AppDrawerScreen()` --calls--> `GroupAppsByLetterUseCase`  [INFERRED]
  app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/lumenlauncher/app/domain/GroupAppsByLetterUseCase.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (145 total, 48 thin omitted)

### Community 0 - "ProfileCarouselViewModel.kt"
Cohesion: 0.22
Nodes (6): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 3 - "FavoriteAppRepository"
Cohesion: 0.09
Nodes (8): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 4 - "ClockFonts.kt"
Cohesion: 0.53
Nodes (5): FontFamily, resolveFontFamily(), variableWeightInstances(), Font, FontStyle

### Community 5 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "WidgetProviderOption"
Cohesion: 0.24
Nodes (11): HubWidgetPickerScreenTest, WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow() (+3 more)

### Community 9 - "AccentSwatch"
Cohesion: 0.18
Nodes (11): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+3 more)

### Community 11 - "AppDrawerSettingsViewModel"
Cohesion: 0.07
Nodes (19): AppDrawerSettingsScreenTest, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT (+11 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.12
Nodes (3): AppWidgetRepository, AppWidgetRepositoryTest, DeleteWidgetUseCaseTest

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockStyleGalleryViewModel"
Cohesion: 0.13
Nodes (4): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel

### Community 15 - "ContactConnection"
Cohesion: 0.20
Nodes (14): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, Multiple, Single, ConnectionRow(), ContactConnectionsSheet() (+6 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "LauncherFontOption"
Cohesion: 0.06
Nodes (22): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, LauncherFontOption, MANROPE, NOTO_SANS, POPPINS (+14 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 19 - ".setContent"
Cohesion: 0.10
Nodes (6): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 20 - "LauncherSettings"
Cohesion: 0.07
Nodes (23): T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT, RIGHT (+15 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - ".setContent"
Cohesion: 0.07
Nodes (21): HomeDrawerRouteTest, EnsureActiveProfileUseCase, ClockAdjustMode, ADJUST, MENU, NONE, Axis, HORIZONTAL (+13 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.05
Nodes (11): Flow, ProfileDao, ProfileEntity, Flow, ProfileRepository, toCsv(), ProfileEntityTest, FakeProfileDao (+3 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.23
Nodes (23): androidx, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile() (+15 more)

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 34 - ".refresh"
Cohesion: 0.17
Nodes (7): Callback, Callback, LumenNotificationListenerService, Callback, NotificationListenerService, StatusBarNotification, UserHandle

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "AppWidgetRepository.kt"
Cohesion: 0.15
Nodes (9): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, IntentSender, toBitmap(), calculateHubCellWidth() (+1 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 40 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 41 - "AppShortcutRepository"
Cohesion: 0.19
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 43 - "HomeScreen.kt"
Cohesion: 0.20
Nodes (21): NotificationBadgeStyle, COUNT, DOT, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel() (+13 more)

### Community 44 - "SettingsRepository.kt"
Cohesion: 0.17
Nodes (9): DataStoreModule, Context, DockDisplayMode, ICONS, TEXT, Keys, Flow, DataStore (+1 more)

### Community 45 - "DefaultFavoriteAppRepository"
Cohesion: 0.16
Nodes (5): DefaultFavoriteAppRepository, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 46 - "ManageProfilesScreen.kt"
Cohesion: 0.05
Nodes (43): ManageProfilesScreenTest, Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), HubGrid() (+35 more)

### Community 47 - "CalendarEventsBlock.kt"
Cohesion: 0.24
Nodes (13): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+5 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "combine"
Cohesion: 0.05
Nodes (17): ProfileSettingsScreenTest, FakeNotificationAccessRepository, NotificationSettingsScreenTest, combine(), Flow, ProfileSettingsViewModel, StateFlow, ViewModel (+9 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (4): AppDrawerScreenTest, AppDrawerScreen(), DrawerGridSize, SearchBarPosition

### Community 52 - ".createViewModel"
Cohesion: 0.14
Nodes (4): DefaultAppRepository, Intent, SelectPreviewAppsUseCase, AppearanceSettingsViewModelTest

### Community 53 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (8): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier

### Community 54 - "AppRepository"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (11): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow (+3 more)

### Community 58 - "HomeDrawerRouteTest.kt"
Cohesion: 0.11
Nodes (16): KeyboardDismissalTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, CleanUpUninstalledAppsUseCase (+8 more)

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.26
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "DockAppRepository"
Cohesion: 0.14
Nodes (3): DockAppRepository, Flow, CleanUpUninstalledAppsUseCaseTest

### Community 61 - "ClockFontOption"
Cohesion: 0.07
Nodes (9): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+1 more)

### Community 62 - "ComponentName"
Cohesion: 0.22
Nodes (6): Intent, Intent, LauncherActivity, Bundle, ComponentActivity, ComponentName

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 66 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.09
Nodes (9): NotificationShadeRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest (+1 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "BackupRestoreViewModel"
Cohesion: 0.11
Nodes (16): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+8 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.32
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "LumenDatabase"
Cohesion: 0.13
Nodes (8): DatabaseModule, Context, LumenDatabase, Migrations, Flow, WidgetPlacementDao, Migration, RoomDatabase

### Community 73 - "homeAppLabelShadow"
Cohesion: 0.15
Nodes (19): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier (+11 more)

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.21
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+6 more)

### Community 75 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+4 more)

### Community 77 - "DefaultFavoriteAppDao"
Cohesion: 0.17
Nodes (3): Flow, DefaultFavoriteAppDao, Flow

### Community 79 - "ProfileCarouselScreen.kt"
Cohesion: 0.10
Nodes (35): ConfirmDialog(), Modifier, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview(), Modifier, HubHeader() (+27 more)

### Community 80 - "DockSettingsViewModel"
Cohesion: 0.22
Nodes (5): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 81 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 82 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.08
Nodes (31): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+23 more)

### Community 86 - "DrawerViewModel"
Cohesion: 0.25
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 87 - "PlaceWidgetResult"
Cohesion: 0.40
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 88 - "CalendarRepository"
Cohesion: 0.09
Nodes (9): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, CalendarSettingsViewModel, StateFlow, ViewModel (+1 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

### Community 99 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 100 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.13
Nodes (8): AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 105 - ".setContent"
Cohesion: 0.30
Nodes (6): HubScreenTest, HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 107 - "AppInfo"
Cohesion: 0.14
Nodes (10): AppInfo, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel (+2 more)

### Community 108 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 111 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 112 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

### Community 114 - "AppRepository.kt"
Cohesion: 0.38
Nodes (4): flattenIcon(), Bitmap, Flow, Drawable

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "ContactConnectionType"
Cohesion: 0.33
Nodes (6): ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER, WHATSAPP

### Community 119 - "SettingsRepository"
Cohesion: 0.06
Nodes (12): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+4 more)

### Community 120 - "AlphabetRail"
Cohesion: 0.38
Nodes (4): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale()

### Community 121 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 135 - "FontWeightOption"
Cohesion: 0.12
Nodes (11): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+3 more)

### Community 137 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.24
Nodes (3): GroupAppsByLetterUseCase, GroupedApps, GroupAppsByLetterUseCaseTest

### Community 138 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 139 - "CardDivider"
Cohesion: 0.42
Nodes (8): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview()

### Community 141 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

## Knowledge Gaps
- **248 isolated node(s):** `LumenType`, `Keys`, `Multiple`, `Single`, `InvalidFile` (+243 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 510 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `ProfileCarouselViewModel.kt`, `.setContent`, `FontWeightOption`, `WidgetProviderOption`, `AccentSwatch`, `CalendarSettingsScreen.kt`, `AppDrawerSettingsViewModel`, `CardDivider`, `DockSettingsScreen.kt`, `.setContent`, `ContactConnection`, `LauncherFontOption`, `WidgetPlacementEntity`, `.setContent`, `LauncherSettings`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `SettingsScreenTest.kt`, `.setContent`, `ProfileSettingsScreen.kt`, `.setContent`, `AppShortcutRepository`, `HomeScreen.kt`, `ManageProfilesScreen.kt`, `combine`, `AppDrawerScreen`, `.createViewModel`, `StickyHeaderLayout`, `.setContent`, `DockAppPickerScreen.kt`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `ComponentName`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `homeAppLabelShadow`, `HomeAppsListSettingsScreen.kt`, `LabeledDropdownRow`, `.setContent`, `ProfileCarouselScreen.kt`, `DrawerViewModel`, `CalendarRepository`, `AppearanceSettingsScreen.kt`, `AppContextMenuTest`, `.setContent`, `AppInfo`, `BackupRestoreContent`, `ClockAdjustSheet.kt`, `PermissionsScreen.kt`, `.setContent`, `BackButton`, `AlphabetRail`, `Type.kt`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ProfileCarouselViewModel.kt`, `FavoriteAppRepository`, `.createViewModel`, `.setContent`, `GroupAppsByLetterUseCaseTest`, `DockSettingsScreen.kt`, `LauncherFontOption`, `.setContent`, `LauncherSettings`, `LumenLauncherTheme`, `.setContent`, `HomeScreen`, `SettingsScreenTest.kt`, `.refresh`, `ProfileSettingsScreen.kt`, `AppShortcutRepository`, `HomeScreen.kt`, `SettingsRepository.kt`, `DefaultFavoriteAppRepository`, `combine`, `AppDrawerScreen`, `.createViewModel`, `AppRepository`, `DockAppPickerScreen.kt`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `DockAppRepository`, `ComponentName`, `.setContent`, `SettingsScreen.kt`, `FakeDockAppDao`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `HomeAppsListSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `DefaultFavoriteAppDao`, `.setContent`, `DockSettingsViewModel`, `DrawerViewModel`, `AppearanceSettingsScreen.kt`, `Fixture`, `AppContextMenuTest`, `Fixture`, `AppRepository.kt`, `SettingsRepository`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ProfileCarouselViewModel.kt`, `NotificationAccessExplanationViewModel.kt`, `.createViewModel`, `ExportBackupUseCaseTest.kt`, `FontWeightOption`, `AppDrawerSettingsViewModel`, `.setContent`, `ClockStyleGalleryViewModel`, `LauncherFontOption`, `.setContent`, `LauncherSettings`, `.setContent`, `ProfileEntity`, `SettingsScreenTest.kt`, `.setContent`, `ClockTemplateId`, `.setContent`, `ImportBackupUseCaseTest.kt`, `SettingsRepository.kt`, `ManageProfilesScreen.kt`, `combine`, `ClockStyleGalleryViewModelTest`, `.createViewModel`, `.setContent`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `ClockFontOption`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `DockSettingsViewModel`, `AppDrawerSettingsViewModelTest`, `DrawerViewModel`, `CalendarRepository`, `SettingsRepositoryTest`, `AppInfo`, `.setContent`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `AppDrawerScreenGridPreview()`) actually correct?**
  _`LumenLauncherTheme()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `LumenType`, `Keys`, `Multiple` to the rest of the system?**
  _248 weakly-connected nodes found - possible documentation gaps or missing edges._