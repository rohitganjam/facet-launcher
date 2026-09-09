# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 287 files · ~689,154 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2845 nodes · 7667 edges · 150 communities (104 shown, 43 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 536 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `fc4d9005`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HubGrid
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- ContactConnection
- LauncherFontOption
- FavoriteAppRepository
- HubViewModel
- Android launcher design planning/support.js
- ContactRepositoryTest
- CalendarPermissionRepository
- ProfileDao
- DrawerGridSize
- AppWidgetRepository
- WidgetPlacementRepository
- ClockColorOption
- .setContent
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- .setContent
- LauncherSettings
- LumenLauncherTheme
- AppShortcutRepository
- 4. Feature Requirements
- SettingsRepository
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- ProfileCarouselScreen.kt
- ProfileCarouselUiState
- Row
- design_handoff_minimal_launcher/support.js
- .setContent
- .setContent
- NotificationBadgeRepository
- .setContent
- ClockTemplateId
- ProfileCarouselViewModel.kt
- ProfileSettingsScreen.kt
- AppRepositoryTest.kt
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- Manrope Font License (SIL OFL 1.1)
- AppInfo
- CardDivider
- ProfileEntity
- FontWeightOption
- CalendarEventsBlock.kt
- Clock Widget Resize — Implementation Spec
- HomeViewModel
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- BackupMapping.kt
- DefaultFavoriteAppRepository
- AccentSwatch
- DockAppRepository
- StickyHeaderLayout
- NotificationAccessRepository
- HomeDrawerRouteTest.kt
- ImportBackupUseCase.kt
- DockSettingsScreen.kt
- ClockFontOption
- DrawerViewModelTest
- .setContent
- SettingsScreen.kt
- DockAppEntity
- ObserveHomeScreenStateUseCase.kt
- github.md
- 4. Feature Requirements
- ColorTest
- LumenNavHost
- AppRepository
- letterAt
- .setContent
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Screens
- .setContent
- .setContent
- FakeDockAppDao
- DockDisplayMode
- DefaultAppRepository
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- LauncherActivity.kt
- CalendarSettingsViewModel
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- SettingsRepositoryTest.kt
- Fixture
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- .createViewModel
- DrawerViewModel
- ExportBackupUseCase.kt
- .setContent
- BackupRestoreContent
- HubWidgetPickerViewModel
- .setContent
- ContactConnectionType
- GetInstalledAppsUseCase
- BackupRestoreMessage
- ClockAdjustSheet.kt
- ClockAdjustMode
- WidgetPlacementDao
- Fixture
- CalendarRepositoryTest
- Design Tokens
- LumenDatabaseMigrationTest
- CalendarSettingsScreen.kt
- BackButton
- combine
- NotificationAccessExplanationViewModel.kt
- AppContextMenuTest
- PermissionKind
- .setContent
- ContactRepository
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- dashedBorder
- .createViewModel
- .setContent
- homeAppLabelShadow
- Type.kt
- .setContent
- GroupAppsByLetterUseCaseTest
- Migrations
- LabeledDropdownRow
- .setContent
- HubContent
- HubWidgetTile
- UsageAccessExplanationViewModel
- ProfileEntityTest
- HubAtCapacityStrip.kt
- HubEmptyState.kt
- HomeDrawerRoute
- HubHeader
- ClockColors.kt

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 203 edges
2. `AppInfo` - 146 edges
3. `ProfileEntity` - 143 edges
4. `SettingsRepository` - 125 edges
5. `Row` - 106 edges
6. `ProfileRepository` - 97 edges
7. `LauncherSettings` - 88 edges
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
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (150 total, 43 thin omitted)

### Community 0 - "HubGrid"
Cohesion: 0.16
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "ContactConnection"
Cohesion: 0.25
Nodes (11): ContactConnectionsSheetTest, ConnectionOption, ContactConnection, ConnectionRow(), ContactConnectionsSheet(), ContactConnectionsSheetPreview(), fallbackIcon(), androidx (+3 more)

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - "FavoriteAppRepository"
Cohesion: 0.05
Nodes (22): FavoritesPickerScreenTest, FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, FavoritesPickerContent(), FavoritesPickerHeader() (+14 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactRepositoryTest"
Cohesion: 0.23
Nodes (3): eq(), ContactRepositoryTest, MatrixCursor

### Community 9 - "CalendarPermissionRepository"
Cohesion: 0.13
Nodes (7): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, CalendarInfo

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "DrawerGridSize"
Cohesion: 0.09
Nodes (14): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerPresentation, GRID, LIST (+6 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 14 - "ClockColorOption"
Cohesion: 0.07
Nodes (15): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+7 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (11): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase (+3 more)

### Community 19 - ".setContent"
Cohesion: 0.09
Nodes (7): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, AppOpsManager, UsageStatsManager

### Community 20 - "LauncherSettings"
Cohesion: 0.21
Nodes (4): LauncherSettings, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.11
Nodes (8): AlphabetRailTest, ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "AppShortcutRepository"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 25 - "HomeScreen"
Cohesion: 0.14
Nodes (5): HomeScreenTest, ClockCornerHandle(), Modifier, HomeScreen(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.16
Nodes (25): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase (+17 more)

### Community 28 - "ProfileCarouselScreen.kt"
Cohesion: 0.20
Nodes (22): Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), AddProfilePage(), AddProfileRow(), CarouselHeaderTitleRow() (+14 more)

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 31 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - ".setContent"
Cohesion: 0.14
Nodes (18): HubWidgetPickerScreenTest, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+10 more)

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.09
Nodes (15): Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow (+7 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "ProfileCarouselViewModel.kt"
Cohesion: 0.20
Nodes (6): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "AppRepositoryTest.kt"
Cohesion: 0.31
Nodes (4): any(), AppRepositoryTest, T, LauncherActivityInfo

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 43 - "AppInfo"
Cohesion: 0.17
Nodes (21): AppInfo, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier (+13 more)

### Community 44 - "CardDivider"
Cohesion: 0.39
Nodes (10): CardDivider(), Modifier, SettingsCard(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen() (+2 more)

### Community 45 - "ProfileEntity"
Cohesion: 0.06
Nodes (6): ProfileEntity, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 46 - "FontWeightOption"
Cohesion: 0.14
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 47 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "HomeViewModel"
Cohesion: 0.24
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 52 - "BackupMapping.kt"
Cohesion: 0.17
Nodes (13): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+5 more)

### Community 53 - "DefaultFavoriteAppRepository"
Cohesion: 0.10
Nodes (8): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 55 - "DockAppRepository"
Cohesion: 0.14
Nodes (3): DockAppRepository, Flow, CleanUpUninstalledAppsUseCaseTest

### Community 56 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier (+1 more)

### Community 57 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (9): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, StateFlow, ViewModel, PermissionsViewModel (+1 more)

### Community 58 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (16): KeyboardDismissalTest, Context, WidgetModule, NotificationShadeRepository, UsageAccessRepository, AppWidgetProviderInfo, IntentSender, SharedFlow (+8 more)

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 61 - "ClockFontOption"
Cohesion: 0.04
Nodes (34): Converters, T, resolveOverride(), ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS (+26 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.13
Nodes (5): DockAppDao, Flow, DockAppEntity, DockAppDaoTest, ExportBackupUseCaseTest

### Community 66 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.21
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "4. Feature Requirements"
Cohesion: 0.13
Nodes (15): 4. Feature Requirements, F10. App Drawer Overlay, F11. Label Customization (icon customization parked), F12. Long-Press Context Menu [decided, in scope], F13. Notification Badges [decided, in scope], F14. Backup & Restore [decided, in scope], F1. Home clock widget + calendar integration, F2. Home screen — Favorites / Recents / Most Used (+7 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.22
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen() (+5 more)

### Community 71 - "AppRepository"
Cohesion: 0.18
Nodes (6): AppRepository, DatabaseModule, Context, LumenDatabase, LauncherApps, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.21
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+6 more)

### Community 75 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 76 - "Screens"
Cohesion: 0.13
Nodes (15): App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Favorites variants, First run (`4f`–`4h`), Home (`1a`), Launcher Hub (`4a`–`4e`) (+7 more)

### Community 79 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 80 - "DockDisplayMode"
Cohesion: 0.15
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 81 - "DefaultAppRepository"
Cohesion: 0.23
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.18
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 87 - "LauncherActivity.kt"
Cohesion: 0.36
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 88 - "CalendarSettingsViewModel"
Cohesion: 0.12
Nodes (5): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel, AssignCalendarColorsUseCaseTest

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.26
Nodes (17): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard() (+9 more)

### Community 90 - "PRD: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 5. Gesture Map, 6. Additional Considerations Still Open, 7. Permissions Summary (+3 more)

### Community 91 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 94 - "Handoff: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): About the Design Files, Assets, Fidelity, Files, Handoff: Minimal Android Launcher, Interactions & Behavior, Open questions, Overview (+3 more)

### Community 100 - "DrawerViewModel"
Cohesion: 0.23
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 101 - "ExportBackupUseCase.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 103 - "BackupRestoreContent"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.11
Nodes (10): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel (+2 more)

### Community 106 - "ContactConnectionType"
Cohesion: 0.20
Nodes (9): ConnectionDetail, ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER, WHATSAPP, Multiple (+1 more)

### Community 107 - "GetInstalledAppsUseCase"
Cohesion: 0.17
Nodes (8): GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 108 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 114 - "Design Tokens"
Cohesion: 0.33
Nodes (6): Accent handling, Dark (`Launcher Dark.dc.html`), Design Tokens, Light (`Launcher.dc.html`), Spacing, radius, shadow, Type

### Community 116 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "combine"
Cohesion: 0.15
Nodes (13): combine(), Flow, DockAppPickerUiState, DockAppPickerViewModel, StateFlow, ViewModel, T1, T2 (+5 more)

### Community 119 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 121 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 122 - ".setContent"
Cohesion: 0.19
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview()

### Community 134 - "homeAppLabelShadow"
Cohesion: 0.50
Nodes (7): accentTonalExtremes(), homeAppLabelShadow(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme, Shadow

### Community 135 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 139 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (10): Modifier, T, LabeledDropdownRow(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+2 more)

### Community 147 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 148 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 151 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 154 - "HubAtCapacityStrip.kt"
Cohesion: 0.83
Nodes (3): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 155 - "HubEmptyState.kt"
Cohesion: 0.83
Nodes (3): HubEmptyState(), HubEmptyStatePreview(), Modifier

### Community 156 - "HomeDrawerRoute"
Cohesion: 0.19
Nodes (12): Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier, Offset (+4 more)

### Community 157 - "HubHeader"
Cohesion: 0.83
Nodes (3): HubHeader(), HubHeaderAtCapacityPreview(), Modifier

## Knowledge Gaps
- **300 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+295 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 565 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HubGrid`, `SettingsScreenTest.kt`, `ContactConnection`, `dashedBorder`, `.setContent`, `FavoriteAppRepository`, `LauncherFontOption`, `.setContent`, `CalendarPermissionRepository`, `Type.kt`, `LabeledDropdownRow`, `.setContent`, `.setContent`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `.setContent`, `AppShortcutRepository`, `HomeScreen`, `HubAtCapacityStrip.kt`, `AppDrawerScreen.kt`, `HubEmptyState.kt`, `HubHeader`, `ProfileCarouselScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `ProfileSettingsScreen.kt`, `AppInfo`, `CardDivider`, `AppDrawerScreen`, `AccentSwatch`, `StickyHeaderLayout`, `NotificationAccessRepository`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `LumenNavHost`, `AppRepository`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `.setContent`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `.setContent`, `BackupRestoreContent`, `.setContent`, `ClockAdjustSheet.kt`, `CalendarSettingsScreen.kt`, `BackButton`, `AppContextMenuTest`, `.setContent`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `SettingsScreenTest.kt`, `.createViewModel`, `FavoriteAppRepository`, `GroupAppsByLetterUseCaseTest`, `CalendarPermissionRepository`, `WallpaperAccentRole`, `.setContent`, `LumenLauncherTheme`, `AppShortcutRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `HomeDrawerRoute`, `ProfileCarouselScreen.kt`, `NotificationBadgeRepository`, `ProfileCarouselViewModel.kt`, `ProfileSettingsScreen.kt`, `AppRepositoryTest.kt`, `AppDrawerScreen`, `DefaultFavoriteAppRepository`, `DockAppRepository`, `StickyHeaderLayout`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `ClockFontOption`, `.setContent`, `SettingsScreen.kt`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `AppRepository`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `FakeDockAppDao`, `DockDisplayMode`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `Fixture`, `SelectPreviewAppsUseCaseTest`, `DrawerViewModel`, `GetInstalledAppsUseCase`, `Fixture`, `combine`, `AppContextMenuTest`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `LauncherFontOption`, `.createViewModel`, `.setContent`, `CalendarPermissionRepository`, `DrawerGridSize`, `.setContent`, `.setContent`, `ClockColorOption`, `WallpaperAccentRole`, `.setContent`, `LauncherSettings`, `.setContent`, `ClockTemplateId`, `ProfileCarouselViewModel.kt`, `FontWeightOption`, `HomeViewModel`, `ClockStyleGalleryViewModelTest`, `BackupMapping.kt`, `NotificationAccessRepository`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `ClockFontOption`, `.setContent`, `DockAppEntity`, `ObserveHomeScreenStateUseCase.kt`, `AppRepository`, `.setContent`, `.setContent`, `.setContent`, `DockDisplayMode`, `AppDrawerSettingsViewModelTest`, `CalendarSettingsViewModel`, `SettingsRepositoryTest.kt`, `.createViewModel`, `SettingsRepositoryTest`, `.createViewModel`, `DrawerViewModel`, `ExportBackupUseCase.kt`, `.setContent`, `GetInstalledAppsUseCase`, `NotificationAccessExplanationViewModel.kt`, `.setContent`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _300 weakly-connected nodes found - possible documentation gaps or missing edges._