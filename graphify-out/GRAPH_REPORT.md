# Graph Report - lumen-launcher  (2026-09-10)

## Corpus Check
- 296 files · ~468,908 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2816 nodes · 7578 edges · 159 communities (112 shown, 44 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 531 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b7b049b7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- HomeWallpaper
- design_handoff_minimal_launcher/support.js
- ManageProfilesViewModel
- FavoriteAppRepository
- HomeDrawerRoute
- FavoritesPickerScreen.kt
- HubViewModel
- Android launcher design planning/support.js
- FontWeightOption
- AccentSwatch
- ProfileDao
- SettingsRepository.kt
- AppWidgetRepository
- WidgetPlacementEntity
- combine
- ContactConnection
- Screens
- ClockAlignment
- ResolveWidgetResizeUseCase
- .setContent
- AppRowPresentation
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
- DockAppRepository
- NotificationBadgeRepository
- WidgetProviderOption
- ClockTemplateId
- HubGrid
- ProfileSettingsScreen.kt
- WallpaperAccentRole
- ExportBackupUseCase.kt
- AppShortcutRepository
- Manrope Font License (SIL OFL 1.1)
- AppInfo
- SettingsRepositoryTest.kt
- DefaultFavoriteAppRepository
- LabeledDropdownRow
- DrawerViewModel
- Clock Widget Resize — Implementation Spec
- .setContent
- AppDrawerScreen
- ClockStyleGalleryViewModel
- .createViewModel
- eq
- .setContent
- SettingsScreenTest.kt
- DockAppPickerScreen.kt
- HomeDrawerRouteTest.kt
- LauncherAppWidgetHost
- ImportBackupUseCase.kt
- AppRepository
- EnsureActiveProfileUseCase
- LauncherActivity.kt
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
- PlaceWidgetUseCase
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- SelectPreviewAppsUseCaseTest
- ProfileSettingsViewModel.kt
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- ClockColorOption
- DockDisplayMode
- ClockFontOption
- PermissionKind
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Implementation Plan
- BackupRestoreMessage
- CardDivider
- CalendarRepository
- AppearanceSettingsScreen.kt
- DefaultAppRepositoryTest
- Lumen Launcher — Built Capabilities
- AssignCalendarColorsUseCaseTest
- DefaultFavoriteAppDao
- homeAppLabelShadow
- .setContent
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- CalendarRepositoryTest
- FavoriteAppDao
- UsageAccessExplanationScreen.kt
- Fixture
- AppContextMenuTest
- PlaceWidgetUseCaseTest
- HubWidgetPickerViewModel
- .setContent
- ContactRepository
- ObserveHubStateUseCase
- BackupRestoreContent
- LauncherFontOption
- DrawerViewModelTest
- BackupMapping.kt
- StickyHeaderLayout
- Fixture
- ClockAdjustSheet.kt
- LumenDatabaseMigrationTest
- .setContent
- BackButton
- DockAppDao
- SettingsRepository
- DockAppEntity
- Type.kt
- .setContent
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- WidgetResizeHandle
- HomeAppsListSettingsViewModel
- LauncherSettings
- ResolveWidgetResizeUseCaseTest
- Lumen Launcher — Onboarding Flow (draft)
- ProfileCarouselScreen.kt
- GroupAppsByLetterUseCaseTest
- CalendarSettingsScreen.kt
- .setContent
- DockSettingsScreen.kt
- .setContent
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- .setContent
- FavoriteAppDaoTest
- rememberTickingNow
- .setContent
- LauncherViewModel
- ProfileCarouselViewModel.kt
- .setContent
- ContactConnectionType
- ClockFonts.kt
- 2. Design tokens
- HubAddWidgetEvent
- ProfileEntityTest
- ImportBackupUseCaseTest
- 2. Gate, seeding & architecture
- 3. Screen-by-screen

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

## Communities (159 total, 44 thin omitted)

### Community 0 - "HomeWallpaper"
Cohesion: 0.33
Nodes (7): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground()

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 3 - "FavoriteAppRepository"
Cohesion: 0.12
Nodes (6): FavoriteAppRepository, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 4 - "HomeDrawerRoute"
Cohesion: 0.14
Nodes (16): ClockAdjustMode, ADJUST, MENU, NONE, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute() (+8 more)

### Community 5 - "FavoritesPickerScreen.kt"
Cohesion: 0.24
Nodes (12): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader(), FavoritesPickerUiState (+4 more)

### Community 6 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - "FontWeightOption"
Cohesion: 0.14
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 9 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 10 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 11 - "SettingsRepository.kt"
Cohesion: 0.08
Nodes (20): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+12 more)

### Community 12 - "AppWidgetRepository"
Cohesion: 0.07
Nodes (13): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+5 more)

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.11
Nodes (9): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, ResolveWidgetDropUseCase, FakeWidgetPlacementDao, Flow (+1 more)

### Community 14 - "combine"
Cohesion: 0.16
Nodes (11): combine(), Flow, Flow, SettingsScreenState, T1, T2, T3, T4 (+3 more)

### Community 15 - "ContactConnection"
Cohesion: 0.20
Nodes (14): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, Multiple, Single, ConnectionRow(), ContactConnectionsSheet() (+6 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "ClockAlignment"
Cohesion: 0.15
Nodes (16): CalendarEvent, ClockAlignment, CENTER, LEFT, RIGHT, CalendarEventsBlock(), EventRow(), Color (+8 more)

### Community 18 - "ResolveWidgetResizeUseCase"
Cohesion: 0.18
Nodes (6): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - ".setContent"
Cohesion: 0.08
Nodes (9): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, Bitmap, WallpaperRepository, UsageStatsRepositoryTest, UsageStatsManager (+1 more)

### Community 20 - "AppRowPresentation"
Cohesion: 0.08
Nodes (9): Converters, AppRowPosition, LEFT, RIGHT, AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY (+1 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (4): AlphabetRailTest, ClockBlockTest, ClockBlock(), LumenLauncherTheme()

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.06
Nodes (7): ProfileEntity, Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.26
Nodes (19): NotificationBadgeStyle, COUNT, DOT, ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent() (+11 more)

### Community 30 - "Row"
Cohesion: 0.24
Nodes (57): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+49 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "DockAppRepository"
Cohesion: 0.14
Nodes (3): DockAppRepository, Flow, CleanUpUninstalledAppsUseCaseTest

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.09
Nodes (15): Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow (+7 more)

### Community 35 - "WidgetProviderOption"
Cohesion: 0.24
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "HubGrid"
Cohesion: 0.18
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "WallpaperAccentRole"
Cohesion: 0.08
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 40 - "ExportBackupUseCase.kt"
Cohesion: 0.23
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 41 - "AppShortcutRepository"
Cohesion: 0.19
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 43 - "AppInfo"
Cohesion: 0.23
Nodes (19): AppInfo, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier (+11 more)

### Community 44 - "SettingsRepositoryTest.kt"
Cohesion: 0.43
Nodes (4): DataStoreModule, Context, DataStore, Preferences

### Community 45 - "DefaultFavoriteAppRepository"
Cohesion: 0.12
Nodes (7): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow, ExportBackupUseCaseTest

### Community 46 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 47 - "DrawerViewModel"
Cohesion: 0.25
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 51 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 53 - "eq"
Cohesion: 0.36
Nodes (5): any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 55 - "SettingsScreenTest.kt"
Cohesion: 0.18
Nodes (6): DefaultLauncherRepository, ObserveSettingsScreenStateUseCase, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "HomeDrawerRouteTest.kt"
Cohesion: 0.07
Nodes (16): KeyboardDismissalTest, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, CleanUpUninstalledAppsUseCase (+8 more)

### Community 58 - "LauncherAppWidgetHost"
Cohesion: 0.23
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 59 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 60 - "AppRepository"
Cohesion: 0.15
Nodes (6): AppRepository, GetInstalledAppsUseCase, Flow, GetInstalledAppsUseCaseTest, LauncherViewModelTest, LauncherApps

### Community 61 - "EnsureActiveProfileUseCase"
Cohesion: 0.20
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 62 - "LauncherActivity.kt"
Cohesion: 0.36
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 63 - ".setContent"
Cohesion: 0.14
Nodes (4): AppearanceSettingsScreenTest, DefaultAppRepository, Intent, SelectPreviewAppsUseCase

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
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 70 - "LumenNavHost"
Cohesion: 0.22
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen() (+5 more)

### Community 71 - "LumenDatabase"
Cohesion: 0.22
Nodes (6): DatabaseModule, Context, LumenDatabase, Migrations, Migration, RoomDatabase

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "PlaceWidgetUseCase"
Cohesion: 0.23
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 74 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.18
Nodes (15): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel() (+7 more)

### Community 75 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 77 - "ProfileSettingsViewModel.kt"
Cohesion: 0.20
Nodes (3): StateFlow, ViewModel, ProfileSettingsViewModel

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "ClockColorOption"
Cohesion: 0.18
Nodes (5): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED

### Community 80 - "DockDisplayMode"
Cohesion: 0.15
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 81 - "ClockFontOption"
Cohesion: 0.18
Nodes (7): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM

### Community 82 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 87 - "CardDivider"
Cohesion: 0.38
Nodes (9): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+1 more)

### Community 88 - "CalendarRepository"
Cohesion: 0.08
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo, CalendarSettingsViewModel, StateFlow (+2 more)

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.26
Nodes (17): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard() (+9 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 94 - "homeAppLabelShadow"
Cohesion: 0.08
Nodes (29): dashedBorder(), Color, Dp, Modifier, ImageVector, Modifier, TonalButton(), HubAtCapacityStrip() (+21 more)

### Community 100 - "UsageAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 104 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 105 - ".setContent"
Cohesion: 0.30
Nodes (6): HubScreenTest, HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 108 - "BackupRestoreContent"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 109 - "LauncherFontOption"
Cohesion: 0.17
Nodes (7): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 111 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 112 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 114 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 117 - "BackButton"
Cohesion: 0.26
Nodes (9): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+1 more)

### Community 119 - "SettingsRepository"
Cohesion: 0.08
Nodes (4): SettingsRepository, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 121 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 132 - "HomeAppsListSettingsViewModel"
Cohesion: 0.13
Nodes (4): HomeAppsListSettingsViewModel, StateFlow, ViewModel, HomeAppsListSettingsViewModelTest

### Community 133 - "LauncherSettings"
Cohesion: 0.11
Nodes (13): T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, LauncherSettings, ListContentMode, FAVORITES (+5 more)

### Community 135 - "Lumen Launcher — Onboarding Flow (draft)"
Cohesion: 0.20
Nodes (10): 10. Open questions, 1. Principles, 4. Coach marks (post-onboarding), 5. New code inventory, 6. Reuse map, 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests), 9. Task breakdown (+2 more)

### Community 136 - "ProfileCarouselScreen.kt"
Cohesion: 0.53
Nodes (8): AddProfilePage(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselScreen(), ProfileCarouselScreenPreview(), ProfilePreviewPage()

### Community 137 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.24
Nodes (3): GroupAppsByLetterUseCase, GroupedApps, GroupAppsByLetterUseCaseTest

### Community 138 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 139 - ".setContent"
Cohesion: 0.15
Nodes (5): FakeNotificationAccessRepository, NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 146 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 148 - "LauncherViewModel"
Cohesion: 0.48
Nodes (5): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel

### Community 149 - "ProfileCarouselViewModel.kt"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 151 - "ContactConnectionType"
Cohesion: 0.33
Nodes (6): ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER, WHATSAPP

### Community 152 - "ClockFonts.kt"
Cohesion: 0.53
Nodes (5): FontFamily, resolveFontFamily(), variableWeightInstances(), Font, FontStyle

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (new, zero-interaction teaser), Step 4 — Set as default (`4h`)

## Knowledge Gaps
- **291 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+286 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 552 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **44 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `WidgetResizeHandle`, `LauncherSettings`, `FavoritesPickerScreen.kt`, `FontWeightOption`, `ProfileCarouselScreen.kt`, `CalendarSettingsScreen.kt`, `.setContent`, `DockSettingsScreen.kt`, `WidgetPlacementEntity`, `.setContent`, `ContactConnection`, `.setContent`, `ClockAlignment`, `AccentSwatch`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `.setContent`, `WidgetProviderOption`, `ProfileSettingsScreen.kt`, `WallpaperAccentRole`, `AppShortcutRepository`, `AppInfo`, `LabeledDropdownRow`, `DrawerViewModel`, `.setContent`, `AppDrawerScreen`, `ClockStyleGalleryViewModel`, `.setContent`, `SettingsScreenTest.kt`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `AppRepository`, `LauncherActivity.kt`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `LumenNavHost`, `PlaceWidgetUseCase`, `HomeAppsListSettingsScreen.kt`, `AppDrawerSettingsScreen.kt`, `CardDivider`, `CalendarRepository`, `AppearanceSettingsScreen.kt`, `homeAppLabelShadow`, `.setContent`, `UsageAccessExplanationScreen.kt`, `AppContextMenuTest`, `.setContent`, `BackupRestoreContent`, `LauncherFontOption`, `StickyHeaderLayout`, `ClockAdjustSheet.kt`, `.setContent`, `BackButton`, `SettingsRepository`, `Type.kt`, `.setContent`?**
  _High betweenness centrality (0.172) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `FavoriteAppRepository`, `HomeDrawerRoute`, `LauncherSettings`, `FavoritesPickerScreen.kt`, `HomeAppsListSettingsViewModel`, `ProfileCarouselScreen.kt`, `GroupAppsByLetterUseCaseTest`, `DockSettingsScreen.kt`, `combine`, `ClockAlignment`, `.setContent`, `LauncherViewModel`, `ProfileCarouselViewModel.kt`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `DockAppRepository`, `NotificationBadgeRepository`, `ProfileSettingsScreen.kt`, `WallpaperAccentRole`, `AppShortcutRepository`, `DefaultFavoriteAppRepository`, `DrawerViewModel`, `AppDrawerScreen`, `eq`, `SettingsScreenTest.kt`, `DockAppPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `AppRepository`, `LauncherActivity.kt`, `.setContent`, `SettingsScreen.kt`, `FakeDockAppDao`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `HomeAppsListSettingsScreen.kt`, `SelectPreviewAppsUseCaseTest`, `ProfileSettingsViewModel.kt`, `DockDisplayMode`, `AppearanceSettingsScreen.kt`, `Fixture`, `AppContextMenuTest`, `Fixture`, `SettingsRepository`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ManageProfilesViewModel`, `HomeAppsListSettingsViewModel`, `LauncherSettings`, `FontWeightOption`, `.setContent`, `SettingsRepository.kt`, `combine`, `ClockAlignment`, `.setContent`, `.setContent`, `AppRowPresentation`, `.setContent`, `.setContent`, `LauncherViewModel`, `ProfileCarouselViewModel.kt`, `ProfileEntity`, `ClockTemplateId`, `WallpaperAccentRole`, `ExportBackupUseCase.kt`, `SettingsRepositoryTest.kt`, `DefaultFavoriteAppRepository`, `DrawerViewModel`, `.setContent`, `ClockStyleGalleryViewModel`, `.createViewModel`, `.setContent`, `SettingsScreenTest.kt`, `HomeDrawerRouteTest.kt`, `ImportBackupUseCase.kt`, `AppRepository`, `EnsureActiveProfileUseCase`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `ProfileSettingsViewModel.kt`, `ClockColorOption`, `DockDisplayMode`, `ClockFontOption`, `AppDrawerSettingsViewModelTest`, `CalendarRepository`, `.setContent`, `SettingsRepositoryTest`, `LauncherFontOption`, `BackupMapping.kt`, `.setContent`, `BackButton`, `.setContent`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 101 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 101 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _291 weakly-connected nodes found - possible documentation gaps or missing edges._