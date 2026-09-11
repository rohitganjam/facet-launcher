# Graph Report - lumen-launcher  (2026-09-11)

## Corpus Check
- 324 files · ~692,981 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3117 nodes · 8449 edges · 148 communities (109 shown, 35 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 647 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4fbfa194`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AppInfo
- design_handoff_minimal_launcher/support.js
- ManageProfilesScreen.kt
- DockAppRepository
- AppDrawerSettingsViewModelTest
- .setContent
- HubViewModel
- Android launcher design planning/support.js
- .refresh
- Screens
- AppWidgetRepository.kt
- ManageProfilesViewModel
- AppWidgetRepository
- WidgetPlacementRepository
- HomeDrawerRouteTest.kt
- ContactConnection
- Screens
- CalendarEventsBlock.kt
- WidgetPlacementEntity
- SettingsRepository
- ClockColorOption
- LumenLauncherTheme
- .setContent
- 4. Feature Requirements
- ProfileEntity
- HomeScreen
- 4. Feature Requirements
- AppDrawerScreen.kt
- AppearanceSettingsViewModel
- ProfileCarouselUiState
- Row
- ContactRepositoryTest
- .setContent
- DefaultFavoriteAppRepository
- .setContent
- ClockStyleGalleryViewModelTest
- ClockTemplateId
- homeAppLabelShadow
- .setContent
- DrawerListItemSize
- ProfileDao
- AppShortcutRepository
- Manrope Font License (SIL OFL 1.1)
- HomeScreen.kt
- OnboardingViewModelTest
- NotificationShadeRepository
- LauncherAppWidgetHost
- CalendarPermissionRepository
- Clock Widget Resize — Implementation Spec
- WallpaperRepositoryTest
- AppDrawerScreen
- ClockStyleGalleryViewModel
- .createViewModel
- .setContent
- .setContent
- 4. Feature Requirements
- NotificationSettingsScreen.kt
- EnsureActiveProfileUseCase
- BackupRestoreViewModel
- BackupRestoreViewModelTest
- ExportBackupUseCase.kt
- .setContent
- HubWidgetPickerViewModel
- BackupWidgetPlacement
- SettingsScreen.kt
- DockAppEntity
- ClockStyleGalleryScreen.kt
- github.md
- .setContent
- ColorTest
- LumenNavHost
- WidgetPlacementDao
- letterAt
- AccentSwatch
- CardDivider
- BackupRestoreContent
- .setContent
- NotificationBadgeRepository
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- WidgetProviderOption
- ObserveHomeScreenStateUseCase.kt
- FavoriteAppRepository
- BackupRestoreViewModelTest.kt
- .useCase
- ContactConnectionsSheet.kt
- Lumen Launcher Implementation Plan
- BackupRestoreMessage
- .setContent
- LumenNotificationListenerService
- AppearanceSettingsScreen.kt
- ProfileCarouselScreen.kt
- Lumen Launcher — Built Capabilities
- ObserveHubStateUseCase
- ProfileCarouselViewModelTest
- HomeDrawerRoute
- .setContent
- SettingsRepositoryTest
- HubContent
- ProfileCarouselViewModel
- HomeSurfacePreview
- DrawerViewModel
- Fixture
- Type.kt
- DefaultLauncherRepositoryTest
- .failAndRelease
- .setContent
- HomeViewModel
- PlaceWidgetResult
- AppContextMenuTest
- ContactRepository
- RankBySearchRelevanceUseCase
- ConvertersTest
- ClockAlignment.kt
- ObserveProfilePreviewsUseCase.kt
- HomeAppsListSettingsScreen.kt
- LumenDatabaseMigrationTest
- .setContent
- NotificationAccessExplanationScreen.kt
- TypeTest.kt
- LauncherActivity.kt
- FirstRunCallout
- GestureHintOverlay
- ClockCornerHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- DefaultAppRepositoryTest
- HomeWallpaper
- LauncherSettings
- .setContent
- Lumen Launcher — Onboarding Flow
- WidgetResizeHandle
- StickyHeaderLayout
- .setContent
- DockSettingsScreen.kt
- LauncherFontOption
- Lumen Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- BackButton
- .rendersOneEntryPerLetterProvided
- 2. Design tokens
- 2. Gate, seeding & architecture
- 3. Screen-by-screen

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 224 edges
2. `AppInfo` - 172 edges
3. `ProfileEntity` - 168 edges
4. `SettingsRepository` - 143 edges
5. `ProfileRepository` - 119 edges
6. `Row` - 119 edges
7. `LauncherSettings` - 103 edges
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

## Communities (148 total, 35 thin omitted)

### Community 0 - "AppInfo"
Cohesion: 0.07
Nodes (12): Flow, ProfileDockAppDao, ProfileDockAppEntity, AppInfo, Flow, ProfileDockAppRepository, toProfileDockAppEntity(), ProfileDockAppDaoTest (+4 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "ManageProfilesScreen.kt"
Cohesion: 0.06
Nodes (61): DragReorderState, Modifier, T, rememberDragReorderState(), Composable, Modifier, PaddingValues, ThemedDropdownMenu() (+53 more)

### Community 3 - "DockAppRepository"
Cohesion: 0.07
Nodes (17): DefaultLauncherRepository, Intent, DockAppRepository, Flow, combine(), Flow, Flow, ObserveSettingsScreenStateUseCase (+9 more)

### Community 5 - ".setContent"
Cohesion: 0.07
Nodes (35): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+27 more)

### Community 6 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".refresh"
Cohesion: 0.26
Nodes (4): Callback, Callback, Callback, UserHandle

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "AppWidgetRepository.kt"
Cohesion: 0.13
Nodes (10): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+2 more)

### Community 11 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (7): StateFlow, ViewModel, ManageProfilesUiState, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (17): AppRepository, flattenIcon(), Bitmap, Flow, ContactPermissionRepository, AppModule, Context, LumenDatabase (+9 more)

### Community 15 - "ContactConnection"
Cohesion: 0.17
Nodes (13): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+5 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.31
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, Context, Intent (+3 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 19 - "SettingsRepository"
Cohesion: 0.05
Nodes (3): ManageProfilesScreenTest, Flow, SettingsRepository

### Community 20 - "ClockColorOption"
Cohesion: 0.05
Nodes (49): Converters, T, resolveOverride(), ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED (+41 more)

### Community 21 - "LumenLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), LumenLauncherTheme()

### Community 22 - ".setContent"
Cohesion: 0.13
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "ProfileEntity"
Cohesion: 0.05
Nodes (8): ProfileEntity, Flow, ProfileRepository, toCsv(), ProfileEntityTest, FakeProfileDao, Flow, ProfileRepositoryTest

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.21
Nodes (21): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), DrawerAppRow() (+13 more)

### Community 28 - "AppearanceSettingsViewModel"
Cohesion: 0.09
Nodes (12): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+4 more)

### Community 30 - "Row"
Cohesion: 0.19
Nodes (65): ClockDateStyle, CONDENSED, FULL, AccentContrastTemplate(), AccentFieldTemplate(), BoldColonTemplate(), BracketMinimalTemplate(), ChipTemplate() (+57 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 32 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 33 - "DefaultFavoriteAppRepository"
Cohesion: 0.07
Nodes (13): DefaultFavoriteAppRepository, Flow, DatabaseModule, Context, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, Migrations (+5 more)

### Community 36 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 37 - "homeAppLabelShadow"
Cohesion: 0.14
Nodes (20): dashedBorder(), Color, Dp, Modifier, ImageVector, Modifier, TonalButton(), HubAtCapacityStrip() (+12 more)

### Community 39 - "DrawerListItemSize"
Cohesion: 0.08
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 40 - "ProfileDao"
Cohesion: 0.16
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 41 - "AppShortcutRepository"
Cohesion: 0.19
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 43 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (18): AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge() (+10 more)

### Community 44 - "OnboardingViewModelTest"
Cohesion: 0.23
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.24
Nodes (7): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost

### Community 47 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (12): FakeCalendarRepository, FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 51 - "ClockStyleGalleryViewModel"
Cohesion: 0.11
Nodes (4): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel

### Community 52 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 54 - ".setContent"
Cohesion: 0.10
Nodes (6): SettingsScreenTest, Intent, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "NotificationSettingsScreen.kt"
Cohesion: 0.25
Nodes (9): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), StateFlow, ViewModel, NotificationSettingsUiState (+1 more)

### Community 57 - "EnsureActiveProfileUseCase"
Cohesion: 0.15
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 58 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 60 - "ExportBackupUseCase.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 61 - ".setContent"
Cohesion: 0.10
Nodes (11): KeyboardDismissalTest, FavoritesPickerScreenTest, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel (+3 more)

### Community 62 - "HubWidgetPickerViewModel"
Cohesion: 0.17
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 63 - "BackupWidgetPlacement"
Cohesion: 0.23
Nodes (7): BackupAppEntry, BackupProfile, BackupWidgetPlacement, toBackupEntry(), toBackupPlacement(), toBackupProfile(), ImportBackupUseCaseTest

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "DockAppEntity"
Cohesion: 0.10
Nodes (8): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 66 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.31
Nodes (11): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+3 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - ".setContent"
Cohesion: 0.12
Nodes (8): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 70 - "LumenNavHost"
Cohesion: 0.28
Nodes (5): Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), NavHostController

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - "AccentSwatch"
Cohesion: 0.05
Nodes (29): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+21 more)

### Community 74 - "CardDivider"
Cohesion: 0.25
Nodes (15): CardDivider(), Modifier, SettingsCard(), AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, appDrawerDisplayLabel() (+7 more)

### Community 75 - "BackupRestoreContent"
Cohesion: 0.35
Nodes (11): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+3 more)

### Community 77 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "WidgetProviderOption"
Cohesion: 0.22
Nodes (15): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+7 more)

### Community 80 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.20
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 81 - "FavoriteAppRepository"
Cohesion: 0.07
Nodes (10): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest (+2 more)

### Community 82 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.25
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 83 - ".useCase"
Cohesion: 0.06
Nodes (9): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, ExportBackupUseCaseTest, GroupAppsByLetterUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest (+1 more)

### Community 84 - "ContactConnectionsSheet.kt"
Cohesion: 0.50
Nodes (7): ConnectionRow(), ContactConnectionsSheet(), fallbackIcon(), androidx, ImageVector, Modifier, subtitle()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 87 - ".setContent"
Cohesion: 0.14
Nodes (5): OnboardingScreenTest, Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 88 - "LumenNotificationListenerService"
Cohesion: 0.43
Nodes (3): LumenNotificationListenerService, NotificationListenerService, StatusBarNotification

### Community 89 - "AppearanceSettingsScreen.kt"
Cohesion: 0.22
Nodes (17): WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel() (+9 more)

### Community 90 - "ProfileCarouselScreen.kt"
Cohesion: 0.25
Nodes (12): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+4 more)

### Community 91 - "Lumen Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 93 - "ProfileCarouselViewModelTest"
Cohesion: 0.29
Nodes (3): WallpaperRepository, ProfileCarouselViewModelTest, WallpaperRepository

### Community 94 - "HomeDrawerRoute"
Cohesion: 0.13
Nodes (17): ClockAdjustMode, ADJUST, MENU, NONE, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute() (+9 more)

### Community 97 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 98 - "ProfileCarouselViewModel"
Cohesion: 0.29
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 99 - "HomeSurfacePreview"
Cohesion: 0.16
Nodes (16): Image, HomeSurfacePreview(), Color, FontWeight, Modifier, Alignment, Modifier, WallpaperBackground() (+8 more)

### Community 100 - "DrawerViewModel"
Cohesion: 0.29
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 102 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 104 - ".failAndRelease"
Cohesion: 0.31
Nodes (3): AddFailureReason, HUB_FULL, SETUP_CANCELLED

### Community 106 - "HomeViewModel"
Cohesion: 0.22
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 107 - "PlaceWidgetResult"
Cohesion: 0.50
Nodes (3): HubFull, Placed, PlaceWidgetResult

### Community 110 - "RankBySearchRelevanceUseCase"
Cohesion: 0.21
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 113 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.23
Nodes (6): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 114 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 117 - "NotificationAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 119 - "LauncherActivity.kt"
Cohesion: 0.36
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 121 - "FirstRunCallout"
Cohesion: 0.83
Nodes (3): FirstRunCallout(), FirstRunCalloutPreview(), Modifier

### Community 122 - "GestureHintOverlay"
Cohesion: 0.83
Nodes (3): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "HomeWallpaper"
Cohesion: 0.06
Nodes (16): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, HomeWallpaper, Tones, Unavailable, Bitmap, WallpaperRepository (+8 more)

### Community 133 - "LauncherSettings"
Cohesion: 0.19
Nodes (4): LauncherSettings, HomeUiState, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 135 - "Lumen Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 138 - "StickyHeaderLayout"
Cohesion: 0.19
Nodes (15): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+7 more)

### Community 139 - ".setContent"
Cohesion: 0.09
Nodes (21): ProfileSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable (+13 more)

### Community 140 - "DockSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader() (+4 more)

### Community 141 - "LauncherFontOption"
Cohesion: 0.11
Nodes (16): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockZoneHandle(), ClockZoneHandlePreview() (+8 more)

### Community 142 - "Lumen Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Lumen Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "BackButton"
Cohesion: 0.26
Nodes (9): BackButton(), Modifier, Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel (+1 more)

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

## Knowledge Gaps
- **348 isolated node(s):** `Keys`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY`, `ACCENT_SECONDARY` (+343 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 626 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `ManageProfilesScreen.kt`, `HomeWallpaper`, `.setContent`, `.setContent`, `WidgetResizeHandle`, `StickyHeaderLayout`, `.setContent`, `DockSettingsScreen.kt`, `LauncherFontOption`, `HomeDrawerRouteTest.kt`, `ContactConnection`, `BackButton`, `WidgetPlacementEntity`, `SettingsRepository`, `ClockColorOption`, `.rendersOneEntryPerLetterProvided`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `AppearanceSettingsViewModel`, `Row`, `.setContent`, `.setContent`, `homeAppLabelShadow`, `.setContent`, `AppShortcutRepository`, `HomeScreen.kt`, `CalendarPermissionRepository`, `AppDrawerScreen`, `.setContent`, `.setContent`, `NotificationSettingsScreen.kt`, `.setContent`, `SettingsScreen.kt`, `ClockStyleGalleryScreen.kt`, `.setContent`, `ColorTest`, `AccentSwatch`, `CardDivider`, `BackupRestoreContent`, `.setContent`, `WidgetProviderOption`, `ContactConnectionsSheet.kt`, `.setContent`, `AppearanceSettingsScreen.kt`, `ProfileCarouselScreen.kt`, `.setContent`, `Type.kt`, `.setContent`, `AppContextMenuTest`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `NotificationAccessExplanationScreen.kt`, `LauncherActivity.kt`, `FirstRunCallout`, `GestureHintOverlay`?**
  _High betweenness centrality (0.124) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ManageProfilesScreen.kt`, `DockAppRepository`, `HomeWallpaper`, `.setContent`, `DockSettingsScreen.kt`, `HomeDrawerRouteTest.kt`, `ContactConnection`, `ClockColorOption`, `.setContent`, `HomeScreen`, `AppDrawerScreen.kt`, `AppearanceSettingsViewModel`, `ContactRepositoryTest`, `DefaultFavoriteAppRepository`, `.setContent`, `AppShortcutRepository`, `HomeScreen.kt`, `OnboardingViewModelTest`, `CalendarPermissionRepository`, `AppDrawerScreen`, `.setContent`, `.setContent`, `.setContent`, `SettingsScreen.kt`, `DockAppEntity`, `.setContent`, `LumenNavHost`, `ObserveHomeScreenStateUseCase.kt`, `FavoriteAppRepository`, `.useCase`, `.setContent`, `AppearanceSettingsScreen.kt`, `ProfileCarouselScreen.kt`, `HomeDrawerRoute`, `HomeSurfacePreview`, `DrawerViewModel`, `Fixture`, `AppContextMenuTest`, `ObserveProfilePreviewsUseCase.kt`, `HomeAppsListSettingsScreen.kt`, `LauncherActivity.kt`?**
  _High betweenness centrality (0.120) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `AppInfo`, `DockAppRepository`, `HomeWallpaper`, `LauncherSettings`, `.setContent`, `AppDrawerSettingsViewModelTest`, `.setContent`, `ManageProfilesViewModel`, `LauncherFontOption`, `HomeDrawerRouteTest.kt`, `ClockColorOption`, `.setContent`, `ProfileEntity`, `AppearanceSettingsViewModel`, `DefaultFavoriteAppRepository`, `.setContent`, `ClockStyleGalleryViewModelTest`, `ClockTemplateId`, `.setContent`, `DrawerListItemSize`, `CalendarPermissionRepository`, `ClockStyleGalleryViewModel`, `.createViewModel`, `.setContent`, `.setContent`, `NotificationSettingsScreen.kt`, `EnsureActiveProfileUseCase`, `ExportBackupUseCase.kt`, `.setContent`, `.setContent`, `AccentSwatch`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `BackupRestoreViewModelTest.kt`, `.useCase`, `.setContent`, `AppearanceSettingsScreen.kt`, `.setContent`, `SettingsRepositoryTest`, `DrawerViewModel`, `HomeViewModel`, `RankBySearchRelevanceUseCase`, `ObserveProfilePreviewsUseCase.kt`, `NotificationAccessExplanationScreen.kt`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `ProfileRepository` (e.g. with `.`addProfile names it Profile N and appends after the last position`()` and `.`deleteProfile removes it`()`) actually correct?**
  _`ProfileRepository` has 19 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `THEME`, `THEME_INVERTED` to the rest of the system?**
  _348 weakly-connected nodes found - possible documentation gaps or missing edges._