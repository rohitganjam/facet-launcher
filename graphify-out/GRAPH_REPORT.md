# Graph Report - lumen-launcher  (2026-09-06)

## Corpus Check
- 267 files · ~385,439 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2408 nodes · 6374 edges · 140 communities (94 shown, 43 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 479 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b214abcd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- LumenLauncherTheme
- support.js
- AppRepository
- HomeDrawerRoute
- FontWeightOption
- Row
- ProfileEntity
- LumenDatabase
- ContactConnection
- CalendarPermissionRepository
- SettingsRepository
- AppDrawerSettingsViewModel
- AppWidgetRepository
- WidgetPlacementRepository
- ClockColorOption
- .setContent
- Settings Screen (main)
- .setContent
- WidgetPlacementEntity
- FavoriteAppRepository
- LauncherSettings
- HubViewModel
- AppShortcutRepository
- EnsureActiveProfileUseCase
- ExportBackupUseCase.kt
- NotificationAccessExplanationViewModel.kt
- KeyboardDismissalTest.kt
- DockAppRepository
- .setContent
- SettingsScreenTest.kt
- HubWidgetPickerViewModel
- AppDrawerScreen.kt
- DockSettingsScreen.kt
- AppInfo
- NotificationBadgeRepository
- AssignCalendarColorsUseCaseTest
- ProfileCarouselScreen.kt
- ClockTemplateId
- ProfileSettingsScreen.kt
- StickyHeaderLayout
- BackupRestoreViewModel
- DefaultFavoriteAppRepository
- Handoff: Minimal Android Launcher (README)
- ObserveProfilePreviewsUseCase.kt
- .setContent
- IconRenderMode
- combine
- LumenNavHost
- FavoritesPickerScreen.kt
- Lumen Launcher Implementation Plan
- .setContent
- Hub, Onboarding & Pickers Spec Sheet
- Dark Theme: Hub, Onboarding & Picker Screens
- LauncherViewModel
- AppearanceSettingsScreen.kt
- CalendarSettingsScreen.kt
- DockAppPickerScreen.kt
- Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)
- FakeDefaultFavoriteAppDao
- HomeScreen
- ImportBackupUseCase.kt
- CalendarEvent
- DrawerViewModelTest
- LabeledDropdownRow
- SettingsScreen.kt
- AccentSwatch
- FakeDockAppDao
- F6 App Drawer
- PRD: Minimal Android Launcher
- ColorTest
- BackButton
- HomeAppsListSettingsScreen.kt
- letterAt
- ProfileCarouselViewModel.kt
- ClockStyleGalleryViewModelTest
- DrawerPresentation
- Settings Screen (dark)
- BackupMapping.kt
- LauncherFontOption
- HomeScreen.kt
- DrawerViewModel
- Fixture
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Light Turn3 Profiles And Settings (Screenshot)
- .setContent
- ClockStyleGalleryScreen.kt
- DockAppEntity
- AppearanceSettingsViewModelTest
- .createViewModel
- Light Turn1: Home & Drawer Design Exploration
- .setContent
- DefaultFavoriteAppDao
- DockAppDao
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- Dark Mode Turn 1: Home & Drawer Screenshot Sheet
- FavoriteAppDao
- HubGrid
- AppearanceSettingsViewModel
- DockDisplayMode
- .setContent
- .setContent
- .setContent
- AppContextMenuTest
- GroupAppsByLetterUseCaseTest
- .setContent
- ContactRepository
- PermissionKind
- ProfileDao
- FavoriteAppDaoTest
- F1 Home clock widget + calendar integration
- LumenDatabaseMigrationTest
- WidgetPlacementDao
- .setContent
- AlphabetRail
- Type.kt
- ProfileDaoTest
- SettingsRepositoryTest.kt
- DrawerGridSize
- UsageAccessExplanationViewModel
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- ThemeMode
- CalendarEventsBlock.kt
- rememberTickingNow
- ContactConnectionType
- .`a valid backup replaces every profile, dock, and default favorite, then reports what was restored`
- Migrations
- androidx
- Color
- FontWeight

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 159 edges
2. `AppInfo` - 128 edges
3. `ProfileEntity` - 124 edges
4. `SettingsRepository` - 112 edges
5. `LauncherSettings` - 85 edges
6. `Row` - 82 edges
7. `ProfileRepository` - 79 edges
8. `WidgetPlacementEntity` - 61 edges
9. `ClockTemplateId` - 53 edges
10. `AppDrawerScreen()` - 50 edges

## Surprising Connections (you probably didn't know these)
- `Dock settings section (icons/text, drag to reorder, 3-5 apps)` --conceptually_related_to--> `DockSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/DockSettingsScreen.kt
- `Delete "Focus" profile confirmation dialog` --conceptually_related_to--> `ProfileSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/profiles/ProfileSettingsScreen.kt
- `Appearance settings section (icon pack, accent color, drawer opacity)` --conceptually_related_to--> `AppearanceSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppearanceSettingsScreen.kt
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

## Communities (140 total, 43 thin omitted)

### Community 0 - "LumenLauncherTheme"
Cohesion: 0.20
Nodes (3): AppDrawerScreenTest, AppDrawerScreen(), LumenLauncherTheme()

### Community 1 - "support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 2 - "AppRepository"
Cohesion: 0.05
Nodes (14): ProfileSettingsScreenTest, DockSettingsScreenTest, HomeAppsListSettingsScreenTest, AppRepository, any(), AppRepositoryTest, eq(), T (+6 more)

### Community 3 - "HomeDrawerRoute"
Cohesion: 0.07
Nodes (24): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModel, StateFlow, ViewModel, HubContent(), HubScreen() (+16 more)

### Community 4 - "FontWeightOption"
Cohesion: 0.04
Nodes (29): Converters, FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN (+21 more)

### Community 5 - "Row"
Cohesion: 0.09
Nodes (58): dashedBorder(), Color, Dp, Modifier, Modifier, RadioDot(), RadioOptionRow(), BoldColonTemplate() (+50 more)

### Community 6 - "ProfileEntity"
Cohesion: 0.07
Nodes (8): ProfileEntity, Flow, ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest, Flow

### Community 7 - "LumenDatabase"
Cohesion: 0.26
Nodes (4): DatabaseModule, Context, LumenDatabase, RoomDatabase

### Community 8 - "ContactConnection"
Cohesion: 0.20
Nodes (14): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnection, Multiple, Single, ConnectionRow(), ContactConnectionsSheet() (+6 more)

### Community 9 - "CalendarPermissionRepository"
Cohesion: 0.08
Nodes (11): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, FakeCalendarPermissionRepository, CalendarPermissionRepository, CalendarRepository, CalendarInfo, CalendarSettingsViewModel (+3 more)

### Community 10 - "SettingsRepository"
Cohesion: 0.07
Nodes (9): PermissionsScreenGrantedTest, PermissionsScreenTest, NotificationAccessRepository, SettingsRepository, UsageAccessRepository, StateFlow, ViewModel, PermissionsViewModel (+1 more)

### Community 11 - "AppDrawerSettingsViewModel"
Cohesion: 0.18
Nodes (7): DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, AppDrawerSettingsViewModel, StateFlow, ViewModel

### Community 12 - "AppWidgetRepository"
Cohesion: 0.08
Nodes (13): AppWidgetRepository, AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender (+5 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.15
Nodes (6): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest, DeleteWidgetUseCaseTest

### Community 14 - "ClockColorOption"
Cohesion: 0.07
Nodes (16): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, ClockFontOption, LAUNCHER_DEFAULT, MANROPE (+8 more)

### Community 15 - ".setContent"
Cohesion: 0.09
Nodes (17): HubWidgetPickerScreenTest, BackupRestoreScreenTest, Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost (+9 more)

### Community 16 - "Settings Screen (main)"
Cohesion: 0.08
Nodes (33): 24-hour Time Toggle, App Drawer Presentation Setting (List / Grid), Appearance: Accent Color Setting, Appearance: Icons Setting (System default / Monochrome), Backup & Restore Setting (Export settings as a file, widgets need re-adding on import), Calendar Events Toggle (2 calendars, all-day hidden), Change Wallpaper Option (Opens the system picker), Clock Style Variant: Date-forward (+25 more)

### Community 17 - ".setContent"
Cohesion: 0.09
Nodes (7): ProfileCarouselScreenTest, AppModule, Context, UsageStatsRepository, UsageStatsRepositoryTest, AppOpsManager, UsageStatsManager

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.12
Nodes (11): WidgetPlacementEntity, CompactWidgetsUseCase, DeleteWidgetUseCase, HubDomainState, HubWidgetState, Flow, ObserveHubStateUseCase, ResolveWidgetDropUseCase (+3 more)

### Community 19 - "FavoriteAppRepository"
Cohesion: 0.13
Nodes (6): FavoriteAppRepository, Flow, FavoriteAppEntity, FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 20 - "LauncherSettings"
Cohesion: 0.17
Nodes (5): LauncherSettings, HomeUiState, ExportBackupUseCaseTest, HomeUiStateTest, ProfileCarouselUiStateTest

### Community 21 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 22 - "AppShortcutRepository"
Cohesion: 0.19
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "EnsureActiveProfileUseCase"
Cohesion: 0.24
Nodes (3): EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao

### Community 24 - "ExportBackupUseCase.kt"
Cohesion: 0.23
Nodes (7): BackupRepository, Uri, BackupBundle, BackupSettings, Uri, toBackupSettings(), BackupRepositoryTest

### Community 25 - "NotificationAccessExplanationViewModel.kt"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, NotificationAccessExplanationViewModel

### Community 26 - "KeyboardDismissalTest.kt"
Cohesion: 0.14
Nodes (7): KeyboardDismissalTest, ContactPermissionRepository, NotificationShadeRepository, CleanUpUninstalledAppsUseCase, T, RankBySearchRelevanceUseCase, NotificationShadeRepositoryTest

### Community 29 - "SettingsScreenTest.kt"
Cohesion: 0.14
Nodes (8): DefaultLauncherRepository, Flow, ObserveSettingsScreenStateUseCase, SettingsScreenState, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

### Community 30 - "HubWidgetPickerViewModel"
Cohesion: 0.09
Nodes (23): WidgetProviderOption, HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile() (+15 more)

### Community 31 - "AppDrawerScreen.kt"
Cohesion: 0.20
Nodes (25): androidx, AppDrawerScreenGridPreview(), ContactRow(), ContactsAccessStrip(), DrawerAppRow(), drawerDashedBorder(), DrawerGridContent(), DrawerGridTile() (+17 more)

### Community 32 - "DockSettingsScreen.kt"
Cohesion: 0.38
Nodes (10): rememberDragReorderState(), DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview() (+2 more)

### Community 33 - "AppInfo"
Cohesion: 0.20
Nodes (5): AppInfo, GetInstalledAppsUseCase, Flow, GetInstalledAppsUseCaseTest, LauncherViewModelTest

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.09
Nodes (15): Callback, Callback, flattenIcon(), Bitmap, Flow, LumenNotificationListenerService, NotificationInfo, StateFlow (+7 more)

### Community 36 - "ProfileCarouselScreen.kt"
Cohesion: 0.26
Nodes (18): Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar(), AddProfilePage(), AddProfileRow(), Modifier (+10 more)

### Community 37 - "ClockTemplateId"
Cohesion: 0.11
Nodes (17): ClockTemplateId, BOLD_COLON, BRACKET_MINIMAL, DATE_FORWARD, FLUID_STACK, ITALIC_ACCENT, LIGHT_STACK, ROBOTO_FLEX_NARROW (+9 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.23
Nodes (21): InheritOverrideCard(), Modifier, RenameDialog(), CardDivider(), Modifier, SettingsCard(), AppsSection(), ClickableRow() (+13 more)

### Community 39 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 40 - "BackupRestoreViewModel"
Cohesion: 0.06
Nodes (27): ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier, MessageBanner() (+19 more)

### Community 41 - "DefaultFavoriteAppRepository"
Cohesion: 0.17
Nodes (3): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity

### Community 42 - "Handoff: Minimal Android Launcher (README)"
Cohesion: 0.18
Nodes (18): Launcher Dark.dc.html (dark theme design canvas), Launcher.dc.html (light theme design canvas), F4 Profiles, F5 Launcher Hub (widgets), F9 Backgrounds (system wallpaper), Handoff: Minimal Android Launcher (README), Screen 2a — Long-press sheet, Screen 3a — Profile carousel (+10 more)

### Community 43 - "ObserveProfilePreviewsUseCase.kt"
Cohesion: 0.23
Nodes (5): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, Fixture, ObserveProfilePreviewsUseCaseTest

### Community 44 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 45 - "IconRenderMode"
Cohesion: 0.25
Nodes (4): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT

### Community 46 - "combine"
Cohesion: 0.22
Nodes (9): combine(), Flow, T1, T2, T3, T4, T5, T6 (+1 more)

### Community 47 - "LumenNavHost"
Cohesion: 0.22
Nodes (11): Modifier, LumenDestinations, LumenNavHost(), popBackStackSafely(), displayLabel(), Modifier, NotificationSettingsContent(), NotificationSettingsScreen() (+3 more)

### Community 48 - "FavoritesPickerScreen.kt"
Cohesion: 0.24
Nodes (12): FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader(), FavoritesPickerUiState (+4 more)

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
Cohesion: 0.45
Nodes (10): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen(), AppearanceSettingsScreenPreview() (+2 more)

### Community 55 - "CalendarSettingsScreen.kt"
Cohesion: 0.44
Nodes (8): CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen(), CalendarSettingsScreenPreview(), Modifier, PermissionDeniedStrip(), CalendarSettingsUiState

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 57 - "Launcher Settings screen, pre-reorg (panel 2c, superseded by 3c)"
Cohesion: 0.18
Nodes (13): App drawer settings section (list/grid presentation, grid size 4x4-5x6), Appearance settings section (icon pack, accent color, drawer opacity), "Change wallpaper" sheet row (opens system picker), Clock style as its own full-width screen (panel 2d), Delete "Focus" profile confirmation dialog, Dock settings section (icons/text, drag to reorder, 3-5 apps), Long-press sheet (panel 2a): Switch profile / Edit profile / Launcher settings / Change wallpaper, Profiles page-grid, 2 of 3 (panel 2b, superseded by 3a) (+5 more)

### Community 58 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.26
Nodes (3): DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 60 - "ImportBackupUseCase.kt"
Cohesion: 0.27
Nodes (5): ImportBackupResult, InvalidFile, Uri, Success, UnsupportedVersion

### Community 61 - "CalendarEvent"
Cohesion: 0.30
Nodes (5): ClockBlockTest, CalendarEvent, ClockBlock(), ClockBlockPreview(), Modifier

### Community 63 - "LabeledDropdownRow"
Cohesion: 0.30
Nodes (12): Modifier, T, LabeledDropdownRow(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+4 more)

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
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 73 - "ProfileCarouselViewModel.kt"
Cohesion: 0.15
Nodes (4): StateFlow, ViewModel, ProfileCarouselUiState, ProfileCarouselViewModel

### Community 75 - "DrawerPresentation"
Cohesion: 0.18
Nodes (6): DrawerPresentation, GRID, LIST, SearchBarPosition, BOTTOM, TOP

### Community 76 - "Settings Screen (dark)"
Cohesion: 0.24
Nodes (13): App Drawer Settings (presentation, grid size, icons/labels/search), Appearance Settings (icon style, accent color, drawer opacity), Calendar Selection Setting (Select calendars, show calendar events), Clock Style Per-Profile Override (Inherit vs Override for Focus), Clock Style Settings (global default), Dock Settings (4 apps, shared across profiles), Favorite Apps Selection (chips, per-profile), Profile Carousel Screen (dark) (+5 more)

### Community 77 - "BackupMapping.kt"
Cohesion: 0.22
Nodes (12): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDefaultFavoriteAppEntity() (+4 more)

### Community 78 - "LauncherFontOption"
Cohesion: 0.14
Nodes (8): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, lumenColorScheme(), ClockFontsTest

### Community 79 - "HomeScreen.kt"
Cohesion: 0.17
Nodes (21): NotificationBadgeStyle, COUNT, DOT, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel() (+13 more)

### Community 80 - "DrawerViewModel"
Cohesion: 0.25
Nodes (4): ContactInfo, DrawerViewModel, StateFlow, ViewModel

### Community 85 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 86 - "Light Turn3 Profiles And Settings (Screenshot)"
Cohesion: 0.35
Nodes (12): Light Turn3 Profiles And Settings (Screenshot), Clock Style Per-Profile Override (3f), Clock Style Settings Page (3e), Launcher Settings Screen (3c), Profile Carousel (3a), Profile Reordering Drag Gesture (3b), Profile Settings / Edit Profile Screen (3d), Shared vs Per-Profile Settings Rationale (+4 more)

### Community 88 - "ClockStyleGalleryScreen.kt"
Cohesion: 0.35
Nodes (9): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier, ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier (+1 more)

### Community 92 - "Light Turn1: Home & Drawer Design Exploration"
Cohesion: 0.35
Nodes (11): 1d: Clock Style - Date-Forward (weekday leads, time recedes, calendar-denied fallback), 1b: Clock Style - Light Stack (200-weight numerals, calendar as quiet list), 1c: Clock Style - Rule & Meridiem (12h, hairline baseline, next event on rule), 1i: Drawer Presentation - 5x6 Grid, No Letter Headers, 9.5px Labels for Five Columns, 1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay, 1e: Favorites Density - Airy, Trailing Dot (18px names, 50px rows), 1f: Favorites Density - Compact, Leading Dot (8 apps fit, dot in icon gutter), 1g: Favorites Density - No Icons, Count-Free Dot, Names Carry Everything (4 apps only) (+3 more)

### Community 99 - "Dark Mode Turn 1: Home & Drawer Screenshot Sheet"
Cohesion: 0.33
Nodes (10): App Drawer: Full Alphabetical Grid, Calendar Access Denied State ("events hidden, Turn on"), Home Screen Clock Widget Variants (compact/expanded), Contact Search Results with Contacts-Permission-Denied Fallback, Favorite/Pinned App Star Marker (dot indicator), Home Screen: Favorites list + Dock (dark), Dark Mode Turn 1: Home & Drawer Screenshot Sheet, App Drawer: Most Used Sort Section (+2 more)

### Community 101 - "HubGrid"
Cohesion: 0.10
Nodes (23): DragReorderState, Modifier, T, HubGrid(), AppWidgetHostView, Context, Modifier, resizedSpan() (+15 more)

### Community 102 - "AppearanceSettingsViewModel"
Cohesion: 0.28
Nodes (3): AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 103 - "DockDisplayMode"
Cohesion: 0.14
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 106 - ".setContent"
Cohesion: 0.18
Nodes (5): FakeNotificationAccessRepository, NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsViewModel

### Community 108 - "GroupAppsByLetterUseCaseTest"
Cohesion: 0.24
Nodes (3): GroupAppsByLetterUseCase, GroupedApps, GroupAppsByLetterUseCaseTest

### Community 111 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 114 - "F1 Home clock widget + calendar integration"
Cohesion: 0.18
Nodes (11): F12 Long-Press Context Menu, F1 Home clock widget + calendar integration, F7 Alphabet Rail, Screen 3e — Clock style page (default), Screen 4i — App long-press context menu, Screen 4l — Calendars picker, Screen — Calendar settings (new), Known gap: no second clock style exists yet (+3 more)

### Community 118 - "AlphabetRail"
Cohesion: 0.38
Nodes (4): AlphabetRailTest, AlphabetRail(), Modifier, magnifyScale()

### Community 119 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 121 - "SettingsRepositoryTest.kt"
Cohesion: 0.48
Nodes (4): DataStoreModule, Context, DataStore, Preferences

### Community 122 - "DrawerGridSize"
Cohesion: 0.29
Nodes (5): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR

### Community 123 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 131 - "ThemeMode"
Cohesion: 0.29
Nodes (4): ThemeMode, DARK, LIGHT, SYSTEM

### Community 132 - "CalendarEventsBlock.kt"
Cohesion: 0.71
Nodes (6): CalendarEventsBlock(), EventRow(), Color, FontFamily, FontWeight, Modifier

### Community 133 - "rememberTickingNow"
Cohesion: 0.52
Nodes (5): Context, Intent, rememberTickingNow(), BroadcastReceiver, State

### Community 134 - "ContactConnectionType"
Cohesion: 0.33
Nodes (6): ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER, WHATSAPP

## Ambiguous Edges - Review These
- `Handoff: Minimal Android Launcher (README)` → `Manrope Font License (SIL OFL 1.1)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/README.md · relation: references
- `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` → `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/screenshots/light-turn1-home-and-drawer.png · relation: conceptually_related_to

## Knowledge Gaps
- **142 isolated node(s):** `HubFull`, `AddFailed`, `LaunchBindPermission`, `LaunchConfigure`, `WidgetAdded` (+137 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 364 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **43 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Handoff: Minimal Android Launcher (README)` and `Manrope Font License (SIL OFL 1.1)`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` and `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `AppRepository`, `ThemeMode`, `FontWeightOption`, `Row`, `LumenDatabase`, `ContactConnection`, `CalendarPermissionRepository`, `SettingsRepository`, `.setContent`, `.setContent`, `WidgetPlacementEntity`, `AppShortcutRepository`, `KeyboardDismissalTest.kt`, `.setContent`, `SettingsScreenTest.kt`, `HubWidgetPickerViewModel`, `AppDrawerScreen.kt`, `DockSettingsScreen.kt`, `ProfileCarouselScreen.kt`, `ProfileSettingsScreen.kt`, `StickyHeaderLayout`, `BackupRestoreViewModel`, `ObserveProfilePreviewsUseCase.kt`, `.setContent`, `IconRenderMode`, `LumenNavHost`, `FavoritesPickerScreen.kt`, `.setContent`, `LauncherViewModel`, `AppearanceSettingsScreen.kt`, `CalendarSettingsScreen.kt`, `DockAppPickerScreen.kt`, `HomeScreen`, `CalendarEvent`, `LabeledDropdownRow`, `SettingsScreen.kt`, `AccentSwatch`, `ColorTest`, `BackButton`, `HomeAppsListSettingsScreen.kt`, `LauncherFontOption`, `HomeScreen.kt`, `DrawerViewModel`, `.setContent`, `ClockStyleGalleryScreen.kt`, `.setContent`, `HubGrid`, `DockDisplayMode`, `.setContent`, `.setContent`, `.setContent`, `AppContextMenuTest`, `.setContent`, `.setContent`, `AlphabetRail`, `Type.kt`?**
  _High betweenness centrality (0.174) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `LumenLauncherTheme`, `AppRepository`, `HomeDrawerRoute`, `FontWeightOption`, `SettingsRepository`, `.setContent`, `WidgetPlacementEntity`, `FavoriteAppRepository`, `LauncherSettings`, `AppShortcutRepository`, `KeyboardDismissalTest.kt`, `DockAppRepository`, `.setContent`, `SettingsScreenTest.kt`, `DockSettingsScreen.kt`, `NotificationBadgeRepository`, `ProfileCarouselScreen.kt`, `ProfileSettingsScreen.kt`, `DefaultFavoriteAppRepository`, `ObserveProfilePreviewsUseCase.kt`, `LumenNavHost`, `FavoritesPickerScreen.kt`, `LauncherViewModel`, `DockAppPickerScreen.kt`, `FakeDefaultFavoriteAppDao`, `HomeScreen`, `SettingsScreen.kt`, `FakeDockAppDao`, `HomeAppsListSettingsScreen.kt`, `ProfileCarouselViewModel.kt`, `HomeScreen.kt`, `DrawerViewModel`, `Fixture`, `.createViewModel`, `DockDisplayMode`, `.setContent`, `AppContextMenuTest`, `GroupAppsByLetterUseCaseTest`, `.setContent`?**
  _High betweenness centrality (0.107) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `AppRepository`, `ThemeMode`, `FontWeightOption`, `ProfileEntity`, `LumenDatabase`, `CalendarPermissionRepository`, `AppDrawerSettingsViewModel`, `ClockColorOption`, `.setContent`, `.setContent`, `WidgetPlacementEntity`, `LauncherSettings`, `EnsureActiveProfileUseCase`, `ExportBackupUseCase.kt`, `NotificationAccessExplanationViewModel.kt`, `KeyboardDismissalTest.kt`, `.setContent`, `SettingsScreenTest.kt`, `AppInfo`, `ClockTemplateId`, `DefaultFavoriteAppRepository`, `ObserveProfilePreviewsUseCase.kt`, `IconRenderMode`, `.setContent`, `LauncherViewModel`, `ImportBackupUseCase.kt`, `ProfileCarouselViewModel.kt`, `ClockStyleGalleryViewModelTest`, `DrawerPresentation`, `BackupMapping.kt`, `LauncherFontOption`, `HomeScreen.kt`, `DrawerViewModel`, `AppDrawerSettingsViewModelTest`, `.setContent`, `AppearanceSettingsViewModelTest`, `.createViewModel`, `.setContent`, `SettingsRepositoryTest`, `AppearanceSettingsViewModel`, `DockDisplayMode`, `.setContent`, `.setContent`, `DrawerGridSize`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `AppDrawerScreenGridPreview()`) actually correct?**
  _`LumenLauncherTheme()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 7 INFERRED edges - model-reasoned connections that need verification._