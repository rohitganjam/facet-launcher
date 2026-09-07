# Graph Report - lumen-launcher  (2026-09-06)

## Corpus Check
- 267 files · ~388,146 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2457 nodes · 6389 edges · 146 communities (97 shown, 46 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 519 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `cdb073ed`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- LumenLauncherTheme
- support.js
- ContactRepositoryTest
- HomeDrawerRoute
- ListContentMode
- Row
- ProfileEntity
- ProfileDao
- ContactConnection
- CalendarInfo
- SettingsRepository
- AppDrawerSettingsViewModel
- AppWidgetRepository.kt
- WidgetPlacementEntity
- ClockColorOption
- HomeDrawerRouteTest.kt
- Settings Screen (main)
- .setContent
- ResolveWidgetDropUseCase
- FavoriteAppRepository
- LauncherSettings
- HubViewModel
- AppShortcutRepository
- SettingsRepositoryTest.kt
- BackupMapping.kt
- NotificationAccessExplanationViewModel.kt
- UsageStatsRepository
- AppDrawerScreen.kt
- PlaceWidgetUseCase
- DefaultFavoriteAppRepository
- HubWidgetPickerViewModel
- AppWidgetRepository
- DockSettingsScreen.kt
- AppInfo
- NotificationBadgeRepository
- .setContent
- AppInfo
- ClockTemplateId
- ProfileSettingsScreen.kt
- CardDivider
- BackupRestoreViewModel
- DefaultFavoriteAppEntity
- Handoff: Minimal Android Launcher (README)
- HomeScreen.kt
- .setContent
- .setContent
- combine
- LumenNavHost
- .setContent
- Lumen Launcher Implementation Plan
- .setContent
- Hub, Onboarding & Pickers Spec Sheet
- Dark Theme: Hub, Onboarding & Picker Screens
- LauncherActivity.kt
- AppearanceSettingsScreen.kt
- CalendarSettingsScreen.kt
- DockAppPickerScreen.kt
- Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)
- HomeScreen
- ExportBackupUseCase.kt
- ImportBackupUseCase.kt
- CalendarEvent
- DrawerViewModelTest
- PaddingValues
- SettingsScreen.kt
- AccentSwatch
- ObserveHomeScreenStateUseCase.kt
- F6 App Drawer
- PRD: Minimal Android Launcher
- ColorTest
- BackButton
- HomeAppsListSettingsScreen.kt
- letterAt
- ProfileCarouselViewModel.kt
- ClockStyleGalleryViewModelTest
- BackupRestoreViewModelTest
- Settings Screen (dark)
- ObserveProfilePreviewsUseCase
- LauncherFontOption
- AppIcon
- ClockFontOption
- Fixture
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Light Turn3 Profiles And Settings (Screenshot)
- WidgetProviderOption
- .setContent
- DockAppEntity
- AppearanceSettingsViewModelTest
- HomeAppsListSettingsViewModel
- Light Turn1: Home & Drawer Design Exploration
- .setContent
- StickyHeaderLayout
- BackupRestoreContent
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- Dark Mode Turn 1: Home & Drawer Screenshot Sheet
- BackupRestoreMessage
- HubGrid
- IconRenderMode
- .createViewModel
- ContactConnectionsSheet.kt
- .setContent
- .setContent
- AlphabetRail
- GroupAppsByLetterUseCaseTest
- ObserveHubStateUseCase
- ContactRepository
- PermissionKind
- Color
- ClockFonts.kt
- F1 Home clock widget + calendar integration
- LumenDatabaseMigrationTest
- Dp
- .setContent
- FontWeight
- Type.kt
- .setContent
- .setContent
- HubAddWidgetEvent
- WidgetResizeHandle
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ContactConnectionsSheetTest
- CalendarEventsBlock.kt
- rememberTickingNow
- AddFailureReason
- AccentSwatch
- AppWidgetHostView
- androidx
- Color
- FontWeight
- Context
- CalendarEvent
- ClockFontOption
- ClockTemplateId
- LauncherFontOption
- Composable

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 152 edges
2. `AppInfo` - 119 edges
3. `ProfileEntity` - 119 edges
4. `SettingsRepository` - 110 edges
5. `Row` - 84 edges
6. `LauncherSettings` - 82 edges
7. `ProfileRepository` - 79 edges
8. `WidgetPlacementEntity` - 61 edges
9. `AppDrawerScreen()` - 50 edges
10. `ClockTemplateId` - 49 edges

## Surprising Connections (you probably didn't know these)
- `Appearance settings section (icon pack, accent color, drawer opacity)` --conceptually_related_to--> `AppearanceSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppearanceSettingsScreen.kt
- `"Change wallpaper" sheet row (opens system picker)` --conceptually_related_to--> `SettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/SettingsScreen.kt
- `Dock settings section (icons/text, drag to reorder, 3-5 apps)` --conceptually_related_to--> `DockSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/DockSettingsScreen.kt
- `Delete "Focus" profile confirmation dialog` --conceptually_related_to--> `ProfileSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/profiles/ProfileSettingsScreen.kt
- `App drawer settings section (list/grid presentation, grid size 4x4-5x6)` --conceptually_related_to--> `AppDrawerSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppDrawerSettingsScreen.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Clock style inheritance: global default, per-profile override, and calendar selection** — design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_clock_style_settings, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_clock_style_per_profile_override, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_calendar_selection_setting [EXTRACTED 1.00]
- **Bundled fonts licensed under SIL OFL 1.1** — third_party_font_licenses_manrope_ofl_manrope_font_license, third_party_font_licenses_notosans_ofl_notosans_font_license, third_party_font_licenses_poppins_ofl_poppins_font_license, third_party_font_licenses_robotoflex_ofl_robotoflex_font_license [EXTRACTED 1.00]
- **Home Long-press Sheet Menu Options** — design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_home_long_press_sheet, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_switch_profile_option, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_edit_profile_option, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_launcher_settings_option, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_change_wallpaper_option [EXTRACTED 1.00]
- **First-Run Onboarding Flow** — design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_onboarding_intro, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_onboarding_favorites_picker, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_default_launcher_prompt [EXTRACTED 1.00]
- **Profile Management Flow (Rename/Add/Switch)** — design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_profile_rename_dialog, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_add_profile_screen, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_profile_carousel_switch [EXTRACTED 1.00]
- **Long-press sheet's four destinations** — design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_long_press_sheet, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_profile_switcher_sheet, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_edit_profile_sheet, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_settings_screen_superseded, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_change_wallpaper_action [EXTRACTED 1.00]
- **Profile Management Flow (list, add, delete)** — design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_profiles_screen, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_profiles_favorites_profile, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_profiles_focus_profile, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_delete_profile_confirmation [EXTRACTED 1.00]
- **Settings screen sections: dock, app drawer, and appearance** — design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_dock_settings, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_app_drawer_settings, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_appearance_settings [EXTRACTED 1.00]
- **Settings screen's sub-sections shown together (panel 2c)** — design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_settings_screen_superseded, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_dock_settings_section, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_app_drawer_settings_section, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_appearance_settings_section, design_handoff_minimal_launcher_screenshots_light_turn2_sheet_and_settings_clock_style_screen [EXTRACTED 1.00]
- **App Drawer Progressive Disclosure Flow (Favorites -> Most Used -> Recents -> Search -> Alphabetical Grid)** — design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_home_favorites_dock, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_most_used_sort, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_recents_list, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_search_index_scrubber, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_alphabetical_app_grid [INFERRED 0.75]
- **Graceful Permission-Denied Fallback Pattern (Calendar events hidden, Contacts search apps-only)** — design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_calendar_permission_denied, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_contact_search_permission_denied, design_handoff_minimal_launcher_screenshots_dark_turn1_home_and_drawer_clock_widget_variant [INFERRED 0.75]
- **Drawer Presentation & Search Exploration (1h, 1i, 1j)** — design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_drawer_list_letter_headers, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_drawer_grid_no_headers, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_search_apps_contacts_actions [INFERRED 0.75]
- **Clock Style Variants (shared between Settings and detail screen)** — design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_clock_style_setting, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_clock_style_detail_screen, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_clock_style_light_stack_variant, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_clock_style_rule_variant, design_handoff_minimal_launcher_screenshots_dark_turn2_sheet_and_settings_clock_style_date_forward_variant [INFERRED 0.80]
- **Home Customization Settings Group (favorites, dock, calendars)** — design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_favorites_settings_screen, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_dock_settings_screen, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_calendar_settings_screen [INFERRED 0.80]
- **Hub Widget Lifecycle (add, populate, remove-when-unavailable)** — design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_hub_widget_grid, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_add_widget_picker, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_widget_unavailable_toast [INFERRED 0.80]
- **Settings Picker Pattern (Favorites/Dock/Calendars)** — design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_favorites_picker, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_dock_picker, design_handoff_minimal_launcher_screenshots_light_turn4_hub_onboarding_pickers_calendars_picker [INFERRED 0.80]
- **Async-vs-waitForIdle Compose testing lessons** — claude_testing_requirement, claude_animation_scale_rule, implementation_plan_waitforidle_vs_async_lesson, implementation_plan_animation_scale_incident [INFERRED 0.85]
- **Clock Style Design Exploration (1b, 1c, 1d)** — design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_clock_light_stack, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_clock_rule_meridiem, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_clock_date_forward [INFERRED 0.85]
- **Favorites Density Design Exploration (1e, 1f, 1g)** — design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_favorites_airy_trailing_dot, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_favorites_compact_leading_dot, design_handoff_minimal_launcher_screenshots_light_turn1_home_and_drawer_favorites_no_icons [INFERRED 0.85]
- **First-Run Onboarding Flow (intro -> favorites -> set default)** — design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_onboarding_intro, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_onboarding_favorites_picker, design_handoff_minimal_launcher_screenshots_dark_turn4_hub_onboarding_pickers_set_default_launcher_prompt [INFERRED 0.85]
- **MVVM layering conventions (composables/state-hoisting/strict-layering)** — claude_mvvm_layering, claude_strict_layering_rule, claude_stateless_composables_state_hoisting [INFERRED 0.85]
- **Per-Profile Clock Style Override Flow** — design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_profile_settings_screen, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_clock_style_page, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_clock_style_override [INFERRED 0.85]
- **Profile management flow: carousel, per-profile detail, and global settings entry point** — design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_profile_carousel_screen, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_profile_detail_screen, design_handoff_minimal_launcher_screenshots_dark_turn3_profiles_and_settings_settings_screen [INFERRED 0.85]
- **Profile Switching & Reordering Flow** — design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_profile_carousel, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_profile_reordering, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_switch_profile_long_press_sheet [INFERRED 0.85]
- **Shared vs Per-Profile Settings Architecture** — design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_launcher_settings_screen, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_profile_settings_screen, design_handoff_minimal_launcher_screenshots_light_turn3_profiles_and_settings_shared_vs_per_profile_settings_rationale [INFERRED 0.85]

## Communities (146 total, 46 thin omitted)

### Community 0 - "LumenLauncherTheme"
Cohesion: 0.20
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), LumenLauncherTheme()

### Community 1 - "support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "ContactRepositoryTest"
Cohesion: 0.11
Nodes (9): any(), AppRepositoryTest, eq(), T, CalendarRepositoryTest, InstanceRow, ContactRepositoryTest, LauncherActivityInfo (+1 more)

### Community 3 - "HomeDrawerRoute"
Cohesion: 0.10
Nodes (16): HomeViewModel, StateFlow, ViewModel, Axis, HORIZONTAL, VERTICAL, HomeDrawerRoute(), NestedScrollConnection (+8 more)

### Community 4 - "ListContentMode"
Cohesion: 0.04
Nodes (21): Converters, AppRowPosition, LEFT, RIGHT, AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY (+13 more)

### Community 5 - "Row"
Cohesion: 0.08
Nodes (73): ConfirmDialog(), Modifier, dashedBorder(), Color, Dp, Modifier, BoldColonTemplate(), BracketMinimalTemplate() (+65 more)

### Community 6 - "ProfileEntity"
Cohesion: 0.09
Nodes (7): ProfileEntity, Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 7 - "ProfileDao"
Cohesion: 0.16
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 8 - "ContactConnection"
Cohesion: 0.16
Nodes (12): ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER (+4 more)

### Community 9 - "CalendarInfo"
Cohesion: 0.07
Nodes (10): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow, ViewModel (+2 more)

### Community 10 - "SettingsRepository"
Cohesion: 0.06
Nodes (14): FakeCalendarPermissionRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, CalendarRepository, ContactPermissionRepository, NotificationAccessRepository, SettingsRepository (+6 more)

### Community 11 - "AppDrawerSettingsViewModel"
Cohesion: 0.08
Nodes (18): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerListItemSize, COMPACT, REGULAR (+10 more)

### Community 12 - "AppWidgetRepository.kt"
Cohesion: 0.16
Nodes (9): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, IntentSender, toBitmap(), calculateHubCellWidth() (+1 more)

### Community 13 - "WidgetPlacementEntity"
Cohesion: 0.13
Nodes (8): Flow, WidgetPlacementDao, WidgetPlacementEntity, Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "ClockColorOption"
Cohesion: 0.06
Nodes (17): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, FontWeightOption, EXTRA_LIGHT, LIGHT (+9 more)

### Community 15 - "HomeDrawerRouteTest.kt"
Cohesion: 0.09
Nodes (15): KeyboardDismissalTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, DeleteWidgetUseCase (+7 more)

### Community 16 - "Settings Screen (main)"
Cohesion: 0.08
Nodes (33): 24-hour Time Toggle, App Drawer Presentation Setting (List / Grid), Appearance: Accent Color Setting, Appearance: Icons Setting (System default / Monochrome), Backup & Restore Setting (Export settings as a file, widgets need re-adding on import), Calendar Events Toggle (2 calendars, all-day hidden), Change Wallpaper Option (Opens the system picker), Clock Style Variant: Date-forward (+25 more)

### Community 18 - "ResolveWidgetDropUseCase"
Cohesion: 0.15
Nodes (7): CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - "FavoriteAppRepository"
Cohesion: 0.11
Nodes (7): FavoriteAppRepository, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow, FavoriteAppDaoTest

### Community 20 - "LauncherSettings"
Cohesion: 0.17
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 22 - "AppShortcutRepository"
Cohesion: 0.14
Nodes (5): AppContextMenuTest, AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 24 - "BackupMapping.kt"
Cohesion: 0.19
Nodes (11): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toEnumOrDefault() (+3 more)

### Community 25 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.27
Nodes (6): StateFlow, ViewModel, NotificationAccessExplanationViewModel, StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 26 - "UsageStatsRepository"
Cohesion: 0.22
Nodes (5): AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, UsageStatsManager

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.21
Nodes (24): androidx, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile() (+16 more)

### Community 28 - "PlaceWidgetUseCase"
Cohesion: 0.39
Nodes (4): HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 29 - "DefaultFavoriteAppRepository"
Cohesion: 0.08
Nodes (15): AppRepository, flattenIcon(), Bitmap, Flow, DefaultFavoriteAppRepository, DockAppRepository, LumenDatabase, CleanUpUninstalledAppsUseCase (+7 more)

### Community 30 - "HubWidgetPickerViewModel"
Cohesion: 0.15
Nodes (5): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel, HubWidgetPickerViewModelTest

### Community 31 - "AppWidgetRepository"
Cohesion: 0.12
Nodes (5): AppWidgetRepository, Intent, AppWidgetRepositoryTest, DeleteWidgetUseCaseTest, ComponentName

### Community 32 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 33 - "AppInfo"
Cohesion: 0.10
Nodes (12): Flow, Flow, AppInfo, GetInstalledAppsUseCase, Flow, SharedFlow, StateFlow, ViewModel (+4 more)

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.11
Nodes (11): Callback, Callback, LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, Callback (+3 more)

### Community 37 - "ClockTemplateId"
Cohesion: 0.10
Nodes (17): ClockTemplateId, BOLD_COLON, BRACKET_MINIMAL, DATE_FORWARD, FLUID_STACK, ITALIC_ACCENT, LIGHT_STACK, ROBOTO_FLEX_NARROW (+9 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "CardDivider"
Cohesion: 0.38
Nodes (9): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+1 more)

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.21
Nodes (6): PendingWidgetUi, BackupRestoreViewModel, SharedFlow, StateFlow, Uri, ViewModel

### Community 41 - "DefaultFavoriteAppEntity"
Cohesion: 0.06
Nodes (13): DatabaseModule, Context, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, FavoriteAppDao, Flow, Migrations (+5 more)

### Community 42 - "Handoff: Minimal Android Launcher (README)"
Cohesion: 0.18
Nodes (18): Launcher Dark.dc.html (dark theme design canvas), Launcher.dc.html (light theme design canvas), F4 Profiles, F5 Launcher Hub (widgets), F9 Backgrounds (system wallpaper), Handoff: Minimal Android Launcher (README), Screen 2a — Long-press sheet, Screen 3a — Profile carousel (+10 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.16
Nodes (21): AppContextMenu(), Modifier, AppRow(), dashedBorder(), DockIcon(), HomeScreenTextOnlyPresentationPreview(), AppInfo, AppRowPosition (+13 more)

### Community 44 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 46 - "combine"
Cohesion: 0.13
Nodes (14): DockSettingsScreenTest, combine(), Flow, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, T1 (+6 more)

### Community 47 - "LumenNavHost"
Cohesion: 0.17
Nodes (16): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier (+8 more)

### Community 48 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 49 - "Lumen Launcher Implementation Plan"
Cohesion: 0.19
Nodes (14): F2 Home screen Favorites/Recents/Most Used, F3 Dock, Screen 1a — Home + drawer prototype, Screen 4j — Favorites picker, Screen 4k — Dock picker, Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan (+6 more)

### Community 50 - ".setContent"
Cohesion: 0.10
Nodes (7): SettingsScreenTest, DefaultLauncherRepository, SettingsUiState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 51 - "Hub, Onboarding & Pickers Spec Sheet"
Cohesion: 0.13
Nodes (15): design_handoff_minimal_launcher/README.md (Design Spec), Add Profile Screen, Calendars Settings Picker (Hide All-Day Events), Set-As-Default-Launcher Prompt, Permission-Denied and Empty States, Dock Settings Picker, Empty Search Results State ("Nothing matches"), Favorites Settings Picker (Drag Reorder + Search) (+7 more)

### Community 52 - "Dark Theme: Hub, Onboarding & Picker Screens"
Cohesion: 0.21
Nodes (16): Add Widget Picker (search + catalog), App Options Long-Press Menu (Info/Uninstall/Widgets/Rename), Calendars Settings Screen, Dock Settings Picker (4 apps), Denied & Empty Home States (no favorites/no apps), Favorites Settings List (search + toggles), Hub Widget Grid (empty & populated states), Onboarding Step: Pick a Few Favorites (+8 more)

### Community 53 - "LauncherActivity.kt"
Cohesion: 0.43
Nodes (4): Intent, LauncherActivity, Bundle, ComponentActivity

### Community 54 - "AppearanceSettingsScreen.kt"
Cohesion: 0.25
Nodes (18): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+10 more)

### Community 55 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.18
Nodes (12): DockAppPickerScreenTest, DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader() (+4 more)

### Community 57 - "Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)"
Cohesion: 0.18
Nodes (13): App drawer settings section (list/grid presentation, grid size 4x4-5x6), Appearance settings section (icon pack, accent color, drawer opacity), "Change wallpaper" sheet row (opens system picker), Clock style as its own full-width screen (panel 2d), Delete "Focus" profile confirmation dialog, Dock settings section (icons/text, drag to reorder, 3-5 apps), Long-press sheet (panel 2a): Switch profile / Edit profile / Launcher settings / Change wallpaper, Profiles page-grid, 2 of 3 (panel 2b, superseded by 3a) (+5 more)

### Community 59 - "ExportBackupUseCase.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 60 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (6): ImportBackupResult, ImportBackupUseCase, InvalidFile, Uri, Success, UnsupportedVersion

### Community 61 - "CalendarEvent"
Cohesion: 0.30
Nodes (5): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier

### Community 63 - "PaddingValues"
Cohesion: 0.20
Nodes (19): Modifier, T, LabeledDropdownRow(), Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar() (+11 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.23
Nodes (16): appsListSummary(), ClickableRow(), ListContentMode, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+8 more)

### Community 65 - "AccentSwatch"
Cohesion: 0.18
Nodes (11): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+3 more)

### Community 66 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.19
Nodes (5): NotificationShadeRepository, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, NotificationShadeRepositoryTest

### Community 67 - "F6 App Drawer"
Cohesion: 0.18
Nodes (12): F13 Notification Badges, F6 App Drawer, Just-in-time permissions principle, Screen 1h — Drawer list layout, Screen 1i — Drawer grid layout, Screen 3c — Launcher settings, Screens 4f-4h — First run flow, Screen 4p — Permission-denied and empty states (+4 more)

### Community 68 - "PRD: Minimal Android Launcher"
Cohesion: 0.17
Nodes (13): F10 App Drawer Overlay, F11 Label Customization (icon customization parked), F14 Backup & Restore, F8 Left-edge Rail Access, Consolidated gesture map, Third-party icon pack support (parked, F11 v2 candidate), PRD: Minimal Android Launcher, Non-functional performance requirements (M15 5G floor) (+5 more)

### Community 70 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 71 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.18
Nodes (15): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel() (+7 more)

### Community 73 - "ProfileCarouselViewModel.kt"
Cohesion: 0.15
Nodes (4): StateFlow, ViewModel, ProfileCarouselUiState, ProfileCarouselViewModel

### Community 76 - "Settings Screen (dark)"
Cohesion: 0.24
Nodes (13): App Drawer Settings (presentation, grid size, icons/labels/search), Appearance Settings (icon style, accent color, drawer opacity), Calendar Selection Setting (Select calendars, show calendar events), Clock Style Per-Profile Override (Inherit vs Override for Focus), Clock Style Settings (global default), Dock Settings (4 apps, shared across profiles), Favorite Apps Selection (chips, per-profile), Profile Carousel Screen (dark) (+5 more)

### Community 77 - "ObserveProfilePreviewsUseCase"
Cohesion: 0.25
Nodes (5): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 78 - "LauncherFontOption"
Cohesion: 0.17
Nodes (7): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockFontsTest

### Community 79 - "AppIcon"
Cohesion: 0.18
Nodes (13): NotificationBadgeStyle, COUNT, DOT, AppIcon(), AppIconGlyph(), badgeLabel(), Dp, Modifier (+5 more)

### Community 80 - "ClockFontOption"
Cohesion: 0.15
Nodes (7): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM

### Community 85 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 86 - "Light Turn3 Profiles And Settings (Screenshot)"
Cohesion: 0.35
Nodes (12): Light Turn3 Profiles And Settings (Screenshot), Clock Style Per-Profile Override (3f), Clock Style Settings Page (3e), Launcher Settings Screen (3c), Profile Carousel (3a), Profile Reordering Drag Gesture (3b), Profile Settings / Edit Profile Screen (3d), Shared vs Per-Profile Settings Rationale (+4 more)

### Community 87 - "WidgetProviderOption"
Cohesion: 0.37
Nodes (10): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+2 more)

### Community 89 - "DockAppEntity"
Cohesion: 0.10
Nodes (8): DockAppDao, Flow, DockAppEntity, toDockAppEntity(), DockAppRepositoryTest, FakeDockAppDao, Flow, DockAppDaoTest

### Community 91 - "HomeAppsListSettingsViewModel"
Cohesion: 0.11
Nodes (5): HomeAppsListSettingsScreenTest, HomeAppsListSettingsViewModel, StateFlow, ViewModel, HomeAppsListSettingsViewModelTest

### Community 92 - "Light Turn1: Home & Drawer Design Exploration"
Cohesion: 0.35
Nodes (11): 1d: Clock Style - Date-Forward (weekday leads, time recedes, calendar-denied fallback), 1b: Clock Style - Light Stack (200-weight numerals, calendar as quiet list), 1c: Clock Style - Rule & Meridiem (12h, hairline baseline, next event on rule), 1i: Drawer Presentation - 5x6 Grid, No Letter Headers, 9.5px Labels for Five Columns, 1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay, 1e: Favorites Density - Airy, Trailing Dot (18px names, 50px rows), 1f: Favorites Density - Compact, Leading Dot (8 apps fit, dot in icon gutter), 1g: Favorites Density - No Icons, Count-Free Dot, Names Carry Everything (4 apps only) (+3 more)

### Community 94 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 95 - "BackupRestoreContent"
Cohesion: 0.49
Nodes (9): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+1 more)

### Community 99 - "Dark Mode Turn 1: Home & Drawer Screenshot Sheet"
Cohesion: 0.33
Nodes (10): App Drawer: Full Alphabetical Grid, Calendar Access Denied State ("events hidden, Turn on"), Home Screen Clock Widget Variants (compact/expanded), Contact Search Results with Contacts-Permission-Denied Fallback, Favorite/Pinned App Star Marker (dot indicator), Home Screen: Favorites list + Dock (dark), Dark Mode Turn 1: Home & Drawer Screenshot Sheet, App Drawer: Most Used Sort Section (+2 more)

### Community 100 - "BackupRestoreMessage"
Cohesion: 0.20
Nodes (9): BackupRestoreEvent, BackupRestoreMessage, ExportFailed, ExportSucceeded, ImportFailedInvalidFile, ImportFailedUnsupportedVersion, ImportSucceeded, LaunchBindPermission (+1 more)

### Community 101 - "HubGrid"
Cohesion: 0.16
Nodes (16): HubGrid(), Modifier, resizedSpan(), ResizeEdge, END, START, toPx(), HubWidgetTile() (+8 more)

### Community 102 - "IconRenderMode"
Cohesion: 0.10
Nodes (12): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+4 more)

### Community 104 - "ContactConnectionsSheet.kt"
Cohesion: 0.44
Nodes (8): ConnectionRow(), ContactConnectionsSheet(), ContactConnectionsSheetPreview(), fallbackIcon(), androidx, Modifier, subtitle(), ImageVector

### Community 105 - ".setContent"
Cohesion: 0.30
Nodes (6): HubScreenTest, HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier

### Community 107 - "AlphabetRail"
Cohesion: 0.38
Nodes (4): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale()

### Community 108 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.24
Nodes (3): GroupAppsByLetterUseCase, GroupedApps, GroupAppsByLetterUseCaseTest

### Community 111 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 113 - "ClockFonts.kt"
Cohesion: 0.43
Nodes (6): FontFamily, resolveFontFamily(), variableWeightInstances(), Font, FontStyle, R

### Community 114 - "F1 Home clock widget + calendar integration"
Cohesion: 0.18
Nodes (11): F12 Long-Press Context Menu, F1 Home clock widget + calendar integration, F7 Alphabet Rail, Screen 3e — Clock style page (default), Screen 4i — App long-press context menu, Screen 4l — Calendars picker, Screen — Calendar settings (new), Known gap: no second clock style exists yet (+3 more)

### Community 119 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 122 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 123 - "WidgetResizeHandle"
Cohesion: 0.70
Nodes (4): Dp, Modifier, WidgetResizeHandle(), WidgetResizeHandlePreview()

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "CalendarEventsBlock.kt"
Cohesion: 0.71
Nodes (6): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier

### Community 133 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 134 - "AddFailureReason"
Cohesion: 0.67
Nodes (3): AddFailureReason, HUB_FULL, SETUP_CANCELLED

## Ambiguous Edges - Review These
- `Handoff: Minimal Android Launcher (README)` → `Manrope Font License (SIL OFL 1.1)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/README.md · relation: references
- `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` → `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/screenshots/light-turn1-home-and-drawer.png · relation: conceptually_related_to

## Knowledge Gaps
- **143 isolated node(s):** `START`, `END`, `BLUE`, `INDIGO`, `PURPLE` (+138 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 379 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **46 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Handoff: Minimal Android Launcher (README)` and `Manrope Font License (SIL OFL 1.1)`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` and `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `ContactConnectionsSheetTest`, `ListContentMode`, `Row`, `ContactConnection`, `CalendarInfo`, `SettingsRepository`, `HomeDrawerRouteTest.kt`, `.setContent`, `ResolveWidgetDropUseCase`, `AppShortcutRepository`, `AppDrawerScreen.kt`, `DefaultFavoriteAppRepository`, `DockSettingsScreen.kt`, `.setContent`, `ProfileSettingsScreen.kt`, `CardDivider`, `HomeScreen.kt`, `.setContent`, `.setContent`, `combine`, `LumenNavHost`, `.setContent`, `.setContent`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `CalendarSettingsScreen.kt`, `DockAppPickerScreen.kt`, `HomeScreen`, `CalendarEvent`, `PaddingValues`, `SettingsScreen.kt`, `AccentSwatch`, `ColorTest`, `BackButton`, `HomeAppsListSettingsScreen.kt`, `LauncherFontOption`, `AppIcon`, `WidgetProviderOption`, `.setContent`, `HomeAppsListSettingsViewModel`, `.setContent`, `StickyHeaderLayout`, `BackupRestoreContent`, `IconRenderMode`, `ContactConnectionsSheet.kt`, `.setContent`, `.setContent`, `AlphabetRail`, `.setContent`, `Type.kt`, `.setContent`, `.setContent`, `WidgetResizeHandle`?**
  _High betweenness centrality (0.194) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `LumenLauncherTheme`, `ContactRepositoryTest`, `HomeDrawerRoute`, `ListContentMode`, `ContactConnection`, `SettingsRepository`, `HomeDrawerRouteTest.kt`, `FavoriteAppRepository`, `LauncherSettings`, `AppShortcutRepository`, `UsageStatsRepository`, `DefaultFavoriteAppRepository`, `DockSettingsScreen.kt`, `ProfileSettingsScreen.kt`, `DefaultFavoriteAppEntity`, `HomeScreen.kt`, `.setContent`, `combine`, `LumenNavHost`, `.setContent`, `.setContent`, `LauncherActivity.kt`, `DockAppPickerScreen.kt`, `HomeScreen`, `ObserveHomeScreenStateUseCase.kt`, `HomeAppsListSettingsScreen.kt`, `ProfileCarouselViewModel.kt`, `ObserveProfilePreviewsUseCase`, `AppIcon`, `Fixture`, `DockAppEntity`, `HomeAppsListSettingsViewModel`, `.createViewModel`, `GroupAppsByLetterUseCaseTest`?**
  _High betweenness centrality (0.134) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ListContentMode`, `CalendarInfo`, `AppDrawerSettingsViewModel`, `ClockColorOption`, `HomeDrawerRouteTest.kt`, `.setContent`, `LauncherSettings`, `SettingsRepositoryTest.kt`, `NotificationAccessExplanationViewModel.kt`, `DefaultFavoriteAppRepository`, `AppInfo`, `.setContent`, `ClockTemplateId`, `.setContent`, `combine`, `.setContent`, `ExportBackupUseCase.kt`, `ImportBackupUseCase.kt`, `ObserveHomeScreenStateUseCase.kt`, `ProfileCarouselViewModel.kt`, `ClockStyleGalleryViewModelTest`, `LauncherFontOption`, `AppIcon`, `ClockFontOption`, `AppDrawerSettingsViewModelTest`, `.setContent`, `AppearanceSettingsViewModelTest`, `HomeAppsListSettingsViewModel`, `SettingsRepositoryTest`, `IconRenderMode`, `.createViewModel`, `.setContent`, `.setContent`, `.setContent`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.themed()`) actually correct?**
  _`LumenLauncherTheme()` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 7 INFERRED edges - model-reasoned connections that need verification._