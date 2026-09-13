# Graph Report - lumen-launcher  (2026-09-13)

## Corpus Check
- 355 files · ~791,330 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3355 nodes · 9097 edges · 176 communities (124 shown, 46 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 721 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `bc6fb647`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModel
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- .setContent
- ConvertersTest
- .createViewModel
- Android launcher design planning/support.js
- .setContent
- Screens
- ClockAccessoryIcons.kt
- FacetDao
- FavoritesPickerViewModel
- FakeFavoriteAppDao
- FacetDockAppEntity
- AppWidgetRepository
- Screens
- DockAppRepository
- FacetEntity
- NotificationAccessRepository
- FacetCarouselUiState
- AppRepository
- SettingsSearchEntry
- 4. Feature Requirements
- LauncherFontOption
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerPresentation
- FacetCarouselScreen.kt
- Row
- ClockColorOption
- .setContent
- .setContent
- .drawerViewModel
- HomeWallpaper
- ContactRepositoryTest
- LauncherAppWidgetHost
- BackupMapping.kt
- homeAppLabelShadow
- SelectPreviewAppsUseCaseTest
- .setContent
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- AccentSwatch
- BackupRestoreViewModel
- HubGrid
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- CalendarInfo
- BackupRestoreContent
- AppearanceSettingsScreen.kt
- CardDivider
- NotificationBadgeRepository
- 4. Feature Requirements
- AppDrawerScreen.kt
- .setContent
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- WidgetPlacementDao
- ManageFacetsScreen.kt
- WallpaperRepository
- DefaultLauncherRepository
- WidgetResizeHandle.kt
- ClockTemplateId
- github.md
- WallpaperRepositoryTest
- OnboardingScreen
- FacetNavHost
- CalendarSettingsScreen.kt
- ManageFacetsViewModel
- NextAlarmRepositoryTest
- CalendarSettingsViewModel
- WallpaperAccentRole
- ImportBackupUseCaseTest
- ResolveWidgetDropUseCaseTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- ClockStyleGalleryScreen.kt
- AppInfo
- OnboardingViewModel
- HomeDrawerRouteTest.kt
- DockAppEntity
- .setContent
- Lumen Launcher Implementation Plan
- .setContent
- Play Console — sensitive permission disclosures
- DockAppPickerViewModel
- .createViewModel
- Fixture
- Facet Launcher — Built Capabilities
- NotificationAccessExplanationViewModel.kt
- Play Console — store listing text
- GestureHintOverlay
- HomeViewModel
- FacetCarouselViewModel
- AppContextMenuTest
- OnboardingFacetsPage.kt
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- build_screenshot.py
- BatteryRepository.kt
- dashedBorder
- DockSettingsScreen.kt
- CompactWidgetsUseCaseTest
- Fixture
- BackButton
- LauncherSettings
- .homeViewModel
- HubWidgetPickerViewModel
- ResolveWidgetResizeUseCaseTest
- ClockAlignment.kt
- FacetDatabaseMigrationTest
- NextAlarmRepository
- CalendarEventsBlock.kt
- .useCase
- AppDrawerSettingsViewModelTest
- .setContent
- .setContent
- SetDefaultLauncherSheet
- AppRepository.kt
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ContactRepository
- GetInstalledAppsUseCase
- WidgetPlacementEntity
- HubContent
- Facet Launcher — Onboarding Flow
- HubWidgetTile
- InheritOverrideCard
- HubViewModel
- combine
- ClockAdjustMode
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- Axis
- SettingsRepositoryTest
- TypeTest.kt
- PermissionKind
- OnboardingViewModelTest
- 2. Design tokens
- GroupAppsByLetterUseCaseTest
- Type.kt
- NotificationBadgeStyle
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- .rendersOneEntryPerLetterProvided
- FavoriteAppDao
- .setContent
- .createViewModel
- AppIcon
- DefaultAppRepositoryTest
- SeedDefaultDockUseCaseTest
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- HomeAppsListSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- PermissionsScreen.kt
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 229 edges
2. `AppInfo` - 184 edges
3. `FacetEntity` - 183 edges
4. `SettingsRepository` - 150 edges
5. `Row` - 128 edges
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
- `ContactRepositoryTest` --calls--> `ContactRepository`  [INFERRED]
  app/src/test/kotlin/com/facetlauncher/app/data/ContactRepositoryTest.kt → app/src/main/kotlin/com/facetlauncher/app/data/ContactRepository.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (176 total, 46 thin omitted)

### Community 0 - "ClockStyleGalleryViewModel"
Cohesion: 0.08
Nodes (5): ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow, ViewModel, ClockStyleGalleryViewModelTest

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.28
Nodes (3): BatteryStatus, BatteryRepositoryTest, ObserveClockAccessoriesUseCaseTest

### Community 4 - ".setContent"
Cohesion: 0.11
Nodes (17): BackupRestoreScreenTest, ExportBackupUseCase, ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion (+9 more)

### Community 6 - ".createViewModel"
Cohesion: 0.22
Nodes (4): fakeWallpaperRepository(), WallpaperRepository, HomeAppsListSettingsViewModelTest, WallpaperRepository

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
Cohesion: 0.29
Nodes (11): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+3 more)

### Community 11 - "FacetDao"
Cohesion: 0.07
Nodes (11): DataStoreModule, Context, FacetDao, Flow, EnsureActiveFacetUseCase, FacetDaoTest, EnsureActiveFacetUseCaseTest, FakeFacetDao (+3 more)

### Community 12 - "FavoritesPickerViewModel"
Cohesion: 0.33
Nodes (5): FavoritesPickerUiState, FavoritesPickerViewModel, Flow, StateFlow, ViewModel

### Community 13 - "FakeFavoriteAppDao"
Cohesion: 0.24
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 14 - "FacetDockAppEntity"
Cohesion: 0.07
Nodes (9): Flow, FacetDockAppDao, Flow, FacetDockAppEntity, toFacetDockAppEntity(), FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow (+1 more)

### Community 15 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "DockAppRepository"
Cohesion: 0.06
Nodes (17): FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, DefaultFavoriteAppRepository, DockAppRepository, FacetDockAppRepository, FavoriteAppRepository, FavoriteAppEntity (+9 more)

### Community 18 - "FacetEntity"
Cohesion: 0.04
Nodes (13): FacetRepository, Flow, toCsv(), FacetEntity, FacetRepositoryTest, FakeFacetDao, Flow, FacetEntityTest (+5 more)

### Community 19 - "NotificationAccessRepository"
Cohesion: 0.11
Nodes (9): FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, NotificationAccessRepository, UsageAccessRepository, StateFlow, ViewModel, PermissionsViewModel (+1 more)

### Community 21 - "AppRepository"
Cohesion: 0.15
Nodes (9): AppRepository, AppModule, Context, FacetDatabase, AppOpsManager, LauncherApps, RoomDatabase, UsageStatsManager (+1 more)

### Community 22 - "SettingsSearchEntry"
Cohesion: 0.22
Nodes (3): SettingsSearchEntry, CatalogEntry, SystemSettingsRepositoryTest

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "LauncherFontOption"
Cohesion: 0.11
Nodes (13): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+5 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerPresentation"
Cohesion: 0.09
Nodes (19): ContactInfo, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT (+11 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.24
Nodes (13): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), Modifier, NestedScrollConnection (+5 more)

### Community 30 - "Row"
Cohesion: 0.22
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ClockColorOption"
Cohesion: 0.05
Nodes (49): Converters, T, resolveOverride(), ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED (+41 more)

### Community 32 - ".setContent"
Cohesion: 0.13
Nodes (3): FacetCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 35 - "HomeWallpaper"
Cohesion: 0.12
Nodes (9): HomeWallpaper, Tones, Unavailable, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest, WallpaperRepository (+1 more)

### Community 36 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 37 - "LauncherAppWidgetHost"
Cohesion: 0.22
Nodes (8): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost, AppWidgetManager

### Community 38 - "BackupMapping.kt"
Cohesion: 0.13
Nodes (17): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupFacet, BackupSettings, BackupWidgetPlacement, T (+9 more)

### Community 39 - "homeAppLabelShadow"
Cohesion: 0.24
Nodes (12): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, Color, resolve(), accentTonalExtremes(), homeAppLabelShadow(), Color (+4 more)

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.24
Nodes (9): detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, androidx, Modifier, NestedScrollConnection, NestedScrollSource, Offset (+1 more)

### Community 44 - "AccentSwatch"
Cohesion: 0.14
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.07
Nodes (17): BackupRestoreEvent, BackupRestoreMessage, BackupRestoreUiState, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded (+9 more)

### Community 46 - "HubGrid"
Cohesion: 0.21
Nodes (11): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+3 more)

### Community 47 - "HomeScreen.kt"
Cohesion: 0.30
Nodes (13): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier, AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview() (+5 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "CalendarInfo"
Cohesion: 0.21
Nodes (4): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo

### Community 51 - "BackupRestoreContent"
Cohesion: 0.56
Nodes (8): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold()

### Community 52 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 53 - "CardDivider"
Cohesion: 0.27
Nodes (16): ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard(), AppDrawerSection(), DockAppsReorderRow(), DockClickableRow() (+8 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.23
Nodes (20): GroupAppsByLetterUseCase, GroupedApps, ContactRow(), ContactsAccessStrip(), ContactsSettingOffStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent() (+12 more)

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (16): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+8 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "WidgetPlacementDao"
Cohesion: 0.15
Nodes (5): Flow, WidgetPlacementDao, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 62 - "ManageFacetsScreen.kt"
Cohesion: 0.21
Nodes (15): DragReorderState, Modifier, T, rememberDragReorderState(), AddFacetRow(), FacetReorderList(), FacetReorderRow(), Modifier (+7 more)

### Community 63 - "WallpaperRepository"
Cohesion: 0.28
Nodes (3): DockSettingsScreenTest, Bitmap, WallpaperRepository

### Community 64 - "DefaultLauncherRepository"
Cohesion: 0.22
Nodes (3): DefaultLauncherRepository, Intent, DefaultLauncherRepositoryTest

### Community 65 - "WidgetResizeHandle.kt"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 66 - "ClockTemplateId"
Cohesion: 0.05
Nodes (37): ClockTemplateId, ACCENT_CONTRAST, ACCENT_FIELD, ACCENTED_FLUID_STACK, ACCENTED_FLUID_STACK_INVERTED, BOLD_COLON, BRACKET_MINIMAL, CHIP (+29 more)

### Community 67 - "github.md"
Cohesion: 0.40
Nodes (4): Last sync, Screen map, Sync history, Updated in this project

### Community 69 - "OnboardingScreen"
Cohesion: 0.22
Nodes (12): Modifier, nextStep(), OnboardingScreen(), OnboardingStep, FACETS, HOME_SETUP, INTRO, SET_DEFAULT (+4 more)

### Community 70 - "FacetNavHost"
Cohesion: 0.28
Nodes (5): FacetDestinations, FacetNavHost(), Modifier, popBackStackSafely(), NavHostController

### Community 71 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 72 - "ManageFacetsViewModel"
Cohesion: 0.10
Nodes (6): ManageFacetsScreenTest, StateFlow, ViewModel, ManageFacetsViewModel, FakeFacetDao, ManageFacetsViewModelTest

### Community 74 - "CalendarSettingsViewModel"
Cohesion: 0.22
Nodes (4): AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel

### Community 75 - "WallpaperAccentRole"
Cohesion: 0.09
Nodes (15): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+7 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.31
Nodes (11): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+3 more)

### Community 80 - "AppInfo"
Cohesion: 0.15
Nodes (18): AppInfo, AppIconSize, Modifier, StickyHeaderLayout(), DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview() (+10 more)

### Community 81 - "OnboardingViewModel"
Cohesion: 0.19
Nodes (4): Intent, StateFlow, ViewModel, OnboardingViewModel

### Community 82 - "HomeDrawerRouteTest.kt"
Cohesion: 0.07
Nodes (23): KeyboardDismissalTest, BatteryRepository, ContactPermissionRepository, DefaultAppRepository, Intent, NotificationShadeRepository, SystemSettingsRepository, AddAppToDockUseCase (+15 more)

### Community 83 - "DockAppEntity"
Cohesion: 0.08
Nodes (9): Flow, DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow (+1 more)

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "DockAppPickerViewModel"
Cohesion: 0.33
Nodes (5): DockAppPickerUiState, DockAppPickerViewModel, Flow, StateFlow, ViewModel

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 92 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 95 - "HomeViewModel"
Cohesion: 0.22
Nodes (3): HomeViewModel, StateFlow, ViewModel

### Community 96 - "FacetCarouselViewModel"
Cohesion: 0.33
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 97 - "AppContextMenuTest"
Cohesion: 0.10
Nodes (6): AppContextMenuTest, SharedFlow, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 98 - "OnboardingFacetsPage.kt"
Cohesion: 0.38
Nodes (8): Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage(), OnboardingFacetsPagePreview()

### Community 99 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.23
Nodes (12): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubEmptyState(), HubEmptyStatePreview() (+4 more)

### Community 103 - "build_screenshot.py"
Cohesion: 0.47
Nodes (5): ImageDraw, build(), draw_tracked_text(), Refresh one Play Store marketing screenshot after a raw device capture changes.…, rounded_mask()

### Community 104 - "BatteryRepository.kt"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus()

### Community 105 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview()

### Community 106 - "DockSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+2 more)

### Community 109 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.17
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, FacetCarouselUiStateTest, HomeUiStateTest

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.08
Nodes (24): HubWidgetPickerScreenTest, WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow() (+16 more)

### Community 116 - "NextAlarmRepository"
Cohesion: 0.31
Nodes (6): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver

### Community 117 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 118 - ".useCase"
Cohesion: 0.17
Nodes (3): AssignCalendarColorsUseCaseTest, CleanUpUninstalledAppsUseCaseTest, PlaceWidgetUseCaseTest

### Community 122 - "SetDefaultLauncherSheet"
Cohesion: 0.62
Nodes (6): OnboardingUiState, AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview()

### Community 123 - "AppRepository.kt"
Cohesion: 0.47
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "ContactRepository"
Cohesion: 0.08
Nodes (34): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+26 more)

### Community 132 - "GetInstalledAppsUseCase"
Cohesion: 0.13
Nodes (9): GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, GetInstalledAppsUseCaseTest (+1 more)

### Community 133 - "WidgetPlacementEntity"
Cohesion: 0.07
Nodes (18): HubScreenTest, WidgetPlacementEntity, Flow, WidgetPlacementRepository, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState (+10 more)

### Community 134 - "HubContent"
Cohesion: 0.67
Nodes (5): HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 136 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 137 - "InheritOverrideCard"
Cohesion: 0.90
Nodes (4): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow()

### Community 138 - "HubViewModel"
Cohesion: 0.22
Nodes (3): HubUiState, HubWidgetUi, HubViewModel

### Community 139 - "combine"
Cohesion: 0.06
Nodes (20): SettingsScreenTest, Flow, Flow, combine(), Flow, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState (+12 more)

### Community 140 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.10
Nodes (7): DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity(), DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 144 - "Axis"
Cohesion: 0.67
Nodes (3): Axis, HORIZONTAL, VERTICAL

### Community 147 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.19
Nodes (4): Fixture, WallpaperRepository, OnboardingViewModelTest, WallpaperRepository

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "Type.kt"
Cohesion: 0.47
Nodes (5): FacetType, facetTypography(), FontFamily, FontWeight, resolve()

### Community 156 - "NotificationBadgeStyle"
Cohesion: 0.23
Nodes (7): NotificationBadgeStyle, COUNT, DOT, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 163 - "FavoriteAppDao"
Cohesion: 0.13
Nodes (6): DatabaseModule, Context, FavoriteAppDao, Flow, Migrations, Migration

### Community 165 - ".setContent"
Cohesion: 0.11
Nodes (17): FacetSettingsScreenTest, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron() (+9 more)

### Community 170 - ".createViewModel"
Cohesion: 0.15
Nodes (4): SelectPreviewAppsUseCase, AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 175 - "AppIcon"
Cohesion: 0.44
Nodes (8): AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp, Modifier, NotificationBadge(), ImageBitmap

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.33
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 206 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.29
Nodes (13): HomeSurfacePreview(), Color, FontWeight, Modifier, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+5 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "PermissionsScreen.kt"
Cohesion: 0.61
Nodes (7): Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview(), PermissionRowState

## Knowledge Gaps
- **366 isolated node(s):** `Keys`, `CatalogEntry`, `THEME`, `THEME_INVERTED`, `ACCENT_PRIMARY` (+361 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 672 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `ContactRepository`, `.setContent`, `WidgetPlacementEntity`, `.setContent`, `.setContent`, `combine`, `DockAppRepository`, `NotificationAccessRepository`, `AppRepository`, `LauncherFontOption`, `.setContent`, `Type.kt`, `DrawerPresentation`, `NotificationBadgeStyle`, `FacetCarouselScreen.kt`, `ClockColorOption`, `.setContent`, `.setContent`, `.rendersOneEntryPerLetterProvided`, `.setContent`, `LauncherAppWidgetHost`, `homeAppLabelShadow`, `.setContent`, `AccentSwatch`, `HomeScreen.kt`, `CalendarInfo`, `BackupRestoreContent`, `AppearanceSettingsScreen.kt`, `CardDivider`, `AppDrawerScreen.kt`, `.setContent`, `LabeledDropdownRow`, `HomeScreen`, `ClockAdjustSheet.kt`, `SettingsScreen.kt`, `ManageFacetsScreen.kt`, `WallpaperRepository`, `ColorTest`, `WidgetResizeHandle.kt`, `CalendarSettingsScreen.kt`, `ManageFacetsViewModel`, `WallpaperAccentRole`, `HomeAppsListSettingsScreen.kt`, `ClockStyleGalleryScreen.kt`, `AppInfo`, `HomeDrawerRouteTest.kt`, `AppDrawerSettingsScreen.kt`, `.setContent`, `.setContent`, `GestureHintOverlay`, `PermissionsScreen.kt`, `.setContent`, `AppContextMenuTest`, `OnboardingFacetsPage.kt`, `AppDrawerScreen`, `TonalButton`, `dashedBorder`, `DockSettingsScreen.kt`, `BackButton`, `HubWidgetPickerViewModel`, `.setContent`, `.setContent`, `SetDefaultLauncherSheet`?**
  _High betweenness centrality (0.158) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `.setContent`, `GetInstalledAppsUseCase`, `.setContent`, `.createViewModel`, `combine`, `FavoritesPickerViewModel`, `DefaultFavoriteAppEntity`, `FacetDockAppEntity`, `FakeFavoriteAppDao`, `DockAppRepository`, `FacetEntity`, `AppRepository`, `OnboardingViewModelTest`, `GroupAppsByLetterUseCaseTest`, `DrawerPresentation`, `FacetCarouselScreen.kt`, `ClockColorOption`, `.setContent`, `HomeWallpaper`, `ContactRepositoryTest`, `SelectPreviewAppsUseCaseTest`, `.setContent`, `.createViewModel`, `HomeDrawerRoute`, `AppIcon`, `HomeScreen.kt`, `AppearanceSettingsScreen.kt`, `CardDivider`, `NotificationBadgeRepository`, `AppDrawerScreen.kt`, `SeedDefaultDockUseCaseTest`, `HomeScreen`, `SettingsScreen.kt`, `FacetNavHost`, `WallpaperAccentRole`, `HomeAppsListSettingsScreen.kt`, `OnboardingViewModel`, `HomeDrawerRouteTest.kt`, `DockAppEntity`, `DockAppPickerViewModel`, `Fixture`, `Fixture`, `AppContextMenuTest`, `AppDrawerScreen`, `DockSettingsScreen.kt`, `Fixture`, `.useCase`, `AppRepository.kt`?**
  _High betweenness centrality (0.138) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModel`, `.setContent`, `.setContent`, `GetInstalledAppsUseCase`, `.createViewModel`, `combine`, `FacetDao`, `DockAppRepository`, `SettingsRepositoryTest`, `NotificationAccessRepository`, `FacetEntity`, `AppRepository`, `LauncherFontOption`, `.setContent`, `NotificationBadgeStyle`, `DrawerPresentation`, `ClockColorOption`, `.setContent`, `.setContent`, `.drawerViewModel`, `HomeWallpaper`, `.setContent`, `BackupMapping.kt`, `.setContent`, `.createViewModel`, `CalendarInfo`, `SeedDefaultDockUseCaseTest`, `.setContent`, `WallpaperRepository`, `ClockTemplateId`, `ManageFacetsViewModel`, `CalendarSettingsViewModel`, `WallpaperAccentRole`, `OnboardingViewModel`, `HomeDrawerRouteTest.kt`, `.createViewModel`, `NotificationAccessExplanationViewModel.kt`, `HomeViewModel`, `LauncherSettings`, `.homeViewModel`, `.useCase`, `AppDrawerSettingsViewModelTest`, `.setContent`, `.setContent`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.themed()` and `facetTypography()`) actually correct?**
  _`FacetLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 123 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 123 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `CatalogEntry`, `THEME` to the rest of the system?**
  _366 weakly-connected nodes found - possible documentation gaps or missing edges._