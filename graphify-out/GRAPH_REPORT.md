# Graph Report - lumen-launcher  (2026-09-13)

## Corpus Check
- 356 files · ~791,155 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3395 nodes · 9092 edges · 196 communities (131 shown, 59 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 818 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8e09e076`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModelTest
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- BackupRestoreViewModelTest.kt
- HomeDrawerRouteTest.kt
- HomeAppsListSettingsViewModelTest.kt
- Android launcher design planning/support.js
- .setContent
- Screens
- ClockAccessoryIcons.kt
- FakeFacetDao
- .setContent
- FavoriteAppRepository
- FacetDockAppRepository
- AppWidgetRepository
- Screens
- ObserveHomeScreenStateUseCase.kt
- FacetEntity
- NotificationAccessRepository
- FacetCarouselUiState
- AppModule
- SettingsSearchEntry
- 4. Feature Requirements
- LauncherFontOption
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- ImportBackupUseCase.kt
- FacetCarouselScreen.kt
- Row
- ListContentMode
- .setContent
- FakeFacetDao
- HubWidgetPickerScreen.kt
- .createViewModel
- AppRepository
- .setContent
- BackupMapping.kt
- homeAppLabelShadow
- SelectPreviewAppsUseCaseTest
- .setContent
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- FacetDao
- BackupRestoreViewModel
- HubGrid
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- CalendarInfo
- FacetDockAppDaoTest
- AppearanceSettingsScreen.kt
- CardDivider
- .refresh
- 4. Feature Requirements
- AppDrawerScreen.kt
- .setContent
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- WidgetPlacementRepository
- HomeWallpaper
- WallpaperRepository
- SettingsViewModelTest
- WidgetResizeHandle.kt
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- .setContent
- FacetNavHost
- .drawerViewModel
- ManageFacetsViewModel
- NextAlarmRepositoryTest
- ClockFontOption
- SettingsRepositoryTest.kt
- BackupRestoreViewModelTest
- ResolveWidgetDropUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- .setContent
- DockAppPickerScreen.kt
- OnboardingViewModel
- .setContent
- DockAppRepository
- PlaceWidgetUseCase
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- combine
- NotificationBadgeRepository
- Fixture
- Facet Launcher — Built Capabilities
- NotificationAccessExplanationScreen.kt
- Play Console — store listing text
- GestureHintOverlay
- HomeViewModel
- ClockColorOption
- AppContextMenuTest
- OnboardingFacetsPage.kt
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- .setContent
- DockAppDao
- ComponentName
- DockSettingsScreen.kt
- CompactWidgetsUseCaseTest
- Fixture
- BackButton
- LauncherSettings
- ObserveFacetPreviewsUseCase.kt
- HubWidgetPickerViewModel
- ResolveWidgetResizeUseCaseTest
- NotificationShadeRepository
- FacetDatabaseMigrationTest
- ObserveHubStateUseCase
- CalendarEventsBlock.kt
- .useCase
- AppDrawerSettingsViewModelTest
- AccentSwatch
- .setContent
- SetDefaultLauncherSheetTest
- DefaultAppRepository
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ContactRepository
- AppInfo
- WidgetPlacementEntity
- HubContent
- Facet Launcher — Onboarding Flow
- ObserveClockAccessoriesUseCase.kt
- FacetEntityTest
- HubViewModel
- .setContent
- ClockAdjustMode
- DefaultFavoriteAppRepository
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- .setContent
- SettingsRepositoryTest
- AddAppToFavoritesUseCaseTest
- CalendarSettingsViewModel
- DrawerViewModel
- androidx
- OnboardingViewModelTest
- NestedScrollConnection
- NestedScrollSource
- 2. Design tokens
- GroupAppsByLetterUseCaseTest
- FontWeightOption
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- Offset
- Intent
- WallpaperRepository
- ClockStyleGalleryScreen.kt
- FacetDatabase
- CalendarSettingsScreen.kt
- DockDisplayMode
- OnboardingIntroPage.kt
- BackupRestoreContent
- Color.kt
- WidgetPlacementDao
- .createViewModel
- CalendarRepository
- FacetCarouselViewModel.kt
- NotificationSettingsScreen.kt
- BackupRestoreMessage
- AppIcon
- AppContextMenu
- InheritOverrideCard
- calculateHubCellWidth
- .rendersOneEntryPerLetterProvided
- DefaultAppRepositoryTest
- SeedDefaultDockUseCaseTest
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- HomeAppsListSettingsScreen.kt
- StickyHeaderLayout
- Fixture
- ClockAccessoryIconsTest
- PermissionsScreen.kt
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 226 edges
2. `FacetEntity` - 178 edges
3. `AppInfo` - 169 edges
4. `SettingsRepository` - 139 edges
5. `Row` - 127 edges
6. `FacetRepository` - 117 edges
7. `LauncherSettings` - 99 edges
8. `ClockTemplateId` - 71 edges
9. `WidgetPlacementEntity` - 60 edges
10. `ClockDateStyle` - 59 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `ClockStyleGalleryViewModel` --calls--> `combine()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/clock/ClockStyleGalleryViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/domain/FlowCombine.kt
- `CompactWidgetsUseCaseTest` --calls--> `CompactWidgetsUseCase`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/domain/CompactWidgetsUseCaseTest.kt → app/src/main/kotlin/com/facetlauncher/app/domain/CompactWidgetsUseCase.kt
- `Fixture` --calls--> `ObserveFacetPreviewsUseCase`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/domain/ObserveFacetPreviewsUseCaseTest.kt → app/src/main/kotlin/com/facetlauncher/app/domain/ObserveFacetPreviewsUseCase.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (196 total, 59 thin omitted)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.16
Nodes (10): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+2 more)

### Community 4 - "BackupRestoreViewModelTest.kt"
Cohesion: 0.25
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 5 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (10): flattenIcon(), Bitmap, AppListLimits, CatalogEntry, CleanUpUninstalledAppsUseCase, Drawable, DrawerPresentation, LauncherApps (+2 more)

### Community 6 - "HomeAppsListSettingsViewModelTest.kt"
Cohesion: 0.16
Nodes (5): SelectPreviewAppsUseCase, fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "ClockAccessoryIcons.kt"
Cohesion: 0.16
Nodes (17): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+9 more)

### Community 11 - "FakeFacetDao"
Cohesion: 0.15
Nodes (8): DataStoreModule, Context, EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow, DataStore, Preferences

### Community 12 - ".setContent"
Cohesion: 0.15
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 13 - "FavoriteAppRepository"
Cohesion: 0.09
Nodes (9): FavoriteAppRepository, FavoriteAppDao, Flow, FavoriteAppEntity, toFavoriteAppEntity(), FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow (+1 more)

### Community 14 - "FacetDockAppRepository"
Cohesion: 0.12
Nodes (7): FacetDockAppRepository, Flow, FacetDockAppEntity, toFacetDockAppEntity(), FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.09
Nodes (10): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+2 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.46
Nodes (3): HomeScreenState, Flow, ObserveHomeScreenStateUseCase

### Community 18 - "FacetEntity"
Cohesion: 0.10
Nodes (6): CalendarPermissionRepository, FacetRepository, toCsv(), FacetEntity, AddAppToDockUseCaseTest, Fixture

### Community 19 - "NotificationAccessRepository"
Cohesion: 0.10
Nodes (10): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow, ViewModel (+2 more)

### Community 21 - "AppModule"
Cohesion: 0.29
Nodes (3): AppModule, Context, AppOpsManager

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "LauncherFontOption"
Cohesion: 0.13
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 25 - ".setContent"
Cohesion: 0.10
Nodes (14): OnboardingScreenTest, Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO (+6 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "ImportBackupUseCase.kt"
Cohesion: 0.09
Nodes (19): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+11 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier (+6 more)

### Community 30 - "Row"
Cohesion: 0.22
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ListContentMode"
Cohesion: 0.05
Nodes (23): Flow, Converters, T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition (+15 more)

### Community 32 - ".setContent"
Cohesion: 0.12
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 33 - "FakeFacetDao"
Cohesion: 0.12
Nodes (3): FacetRepositoryTest, FakeFacetDao, Flow

### Community 34 - "HubWidgetPickerScreen.kt"
Cohesion: 0.18
Nodes (17): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile(), AddFailed (+9 more)

### Community 36 - "AppRepository"
Cohesion: 0.08
Nodes (11): FacetSettingsScreenTest, AppRepository, any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow (+3 more)

### Community 37 - ".setContent"
Cohesion: 0.16
Nodes (9): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 38 - "BackupMapping.kt"
Cohesion: 0.11
Nodes (20): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupFacet, BackupSettings, BackupWidgetPlacement, T (+12 more)

### Community 39 - "homeAppLabelShadow"
Cohesion: 0.20
Nodes (14): dashedBorder(), Color, Dp, Modifier, HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, HubEmptyState() (+6 more)

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.17
Nodes (15): androidx, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection, AppInfo, LauncherViewModel (+7 more)

### Community 44 - "FacetDao"
Cohesion: 0.15
Nodes (3): FacetDao, Flow, FacetDaoTest

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.15
Nodes (10): BackupRestoreEvent, BackupRestoreUiState, LaunchBindPermission, LaunchConfigure, PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow (+2 more)

### Community 46 - "HubGrid"
Cohesion: 0.18
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

### Community 47 - "HomeScreen.kt"
Cohesion: 0.28
Nodes (14): HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview() (+6 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 52 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 53 - "CardDivider"
Cohesion: 0.27
Nodes (16): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+8 more)

### Community 54 - ".refresh"
Cohesion: 0.15
Nodes (8): Callback, Callback, Flow, FacetNotificationListenerService, Callback, NotificationListenerService, StatusBarNotification, UserHandle

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.20
Nodes (23): NotificationBadgeStyle, COUNT, DOT, GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip() (+15 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 62 - "HomeWallpaper"
Cohesion: 0.24
Nodes (9): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground(), WallpaperRepository (+1 more)

### Community 63 - "WallpaperRepository"
Cohesion: 0.15
Nodes (8): DockSettingsScreenTest, Bitmap, WallpaperRepository, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, WallpaperManager

### Community 64 - "SettingsViewModelTest"
Cohesion: 0.19
Nodes (4): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, SettingsViewModelTest

### Community 65 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 70 - "FacetNavHost"
Cohesion: 0.26
Nodes (7): ClockStyleGalleryRoute(), FacetClockStyleGalleryScreen(), FacetDestinations, FacetNavHost(), Modifier, popBackStackSafely(), NavHostController

### Community 72 - "ManageFacetsViewModel"
Cohesion: 0.12
Nodes (7): StateFlow, ViewModel, ManageFacetsUiState, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 73 - "NextAlarmRepositoryTest"
Cohesion: 0.19
Nodes (7): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver, NextAlarmRepositoryTest

### Community 74 - "ClockFontOption"
Cohesion: 0.14
Nodes (7): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM

### Community 75 - "SettingsRepositoryTest.kt"
Cohesion: 0.09
Nodes (16): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+8 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "DockAppPickerScreen.kt"
Cohesion: 0.22
Nodes (13): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+5 more)

### Community 81 - "OnboardingViewModel"
Cohesion: 0.22
Nodes (5): AppInfo, ListContentMode, StateFlow, ViewModel, OnboardingViewModel

### Community 82 - ".setContent"
Cohesion: 0.19
Nodes (10): KeyboardDismissalTest, LauncherViewModel, SystemSettingsRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, Flow, ObserveQuickAddStateUseCase, QuickAddState (+2 more)

### Community 83 - "DockAppRepository"
Cohesion: 0.11
Nodes (7): DockAppRepository, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest, CleanUpUninstalledAppsUseCaseTest

### Community 84 - "PlaceWidgetUseCase"
Cohesion: 0.23
Nodes (5): HubWidgetPickerScreenTest, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "combine"
Cohesion: 0.13
Nodes (12): Flow, Flow, Flow, combine(), Flow, T1, T2, T3 (+4 more)

### Community 89 - "NotificationBadgeRepository"
Cohesion: 0.32
Nodes (4): NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "NotificationAccessExplanationScreen.kt"
Cohesion: 0.33
Nodes (7): Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 95 - "HomeViewModel"
Cohesion: 0.13
Nodes (9): HomeViewModel, StateFlow, ViewModel, HomeViewModelTest, FacetEntity, LauncherSettings, DefaultLauncherRepository, FacetRepository (+1 more)

### Community 96 - "ClockColorOption"
Cohesion: 0.06
Nodes (15): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockAlignment, CENTER, LEFT (+7 more)

### Community 97 - "AppContextMenuTest"
Cohesion: 0.10
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 98 - "OnboardingFacetsPage.kt"
Cohesion: 0.35
Nodes (9): Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview() (+1 more)

### Community 99 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.12
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 105 - "ComponentName"
Cohesion: 0.27
Nodes (5): Intent, LauncherActivity, Bundle, ComponentActivity, ComponentName

### Community 106 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (9): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+1 more)

### Community 109 - "BackButton"
Cohesion: 0.26
Nodes (9): BackButton(), Modifier, Modifier, UsageAccessExplanationContent(), UsageAccessExplanationScreen(), UsageAccessExplanationScreenPreview(), StateFlow, ViewModel (+1 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.10
Nodes (17): LauncherSettings, HomeUiState, FacetEntity, ListContentMode, ExportBackupUseCaseTest, FacetCarouselUiStateTest, HomeUiStateTest, AppListVerticalAlignment (+9 more)

### Community 111 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.42
Nodes (4): FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.14
Nodes (6): WidgetProviderOption, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 117 - "CalendarEventsBlock.kt"
Cohesion: 0.29
Nodes (11): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+3 more)

### Community 120 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 122 - "SetDefaultLauncherSheetTest"
Cohesion: 0.20
Nodes (7): SetDefaultLauncherSheetTest, AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview(), Intent

### Community 123 - "DefaultAppRepository"
Cohesion: 0.24
Nodes (3): DefaultAppRepository, Intent, SeedDefaultDockUseCase

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "ContactRepository"
Cohesion: 0.11
Nodes (22): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+14 more)

### Community 132 - "AppInfo"
Cohesion: 0.13
Nodes (10): AppInfo, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel (+2 more)

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.15
Nodes (9): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+1 more)

### Community 134 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "ObserveClockAccessoriesUseCase.kt"
Cohesion: 0.60
Nodes (3): ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase

### Community 138 - "HubViewModel"
Cohesion: 0.16
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 139 - ".setContent"
Cohesion: 0.09
Nodes (8): SettingsScreenTest, DefaultLauncherRepository, Intent, Intent, StateFlow, ViewModel, SettingsViewModel, DefaultLauncherRepositoryTest

### Community 140 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 141 - "DefaultFavoriteAppRepository"
Cohesion: 0.10
Nodes (7): DefaultFavoriteAppRepository, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - ".setContent"
Cohesion: 0.24
Nodes (3): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarPermissionRepository

### Community 147 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 148 - "DrawerViewModel"
Cohesion: 0.18
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.24
Nodes (4): Fixture, AppInfo, LauncherSettings, OnboardingViewModelTest

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "FontWeightOption"
Cohesion: 0.10
Nodes (13): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FacetType (+5 more)

### Community 156 - ".setContent"
Cohesion: 0.19
Nodes (4): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 162 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.36
Nodes (9): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview() (+1 more)

### Community 163 - "FacetDatabase"
Cohesion: 0.11
Nodes (8): DatabaseModule, Context, FacetDatabase, FacetDockAppDao, Flow, Migrations, Migration, RoomDatabase

### Community 164 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 165 - "DockDisplayMode"
Cohesion: 0.11
Nodes (20): DockDisplayMode, ICONS, TEXT, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable (+12 more)

### Community 166 - "OnboardingIntroPage.kt"
Cohesion: 0.53
Nodes (8): AppRowBar(), ConceptLine(), HomeDiagram(), Dp, Modifier, LeaderRow(), OnboardingIntroPage(), OnboardingIntroPagePreview()

### Community 167 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 168 - "Color.kt"
Cohesion: 0.39
Nodes (7): Color, resolve(), accentTonalExtremes(), Color, toneOf(), wallpaperPrimaryAndSecondary(), ColorScheme

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 172 - "FacetCarouselViewModel.kt"
Cohesion: 0.38
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 173 - "NotificationSettingsScreen.kt"
Cohesion: 0.57
Nodes (6): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), NotificationSettingsUiState

### Community 174 - "BackupRestoreMessage"
Cohesion: 0.33
Nodes (6): BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded

### Community 175 - "AppIcon"
Cohesion: 0.53
Nodes (8): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 176 - "AppContextMenu"
Cohesion: 0.80
Nodes (4): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier

### Community 177 - "InheritOverrideCard"
Cohesion: 0.90
Nodes (4): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow()

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 206 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.17
Nodes (16): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), detectHomeSwipeGestures(), DefaultFavoritesReorderList(), HomeAppsListClickableRow() (+8 more)

### Community 211 - "StickyHeaderLayout"
Cohesion: 0.36
Nodes (10): Modifier, StickyHeaderLayout(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow() (+2 more)

### Community 222 - "PermissionsScreen.kt"
Cohesion: 0.23
Nodes (13): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionKind, CALENDAR (+5 more)

## Knowledge Gaps
- **365 isolated node(s):** `Category`, `Short description (max 80 characters)`, `Full description (max 4000 characters — this draft is ~1,750)`, `Multiple`, `Single` (+360 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 674 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **59 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ContactRepository`, `.setContent`, `WidgetPlacementEntity`, `HomeDrawerRouteTest.kt`, `.setContent`, `.setContent`, `.setContent`, `.setContent`, `NotificationAccessRepository`, `DrawerViewModel`, `LauncherFontOption`, `.setContent`, `FontWeightOption`, `.setContent`, `FacetCarouselScreen.kt`, `.setContent`, `ClockStyleGalleryScreen.kt`, `HubWidgetPickerScreen.kt`, `AppRepository`, `.setContent`, `DockDisplayMode`, `homeAppLabelShadow`, `OnboardingIntroPage.kt`, `.setContent`, `BackupRestoreContent`, `CalendarRepository`, `CalendarSettingsScreen.kt`, `NotificationSettingsScreen.kt`, `HomeScreen.kt`, `.rendersOneEntryPerLetterProvided`, `AppearanceSettingsScreen.kt`, `CardDivider`, `AppDrawerScreen.kt`, `.setContent`, `LabeledDropdownRow`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `WallpaperRepository`, `ColorTest`, `WidgetResizeHandle.kt`, `.setContent`, `SettingsRepositoryTest.kt`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `DockAppPickerScreen.kt`, `.setContent`, `StickyHeaderLayout`, `PlaceWidgetUseCase`, `.setContent`, `NotificationAccessExplanationScreen.kt`, `GestureHintOverlay`, `PermissionsScreen.kt`, `.setContent`, `AppContextMenuTest`, `OnboardingFacetsPage.kt`, `AppDrawerScreen`, `TonalButton`, `.setContent`, `ComponentName`, `DockSettingsScreen.kt`, `BackButton`, `ObserveFacetPreviewsUseCase.kt`, `AccentSwatch`, `.setContent`, `SetDefaultLauncherSheetTest`, `DefaultAppRepository`?**
  _High betweenness centrality (0.161) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `HomeDrawerRouteTest.kt`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `DefaultFavoriteAppRepository`, `FacetDockAppRepository`, `FavoriteAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `FacetEntity`, `AddAppToFavoritesUseCaseTest`, `DrawerViewModel`, `GroupAppsByLetterUseCaseTest`, `FacetCarouselScreen.kt`, `ListContentMode`, `.setContent`, `.createViewModel`, `AppRepository`, `DockDisplayMode`, `SelectPreviewAppsUseCaseTest`, `.setContent`, `FacetCarouselViewModel.kt`, `HomeScreen.kt`, `AppContextMenu`, `AppearanceSettingsScreen.kt`, `CardDivider`, `.refresh`, `AppDrawerScreen.kt`, `SeedDefaultDockUseCaseTest`, `HomeScreen`, `SettingsScreen.kt`, `WallpaperRepository`, `SettingsViewModelTest`, `FacetNavHost`, `SettingsRepositoryTest.kt`, `HomeAppsListSettingsScreen.kt`, `DockAppPickerScreen.kt`, `.setContent`, `DockAppRepository`, `.setContent`, `combine`, `Fixture`, `Fixture`, `AppContextMenuTest`, `AppDrawerScreen`, `FacetLauncherTheme`, `ComponentName`, `DockSettingsScreen.kt`, `Fixture`, `ObserveFacetPreviewsUseCase.kt`, `.useCase`, `DefaultAppRepository`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModelTest`, `BackupRestoreViewModelTest.kt`, `HomeDrawerRouteTest.kt`, `AppInfo`, `HomeAppsListSettingsViewModelTest.kt`, `.setContent`, `FakeFacetDao`, `DefaultFavoriteAppRepository`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `SettingsRepositoryTest`, `NotificationAccessRepository`, `CalendarSettingsViewModel`, `FacetEntity`, `LauncherFontOption`, `FontWeightOption`, `ImportBackupUseCase.kt`, `.setContent`, `ListContentMode`, `.setContent`, `.createViewModel`, `AppRepository`, `.setContent`, `DockDisplayMode`, `BackupMapping.kt`, `.setContent`, `.createViewModel`, `CalendarRepository`, `FacetCarouselViewModel.kt`, `CalendarInfo`, `SeedDefaultDockUseCaseTest`, `.setContent`, `WallpaperRepository`, `SettingsViewModelTest`, `.setContent`, `.drawerViewModel`, `ManageFacetsViewModel`, `ClockFontOption`, `SettingsRepositoryTest.kt`, `.setContent`, `DockAppRepository`, `NotificationAccessExplanationScreen.kt`, `ClockColorOption`, `.setContent`, `LauncherSettings`, `ObserveFacetPreviewsUseCase.kt`, `.useCase`, `AppDrawerSettingsViewModelTest`, `.setContent`, `DefaultAppRepository`?**
  _High betweenness centrality (0.085) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Category`, `Short description (max 80 characters)`, `Full description (max 4000 characters — this draft is ~1,750)` to the rest of the system?**
  _365 weakly-connected nodes found - possible documentation gaps or missing edges._