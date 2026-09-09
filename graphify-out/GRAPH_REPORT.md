# Graph Report - lumen-launcher  (2026-09-09)

## Corpus Check
- 288 files · ~688,455 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2861 nodes · 7670 edges · 136 communities (96 shown, 37 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 529 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `6da84449`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HubViewModel
- design_handoff_minimal_launcher/support.js
- SettingsScreenTest.kt
- ContactConnection
- ClockFontOption
- HomeDrawerRoute
- ProfileEntity
- Android launcher design planning/support.js
- AppRepository
- CalendarPermissionRepository
- FontWeightOption
- SettingsRepository.kt
- AppWidgetRepository
- WidgetPlacementRepository
- WidgetProviderOption
- LauncherAppWidgetHost
- Screens
- WallpaperAccentRole
- WidgetPlacementEntity
- FavoriteAppRepository
- LauncherSettings
- LumenLauncherTheme
- AppShortcutRepository
- 4. Feature Requirements
- SettingsRepository
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- ProfileCarouselUiState
- DockAppEntity
- Row
- design_handoff_minimal_launcher/support.js
- .setContent
- .setContent
- NotificationBadgeRepository
- AppRowPresentation
- ClockTemplateId
- ClockAlignment
- ProfileSettingsScreen.kt
- LauncherViewModel
- BackupRestoreViewModel
- ContactRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- Proposed Changes
- .setContent
- .createViewModel
- ObserveProfilePreviewsUseCase.kt
- Clock Widget Resize — Implementation Spec
- .setContent
- AppDrawerScreen
- ClockStyleGalleryViewModel
- BackupMapping.kt
- DefaultFavoriteAppRepository
- AccentSwatch
- BackupRestoreViewModelTest
- DockAppPickerScreen.kt
- HomeDrawerRouteTest.kt
- AppInfo
- ComponentName
- .setContent
- ImportBackupUseCase.kt
- DrawerViewModelTest
- .setContent
- SettingsScreen.kt
- FakeDockAppDao
- ObserveHomeScreenStateUseCase.kt
- github.md
- 4. Feature Requirements
- ColorTest
- LumenNavHost
- DockAppDao
- letterAt
- .setContent
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Screens
- WidgetPlacementDao
- LauncherFontOption
- ProfileCarouselScreen.kt
- ProfileCarouselViewModel.kt
- DefaultAppRepository
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Fixture
- ProfileDao
- AppearanceSettingsScreen.kt
- PRD: Minimal Android Launcher
- EnsureActiveProfileUseCase
- DockSettingsScreen.kt
- .createViewModel
- Handoff: Minimal Android Launcher
- SelectPreviewAppsUseCaseTest
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- StickyHeaderLayout
- DrawerViewModel
- .setContent
- .setContent
- rememberDragReorderState
- HubWidgetPickerViewModel
- .setContent
- combine
- GroupAppsByLetterUseCaseTest
- ContactConnectionsSheet.kt
- ClockAdjustSheet.kt
- AppContextMenuTest
- ObserveHubStateUseCase
- AssignCalendarColorsUseCaseTest
- HubWidgetTile
- Design Tokens
- LumenDatabaseMigrationTest
- CalendarSettingsScreen.kt
- BackButton
- rememberTickingNow
- ProfileEntityTest
- FontWeightSlider
- PermissionKind
- UsageAccessExplanationViewModel
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DockAppRepository
- AlphabetRail
- .rendersOneEntryPerLetterProvided
- Type.kt
- ClockCornerHandle
- CardDivider

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 203 edges
2. `AppInfo` - 146 edges
3. `ProfileEntity` - 143 edges
4. `SettingsRepository` - 125 edges
5. `Row` - 105 edges
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

## Communities (136 total, 37 thin omitted)

### Community 0 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 3 - "ContactConnection"
Cohesion: 0.16
Nodes (13): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+5 more)

### Community 4 - "ClockFontOption"
Cohesion: 0.07
Nodes (12): Converters, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM (+4 more)

### Community 5 - "HomeDrawerRoute"
Cohesion: 0.06
Nodes (46): dashedBorder(), Color, Dp, Modifier, ClockAdjustMode, ADJUST, MENU, NONE (+38 more)

### Community 6 - "ProfileEntity"
Cohesion: 0.06
Nodes (7): ProfileEntity, Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "AppRepository"
Cohesion: 0.06
Nodes (13): ProfileSettingsScreenTest, DockSettingsScreenTest, AppRepository, any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest (+5 more)

### Community 9 - "CalendarPermissionRepository"
Cohesion: 0.07
Nodes (12): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase (+4 more)

### Community 10 - "FontWeightOption"
Cohesion: 0.14
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 11 - "SettingsRepository.kt"
Cohesion: 0.06
Nodes (27): DataStoreModule, Context, DockDisplayMode, ICONS, TEXT, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX (+19 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (13): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+5 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "WidgetProviderOption"
Cohesion: 0.18
Nodes (18): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+10 more)

### Community 15 - "LauncherAppWidgetHost"
Cohesion: 0.15
Nodes (9): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (17): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+9 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.15
Nodes (8): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - "FavoriteAppRepository"
Cohesion: 0.06
Nodes (15): DatabaseModule, Context, FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, LumenDatabase (+7 more)

### Community 20 - "LauncherSettings"
Cohesion: 0.11
Nodes (13): T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, LauncherSettings, ListContentMode, FAVORITES (+5 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.15
Nodes (3): ClockBlockTest, ClockBlock(), LumenLauncherTheme()

### Community 22 - "AppShortcutRepository"
Cohesion: 0.25
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.06
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, SettingsRepository, StateFlow, ViewModel (+1 more)

### Community 25 - "HomeScreen"
Cohesion: 0.15
Nodes (3): HomeScreenTest, HomeScreen(), com

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.21
Nodes (21): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), DrawerAppRow() (+13 more)

### Community 29 - "DockAppEntity"
Cohesion: 0.25
Nodes (3): DockAppEntity, DockAppDaoTest, ExportBackupUseCaseTest

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

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.09
Nodes (15): Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow (+7 more)

### Community 35 - "AppRowPresentation"
Cohesion: 0.12
Nodes (7): AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY, StateFlow, ViewModel, ProfileSettingsViewModel

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "ClockAlignment"
Cohesion: 0.16
Nodes (16): CalendarEvent, ClockAlignment, CENTER, LEFT, RIGHT, CalendarEventsBlock(), EventRow(), Color (+8 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "LauncherViewModel"
Cohesion: 0.23
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.10
Nodes (26): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+18 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (17): AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+9 more)

### Community 44 - "Proposed Changes"
Cohesion: 0.08
Nodes (25): 1. Data Model & Persistence, 2. UI State & Preview, 3. Rendering Mechanism, 4. UI Components (Sheet & Handles), 5. Home Screen Integration, 6. Alignment & Clamping Logic, Automated Tests, Implementation Plan — Clock Widget Resize (+17 more)

### Community 47 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.25
Nodes (5): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 49 - ".setContent"
Cohesion: 0.19
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 51 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 52 - "BackupMapping.kt"
Cohesion: 0.17
Nodes (13): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+5 more)

### Community 53 - "DefaultFavoriteAppRepository"
Cohesion: 0.05
Nodes (13): HomeAppsListSettingsScreenTest, DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, HomeAppsListSettingsViewModel, StateFlow (+5 more)

### Community 54 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.17
Nodes (12): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+4 more)

### Community 57 - "HomeDrawerRouteTest.kt"
Cohesion: 0.06
Nodes (17): KeyboardDismissalTest, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, CleanUpUninstalledAppsUseCase (+9 more)

### Community 58 - "AppInfo"
Cohesion: 0.18
Nodes (7): AppInfo, GetInstalledAppsUseCase, Flow, AppDrawerScreenGridPreview(), HomeScreenTextOnlyPresentationPreview(), GetInstalledAppsUseCaseTest, LauncherViewModelTest

### Community 59 - "ComponentName"
Cohesion: 0.27
Nodes (5): HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, ComponentName

### Community 61 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

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

### Community 68 - "4. Feature Requirements"
Cohesion: 0.13
Nodes (15): 4. Feature Requirements, F10. App Drawer Overlay, F11. Label Customization (icon customization parked), F12. Long-Press Context Menu [decided, in scope], F13. Notification Badges [decided, in scope], F14. Backup & Restore [decided, in scope], F1. Home clock widget + calendar integration, F2. Home screen — Favorites / Recents / Most Used (+7 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.32
Nodes (7): ClockStyleGalleryRoute(), ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 73 - ".setContent"
Cohesion: 0.09
Nodes (6): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 75 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 76 - "Screens"
Cohesion: 0.13
Nodes (15): App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Favorites variants, First run (`4f`–`4h`), Home (`1a`), Launcher Hub (`4a`–`4e`) (+7 more)

### Community 78 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 79 - "ProfileCarouselScreen.kt"
Cohesion: 0.13
Nodes (31): Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), HubGrid(), AppWidgetHostView, Context (+23 more)

### Community 80 - "ProfileCarouselViewModel.kt"
Cohesion: 0.28
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 81 - "DefaultAppRepository"
Cohesion: 0.21
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.18
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 88 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.35
Nodes (14): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+6 more)

### Community 90 - "PRD: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 5. Gesture Map, 6. Additional Considerations Still Open, 7. Permissions Summary (+3 more)

### Community 91 - "EnsureActiveProfileUseCase"
Cohesion: 0.20
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 92 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 94 - "Handoff: Minimal Android Launcher"
Cohesion: 0.18
Nodes (11): About the Design Files, Assets, Fidelity, Files, Handoff: Minimal Android Launcher, Interactions & Behavior, Open questions, Overview (+3 more)

### Community 99 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 100 - "DrawerViewModel"
Cohesion: 0.21
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 101 - ".setContent"
Cohesion: 0.16
Nodes (9): BackupRestoreScreenTest, BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings() (+1 more)

### Community 103 - "rememberDragReorderState"
Cohesion: 0.19
Nodes (10): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), Dp, Modifier, WidgetResizeHandle() (+2 more)

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 106 - "combine"
Cohesion: 0.16
Nodes (13): combine(), Flow, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, T1, T2 (+5 more)

### Community 108 - "ContactConnectionsSheet.kt"
Cohesion: 0.50
Nodes (7): ConnectionRow(), ContactConnectionsSheet(), fallbackIcon(), androidx, Modifier, subtitle(), ImageVector

### Community 109 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 113 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 114 - "Design Tokens"
Cohesion: 0.33
Nodes (6): Accent handling, Dark (`Launcher Dark.dc.html`), Design Tokens, Light (`Launcher.dc.html`), Spacing, radius, shadow, Type

### Community 116 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 118 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 120 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 121 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 123 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "AlphabetRail"
Cohesion: 0.83
Nodes (3): AlphabetRail(), Modifier, magnifyScale()

### Community 135 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 139 - "CardDivider"
Cohesion: 0.19
Nodes (19): Modifier, T, LabeledDropdownRow(), CardDivider(), Modifier, SettingsCard(), ClockPositionResetRow(), clockStyleGalleryDisplayLabel() (+11 more)

## Knowledge Gaps
- **316 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+311 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 580 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **37 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `SettingsScreenTest.kt`, `ContactConnection`, `HomeDrawerRoute`, `.rendersOneEntryPerLetterProvided`, `Type.kt`, `AppRepository`, `CalendarPermissionRepository`, `FontWeightOption`, `CardDivider`, `WidgetProviderOption`, `LauncherAppWidgetHost`, `WallpaperAccentRole`, `WidgetPlacementEntity`, `AppShortcutRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `.setContent`, `ClockAlignment`, `ProfileSettingsScreen.kt`, `LauncherViewModel`, `BackupRestoreViewModel`, `HomeScreen.kt`, `.setContent`, `.setContent`, `AppDrawerScreen`, `ClockStyleGalleryViewModel`, `DefaultFavoriteAppRepository`, `AccentSwatch`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `AppInfo`, `.setContent`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `LauncherFontOption`, `ProfileCarouselScreen.kt`, `DefaultAppRepository`, `AppearanceSettingsScreen.kt`, `DockSettingsScreen.kt`, `StickyHeaderLayout`, `DrawerViewModel`, `.setContent`, `.setContent`, `rememberDragReorderState`, `.setContent`, `ContactConnectionsSheet.kt`, `ClockAdjustSheet.kt`, `AppContextMenuTest`, `CalendarSettingsScreen.kt`, `BackButton`, `FontWeightSlider`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `SettingsScreenTest.kt`, `DockAppRepository`, `HomeDrawerRoute`, `AppRepository`, `WallpaperAccentRole`, `FavoriteAppRepository`, `LauncherSettings`, `AppShortcutRepository`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `NotificationBadgeRepository`, `AppRowPresentation`, `ClockAlignment`, `ProfileSettingsScreen.kt`, `LauncherViewModel`, `HomeScreen.kt`, `.setContent`, `.createViewModel`, `ObserveProfilePreviewsUseCase.kt`, `AppDrawerScreen`, `DefaultFavoriteAppRepository`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `.setContent`, `SettingsScreen.kt`, `FakeDockAppDao`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `ProfileCarouselScreen.kt`, `ProfileCarouselViewModel.kt`, `DefaultAppRepository`, `Fixture`, `AppearanceSettingsScreen.kt`, `DockSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `DrawerViewModel`, `combine`, `GroupAppsByLetterUseCaseTest`, `AppContextMenuTest`?**
  _High betweenness centrality (0.098) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `SettingsScreenTest.kt`, `ClockFontOption`, `ProfileEntity`, `AppRepository`, `CalendarPermissionRepository`, `FontWeightOption`, `SettingsRepository.kt`, `WallpaperAccentRole`, `LauncherSettings`, `DockAppEntity`, `AppRowPresentation`, `ClockAlignment`, `LauncherViewModel`, `.setContent`, `.createViewModel`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `ClockStyleGalleryViewModel`, `BackupMapping.kt`, `DefaultFavoriteAppRepository`, `HomeDrawerRouteTest.kt`, `AppInfo`, `.setContent`, `ImportBackupUseCase.kt`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `LauncherFontOption`, `ProfileCarouselViewModel.kt`, `DefaultAppRepository`, `AppDrawerSettingsViewModelTest`, `EnsureActiveProfileUseCase`, `.createViewModel`, `SettingsRepositoryTest`, `DrawerViewModel`, `.setContent`, `.setContent`, `combine`?**
  _High betweenness centrality (0.064) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 100 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 100 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _316 weakly-connected nodes found - possible documentation gaps or missing edges._