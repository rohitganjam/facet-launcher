# Graph Report - lumen-launcher  (2026-09-07)

## Corpus Check
- 272 files · ~398,613 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2501 nodes · 6699 edges · 144 communities (97 shown, 44 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 503 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `660d6667`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ClockFontOption
- support.js
- ContactRepositoryTest
- HomeDrawerRoute
- ListContentMode
- Row
- ProfileRepository
- ProfileDao
- ContactConnectionType
- CalendarRepository
- CalendarPermissionRepository
- DrawerGridSize
- AppWidgetRepository.kt
- WidgetPlacementRepository
- ClockStyleGalleryViewModelTest
- BackupRestoreScreenTest.kt
- Settings Screen (main)
- IconRenderMode
- WidgetPlacementEntity
- FakeFavoriteAppDao
- ProfileEntity
- HubViewModel
- AppShortcutRepository
- SettingsRepositoryTest.kt
- BackupMapping.kt
- UsageAccessExplanationViewModel
- ClockTemplateId
- AppDrawerScreen.kt
- ProfileCarouselUiState
- ProfileCarouselViewModel.kt
- HubWidgetPickerViewModel
- LumenDatabase
- BackupRestoreViewModelTest
- AppInfo
- NotificationBadgeRepository
- .setContent
- AppRepository
- SettingsRepository
- ProfileSettingsScreen.kt
- AppWidgetRepository
- .setContent
- FakeDefaultFavoriteAppDao
- Handoff: Minimal Android Launcher (README)
- HomeScreen.kt
- .setContent
- .setContent
- DockDisplayMode
- FontWeightSlider
- .setContent
- Lumen Launcher Implementation Plan
- SettingsScreenTest.kt
- Hub, Onboarding & Pickers Spec Sheet
- Dark Theme: Hub, Onboarding & Picker Screens
- LauncherActivity.kt
- AppearanceSettingsScreen.kt
- DockAppRepository
- DockAppPickerScreen.kt
- .setContent
- LumenLauncherTheme
- ExportBackupUseCase.kt
- .refresh
- .setContent
- DrawerViewModelTest
- LabeledDropdownRow
- SettingsScreen.kt
- FakeDockAppDao
- ObserveHomeScreenStateUseCase.kt
- F6 App Drawer
- PRD: Minimal Android Launcher
- ColorTest
- LumenNavHost
- rememberDragReorderState
- letterAt
- .setContent
- FavoriteAppDaoTest
- CardDivider
- Settings Screen (dark)
- FontWeightOption
- LauncherFontOption
- ProfileCarouselScreen.kt
- CalendarRepositoryTest
- DefaultFavoriteAppRepository
- PlaceWidgetUseCaseTest
- ResolveWidgetDropUseCaseTest
- AppDrawerSettingsViewModelTest
- Lumen Launcher Engineering Conventions (CLAUDE.md)
- Light Turn3 Profiles And Settings (Screenshot)
- HubWidgetPickerScreen.kt
- combine
- DockAppDao
- AppearanceSettingsViewModelTest
- .createViewModel
- Light Turn1: Home & Drawer Design Exploration
- .setContent
- StickyHeaderLayout
- .setContent
- SettingsRepositoryTest
- CompactWidgetsUseCaseTest
- ResolveWidgetResizeUseCaseTest
- Dark Mode Turn 1: Home & Drawer Screenshot Sheet
- AppearanceSettingsViewModel
- HubWidgetTile
- AccentSwatch
- DockSettingsScreen.kt
- ComponentName
- .setContent
- .setContent
- GroupAppsByLetterUseCaseTest
- FavoriteAppRepository
- ObserveHubStateUseCase
- ContactRepository
- PermissionKind
- HomeDrawerRouteTest.kt
- .setContent
- F1 Home clock widget + calendar integration
- LumenDatabaseMigrationTest
- CalendarSettingsScreen.kt
- BackButton
- AppContextMenuTest
- Type.kt
- .setContent
- DockAppEntity
- HubAddWidgetEvent
- FavoriteAppDao
- gradlew
- LumenApplication.kt
- Noto Sans Font License (SIL OFL 1.1)
- Poppins Font License (SIL OFL 1.1)
- .setContent
- ContactConnection
- HomeAppsListSettingsScreen.kt
- .setContent
- DefaultFavoriteAppDao
- DrawerViewModel
- AppRepository.kt
- Migrations
- AddFailureReason
- androidx
- Modifier
- Offset
- ProfileEntityTest

## God Nodes (most connected - your core abstractions)
1. `LumenLauncherTheme()` - 179 edges
2. `ProfileEntity` - 139 edges
3. `AppInfo` - 133 edges
4. `SettingsRepository` - 120 edges
5. `ProfileRepository` - 92 edges
6. `LauncherSettings` - 87 edges
7. `Row` - 86 edges
8. `WidgetPlacementEntity` - 61 edges
9. `HomeScreen()` - 55 edges
10. `ClockTemplateId` - 53 edges

## Surprising Connections (you probably didn't know these)
- `Dock settings section (icons/text, drag to reorder, 3-5 apps)` --conceptually_related_to--> `DockSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/DockSettingsScreen.kt
- `Delete "Focus" profile confirmation dialog` --conceptually_related_to--> `ProfileSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/profiles/ProfileSettingsScreen.kt
- `Appearance settings section (icon pack, accent color, drawer opacity)` --conceptually_related_to--> `AppearanceSettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/AppearanceSettingsScreen.kt
- `"Change wallpaper" sheet row (opens system picker)` --conceptually_related_to--> `SettingsScreen()`  [INFERRED]
  design_handoff_minimal_launcher/screenshots/light-turn2-sheet-and-settings.png → app/src/main/kotlin/com/lumenlauncher/app/ui/settings/SettingsScreen.kt
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

## Communities (144 total, 44 thin omitted)

### Community 0 - "ClockFontOption"
Cohesion: 0.06
Nodes (17): ClockFontOption, LAUNCHER_DEFAULT, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, ClockAlignment (+9 more)

### Community 1 - "support.js"
Cohesion: 0.06
Nodes (75): boot(), bundledBlob(), cdnScriptFor(), collectProps(), compileAttr(), compileTemplate(), contentKey(), createComponentFactory() (+67 more)

### Community 3 - "HomeDrawerRoute"
Cohesion: 0.12
Nodes (23): androidx, HubContent(), HubScreen(), AppWidgetHostView, Context, Modifier, Axis, HORIZONTAL (+15 more)

### Community 4 - "ListContentMode"
Cohesion: 0.04
Nodes (25): Converters, T, resolveOverride(), AppListVerticalAlignment, BOTTOM, TOP, AppRowPosition, LEFT (+17 more)

### Community 5 - "Row"
Cohesion: 0.13
Nodes (50): dashedBorder(), Color, Dp, Modifier, BoldColonTemplate(), BracketMinimalTemplate(), ClockDisplay(), dateFormatter() (+42 more)

### Community 6 - "ProfileRepository"
Cohesion: 0.07
Nodes (5): ProfileRepository, toCsv(), FakeProfileDao, Flow, ProfileRepositoryTest

### Community 7 - "ProfileDao"
Cohesion: 0.17
Nodes (3): Flow, ProfileDao, ProfileDaoTest

### Community 8 - "ContactConnectionType"
Cohesion: 0.16
Nodes (11): ContactConnectionsSheetTest, ConnectionDetail, ConnectionOption, ContactConnectionType, CALL, EMAIL, MESSAGE, OTHER (+3 more)

### Community 9 - "CalendarRepository"
Cohesion: 0.07
Nodes (11): CalendarSettingsScreenGrantedTest, CalendarSettingsScreenTest, FakeCalendarRepository, CalendarRepository, CalendarInfo, AssignCalendarColorsUseCase, CalendarSettingsViewModel, StateFlow (+3 more)

### Community 10 - "CalendarPermissionRepository"
Cohesion: 0.09
Nodes (12): FakeCalendarPermissionRepository, FakeNotificationAccessRepository, PermissionsScreenGrantedTest, PermissionsScreenTest, CalendarPermissionRepository, ContactPermissionRepository, NotificationAccessRepository, UsageAccessRepository (+4 more)

### Community 11 - "DrawerGridSize"
Cohesion: 0.09
Nodes (14): DrawerGridSize, FIVE_BY_FIVE, FIVE_BY_SIX, FOUR_BY_FIVE, FOUR_BY_FOUR, DrawerPresentation, GRID, LIST (+6 more)

### Community 12 - "AppWidgetRepository.kt"
Cohesion: 0.14
Nodes (10): AppWidgetHostView, AppWidgetProviderInfo, Bitmap, Context, Flow, Intent, IntentSender, toBitmap() (+2 more)

### Community 13 - "WidgetPlacementRepository"
Cohesion: 0.18
Nodes (5): Flow, WidgetPlacementRepository, FakeWidgetPlacementDao, Flow, WidgetPlacementRepositoryTest

### Community 15 - "BackupRestoreScreenTest.kt"
Cohesion: 0.14
Nodes (12): Context, WidgetModule, AppWidgetProviderInfo, IntentSender, SharedFlow, LauncherAppWidgetHost, HubFull, Placed (+4 more)

### Community 16 - "Settings Screen (main)"
Cohesion: 0.06
Nodes (46): 24-hour Time Toggle, App Drawer Presentation Setting (List / Grid), Appearance: Accent Color Setting, Appearance: Icons Setting (System default / Monochrome), Backup & Restore Setting (Export settings as a file, widgets need re-adding on import), Calendar Events Toggle (2 calendars, all-day hidden), Change Wallpaper Option (Opens the system picker), Clock Style Variant: Date-forward (+38 more)

### Community 17 - "IconRenderMode"
Cohesion: 0.12
Nodes (9): IconRenderMode, MONOCHROME_ACCENT, MONOCHROME_BLACK_WHITE, SYSTEM_DEFAULT, ThemeMode, DARK, LIGHT, SYSTEM (+1 more)

### Community 18 - "WidgetPlacementEntity"
Cohesion: 0.15
Nodes (8): WidgetPlacementEntity, CompactWidgetsUseCase, HubDomainState, HubWidgetState, Flow, ResolveWidgetDropUseCase, ResolveWidgetResizeUseCase, HubViewModelTest

### Community 19 - "FakeFavoriteAppDao"
Cohesion: 0.20
Nodes (3): FakeFavoriteAppDao, FavoriteAppRepositoryTest, Flow

### Community 20 - "ProfileEntity"
Cohesion: 0.11
Nodes (10): ProfileEntity, LauncherSettings, HomeUiState, ExportBackupUseCaseTest, Fixture, ObserveHomeScreenStateUseCaseTest, Fixture, ObserveProfilePreviewsUseCaseTest (+2 more)

### Community 21 - "HubViewModel"
Cohesion: 0.17
Nodes (6): HubUiState, HubWidgetUi, HubViewModel, AppWidgetHostView, Context, ViewModel

### Community 22 - "AppShortcutRepository"
Cohesion: 0.25
Nodes (4): AppShortcutRepository, AppShortcut, AppShortcutRepositoryTest, ShortcutInfo

### Community 23 - "SettingsRepositoryTest.kt"
Cohesion: 0.14
Nodes (8): DataStoreModule, Context, EnsureActiveProfileUseCase, EnsureActiveProfileUseCaseTest, FakeProfileDao, Flow, DataStore, Preferences

### Community 24 - "BackupMapping.kt"
Cohesion: 0.19
Nodes (11): BackupAppEntry, BackupProfile, BackupWidgetPlacement, T, toBackupEntry(), toBackupPlacement(), toBackupProfile(), toDockAppEntity() (+3 more)

### Community 25 - "UsageAccessExplanationViewModel"
Cohesion: 0.60
Nodes (3): StateFlow, ViewModel, UsageAccessExplanationViewModel

### Community 26 - "ClockTemplateId"
Cohesion: 0.10
Nodes (17): ClockTemplateId, BOLD_COLON, BRACKET_MINIMAL, DATE_FORWARD, FLUID_STACK, ITALIC_ACCENT, LIGHT_STACK, ROBOTO_FLEX_NARROW (+9 more)

### Community 27 - "AppDrawerScreen.kt"
Cohesion: 0.17
Nodes (23): ContactInfo, DrawerListItemSize, COMPACT, REGULAR, SPACIOUS, GroupAppsByLetterUseCase, GroupedApps, ContactRow() (+15 more)

### Community 29 - "ProfileCarouselViewModel.kt"
Cohesion: 0.18
Nodes (6): Flow, ObserveProfilePreviewsUseCase, ProfilePreviewData, StateFlow, ViewModel, ProfileCarouselViewModel

### Community 30 - "HubWidgetPickerViewModel"
Cohesion: 0.29
Nodes (4): HubWidgetPickerViewModel, SharedFlow, StateFlow, ViewModel

### Community 31 - "LumenDatabase"
Cohesion: 0.13
Nodes (6): DatabaseModule, Context, LumenDatabase, Flow, WidgetPlacementDao, RoomDatabase

### Community 33 - "AppInfo"
Cohesion: 0.10
Nodes (13): AppInfo, CleanUpUninstalledAppsUseCase, GetInstalledAppsUseCase, Flow, AppDrawerScreenGridPreview(), SharedFlow, StateFlow, ViewModel (+5 more)

### Community 34 - "NotificationBadgeRepository"
Cohesion: 0.19
Nodes (7): LumenNotificationListenerService, NotificationInfo, StateFlow, NotificationBadgeRepository, NotificationBadgeRepositoryTest, NotificationListenerService, StatusBarNotification

### Community 36 - "AppRepository"
Cohesion: 0.36
Nodes (6): AppRepository, any(), AppRepositoryTest, eq(), T, LauncherActivityInfo

### Community 37 - "SettingsRepository"
Cohesion: 0.05
Nodes (17): ClockColorOption, ACCENT_PRIMARY, ACCENT_SECONDARY, THEME, THEME_INVERTED, Keys, Flow, SettingsRepository (+9 more)

### Community 38 - "ProfileSettingsScreen.kt"
Cohesion: 0.27
Nodes (17): Modifier, RenameDialog(), AppsSection(), ClickableRow(), DisabledRow(), displayLabel(), FavoritesReorderList(), Composable (+9 more)

### Community 39 - "AppWidgetRepository"
Cohesion: 0.14
Nodes (4): AppWidgetRepository, DeleteWidgetUseCase, AppWidgetRepositoryTest, DeleteWidgetUseCaseTest

### Community 40 - ".setContent"
Cohesion: 0.08
Nodes (27): BackupRestoreScreenTest, ConfirmDialog(), Modifier, BackupRestoreContent(), BackupRestoreHeader(), BackupRestoreScreen(), BackupRestoreScreenPreview(), Modifier (+19 more)

### Community 41 - "FakeDefaultFavoriteAppDao"
Cohesion: 0.26
Nodes (3): DefaultFavoriteAppRepositoryTest, FakeDefaultFavoriteAppDao, Flow

### Community 42 - "Handoff: Minimal Android Launcher (README)"
Cohesion: 0.18
Nodes (18): Launcher Dark.dc.html (dark theme design canvas), Launcher.dc.html (light theme design canvas), F4 Profiles, F5 Launcher Hub (widgets), F9 Backgrounds (system wallpaper), Handoff: Minimal Android Launcher (README), Screen 2a — Long-press sheet, Screen 3a — Profile carousel (+10 more)

### Community 43 - "HomeScreen.kt"
Cohesion: 0.19
Nodes (22): NotificationBadgeStyle, COUNT, DOT, AppContextMenu(), Modifier, AppIcon(), AppIconGlyph(), badgeLabel() (+14 more)

### Community 44 - ".setContent"
Cohesion: 0.33
Nodes (3): HubGestureTest, Offset, T

### Community 46 - "DockDisplayMode"
Cohesion: 0.14
Nodes (8): DockDisplayMode, ICONS, TEXT, DockSettingsUiState, DockSettingsViewModel, StateFlow, ViewModel, DockSettingsViewModelTest

### Community 47 - "FontWeightSlider"
Cohesion: 0.83
Nodes (3): FontWeightSlider(), FontWeightSliderAllStopsContent(), Modifier

### Community 48 - ".setContent"
Cohesion: 0.16
Nodes (13): FavoritesPickerScreenTest, FavoritesPickerContent(), FavoritesPickerHeader(), FavoritesPickerRow(), FavoritesPickerScreen(), FavoritesPickerScreenPreview(), Modifier, PickerSectionHeader() (+5 more)

### Community 49 - "Lumen Launcher Implementation Plan"
Cohesion: 0.19
Nodes (14): F2 Home screen Favorites/Recents/Most Used, F3 Dock, Screen 1a — Home + drawer prototype, Screen 4j — Favorites picker, Screen 4k — Dock picker, Standing convention: every non-root screen gets a back button, Default favorites (global list, mirrors Dock), Lumen Launcher Implementation Plan (+6 more)

### Community 50 - "SettingsScreenTest.kt"
Cohesion: 0.18
Nodes (6): DefaultLauncherRepository, ObserveSettingsScreenStateUseCase, StateFlow, ViewModel, SettingsViewModel, SettingsViewModelTest

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
Cohesion: 0.42
Nodes (11): AccentColorSection(), AccentSwatchCircle(), AccentSwatchGrid(), appearanceDisplayLabel(), AppearancePreviewCard(), AppearanceSettingsContent(), AppearanceSettingsHeader(), AppearanceSettingsScreen() (+3 more)

### Community 56 - "DockAppPickerScreen.kt"
Cohesion: 0.28
Nodes (11): DockAppPickerContent(), DockAppPickerHeader(), DockAppPickerScreen(), DockAppPickerScreenPreview(), DockPickerRow(), Modifier, PickerSectionHeader(), DockAppPickerUiState (+3 more)

### Community 58 - "LumenLauncherTheme"
Cohesion: 0.05
Nodes (28): AlphabetRailTest, AppDrawerScreenTest, ClockBlockTest, HomeScreenTest, AppDrawerSettingsScreenTest, CalendarEvent, AppDrawerScreen(), CalendarEventsBlock() (+20 more)

### Community 59 - "ExportBackupUseCase.kt"
Cohesion: 0.22
Nodes (8): BackupRepository, Uri, BackupBundle, BackupSettings, ExportBackupUseCase, Uri, toBackupSettings(), BackupRepositoryTest

### Community 60 - ".refresh"
Cohesion: 0.21
Nodes (5): Callback, Callback, Flow, Callback, UserHandle

### Community 63 - "LabeledDropdownRow"
Cohesion: 0.15
Nodes (20): Modifier, T, LabeledDropdownRow(), Composable, Modifier, ThemedDropdownMenu(), ThemedDropdownMenuItem(), DrawerSearchBar() (+12 more)

### Community 64 - "SettingsScreen.kt"
Cohesion: 0.32
Nodes (12): appsListSummary(), ClickableRow(), Composable, Modifier, NavigationChevron(), notificationsSummaryLabel(), SectionHeader(), SettingsContent() (+4 more)

### Community 65 - "FakeDockAppDao"
Cohesion: 0.24
Nodes (3): DockAppRepositoryTest, FakeDockAppDao, Flow

### Community 66 - "ObserveHomeScreenStateUseCase.kt"
Cohesion: 0.21
Nodes (4): HomeScreenState, Flow, ObserveHomeScreenStateUseCase, HomeViewModelTest

### Community 67 - "F6 App Drawer"
Cohesion: 0.18
Nodes (12): F13 Notification Badges, F6 App Drawer, Just-in-time permissions principle, Screen 1h — Drawer list layout, Screen 1i — Drawer grid layout, Screen 3c — Launcher settings, Screens 4f-4h — First run flow, Screen 4p — Permission-denied and empty states (+4 more)

### Community 68 - "PRD: Minimal Android Launcher"
Cohesion: 0.17
Nodes (13): F10 App Drawer Overlay, F11 Label Customization (icon customization parked), F14 Backup & Restore, F8 Left-edge Rail Access, Consolidated gesture map, Third-party icon pack support (parked, F11 v2 candidate), PRD: Minimal Android Launcher, Non-functional performance requirements (M15 5G floor) (+5 more)

### Community 70 - "LumenNavHost"
Cohesion: 0.22
Nodes (13): ClockPositionResetRow(), clockStyleGalleryDisplayLabel(), ClockStyleGalleryHeader(), ClockStyleGalleryRoute(), ClockStyleGalleryScreen(), ClockStyleGalleryScreenPreview(), Modifier, ProfileClockStyleGalleryScreen() (+5 more)

### Community 71 - "rememberDragReorderState"
Cohesion: 0.19
Nodes (10): DragReorderState, Modifier, T, rememberDragReorderState(), detectGrabOrResizeGesture(), Dp, Modifier, WidgetResizeHandle() (+2 more)

### Community 72 - "letterAt"
Cohesion: 0.24
Nodes (5): AlphabetRail(), Modifier, letterAt(), magnifyScale(), AlphabetRailMappingTest

### Community 73 - ".setContent"
Cohesion: 0.13
Nodes (3): ProfileCarouselScreenTest, UsageStatsRepository, UsageStatsRepositoryTest

### Community 75 - "CardDivider"
Cohesion: 0.25
Nodes (16): CardDivider(), Modifier, SettingsCard(), appDrawerDisplayLabel(), AppDrawerSettingsContent(), AppDrawerSettingsHeader(), AppDrawerSettingsScreen(), AppDrawerSettingsScreenPreview() (+8 more)

### Community 76 - "Settings Screen (dark)"
Cohesion: 0.24
Nodes (13): App Drawer Settings (presentation, grid size, icons/labels/search), Appearance Settings (icon style, accent color, drawer opacity), Calendar Selection Setting (Select calendars, show calendar events), Clock Style Per-Profile Override (Inherit vs Override for Focus), Clock Style Settings (global default), Dock Settings (4 apps, shared across profiles), Favorite Apps Selection (chips, per-profile), Profile Carousel Screen (dark) (+5 more)

### Community 77 - "FontWeightOption"
Cohesion: 0.13
Nodes (8): FontWeightOption, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN, TypeTest

### Community 78 - "LauncherFontOption"
Cohesion: 0.14
Nodes (12): LauncherFontOption, MANROPE, NOTO_SANS, POPPINS, ROBOTO_FLEX, SYSTEM, FontFamily, resolveFontFamily() (+4 more)

### Community 79 - "ProfileCarouselScreen.kt"
Cohesion: 0.40
Nodes (12): AddProfilePage(), AddProfileRow(), Modifier, LauncherSettingsRow(), previewLabel(), ProfileCarouselContent(), ProfileCarouselScreen(), ProfileCarouselScreenPreview() (+4 more)

### Community 81 - "DefaultFavoriteAppRepository"
Cohesion: 0.18
Nodes (4): DefaultFavoriteAppRepository, Flow, DefaultFavoriteAppEntity, toDefaultFavoriteAppEntity()

### Community 85 - "Lumen Launcher Engineering Conventions (CLAUDE.md)"
Cohesion: 0.21
Nodes (12): Never disable device/emulator animation scales rule, Lumen Launcher Engineering Conventions (CLAUDE.md), Instrumented tests run on emulator only rule, Material 3 real shape-scale governing principle, Theme every Material3 component explicitly (never stock defaults), MVVM layering (composables -> ViewModels -> Repositories -> domain use cases), Stateless composables + state hoisting convention, Strict layering rule (composables never call data/domain directly) (+4 more)

### Community 86 - "Light Turn3 Profiles And Settings (Screenshot)"
Cohesion: 0.35
Nodes (12): Light Turn3 Profiles And Settings (Screenshot), Clock Style Per-Profile Override (3f), Clock Style Settings Page (3e), Launcher Settings Screen (3c), Profile Carousel (3a), Profile Reordering Drag Gesture (3b), Profile Settings / Edit Profile Screen (3d), Shared vs Per-Profile Settings Rationale (+4 more)

### Community 87 - "HubWidgetPickerScreen.kt"
Cohesion: 0.42
Nodes (9): HubWidgetPickerContent(), HubWidgetPickerHeader(), HubWidgetPickerScreen(), HubWidgetPickerScreenPreview(), Modifier, WidgetProviderGroupRow(), WidgetProviderOptionTile(), HubWidgetPickerUiState (+1 more)

### Community 88 - "combine"
Cohesion: 0.16
Nodes (11): combine(), Flow, Flow, SettingsScreenState, T1, T2, T3, T4 (+3 more)

### Community 92 - "Light Turn1: Home & Drawer Design Exploration"
Cohesion: 0.35
Nodes (11): 1d: Clock Style - Date-Forward (weekday leads, time recedes, calendar-denied fallback), 1b: Clock Style - Light Stack (200-weight numerals, calendar as quiet list), 1c: Clock Style - Rule & Meridiem (12h, hairline baseline, next event on rule), 1i: Drawer Presentation - 5x6 Grid, No Letter Headers, 9.5px Labels for Five Columns, 1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay, 1e: Favorites Density - Airy, Trailing Dot (18px names, 50px rows), 1f: Favorites Density - Compact, Leading Dot (8 apps fit, dot in icon gutter), 1g: Favorites Density - No Icons, Count-Free Dot, Names Carry Everything (4 apps only) (+3 more)

### Community 94 - "StickyHeaderLayout"
Cohesion: 0.40
Nodes (9): Modifier, StickyHeaderLayout(), Modifier, PermissionRow(), PermissionsContent(), PermissionsHeader(), PermissionsScreen(), PermissionsScreenPreview() (+1 more)

### Community 95 - ".setContent"
Cohesion: 0.29
Nodes (3): KeyboardDismissalTest, T, RankBySearchRelevanceUseCase

### Community 99 - "Dark Mode Turn 1: Home & Drawer Screenshot Sheet"
Cohesion: 0.33
Nodes (10): App Drawer: Full Alphabetical Grid, Calendar Access Denied State ("events hidden, Turn on"), Home Screen Clock Widget Variants (compact/expanded), Contact Search Results with Contacts-Permission-Denied Fallback, Favorite/Pinned App Star Marker (dot indicator), Home Screen: Favorites list + Dock (dark), Dark Mode Turn 1: Home & Drawer Screenshot Sheet, App Drawer: Most Used Sort Section (+2 more)

### Community 100 - "AppearanceSettingsViewModel"
Cohesion: 0.28
Nodes (3): AppearanceSettingsViewModel, StateFlow, ViewModel

### Community 101 - "HubWidgetTile"
Cohesion: 0.60
Nodes (5): HubWidgetTile(), AppWidgetHostView, Context, Dp, Modifier

### Community 102 - "AccentSwatch"
Cohesion: 0.15
Nodes (13): AccentSwatch, AMBER, BLUE, CYAN, GREEN, INDIGO, ORANGE, PINK (+5 more)

### Community 103 - "DockSettingsScreen.kt"
Cohesion: 0.53
Nodes (8): DockAppsRow(), DockClickableRow(), dockDisplayLabel(), DockSettingsContent(), DockSettingsHeader(), DockSettingsScreen(), DockSettingsScreenPreview(), Modifier

### Community 104 - "ComponentName"
Cohesion: 0.15
Nodes (3): WidgetProviderOption, HubWidgetPickerViewModelTest, ComponentName

### Community 106 - ".setContent"
Cohesion: 0.18
Nodes (5): NotificationSettingsScreenTest, StateFlow, ViewModel, NotificationSettingsUiState, NotificationSettingsViewModel

### Community 108 - "FavoriteAppRepository"
Cohesion: 0.21
Nodes (4): FavoriteAppRepository, Flow, FavoriteAppEntity, toFavoriteAppEntity()

### Community 111 - "PermissionKind"
Cohesion: 0.29
Nodes (6): PermissionKind, CALENDAR, CONTACTS, NOTIFICATION_ACCESS, USAGE_ACCESS, PermissionsUiState

### Community 112 - "HomeDrawerRouteTest.kt"
Cohesion: 0.12
Nodes (10): AppModule, Context, NotificationShadeRepository, HomeViewModel, StateFlow, ViewModel, NotificationShadeRepositoryTest, AppOpsManager (+2 more)

### Community 114 - "F1 Home clock widget + calendar integration"
Cohesion: 0.18
Nodes (11): F12 Long-Press Context Menu, F1 Home clock widget + calendar integration, F7 Alphabet Rail, Screen 3e — Clock style page (default), Screen 4i — App long-press context menu, Screen 4l — Calendars picker, Screen — Calendar settings (new), Known gap: no second clock style exists yet (+3 more)

### Community 116 - "CalendarSettingsScreen.kt"
Cohesion: 0.30
Nodes (12): InheritOverrideCard(), Modifier, RadioDot(), RadioOptionRow(), CalendarPickerRow(), CalendarSettingsContent(), CalendarSettingsHeader(), CalendarSettingsScreen() (+4 more)

### Community 117 - "BackButton"
Cohesion: 0.29
Nodes (10): BackButton(), Modifier, Modifier, NotificationAccessExplanationContent(), NotificationAccessExplanationScreen(), NotificationAccessExplanationScreenPreview(), Modifier, UsageAccessExplanationContent() (+2 more)

### Community 119 - "Type.kt"
Cohesion: 0.47
Nodes (5): FontFamily, FontWeight, LumenType, lumenTypography(), resolve()

### Community 122 - "HubAddWidgetEvent"
Cohesion: 0.40
Nodes (5): AddFailed, HubAddWidgetEvent, LaunchBindPermission, LaunchConfigure, WidgetAdded

### Community 124 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 132 - "ContactConnection"
Cohesion: 0.42
Nodes (9): ContactConnection, ConnectionRow(), ContactConnectionsSheet(), ContactConnectionsSheetPreview(), fallbackIcon(), androidx, Modifier, subtitle() (+1 more)

### Community 133 - "HomeAppsListSettingsScreen.kt"
Cohesion: 0.47
Nodes (9): DefaultFavoritesReorderList(), HomeAppsListClickableRow(), homeAppsListDisplayLabel(), HomeAppsListSettingsContent(), HomeAppsListSettingsHeader(), HomeAppsListSettingsScreen(), HomeAppsListSettingsScreenPreview(), Modifier (+1 more)

### Community 136 - "DrawerViewModel"
Cohesion: 0.28
Nodes (3): DrawerViewModel, StateFlow, ViewModel

### Community 137 - "AppRepository.kt"
Cohesion: 0.60
Nodes (3): flattenIcon(), Bitmap, Drawable

### Community 139 - "AddFailureReason"
Cohesion: 0.67
Nodes (3): AddFailureReason, HUB_FULL, SETUP_CANCELLED

## Ambiguous Edges - Review These
- `Handoff: Minimal Android Launcher (README)` → `Manrope Font License (SIL OFL 1.1)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/README.md · relation: references
- `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` → `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`  [AMBIGUOUS]
  design_handoff_minimal_launcher/screenshots/light-turn1-home-and-drawer.png · relation: conceptually_related_to

## Knowledge Gaps
- **147 isolated node(s):** `VERTICAL`, `HORIZONTAL`, `HubFull`, `AddFailed`, `LaunchBindPermission` (+142 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 377 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **44 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **What is the exact relationship between `Handoff: Minimal Android Launcher (README)` and `Manrope Font License (SIL OFL 1.1)`?**
  _Edge tagged AMBIGUOUS (relation: references) - confidence is low._
- **What is the exact relationship between `1h: Drawer Presentation - List with Letter Headers, Right Rail, 86% Wallpaper Overlay` and `1a: Working Prototype Gestures (drawer drag-up, long-press sheet, carousel drag, rail jump)`?**
  _Edge tagged AMBIGUOUS (relation: conceptually_related_to) - confidence is low._
- **Why does `LumenLauncherTheme()` connect `LumenLauncherTheme` to `.setContent`, `ListContentMode`, `ContactConnection`, `.setContent`, `Row`, `ContactConnectionType`, `CalendarRepository`, `CalendarPermissionRepository`, `HomeAppsListSettingsScreen.kt`, `BackupRestoreScreenTest.kt`, `IconRenderMode`, `WidgetPlacementEntity`, `AppShortcutRepository`, `AppDrawerScreen.kt`, `ProfileCarouselViewModel.kt`, `LumenDatabase`, `AppInfo`, `.setContent`, `ProfileSettingsScreen.kt`, `.setContent`, `HomeScreen.kt`, `.setContent`, `.setContent`, `FontWeightSlider`, `.setContent`, `SettingsScreenTest.kt`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `DockAppPickerScreen.kt`, `.setContent`, `.setContent`, `SettingsScreen.kt`, `ColorTest`, `LumenNavHost`, `rememberDragReorderState`, `.setContent`, `CardDivider`, `FontWeightOption`, `LauncherFontOption`, `ProfileCarouselScreen.kt`, `HubWidgetPickerScreen.kt`, `.setContent`, `StickyHeaderLayout`, `.setContent`, `AccentSwatch`, `DockSettingsScreen.kt`, `.setContent`, `.setContent`, `HomeDrawerRouteTest.kt`, `.setContent`, `CalendarSettingsScreen.kt`, `BackButton`, `AppContextMenuTest`, `Type.kt`, `.setContent`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `AppInfo` connect `AppInfo` to `ListContentMode`, `HomeAppsListSettingsScreen.kt`, `DrawerViewModel`, `AppRepository.kt`, `CalendarPermissionRepository`, `FakeFavoriteAppDao`, `ProfileEntity`, `AppShortcutRepository`, `AppDrawerScreen.kt`, `ProfileCarouselViewModel.kt`, `AppRepository`, `ProfileSettingsScreen.kt`, `FakeDefaultFavoriteAppDao`, `HomeScreen.kt`, `.setContent`, `DockDisplayMode`, `.setContent`, `SettingsScreenTest.kt`, `LauncherActivity.kt`, `AppearanceSettingsScreen.kt`, `DockAppRepository`, `DockAppPickerScreen.kt`, `LumenLauncherTheme`, `.refresh`, `SettingsScreen.kt`, `FakeDockAppDao`, `ObserveHomeScreenStateUseCase.kt`, `LumenNavHost`, `.setContent`, `ProfileCarouselScreen.kt`, `DefaultFavoriteAppRepository`, `combine`, `.createViewModel`, `.setContent`, `DockSettingsScreen.kt`, `GroupAppsByLetterUseCaseTest`, `FavoriteAppRepository`, `HomeDrawerRouteTest.kt`, `AppContextMenuTest`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **Why does `SettingsRepository` connect `SettingsRepository` to `ClockFontOption`, `.setContent`, `ListContentMode`, `.setContent`, `DrawerViewModel`, `CalendarRepository`, `CalendarPermissionRepository`, `DrawerGridSize`, `ClockStyleGalleryViewModelTest`, `BackupRestoreScreenTest.kt`, `IconRenderMode`, `ProfileEntity`, `SettingsRepositoryTest.kt`, `BackupMapping.kt`, `ClockTemplateId`, `ProfileCarouselViewModel.kt`, `LumenDatabase`, `AppInfo`, `.setContent`, `.setContent`, `.setContent`, `DockDisplayMode`, `SettingsScreenTest.kt`, `DockAppRepository`, `.setContent`, `LumenLauncherTheme`, `ExportBackupUseCase.kt`, `.setContent`, `ObserveHomeScreenStateUseCase.kt`, `.setContent`, `FontWeightOption`, `LauncherFontOption`, `AppDrawerSettingsViewModelTest`, `combine`, `AppearanceSettingsViewModelTest`, `.createViewModel`, `.setContent`, `.setContent`, `SettingsRepositoryTest`, `AppearanceSettingsViewModel`, `.setContent`, `HomeDrawerRouteTest.kt`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `LumenLauncherTheme()` (e.g. with `.themed()` and `lumenTypography()`) actually correct?**
  _`LumenLauncherTheme()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `ProfileEntity` (e.g. with `.`deleteByComponent removes only the matching profile's entry`()` and `.`deleting a profile cascades to its favorites`()`) actually correct?**
  _`ProfileEntity` has 9 INFERRED edges - model-reasoned connections that need verification._