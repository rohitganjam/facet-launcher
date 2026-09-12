# Graph Report - lumen-launcher  (2026-09-12)

## Corpus Check
- 352 files · ~782,718 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3382 nodes · 9009 edges · 198 communities (129 shown, 63 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 897 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `70f807e5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockColorOption
- design_handoff_minimal_launcher/support.js
- FakeProfileDao
- GetInstalledAppsUseCase
- ImportBackupResult
- ListContentMode
- combine
- Android launcher design planning/support.js
- .setContent
- Screens
- HomeDrawerRoute
- FakeProfileDao
- ProfileRepository
- FavoriteAppRepository
- DrawerViewModel
- AppWidgetRepository
- Screens
- ClockAlignment
- ClockStyleGalleryViewModelTest
- calculateHubCellWidth
- FavoritesPickerScreen.kt
- AppWidgetRepository.kt
- SettingsSearchEntry
- 4. Feature Requirements
- SettingsRepository
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerPresentation
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- AppInfo
- .setContent
- DefaultAppRepository
- .createViewModel
- WidgetPlacementEntity
- SettingsRepository.kt
- ImportBackupUseCase.kt
- ClockAccessoryIcons.kt
- CalendarRepository
- AppearanceSettingsScreen.kt
- Manrope Font License (SIL OFL 1.1)
- LauncherFontOption
- DefaultFavoriteAppDao
- BackupRestoreViewModel
- .setContent
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- NextAlarmRepositoryTest
- HomeAppsListSettingsViewModel.kt
- WidgetProviderOption
- OnboardingHomeSetupPage.kt
- NotificationBadgeRepository
- 4. Feature Requirements
- AppDrawerScreen.kt
- CardDivider
- FavoriteAppDao
- HomeScreen
- ClockAdjustSheet.kt
- FakeWidgetPlacementDao
- .setContent
- .drawerViewModel
- ManageProfilesScreen.kt
- ResolveWidgetDropUseCaseTest
- ClockTemplateId
- github.md
- Color.kt
- LauncherApps
- FacetNavHost
- DefaultLauncherRepositoryTest
- NotificationSettingsViewModel
- .setContent
- .setContent
- Dp
- .setContent
- SelectPreviewAppsUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- HomeWallpaper
- LabeledDropdownRow
- DockAppDao
- CalendarPermissionRepository
- DockAppRepository
- GestureHintOverlay
- Lumen Launcher Implementation Plan
- ObserveHomeScreenStateUseCase.kt
- Play Console — sensitive permission disclosures
- .setContent
- .createViewModel
- PermissionKind
- Facet Launcher — Built Capabilities
- ObserveHubStateUseCase
- Play Console — store listing text
- ProfileCarouselViewModel.kt
- ProfileDaoTest
- .setContent
- AppContextMenuTest
- ProfileDockAppDao
- AppDrawerScreen
- FacetLauncherTheme
- PlaceWidgetUseCase
- AppRepository
- HubContent
- WallpaperRepository
- OnboardingViewModel
- ProfileCarouselUiState
- CompactWidgetsUseCaseTest
- Offset
- NotificationAccessExplanationViewModel.kt
- LauncherSettings
- CalendarSettingsViewModel
- HubWidgetPickerViewModel
- ManageProfilesViewModel
- homeAppLabelShadow
- FacetDatabaseMigrationTest
- ResolveWidgetResizeUseCaseTest
- AccentSwatch
- .useCase
- AppDrawerSettingsViewModelTest
- Intent
- AppModule
- Flow
- Color
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- FontWeight
- NestedScrollConnection
- ResolveWidgetDropUseCase
- NestedScrollSource
- Facet Launcher — Onboarding Flow
- Offset
- .createViewModel
- HubViewModel
- SettingsScreenTest.kt
- rememberTickingNow
- DefaultFavoriteAppRepository
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- AppIcon
- SettingsRepositoryTest
- DockAppDaoTest
- SetDefaultLauncherSheet
- BackButton
- SettingsViewModelTest
- OnboardingViewModelTest
- .setContent
- ObserveProfilePreviewsUseCase.kt
- 2. Design tokens
- GroupAppsByLetterUseCaseTest
- HubWidgetTile
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- .setContent
- SeedDefaultDockUseCaseTest
- ProfileDockAppEntity
- AppContextMenu
- FacetDatabase
- HomeDrawerRouteTest.kt
- .setContent
- DeleteWidgetUseCaseTest.kt
- SettingsViewModel
- ProfileDockAppRepository
- T
- .createViewModel
- Modifier
- Dp
- AddAppToDockUseCaseTest.kt
- AddAppToFavoritesUseCaseTest
- ProfileDao
- BackupRestoreViewModelTest
- SettingsScreen.kt
- ProfileEntity
- HomeAppsListSettingsViewModelTest.kt
- ColorTest
- letterAt
- BatteryStatus
- .homeViewModel
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- HomeAppsListSettingsScreen.kt
- Fixture
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- FontWeightOption
- HubGrid
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 213 edges
2. `AppInfo` - 154 edges
3. `SettingsRepository` - 142 edges
4. `Row` - 127 edges
5. `ProfileRepository` - 122 edges
6. `LauncherSettings` - 107 edges
7. `ProfileEntity` - 104 edges
8. `ClockTemplateId` - 66 edges
9. `WidgetPlacementEntity` - 57 edges
10. `HomeScreen()` - 56 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `HubGrid()` --calls--> `WidgetResizeHandle()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/HubGrid.kt → app/src/main/kotlin/com/facetlauncher/app/ui/hub/WidgetResizeHandle.kt
- `WidgetResizeHandlePreview()` --calls--> `FacetLauncherTheme()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/WidgetResizeHandle.kt → app/src/main/kotlin/com/facetlauncher/app/ui/theme/Theme.kt
- `ClockStyleGalleryViewModel` --calls--> `combine()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/home/clock/ClockStyleGalleryViewModel.kt → app/src/main/kotlin/com/facetlauncher/app/domain/FlowCombine.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (198 total, 63 thin omitted)

### Community 0 - "ClockColorOption"
Cohesion: 0.06
Nodes (17): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockFontOption, LAUNCHER_DEFAULT, MANROPE (+9 more)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "FakeProfileDao"
Cohesion: 0.12
Nodes (4): FakeProfileDao, Flow, ProfileEntity, ProfileRepositoryTest

### Community 3 - "GetInstalledAppsUseCase"
Cohesion: 0.12
Nodes (10): CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, SeedDefaultDockUseCase, SharedFlow, StateFlow, ViewModel, LauncherUiState (+2 more)

### Community 4 - "ImportBackupResult"
Cohesion: 0.28
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 5 - "ListContentMode"
Cohesion: 0.07
Nodes (13): Converters, AppRowPosition, LEFT, RIGHT, AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY (+5 more)

### Community 6 - "combine"
Cohesion: 0.15
Nodes (11): Flow, Flow, combine(), Flow, T1, T2, T3, T4 (+3 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.13
Nodes (6): HubGestureTest, HomeDrawerRouteTest, Offset, T, WidgetPlacementEntity, WidgetPlacementRepository

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "HomeDrawerRoute"
Cohesion: 0.16
Nodes (16): Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, AppInfo (+8 more)

### Community 11 - "FakeProfileDao"
Cohesion: 0.15
Nodes (10): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, ProfileEntity, SettingsRepository (+2 more)

### Community 12 - "ProfileRepository"
Cohesion: 0.09
Nodes (4): ProfileEntity, ProfileRepository, toCsv(), DockDisplayMode

### Community 13 - "FavoriteAppRepository"
Cohesion: 0.10
Nodes (8): FavoriteAppRepository, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest, FacetDatabase

### Community 14 - "DrawerViewModel"
Cohesion: 0.21
Nodes (6): DrawerViewModel, AppInfo, ContactInfo, StateFlow, ViewModel, AppShortcut

### Community 15 - "AppWidgetRepository"
Cohesion: 0.10
Nodes (7): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Context, Flow, IntentSender, AppWidgetRepositoryTest

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "ClockAlignment"
Cohesion: 0.16
Nodes (16): CalendarEvent, ClockAlignment, CENTER, LEFT, RIGHT, CalendarEventsBlock(), EventRow(), Color (+8 more)

### Community 20 - "FavoritesPickerScreen.kt"
Cohesion: 0.16
Nodes (14): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+6 more)

### Community 21 - "AppWidgetRepository.kt"
Cohesion: 0.43
Nodes (5): flattenIcon(), Bitmap, Bitmap, toBitmap(), Drawable

### Community 22 - "SettingsSearchEntry"
Cohesion: 0.18
Nodes (5): DefaultLauncherRepository, SettingsSearchEntry, CatalogEntry, SystemSettingsRepositoryTest, Intent

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.06
Nodes (3): ListContentMode, SettingsRepository, ClockColorOption

### Community 25 - ".setContent"
Cohesion: 0.05
Nodes (40): OnboardingScreenTest, Intent, LauncherActivity, WidgetResizeHandle(), WidgetResizeHandlePreview(), Modifier, OnboardingDots(), AppRowBar() (+32 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerPresentation"
Cohesion: 0.08
Nodes (19): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+11 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+6 more)

### Community 30 - "Row"
Cohesion: 0.22
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (30): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+22 more)

### Community 32 - "AppInfo"
Cohesion: 0.15
Nodes (7): AppInfo, UsageAccessRepository, UsageStatsRepository, GroupAppsByLetterUseCase, GroupedApps, UsageStatsRepositoryTest, GetInstalledAppsUseCaseTest

### Community 34 - "DefaultAppRepository"
Cohesion: 0.22
Nodes (3): DefaultAppRepository, Intent, DefaultAppRepositoryTest

### Community 35 - ".createViewModel"
Cohesion: 0.23
Nodes (3): DockSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 36 - "WidgetPlacementEntity"
Cohesion: 0.14
Nodes (6): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, ResolveWidgetResizeUseCase

### Community 37 - "SettingsRepository.kt"
Cohesion: 0.14
Nodes (17): AppListVerticalAlignment, BOTTOM, TOP, Flow, Keys, AppListVerticalAlignment, AppRowPosition, AppRowPresentation (+9 more)

### Community 38 - "ImportBackupUseCase.kt"
Cohesion: 0.11
Nodes (21): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, T (+13 more)

### Community 39 - "ClockAccessoryIcons.kt"
Cohesion: 0.31
Nodes (10): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+2 more)

### Community 40 - "CalendarRepository"
Cohesion: 0.20
Nodes (5): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo

### Community 41 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 43 - "LauncherFontOption"
Cohesion: 0.16
Nodes (19): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockPositionResetRow(), clockStyleGalleryDisplayLabel() (+11 more)

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.11
Nodes (16): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+8 more)

### Community 46 - ".setContent"
Cohesion: 0.15
Nodes (9): BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 47 - "HomeScreen.kt"
Cohesion: 0.16
Nodes (20): DockDisplayMode, ICONS, TEXT, HomeSurfacePreview(), Color, FontWeight, Modifier, AppRow() (+12 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "NextAlarmRepositoryTest"
Cohesion: 0.19
Nodes (7): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver, NextAlarmRepositoryTest

### Community 51 - "HomeAppsListSettingsViewModel.kt"
Cohesion: 0.23
Nodes (5): HomeAppsListSettingsScreenTest, HomeAppsListSettingsViewModel, HomeAppsListUiState, StateFlow, ViewModel

### Community 52 - "WidgetProviderOption"
Cohesion: 0.14
Nodes (19): HubWidgetPickerScreenTest, WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow() (+11 more)

### Community 53 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.29
Nodes (16): ConfirmDialog(), Modifier, AppDrawerSection(), DockAppsReorderRow(), DockClickableRow(), DockSection(), FavoritesClickableRow(), FavoritesReorderList() (+8 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.22
Nodes (24): NotificationBadgeStyle, COUNT, DOT, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder() (+16 more)

### Community 57 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier (+8 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "FakeWidgetPlacementDao"
Cohesion: 0.36
Nodes (3): FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 62 - ".setContent"
Cohesion: 0.20
Nodes (7): KeyboardDismissalTest, LauncherViewModel, AddAppToDockUseCase, AddAppToFavoritesUseCase, Flow, ObserveQuickAddStateUseCase, QuickAddState

### Community 63 - ".drawerViewModel"
Cohesion: 0.21
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 64 - "ManageProfilesScreen.kt"
Cohesion: 0.35
Nodes (11): ReorderRowDefaults, AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview() (+3 more)

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 68 - "Color.kt"
Cohesion: 0.17
Nodes (8): Color, resolve(), accentTonalExtremes(), Color, toneOf(), wallpaperPrimaryAndSecondary(), WallpaperRepositoryTest, ColorScheme

### Community 69 - "LauncherApps"
Cohesion: 0.24
Nodes (4): AppListLimits, AppRepository, LauncherApps, WallpaperManager

### Community 70 - "FacetNavHost"
Cohesion: 0.28
Nodes (5): FacetDestinations, FacetNavHost(), Modifier, popBackStackSafely(), NavHostController

### Community 72 - "NotificationSettingsViewModel"
Cohesion: 0.36
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 73 - ".setContent"
Cohesion: 0.15
Nodes (14): DockAppPickerScreenTest, AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier (+6 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "HomeWallpaper"
Cohesion: 0.08
Nodes (23): HomeWallpaper, Image, Tones, Unavailable, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT (+15 more)

### Community 80 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 82 - "CalendarPermissionRepository"
Cohesion: 0.12
Nodes (10): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, StateFlow (+2 more)

### Community 83 - "DockAppRepository"
Cohesion: 0.15
Nodes (5): DockAppRepository, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 84 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.22
Nodes (6): ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase, HomeScreenState, Flow, ObserveHomeScreenStateUseCase

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 90 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "ProfileCarouselViewModel.kt"
Cohesion: 0.28
Nodes (5): T, resolveOverride(), StateFlow, ViewModel, ProfileCarouselViewModel

### Community 97 - "AppContextMenuTest"
Cohesion: 0.11
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (4): AlphabetRailTest, ClockBlockTest, ClockBlock(), FacetLauncherTheme()

### Community 101 - "PlaceWidgetUseCase"
Cohesion: 0.39
Nodes (4): HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 102 - "AppRepository"
Cohesion: 0.29
Nodes (4): AppRepository, AppRepositoryTest, CleanUpUninstalledAppsUseCaseTest, LauncherActivityInfo

### Community 103 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 104 - "WallpaperRepository"
Cohesion: 0.30
Nodes (3): DockSettingsScreenTest, Bitmap, WallpaperRepository

### Community 105 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 109 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 110 - "LauncherSettings"
Cohesion: 0.19
Nodes (4): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest

### Community 111 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.12
Nodes (7): Intent, HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest, ComponentName

### Community 113 - "ManageProfilesViewModel"
Cohesion: 0.14
Nodes (8): StateFlow, ViewModel, ManageProfilesViewModel, FakeProfileDao, Flow, ProfileEntity, SettingsRepository, ManageProfilesViewModelTest

### Community 114 - "homeAppLabelShadow"
Cohesion: 0.12
Nodes (24): dashedBorder(), Color, Dp, Modifier, Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector (+16 more)

### Community 117 - "AccentSwatch"
Cohesion: 0.13
Nodes (21): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState (+13 more)

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "ResolveWidgetDropUseCase"
Cohesion: 0.18
Nodes (7): CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, HubViewModelTest

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 139 - "SettingsScreenTest.kt"
Cohesion: 0.38
Nodes (3): Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState

### Community 140 - "rememberTickingNow"
Cohesion: 0.39
Nodes (6): BroadcastReceiver, Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 141 - "DefaultFavoriteAppRepository"
Cohesion: 0.14
Nodes (6): DefaultFavoriteAppRepository, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "AppIcon"
Cohesion: 0.53
Nodes (8): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 147 - "SetDefaultLauncherSheet"
Cohesion: 0.62
Nodes (6): AlreadyDefaultContent(), Modifier, OnboardingUiState, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 148 - "BackButton"
Cohesion: 0.20
Nodes (13): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+5 more)

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 152 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.42
Nodes (4): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 161 - "ProfileDockAppEntity"
Cohesion: 0.13
Nodes (5): ProfileDockAppEntity, toProfileDockAppEntity(), FacetDatabase, ProfileDockAppDaoTest, Flow

### Community 162 - "AppContextMenu"
Cohesion: 0.80
Nodes (4): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier

### Community 163 - "FacetDatabase"
Cohesion: 0.22
Nodes (6): DatabaseModule, Context, FacetDatabase, Migrations, Migration, RoomDatabase

### Community 164 - "HomeDrawerRouteTest.kt"
Cohesion: 0.16
Nodes (4): SystemSettingsRepository, AppOpsManager, QuickAddState, UsageStatsManager

### Community 165 - ".setContent"
Cohesion: 0.09
Nodes (22): ProfileSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable (+14 more)

### Community 167 - "SettingsViewModel"
Cohesion: 0.53
Nodes (4): Intent, StateFlow, ViewModel, SettingsViewModel

### Community 168 - "ProfileDockAppRepository"
Cohesion: 0.21
Nodes (4): Flow, ProfileDockAppRepository, FakeProfileDockAppDao, ProfileDockAppRepositoryTest

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 184 - "ProfileDao"
Cohesion: 0.27
Nodes (3): Flow, ProfileEntity, ProfileDao

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 191 - "ProfileEntity"
Cohesion: 0.18
Nodes (5): ProfileEntity, ProfileEntityTest, Fixture, ObserveHomeScreenStateUseCaseTest, ProfileCarouselUiStateTest

### Community 192 - "HomeAppsListSettingsViewModelTest.kt"
Cohesion: 0.21
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 196 - "BatteryStatus"
Cohesion: 0.17
Nodes (10): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+2 more)

### Community 203 - ".homeViewModel"
Cohesion: 0.11
Nodes (6): NotificationShadeRepository, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest, HomeViewModelTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.29
Nodes (12): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier (+4 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 213 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.19
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel() (+6 more)

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 233 - "FontWeightOption"
Cohesion: 0.10
Nodes (17): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FontWeightSlider() (+9 more)

### Community 237 - "HubGrid"
Cohesion: 0.29
Nodes (10): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+2 more)

## Knowledge Gaps
- **365 isolated node(s):** `HubFull`, `AddFailed`, `LaunchBindPermission`, `LaunchConfigure`, `WidgetAdded` (+360 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 671 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **63 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `.setContent`, `SettingsScreenTest.kt`, `ClockAlignment`, `SetDefaultLauncherSheet`, `FavoritesPickerScreen.kt`, `BackButton`, `.setContent`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `.setContent`, `ProfileCarouselScreen.kt`, `ContactRepositoryTest`, `.setContent`, `.setContent`, `WidgetPlacementEntity`, `.setContent`, `CalendarRepository`, `AppearanceSettingsScreen.kt`, `LauncherFontOption`, `.setContent`, `HomeScreen.kt`, `HomeAppsListSettingsViewModel.kt`, `WidgetProviderOption`, `OnboardingHomeSetupPage.kt`, `AppDrawerScreen.kt`, `CardDivider`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `.setContent`, `ManageProfilesScreen.kt`, `ColorTest`, `LauncherApps`, `.setContent`, `.setContent`, `.setContent`, `DockSettingsScreen.kt`, `HomeWallpaper`, `CalendarPermissionRepository`, `AppDrawerSettingsScreen.kt`, `GestureHintOverlay`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `AppContextMenuTest`, `.setContent`, `AppDrawerScreen`, `AppRepository`, `WallpaperRepository`, `FontWeightOption`, `homeAppLabelShadow`, `AccentSwatch`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockColorOption`, `GetInstalledAppsUseCase`, `ImportBackupResult`, `ListContentMode`, `.setContent`, `.createViewModel`, `SettingsScreenTest.kt`, `DefaultFavoriteAppRepository`, `ClockAlignment`, `SettingsRepositoryTest`, `ClockStyleGalleryViewModelTest`, `.setContent`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `.setContent`, `DrawerPresentation`, `.setContent`, `AppInfo`, `.setContent`, `DefaultAppRepository`, `.createViewModel`, `HomeDrawerRouteTest.kt`, `.setContent`, `SettingsRepository.kt`, `ImportBackupUseCase.kt`, `CalendarRepository`, `.createViewModel`, `.setContent`, `HomeScreen.kt`, `AddAppToDockUseCaseTest.kt`, `HomeAppsListSettingsViewModel.kt`, `.setContent`, `ProfileEntity`, `HomeAppsListSettingsViewModelTest.kt`, `LauncherApps`, `NotificationSettingsViewModel`, `.setContent`, `.homeViewModel`, `DockSettingsScreen.kt`, `HomeWallpaper`, `CalendarPermissionRepository`, `DockAppRepository`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `.createViewModel`, `ProfileCarouselViewModel.kt`, `.setContent`, `AppRepository`, `FontWeightOption`, `OnboardingViewModel`, `NotificationAccessExplanationViewModel.kt`, `LauncherSettings`, `CalendarSettingsViewModel`, `ManageProfilesViewModel`, `.useCase`, `AppDrawerSettingsViewModelTest`?**
  _High betweenness centrality (0.093) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `GetInstalledAppsUseCase`, `combine`, `SettingsScreenTest.kt`, `DefaultFavoriteAppRepository`, `FavoriteAppRepository`, `ClockAlignment`, `FavoritesPickerScreen.kt`, `AppWidgetRepository.kt`, `OnboardingViewModelTest`, `SettingsViewModelTest`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `GroupAppsByLetterUseCaseTest`, `DrawerPresentation`, `ProfileCarouselScreen.kt`, `SeedDefaultDockUseCaseTest`, `ProfileDockAppEntity`, `AppContextMenu`, `DefaultAppRepository`, `HomeDrawerRouteTest.kt`, `.setContent`, `.createViewModel`, `ProfileDockAppRepository`, `AppearanceSettingsScreen.kt`, `HomeScreen.kt`, `AddAppToDockUseCaseTest.kt`, `AddAppToFavoritesUseCaseTest`, `HomeAppsListSettingsViewModel.kt`, `NotificationBadgeRepository`, `HomeScreen`, `SettingsScreen.kt`, `.setContent`, `ProfileEntity`, `HomeAppsListSettingsViewModelTest.kt`, `LauncherApps`, `FacetNavHost`, `.setContent`, `.setContent`, `SelectPreviewAppsUseCaseTest`, `DockSettingsScreen.kt`, `HomeWallpaper`, `DockAppRepository`, `HomeAppsListSettingsScreen.kt`, `ObserveHomeScreenStateUseCase.kt`, `Fixture`, `Fixture`, `ProfileCarouselViewModel.kt`, `AppContextMenuTest`, `AppRepository`, `FontWeightOption`, `OnboardingViewModel`, `LauncherSettings`, `.useCase`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Are the 45 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.bottomPositionedSearchBarSitsInTheLowerHalfOfTheScreen()`) actually correct?**
  _`FacetLauncherTheme()` has 45 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **Are the 21 inferred relationships involving `ProfileRepository` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`ProfileRepository` has 21 INFERRED edges - model-reasoned connections that need verification._
- **What connects `HubFull`, `AddFailed`, `LaunchBindPermission` to the rest of the system?**
  _365 weakly-connected nodes found - possible documentation gaps or missing edges._