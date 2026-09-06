# Graph Report - lumen-launcher  (2026-09-06)

## Corpus Check
- 267 files · ~387,351 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2446 nodes · 6386 edges · 133 communities (88 shown, 42 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 505 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1360ec4e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- LumenLauncherTheme
- support.js
- ContactRepositoryTest
- .setContent
- ListContentMode
- Row
- ProfileEntity
- LumenDatabase
- ContactConnection
- CalendarRepository
- CalendarPermissionRepository
- AppDrawerSettingsViewModel
- AppWidgetRepository
- WidgetPlacementRepository
- SettingsRepository
- LauncherAppWidgetHost
- Settings Screen (main)
- .setContent
- WidgetPlacementEntity
- FavoriteAppRepository
- LauncherSettings
- HubViewModel
- AppShortcutRepository
- SettingsRepositoryTest.kt
- BackupMapping.kt
- NotificationAccessExplanationViewModel.kt
- HomeDrawerRouteTest.kt
- DockAppRepository
- .setContent
- SettingsScreenTest.kt
- HubWidgetPickerViewModel
- .refresh
- StickyHeaderLayout
- AppInfo
- NotificationBadgeRepository
- .setContent
- ProfileCarouselScreen.kt
- ClockTemplateId
- ProfileSettingsScreen.kt
- CardDivider
- BackupRestoreViewModel
- DefaultFavoriteAppRepository
- Handoff: Minimal Android Launcher (README)
- ProfileCarouselViewModel.kt
- .setContent
- AppRepository
- combine
- LumenNavHost
- .setContent
- Lumen Launcher Implementation Plan
- .setContent
- Hub, Onboarding & Pickers Spec Sheet
- Dark Theme: Hub, Onboarding & Picker Screens
- LauncherViewModel
- AppearanceSettingsScreen.kt
- CalendarSettingsScreen.kt
- DockAppPickerScreen.kt
- Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)
- CalendarRepositoryTest
- .setContent
- ImportBackupUseCase.kt
- CalendarEvent
- DrawerViewModelTest
- PaddingValues
- SettingsScreen.kt
- AccentSwatch
- FakeDockAppDao
- F6 App Drawer
- PRD: Minimal Android Launcher
- ColorTest
- BackButton
- HomeAppsListSettingsScreen.kt
- letterAt
- ProfileCarouselUiState
- ClockStyleGalleryViewModelTest
- DrawerPresentation
- Settings Screen (dark)
- Fixture
- LauncherFontOption
- NotificationBadgeStyle
- DrawerViewModel
- Fixture
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Light Turn3 Profiles And Settings (Screenshot)
- .setContent
- DockSettingsViewModel
- DockAppEntity
- AppearanceSettingsViewModelTest
- .createViewModel
- Light Turn1: Home & Drawer Design Exploration
- .setContent
- .setContent
- DrawerListItemSize
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- Dark Mode Turn 1: Home & Drawer Screenshot Sheet
- AppRepository.kt
- HubGrid
- IconRenderMode
- .createViewModel
- .setContent
- .setContent
- .setContent
- AppContextMenuTest
- GroupAppsByLetterUseCaseTest
- CleanUpUninstalledAppsUseCaseTest.kt
- ContactRepository
- PermissionKind
- Color
- FavoriteAppDaoTest
- F1 Home clock widget + calendar integration
- LumenDatabaseMigrationTest
- Dp
- .setContent
- FontWeight
- Type.kt
- UsageAccessExplanationViewModel
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- CalendarEventsBlock.kt
- rememberTickingNow
- androidx
- Color
- FontWeight

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 155 edges
2. `AppInfo` - 121 edges
3. `ProfileEntity` - 119 edges
4. `SettingsRepository` - 110 edges
5. `Row` - 83 edges
6. `LauncherSettings` - 82 edges
7. `ProfileRepository` - 79 edges
8. `WidgetPlacementEntity` - 61 edges
9. `AppDrawerScreen()` - 50 edges
10. `ClockTemplateId` - 49 edges

## Surprising Connections (you probably didn't know these)
- `Appearance settings section (icon pack, accent color, drawer opacity)` --conceptually_related_to--> `AppearanceSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppearanceSettingsScreen.kt
- `Dock settings section (icons/text, drag to reorder, 3-5 apps)` --conceptually_related_to--> `DockSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/DockSettingsScreen.kt
- `Delete "Focus" profile confirmation dialog` --conceptually_related_to--> `ProfileSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/profiles/ProfileSettingsScreen.kt
- `App drawer settings section (list/grid presentation, grid size 4x4-5x6)` --conceptually_related_to--> `AppDrawerSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppDrawerSettingsScreen.kt
- `"Change wallpaper" sheet row (opens system picker)` --conceptually_related_to--> `SettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/SettingsScreen.kt

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

## Communities (133 total, 42 thin omitted)

### Community 0 - "LumenLauncherTheme"
Cohesion: 0.05
Nodes (61): androidx, AlphabetRailTest, AppDrawerScreenTest, HomeScreenTest, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph() (+53 more)

### Community 1 - "support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 3 - ".setContent"
Cohesion: 0.06
Nodes (25): HomeDrawerRouteTest, HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel, HubContent() (+17 more)

### Community 4 - "ListContentMode"
Cohesion: 0.04
Nodes (22): Converters, AppRowPosition, LEFT, RIGHT, AppRowPresentation, ICON_AND_TEXT, ICON_ONLY, TEXT_ONLY (+14 more)

### Community 5 - "Row"
Cohesion: 0.12
Nodes (49): dashedBorder(), Color, Dp, Modifier, BoldColonTemplate(), BracketMinimalTemplate(), ClockDisplay(), dateFormatter() (+41 more)

### Community 6 - "ProfileEntity"
Cohesion: 0.10
Nodes (7): ProfileEntity, Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 7 - "LumenDatabase"
Cohesion: 0.06
Nodes (12): ClockStyleGalleryScreenTest, DatabaseModule, Context, LumenDatabase, Migrations, Flow, ProfileDao, Flow (+4 more)

### Community 8 - "ContactConnection"
Cohesion: 0.14
Nodes (20): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, ContactConnectionType, CALL, EMAIL, MESSAGE (+12 more)

### Community 9 - "CalendarRepository"
Cohesion: 0.06
Nodes (12): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel (+4 more)

### Community 10 - "CalendarPermissionRepository"
Cohesion: 0.10
Nodes (10): PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository, StateFlow, ViewModel (+2 more)

### Community 11 - "AppDrawerSettingsViewModel"
Cohesion: 0.16
Nodes (8): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, AppDrawerSettingsViewModel, StateFlow, ViewModel

### Community 12 - "AppWidgetRepository"
Cohesion: 0.09
Nodes (12): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+4 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.19
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 14 - "SettingsRepository"
Cohesion: 0.04
Nodes (27): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockFontOption, LAUNCHER_DEFAULT, MANROPE (+19 more)

### Community 15 - "LauncherAppWidgetHost"
Cohesion: 0.15
Nodes (9): HubWidgetPickerScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, AppWidgetHost (+1 more)

### Community 16 - "Settings Screen (main)"
Cohesion: 0.08
Nodes (33): 24-hour Time Toggle, App Drawer Presentation Setting (List / Grid), Appearance: Accent Color Setting, Appearance: Icons Setting (System default / Monochrome), Backup & Restore Setting (Export settings as a file, widgets need re-adding on import), Calendar Events Toggle (2 calendars, all-day hidden), Change Wallpaper Option (Opens the system picker), Clock Style Variant: Date-forward (+25 more)

### Community 17 - ".setContent"
Cohesion: 0.14
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (10): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase (+2 more)

### Community 19 - "FavoriteAppRepository"
Cohesion: 0.09
Nodes (8): FavoriteAppRepository, Flow, FavoriteAppDao, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 20 - "LauncherSettings"
Cohesion: 0.16
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 22 - "AppShortcutRepository"
Cohesion: 0.21
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 24 - "BackupMapping.kt"
Cohesion: 0.11
Nodes (20): BackupRepository, Uri, BackupAppEntry, BackupBundle, BackupProfile, BackupSettings, BackupWidgetPlacement, T (+12 more)

### Community 25 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 26 - "HomeDrawerRouteTest.kt"
Cohesion: 0.13
Nodes (9): AppModule, Context, NotificationShadeRepository, T, RankBySearchRelevanceUseCase, NotificationShadeRepositoryTest, AppOpsManager, LauncherApps (+1 more)

### Community 28 - ".setContent"
Cohesion: 0.22
Nodes (7): BackupRestoreScreenTest, ExportBackupUseCase, ImportBackupUseCase, HubFull, Placed, PlaceWidgetResult, PlaceWidgetUseCase

### Community 29 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 30 - "HubWidgetPickerViewModel"
Cohesion: 0.09
Nodes (24): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+16 more)

### Community 31 - ".refresh"
Cohesion: 0.21
Nodes (5): Callback, Callback, Flow, Callback, UserHandle

### Community 32 - "StickyHeaderLayout"
Cohesion: 0.36
Nodes (10): Modifier, StickyHeaderLayout(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen() (+2 more)

### Community 33 - "AppInfo"
Cohesion: 0.15
Nodes (6): AppInfo, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, GetInstalledAppsUseCaseTest, LauncherViewModelTest

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.19
Nodes (7): LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, NotificationListenerService, StatusBarNotification

### Community 36 - "ProfileCarouselScreen.kt"
Cohesion: 0.16
Nodes (27): ConfirmDialog(), Modifier, AddProfilePage(), AddProfileRow(), AppInfo, AppRowPosition, AppRowPresentation, CalendarEvent (+19 more)

### Community 37 - "ClockTemplateId"
Cohesion: 0.10
Nodes (17): ClockTemplateId, BOLD_COLON, BRACKET_MINIMAL, DATE_FORWARD, FLUID_STACK, ITALIC_ACCENT, LIGHT_STACK, ROBOTO_FLEX_NARROW (+9 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen(), NotificationSettingsScreenPreview() (+8 more)

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.07
Nodes (25): BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner(), PendingWidgetRow(), RowScaffold() (+17 more)

### Community 41 - "DefaultFavoriteAppRepository"
Cohesion: 0.09
Nodes (8): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppDao, Flow, DefaultFavoriteAppEntity, DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 42 - "Handoff: Minimal Android Launcher (README)"
Cohesion: 0.18
Nodes (18): Launcher Dark.dc.html (dark theme design canvas), Launcher.dc.html (light theme design canvas), F4 Profiles, F5 Launcher Hub (widgets), F9 Backgrounds (system wallpaper), Handoff: Minimal Android Launcher (README), Screen 2a — Long-press sheet, Screen 3a — Profile carousel (+10 more)

### Community 43 - "ProfileCarouselViewModel.kt"
Cohesion: 0.18
Nodes (6): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 44 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 45 - "AppRepository"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 46 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 47 - "LumenNavHost"
Cohesion: 0.20
Nodes (13): ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen(), Modifier, LumenDestinations (+5 more)

### Community 48 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 49 - "Lumen Launcher Implementation Plan"
Cohesion: 0.19
Nodes (14): F2 Home screen Favorites/Recents/Most Used, F3 Dock, Screen 1a — Home + drawer prototype, Screen 4j — Favorites picker, Screen 4k — Dock picker, Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan (+6 more)

### Community 51 - "Hub, Onboarding & Pickers Spec Sheet"
Cohesion: 0.13
Nodes (15): design_handoff_minimal_launcher/README.md (Design Spec), Add Profile Screen, Calendars Settings Picker (Hide All-Day Events), Set-As-Default-Launcher Prompt, Permission-Denied and Empty States, Dock Settings Picker, Empty Search Results State ("Nothing matches"), Favorites Settings Picker (Drag Reorder + Search) (+7 more)

### Community 52 - "Dark Theme: Hub, Onboarding & Picker Screens"
Cohesion: 0.21
Nodes (16): Add Widget Picker (search + catalog), App Options Long-Press Menu (Info/Uninstall/Widgets/Rename), Calendars Settings Screen, Dock Settings Picker (4 apps), Denied & Empty Home States (no favorites/no apps), Favorites Settings List (search + toggles), Hub Widget Grid (empty & populated states), Onboarding Step: Pick a Few Favorites (+8 more)

### Community 53 - "LauncherViewModel"
Cohesion: 0.23
Nodes (9): Intent, LauncherActivity, SharedFlow, StateFlow, ViewModel, LauncherUiState, LauncherViewModel, Bundle (+1 more)

### Community 54 - "AppearanceSettingsScreen.kt"
Cohesion: 0.20
Nodes (20): AccentSwatch, FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel() (+12 more)

### Community 55 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)"
Cohesion: 0.18
Nodes (13): App drawer settings section (list/grid presentation, grid size 4x4-5x6), Appearance settings section (icon pack, accent color, drawer opacity), "Change wallpaper" sheet row (opens system picker), Clock style as its own full-width screen (panel 2d), Delete "Focus" profile confirmation dialog, Dock settings section (icons/text, drag to reorder, 3-5 apps), Long-press sheet (panel 2a): Switch profile / Edit profile / Launcher settings / Change wallpaper, Profiles page-grid, 2 of 3 (panel 2b, superseded by 3a) (+5 more)

### Community 60 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 61 - "CalendarEvent"
Cohesion: 0.30
Nodes (5): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier

### Community 63 - "PaddingValues"
Cohesion: 0.20
Nodes (19): Modifier, T, LabeledDropdownRow(), Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar() (+11 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 66 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

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
Cohesion: 0.21
Nodes (14): DragReorderState, Modifier, T, rememberDragReorderState(), DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent() (+6 more)

### Community 75 - "DrawerPresentation"
Cohesion: 0.18
Nodes (6): DrawerPresentation, GRID, LIST, SearchBarPosition, BOTTOM, TOP

### Community 76 - "Settings Screen (dark)"
Cohesion: 0.24
Nodes (13): App Drawer Settings (presentation, grid size, icons/labels/search), Appearance Settings (icon style, accent color, drawer opacity), Calendar Selection Setting (Select calendars, show calendar events), Clock Style Per-Profile Override (Inherit vs Override for Focus), Clock Style Settings (global default), Dock Settings (4 apps, shared across profiles), Favorite Apps Selection (chips, per-profile), Profile Carousel Screen (dark) (+5 more)

### Community 78 - "LauncherFontOption"
Cohesion: 0.13
Nodes (13): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+5 more)

### Community 79 - "NotificationBadgeStyle"
Cohesion: 0.24
Nodes (6): NotificationBadgeStyle, COUNT, DOT, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 80 - "DrawerViewModel"
Cohesion: 0.29
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 85 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 86 - "Light Turn3 Profiles And Settings (Screenshot)"
Cohesion: 0.35
Nodes (12): Light Turn3 Profiles And Settings (Screenshot), Clock Style Per-Profile Override (3f), Clock Style Settings Page (3e), Launcher Settings Screen (3c), Profile Carousel (3a), Profile Reordering Drag Gesture (3b), Profile Settings / Edit Profile Screen (3d), Shared vs Per-Profile Settings Rationale (+4 more)

### Community 88 - "DockSettingsViewModel"
Cohesion: 0.43
Nodes (4): DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel

### Community 89 - "DockAppEntity"
Cohesion: 0.17
Nodes (4): DockAppDao, Flow, DockAppEntity, DockAppDaoTest

### Community 92 - "Light Turn1: Home & Drawer Design Exploration"
Cohesion: 0.35
Nodes (11): 1d: Clock Style - Date-Forward (weekday leads, time recedes, calendar-denied fallback), 1b: Clock Style - Light Stack (200-weight numerals, calendar as quiet list), 1c: Clock Style - Rule & Meridiem (12h, hairline baseline, next event on rule), 1i: Drawer Presentation - 5x6 Grid, No Letter Headers, 9.5px Labels for Five Columns, 1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay, 1e: Favorites Density - Airy, Trailing Dot (18px names, 50px rows), 1f: Favorites Density - Compact, Leading Dot (8 apps fit, dot in icon gutter), 1g: Favorites Density - No Icons, Count-Free Dot, Names Carry Everything (4 apps only) (+3 more)

### Community 95 - "DrawerListItemSize"
Cohesion: 0.33
Nodes (4): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS

### Community 99 - "Dark Mode Turn 1: Home & Drawer Screenshot Sheet"
Cohesion: 0.33
Nodes (10): App Drawer: Full Alphabetical Grid, Calendar Access Denied State ("events hidden, Turn on"), Home Screen Clock Widget Variants (compact/expanded), Contact Search Results with Contacts-Permission-Denied Fallback, Favorite/Pinned App Star Marker (dot indicator), Home Screen: Favorites list + Dock (dark), Dark Mode Turn 1: Home & Drawer Screenshot Sheet, App Drawer: Most Used Sort Section (+2 more)

### Community 100 - "AppRepository.kt"
Cohesion: 0.60
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 101 - "HubGrid"
Cohesion: 0.12
Nodes (20): HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan(), ResizeEdge, END, START (+12 more)

### Community 102 - "IconRenderMode"
Cohesion: 0.10
Nodes (12): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+4 more)

### Community 105 - ".setContent"
Cohesion: 0.24
Nodes (3): HubScreenTest, DeleteWidgetUseCase, DeleteWidgetUseCaseTest

### Community 108 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.24
Nodes (3): GroupAppsByLetterUseCase, GroupedApps, GroupAppsByLetterUseCaseTest

### Community 111 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 114 - "F1 Home clock widget + calendar integration"
Cohesion: 0.18
Nodes (11): F12 Long-Press Context Menu, F1 Home clock widget + calendar integration, F7 Alphabet Rail, Screen 3e — Clock style page (default), Screen 4i — App long-press context menu, Screen 4l — Calendars picker, Screen — Calendar settings (new), Known gap: no second clock style exists yet (+3 more)

### Community 119 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 123 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "CalendarEventsBlock.kt"
Cohesion: 0.71
Nodes (6): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier

### Community 133 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

## Ambiguous Edges - Review These
- `Handoff: Minimal Android Launcher (README)` → `Manrope Font License (SIL OFL 1.1)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/README.md · relation: references
- `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` → `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/screenshots/light-turn1-home-and-drawer.png · relation: conceptually_related_to

## Knowledge Gaps
- **143 isolated node(s):** `LumenType`, `HubFull`, `AddFailed`, `LaunchBindPermission`, `LaunchConfigure` (+138 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 367 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **42 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Handoff: Minimal Android Launcher (README)` and `Manrope Font License (SIL OFL 1.1)`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` and `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `.setContent`, `ListContentMode`, `Row`, `LumenDatabase`, `ContactConnection`, `CalendarRepository`, `CalendarPermissionRepository`, `LauncherAppWidgetHost`, `.setContent`, `WidgetPlacementEntity`, `AppShortcutRepository`, `HomeDrawerRouteTest.kt`, `.setContent`, `SettingsScreenTest.kt`, `HubWidgetPickerViewModel`, `StickyHeaderLayout`, `AppInfo`, `.setContent`, `ProfileCarouselScreen.kt`, `ProfileSettingsScreen.kt`, `CardDivider`, `BackupRestoreViewModel`, `ProfileCarouselViewModel.kt`, `.setContent`, `LumenNavHost`, `.setContent`, `.setContent`, `LauncherViewModel`, `AppearanceSettingsScreen.kt`, `CalendarSettingsScreen.kt`, `DockAppPickerScreen.kt`, `.setContent`, `CalendarEvent`, `PaddingValues`, `SettingsScreen.kt`, `AccentSwatch`, `ColorTest`, `BackButton`, `HomeAppsListSettingsScreen.kt`, `LauncherFontOption`, `NotificationBadgeStyle`, `.setContent`, `.setContent`, `.setContent`, `HubGrid`, `IconRenderMode`, `.setContent`, `.setContent`, `.setContent`, `AppContextMenuTest`, `.setContent`, `Type.kt`?**
  _High betweenness centrality (0.146) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `LumenLauncherTheme`, `.setContent`, `ListContentMode`, `ContactConnection`, `CalendarPermissionRepository`, `.setContent`, `FavoriteAppRepository`, `LauncherSettings`, `AppShortcutRepository`, `HomeDrawerRouteTest.kt`, `DockAppRepository`, `SettingsScreenTest.kt`, `.refresh`, `StickyHeaderLayout`, `ProfileSettingsScreen.kt`, `DefaultFavoriteAppRepository`, `ProfileCarouselViewModel.kt`, `AppRepository`, `LumenNavHost`, `.setContent`, `LauncherViewModel`, `DockAppPickerScreen.kt`, `SettingsScreen.kt`, `FakeDockAppDao`, `HomeAppsListSettingsScreen.kt`, `Fixture`, `DrawerViewModel`, `Fixture`, `DockSettingsViewModel`, `.createViewModel`, `.setContent`, `AppRepository.kt`, `.createViewModel`, `.setContent`, `AppContextMenuTest`, `GroupAppsByLetterUseCaseTest`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `.setContent`, `ListContentMode`, `LumenDatabase`, `CalendarRepository`, `CalendarPermissionRepository`, `AppDrawerSettingsViewModel`, `.setContent`, `LauncherSettings`, `SettingsRepositoryTest.kt`, `BackupMapping.kt`, `NotificationAccessExplanationViewModel.kt`, `HomeDrawerRouteTest.kt`, `.setContent`, `SettingsScreenTest.kt`, `AppInfo`, `.setContent`, `ClockTemplateId`, `DefaultFavoriteAppRepository`, `ProfileCarouselViewModel.kt`, `.setContent`, `LauncherViewModel`, `.setContent`, `ImportBackupUseCase.kt`, `ClockStyleGalleryViewModelTest`, `DrawerPresentation`, `LauncherFontOption`, `NotificationBadgeStyle`, `DrawerViewModel`, `AppDrawerSettingsViewModelTest`, `.setContent`, `DockSettingsViewModel`, `AppearanceSettingsViewModelTest`, `.createViewModel`, `.setContent`, `DrawerListItemSize`, `SettingsRepositoryTest`, `IconRenderMode`, `.createViewModel`, `.setContent`, `.setContent`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.setContent()` and `.themed()`) actually correct?**
  _`LumenLauncherTheme()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 7 INFERRED edges - model-reasoned connections that need verification._