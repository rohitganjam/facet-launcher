# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 287 files · ~688,518 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2836 nodes · 7649 edges · 136 communities (98 shown, 35 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 530 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d54a6082`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HubGrid
- design_handoff_minimal_launcher/support.js
- combine
- ContactConnection
- LauncherFontOption
- FakeFavoriteAppDao
- HubViewModel
- Android launcher design planning/support.js
- ContactRepositoryTest
- CalendarRepository
- ProfileDao
- DrawerGridSize
- ComponentName
- WidgetPlacementRepository
- FavoriteAppRepository
- AppWidgetRepository
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- BackupRestoreViewModelTest
- ProfileEntity
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
- FavoritesPickerViewModel
- NotificationBadgeRepository
- .setContent
- ClockTemplateId
- ProfileCarouselViewModel.kt
- ProfileSettingsScreen.kt
- WidgetProviderOption
- BackupRestoreViewModel
- DefaultFavoriteAppRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- StickyHeaderLayout
- ProfileRepository
- DefaultFavoriteAppDao
- CalendarEvent
- Clock Widget Resize — Implementation Spec
- HomeViewModel
- AppDrawerScreen
- ClockStyleGalleryViewModel
- BackupMapping.kt
- DefaultFavoriteAppEntity
- AccentSwatch
- ClockFontOption
- DockAppPickerScreen.kt
- CalendarPermissionRepository
- HomeDrawerRouteTest.kt
- ImportBackupUseCase.kt
- DockSettingsScreen.kt
- ListContentMode
- DrawerViewModelTest
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
- LabeledDropdownRow
- Screens
- .setContent
- LauncherViewModel
- ProfileCarouselScreen.kt
- .setContent
- DefaultAppRepository
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- LauncherActivity.kt
- FavoritesPickerScreen.kt
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- SettingsRepositoryTest.kt
- FavoriteAppDaoTest
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- .setContent
- DrawerViewModel
- ImportBackupUseCaseTest.kt
- .setContent
- rememberTickingNow
- HubWidgetPickerViewModel
- .setContent
- WidgetResizeHandle
- AppInfo
- FontWeightSlider
- ClockAdjustSheet.kt
- ClockAdjustMode
- .`a valid backup replaces every profile, dock, and default favorite, then reports what was restored`
- HubAddWidgetEvent
- ExportBackupUseCaseTest.kt
- Design Tokens
- LumenDatabaseMigrationTest
- CalendarSettingsScreen.kt
- BackButton
- AddFailureReason
- NotificationAccessExplanationViewModel.kt
- PermissionKind
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .createViewModel
- .rendersOneEntryPerLetterProvided
- Type.kt
- ClockCornerHandle
- CardDivider
- FavoriteAppDao
- .setContent
- HomeDrawerRoute

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

## Communities (136 total, 35 thin omitted)

### Community 0 - "HubGrid"
Cohesion: 0.06
Nodes (46): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+38 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "combine"
Cohesion: 0.05
Nodes (22): NotificationSettingsScreenTest, SettingsScreenTest, DefaultLauncherRepository, combine(), Flow, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState (+14 more)

### Community 3 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - "FakeFavoriteAppDao"
Cohesion: 0.24
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 6 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 9 - "CalendarRepository"
Cohesion: 0.06
Nodes (12): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel (+4 more)

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "DrawerGridSize"
Cohesion: 0.10
Nodes (14): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerPresentation, GRID, LIST (+6 more)

### Community 12 - "ComponentName"
Cohesion: 0.12
Nodes (11): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+3 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "FavoriteAppRepository"
Cohesion: 0.19
Nodes (3): FavoriteAppRepository, Flow, FavoriteAppEntity

### Community 15 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (15): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetRepository, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost (+7 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (17): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+9 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 20 - "ProfileEntity"
Cohesion: 0.10
Nodes (10): ProfileEntity, LauncherSettings, HomeUiState, ProfileEntityTest, Fixture, ObserveHomeScreenStateUseCaseTest, Fixture, ObserveProfilePreviewsUseCaseTest (+2 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.14
Nodes (3): ClockBlockTest, ClockBlock(), LumenLauncherTheme()

### Community 22 - "AppShortcutRepository"
Cohesion: 0.14
Nodes (5): AppContextMenuTest, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.04
Nodes (22): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, FontWeightOption, EXTRA_LIGHT, LIGHT (+14 more)

### Community 25 - "HomeScreen"
Cohesion: 0.15
Nodes (3): HomeScreenTest, HomeScreen(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.21
Nodes (21): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupedApps, ContactRow(), ContactsAccessStrip(), DrawerAppRow() (+13 more)

### Community 28 - ".setContent"
Cohesion: 0.13
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 31 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

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

### Community 39 - "WidgetProviderOption"
Cohesion: 0.37
Nodes (10): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+2 more)

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.10
Nodes (26): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+18 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (20): NotificationBadgeStyle, COUNT, DOT, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel() (+12 more)

### Community 44 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 45 - "ProfileRepository"
Cohesion: 0.07
Nodes (5): ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 47 - "CalendarEvent"
Cohesion: 0.32
Nodes (10): CalendarEvent, CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, ClockBlockPreview() (+2 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - "HomeViewModel"
Cohesion: 0.24
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 51 - "ClockStyleGalleryViewModel"
Cohesion: 0.08
Nodes (5): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 52 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 53 - "DefaultFavoriteAppEntity"
Cohesion: 0.24
Nodes (4): DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 55 - "ClockFontOption"
Cohesion: 0.17
Nodes (7): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.18
Nodes (12): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+4 more)

### Community 57 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (11): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow (+3 more)

### Community 58 - "HomeDrawerRouteTest.kt"
Cohesion: 0.07
Nodes (15): KeyboardDismissalTest, AppRepository, AppModule, Context, NotificationShadeRepository, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow (+7 more)

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 61 - "ListContentMode"
Cohesion: 0.04
Nodes (25): Converters, T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT (+17 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppRepository"
Cohesion: 0.05
Nodes (18): DockSettingsScreenTest, DockAppRepository, Flow, DockAppDao, Flow, DockAppEntity, DockDisplayMode, ICONS (+10 more)

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

### Community 71 - "LumenDatabase"
Cohesion: 0.13
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

### Community 78 - "LauncherViewModel"
Cohesion: 0.24
Nodes (6): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 79 - "ProfileCarouselScreen.kt"
Cohesion: 0.20
Nodes (22): Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), AddProfilePage(), AddProfileRow(), CarouselHeaderTitleRow() (+14 more)

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
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 88 - "FavoritesPickerScreen.kt"
Cohesion: 0.61
Nodes (7): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

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
Cohesion: 0.16
Nodes (6): ContactRepository, LabeledValue, ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 101 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 103 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 105 - ".setContent"
Cohesion: 0.24
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 106 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 107 - "AppInfo"
Cohesion: 0.15
Nodes (6): AppInfo, GroupAppsByLetterUseCase, AppDrawerScreenGridPreview(), HomeScreenTextOnlyPresentationPreview(), GetInstalledAppsUseCaseTest, GroupAppsByLetterUseCaseTest

### Community 108 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 112 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 114 - "Design Tokens"
Cohesion: 0.33
Nodes (6): Accent handling, Dark (`Launcher Dark.dc.html`), Design Tokens, Light (`Launcher.dc.html`), Spacing, radius, shadow, Type

### Community 116 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "AddFailureReason"
Cohesion: 0.67
Nodes (3): AddFailureReason, HUB_FULL, SETUP_CANCELLED

### Community 119 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 121 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 135 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 139 - "CardDivider"
Cohesion: 0.38
Nodes (9): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+1 more)

### Community 156 - "HomeDrawerRoute"
Cohesion: 0.19
Nodes (12): Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier, Offset (+4 more)

## Knowledge Gaps
- **300 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+295 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 563 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HubGrid`, `combine`, `ContactConnection`, `LauncherFontOption`, `.rendersOneEntryPerLetterProvided`, `Type.kt`, `CalendarRepository`, `CardDivider`, `.setContent`, `AppWidgetRepository`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `AppShortcutRepository`, `SettingsRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `ProfileSettingsScreen.kt`, `WidgetProviderOption`, `BackupRestoreViewModel`, `HomeScreen.kt`, `StickyHeaderLayout`, `CalendarEvent`, `AppDrawerScreen`, `AccentSwatch`, `DockAppPickerScreen.kt`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `.setContent`, `SettingsScreen.kt`, `DockAppRepository`, `ColorTest`, `LumenNavHost`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `LabeledDropdownRow`, `.setContent`, `ProfileCarouselScreen.kt`, `.setContent`, `LauncherActivity.kt`, `FavoritesPickerScreen.kt`, `AppearanceSettingsScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `WidgetResizeHandle`, `AppInfo`, `FontWeightSlider`, `ClockAdjustSheet.kt`, `CalendarSettingsScreen.kt`, `BackButton`?**
  _High betweenness centrality (0.159) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `combine`, `ContactConnection`, `.createViewModel`, `FakeFavoriteAppDao`, `ContactRepositoryTest`, `FavoriteAppRepository`, `WallpaperAccentRole`, `ProfileEntity`, `AppShortcutRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `HomeDrawerRoute`, `FavoritesPickerViewModel`, `NotificationBadgeRepository`, `ProfileCarouselViewModel.kt`, `ProfileSettingsScreen.kt`, `DefaultFavoriteAppRepository`, `HomeScreen.kt`, `CalendarEvent`, `AppDrawerScreen`, `DefaultFavoriteAppEntity`, `DockAppPickerScreen.kt`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `DockSettingsScreen.kt`, `ListContentMode`, `.setContent`, `SettingsScreen.kt`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `LauncherViewModel`, `ProfileCarouselScreen.kt`, `LauncherActivity.kt`, `FavoritesPickerScreen.kt`, `AppearanceSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `DrawerViewModel`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `combine`, `LauncherFontOption`, `.createViewModel`, `CalendarRepository`, `DrawerGridSize`, `.setContent`, `AppWidgetRepository`, `WallpaperAccentRole`, `ProfileEntity`, `.setContent`, `.setContent`, `ProfileCarouselViewModel.kt`, `HomeViewModel`, `ClockStyleGalleryViewModel`, `ClockFontOption`, `CalendarPermissionRepository`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `ListContentMode`, `.setContent`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `.setContent`, `LauncherViewModel`, `AppDrawerSettingsViewModelTest`, `SettingsRepositoryTest.kt`, `.createViewModel`, `SettingsRepositoryTest`, `DrawerViewModel`, `ImportBackupUseCaseTest.kt`, `.setContent`, `ExportBackupUseCaseTest.kt`, `NotificationAccessExplanationViewModel.kt`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _300 weakly-connected nodes found - possible documentation gaps or missing edges._