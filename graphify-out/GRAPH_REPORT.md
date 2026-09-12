# Graph Report - lumen-launcher  (2026-09-12)

## Corpus Check
- 353 files · ~787,645 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3329 nodes · 9038 edges · 165 communities (111 shown, 48 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 724 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `43f64203`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- .setContent
- GetInstalledAppsUseCase
- .setContent
- ListContentMode
- FakeFacetDao
- Android launcher design planning/support.js
- .setContent
- Screens
- HomeDrawerRoute
- EnsureActiveFacetUseCase
- FavoritesPickerViewModel
- FavoriteAppRepository
- FacetDockAppRepository
- AppWidgetRepository
- Screens
- ClockAlignment
- FacetEntity
- ManageFacetsViewModel
- FacetCarouselUiState
- FacetDao
- SystemSettingsRepositoryTest
- 4. Feature Requirements
- SettingsRepository
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- AppDrawerSettingsViewModel
- FacetCarouselScreen.kt
- Row
- ContactRepositoryTest
- .setContent
- .setContent
- DefaultAppRepositoryTest
- .createViewModel
- FacetDockAppEntity
- BackupMapping.kt
- ImportBackupUseCaseTest.kt
- ClockAccessoryIcons.kt
- CalendarRepository
- AppearanceSettingsScreen.kt
- Manrope Font License (SIL OFL 1.1)
- LauncherFontOption
- NotificationBadgeRepository
- BackupRestoreViewModel
- .setContent
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- NextAlarmRepositoryTest
- .setContent
- WidgetProviderOption
- CardDivider
- .refresh
- 4. Feature Requirements
- AppDrawerScreen.kt
- BackupRestoreContent
- ManageFacetsScreen.kt
- HomeScreen
- ClockAdjustSheet.kt
- FakeWidgetPlacementDao
- HomeDrawerRouteTest.kt
- .drawerViewModel
- BackupRestoreMessage
- ResolveWidgetDropUseCaseTest
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- FacetDockAppDao
- FacetNavHost
- NextAlarmRepository
- .setContent
- OnboardingScreen
- .setContent
- BatteryRepository.kt
- BackButton
- SelectPreviewAppsUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Fixture
- LabeledDropdownRow
- DockAppDao
- CalendarPermissionRepository
- DockAppRepository
- GestureHintOverlay
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- ObserveFacetPreviewsUseCase.kt
- LauncherActivity.kt
- .failAndRelease
- Facet Launcher — Built Capabilities
- DockSettingsViewModel.kt
- Play Console — store listing text
- .setContent
- HubAddWidgetEvent
- combine
- AppContextMenuTest
- FacetEntityTest
- AppDrawerScreen
- FacetLauncherTheme
- ClockAdjustMode
- AppInfo
- WallpaperRepository
- CompactWidgetsUseCaseTest
- NotificationAccessExplanationScreen.kt
- LauncherSettings
- HubWidgetPickerViewModel
- HubGrid
- FacetDatabaseMigrationTest
- ResolveWidgetResizeUseCaseTest
- AccentSwatch
- .useCase
- AppDrawerSettingsViewModelTest
- AppModule
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- WidgetPlacementEntity
- Facet Launcher — Onboarding Flow
- HubViewModel
- rememberTickingNow
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- SettingsRepositoryTest
- DockAppDaoTest
- UsageAccessExplanationScreen.kt
- OnboardingViewModelTest
- 2. Design tokens
- GroupAppsByLetterUseCaseTest
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- SeedDefaultDockUseCaseTest
- FacetDatabase
- DefaultAppRepository
- DockDisplayMode
- .createViewModel
- AddAppToDockUseCaseTest
- AddAppToFavoritesUseCaseTest
- BackupRestoreViewModelTest
- SettingsScreen.kt
- Fixture
- .createViewModel
- ColorTest
- letterAt
- BatteryStatus
- ObserveHomeScreenStateUseCase.kt
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- HomeAppsListSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- FontWeightOption
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 228 edges
2. `AppInfo` - 184 edges
3. `FacetEntity` - 183 edges
4. `SettingsRepository` - 149 edges
5. `Row` - 127 edges
6. `FacetRepository` - 124 edges
7. `LauncherSettings` - 108 edges
8. `ClockTemplateId` - 73 edges
9. `WidgetPlacementEntity` - 61 edges
10. `ClockDateStyle` - 61 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `BatteryRepositoryTest` --calls--> `BatteryRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/BatteryRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/BatteryRepository.kt
- `CalendarRepositoryTest` --calls--> `CalendarRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/CalendarRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/CalendarRepository.kt
- `DefaultAppRepositoryTest` --calls--> `DefaultAppRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/DefaultAppRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/DefaultAppRepository.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (165 total, 48 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.05
Nodes (6): ClockStyleGalleryScreenTest, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 3 - "GetInstalledAppsUseCase"
Cohesion: 0.13
Nodes (9): GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, GetInstalledAppsUseCaseTest (+1 more)

### Community 4 - ".setContent"
Cohesion: 0.18
Nodes (7): BackupRestoreScreenTest, ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 5 - "ListContentMode"
Cohesion: 0.05
Nodes (30): Converters, T, resolveOverride(), AppListLimits, ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS (+22 more)

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
Cohesion: 0.14
Nodes (14): FacetCarouselScreen(), FacetCarouselViewModel, Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection (+6 more)

### Community 11 - "EnsureActiveFacetUseCase"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow, DataStore, Preferences

### Community 12 - "FavoritesPickerViewModel"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 13 - "FavoriteAppRepository"
Cohesion: 0.08
Nodes (9): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow (+1 more)

### Community 14 - "FacetDockAppRepository"
Cohesion: 0.17
Nodes (5): FacetDockAppRepository, Flow, FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "ClockAlignment"
Cohesion: 0.16
Nodes (16): CalendarEvent, ClockAlignment, CENTER, LEFT, RIGHT, CalendarEventsBlock(), EventRow(), Color (+8 more)

### Community 18 - "FacetEntity"
Cohesion: 0.08
Nodes (4): FacetRepository, Flow, toCsv(), FacetEntity

### Community 19 - "ManageFacetsViewModel"
Cohesion: 0.13
Nodes (6): StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 21 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.03
Nodes (38): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE (+30 more)

### Community 25 - ".setContent"
Cohesion: 0.06
Nodes (19): OnboardingScreenTest, Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage() (+11 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "AppDrawerSettingsViewModel"
Cohesion: 0.14
Nodes (4): AppDrawerSettingsScreenTest, AppDrawerSettingsViewModel, StateFlow, ViewModel

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.25
Nodes (13): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier, NestedScrollConnection (+5 more)

### Community 30 - "Row"
Cohesion: 0.18
Nodes (73): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+65 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (29): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+21 more)

### Community 32 - ".setContent"
Cohesion: 0.13
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 37 - "BackupMapping.kt"
Cohesion: 0.20
Nodes (13): BackupAppEntry, BackupFacet, BackupWidgetPlacement, T, toBackupEntry(), toBackupFacet(), toBackupPlacement(), toDefaultFavoriteAppEntity() (+5 more)

### Community 38 - "ImportBackupUseCaseTest.kt"
Cohesion: 0.13
Nodes (10): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest (+2 more)

### Community 39 - "ClockAccessoryIcons.kt"
Cohesion: 0.31
Nodes (10): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+2 more)

### Community 40 - "CalendarRepository"
Cohesion: 0.11
Nodes (7): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo, CalendarSettingsViewModelTest

### Community 41 - "AppearanceSettingsScreen.kt"
Cohesion: 0.14
Nodes (26): HomeWallpaper, Image, Tones, Unavailable, HomeSurfacePreview(), Color, FontWeight, Modifier (+18 more)

### Community 43 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 44 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.23
Nodes (5): BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 46 - ".setContent"
Cohesion: 0.15
Nodes (9): HubScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 47 - "HomeScreen.kt"
Cohesion: 0.20
Nodes (21): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel() (+13 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 52 - "WidgetProviderOption"
Cohesion: 0.37
Nodes (10): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+2 more)

### Community 53 - "CardDivider"
Cohesion: 0.27
Nodes (16): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+8 more)

### Community 54 - ".refresh"
Cohesion: 0.12
Nodes (11): Callback, Callback, flattenIcon(), Bitmap, Flow, FacetNotificationListenerService, Callback, Drawable (+3 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.13
Nodes (29): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT (+21 more)

### Community 57 - "BackupRestoreContent"
Cohesion: 0.49
Nodes (9): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+1 more)

### Community 58 - "ManageFacetsScreen.kt"
Cohesion: 0.45
Nodes (10): AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier, PaddingValues, ManageFacetsContent(), ManageFacetsHeader(), ManageFacetsScreen() (+2 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "FakeWidgetPlacementDao"
Cohesion: 0.32
Nodes (3): FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 62 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (19): KeyboardDismissalTest, BatteryRepository, ContactPermissionRepository, NotificationShadeRepository, SystemSettingsRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, CleanUpUninstalledAppsUseCase (+11 more)

### Community 64 - "BackupRestoreMessage"
Cohesion: 0.18
Nodes (10): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+2 more)

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 70 - "FacetNavHost"
Cohesion: 0.19
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), FacetClockStyleGalleryScreen(), Modifier (+5 more)

### Community 71 - "NextAlarmRepository"
Cohesion: 0.31
Nodes (6): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver

### Community 73 - "OnboardingScreen"
Cohesion: 0.12
Nodes (25): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+17 more)

### Community 75 - "BatteryRepository.kt"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus()

### Community 76 - "BackButton"
Cohesion: 0.42
Nodes (7): BackButton(), Modifier, displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview()

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 82 - "CalendarPermissionRepository"
Cohesion: 0.10
Nodes (10): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow, ViewModel (+2 more)

### Community 83 - "DockAppRepository"
Cohesion: 0.13
Nodes (6): DockAppRepository, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 84 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.50
Nodes (4): FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase

### Community 89 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 90 - ".failAndRelease"
Cohesion: 0.36
Nodes (3): AddFailureReason, HUB_FULL, SETUP_CANCELLED

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "DockSettingsViewModel.kt"
Cohesion: 0.43
Nodes (4): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 95 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 96 - "combine"
Cohesion: 0.05
Nodes (21): SettingsScreenTest, DefaultLauncherRepository, Intent, combine(), Flow, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState (+13 more)

### Community 97 - "AppContextMenuTest"
Cohesion: 0.10
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 99 - "AppDrawerScreen"
Cohesion: 0.13
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (4): AlphabetRailTest, ClockBlockTest, ClockBlock(), FacetLauncherTheme()

### Community 101 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 102 - "AppInfo"
Cohesion: 0.09
Nodes (10): AppRepository, DefaultFavoriteAppRepository, Flow, AppInfo, SelectPreviewAppsUseCase, AppRepositoryTest, CleanUpUninstalledAppsUseCaseTest, LauncherActivityInfo (+2 more)

### Community 104 - "WallpaperRepository"
Cohesion: 0.23
Nodes (4): DockSettingsScreenTest, Bitmap, WallpaperRepository, WallpaperManager

### Community 109 - "NotificationAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 110 - "LauncherSettings"
Cohesion: 0.19
Nodes (4): LauncherSettings, HomeUiState, FacetCarouselUiStateTest, HomeUiStateTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (6): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 114 - "HubGrid"
Cohesion: 0.05
Nodes (56): dashedBorder(), Color, Dp, Modifier, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector (+48 more)

### Community 117 - "AccentSwatch"
Cohesion: 0.08
Nodes (29): AssignCalendarColorsUseCase, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader() (+21 more)

### Community 121 - "AppModule"
Cohesion: 0.29
Nodes (3): AppModule, Context, AppOpsManager

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.08
Nodes (18): WidgetPlacementEntity, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow (+10 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 140 - "rememberTickingNow"
Cohesion: 0.39
Nodes (6): BroadcastReceiver, Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.12
Nodes (6): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 148 - "UsageAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 156 - ".setContent"
Cohesion: 0.18
Nodes (5): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 163 - "FacetDatabase"
Cohesion: 0.12
Nodes (8): DatabaseModule, Context, FacetDatabase, Migrations, Flow, WidgetPlacementDao, Migration, RoomDatabase

### Community 165 - "DockDisplayMode"
Cohesion: 0.12
Nodes (20): DockDisplayMode, ICONS, TEXT, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable (+12 more)

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 192 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 196 - "BatteryStatus"
Cohesion: 0.28
Nodes (3): BatteryStatus, BatteryRepositoryTest, ObserveClockAccessoriesUseCaseTest

### Community 203 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.10
Nodes (9): ClockAccessoryState, Flow, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel (+1 more)

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+1 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 213 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.22
Nodes (13): DragReorderState, Modifier, T, rememberDragReorderState(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+5 more)

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.19
Nodes (15): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+7 more)

### Community 233 - "FontWeightOption"
Cohesion: 0.09
Nodes (16): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+8 more)

## Knowledge Gaps
- **365 isolated node(s):** `Keys`, `CatalogEntry`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY` (+360 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 666 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **48 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ClockStyleGalleryViewModel`, `.setContent`, `.setContent`, `WidgetPlacementEntity`, `.setContent`, `FavoritesPickerViewModel`, `ClockAlignment`, `UsageAccessExplanationScreen.kt`, `SettingsRepository`, `.setContent`, `AppDrawerSettingsViewModel`, `.setContent`, `FacetCarouselScreen.kt`, `ContactRepositoryTest`, `.setContent`, `.setContent`, `Row`, `DockDisplayMode`, `CalendarRepository`, `AppearanceSettingsScreen.kt`, `LauncherFontOption`, `.setContent`, `HomeScreen.kt`, `.setContent`, `WidgetProviderOption`, `CardDivider`, `AppDrawerScreen.kt`, `BackupRestoreContent`, `ManageFacetsScreen.kt`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `HomeDrawerRouteTest.kt`, `ColorTest`, `FacetNavHost`, `.setContent`, `OnboardingScreen`, `.setContent`, `BackButton`, `DockSettingsScreen.kt`, `CalendarPermissionRepository`, `AppDrawerSettingsScreen.kt`, `GestureHintOverlay`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `LauncherActivity.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `AppContextMenuTest`, `combine`, `AppDrawerScreen`, `AppInfo`, `WallpaperRepository`, `FontWeightOption`, `NotificationAccessExplanationScreen.kt`, `HubGrid`, `AccentSwatch`?**
  _High betweenness centrality (0.170) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `.setContent`, `GetInstalledAppsUseCase`, `ListContentMode`, `HomeDrawerRoute`, `FavoritesPickerViewModel`, `FavoriteAppRepository`, `FacetDockAppRepository`, `DefaultFavoriteAppEntity`, `ClockAlignment`, `FacetEntity`, `OnboardingViewModelTest`, `SettingsRepository`, `.setContent`, `GroupAppsByLetterUseCaseTest`, `FacetCarouselScreen.kt`, `.setContent`, `SeedDefaultDockUseCaseTest`, `.createViewModel`, `FacetDockAppEntity`, `DockDisplayMode`, `AppearanceSettingsScreen.kt`, `HomeScreen.kt`, `AddAppToDockUseCaseTest`, `AddAppToFavoritesUseCaseTest`, `CardDivider`, `.refresh`, `AppDrawerScreen.kt`, `HomeScreen`, `SettingsScreen.kt`, `HomeDrawerRouteTest.kt`, `Fixture`, `.createViewModel`, `FacetNavHost`, `OnboardingScreen`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `SelectPreviewAppsUseCaseTest`, `DockSettingsScreen.kt`, `Fixture`, `CalendarPermissionRepository`, `DockAppRepository`, `HomeAppsListSettingsScreen.kt`, `ObserveFacetPreviewsUseCase.kt`, `LauncherActivity.kt`, `Fixture`, `DockSettingsViewModel.kt`, `combine`, `AppContextMenuTest`, `AppDrawerScreen`, `LauncherSettings`, `.useCase`?**
  _High betweenness centrality (0.121) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `GetInstalledAppsUseCase`, `.setContent`, `ListContentMode`, `EnsureActiveFacetUseCase`, `ClockAlignment`, `SettingsRepositoryTest`, `ManageFacetsViewModel`, `FacetEntity`, `.setContent`, `.setContent`, `AppDrawerSettingsViewModel`, `.setContent`, `.setContent`, `SeedDefaultDockUseCaseTest`, `.createViewModel`, `DefaultAppRepository`, `DockDisplayMode`, `ImportBackupUseCaseTest.kt`, `CalendarRepository`, `.createViewModel`, `LauncherFontOption`, `.setContent`, `HomeDrawerRouteTest.kt`, `.createViewModel`, `.setContent`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `CalendarPermissionRepository`, `DockAppRepository`, `ObserveFacetPreviewsUseCase.kt`, `DockSettingsViewModel.kt`, `combine`, `AppInfo`, `WallpaperRepository`, `FontWeightOption`, `NotificationAccessExplanationScreen.kt`, `LauncherSettings`, `AccentSwatch`, `.useCase`, `AppDrawerSettingsViewModelTest`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.themed()` and `facetTypography()`) actually correct?**
  _`FacetLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `CatalogEntry`, `THEME` to the rest of the system?**
  _365 weakly-connected nodes found - possible documentation gaps or missing edges._