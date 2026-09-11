# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 325 files · ~693,419 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3243 nodes · 8398 edges · 169 communities (120 shown, 44 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 878 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e228580a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- combine
- design_handoff_minimal_launcher/support.js
- PaddingValues
- DefaultLauncherRepository
- AppDrawerSettingsViewModelTest
- AppInfo
- HubViewModel
- Android launcher design planning/support.js
- .refresh
- Screens
- AppWidgetRepository
- ManageProfilesViewModel
- FakeProfileDao
- FakeWidgetPlacementDao
- ProfileDockAppRepository
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementEntity
- SettingsRepository
- AppRowPosition
- LumenLauncherTheme
- .setContent
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- WallpaperAccentRole
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- FakeDefaultFavoriteAppDao
- .setContent
- ClockStyleGalleryViewModelTest
- ClockTemplateId
- TonalButton
- .setContent
- DrawerListItemSize
- ProfileDao
- DrawerViewModel.kt
- Manrope Font License (SIL OFL 1.1)
- AppIcon
- OnboardingViewModelTest
- NotificationShadeRepository
- LauncherAppWidgetHost
- NotificationAccessRepository
- Clock Widget Resize — Implementation Spec
- WallpaperRepositoryTest
- AppDrawerScreen
- ClockStyleGalleryViewModel
- .createViewModel
- .setContent
- .setContent
- 4. Feature Requirements
- NotificationSettingsScreen.kt
- FakeProfileDao
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- DefaultFavoriteAppRepository
- GetInstalledAppsUseCase
- HubWidgetPickerViewModel
- BackupMapping.kt
- SettingsScreen.kt
- DockAppEntity
- CalendarInfo
- github.md
- .setContent
- ColorTest
- LumenNavHost
- .setContent
- letterAt
- CalendarSettingsViewModel
- StickyHeaderLayout
- DatabaseModule.kt
- .setContent
- NotificationBadgeRepository
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- HubWidgetPickerScreen.kt
- ObserveHomeScreenStateUseCase.kt
- FakeFavoriteAppDao
- .createViewModel
- .useCase
- FavoriteAppRepository
- Lumen Launcher Implementation Plan
- OnboardingHomeSetupPage.kt
- .setContent
- LumenNotificationListenerService
- AppearanceSettingsScreen.kt
- AccentSwatch
- Lumen Launcher — Built Capabilities
- HomeWallpaper
- WallpaperRepository
- .setContent
- .setContent
- SettingsRepositoryTest
- HubContent
- .createViewModel
- Color.kt
- eq
- Fixture
- FontWeightOption
- DefaultLauncherRepositoryTest
- DockDisplayMode
- HubGrid
- OnboardingScreen
- OnboardingViewModel
- AppContextMenu
- ContactRepository
- DrawerViewModelTest
- Converters
- ClockAlignment
- ObserveProfilePreviewsUseCase.kt
- HomeAppsListSettingsScreen.kt
- LumenDatabaseMigrationTest
- SettingsViewModelTest
- NotificationAccessExplanationScreen.kt
- DragReorderState
- DockAppPickerViewModel
- ClockAdjustSheet.kt
- Modifier
- OnboardingProfilesPage.kt
- CalendarRepositoryTest
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DefaultAppRepositoryTest
- HomeAppsListSettingsViewModelTest.kt
- LauncherSettings
- .setContent
- Lumen Launcher — Onboarding Flow
- .setContent
- WidgetResizeHandle
- PermissionKind
- ProfileSettingsContent
- DockSettingsScreen.kt
- LauncherFontOption
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- AppearanceSettingsViewModelTest.kt
- dashedBorder
- DockAppPickerScreen.kt
- .setContent
- CalendarSettingsScreen.kt
- .setContent
- SetDefaultLauncherSheet
- 2. Design tokens
- AppRepository.kt
- HubWidgetTile
- SettingsViewModel
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- ObserveSettingsScreenStateUseCase.kt
- ProfileEntityTest
- Migrations
- ClockColors.kt
- Context
- Alignment
- FontFamily
- SharedFlow
- PaddingValues

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 192 edges
2. `ProfileEntity` - 168 edges
3. `SettingsRepository` - 142 edges
4. `Row` - 119 edges
5. `ProfileRepository` - 118 edges
6. `LauncherSettings` - 102 edges
7. `WidgetPlacementEntity` - 58 edges
8. `HomeScreen()` - 57 edges
9. `DefaultFavoriteAppRepository` - 53 edges
10. `ClockBlock()` - 53 edges

## Surprising Connections (you probably didn't know these)
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `AppDrawerScreen()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/drawer/AppDrawerScreen.kt
- `Reset Drawer State on Close or App Launch (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/75674885-a34f-47da-869a-e70b8bd5487f/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Keyboard Dismissal on Home Gesture (fix plan)` --references--> `HomeDrawerRoute()`  [EXTRACTED]
  .artifacts/60cb353d-6cb8-4d22-8155-1466a7efb5d2/implementation_plan.artifact.md → app/src/main/kotlin/com/lumenlauncher/app/ui/launcher/HomeDrawerRoute.kt
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (169 total, 44 thin omitted)

### Community 0 - "combine"
Cohesion: 0.05
Nodes (20): Flow, Flow, ProfileDockAppDao, ProfileDockAppEntity, AppInfo, Flow, toProfileDockAppEntity(), combine() (+12 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "PaddingValues"
Cohesion: 0.17
Nodes (22): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+14 more)

### Community 5 - "AppInfo"
Cohesion: 0.13
Nodes (16): AppInfo, GroupAppsByLetterUseCase, GroupedApps, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview() (+8 more)

### Community 6 - "HubViewModel"
Cohesion: 0.22
Nodes (3): HubUiState, HubWidgetUi, HubViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".refresh"
Cohesion: 0.21
Nodes (5): Callback, Callback, Flow, Callback, UserHandle

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 12 - "FakeProfileDao"
Cohesion: 0.12
Nodes (4): FakeProfileDao, Flow, ProfileDao, ProfileRepositoryTest

### Community 13 - "FakeWidgetPlacementDao"
Cohesion: 0.17
Nodes (5): Flow, WidgetPlacementDao, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ProfileDockAppRepository"
Cohesion: 0.08
Nodes (19): AppModule, Context, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, LumenDatabase (+11 more)

### Community 15 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.08
Nodes (18): HubScreenTest, WidgetPlacementEntity, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState (+10 more)

### Community 19 - "SettingsRepository"
Cohesion: 0.05
Nodes (14): DataStoreModule, Context, Keys, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow (+6 more)

### Community 20 - "AppRowPosition"
Cohesion: 0.10
Nodes (18): AppListLimits, AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT, RIGHT, AppRowPresentation (+10 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (6): AlphabetRailTest, ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), LumenLauncherTheme()

### Community 22 - ".setContent"
Cohesion: 0.13
Nodes (4): ProfileCarouselScreenTest, AppInfo, UsageStatsRepository, UsageStatsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.08
Nodes (9): ProfileEntity, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, FontWeightOption, ProfileRepository (+1 more)

### Community 25 - "HomeScreen"
Cohesion: 0.08
Nodes (27): HomeScreenTest, AppInfo, ClockCornerHandle(), Modifier, AppRow(), ClockAdjustMode, ADJUST, MENU (+19 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.22
Nodes (23): NotificationBadgeStyle, COUNT, DOT, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder() (+15 more)

### Community 28 - "WallpaperAccentRole"
Cohesion: 0.09
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.06
Nodes (37): T, resolveOverride(), ConfirmDialog(), Modifier, AddProfilePage(), AppInfo, CalendarEvent, ClockColorOption (+29 more)

### Community 30 - "Row"
Cohesion: 0.18
Nodes (69): Alignment, GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate() (+61 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "TonalButton"
Cohesion: 0.29
Nodes (9): ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview(), Modifier, HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 39 - "DrawerListItemSize"
Cohesion: 0.09
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 40 - "ProfileDao"
Cohesion: 0.21
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 41 - "DrawerViewModel.kt"
Cohesion: 0.13
Nodes (8): AppShortcutRepository, AppShortcut, ContactInfo, DrawerViewModel, StateFlow, ViewModel, AppShortcutRepositoryTest, ShortcutInfo

### Community 43 - "AppIcon"
Cohesion: 0.61
Nodes (7): AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.18
Nodes (6): Fixture, AppInfo, HomeWallpaper, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.16
Nodes (9): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 47 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (12): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 51 - "ClockStyleGalleryViewModel"
Cohesion: 0.07
Nodes (27): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockDateStyle, CONDENSED, FULL (+19 more)

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "NotificationSettingsScreen.kt"
Cohesion: 0.25
Nodes (9): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), StateFlow, ViewModel, NotificationSettingsUiState (+1 more)

### Community 57 - "FakeProfileDao"
Cohesion: 0.22
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.09
Nodes (29): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen() (+21 more)

### Community 60 - "DefaultFavoriteAppRepository"
Cohesion: 0.16
Nodes (5): FavoritesPickerScreenTest, DefaultFavoriteAppRepository, AppInfo, DefaultFavoriteAppEntity, Flow

### Community 61 - "GetInstalledAppsUseCase"
Cohesion: 0.11
Nodes (13): GetInstalledAppsUseCase, Flow, AppInfo, Intent, LauncherActivity, StateFlow, ViewModel, LauncherUiState (+5 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.08
Nodes (21): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, AddFailed, AddFailureReason, HUB_FULL (+13 more)

### Community 63 - "BackupMapping.kt"
Cohesion: 0.11
Nodes (19): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, DefaultFavoriteAppEntity (+11 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appsListSummary(), ClickableRow(), Composable, ListContentMode, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.09
Nodes (8): DockAppRepository, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 66 - "CalendarInfo"
Cohesion: 0.20
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".setContent"
Cohesion: 0.23
Nodes (6): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, AppInfo, StateFlow, ViewModel

### Community 70 - "LumenNavHost"
Cohesion: 0.26
Nodes (6): AppInfo, Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "CalendarSettingsViewModel"
Cohesion: 0.19
Nodes (5): AssignCalendarColorsUseCase, CalendarSettingsUiState, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 74 - "StickyHeaderLayout"
Cohesion: 0.20
Nodes (20): CardDivider(), Modifier, SettingsCard(), Modifier, StickyHeaderLayout(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader() (+12 more)

### Community 75 - "DatabaseModule.kt"
Cohesion: 0.17
Nodes (7): DatabaseModule, DefaultFavoriteAppDao, DockAppDao, FavoriteAppDao, ProfileDao, WidgetPlacementDao, Context

### Community 77 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "HubWidgetPickerScreen.kt"
Cohesion: 0.38
Nodes (11): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), HubWidgetPickerViewModel, Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+3 more)

### Community 80 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.17
Nodes (6): HomeScreenState, AppInfo, CalendarEvent, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 81 - "FakeFavoriteAppDao"
Cohesion: 0.10
Nodes (7): FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 82 - ".createViewModel"
Cohesion: 0.22
Nodes (6): DockSettingsViewModelTest, WallpaperRepository, AppInfo, DockAppRepository, HomeWallpaper, WallpaperRepository

### Community 83 - ".useCase"
Cohesion: 0.06
Nodes (10): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, AppInfo, DockAppRepository (+2 more)

### Community 84 - "FavoriteAppRepository"
Cohesion: 0.30
Nodes (4): FavoriteAppRepository, AppInfo, FavoriteAppEntity, Flow

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.37
Nodes (13): rememberDragReorderState(), DockAppsReorderRow(), DockSection(), FavoritesClickableRow(), FavoritesReorderList(), HomeAppsSection(), AppInfo, ListContentMode (+5 more)

### Community 88 - "LumenNotificationListenerService"
Cohesion: 0.43
Nodes (3): LumenNotificationListenerService, NotificationListenerService, StatusBarNotification

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): AccentSwatch, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+9 more)

### Community 90 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "HomeWallpaper"
Cohesion: 0.24
Nodes (9): HomeWallpaper, Image, Tones, Unavailable, Bitmap, WallpaperRepository, Alignment, Modifier (+1 more)

### Community 94 - ".setContent"
Cohesion: 0.08
Nodes (24): KeyboardDismissalTest, T, RankBySearchRelevanceUseCase, HomeViewModel, StateFlow, ViewModel, Axis, HORIZONTAL (+16 more)

### Community 97 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 99 - "Color.kt"
Cohesion: 0.32
Nodes (10): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, accentTonalExtremes(), homeAppLabelShadow(), Color, toneOf(), wallpaperPrimaryAndSecondary() (+2 more)

### Community 100 - "eq"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 102 - "FontWeightOption"
Cohesion: 0.11
Nodes (16): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+8 more)

### Community 104 - "DockDisplayMode"
Cohesion: 0.26
Nodes (9): DockDisplayMode, ICONS, TEXT, HomeSurfacePreview(), AppInfo, Color, FontWeight, HomeWallpaper (+1 more)

### Community 105 - "HubGrid"
Cohesion: 0.26
Nodes (10): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+2 more)

### Community 106 - "OnboardingScreen"
Cohesion: 0.20
Nodes (11): Modifier, OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT, OnboardingSubScreen (+3 more)

### Community 107 - "OnboardingViewModel"
Cohesion: 0.26
Nodes (6): AppInfo, Intent, ListContentMode, StateFlow, ViewModel, OnboardingViewModel

### Community 108 - "AppContextMenu"
Cohesion: 0.26
Nodes (3): AppContextMenuTest, AppContextMenu(), Modifier

### Community 111 - "Converters"
Cohesion: 0.09
Nodes (6): Converters, ListContentMode, FAVORITES, MOST_USED, RECENTS, ConvertersTest

### Community 112 - "ClockAlignment"
Cohesion: 0.11
Nodes (17): ClockAlignment, CENTER, LEFT, RIGHT, AppInfo, ClockColorOption, ClockDateStyle, ClockFontOption (+9 more)

### Community 113 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.15
Nodes (8): Inputs, AppInfo, CalendarEvent, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 114 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.42
Nodes (10): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), AppInfo (+2 more)

### Community 117 - "NotificationAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 118 - "DragReorderState"
Cohesion: 0.29
Nodes (4): DragReorderState, Modifier, T, detectGrabOrResizeGesture()

### Community 119 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (6): DockAppPickerUiState, DockAppPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 120 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 122 - "OnboardingProfilesPage.kt"
Cohesion: 0.38
Nodes (8): Modifier, OnboardingDots(), Modifier, MockProfile, MockProfileCard(), OnboardingProfilesPage(), OnboardingProfilesPagePreview(), ProfileSwitchDemo()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "HomeAppsListSettingsViewModelTest.kt"
Cohesion: 0.19
Nodes (6): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, AppInfo, HomeWallpaper, WallpaperRepository

### Community 133 - "LauncherSettings"
Cohesion: 0.11
Nodes (13): LauncherSettings, toDockAppEntity(), HomeUiState, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, FontWeightOption (+5 more)

### Community 134 - ".setContent"
Cohesion: 0.26
Nodes (5): BackupRestoreScreenTest, ExportBackupUseCase, Uri, toBackupSettings(), ImportBackupUseCase

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 138 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 139 - "ProfileSettingsContent"
Cohesion: 0.18
Nodes (17): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable, Modifier (+9 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.36
Nodes (10): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+2 more)

### Community 141 - "LauncherFontOption"
Cohesion: 0.13
Nodes (14): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, Modifier, uniformScale() (+6 more)

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "BackButton"
Cohesion: 0.26
Nodes (9): BackButton(), Modifier, Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel (+1 more)

### Community 146 - "AppearanceSettingsViewModelTest.kt"
Cohesion: 0.31
Nodes (3): DefaultAppRepository, Intent, SelectPreviewAppsUseCase

### Community 147 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview()

### Community 148 - "DockAppPickerScreen.kt"
Cohesion: 0.56
Nodes (8): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), AppInfo, Modifier, PickerSectionHeader()

### Community 150 - "CalendarSettingsScreen.kt"
Cohesion: 0.61
Nodes (7): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip()

### Community 152 - "SetDefaultLauncherSheet"
Cohesion: 0.62
Nodes (6): OnboardingUiState, AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "AppRepository.kt"
Cohesion: 0.47
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 155 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 156 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "ObserveSettingsScreenStateUseCase.kt"
Cohesion: 0.60
Nodes (3): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

## Knowledge Gaps
- **349 isolated node(s):** `1. Overview`, `2. Goals`, `3. Non-Goals (v1)`, `3a. Parked for Future Consideration`, `F1. Home clock widget + calendar integration` (+344 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 617 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **44 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `PaddingValues`, `AppInfo`, `.setContent`, `.setContent`, `WidgetResizeHandle`, `ProfileSettingsContent`, `DockSettingsScreen.kt`, `LauncherFontOption`, `ContactConnection`, `BackButton`, `WidgetPlacementEntity`, `AppearanceSettingsViewModelTest.kt`, `DockAppPickerScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `dashedBorder`, `HomeScreen`, `SetDefaultLauncherSheet`, `AppDrawerScreen.kt`, `CalendarSettingsScreen.kt`, `ProfileCarouselScreen.kt`, `Row`, `WallpaperAccentRole`, `.setContent`, `.setContent`, `TonalButton`, `.setContent`, `DrawerViewModel.kt`, `LauncherAppWidgetHost`, `NotificationAccessRepository`, `AppDrawerScreen`, `ClockStyleGalleryViewModel`, `.setContent`, `.setContent`, `NotificationSettingsScreen.kt`, `BackupRestoreViewModel`, `DefaultFavoriteAppRepository`, `GetInstalledAppsUseCase`, `SettingsScreen.kt`, `CalendarInfo`, `.setContent`, `ColorTest`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `HubWidgetPickerScreen.kt`, `OnboardingHomeSetupPage.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `AccentSwatch`, `.setContent`, `.setContent`, `Color.kt`, `FontWeightOption`, `AppContextMenu`, `HomeAppsListSettingsScreen.kt`, `NotificationAccessExplanationScreen.kt`, `ClockAdjustSheet.kt`, `OnboardingProfilesPage.kt`?**
  _High betweenness centrality (0.183) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `AppDrawerSettingsViewModelTest`, `LauncherSettings`, `.setContent`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `ManageProfilesViewModel`, `ProfileDockAppRepository`, `AppearanceSettingsViewModelTest.kt`, `AppRowPosition`, `.setContent`, `.setContent`, `ProfileEntity`, `WallpaperAccentRole`, `ProfileCarouselScreen.kt`, `ObserveSettingsScreenStateUseCase.kt`, `.setContent`, `ClockStyleGalleryViewModelTest`, `.setContent`, `DrawerListItemSize`, `DrawerViewModel.kt`, `OnboardingViewModelTest`, `NotificationAccessRepository`, `ClockStyleGalleryViewModel`, `.createViewModel`, `.setContent`, `.setContent`, `NotificationSettingsScreen.kt`, `FakeProfileDao`, `DefaultFavoriteAppRepository`, `GetInstalledAppsUseCase`, `CalendarInfo`, `.setContent`, `.setContent`, `CalendarSettingsViewModel`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.createViewModel`, `.useCase`, `.setContent`, `.setContent`, `.setContent`, `SettingsRepositoryTest`, `.createViewModel`, `FontWeightOption`, `DockDisplayMode`, `OnboardingViewModel`, `ClockAlignment`, `ObserveProfilePreviewsUseCase.kt`, `NotificationAccessExplanationScreen.kt`?**
  _High betweenness centrality (0.114) - this node is a cross-community bridge._
- **Why does `LauncherSettings` connect `LauncherSettings` to `AppDrawerSettingsViewModelTest`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `ManageProfilesViewModel`, `ProfileDockAppRepository`, `AppearanceSettingsViewModelTest.kt`, `SettingsRepository`, `AppRowPosition`, `ProfileEntity`, `WallpaperAccentRole`, `ProfileCarouselScreen.kt`, `ObserveSettingsScreenStateUseCase.kt`, `ClockStyleGalleryViewModelTest`, `DrawerListItemSize`, `DrawerViewModel.kt`, `OnboardingViewModelTest`, `NotificationAccessRepository`, `ClockStyleGalleryViewModel`, `.createViewModel`, `NotificationSettingsScreen.kt`, `DefaultFavoriteAppRepository`, `GetInstalledAppsUseCase`, `SettingsScreen.kt`, `StickyHeaderLayout`, `ObserveHomeScreenStateUseCase.kt`, `.createViewModel`, `.useCase`, `AppearanceSettingsScreen.kt`, `.createViewModel`, `Fixture`, `DrawerViewModelTest`, `ObserveProfilePreviewsUseCase.kt`, `SettingsViewModelTest`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Are the 58 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.aLongAppListScrollsIndependentlyOnceTheZoneHeightForcesItTo()`) actually correct?**
  _`LumenLauncherTheme()` has 58 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 114 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 114 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `ProfileRepository` (e.g. with `.`addProfile names it Profile N and appends after the last position`()` and `.`deleteProfile removes it`()`) actually correct?**
  _`ProfileRepository` has 19 INFERRED edges - model-reasoned connections that need verification._