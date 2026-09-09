# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 287 files · ~689,427 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2862 nodes · 7670 edges · 148 communities (102 shown, 43 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 606 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `01c0cd01`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HubGrid
- design_handoff_minimal_launcher/support.js
- .setContent
- FavoriteAppRepository
- LauncherFontOption
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ContactRepositoryTest
- HomeDrawerRouteTest.kt
- ProfileDao
- AppRowPosition
- AppWidgetRepository
- WidgetPlacementRepository
- SettingsRepository
- HubWidgetPickerViewModel
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- .setContent
- ProfileEntity
- LumenLauncherTheme
- AppShortcut
- 4. Feature Requirements
- FavoriteAppEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- ProfileCarouselScreen.kt
- ProfileCarouselUiState
- Row
- design_handoff_minimal_launcher/support.js
- .setContent
- HubWidgetPickerScreen.kt
- NotificationBadgeRepository
- .setContent
- ClockTemplateId
- ProfileCarouselViewModel.kt
- ProfileSettingsScreen.kt
- AppRepository
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- PermissionsScreen.kt
- ProfileRepository
- PaddingValues
- CalendarEvent
- Clock Widget Resize — Implementation Spec
- HomeViewModel
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- ImportBackupUseCaseTest.kt
- DefaultFavoriteAppRepository
- AccentSwatch
- FavoriteAppDao
- StickyHeaderLayout
- CalendarPermissionRepository
- .setContent
- ImportBackupUseCase.kt
- DockSettingsScreen.kt
- ClockFontOption
- RankBySearchRelevanceUseCase
- .setContent
- SettingsScreen.kt
- DockAppRepository
- ObserveHomeScreenStateUseCase.kt
- github.md
- 4. Feature Requirements
- ColorTest
- LumenNavHost
- LumenDatabase
- letterAt
- .setContent
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Screens
- .setContent
- AppWidgetRepository.kt
- BackupMapping.kt
- DockDisplayMode
- DefaultAppRepository
- .setContent
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- LauncherActivity.kt
- CalendarRepository
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- SettingsRepositoryTest.kt
- rememberTickingNow
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- NotificationSettingsViewModel
- DrawerViewModel.kt
- BackupRepository
- .setContent
- .createViewModel
- ComponentName
- .setContent
- .setContent
- GetInstalledAppsUseCase
- HubAddWidgetEvent
- ClockAdjustSheet.kt
- WidgetResizeHandle
- WidgetPlacementDao
- .hydrate
- HubGridConstants.kt
- Design Tokens
- LumenDatabaseMigrationTest
- CalendarSettingsScreen.kt
- BackButton
- combine
- NotificationAccessRepository
- .rendersOneEntryPerLetterProvided
- PermissionKind
- .setContent
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- HubUiState.kt
- HomeAppsListSettingsViewModel
- AppWidgetProviderInfo
- Bitmap
- Type.kt
- .setContent
- AppInfo
- Flow
- CardDivider
- Intent
- IntentSender
- .setContent
- SharedFlow
- StateFlow
- Dp
- UsageAccessExplanationViewModel
- HomeDrawerRoute

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 200 edges
2. `AppInfo` - 144 edges
3. `ProfileEntity` - 143 edges
4. `SettingsRepository` - 123 edges
5. `Row` - 106 edges
6. `ProfileRepository` - 95 edges
7. `LauncherSettings` - 88 edges
8. `ClockTemplateId` - 73 edges
9. `ClockDateStyle` - 61 edges
10. `HomeScreen()` - 56 edges

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

## Communities (148 total, 43 thin omitted)

### Community 0 - "HubGrid"
Cohesion: 0.06
Nodes (48): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+40 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - ".setContent"
Cohesion: 0.11
Nodes (7): SettingsScreenTest, DefaultLauncherRepository, ObserveSettingsScreenStateUseCase, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "FavoriteAppRepository"
Cohesion: 0.18
Nodes (3): FavoriteAppRepository, FakeFavoriteAppDao, FavoriteAppRepositoryTest

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.26
Nodes (3): HubViewModel, ViewModel, WidgetPlacementEntity

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (29): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+21 more)

### Community 9 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (7): AppModule, Context, AppWidgetHostView, Context, AppOpsManager, AppWidgetManager, UsageStatsManager

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "AppRowPosition"
Cohesion: 0.08
Nodes (17): AppRowPosition, LEFT, RIGHT, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR (+9 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.13
Nodes (4): AppWidgetRepository, AppWidgetRepositoryTest, AppWidgetProviderInfo, IntentSender

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 14 - "SettingsRepository"
Cohesion: 0.03
Nodes (26): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, FontWeightOption, EXTRA_LIGHT, LIGHT (+18 more)

### Community 15 - "HubWidgetPickerViewModel"
Cohesion: 0.21
Nodes (8): AddFailureReason, HubWidgetPickerViewModel, ViewModel, WidgetProviderOption, HubAddWidgetEvent, HubWidgetPickerUiState, SharedFlow, StateFlow

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.09
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (11): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase (+3 more)

### Community 19 - ".setContent"
Cohesion: 0.12
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 20 - "ProfileEntity"
Cohesion: 0.09
Nodes (11): ProfileEntity, LauncherSettings, HomeUiState, ProfileEntityTest, ExportBackupUseCaseTest, Fixture, ObserveHomeScreenStateUseCaseTest, Fixture (+3 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.14
Nodes (3): ClockBlockTest, ClockBlock(), LumenLauncherTheme()

### Community 22 - "AppShortcut"
Cohesion: 0.14
Nodes (5): AppContextMenuTest, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "FavoriteAppEntity"
Cohesion: 0.23
Nodes (3): FavoriteAppEntity, Flow, FavoriteAppDaoTest

### Community 25 - "HomeScreen"
Cohesion: 0.15
Nodes (3): HomeScreenTest, HomeScreen(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.20
Nodes (24): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT, GroupedApps (+16 more)

### Community 28 - "ProfileCarouselScreen.kt"
Cohesion: 0.30
Nodes (15): AddProfilePage(), AddProfileRow(), CarouselHeaderTitleRow(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselMode (+7 more)

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 31 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "HubWidgetPickerScreen.kt"
Cohesion: 0.29
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

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

### Community 39 - "AppRepository"
Cohesion: 0.17
Nodes (5): DockAppPickerScreenTest, AppRepository, AppRepositoryTest, LauncherActivityInfo, LauncherApps

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.10
Nodes (26): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+18 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.26
Nodes (17): AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+9 more)

### Community 44 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

### Community 45 - "ProfileRepository"
Cohesion: 0.06
Nodes (6): Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 46 - "PaddingValues"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar() (+3 more)

### Community 47 - "CalendarEvent"
Cohesion: 0.32
Nodes (10): CalendarEvent, CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, ClockBlockPreview() (+2 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "HomeViewModel"
Cohesion: 0.15
Nodes (5): NotificationShadeRepository, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest

### Community 52 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.21
Nodes (10): BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, toBackupProfile(), ExportBackupUseCase, Uri (+2 more)

### Community 53 - "DefaultFavoriteAppRepository"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 56 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier (+1 more)

### Community 57 - "CalendarPermissionRepository"
Cohesion: 0.14
Nodes (9): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, UsageAccessRepository, StateFlow, ViewModel (+1 more)

### Community 58 - ".setContent"
Cohesion: 0.17
Nodes (9): KeyboardDismissalTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 61 - "ClockFontOption"
Cohesion: 0.04
Nodes (25): Converters, T, resolveOverride(), ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS (+17 more)

### Community 62 - "RankBySearchRelevanceUseCase"
Cohesion: 0.23
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppRepository"
Cohesion: 0.08
Nodes (9): DockAppRepository, DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow (+1 more)

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
Nodes (8): DatabaseModule, Context, DefaultFavoriteAppDao, Flow, LumenDatabase, Migrations, Migration, RoomDatabase

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

### Community 78 - "AppWidgetRepository.kt"
Cohesion: 0.28
Nodes (6): AppWidgetHostView, Context, WidgetProviderOption, toBitmap(), Bitmap, Flow

### Community 79 - "BackupMapping.kt"
Cohesion: 0.29
Nodes (7): T, toBackupEntry(), toBackupPlacement(), toDefaultFavoriteAppEntity(), toEnumOrDefault(), toFavoriteAppEntity(), toProfileEntity()

### Community 80 - "DockDisplayMode"
Cohesion: 0.15
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 81 - "DefaultAppRepository"
Cohesion: 0.23
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 82 - ".setContent"
Cohesion: 0.12
Nodes (6): BackupRestoreScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, PlaceWidgetUseCaseTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.18
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 87 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 88 - "CalendarRepository"
Cohesion: 0.07
Nodes (11): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow (+3 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.26
Nodes (17): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard() (+9 more)

### Community 90 - "PRD: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 5. Gesture Map, 6. Additional Considerations Still Open, 7. Permissions Summary (+3 more)

### Community 91 - "SettingsRepositoryTest.kt"
Cohesion: 0.15
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 92 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 94 - "Handoff: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): About the Design Files, Assets, Fidelity, Files, Handoff: Minimal Android Launcher, Interactions & Behavior, Open questions, Overview (+3 more)

### Community 99 - "NotificationSettingsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, NotificationSettingsViewModel

### Community 100 - "DrawerViewModel.kt"
Cohesion: 0.23
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 101 - "BackupRepository"
Cohesion: 0.31
Nodes (3): BackupRepository, Uri, BackupRepositoryTest

### Community 104 - "ComponentName"
Cohesion: 0.17
Nodes (3): HubWidgetPickerViewModelTest, ComponentName, Intent

### Community 107 - "GetInstalledAppsUseCase"
Cohesion: 0.12
Nodes (10): CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel (+2 more)

### Community 108 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

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
Cohesion: 0.11
Nodes (16): Flow, combine(), Flow, Flow, SettingsScreenState, DockAppPickerUiState, DockAppPickerViewModel, StateFlow (+8 more)

### Community 119 - "NotificationAccessRepository"
Cohesion: 0.27
Nodes (5): FakeNotificationAccessRepository, NotificationAccessRepository, StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 121 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "HomeAppsListSettingsViewModel"
Cohesion: 0.13
Nodes (4): HomeAppsListSettingsViewModel, StateFlow, ViewModel, HomeAppsListSettingsViewModelTest

### Community 135 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 137 - "AppInfo"
Cohesion: 0.17
Nodes (6): AppInfo, GroupAppsByLetterUseCase, AppDrawerScreenGridPreview(), HomeScreenTextOnlyPresentationPreview(), GetInstalledAppsUseCaseTest, GroupAppsByLetterUseCaseTest

### Community 139 - "CardDivider"
Cohesion: 0.25
Nodes (15): CardDivider(), Modifier, SettingsCard(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+7 more)

### Community 151 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 156 - "HomeDrawerRoute"
Cohesion: 0.14
Nodes (16): ClockAdjustMode, ADJUST, MENU, NONE, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute() (+8 more)

## Knowledge Gaps
- **302 isolated node(s):** `START`, `END`, `HubFull`, `Multiple`, `Single` (+297 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 575 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HubGrid`, `.setContent`, `LauncherFontOption`, `.setContent`, `Type.kt`, `ContactRepositoryTest`, `.setContent`, `AppInfo`, `CardDivider`, `.setContent`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `.setContent`, `AppShortcut`, `HomeScreen`, `AppDrawerScreen.kt`, `ProfileCarouselScreen.kt`, `.setContent`, `HubWidgetPickerScreen.kt`, `.setContent`, `ProfileSettingsScreen.kt`, `AppRepository`, `BackupRestoreViewModel`, `HomeScreen.kt`, `PermissionsScreen.kt`, `CalendarEvent`, `AppDrawerScreen`, `AccentSwatch`, `StickyHeaderLayout`, `CalendarPermissionRepository`, `.setContent`, `DockSettingsScreen.kt`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `LumenDatabase`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `.setContent`, `LauncherActivity.kt`, `CalendarRepository`, `AppearanceSettingsScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `ClockAdjustSheet.kt`, `WidgetResizeHandle`, `CalendarSettingsScreen.kt`, `BackButton`, `.rendersOneEntryPerLetterProvided`, `.setContent`?**
  _High betweenness centrality (0.160) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `.setContent`, `FavoriteAppRepository`, `HomeAppsListSettingsViewModel`, `.setContent`, `ContactRepositoryTest`, `HomeDrawerRouteTest.kt`, `WallpaperAccentRole`, `.setContent`, `ProfileEntity`, `AppShortcut`, `FavoriteAppEntity`, `HomeScreen`, `AppDrawerScreen.kt`, `HomeDrawerRoute`, `ProfileCarouselScreen.kt`, `NotificationBadgeRepository`, `ProfileCarouselViewModel.kt`, `ProfileSettingsScreen.kt`, `AppRepository`, `HomeScreen.kt`, `CalendarEvent`, `AppDrawerScreen`, `DefaultFavoriteAppRepository`, `StickyHeaderLayout`, `.setContent`, `DockSettingsScreen.kt`, `ClockFontOption`, `.setContent`, `SettingsScreen.kt`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `DockDisplayMode`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `DrawerViewModel.kt`, `GetInstalledAppsUseCase`, `.hydrate`, `combine`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `.setContent`, `LauncherFontOption`, `HomeAppsListSettingsViewModel`, `.setContent`, `HomeDrawerRouteTest.kt`, `AppRowPosition`, `.setContent`, `WallpaperAccentRole`, `.setContent`, `ProfileEntity`, `.setContent`, `ProfileCarouselViewModel.kt`, `AppRepository`, `HomeViewModel`, `ClockStyleGalleryViewModelTest`, `ImportBackupUseCaseTest.kt`, `CalendarPermissionRepository`, `.setContent`, `ImportBackupUseCase.kt`, `ClockFontOption`, `RankBySearchRelevanceUseCase`, `.setContent`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `LumenDatabase`, `.setContent`, `.setContent`, `DockDisplayMode`, `.setContent`, `AppDrawerSettingsViewModelTest`, `CalendarRepository`, `SettingsRepositoryTest.kt`, `.createViewModel`, `SettingsRepositoryTest`, `NotificationSettingsViewModel`, `DrawerViewModel.kt`, `.setContent`, `.createViewModel`, `GetInstalledAppsUseCase`, `combine`, `NotificationAccessRepository`, `.setContent`?**
  _High betweenness centrality (0.070) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.aFailedAddShowsAnErrorMessageInsteadOfSilentlyDoingNothing()` and `.setContent()`) actually correct?**
  _`LumenLauncherTheme()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `AppInfo` (e.g. with `HomeDrawerRouteTest` and `KeyboardDismissalTest`) actually correct?**
  _`AppInfo` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 2 inferred relationships involving `SettingsRepository` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`SettingsRepository` has 2 INFERRED edges - model-reasoned connections that need verification._