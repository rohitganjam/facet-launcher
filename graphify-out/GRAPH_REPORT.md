# Graph Report - lumen-launcher  (2026-09-13)

## Corpus Check
- 356 files · ~793,746 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 3406 nodes · 9100 edges · 166 communities (114 shown, 46 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 826 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `44cb71ae`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockStyleGalleryViewModelTest
- design_handoff_minimal_launcher/support.js
- BatteryStatus
- .setContent
- ImportBackupResult
- HomeDrawerRouteTest.kt
- .createViewModel
- Android launcher design planning/support.js
- .setContent
- Screens
- HubWidgetPickerScreen.kt
- FakeFacetDao
- FavoritesPickerScreen.kt
- FavoriteAppEntity
- FakeFacetDockAppDao
- AppWidgetRepository
- Screens
- ObserveHomeScreenStateUseCase.kt
- FacetEntity
- CalendarPermissionRepository
- FacetCarouselUiState
- AppShortcut
- SettingsSearchEntry
- 4. Feature Requirements
- LauncherFontOption
- .setContent
- 4. Feature Requirements
- Keyboard Dismissal on Home Gesture (fix plan)
- DrawerGridSize
- FacetCarouselScreen.kt
- Row
- ListContentMode
- .setContent
- FakeFavoriteAppDao
- BackupMapping.kt
- UsageStatsRepository
- ContactRepositoryTest
- LauncherAppWidgetHost
- DockAppRepository
- dashedBorder
- ClockAlignment
- .setContent
- Manrope Font License (SIL OFL 1.1)
- HomeDrawerRoute
- FacetDaoTest
- BackupRestoreViewModel
- HubWidgetTile
- HomeScreen.kt
- Clock Widget Resize — Implementation Spec
- ClockCornerHandle
- AccentSwatch
- FacetDockAppEntity
- AppearanceSettingsScreen.kt
- CardDivider
- NotificationBadgeRepository
- 4. Feature Requirements
- AppDrawerScreen.kt
- NextAlarmRepository
- LabeledDropdownRow
- HomeScreen
- ClockAdjustSheet.kt
- WidgetPlacementEntity
- HomeWallpaper
- .setContent
- PermissionKind
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
- DockDisplayMode
- BackupRestoreViewModelTest
- .currentHomeWallpaper
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- .setContent
- .setContent
- OnboardingViewModel
- .setContent
- DockAppEntity
- HubEmptyState.kt
- Lumen Launcher Implementation Plan
- ClockColors.kt
- Play Console — sensitive permission disclosures
- AppInfo
- AppInfo
- Fixture
- Facet Launcher — Built Capabilities
- LauncherViewModel
- Play Console — store listing text
- GestureHintOverlay
- HomeViewModel
- ClockColorOption
- AppContextMenuTest
- AppDrawerScreen
- FacetLauncherTheme
- TonalButton
- SettingsRepository
- .setContent
- HomeAppsListSettingsScreen.kt
- ObserveFacetPreviewsUseCase.kt
- BackButton
- LauncherSettings
- HubWidgetPickerViewModel
- NotificationShadeRepository
- FacetDatabaseMigrationTest
- CalendarEventsBlock.kt
- .useCase
- SetDefaultLauncherSheetTest
- gradlew
- FacetApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ResolveWidgetResizeUseCase
- Facet Launcher — Onboarding Flow
- HubViewModel
- .setContent
- ClockAdjustMode
- DefaultFavoriteAppEntity
- Facet Launcher — Onboarding & Coach Marks: Design Brief
- 3. Canvas plan (artboards)
- SettingsRepositoryTest
- androidx
- OnboardingViewModelTest
- NestedScrollConnection
- NestedScrollSource
- 2. Design tokens
- FontWeightOption
- .setContent
- 2. Gate, seeding & architecture
- 3. Screen-by-screen
- Offset
- Intent
- WallpaperRepository
- ClockStyleGalleryScreen.kt
- FacetDatabase
- .setContent
- OnboardingIntroPage.kt
- homeAppLabelShadow
- .createViewModel
- FacetCarouselViewModel.kt
- NotificationSettingsScreen.kt
- AppContextMenu
- .rendersOneEntryPerLetterProvided
- DefaultAppRepositoryTest
- SettingsScreen.kt
- ColorTest
- letterAt
- release.sh
- DockSettingsScreen.kt
- AppDrawerSettingsScreen.kt
- Fixture
- ClockAccessoryIconsTest
- StickyHeaderLayout
- .setContent
- WeatherInfo.kt

## God Nodes (most connected - your core abstractions)
1. `FacetLauncherTheme()` - 225 edges
2. `FacetEntity` - 178 edges
3. `AppInfo` - 169 edges
4. `SettingsRepository` - 140 edges
5. `Row` - 127 edges
6. `FacetRepository` - 118 edges
7. `LauncherSettings` - 99 edges
8. `ClockTemplateId` - 71 edges
9. `WidgetPlacementEntity` - 60 edges
10. `ClockDateStyle` - 59 edges

## Surprising Connections (you probably didn't know these)
- `Lumen Launcher Engineering Conventions (CLAUDE.md)` --references--> `Lumen Launcher Implementation Plan`  [EXTRACTED]
  CLAUDE.md → IMPLEMENTATION_PLAN.md
- `Phase 10 — Advanced Clock Templates (F1 follow-up)` --references--> `Roboto Flex Font License (SIL OFL 1.1)`  [INFERRED]
  IMPLEMENTATION_PLAN.md → THIRD_PARTY_FONT_LICENSES/robotoflex_OFL.txt
- `HubWidgetPickerContent()` --calls--> `StickyHeaderLayout()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/picker/HubWidgetPickerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/components/StickyHeaderLayout.kt
- `HubWidgetPickerHeader()` --calls--> `BackButton()`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/picker/HubWidgetPickerScreen.kt → app/src/main/kotlin/com/facetlauncher/app/ui/components/BackButton.kt
- `HubWidgetPickerHeader()` --calls--> `Row`  [INFERRED]
  app/src/main/kotlin/com/facetlauncher/app/ui/hub/picker/HubWidgetPickerScreen.kt → app/src/test/kotlin/com/facetlauncher/app/data/ContactRepositoryTest.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]

## Communities (166 total, 46 thin omitted)

### Community 1 - "design_handoff_minimal_launcher/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "BatteryStatus"
Cohesion: 0.13
Nodes (13): BatteryRepository, BroadcastReceiver, BroadcastReceiver, Context, Flow, Intent, toBatteryStatus(), BatteryStatus (+5 more)

### Community 4 - "ImportBackupResult"
Cohesion: 0.47
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 5 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (10): flattenIcon(), Bitmap, AppModule, Context, AppListLimits, SeedDefaultDockUseCase, AppOpsManager, Drawable (+2 more)

### Community 7 - "Android launcher design planning/support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 8 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 9 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock style page (`3e` default, `3f` profile override), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 10 - "HubWidgetPickerScreen.kt"
Cohesion: 0.11
Nodes (30): AlarmClockIcon(), BatteryIcon(), BatteryIconState, CHARGING, FULL, LOW, PARTIAL, Color (+22 more)

### Community 11 - "FakeFacetDao"
Cohesion: 0.20
Nodes (4): EnsureActiveFacetUseCase, EnsureActiveFacetUseCaseTest, FakeFacetDao, Flow

### Community 12 - "FavoritesPickerScreen.kt"
Cohesion: 0.22
Nodes (13): AppIconSize, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 13 - "FavoriteAppEntity"
Cohesion: 0.11
Nodes (4): FavoriteAppDao, Flow, FavoriteAppEntity, FavoriteAppDaoTest

### Community 14 - "FakeFacetDockAppDao"
Cohesion: 0.24
Nodes (3): FacetDockAppRepositoryTest, FakeFacetDockAppDao, Flow

### Community 15 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 16 - "Screens"
Cohesion: 0.06
Nodes (32): About the Design Files, Accent handling, App Drawer (`1a`, `1h`, `1i`), App long-press menu (`4i`), Assets, Clock card (`3d`) and Calendar settings (new screen, wraps `4l`), Clock variants, Dark (`Launcher Dark.dc.html`) (+24 more)

### Community 17 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.27
Nodes (4): CalendarRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase

### Community 18 - "FacetEntity"
Cohesion: 0.05
Nodes (13): FacetRepository, toCsv(), FacetDao, Flow, FacetEntity, FacetRepositoryTest, FakeFacetDao, Flow (+5 more)

### Community 19 - "CalendarPermissionRepository"
Cohesion: 0.11
Nodes (11): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow (+3 more)

### Community 21 - "AppShortcut"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 24 - "LauncherFontOption"
Cohesion: 0.19
Nodes (11): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+3 more)

### Community 25 - ".setContent"
Cohesion: 0.08
Nodes (23): OnboardingScreenTest, Modifier, OnboardingDots(), FacetSwitchDemo(), Modifier, MockFacet, MockFacetCard(), OnboardingFacetsPage() (+15 more)

### Community 26 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 28 - "DrawerGridSize"
Cohesion: 0.07
Nodes (10): AppDrawerSettingsScreenTest, DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, AppDrawerSettingsViewModel, StateFlow (+2 more)

### Community 29 - "FacetCarouselScreen.kt"
Cohesion: 0.24
Nodes (14): AddFacetPage(), FacetCarouselContent(), NestedScrollConnection, FacetCarouselHeader(), FacetCarouselScreen(), FacetCarouselScreenPreview(), FacetPreviewPage(), Modifier (+6 more)

### Community 30 - "Row"
Cohesion: 0.21
Nodes (65): ClockDateStyle, CONDENSED, FULL, AlarmAccessoryContent(), BatteryAccessoryContent(), ClockAccessoryRow(), Color, FontFamily (+57 more)

### Community 31 - "ListContentMode"
Cohesion: 0.04
Nodes (22): Flow, Converters, T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition (+14 more)

### Community 33 - "FakeFavoriteAppDao"
Cohesion: 0.24
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 34 - "BackupMapping.kt"
Cohesion: 0.20
Nodes (13): BackupAppEntry, BackupFacet, BackupWidgetPlacement, T, toBackupEntry(), toBackupFacet(), toBackupPlacement(), toDefaultFavoriteAppEntity() (+5 more)

### Community 36 - "ContactRepositoryTest"
Cohesion: 0.05
Nodes (31): ContactConnectionsSheetTest, ContactRepository, LabeledValue, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL (+23 more)

### Community 37 - "LauncherAppWidgetHost"
Cohesion: 0.15
Nodes (9): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 38 - "DockAppRepository"
Cohesion: 0.08
Nodes (20): FavoritesPickerScreenTest, BackupRestoreScreenTest, AppRepository, BackupRepository, Uri, DefaultFavoriteAppRepository, DockAppRepository, FacetDockAppRepository (+12 more)

### Community 39 - "dashedBorder"
Cohesion: 0.36
Nodes (7): dashedBorder(), Color, Dp, Modifier, Modifier, OrphanedWidgetTile(), OrphanedWidgetTilePreview()

### Community 40 - "ClockAlignment"
Cohesion: 0.18
Nodes (6): ClockAlignment, CENTER, LEFT, RIGHT, Alignment, resolve()

### Community 43 - "HomeDrawerRoute"
Cohesion: 0.15
Nodes (17): androidx, Axis, HORIZONTAL, VERTICAL, detectHomeSwipeGestures(), HomeDrawerRoute(), NestedScrollConnection, HubWidgetPickerViewModel (+9 more)

### Community 45 - "BackupRestoreViewModel"
Cohesion: 0.12
Nodes (21): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+13 more)

### Community 46 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 47 - "HomeScreen.kt"
Cohesion: 0.19
Nodes (24): NotificationBadgeStyle, COUNT, DOT, AppIcon(), appIconCornerRadiusFor(), AppIconGlyph(), badgeLabel(), Dp (+16 more)

### Community 48 - "Clock Widget Resize — Implementation Spec"
Cohesion: 0.11
Nodes (18): 10. Open decisions (resolve before implementing), 1. Scope, 2. Data model, 3. Persistence, 4. Resolution (global vs. profile), 5.1 Anchor point — corrected design (do not use a per-corner `TransformOrigin` table), 5. Rendering mechanism, 6.1 Delta-to-scale conversion — alignment-dependent factor (important, easy to get wrong) (+10 more)

### Community 50 - "AccentSwatch"
Cohesion: 0.05
Nodes (34): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, InheritOverrideCard(), Modifier, RadioDot() (+26 more)

### Community 51 - "FacetDockAppEntity"
Cohesion: 0.11
Nodes (4): FacetDockAppDao, Flow, FacetDockAppEntity, FacetDockAppDaoTest

### Community 52 - "AppearanceSettingsScreen.kt"
Cohesion: 0.37
Nodes (13): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+5 more)

### Community 53 - "CardDivider"
Cohesion: 0.21
Nodes (19): DrawerPresentation, GRID, LIST, ConfirmDialog(), Modifier, CardDivider(), Modifier, SettingsCard() (+11 more)

### Community 54 - "NotificationBadgeRepository"
Cohesion: 0.10
Nodes (12): Callback, Callback, Flow, FacetNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest (+4 more)

### Community 55 - "4. Feature Requirements"
Cohesion: 0.07
Nodes (26): 10. Open Questions / Decisions Needed, 1. Overview, 2. Goals, 3. Non-Goals (v1), 3a. Parked for Future Consideration, 4. Feature Requirements, 5. Gesture Map, 6. Additional Considerations Still Open (+18 more)

### Community 56 - "AppDrawerScreen.kt"
Cohesion: 0.16
Nodes (25): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupAppsByLetterUseCase, GroupedApps, ContactRow() (+17 more)

### Community 57 - "NextAlarmRepository"
Cohesion: 0.36
Nodes (6): BroadcastReceiver, Context, Flow, Intent, NextAlarmRepository, BroadcastReceiver

### Community 58 - "LabeledDropdownRow"
Cohesion: 0.20
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, PaddingValues, ThemedDropdownMenu(), ThemedDropdownMenuItem() (+12 more)

### Community 60 - "ClockAdjustSheet.kt"
Cohesion: 0.33
Nodes (8): AdjustRow(), ClockAdjustSheet(), ClockAdjustSheetPreview(), Modifier, ClockZoneHandle(), ClockZoneHandlePreview(), Modifier, R

### Community 61 - "WidgetPlacementEntity"
Cohesion: 0.11
Nodes (9): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, ResolveWidgetDropUseCase, FakeWidgetPlacementDao, Flow (+1 more)

### Community 62 - "HomeWallpaper"
Cohesion: 0.14
Nodes (12): HomeWallpaper, Image, Tones, Unavailable, Alignment, Modifier, WallpaperBackground(), WallpaperRepository (+4 more)

### Community 63 - ".setContent"
Cohesion: 0.15
Nodes (6): DockSettingsScreenTest, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 64 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

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
Cohesion: 0.13
Nodes (14): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, FacetDestinations (+6 more)

### Community 72 - "ManageFacetsViewModel"
Cohesion: 0.12
Nodes (7): StateFlow, ViewModel, ManageFacetsUiState, ManageFacetsViewModel, FakeFacetDao, Flow, ManageFacetsViewModelTest

### Community 74 - "ClockFontOption"
Cohesion: 0.12
Nodes (8): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 75 - "DockDisplayMode"
Cohesion: 0.05
Nodes (28): DataStoreModule, Context, IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, DockDisplayMode, ICONS (+20 more)

### Community 78 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 79 - ".setContent"
Cohesion: 0.24
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 80 - ".setContent"
Cohesion: 0.16
Nodes (13): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+5 more)

### Community 81 - "OnboardingViewModel"
Cohesion: 0.21
Nodes (6): AppInfo, ListContentMode, StateFlow, ViewModel, OnboardingViewModel, DrawerPresentation

### Community 82 - ".setContent"
Cohesion: 0.09
Nodes (15): KeyboardDismissalTest, LauncherViewModel, CatalogEntry, SystemSettingsRepository, AddAppToDockUseCase, AddAppToFavoritesUseCase, Flow, ObserveQuickAddStateUseCase (+7 more)

### Community 83 - "DockAppEntity"
Cohesion: 0.10
Nodes (7): DockAppDao, Flow, DockAppEntity, DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 84 - "HubEmptyState.kt"
Cohesion: 0.83
Nodes (3): HubEmptyState(), HubEmptyStatePreview(), Modifier

### Community 85 - "Lumen Launcher Implementation Plan"
Cohesion: 0.11
Nodes (19): Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan, Inherit-default / Override-for-this-profile switch pattern, Known gap: no second clock style exists yet, Phase 0 — Repo & tooling setup, Phase 10 — Advanced Clock Templates (F1 follow-up), Phase 1 — Project scaffold + Home/Drawer skeleton (+11 more)

### Community 87 - "Play Console — sensitive permission disclosures"
Cohesion: 0.25
Nodes (7): Calendar — `READ_CALENDAR` (normal runtime permission, lower scrutiny), Contacts — `READ_CONTACTS` (normal runtime permission, lower scrutiny), Data Safety section — top-level answer, Notification access (powers app-icon badges), Play Console — sensitive permission disclosures, Uninstall shortcut — `REQUEST_DELETE_PACKAGES`, Usage access — `PACKAGE_USAGE_STATS` (powers "Most used" app sort)

### Community 88 - "AppInfo"
Cohesion: 0.07
Nodes (16): Flow, Flow, Flow, Flow, AppInfo, combine(), Flow, Flow (+8 more)

### Community 91 - "Facet Launcher — Built Capabilities"
Cohesion: 0.18
Nodes (11): 10. Gaps relevant to building onboarding, 1. What exists at launch today (no onboarding), 2. Home surface, 3. App Drawer, 4. Profiles, 5. Launcher Hub (widgets), 6. Appearance & theming, 7. Settings map (all built unless noted) (+3 more)

### Community 93 - "Play Console — store listing text"
Cohesion: 0.40
Nodes (4): Category, Full description (max 4000 characters — this draft is ~1,750), Play Console — store listing text, Short description (max 80 characters)

### Community 94 - "GestureHintOverlay"
Cohesion: 0.43
Nodes (5): GestureHintOverlay(), GestureHintOverlayPreview(), Modifier, Modifier, SurfaceButton()

### Community 95 - "HomeViewModel"
Cohesion: 0.12
Nodes (10): HomeViewModel, StateFlow, ViewModel, HomeViewModelTest, FacetEntity, LauncherSettings, DefaultLauncherRepository, FacetRepository (+2 more)

### Community 96 - "ClockColorOption"
Cohesion: 0.08
Nodes (9): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockStyleGalleryUiState, ClockStyleGalleryViewModel, StateFlow (+1 more)

### Community 99 - "AppDrawerScreen"
Cohesion: 0.12
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), AppDrawerScreenGridPreview()

### Community 100 - "FacetLauncherTheme"
Cohesion: 0.13
Nodes (7): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier, uniformScale(), FacetLauncherTheme()

### Community 101 - "TonalButton"
Cohesion: 0.30
Nodes (9): Modifier, ScreenHeader(), ScreenHeaderPreview(), ImageVector, Modifier, TonalButton(), HubHeader(), HubHeaderAtCapacityPreview() (+1 more)

### Community 102 - "SettingsRepository"
Cohesion: 0.05
Nodes (10): HomeAppsListSettingsScreenTest, DefaultAppRepository, Intent, SettingsRepository, WallpaperRepository, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, SelectPreviewAppsUseCase (+2 more)

### Community 106 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.35
Nodes (10): ReorderRowDefaults, DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview() (+2 more)

### Community 108 - "ObserveFacetPreviewsUseCase.kt"
Cohesion: 0.23
Nodes (6): FacetPreviewData, Inputs, Flow, ObserveFacetPreviewsUseCase, Fixture, ObserveFacetPreviewsUseCaseTest

### Community 109 - "BackButton"
Cohesion: 0.15
Nodes (16): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), StateFlow, ViewModel (+8 more)

### Community 110 - "LauncherSettings"
Cohesion: 0.09
Nodes (17): LauncherSettings, HomeUiState, FacetEntity, ListContentMode, ExportBackupUseCaseTest, FacetCarouselUiStateTest, HomeUiStateTest, AppListVerticalAlignment (+9 more)

### Community 112 - "HubWidgetPickerViewModel"
Cohesion: 0.07
Nodes (24): WidgetProviderOption, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase, AddFailed, AddFailureReason, HUB_FULL (+16 more)

### Community 117 - "CalendarEventsBlock.kt"
Cohesion: 0.27
Nodes (12): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier, BroadcastReceiver, Context (+4 more)

### Community 118 - ".useCase"
Cohesion: 0.06
Nodes (9): AssignCalendarColorsUseCaseTest, CompactWidgetsUseCaseTest, GroupAppsByLetterUseCaseTest, ImportBackupUseCaseTest, PlaceWidgetUseCaseTest, ResolveWidgetDropUseCaseTest, ResolveWidgetResizeUseCaseTest, SeedDefaultDockUseCaseTest (+1 more)

### Community 122 - "SetDefaultLauncherSheetTest"
Cohesion: 0.20
Nodes (7): SetDefaultLauncherSheetTest, AlreadyDefaultContent(), Modifier, SetDefaultContent(), SetDefaultLauncherSheet(), SetDefaultLauncherSheetAlreadyDefaultPreview(), Intent

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 133 - "ResolveWidgetResizeUseCase"
Cohesion: 0.15
Nodes (8): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetResizeUseCase, ObserveHubStateUseCaseTest, HubViewModelTest

### Community 135 - "Facet Launcher — Onboarding Flow"
Cohesion: 0.20
Nodes (10): 10. Open questions — resolved during the build, 1. Principles, 4. Coach marks (post-onboarding), 5. Code inventory (as shipped), 6. Reuse map (as shipped), 7. Edge cases, 8. Test plan (CLAUDE.md bar — no box ticked without green tests) — ✅ all green, 9. Task breakdown — all complete (+2 more)

### Community 138 - "HubViewModel"
Cohesion: 0.09
Nodes (21): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+13 more)

### Community 139 - ".setContent"
Cohesion: 0.07
Nodes (9): SettingsScreenTest, DefaultLauncherRepository, Intent, Intent, StateFlow, ViewModel, SettingsViewModel, DefaultLauncherRepositoryTest (+1 more)

### Community 140 - "ClockAdjustMode"
Cohesion: 0.50
Nodes (4): ClockAdjustMode, ADJUST, MENU, NONE

### Community 141 - "DefaultFavoriteAppEntity"
Cohesion: 0.17
Nodes (4): DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 142 - "Facet Launcher — Onboarding & Coach Marks: Design Brief"
Cohesion: 0.25
Nodes (5): 1. Product context, 4. Component inventory (reused across artboards), 5. Out of scope for these mocks, 6. Open design questions to explore in the mocks, Facet Launcher — Onboarding & Coach Marks: Design Brief

### Community 143 - "3. Canvas plan (artboards)"
Cohesion: 0.25
Nodes (8): 3. Canvas plan (artboards), Artboard 1 — Step 1: Intro (`4f`), Artboard 2 & 3 — Step 2: Set up your home screen (`4g`, extended), Artboard 4 — Step 3: Profiles teaser (new), Artboard 5 & 6 — Step 4: Set as default (`4h`), Artboard 7 — Coach mark: Home gesture hint overlay, Artboard 8 — Coach mark: Profiles callout, Artboard 9 — Coach mark: Hub callout *(optional)*

### Community 150 - "OnboardingViewModelTest"
Cohesion: 0.24
Nodes (4): Fixture, AppInfo, LauncherSettings, OnboardingViewModelTest

### Community 153 - "2. Design tokens"
Cohesion: 0.33
Nodes (6): 2. Design tokens, Dark, Device frame, Light, Shape (Material 3 scale), Type

### Community 155 - "FontWeightOption"
Cohesion: 0.10
Nodes (13): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, FacetType (+5 more)

### Community 157 - "2. Gate, seeding & architecture"
Cohesion: 0.40
Nodes (5): 2. Gate, seeding & architecture, Branch point — not a NavHost route, Dock auto-seed — runs before onboarding UI, Internal step navigation, New persisted state (DataStore, on `LauncherSettings`)

### Community 158 - "3. Screen-by-screen"
Cohesion: 0.40
Nodes (5): 3. Screen-by-screen, Step 1 — Intro (`4f`), Step 2 — Your home screen (`4g`, extended), Step 3 — Profiles (mockup + animated demo, zero real interaction), Step 4 — Set as default (`4h`)

### Community 162 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.31
Nodes (11): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen() (+3 more)

### Community 163 - "FacetDatabase"
Cohesion: 0.12
Nodes (8): DatabaseModule, Context, DefaultFavoriteAppDao, Flow, FacetDatabase, Migrations, Migration, RoomDatabase

### Community 165 - ".setContent"
Cohesion: 0.10
Nodes (18): FacetSettingsScreenTest, Modifier, RenameDialog(), FacetSettingsHeader(), FacetSettingsRow(), Composable, Modifier, NavigationChevron() (+10 more)

### Community 166 - "OnboardingIntroPage.kt"
Cohesion: 0.53
Nodes (8): AppRowBar(), ConceptLine(), HomeDiagram(), Dp, Modifier, LeaderRow(), OnboardingIntroPage(), OnboardingIntroPagePreview()

### Community 168 - "homeAppLabelShadow"
Cohesion: 0.32
Nodes (10): HubAtCapacityStrip(), HubAtCapacityStripPreview(), Modifier, accentTonalExtremes(), homeAppLabelShadow(), Color, toneOf(), wallpaperPrimaryAndSecondary() (+2 more)

### Community 170 - ".createViewModel"
Cohesion: 0.20
Nodes (3): AppearanceSettingsViewModelTest, WallpaperRepository, WallpaperRepository

### Community 172 - "FacetCarouselViewModel.kt"
Cohesion: 0.38
Nodes (3): FacetCarouselViewModel, StateFlow, ViewModel

### Community 173 - "NotificationSettingsScreen.kt"
Cohesion: 0.25
Nodes (9): displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview(), StateFlow, ViewModel, NotificationSettingsUiState (+1 more)

### Community 176 - "AppContextMenu"
Cohesion: 0.80
Nodes (4): AppContextMenu(), AppContextMenuDragHandle(), AppContextMenuItem(), Modifier

### Community 189 - "SettingsScreen.kt"
Cohesion: 0.30
Nodes (13): appDrawerSummary(), appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader() (+5 more)

### Community 195 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 206 - "DockSettingsScreen.kt"
Cohesion: 0.19
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DockAppsRow(), DockClickableRow(), dockDisplayLabel() (+6 more)

### Community 211 - "AppDrawerSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview(), AppDrawerToggleRow(), DrawerOpacitySlider(), Modifier

### Community 222 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

## Knowledge Gaps
- **365 isolated node(s):** `VERTICAL`, `HORIZONTAL`, `Multiple`, `Single`, `ExportFailed` (+360 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 676 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `FacetLauncherTheme()` connect `FacetLauncherTheme` to `.setContent`, `.setContent`, `HubWidgetPickerScreen.kt`, `.setContent`, `FavoritesPickerScreen.kt`, `ObserveHomeScreenStateUseCase.kt`, `CalendarPermissionRepository`, `LauncherFontOption`, `.setContent`, `FontWeightOption`, `DrawerGridSize`, `.setContent`, `FacetCarouselScreen.kt`, `.setContent`, `ClockStyleGalleryScreen.kt`, `ContactRepositoryTest`, `.setContent`, `DockAppRepository`, `LauncherAppWidgetHost`, `homeAppLabelShadow`, `.setContent`, `dashedBorder`, `OnboardingIntroPage.kt`, `BackupRestoreViewModel`, `NotificationSettingsScreen.kt`, `HomeScreen.kt`, `AccentSwatch`, `.rendersOneEntryPerLetterProvided`, `AppearanceSettingsScreen.kt`, `CardDivider`, `AppDrawerScreen.kt`, `LabeledDropdownRow`, `HomeScreen`, `ClockAdjustSheet.kt`, `WidgetPlacementEntity`, `SettingsScreen.kt`, `.setContent`, `ColorTest`, `WidgetResizeHandle.kt`, `.setContent`, `FacetNavHost`, `DockDisplayMode`, `DockSettingsScreen.kt`, `.setContent`, `.setContent`, `.setContent`, `AppDrawerSettingsScreen.kt`, `HubEmptyState.kt`, `GestureHintOverlay`, `StickyHeaderLayout`, `.setContent`, `AppContextMenuTest`, `AppDrawerScreen`, `TonalButton`, `SettingsRepository`, `.setContent`, `HomeAppsListSettingsScreen.kt`, `BackButton`, `SetDefaultLauncherSheetTest`?**
  _High betweenness centrality (0.160) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `HomeDrawerRouteTest.kt`, `.createViewModel`, `.setContent`, `FavoritesPickerScreen.kt`, `DefaultFavoriteAppEntity`, `FavoriteAppEntity`, `FakeFacetDockAppDao`, `ObserveHomeScreenStateUseCase.kt`, `FacetEntity`, `AppShortcut`, `FacetCarouselScreen.kt`, `ListContentMode`, `FakeFavoriteAppDao`, `UsageStatsRepository`, `ContactRepositoryTest`, `.setContent`, `DockAppRepository`, `.setContent`, `FacetCarouselViewModel.kt`, `HomeScreen.kt`, `AppContextMenu`, `FacetDockAppEntity`, `AppearanceSettingsScreen.kt`, `CardDivider`, `NotificationBadgeRepository`, `AppDrawerScreen.kt`, `HomeScreen`, `SettingsScreen.kt`, `.setContent`, `FacetNavHost`, `DockDisplayMode`, `DockSettingsScreen.kt`, `.setContent`, `.setContent`, `DockAppEntity`, `Fixture`, `Fixture`, `AppContextMenuTest`, `AppDrawerScreen`, `FacetLauncherTheme`, `SettingsRepository`, `HomeAppsListSettingsScreen.kt`, `ObserveFacetPreviewsUseCase.kt`, `.useCase`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockStyleGalleryViewModelTest`, `.setContent`, `HomeDrawerRouteTest.kt`, `.createViewModel`, `.setContent`, `FakeFacetDao`, `ObserveHomeScreenStateUseCase.kt`, `SettingsRepositoryTest`, `CalendarPermissionRepository`, `FacetEntity`, `LauncherFontOption`, `FontWeightOption`, `.setContent`, `DrawerGridSize`, `Row`, `ListContentMode`, `.setContent`, `.setContent`, `DockAppRepository`, `ClockAlignment`, `.setContent`, `.createViewModel`, `FacetCarouselViewModel.kt`, `NotificationSettingsScreen.kt`, `AccentSwatch`, `CardDivider`, `AppDrawerScreen.kt`, `.setContent`, `ClockTemplateId`, `.setContent`, `FacetNavHost`, `.drawerViewModel`, `ManageFacetsViewModel`, `ClockFontOption`, `DockDisplayMode`, `.setContent`, `ClockColorOption`, `.setContent`, `ObserveFacetPreviewsUseCase.kt`, `BackButton`, `LauncherSettings`, `.useCase`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `FacetLauncherTheme()` (e.g. with `.setContent()` and `.setContent()`) actually correct?**
  _`FacetLauncherTheme()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `FacetEntity` (e.g. with `.`a non-null listContentMode round-trips through Room, not just an in-memory copy`()` and `.`every ListContentMode value round-trips`()`) actually correct?**
  _`FacetEntity` has 14 INFERRED edges - model-reasoned connections that need verification._
- **Are the 122 inferred relationships involving `Row` (e.g. with `.dynamicConnections()` and `AppContextMenu()`) actually correct?**
  _`Row` has 122 INFERRED edges - model-reasoned connections that need verification._
- **What connects `VERTICAL`, `HORIZONTAL`, `Multiple` to the rest of the system?**
  _365 weakly-connected nodes found - possible documentation gaps or missing edges._