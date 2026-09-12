# Graph Report - lumen-launcher  (2026-09-12)

## Corpus Check
- 353 files · ~788,562 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3462 nodes · 9010 edges · 189 communities (132 shown, 51 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 923 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `217ae0de`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- .setContent
- LauncherViewModel
- HomeDrawerRouteTest.kt
- ListContentMode
- AppRowPosition
- Android launcher design planning/support.js
- .setContent
- Screens
- HomeDrawerRoute
- EnsureActiveFacetUseCase
- FavoritesPickerViewModel
- FavoriteAppRepository
- FakeFacetDockAppDao
- AppWidgetRepository
- Screens
- Alignment
- FacetEntity
- ManageFacetsViewModel
- FacetCarouselViewModel.kt
- FacetDao
- SettingsSearchEntry
- 4. Feature Requirements
- SettingsRepository
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerPresentation
- FacetCarouselScreen.kt
- Row
- ContactConnectionsSheet.kt
- .setContent
- WidgetPlacementEntity
- DefaultAppRepository
- .createViewModel
- FacetDockAppEntity
- ClockStyleGalleryViewModelTest
- ImportBackupUseCase.kt
- ClockStyleGalleryScreen.kt
- CalendarSettingsViewModel
- AppearanceSettingsScreen.kt
- Manrope Font License (SIL OFL 1.1)
- SettingsRepository.kt
- SettingsViewModelTest
- BackupRestoreViewModel
- .setContent
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- .createViewModel
- .setContent
- HubWidgetPickerScreen.kt
- OnboardingHomeSetupPage.kt
- ContactRepositoryTest
- 4. Feature Requirements
- AppDrawerScreen.kt
- BackupRestoreContent
- PaddingValues
- HomeScreen
- ClockAdjustSheet.kt
- FakeWidgetPlacementDao
- .setContent
- .drawerViewModel
- AppShortcutRepository
- ResolveWidgetDropUseCaseTest
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- FacetDockAppDao
- FacetNavHost
- CalendarSettingsScreen.kt
- .setContent
- OnboardingScreen
- .setContent
- AppearanceSettingsViewModel.kt
- WallpaperAccentRole
- FacetSettingsViewModel.kt
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Fixture
- ThemedDropdownMenu.kt
- .setContent
- NotificationAccessRepository
- DockAppEntity
- UsageStatsRepository
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- ObserveFacetPreviewsUseCase.kt
- ComponentName
- AppRowPresentation
- Facet Launcher — Built Capabilities
- DockDisplayMode
- Play Console — store listing text
- .setContent
- HubAddWidgetEvent
- Intent
- AppContextMenuTest
- FacetEntityTest
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- DefaultFavoriteAppRepository
- Color.kt
- ClockColorOption
- DrawerViewModel
- combine
- CompactWidgetsUseCaseTest
- FavoritesPickerScreen.kt
- BackButton
- LauncherSettings
- DockAppPickerViewModel
- HubWidgetPickerViewModel
- AppContextMenu
- HubGrid
- FacetDatabaseMigrationTest
- ResolveWidgetResizeUseCaseTest
- AccentSwatch
- .useCase
- AppDrawerSettingsViewModelTest
- AppIcon
- AppModule
- DashedBorder.kt
- DockAppPickerScreen.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- OnboardingIntroPage.kt
- .setContent
- ResolveWidgetDropUseCase
- .setContent
- Facet Launcher — Onboarding Flow
- .setContent
- HubContent
- HubViewModel
- SettingsViewModel
- CalendarEventsBlock.kt
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- NotificationShadeRepository
- SettingsRepositoryTest
- ObserveHubStateUseCase
- PermissionKind
- UsageAccessExplanationViewModel.kt
- FacetDockAppDaoTest
- HomeWallpaper
- ThemeMode
- HubWidgetTile
- 2. Design tokens
- AppInfo
- Type.kt
- NotificationBadgeStyle
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- WidgetResizeHandle.kt
- calculateHubCellWidth
- HubEmptyState.kt
- .rendersOneEntryPerLetterProvided
- FacetDatabase
- ClockColors.kt
- .setContent
- Intent
- ImageVector
- PaddingValues
- AppWidgetHostView
- .createViewModel
- FavoriteAppDaoTest
- SettingsScreen.kt
- Fixture
- ColorTest
- letterAt
- .homeViewModel
- DockSettingsScreen.kt
- StickyHeaderLayout
- Alignment
- Fixture
- ClockAccessoryIconsTest
- CardDivider
- .setContent
- FontWeightOption
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 186 edges
2. `FacetEntity` - 183 edges
3. `SettingsRepository` - 149 edges
4. `Row` - 127 edges
5. `FacetRepository` - 124 edges
6. `LauncherSettings` - 108 edges
7. `AppInfo` - 62 edges
8. `DefaultFavoriteAppRepository` - 57 edges
9. `HomeScreen()` - 57 edges
10. `FacetDockAppRepository` - 55 edges

## Surprising Connections (you probably didn't know these)
- `HubWidgetPickerViewModel` --calls--> `WidgetProviderGroup`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/picker/HubWidgetPickerViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/ui/hub/picker/HubWidgetPickerUiState.kt
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `ContactRepositoryTest` --calls--> `ContactRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/ContactRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/ContactRepository.kt
- `ConvertersTest` --calls--> `Converters`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/local/ConvertersTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/local/Converters.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (189 total, 51 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.08
Nodes (9): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, StateFlow (+1 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - ".setContent"
Cohesion: 0.06
Nodes (21): HomeDrawerRouteTest, BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus() (+13 more)

### Community 3 - "LauncherViewModel"
Cohesion: 0.21
Nodes (6): SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, LauncherViewModelTest

### Community 4 - "HomeDrawerRouteTest.kt"
Cohesion: 0.13
Nodes (9): CatalogEntry, CleanUpUninstalledAppsUseCase, CleanUpUninstalledAppsUseCaseTest, AppOpsManager, AppWidgetManager, CalendarPermissionRepository, DrawerViewModel, NotificationShadeRepository (+1 more)

### Community 5 - "ListContentMode"
Cohesion: 0.11
Nodes (8): Converters, ClockColorOption, ClockFontOption, ClockTemplateId, ListContentMode, FAVORITES, MOST_USED, RECENTS

### Community 6 - "AppRowPosition"
Cohesion: 0.07
Nodes (16): T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT, RIGHT (+8 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "HomeDrawerRoute"
Cohesion: 0.06
Nodes (37): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton(), AlarmClockIcon(), BatteryIcon(), BatteryIconState (+29 more)

### Community 11 - "EnsureActiveFacetUseCase"
Cohesion: 0.18
Nodes (4): EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 12 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (6): FavoritesPickerUiState, FavoritesPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 13 - "FavoriteAppRepository"
Cohesion: 0.09
Nodes (9): FavoriteAppRepository, AppInfo, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest (+1 more)

### Community 14 - "FakeFacetDockAppDao"
Cohesion: 0.20
Nodes (3): FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.09
Nodes (9): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, IntentSender, toBitmap() (+1 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 18 - "FacetEntity"
Cohesion: 0.07
Nodes (11): FacetRepository, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, Flow, toCsv(), FacetEntity (+3 more)

### Community 19 - "ManageFacetsViewModel"
Cohesion: 0.13
Nodes (6): StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 20 - "FacetCarouselViewModel.kt"
Cohesion: 0.12
Nodes (8): FacetCarouselUiState, FacetCarouselViewModel, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, StateFlow, ViewModel

### Community 21 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.06
Nodes (6): ClockColorOption, ClockFontOption, SettingsRepository, StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerPresentation"
Cohesion: 0.08
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.17
Nodes (21): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), AppInfo (+13 more)

### Community 30 - "Row"
Cohesion: 0.13
Nodes (84): ClockDateStyle, CONDENSED, FULL, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS (+76 more)

### Community 31 - "ContactConnectionsSheet.kt"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 33 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (9): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, HubFull, Placed, PlaceWidgetResult (+1 more)

### Community 34 - "DefaultAppRepository"
Cohesion: 0.22
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 35 - ".createViewModel"
Cohesion: 0.24
Nodes (4): DockSettingsViewModelTest, WallpaperRepository, AppInfo, WallpaperRepository

### Community 36 - "FacetDockAppEntity"
Cohesion: 0.21
Nodes (4): AppInfo, Flow, FacetDockAppEntity, toFacetDockAppEntity()

### Community 38 - "ImportBackupUseCase.kt"
Cohesion: 0.08
Nodes (28): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupFacet, BackupSettings, BackupWidgetPlacement, T (+20 more)

### Community 39 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.22
Nodes (16): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+8 more)

### Community 40 - "CalendarSettingsViewModel"
Cohesion: 0.09
Nodes (13): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, CalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo (+5 more)

### Community 41 - "AppearanceSettingsScreen.kt"
Cohesion: 0.32
Nodes (15): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+7 more)

### Community 43 - "SettingsRepository.kt"
Cohesion: 0.14
Nodes (10): DataStoreModule, Context, Keys, ClockDateStyle, ClockTemplateId, Flow, IconRenderMode, LauncherFontOption (+2 more)

### Community 44 - "SettingsViewModelTest"
Cohesion: 0.19
Nodes (4): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, SettingsViewModelTest

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.07
Nodes (17): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+9 more)

### Community 46 - ".setContent"
Cohesion: 0.15
Nodes (9): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, PlaceWidgetUseCase (+1 more)

### Community 47 - "HomeScreen.kt"
Cohesion: 0.23
Nodes (16): AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview(), AppInfo, CalendarEvent, ClockColorOption, ClockDateStyle (+8 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 52 - "HubWidgetPickerScreen.kt"
Cohesion: 0.38
Nodes (11): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), HubWidgetPickerViewModel, Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+3 more)

### Community 53 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.19
Nodes (19): ConfirmDialog(), Modifier, DragReorderState, Modifier, T, rememberDragReorderState(), AppDrawerSection(), DockAppsReorderRow() (+11 more)

### Community 54 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (22): AppRepository, Callback, Callback, flattenIcon(), Bitmap, Flow, FacetNotificationListenerService, NotificationInfo (+14 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.24
Nodes (22): AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile(), DrawerListContent() (+14 more)

### Community 57 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 58 - "PaddingValues"
Cohesion: 0.45
Nodes (10): AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier, ManageFacetsContent(), ManageFacetsHeader(), ManageFacetsScreen(), ManageFacetsScreenPreview() (+2 more)

### Community 59 - "HomeScreen"
Cohesion: 0.14
Nodes (3): HomeScreenTest, AppInfo, HomeScreen()

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "FakeWidgetPlacementDao"
Cohesion: 0.36
Nodes (3): FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 62 - ".setContent"
Cohesion: 0.14
Nodes (12): KeyboardDismissalTest, SystemSettingsRepository, AddAppToDockUseCase, AppInfo, AddAppToFavoritesUseCase, AppInfo, Flow, ObserveQuickAddStateUseCase (+4 more)

### Community 64 - "AppShortcutRepository"
Cohesion: 0.24
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 70 - "FacetNavHost"
Cohesion: 0.26
Nodes (6): FacetDestinations, FacetNavHost(), AppInfo, Modifier, popBackStackSafely(), NavHostController

### Community 71 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (13): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+5 more)

### Community 73 - "OnboardingScreen"
Cohesion: 0.20
Nodes (13): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO, SET_DEFAULT (+5 more)

### Community 75 - "AppearanceSettingsViewModel.kt"
Cohesion: 0.20
Nodes (8): AppearanceSettingsViewModel, AccentSwatch, AppInfo, ClockColorOption, IconRenderMode, LauncherFontOption, StateFlow, ViewModel

### Community 76 - "WallpaperAccentRole"
Cohesion: 0.15
Nodes (9): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY, SECONDARY, TERTIARY (+1 more)

### Community 77 - "FacetSettingsViewModel.kt"
Cohesion: 0.23
Nodes (9): FacetSettingsUiState, FacetSettingsViewModel, AppInfo, ClockColorOption, ClockDateStyle, ClockFontOption, ClockTemplateId, StateFlow (+1 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "ThemedDropdownMenu.kt"
Cohesion: 0.42
Nodes (8): Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), DpOffset, Shape

### Community 81 - ".setContent"
Cohesion: 0.27
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 82 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (11): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow (+3 more)

### Community 83 - "DockAppEntity"
Cohesion: 0.09
Nodes (9): DockAppRepository, Flow, DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow (+1 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.40
Nodes (6): FacetPreviewData, Inputs, AppInfo, CalendarEvent, Flow, ObserveFacetPreviewsUseCase

### Community 89 - "ComponentName"
Cohesion: 0.18
Nodes (6): Intent, Intent, LauncherActivity, Bundle, ComponentActivity, ComponentName

### Community 90 - "AppRowPresentation"
Cohesion: 0.24
Nodes (9): AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY, HomeSurfacePreview(), AppInfo, Color, FontWeight (+1 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "DockDisplayMode"
Cohesion: 0.20
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, AppInfo, StateFlow, ViewModel

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 95 - "HubAddWidgetEvent"
Cohesion: 0.18
Nodes (10): AddFailed, AddFailureReason, HUB_FULL, SETUP_CANCELLED, HubAddWidgetEvent, HubWidgetPickerUiState, LaunchBindPermission, LaunchConfigure (+2 more)

### Community 96 - "Intent"
Cohesion: 0.05
Nodes (26): SettingsScreenTest, ContactRepository, ContactInfo, LabeledValue, DefaultLauncherRepository, Modifier, OnboardingDots(), FacetSwitchDemo() (+18 more)

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.11
Nodes (13): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), CalendarEvent, ClockColorOption, ClockDateStyle, ClockFontOption (+5 more)

### Community 101 - "TonalButton"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview(), Modifier (+1 more)

### Community 102 - "DefaultFavoriteAppRepository"
Cohesion: 0.11
Nodes (10): DockSettingsScreenTest, DefaultFavoriteAppRepository, AppInfo, Flow, FacetDockAppRepository, AppListLimits, AppRepository, DockAppRepository (+2 more)

### Community 103 - "Color.kt"
Cohesion: 0.32
Nodes (10): Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview(), accentTonalExtremes(), homeAppLabelShadow(), Color, toneOf(), wallpaperPrimaryAndSecondary() (+2 more)

### Community 104 - "ClockColorOption"
Cohesion: 0.18
Nodes (6): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ConvertersTest

### Community 105 - "DrawerViewModel"
Cohesion: 0.22
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 106 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 108 - "FavoritesPickerScreen.kt"
Cohesion: 0.47
Nodes (9): AppIconSize, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), AppInfo, Modifier (+1 more)

### Community 109 - "BackButton"
Cohesion: 0.26
Nodes (11): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+3 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.18
Nodes (4): LauncherSettings, HomeUiState, FacetCarouselUiStateTest, HomeUiStateTest

### Community 111 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (6): DockAppPickerUiState, DockAppPickerViewModel, AppInfo, Flow, StateFlow, ViewModel

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (6): WidgetProviderOption, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 113 - "AppContextMenu"
Cohesion: 0.36
Nodes (6): SharedFlow, AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), AppInfo, Modifier

### Community 114 - "HubGrid"
Cohesion: 0.21
Nodes (11): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+3 more)

### Community 117 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 118 - ".useCase"
Cohesion: 0.11
Nodes (4): AssignCalendarColorsUseCaseTest, PlaceWidgetUseCaseTest, SeedDefaultDockUseCaseTest, SelectPreviewAppsUseCaseTest

### Community 120 - "AppIcon"
Cohesion: 0.53
Nodes (8): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 122 - "DashedBorder.kt"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier

### Community 123 - "DockAppPickerScreen.kt"
Cohesion: 0.56
Nodes (8): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), AppInfo, Modifier, PickerSectionHeader()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "OnboardingIntroPage.kt"
Cohesion: 0.53
Nodes (8): AppRowBar(), ConceptLine(), HomeDiagram(), Dp, Modifier, LeaderRow(), OnboardingIntroPage(), OnboardingIntroPagePreview()

### Community 133 - "ResolveWidgetDropUseCase"
Cohesion: 0.21
Nodes (6): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, HubViewModelTest

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 137 - "HubContent"
Cohesion: 0.43
Nodes (7): HubContent(), HubScreen(), Context, HubViewModel, Modifier, AppWidgetHostView, HubUiState

### Community 138 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 139 - "SettingsViewModel"
Cohesion: 0.36
Nodes (5): SettingsUiState, Intent, StateFlow, ViewModel, SettingsViewModel

### Community 140 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.12
Nodes (7): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 147 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 148 - "UsageAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 150 - "HomeWallpaper"
Cohesion: 0.05
Nodes (22): HomeWallpaper, Image, Tones, Unavailable, Bitmap, WallpaperRepository, Alignment, Modifier (+14 more)

### Community 151 - "ThemeMode"
Cohesion: 0.33
Nodes (4): ThemeMode, DARK, LIGHT, SYSTEM

### Community 152 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 154 - "AppInfo"
Cohesion: 0.13
Nodes (8): AppInfo, GetInstalledAppsUseCase, Flow, GroupAppsByLetterUseCase, GroupedApps, SelectPreviewAppsUseCase, GetInstalledAppsUseCaseTest, GroupAppsByLetterUseCaseTest

### Community 155 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 156 - "NotificationBadgeStyle"
Cohesion: 0.24
Nodes (6): NotificationBadgeStyle, COUNT, DOT, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 159 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 161 - "HubEmptyState.kt"
Cohesion: 0.83
Nodes (3): HubEmptyState(), HubEmptyStatePreview(), Modifier

### Community 163 - "FacetDatabase"
Cohesion: 0.12
Nodes (12): DatabaseModule, Context, DefaultFavoriteAppDao, DockAppDao, WidgetPlacementDao, FacetDatabase, DefaultFavoriteAppDao, DockAppDao (+4 more)

### Community 165 - ".setContent"
Cohesion: 0.14
Nodes (14): FacetSettingsScreenTest, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron() (+6 more)

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 176 - "FavoriteAppDaoTest"
Cohesion: 0.19
Nodes (5): FavoriteAppDaoTest, AddAppToDockUseCaseTest, Fixture, AddAppToFavoritesUseCaseTest, Fixture

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (13): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 203 - ".homeViewModel"
Cohesion: 0.17
Nodes (6): HomeScreenState, AppInfo, CalendarEvent, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+1 more)

### Community 211 - "StickyHeaderLayout"
Cohesion: 0.36
Nodes (10): Modifier, StickyHeaderLayout(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow() (+2 more)

### Community 213 - "Alignment"
Cohesion: 0.23
Nodes (15): Alignment, Modifier, T, LabeledDropdownRow(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+7 more)

### Community 222 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+8 more)

### Community 233 - "FontWeightOption"
Cohesion: 0.10
Nodes (12): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, ClockAlignment (+4 more)

## Knowledge Gaps
- **366 isolated node(s):** `1. Overview`, `2. Goals`, `3. Non-Goals (v1)`, `3a. Parked for Future Consideration`, `F1. Home clock widget + calendar integration` (+361 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 669 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockStyleGalleryViewModel`, `.setContent`, `OnboardingIntroPage.kt`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `HomeDrawerRoute`, `ThemeMode`, `.setContent`, `AppInfo`, `Type.kt`, `NotificationBadgeStyle`, `FacetCarouselScreen.kt`, `Row`, `ContactConnectionsSheet.kt`, `.setContent`, `WidgetPlacementEntity`, `.rendersOneEntryPerLetterProvided`, `HubEmptyState.kt`, `WidgetResizeHandle.kt`, `.setContent`, `ClockStyleGalleryScreen.kt`, `CalendarSettingsViewModel`, `AppearanceSettingsScreen.kt`, `.setContent`, `HomeScreen.kt`, `.setContent`, `HubWidgetPickerScreen.kt`, `OnboardingHomeSetupPage.kt`, `AppDrawerScreen.kt`, `BackupRestoreContent`, `PaddingValues`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `.setContent`, `ColorTest`, `CalendarSettingsScreen.kt`, `.setContent`, `.setContent`, `WallpaperAccentRole`, `DockSettingsScreen.kt`, `.setContent`, `NotificationAccessRepository`, `StickyHeaderLayout`, `Alignment`, `.setContent`, `ComponentName`, `.setContent`, `CardDivider`, `.setContent`, `AppContextMenuTest`, `Intent`, `AppDrawerScreen`, `TonalButton`, `DefaultFavoriteAppRepository`, `Color.kt`, `FavoritesPickerScreen.kt`, `BackButton`, `AccentSwatch`, `DashedBorder.kt`, `DockAppPickerScreen.kt`?**
  _High betweenness centrality (0.145) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `LauncherViewModel`, `HomeDrawerRouteTest.kt`, `ListContentMode`, `.setContent`, `AppRowPosition`, `.setContent`, `HomeDrawerRoute`, `EnsureActiveFacetUseCase`, `SettingsRepositoryTest`, `ManageFacetsViewModel`, `FacetCarouselViewModel.kt`, `HomeWallpaper`, `ThemeMode`, `.setContent`, `AppInfo`, `NotificationBadgeStyle`, `DrawerPresentation`, `.setContent`, `DefaultAppRepository`, `.createViewModel`, `.setContent`, `ImportBackupUseCase.kt`, `ClockStyleGalleryViewModelTest`, `CalendarSettingsViewModel`, `.createViewModel`, `SettingsRepository.kt`, `SettingsViewModelTest`, `.setContent`, `.createViewModel`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `AppearanceSettingsViewModel.kt`, `WallpaperAccentRole`, `FacetSettingsViewModel.kt`, `.homeViewModel`, `NotificationAccessRepository`, `ObserveFacetPreviewsUseCase.kt`, `AppRowPresentation`, `DockDisplayMode`, `Intent`, `DefaultFavoriteAppRepository`, `FontWeightOption`, `LauncherSettings`, `.useCase`, `AppDrawerSettingsViewModelTest`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **Why does `Row` connect `Row` to `OnboardingIntroPage.kt`, `HomeDrawerRoute`, `CalendarEventsBlock.kt`, `FacetCarouselScreen.kt`, `ContactConnectionsSheet.kt`, `.setContent`, `ClockStyleGalleryScreen.kt`, `AppearanceSettingsScreen.kt`, `HomeScreen.kt`, `HubWidgetPickerScreen.kt`, `OnboardingHomeSetupPage.kt`, `ContactRepositoryTest`, `AppDrawerScreen.kt`, `BackupRestoreContent`, `PaddingValues`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `CalendarSettingsScreen.kt`, `DockSettingsScreen.kt`, `ThemedDropdownMenu.kt`, `StickyHeaderLayout`, `Alignment`, `AppRowPresentation`, `CardDivider`, `Intent`, `AppDrawerScreen`, `TonalButton`, `Color.kt`, `FavoritesPickerScreen.kt`, `BackButton`, `AppContextMenu`, `DashedBorder.kt`, `DockAppPickerScreen.kt`?**
  _High betweenness centrality (0.062) - this node is a cross-community bridge._
- **Are the 71 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 71 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `FacetRepository` (e.g. with `.`addFacet names it Facet N and appends after the last position`()` and `.`deleteFacet removes it`()`) actually correct?**
  _`FacetRepository` has 19 INFERRED edges - model-reasoned connections that need verification._