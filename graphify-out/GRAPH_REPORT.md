# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 291 files · ~690,346 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2875 nodes · 7724 edges · 142 communities (100 shown, 39 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 543 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a5aac352`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HomeDrawerRoute
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- FavoriteAppRepository
- LauncherFontOption
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- ContactRepositoryTest
- ListContentMode
- ProfileDaoTest
- DrawerListItemSize
- AppWidgetRepository
- WidgetPlacementRepository
- ClockColorOption
- SettingsRepository
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- .setContent
- LauncherSettings
- LumenLauncherTheme
- DrawerViewModel
- 4. Feature Requirements
- FavoriteAppDaoTest
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- ProfileCarouselScreen.kt
- ProfileCarouselUiState
- Row
- design_handoff_minimal_launcher/support.js
- .setContent
- WidgetProviderOption
- .refresh
- .setContent
- ClockTemplateId
- ObserveProfilePreviewsUseCase.kt
- ProfileSettingsScreen.kt
- AppRepository
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- ManageProfilesViewModel
- ProfileEntity
- HubGrid
- CalendarEventsBlock.kt
- Clock Widget Resize — Implementation Spec
- .setContent
- AppDrawerScreen
- ClockStyleGalleryViewModelTest
- BackupMapping.kt
- DefaultFavoriteAppRepository
- AccentSwatch
- FavoriteAppDao
- DockAppPickerScreen.kt
- HomeDrawerRouteTest.kt
- LauncherAppWidgetHost
- ImportBackupUseCase.kt
- SettingsRepository.kt
- ClockFontOption
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
- DockSettingsScreen.kt
- LabeledDropdownRow
- Screens
- .setContent
- ClockAlignment
- NotificationBadgeRepository
- DockSettingsViewModel
- DefaultAppRepositoryTest
- .setContent
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- LauncherViewModel
- CalendarInfo
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- EnsureActiveProfileUseCase
- BackupRestoreContent
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- NotificationSettingsViewModel
- ProfileSettingsViewModel.kt
- Fixture
- .setContent
- PlaceWidgetUseCaseTest
- HubWidgetPickerViewModel
- .setContent
- AppearanceSettingsViewModelTest.kt
- AppInfo
- HubAddWidgetEvent
- ClockAdjustSheet.kt
- WidgetResizeHandle
- AppContextMenuTest
- StickyHeaderLayout
- ManageProfilesScreen.kt
- Design Tokens
- LumenDatabaseMigrationTest
- HomeAppsListSettingsScreen.kt
- BackButton
- combine
- NotificationAccessExplanationViewModel.kt
- .rendersOneEntryPerLetterProvided
- PermissionKind
- .setContent
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .setContent
- .createViewModel
- .setContent
- ProfileCarouselViewModel.kt
- FontWeightOption
- FontWeightSlider
- GroupAppsByLetterUseCaseTest
- CardDivider
- .setContent
- HubWidgetTile
- UsageAccessExplanationViewModel

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
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (142 total, 39 thin omitted)

### Community 0 - "HomeDrawerRoute"
Cohesion: 0.06
Nodes (46): dashedBorder(), Color, Dp, Modifier, ClockAdjustMode, ADJUST, MENU, NONE (+38 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "FavoriteAppRepository"
Cohesion: 0.13
Nodes (6): FavoriteAppRepository, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 4 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 5 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 6 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (30): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+22 more)

### Community 9 - "ListContentMode"
Cohesion: 0.06
Nodes (20): T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT, RIGHT (+12 more)

### Community 11 - "DrawerListItemSize"
Cohesion: 0.09
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.12
Nodes (7): Flow, WidgetPlacementDao, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockColorOption"
Cohesion: 0.08
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 19 - ".setContent"
Cohesion: 0.10
Nodes (6): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 20 - "LauncherSettings"
Cohesion: 0.16
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - "DrawerViewModel"
Cohesion: 0.16
Nodes (7): AppShortcutRepository, AppShortcut, DrawerViewModel, StateFlow, ViewModel, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 25 - "HomeScreen"
Cohesion: 0.15
Nodes (3): HomeScreenTest, HomeScreen(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.25
Nodes (20): NotificationBadgeStyle, COUNT, DOT, GroupedApps, ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder() (+12 more)

### Community 28 - "ProfileCarouselScreen.kt"
Cohesion: 0.49
Nodes (9): AddProfilePage(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselScreen(), ProfileCarouselScreenPreview(), ProfilePreviewPage() (+1 more)

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 31 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "WidgetProviderOption"
Cohesion: 0.20
Nodes (14): HubWidgetPickerScreenTest, WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow() (+6 more)

### Community 34 - ".refresh"
Cohesion: 0.12
Nodes (11): Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, Callback, Drawable (+3 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.25
Nodes (5): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.21
Nodes (21): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), AppsSection(), ClickableRow() (+13 more)

### Community 39 - "AppRepository"
Cohesion: 0.18
Nodes (5): DockSettingsScreenTest, AppRepository, AppRepositoryTest, LauncherActivityInfo, LauncherApps

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.11
Nodes (16): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+8 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.21
Nodes (18): AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+10 more)

### Community 44 - "ManageProfilesViewModel"
Cohesion: 0.16
Nodes (6): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, ManageProfilesViewModelTest

### Community 45 - "ProfileEntity"
Cohesion: 0.08
Nodes (7): ProfileEntity, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest, Flow

### Community 46 - "HubGrid"
Cohesion: 0.19
Nodes (17): Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), HubGrid(), AppWidgetHostView (+9 more)

### Community 47 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 52 - "BackupMapping.kt"
Cohesion: 0.11
Nodes (21): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, T (+13 more)

### Community 53 - "DefaultFavoriteAppRepository"
Cohesion: 0.10
Nodes (8): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 54 - "AccentSwatch"
Cohesion: 0.18
Nodes (11): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+3 more)

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "HomeDrawerRouteTest.kt"
Cohesion: 0.07
Nodes (17): KeyboardDismissalTest, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, ContactPermissionRepository, NotificationAccessRepository (+9 more)

### Community 58 - "LauncherAppWidgetHost"
Cohesion: 0.20
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.43
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "SettingsRepository.kt"
Cohesion: 0.18
Nodes (9): DataStoreModule, Context, DockDisplayMode, ICONS, TEXT, Keys, Flow, DataStore (+1 more)

### Community 61 - "ClockFontOption"
Cohesion: 0.07
Nodes (9): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+1 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppRepository"
Cohesion: 0.07
Nodes (10): DockAppRepository, Flow, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow (+2 more)

### Community 66 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.09
Nodes (9): NotificationShadeRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest (+1 more)

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
Cohesion: 0.13
Nodes (8): DatabaseModule, Context, LumenDatabase, Migrations, Flow, ProfileDao, Migration, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 74 - "DockSettingsScreen.kt"
Cohesion: 0.19
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DockAppsRow(), DockClickableRow(), dockDisplayLabel() (+6 more)

### Community 75 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+4 more)

### Community 76 - "Screens"
Cohesion: 0.13
Nodes (15): App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Favorites variants, First run (`4f`–`4h`), Home (`1a`), Launcher Hub (`4a`–`4e`) (+7 more)

### Community 78 - "ClockAlignment"
Cohesion: 0.18
Nodes (6): ClockAlignment, CENTER, LEFT, RIGHT, Alignment, resolve()

### Community 79 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 80 - "DockSettingsViewModel"
Cohesion: 0.20
Nodes (5): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 82 - ".setContent"
Cohesion: 0.17
Nodes (6): BackupRestoreScreenTest, ImportBackupUseCase, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.18
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 87 - "LauncherViewModel"
Cohesion: 0.23
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 88 - "CalendarInfo"
Cohesion: 0.05
Nodes (21): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent() (+13 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

### Community 90 - "PRD: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 5. Gesture Map, 6. Additional Considerations Still Open, 7. Permissions Summary (+3 more)

### Community 91 - "EnsureActiveProfileUseCase"
Cohesion: 0.20
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 92 - "BackupRestoreContent"
Cohesion: 0.38
Nodes (10): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+2 more)

### Community 94 - "Handoff: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): About the Design Files, Assets, Fidelity, Files, Handoff: Minimal Android Launcher, Interactions & Behavior, Open questions, Overview (+3 more)

### Community 99 - "NotificationSettingsViewModel"
Cohesion: 0.36
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 100 - "ProfileSettingsViewModel.kt"
Cohesion: 0.20
Nodes (3): StateFlow, ViewModel, ProfileSettingsViewModel

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.13
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 105 - ".setContent"
Cohesion: 0.24
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 106 - "AppearanceSettingsViewModelTest.kt"
Cohesion: 0.25
Nodes (3): DefaultAppRepository, Intent, SelectPreviewAppsUseCase

### Community 107 - "AppInfo"
Cohesion: 0.16
Nodes (6): AppInfo, GetInstalledAppsUseCase, Flow, GroupAppsByLetterUseCase, GetInstalledAppsUseCaseTest, LauncherViewModelTest

### Community 108 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 110 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 112 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (8): Modifier, StickyHeaderLayout(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier

### Community 113 - "ManageProfilesScreen.kt"
Cohesion: 0.51
Nodes (9): AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList() (+1 more)

### Community 114 - "Design Tokens"
Cohesion: 0.33
Nodes (6): Accent handling, Dark (`Launcher Dark.dc.html`), Design Tokens, Light (`Launcher.dc.html`), Spacing, radius, shadow, Type

### Community 116 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

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

### Community 134 - "ProfileCarouselViewModel.kt"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 135 - "FontWeightOption"
Cohesion: 0.10
Nodes (13): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontFamily (+5 more)

### Community 136 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 139 - "CardDivider"
Cohesion: 0.26
Nodes (15): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+7 more)

### Community 148 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 151 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

## Knowledge Gaps
- **298 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+293 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 567 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **39 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `HomeDrawerRoute`, `SettingsScreenTest.kt`, `.setContent`, `LauncherFontOption`, `.setContent`, `.setContent`, `FontWeightOption`, `ContactRepositoryTest`, `FontWeightSlider`, `CardDivider`, `.setContent`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `ProfileCarouselScreen.kt`, `.setContent`, `WidgetProviderOption`, `.setContent`, `ProfileSettingsScreen.kt`, `AppRepository`, `HomeScreen.kt`, `.setContent`, `AppDrawerScreen`, `AccentSwatch`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `LauncherAppWidgetHost`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `.setContent`, `DockSettingsScreen.kt`, `LabeledDropdownRow`, `.setContent`, `.setContent`, `LauncherViewModel`, `CalendarInfo`, `AppearanceSettingsScreen.kt`, `BackupRestoreContent`, `.setContent`, `.setContent`, `AppearanceSettingsViewModelTest.kt`, `AppInfo`, `ClockAdjustSheet.kt`, `WidgetResizeHandle`, `AppContextMenuTest`, `StickyHeaderLayout`, `ManageProfilesScreen.kt`, `HomeAppsListSettingsScreen.kt`, `BackButton`, `.rendersOneEntryPerLetterProvided`, `.setContent`?**
  _High betweenness centrality (0.164) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `.setContent`, `LauncherFontOption`, `.createViewModel`, `ProfileCarouselViewModel.kt`, `FontWeightOption`, `ListContentMode`, `DrawerListItemSize`, `.setContent`, `ClockColorOption`, `WallpaperAccentRole`, `.setContent`, `LauncherSettings`, `DrawerViewModel`, `.setContent`, `ObserveProfilePreviewsUseCase.kt`, `AppRepository`, `ManageProfilesViewModel`, `ProfileEntity`, `.setContent`, `ClockStyleGalleryViewModelTest`, `BackupMapping.kt`, `DefaultFavoriteAppRepository`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `SettingsRepository.kt`, `ClockFontOption`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `.setContent`, `ClockAlignment`, `DockSettingsViewModel`, `.setContent`, `AppDrawerSettingsViewModelTest`, `LauncherViewModel`, `CalendarInfo`, `EnsureActiveProfileUseCase`, `.createViewModel`, `SettingsRepositoryTest`, `NotificationSettingsViewModel`, `ProfileSettingsViewModel.kt`, `.setContent`, `AppearanceSettingsViewModelTest.kt`, `AppInfo`, `NotificationAccessExplanationViewModel.kt`, `.setContent`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `HomeDrawerRoute`, `SettingsScreenTest.kt`, `FavoriteAppRepository`, `.createViewModel`, `.setContent`, `ProfileCarouselViewModel.kt`, `ContactRepositoryTest`, `ListContentMode`, `GroupAppsByLetterUseCaseTest`, `WallpaperAccentRole`, `.setContent`, `LauncherSettings`, `LumenLauncherTheme`, `DrawerViewModel`, `HomeScreen`, `AppDrawerScreen.kt`, `ProfileCarouselScreen.kt`, `.refresh`, `ObserveProfilePreviewsUseCase.kt`, `ProfileSettingsScreen.kt`, `AppRepository`, `HomeScreen.kt`, `AppDrawerScreen`, `DefaultFavoriteAppRepository`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `SettingsRepository.kt`, `.setContent`, `SettingsScreen.kt`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `DockSettingsScreen.kt`, `DockSettingsViewModel`, `LauncherViewModel`, `AppearanceSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `ProfileSettingsViewModel.kt`, `Fixture`, `AppearanceSettingsViewModelTest.kt`, `AppContextMenuTest`, `HomeAppsListSettingsScreen.kt`?**
  _High betweenness centrality (0.074) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _298 weakly-connected nodes found - possible documentation gaps or missing edges._