# Graph Report - lumen-launcher  (2026-09-12)

## Corpus Check
- 352 files · ~782,200 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3353 nodes · 9006 edges · 176 communities (118 shown, 52 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 881 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `28e28d60`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- HubWidgetTile
- AppInfo
- PlaceWidgetUseCase
- ClockColorOption
- combine
- Android launcher design planning/support.js
- .setContent
- Screens
- .setContent
- FakeProfileDao
- ProfileEntity
- FakeFavoriteAppDao
- DrawerViewModel.kt
- AppWidgetRepository
- Screens
- CalendarEventsBlock.kt
- ClockStyleGalleryViewModelTest
- AppWidgetProviderInfo
- DockAppPickerScreen.kt
- AppWidgetRepository.kt
- SettingsSearchEntry
- 4. Feature Requirements
- SettingsRepository
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerGridSize
- ProfileCarouselScreen.kt
- Row
- ContactRepositoryTest
- FavoritesPickerViewModel
- .setContent
- DefaultAppRepositoryTest
- HomeWallpaper
- AppModule
- dashedBorder
- DockAppRepository
- ClockAccessoryIcons.kt
- AccentSwatch
- AppearanceSettingsScreen.kt
- Manrope Font License (SIL OFL 1.1)
- LauncherFontOption
- DefaultFavoriteAppDao
- BackupRestoreViewModel
- LauncherAppWidgetHost
- TonalButton
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- FavoriteAppEntity
- OnboardingScreen
- WidgetProviderOption
- OnboardingHomeSetupPage.kt
- NotificationBadgeRepository
- 4. Feature Requirements
- AppDrawerScreen.kt
- CardDivider
- FavoriteAppDao
- HomeScreen
- ClockAdjustSheet.kt
- WidgetPlacementRepository
- BackupRestoreContent
- .drawerViewModel
- ManageProfilesScreen.kt
- FavoritesPickerScreen.kt
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- HomeDrawerRouteTest.kt
- FacetNavHost
- DefaultLauncherRepositoryTest
- NotificationSettingsViewModel
- DockAppPickerViewModel
- EnsureActiveProfileUseCaseTest.kt
- Dp
- .setContent
- LauncherViewModel.kt
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- .setContent
- LabeledDropdownRow
- .setContent
- .setContent
- DockAppEntity
- GestureHintOverlay
- Lumen Launcher Implementation Plan
- PermissionsViewModel
- Play Console — sensitive permission disclosures
- .setContent
- .createViewModel
- PermissionKind
- Facet Launcher — Built Capabilities
- ObserveHubStateUseCase
- Play Console — store listing text
- ProfileCarouselViewModel
- ProfileDaoTest
- .setContent
- AppContextMenuTest
- NotificationAccessRepository
- AppDrawerScreen
- FacetLauncherTheme
- .setContent
- ConvertersTest
- HubContent
- CleanUpUninstalledAppsUseCase
- DrawerPresentation
- ProfileCarouselUiState
- HubAddWidgetEvent
- .setContent
- NotificationAccessExplanationViewModel.kt
- LauncherSettings
- .rendersOneEntryPerLetterProvided
- HubWidgetPickerViewModel
- ManageProfilesViewModel
- homeAppLabelShadow
- FacetDatabaseMigrationTest
- ClockAlignment.kt
- ClockColors.kt
- .useCase
- TypeTest.kt
- Intent
- BackupRestoreMessage
- Flow
- Color
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- FontWeight
- NestedScrollConnection
- WidgetPlacementEntity
- NestedScrollSource
- Facet Launcher — Onboarding Flow
- Offset
- HubViewModel
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- SettingsRepositoryTest
- BackButton
- OnboardingViewModelTest
- 2. Design tokens
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- FacetDatabase
- .setContent
- ProfileDockAppEntity
- .createViewModel
- ProfileDao
- BackupRestoreViewModelTest
- SettingsScreen.kt
- WidgetPlacementDao
- Fixture
- .createViewModel
- ColorTest
- letterAt
- NextAlarmRepositoryTest
- .homeViewModel
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- DockSettingsScreen.kt
- ObserveProfilePreviewsUseCase.kt
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- Type.kt
- HubGrid
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 218 edges
2. `ProfileEntity` - 178 edges
3. `AppInfo` - 156 edges
4. `SettingsRepository` - 149 edges
5. `Row` - 127 edges
6. `ProfileRepository` - 122 edges
7. `LauncherSettings` - 108 edges
8. `ClockTemplateId` - 70 edges
9. `WidgetPlacementEntity` - 60 edges
10. `ClockDateStyle` - 58 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `AppDrawerScreen()` --calls--> `GroupAppsByLetterUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/GroupAppsByLetterUseCase.kt
- `AppDrawerScreen()` --calls--> `RankBySearchRelevanceUseCase`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/domain/RankBySearchRelevanceUseCase.kt
- `AppDrawerScreen()` --calls--> `ContactConnectionsSheet()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/drawer/AppDrawerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/drawer/ContactConnectionsSheet.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (176 total, 52 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.13
Nodes (4): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 3 - "AppInfo"
Cohesion: 0.05
Nodes (15): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, AppInfo, ThemeMode, DARK, LIGHT, SYSTEM (+7 more)

### Community 4 - "PlaceWidgetUseCase"
Cohesion: 0.16
Nodes (10): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion, HubFull, Placed (+2 more)

### Community 5 - "ClockColorOption"
Cohesion: 0.05
Nodes (56): Converters, T, resolveOverride(), AppListLimits, ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME (+48 more)

### Community 6 - "combine"
Cohesion: 0.09
Nodes (17): Flow, Flow, combine(), Flow, Flow, ObserveQuickAddStateUseCase, QuickAddState, Flow (+9 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - ".setContent"
Cohesion: 0.14
Nodes (18): KeyboardDismissalTest, LauncherViewModel, Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection (+10 more)

### Community 11 - "FakeProfileDao"
Cohesion: 0.22
Nodes (4): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow

### Community 12 - "ProfileEntity"
Cohesion: 0.05
Nodes (12): ProfileEntity, Flow, ProfileRepository, toCsv(), ProfileEntityTest, FakeProfileDao, Flow, ProfileRepositoryTest (+4 more)

### Community 13 - "FakeFavoriteAppDao"
Cohesion: 0.24
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 14 - "DrawerViewModel.kt"
Cohesion: 0.12
Nodes (11): ContactInfo, AddAppToDockUseCase, AddAppToFavoritesUseCase, DrawerViewModel, AppInfo, ContactInfo, StateFlow, ViewModel (+3 more)

### Community 15 - "AppWidgetRepository"
Cohesion: 0.11
Nodes (5): AppWidgetRepository, Flow, Intent, AppWidgetRepositoryTest, ComponentName

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 19 - "AppWidgetProviderInfo"
Cohesion: 0.16
Nodes (6): AppWidgetHostView, AppWidgetProviderInfo, Context, IntentSender, calculateHubCellWidth(), Context

### Community 20 - "DockAppPickerScreen.kt"
Cohesion: 0.50
Nodes (8): AppIconSize, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader()

### Community 21 - "AppWidgetRepository.kt"
Cohesion: 0.24
Nodes (6): flattenIcon(), Bitmap, Bitmap, Bitmap, toBitmap(), Drawable

### Community 22 - "SettingsSearchEntry"
Cohesion: 0.15
Nodes (6): DefaultLauncherRepository, SettingsSearchEntry, CatalogEntry, SystemSettingsRepository, SystemSettingsRepositoryTest, Intent

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "SettingsRepository"
Cohesion: 0.05
Nodes (6): ListContentMode, SettingsRepository, ClockColorOption, ClockFontOption, Flow, FontWeightOption

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerGridSize"
Cohesion: 0.06
Nodes (13): AppDrawerSettingsScreenTest, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, SearchBarPosition, BOTTOM (+5 more)

### Community 29 - "ProfileCarouselScreen.kt"
Cohesion: 0.24
Nodes (13): AddProfilePage(), Modifier, NestedScrollConnection, NestedScrollSource, Offset, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent() (+5 more)

### Community 30 - "Row"
Cohesion: 0.22
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ContactRepositoryTest"
Cohesion: 0.06
Nodes (31): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+23 more)

### Community 32 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 35 - "HomeWallpaper"
Cohesion: 0.13
Nodes (9): HomeWallpaper, Tones, Unavailable, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest, WallpaperRepository (+1 more)

### Community 36 - "AppModule"
Cohesion: 0.27
Nodes (4): AppModule, Context, UsageStatsManager, WallpaperManager

### Community 37 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, HubEmptyState(), HubEmptyStatePreview(), Modifier

### Community 38 - "DockAppRepository"
Cohesion: 0.08
Nodes (22): BackupRestoreScreenTest, DockSettingsScreenTest, AppRepository, BackupRepository, Uri, DefaultFavoriteAppRepository, DockAppRepository, FavoriteAppRepository (+14 more)

### Community 39 - "ClockAccessoryIcons.kt"
Cohesion: 0.31
Nodes (10): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+2 more)

### Community 40 - "AccentSwatch"
Cohesion: 0.05
Nodes (31): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarPickerRow(), CalendarSettingsContent() (+23 more)

### Community 41 - "AppearanceSettingsScreen.kt"
Cohesion: 0.07
Nodes (28): AppearanceSettingsScreenTest, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, WallpaperAccentRole, PRIMARY, SECONDARY (+20 more)

### Community 43 - "LauncherFontOption"
Cohesion: 0.13
Nodes (13): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+5 more)

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.20
Nodes (6): BackupRestoreUiState, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 46 - "LauncherAppWidgetHost"
Cohesion: 0.22
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 47 - "TonalButton"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "FavoriteAppEntity"
Cohesion: 0.17
Nodes (3): FavoriteAppEntity, toFavoriteAppEntity(), FavoriteAppDaoTest

### Community 51 - "OnboardingScreen"
Cohesion: 0.18
Nodes (15): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, HOME_SETUP, INTRO, PROFILES, SET_DEFAULT (+7 more)

### Community 52 - "WidgetProviderOption"
Cohesion: 0.27
Nodes (13): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+5 more)

### Community 53 - "OnboardingHomeSetupPage.kt"
Cohesion: 0.29
Nodes (16): ConfirmDialog(), Modifier, AppDrawerSection(), DockAppsReorderRow(), DockClickableRow(), DockSection(), FavoritesClickableRow(), FavoritesReorderList() (+8 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.11
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.05
Nodes (84): Image, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, NotificationBadgeStyle, COUNT, DOT (+76 more)

### Community 57 - "CardDivider"
Cohesion: 0.42
Nodes (8): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview()

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 62 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 63 - ".drawerViewModel"
Cohesion: 0.19
Nodes (3): T, RankBySearchRelevanceUseCase, DrawerViewModelTest

### Community 64 - "ManageProfilesScreen.kt"
Cohesion: 0.45
Nodes (10): AddProfileRow(), Modifier, PaddingValues, ManageProfilesContent(), ManageProfilesHeader(), ManageProfilesScreen(), ManageProfilesScreenPreview(), ProfileReorderList() (+2 more)

### Community 65 - "FavoritesPickerScreen.kt"
Cohesion: 0.61
Nodes (7): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader()

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 69 - "HomeDrawerRouteTest.kt"
Cohesion: 0.10
Nodes (9): ContactPermissionRepository, Flow, Flow, Flow, ClockAccessoryState, Flow, ObserveClockAccessoriesUseCase, SeedDefaultDockUseCase (+1 more)

### Community 70 - "FacetNavHost"
Cohesion: 0.19
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen() (+5 more)

### Community 72 - "NotificationSettingsViewModel"
Cohesion: 0.36
Nodes (4): StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 73 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 74 - "EnsureActiveProfileUseCaseTest.kt"
Cohesion: 0.48
Nodes (4): DataStoreModule, Context, DataStore, Preferences

### Community 77 - "LauncherViewModel.kt"
Cohesion: 0.21
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 80 - "LabeledDropdownRow"
Cohesion: 0.29
Nodes (11): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+3 more)

### Community 82 - ".setContent"
Cohesion: 0.27
Nodes (3): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest

### Community 83 - "DockAppEntity"
Cohesion: 0.10
Nodes (8): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 84 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 86 - "PermissionsViewModel"
Cohesion: 0.38
Nodes (3): StateFlow, ViewModel, PermissionsViewModel

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - ".setContent"
Cohesion: 0.12
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 90 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "ProfileCarouselViewModel"
Cohesion: 0.33
Nodes (3): StateFlow, ViewModel, ProfileCarouselViewModel

### Community 96 - ".setContent"
Cohesion: 0.10
Nodes (6): SettingsScreenTest, Intent, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 97 - "AppContextMenuTest"
Cohesion: 0.11
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 103 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 105 - "DrawerPresentation"
Cohesion: 0.14
Nodes (8): DrawerPresentation, GRID, LIST, OnboardingUiState, Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 107 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 108 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 109 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 110 - "LauncherSettings"
Cohesion: 0.11
Nodes (9): CalendarPermissionRepository, LauncherSettings, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest (+1 more)

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 113 - "ManageProfilesViewModel"
Cohesion: 0.10
Nodes (7): ManageProfilesScreenTest, StateFlow, ViewModel, ManageProfilesViewModel, FakeProfileDao, Flow, ManageProfilesViewModelTest

### Community 114 - "homeAppLabelShadow"
Cohesion: 0.23
Nodes (13): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview(), accentTonalExtremes(), homeAppLabelShadow() (+5 more)

### Community 118 - ".useCase"
Cohesion: 0.05
Nodes (11): GroupAppsByLetterUseCase, GroupedApps, AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, ImportBackupUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest (+3 more)

### Community 121 - "BackupRestoreMessage"
Cohesion: 0.18
Nodes (10): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+2 more)

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.15
Nodes (9): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+1 more)

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "HubViewModel"
Cohesion: 0.15
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.19
Nodes (5): DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 148 - "BackButton"
Cohesion: 0.20
Nodes (13): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+5 more)

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 163 - "FacetDatabase"
Cohesion: 0.20
Nodes (6): DatabaseModule, Context, FacetDatabase, Migrations, Migration, RoomDatabase

### Community 165 - ".setContent"
Cohesion: 0.09
Nodes (21): ProfileSettingsScreenTest, InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), Modifier, RenameDialog(), Composable (+13 more)

### Community 168 - "ProfileDockAppEntity"
Cohesion: 0.09
Nodes (8): Flow, ProfileDockAppDao, ProfileDockAppEntity, toProfileDockAppEntity(), ProfileDockAppDaoTest, FakeProfileDockAppDao, Flow, ProfileDockAppRepositoryTest

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.36
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 192 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 196 - "NextAlarmRepositoryTest"
Cohesion: 0.09
Nodes (17): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+9 more)

### Community 203 - ".homeViewModel"
Cohesion: 0.11
Nodes (6): NotificationShadeRepository, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest, HomeViewModelTest

### Community 206 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+2 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 213 - "DockSettingsScreen.kt"
Cohesion: 0.21
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent() (+6 more)

### Community 215 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.23
Nodes (6): Inputs, Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 233 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 237 - "HubGrid"
Cohesion: 0.17
Nodes (15): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+7 more)

## Knowledge Gaps
- **365 isolated node(s):** `Keys`, `CatalogEntry`, `ICONS`, `TEXT`, `LIST` (+360 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 664 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `AppInfo`, `ClockColorOption`, `WidgetPlacementEntity`, `.setContent`, `.setContent`, `DockAppPickerScreen.kt`, `BackButton`, `.setContent`, `DrawerGridSize`, `.setContent`, `ProfileCarouselScreen.kt`, `ContactRepositoryTest`, `.setContent`, `FacetDatabase`, `.setContent`, `DockAppRepository`, `dashedBorder`, `AccentSwatch`, `AppearanceSettingsScreen.kt`, `LauncherFontOption`, `LauncherAppWidgetHost`, `TonalButton`, `WidgetProviderOption`, `OnboardingHomeSetupPage.kt`, `AppDrawerScreen.kt`, `CardDivider`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `BackupRestoreContent`, `ManageProfilesScreen.kt`, `ColorTest`, `FavoritesPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `FacetNavHost`, `.setContent`, `LauncherViewModel.kt`, `HomeAppsListSettingsScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `AppDrawerSettingsScreen.kt`, `GestureHintOverlay`, `DockSettingsScreen.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `AppContextMenuTest`, `.setContent`, `AppDrawerScreen`, `.setContent`, `Type.kt`, `.setContent`, `HubGrid`, `.rendersOneEntryPerLetterProvided`, `ManageProfilesViewModel`, `homeAppLabelShadow`?**
  _High betweenness centrality (0.159) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `AppInfo`, `PlaceWidgetUseCase`, `ClockColorOption`, `combine`, `.setContent`, `.setContent`, `FakeProfileDao`, `ProfileEntity`, `DrawerViewModel.kt`, `SettingsRepositoryTest`, `ClockStyleGalleryViewModelTest`, `.setContent`, `.setContent`, `DrawerGridSize`, `.setContent`, `FacetDatabase`, `HomeWallpaper`, `.setContent`, `DockAppRepository`, `AccentSwatch`, `AppearanceSettingsScreen.kt`, `.createViewModel`, `.createViewModel`, `HomeDrawerRouteTest.kt`, `NotificationSettingsViewModel`, `EnsureActiveProfileUseCaseTest.kt`, `.homeViewModel`, `LauncherViewModel.kt`, `.setContent`, `PermissionsViewModel`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `.createViewModel`, `.setContent`, `DrawerPresentation`, `NotificationAccessExplanationViewModel.kt`, `LauncherSettings`, `ManageProfilesViewModel`, `.useCase`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ClockColorOption`, `combine`, `ProfileEntity`, `DefaultFavoriteAppEntity`, `DrawerViewModel.kt`, `FakeFavoriteAppDao`, `DockAppPickerScreen.kt`, `AppWidgetRepository.kt`, `SettingsSearchEntry`, `OnboardingViewModelTest`, `ProfileCarouselScreen.kt`, `ContactRepositoryTest`, `FavoritesPickerViewModel`, `HomeWallpaper`, `AppModule`, `DockAppRepository`, `ProfileDockAppEntity`, `AppearanceSettingsScreen.kt`, `FavoriteAppEntity`, `AppDrawerScreen.kt`, `HomeScreen`, `SettingsScreen.kt`, `Fixture`, `.createViewModel`, `FavoritesPickerScreen.kt`, `HomeDrawerRouteTest.kt`, `FacetNavHost`, `DockAppPickerViewModel`, `LauncherViewModel.kt`, `HomeAppsListSettingsScreen.kt`, `DockAppEntity`, `DockSettingsScreen.kt`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `Fixture`, `.setContent`, `AppContextMenuTest`, `DrawerPresentation`, `LauncherSettings`, `.useCase`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Are the 38 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.bottomPositionedSearchBarSitsInTheLowerHalfOfTheScreen()` and `.clearingTheSearchQueryRestoresTheFullList()`) actually correct?**
  _`FacetLauncherTheme()` has 38 INFERRED edges - model-reasoned connections that need verification._
- **Are the 13 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 13 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `CatalogEntry`, `ICONS` to the rest of the system?**
  _365 weakly-connected nodes found - possible documentation gaps or missing edges._