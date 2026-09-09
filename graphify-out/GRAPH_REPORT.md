# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 288 files · ~459,441 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2757 nodes · 7437 edges · 144 communities (90 shown, 51 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 535 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `703bcecb`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HomeScreen
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- FavoriteAppRepository
- LauncherFontOption
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ContactConnection
- AccentSwatch
- ProfileDao
- DrawerGridSize
- AppWidgetRepository
- WidgetPlacementEntity
- ClockColorOption
- .setContent
- Screens
- WallpaperAccentRole
- ResolveWidgetDropUseCase
- .setContent
- LauncherSettings
- LumenLauncherTheme
- DrawerViewModel
- 4. Feature Requirements
- ProfileEntity
- HomeScreenTest
- 4. Feature Requirements
- AppDrawerScreen.kt
- CalendarSettingsViewModel
- ProfileCarouselViewModel.kt
- Row
- ContactRepositoryTest
- .setContent
- .setContent
- .refresh
- .setContent
- ClockTemplateId
- .createViewModel
- ProfileSettingsScreen.kt
- .setContent
- ExportBackupUseCase.kt
- DockAppRepository
- Manrope Font License (SIL OFL 1.1)
- AppIcon
- ManageProfilesViewModel
- DefaultFavoriteAppRepository
- HubGrid
- CalendarEventsBlock.kt
- Clock Widget Resize — Implementation Spec
- .setContent
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- .createViewModel
- FakeDefaultFavoriteAppDao
- AppRepository
- CalendarRepositoryTest
- DockAppPickerScreen.kt
- SettingsRepository
- LauncherAppWidgetHost
- .setContent
- AppearanceSettingsViewModelTest.kt
- ClockFontOption
- HomeAppsListSettingsViewModel
- .setContent
- SettingsScreen.kt
- FakeDockAppDao
- HomeViewModel
- github.md
- BackupRestoreViewModel
- ColorTest
- LumenNavHost
- LumenDatabase
- letterAt
- HomeDrawerRoute
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- SelectPreviewAppsUseCaseTest
- .setContent
- .setContent
- ContactRepository
- .createViewModel
- NotificationSettingsViewModel
- PermissionKind
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- AppWidgetHostView
- LumenNotificationListenerService
- CalendarInfo
- AppearanceSettingsScreen.kt
- DefaultAppRepositoryTest
- SettingsRepositoryTest.kt
- AssignCalendarColorsUseCaseTest
- SettingsViewModelTest
- ContactConnectionType
- Type.kt
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- ExportBackupUseCaseTest.kt
- Intent
- Fixture
- NotificationBadgeRepository
- PlaceWidgetUseCaseTest
- HubWidgetPickerViewModel
- .setContent
- Dp
- LauncherViewModel
- FontWeight
- ClockAdjustSheet.kt
- WidgetResizeHandle
- DockAppEntity
- StickyHeaderLayout
- ManageProfilesScreen.kt
- Context
- LumenDatabaseMigrationTest
- androidx
- BackButton
- combine
- NotificationAccessExplanationViewModel.kt
- .rendersOneEntryPerLetterProvided
- Offset
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
- AppInfo
- CalendarSettingsScreen.kt
- CardDivider
- DockSettingsScreen.kt
- .setContent
- LauncherActivity.kt
- HomeDrawerRouteTest.kt
- DockSettingsViewModel

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 206 edges
2. `ProfileEntity` - 151 edges
3. `AppInfo` - 139 edges
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
- `Fixture` --calls--> `ObserveHomeScreenStateUseCase`  [INFERRED]
  app/src/test/kotlin/com/lumenlauncher/app/domain/ObserveHomeScreenStateUseCaseTest.kt → app/src/main/kotlin/com/lumenlauncher/app/domain/ObserveHomeScreenStateUseCase.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (144 total, 51 thin omitted)

### Community 0 - "HomeScreen"
Cohesion: 0.16
Nodes (29): AppRow(), ClockAdjustMode, ADJUST, MENU, NONE, dashedBorder(), DockIcon(), HomeScreen() (+21 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.21
Nodes (7): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel

### Community 3 - "FavoriteAppRepository"
Cohesion: 0.08
Nodes (10): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest (+2 more)

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactConnection"
Cohesion: 0.20
Nodes (14): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, Multiple, Single, ConnectionRow(), ContactConnectionsSheet() (+6 more)

### Community 9 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "DrawerGridSize"
Cohesion: 0.10
Nodes (14): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerPresentation, GRID, LIST (+6 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockColorOption"
Cohesion: 0.06
Nodes (15): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+7 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 18 - "ResolveWidgetDropUseCase"
Cohesion: 0.15
Nodes (7): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - ".setContent"
Cohesion: 0.15
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 20 - "LauncherSettings"
Cohesion: 0.20
Nodes (4): LauncherSettings, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "DrawerViewModel"
Cohesion: 0.08
Nodes (9): AppContextMenuTest, AppShortcutRepository, AppShortcut, DrawerViewModel, StateFlow, ViewModel, AppShortcutRepositoryTest, DrawerViewModelTest (+1 more)

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.06
Nodes (8): ProfileEntity, Flow, ProfileRepository, toCsv(), ProfileEntityTest, FakeProfileDao, Flow, ProfileRepositoryTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.15
Nodes (25): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT (+17 more)

### Community 28 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 29 - "ProfileCarouselViewModel.kt"
Cohesion: 0.14
Nodes (4): StateFlow, ViewModel, ProfileCarouselUiState, ProfileCarouselViewModel

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 34 - ".refresh"
Cohesion: 0.16
Nodes (8): Callback, Callback, flattenIcon(), Bitmap, Flow, Callback, Drawable, UserHandle

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.22
Nodes (20): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), AppsSection(), ClickableRow() (+12 more)

### Community 40 - "ExportBackupUseCase.kt"
Cohesion: 0.14
Nodes (12): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, toBackupEntry(), toBackupProfile() (+4 more)

### Community 43 - "AppIcon"
Cohesion: 0.50
Nodes (7): AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 44 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 45 - "DefaultFavoriteAppRepository"
Cohesion: 0.17
Nodes (4): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity()

### Community 46 - "HubGrid"
Cohesion: 0.12
Nodes (24): AppContextMenu(), Modifier, Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar() (+16 more)

### Community 47 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 53 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.26
Nodes (3): DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AppRepository"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "SettingsRepository"
Cohesion: 0.04
Nodes (20): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, NotificationAccessRepository, SettingsRepository, UsageAccessRepository (+12 more)

### Community 58 - "LauncherAppWidgetHost"
Cohesion: 0.15
Nodes (9): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 59 - ".setContent"
Cohesion: 0.13
Nodes (12): BackupRestoreScreenTest, ExportBackupUseCase, ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion (+4 more)

### Community 60 - "AppearanceSettingsViewModelTest.kt"
Cohesion: 0.25
Nodes (3): DefaultAppRepository, SelectPreviewAppsUseCase, Intent

### Community 61 - "ClockFontOption"
Cohesion: 0.04
Nodes (40): Converters, T, resolveOverride(), BackupWidgetPlacement, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS (+32 more)

### Community 62 - "HomeAppsListSettingsViewModel"
Cohesion: 0.24
Nodes (4): HomeAppsListSettingsViewModel, HomeAppsListUiState, StateFlow, ViewModel

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 66 - "HomeViewModel"
Cohesion: 0.16
Nodes (4): HomeViewModel, StateFlow, ViewModel, HomeViewModelTest

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "BackupRestoreViewModel"
Cohesion: 0.06
Nodes (27): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+19 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.32
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "LumenDatabase"
Cohesion: 0.22
Nodes (6): DatabaseModule, Context, LumenDatabase, Migrations, Migration, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "HomeDrawerRoute"
Cohesion: 0.06
Nodes (50): androidx, dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier (+42 more)

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.19
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel() (+6 more)

### Community 75 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 81 - "NotificationSettingsViewModel"
Cohesion: 0.36
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 82 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.08
Nodes (31): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+23 more)

### Community 87 - "LumenNotificationListenerService"
Cohesion: 0.43
Nodes (3): LumenNotificationListenerService, NotificationListenerService, StatusBarNotification

### Community 88 - "CalendarInfo"
Cohesion: 0.21
Nodes (4): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.27
Nodes (16): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent() (+8 more)

### Community 91 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 94 - "ContactConnectionType"
Cohesion: 0.33
Nodes (6): ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER, WHATSAPP

### Community 95 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 102 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.09
Nodes (24): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+16 more)

### Community 105 - ".setContent"
Cohesion: 0.16
Nodes (5): HubScreenTest, DeleteWidgetUseCase, ObserveHubStateUseCase, DeleteWidgetUseCaseTest, ObserveHubStateUseCaseTest

### Community 107 - "LauncherViewModel"
Cohesion: 0.14
Nodes (8): CleanUpUninstalledAppsUseCase, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, CleanUpUninstalledAppsUseCaseTest, LauncherViewModelTest

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 111 - "DockAppEntity"
Cohesion: 0.30
Nodes (3): DockAppEntity, toDockAppEntity(), DockAppDaoTest

### Community 112 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 113 - "ManageProfilesScreen.kt"
Cohesion: 0.51
Nodes (9): AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList() (+1 more)

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 119 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 135 - "FontWeightOption"
Cohesion: 0.12
Nodes (17): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, AddProfilePage() (+9 more)

### Community 137 - "AppInfo"
Cohesion: 0.14
Nodes (7): AppInfo, GetInstalledAppsUseCase, Flow, GroupAppsByLetterUseCase, AppDrawerScreenGridPreview(), GetInstalledAppsUseCaseTest, GroupAppsByLetterUseCaseTest

### Community 138 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 139 - "CardDivider"
Cohesion: 0.20
Nodes (18): Modifier, T, LabeledDropdownRow(), CardDivider(), Modifier, SettingsCard(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel() (+10 more)

### Community 141 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 143 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 144 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (11): KeyboardDismissalTest, ContactPermissionRepository, AppModule, Context, NotificationShadeRepository, T, RankBySearchRelevanceUseCase, NotificationShadeRepositoryTest (+3 more)

### Community 149 - "DockSettingsViewModel"
Cohesion: 0.43
Nodes (4): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

## Knowledge Gaps
- **247 isolated node(s):** `1. Overview`, `2. Goals`, `3. Non-Goals (v1)`, `3a. Parked for Future Consideration`, `F1. Home clock widget + calendar integration` (+242 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 514 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HomeScreen`, `SettingsScreenTest.kt`, `.setContent`, `LauncherFontOption`, `.setContent`, `FontWeightOption`, `ContactConnection`, `AppInfo`, `CalendarSettingsScreen.kt`, `DrawerGridSize`, `CardDivider`, `DockSettingsScreen.kt`, `.setContent`, `.setContent`, `HomeDrawerRouteTest.kt`, `LauncherActivity.kt`, `WallpaperAccentRole`, `.setContent`, `DrawerViewModel`, `HomeScreenTest`, `AppDrawerScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `ProfileSettingsScreen.kt`, `.setContent`, `DefaultFavoriteAppRepository`, `AccentSwatch`, `.setContent`, `AppDrawerScreen`, `DockAppPickerScreen.kt`, `SettingsRepository`, `LauncherAppWidgetHost`, `.setContent`, `AppearanceSettingsViewModelTest.kt`, `ClockFontOption`, `.setContent`, `SettingsScreen.kt`, `BackupRestoreViewModel`, `ColorTest`, `HomeDrawerRoute`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `.setContent`, `CalendarInfo`, `AppearanceSettingsScreen.kt`, `Type.kt`, `HubWidgetPickerViewModel`, `.setContent`, `ClockAdjustSheet.kt`, `WidgetResizeHandle`, `StickyHeaderLayout`, `ManageProfilesScreen.kt`, `BackButton`, `.rendersOneEntryPerLetterProvided`, `.setContent`?**
  _High betweenness centrality (0.181) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `SettingsScreenTest.kt`, `FavoriteAppRepository`, `.createViewModel`, `.setContent`, `FontWeightOption`, `DrawerGridSize`, `DockSettingsScreen.kt`, `LauncherActivity.kt`, `HomeDrawerRouteTest.kt`, `WallpaperAccentRole`, `.setContent`, `LauncherSettings`, `DockSettingsViewModel`, `DrawerViewModel`, `HomeScreenTest`, `AppDrawerScreen.kt`, `ProfileCarouselViewModel.kt`, `.setContent`, `.refresh`, `ProfileSettingsScreen.kt`, `DockAppRepository`, `AppIcon`, `DefaultFavoriteAppRepository`, `HubGrid`, `AppDrawerScreen`, `FakeDefaultFavoriteAppDao`, `AppRepository`, `DockAppPickerScreen.kt`, `SettingsRepository`, `AppearanceSettingsViewModelTest.kt`, `ClockFontOption`, `HomeAppsListSettingsViewModel`, `.setContent`, `SettingsScreen.kt`, `FakeDockAppDao`, `LumenNavHost`, `HomeAppsListSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `.createViewModel`, `AppearanceSettingsScreen.kt`, `SettingsViewModelTest`, `Fixture`, `LauncherViewModel`?**
  _High betweenness centrality (0.114) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `.setContent`, `LauncherFontOption`, `.createViewModel`, `FontWeightOption`, `DrawerGridSize`, `.setContent`, `.setContent`, `HomeDrawerRouteTest.kt`, `ClockColorOption`, `WallpaperAccentRole`, `.setContent`, `LauncherSettings`, `DockSettingsViewModel`, `DrawerViewModel`, `ProfileEntity`, `AppDrawerScreen.kt`, `CalendarSettingsViewModel`, `ProfileCarouselViewModel.kt`, `.setContent`, `.setContent`, `.createViewModel`, `.setContent`, `ExportBackupUseCase.kt`, `ManageProfilesViewModel`, `DefaultFavoriteAppRepository`, `.setContent`, `ClockStyleGalleryViewModelTest`, `.createViewModel`, `.setContent`, `AppearanceSettingsViewModelTest.kt`, `ClockFontOption`, `HomeAppsListSettingsViewModel`, `.setContent`, `HomeViewModel`, `.setContent`, `.createViewModel`, `NotificationSettingsViewModel`, `AppDrawerSettingsViewModelTest`, `CalendarInfo`, `SettingsRepositoryTest.kt`, `SettingsRepositoryTest`, `ExportBackupUseCaseTest.kt`, `LauncherViewModel`, `NotificationAccessExplanationViewModel.kt`, `.setContent`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `HomeScreenTextOnlyPresentationPreview()`) actually correct?**
  _`LumenLauncherTheme()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `1. Overview`, `2. Goals`, `3. Non-Goals (v1)` to the rest of the system?**
  _247 weakly-connected nodes found - possible documentation gaps or missing edges._