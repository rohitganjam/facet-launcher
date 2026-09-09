# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 287 files · ~688,518 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2853 nodes · 7639 edges · 159 communities (110 shown, 46 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 539 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `11b0a8dd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- PaddingValues
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- ContactConnection
- LauncherFontOption
- FavoriteAppRepository
- HubViewModel
- Android launcher design planning/support.js
- ContactRepositoryTest
- CalendarRepository
- ProfileDaoTest
- SettingsRepositoryTest.kt
- AppWidgetRepository
- WidgetPlacementRepository
- ClockColorOption
- .setContent
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- UsageStatsRepository
- LauncherSettings
- LumenLauncherTheme
- AppShortcutRepository
- 4. Feature Requirements
- SettingsRepository
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- .setContent
- ProfileCarouselUiState
- Row
- design_handoff_minimal_launcher/support.js
- .setContent
- .setContent
- .refresh
- .setContent
- ClockTemplateId
- ProfileCarouselViewModel.kt
- ProfileSettingsScreen.kt
- AppRepository
- BackupRestoreViewModel
- DefaultFavoriteAppRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- StickyHeaderLayout
- ProfileEntity
- DefaultFavoriteAppDao
- CalendarEvent
- Clock Widget Resize — Implementation Spec
- HomeViewModel
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- BackupMapping.kt
- DefaultFavoriteAppEntity
- AccentSwatch
- DockAppRepository
- DockAppPickerScreen.kt
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
- LumenDatabase
- letterAt
- .setContent
- HomeAppsListSettingsScreen.kt
- LabeledDropdownRow
- Screens
- .setContent
- .setContent
- Modifier
- DockSettingsViewModel
- DefaultAppRepository
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- LauncherViewModel
- CalendarSettingsViewModel
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- ProfileDao
- CalendarPermissionRepository
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- .createViewModel
- DrawerViewModel
- ImportBackupUseCaseTest.kt
- .setContent
- rememberTickingNow
- HubWidgetPickerViewModel
- .setContent
- WidgetResizeHandle
- AppInfo
- ClockStyleGalleryScreen.kt
- ClockAdjustSheet.kt
- ClockAdjustMode
- NotificationBadgeRepository
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
- .rendersOneEntryPerLetterProvided
- homeAppLabelShadow
- Type.kt
- ClockCornerHandle
- GroupAppsByLetterUseCaseTest
- LumenNotificationListenerService
- CardDivider
- FavoriteAppEntity
- NotificationSettingsViewModel
- .setContent
- PermissionsViewModel
- AssignCalendarColorsUseCaseTest
- .createViewModel
- .setContent
- HubContent
- HubWidgetTile
- AppRepository.kt
- InheritOverrideCard
- UsageAccessExplanationViewModel
- ProfileEntityTest
- ContactConnectionsSheetTest
- HubAtCapacityStrip.kt
- HubEmptyState.kt
- HomeDrawerRoute
- HubHeader
- ClockColors.kt

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 202 edges
2. `AppInfo` - 144 edges
3. `ProfileEntity` - 138 edges
4. `SettingsRepository` - 125 edges
5. `Row` - 106 edges
6. `ProfileRepository` - 97 edges
7. `LauncherSettings` - 88 edges
8. `ClockTemplateId` - 71 edges
9. `WidgetPlacementEntity` - 61 edges
10. `ClockDateStyle` - 59 edges

## Surprising Connections (you probably didn't know these)
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
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

## Communities (159 total, 46 thin omitted)

### Community 0 - "PaddingValues"
Cohesion: 0.16
Nodes (18): Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), HubGrid(), AppWidgetHostView, Context (+10 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "ContactConnection"
Cohesion: 0.16
Nodes (19): ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER (+11 more)

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - "FavoriteAppRepository"
Cohesion: 0.13
Nodes (5): FavoriteAppRepository, Flow, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 6 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 9 - "CalendarRepository"
Cohesion: 0.16
Nodes (6): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo

### Community 11 - "SettingsRepositoryTest.kt"
Cohesion: 0.08
Nodes (17): DockDisplayMode, ICONS, TEXT, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR (+9 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.10
Nodes (11): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+3 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 14 - "ClockColorOption"
Cohesion: 0.09
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 15 - ".setContent"
Cohesion: 0.11
Nodes (13): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, HubFull (+5 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.14
Nodes (9): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+1 more)

### Community 19 - "UsageStatsRepository"
Cohesion: 0.17
Nodes (6): AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, AppOpsManager, UsageStatsManager

### Community 20 - "LauncherSettings"
Cohesion: 0.20
Nodes (4): LauncherSettings, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.15
Nodes (3): ClockBlockTest, ClockBlock(), LumenLauncherTheme()

### Community 22 - "AppShortcutRepository"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.05
Nodes (17): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, ClockAlignment (+9 more)

### Community 25 - "HomeScreen"
Cohesion: 0.15
Nodes (4): HomeScreenTest, HomeScreen(), HomeScreenTextOnlyPresentationPreview(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.19
Nodes (22): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip() (+14 more)

### Community 28 - ".setContent"
Cohesion: 0.08
Nodes (32): ProfileCarouselScreenTest, AddProfilePage(), AddProfileRow(), CarouselHeaderTitleRow(), LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselMode (+24 more)

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
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 34 - ".refresh"
Cohesion: 0.21
Nodes (5): Callback, Callback, Flow, Callback, UserHandle

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "ProfileCarouselViewModel.kt"
Cohesion: 0.20
Nodes (6): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "AppRepository"
Cohesion: 0.19
Nodes (8): AppRepository, CleanUpUninstalledAppsUseCase, any(), AppRepositoryTest, eq(), T, CleanUpUninstalledAppsUseCaseTest, LauncherActivityInfo

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.06
Nodes (27): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+19 more)

### Community 41 - "DefaultFavoriteAppRepository"
Cohesion: 0.24
Nodes (3): DefaultFavoriteAppRepository, Flow, LauncherApps

### Community 43 - "HomeScreen.kt"
Cohesion: 0.21
Nodes (20): NotificationBadgeStyle, COUNT, DOT, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel() (+12 more)

### Community 44 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 45 - "ProfileEntity"
Cohesion: 0.06
Nodes (6): ProfileEntity, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 47 - "CalendarEvent"
Cohesion: 0.26
Nodes (10): CalendarEvent, CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, ClockBlockPreview() (+2 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "HomeViewModel"
Cohesion: 0.24
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 52 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 53 - "DefaultFavoriteAppEntity"
Cohesion: 0.17
Nodes (4): DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 55 - "DockAppRepository"
Cohesion: 0.15
Nodes (3): DockSettingsScreenTest, DockAppRepository, Flow

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "NotificationAccessRepository"
Cohesion: 0.18
Nodes (5): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository

### Community 58 - "HomeDrawerRouteTest.kt"
Cohesion: 0.13
Nodes (7): KeyboardDismissalTest, NotificationShadeRepository, ObserveHubStateUseCase, T, RankBySearchRelevanceUseCase, NotificationShadeRepositoryTest, ObserveHubStateUseCaseTest

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 61 - "ClockFontOption"
Cohesion: 0.04
Nodes (32): Converters, T, resolveOverride(), ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS (+24 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.11
Nodes (7): DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

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
Cohesion: 0.32
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 71 - "LumenDatabase"
Cohesion: 0.12
Nodes (8): DatabaseModule, Context, LumenDatabase, Migrations, Flow, WidgetPlacementDao, Migration, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.21
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+6 more)

### Community 75 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+4 more)

### Community 76 - "Screens"
Cohesion: 0.13
Nodes (15): App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Favorites variants, First run (`4f`–`4h`), Home (`1a`), Launcher Hub (`4a`–`4e`) (+7 more)

### Community 80 - "DockSettingsViewModel"
Cohesion: 0.18
Nodes (5): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 81 - "DefaultAppRepository"
Cohesion: 0.21
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.18
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 87 - "LauncherViewModel"
Cohesion: 0.23
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 88 - "CalendarSettingsViewModel"
Cohesion: 0.20
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

### Community 90 - "PRD: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 5. Gesture Map, 6. Additional Considerations Still Open, 7. Permissions Summary (+3 more)

### Community 91 - "ProfileDao"
Cohesion: 0.10
Nodes (10): DataStoreModule, Context, Flow, ProfileDao, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow (+2 more)

### Community 92 - "CalendarPermissionRepository"
Cohesion: 0.23
Nodes (3): CalendarPermissionRepository, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 94 - "Handoff: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): About the Design Files, Assets, Fidelity, Files, Handoff: Minimal Android Launcher, Interactions & Behavior, Open questions, Overview (+3 more)

### Community 100 - "DrawerViewModel"
Cohesion: 0.29
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 101 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.17
Nodes (9): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest (+1 more)

### Community 103 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.07
Nodes (26): HubWidgetPickerScreenTest, WidgetProviderOption, calculateHubCellWidth(), Context, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview() (+18 more)

### Community 106 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 107 - "AppInfo"
Cohesion: 0.13
Nodes (7): DockAppPickerScreenTest, AppInfo, GetInstalledAppsUseCase, Flow, AppDrawerScreenGridPreview(), GetInstalledAppsUseCaseTest, LauncherViewModelTest

### Community 108 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.36
Nodes (9): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+1 more)

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 111 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 114 - "Design Tokens"
Cohesion: 0.33
Nodes (6): Accent handling, Dark (`Launcher Dark.dc.html`), Design Tokens, Light (`Launcher.dc.html`), Spacing, radius, shadow, Type

### Community 116 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 119 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 121 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

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

### Community 138 - "LumenNotificationListenerService"
Cohesion: 0.43
Nodes (3): LumenNotificationListenerService, NotificationListenerService, StatusBarNotification

### Community 139 - "CardDivider"
Cohesion: 0.38
Nodes (9): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+1 more)

### Community 140 - "FavoriteAppEntity"
Cohesion: 0.12
Nodes (5): FavoriteAppDao, Flow, FavoriteAppEntity, FavoriteAppDaoTest, ExportBackupUseCaseTest

### Community 141 - "NotificationSettingsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, NotificationSettingsViewModel

### Community 143 - "PermissionsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, PermissionsViewModel

### Community 147 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 148 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 149 - "AppRepository.kt"
Cohesion: 0.60
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 150 - "InheritOverrideCard"
Cohesion: 0.90
Nodes (4): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow()

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
- **300 isolated node(s):** `SWITCH`, `MANAGE`, `AddFailed`, `LaunchBindPermission`, `LaunchConfigure` (+295 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 565 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `SettingsScreenTest.kt`, `ContactConnection`, `dashedBorder`, `.rendersOneEntryPerLetterProvided`, `LauncherFontOption`, `Type.kt`, `CalendarRepository`, `SettingsRepositoryTest.kt`, `CardDivider`, `.setContent`, `.setContent`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `.setContent`, `AppShortcutRepository`, `SettingsRepository`, `HomeScreen`, `ContactConnectionsSheetTest`, `AppDrawerScreen.kt`, `.setContent`, `HubAtCapacityStrip.kt`, `HubEmptyState.kt`, `HubHeader`, `.setContent`, `.setContent`, `.setContent`, `ProfileSettingsScreen.kt`, `BackupRestoreViewModel`, `DefaultFavoriteAppRepository`, `HomeScreen.kt`, `StickyHeaderLayout`, `CalendarEvent`, `AppDrawerScreen`, `AccentSwatch`, `DockAppRepository`, `DockAppPickerScreen.kt`, `NotificationAccessRepository`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `LabeledDropdownRow`, `.setContent`, `.setContent`, `DefaultAppRepository`, `LauncherViewModel`, `AppearanceSettingsScreen.kt`, `.setContent`, `HubWidgetPickerViewModel`, `.setContent`, `WidgetResizeHandle`, `AppInfo`, `ClockStyleGalleryScreen.kt`, `ClockAdjustSheet.kt`, `CalendarSettingsScreen.kt`, `BackButton`, `AppContextMenuTest`, `.setContent`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `SettingsScreenTest.kt`, `.createViewModel`, `FavoriteAppRepository`, `GroupAppsByLetterUseCaseTest`, `SettingsRepositoryTest.kt`, `WallpaperAccentRole`, `UsageStatsRepository`, `LauncherSettings`, `AppRepository.kt`, `AppShortcutRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `HomeDrawerRoute`, `.setContent`, `.refresh`, `ProfileCarouselViewModel.kt`, `ProfileSettingsScreen.kt`, `AppRepository`, `DefaultFavoriteAppRepository`, `HomeScreen.kt`, `CalendarEvent`, `AppDrawerScreen`, `DefaultFavoriteAppEntity`, `DockAppRepository`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `ClockFontOption`, `.setContent`, `SettingsScreen.kt`, `DockAppEntity`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `DockSettingsViewModel`, `DefaultAppRepository`, `LauncherViewModel`, `AppearanceSettingsScreen.kt`, `CalendarPermissionRepository`, `.createViewModel`, `SelectPreviewAppsUseCaseTest`, `DrawerViewModel`, `Fixture`, `AppContextMenuTest`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `LauncherFontOption`, `.createViewModel`, `CalendarRepository`, `SettingsRepositoryTest.kt`, `FavoriteAppEntity`, `NotificationSettingsViewModel`, `.setContent`, `.setContent`, `ClockColorOption`, `WallpaperAccentRole`, `.setContent`, `PermissionsViewModel`, `LauncherSettings`, `.createViewModel`, `.setContent`, `.setContent`, `ProfileCarouselViewModel.kt`, `AppRepository`, `DefaultFavoriteAppRepository`, `ProfileEntity`, `HomeViewModel`, `ClockStyleGalleryViewModelTest`, `DockAppRepository`, `NotificationAccessRepository`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `ClockFontOption`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `.setContent`, `.setContent`, `DockSettingsViewModel`, `DefaultAppRepository`, `AppDrawerSettingsViewModelTest`, `LauncherViewModel`, `CalendarSettingsViewModel`, `ProfileDao`, `CalendarPermissionRepository`, `.createViewModel`, `SettingsRepositoryTest`, `.createViewModel`, `DrawerViewModel`, `ImportBackupUseCaseTest.kt`, `.setContent`, `NotificationAccessExplanationViewModel.kt`, `.setContent`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `ProfileManageScreenPreview()`) actually correct?**
  _`LumenLauncherTheme()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `SWITCH`, `MANAGE`, `AddFailed` to the rest of the system?**
  _300 weakly-connected nodes found - possible documentation gaps or missing edges._