package com.lumenlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.DrawerGridSize
import com.lumenlauncher.app.data.model.DrawerListItemSize
import com.lumenlauncher.app.data.model.DrawerPresentation
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.IconRenderMode
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.LauncherSettings
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.data.model.SearchBarPosition
import com.lumenlauncher.app.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object Keys {
    val USE_24_HOUR_TIME = booleanPreferencesKey("use_24_hour_time")
    val DOCK_DISPLAY_MODE = stringPreferencesKey("dock_display_mode")
    val DRAWER_PRESENTATION = stringPreferencesKey("drawer_presentation")
    val DRAWER_GRID_SIZE = stringPreferencesKey("drawer_grid_size")
    val DRAWER_LIST_ITEM_SIZE = stringPreferencesKey("drawer_list_item_size")
    val DRAWER_OPACITY = floatPreferencesKey("drawer_opacity")
    val NOTIFICATION_DOTS_ENABLED = booleanPreferencesKey("notification_dots_enabled")
    val NOTIFICATION_BADGE_STYLE = stringPreferencesKey("notification_badge_style")
    val SHOW_DRAWER_ICONS = booleanPreferencesKey("show_drawer_icons")
    val SHOW_DRAWER_LABELS = booleanPreferencesKey("show_drawer_labels")
    val SEARCH_BAR_POSITION = stringPreferencesKey("search_bar_position")
    val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
    val SHOW_ALL_DAY_EVENTS = booleanPreferencesKey("show_all_day_events")
    val SELECTED_CALENDAR_IDS = stringSetPreferencesKey("selected_calendar_ids")
    val CALENDAR_COLORS = stringSetPreferencesKey("calendar_colors")
    val SEARCH_CONTACTS_ENABLED = booleanPreferencesKey("search_contacts_enabled")
    val ACCENT_FROM_SYSTEM = booleanPreferencesKey("accent_from_system")
    val CUSTOM_ACCENT_SWATCH = stringPreferencesKey("custom_accent_swatch")
    val ICON_RENDER_MODE = stringPreferencesKey("icon_render_mode")
    val LAUNCHER_FONT_OPTION = stringPreferencesKey("launcher_font_option")
    val APP_LABEL_COLOR_OPTION = stringPreferencesKey("app_label_color_option")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val CALENDAR_PERMISSION_REQUESTED = booleanPreferencesKey("calendar_permission_requested")
    val CONTACTS_PERMISSION_REQUESTED = booleanPreferencesKey("contacts_permission_requested")
    val APP_ROW_POSITION = stringPreferencesKey("app_row_position")
    val APP_ROW_PRESENTATION = stringPreferencesKey("app_row_presentation")
    val LIST_CONTENT_MODE = stringPreferencesKey("list_content_mode")
    val APPS_TO_SHOW_COUNT = intPreferencesKey("apps_to_show_count")
    val CLOCK_TEMPLATE_ID = stringPreferencesKey("clock_template_id")
    val CLOCK_FONT_OPTION = stringPreferencesKey("clock_font_option")
    val CLOCK_COLOR_OPTION = stringPreferencesKey("clock_color_option")
    val CLOCK_SHOW_MERIDIEM = booleanPreferencesKey("clock_show_meridiem")
    val CALENDAR_FONT_OPTION = stringPreferencesKey("calendar_font_option")
    val CALENDAR_COLOR_OPTION = stringPreferencesKey("calendar_color_option")
    val CALENDAR_FONT_WEIGHT = stringPreferencesKey("calendar_font_weight")
    val HOME_APPS_FONT_WEIGHT = stringPreferencesKey("home_apps_font_weight")
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val settings: Flow<LauncherSettings> = dataStore.data.map { preferences ->
        val defaults = LauncherSettings()
        LauncherSettings(
            use24HourTime = preferences[Keys.USE_24_HOUR_TIME] ?: defaults.use24HourTime,
            dockDisplayMode = preferences[Keys.DOCK_DISPLAY_MODE]?.let { runCatching { DockDisplayMode.valueOf(it) }.getOrNull() }
                ?: defaults.dockDisplayMode,
            drawerPresentation = preferences[Keys.DRAWER_PRESENTATION]?.let { runCatching { DrawerPresentation.valueOf(it) }.getOrNull() }
                ?: defaults.drawerPresentation,
            drawerGridSize = preferences[Keys.DRAWER_GRID_SIZE]?.let { runCatching { DrawerGridSize.valueOf(it) }.getOrNull() }
                ?: defaults.drawerGridSize,
            drawerListItemSize = preferences[Keys.DRAWER_LIST_ITEM_SIZE]?.let { runCatching { DrawerListItemSize.valueOf(it) }.getOrNull() }
                ?: defaults.drawerListItemSize,
            drawerOpacity = preferences[Keys.DRAWER_OPACITY] ?: defaults.drawerOpacity,
            notificationDotsEnabled = preferences[Keys.NOTIFICATION_DOTS_ENABLED] ?: defaults.notificationDotsEnabled,
            notificationBadgeStyle = preferences[Keys.NOTIFICATION_BADGE_STYLE]?.let {
                runCatching { NotificationBadgeStyle.valueOf(it) }.getOrNull()
            } ?: defaults.notificationBadgeStyle,
            showDrawerIcons = preferences[Keys.SHOW_DRAWER_ICONS] ?: defaults.showDrawerIcons,
            showDrawerLabels = preferences[Keys.SHOW_DRAWER_LABELS] ?: defaults.showDrawerLabels,
            searchBarPosition = preferences[Keys.SEARCH_BAR_POSITION]?.let {
                runCatching { SearchBarPosition.valueOf(it) }.getOrNull()
            } ?: defaults.searchBarPosition,
            activeProfileId = preferences[Keys.ACTIVE_PROFILE_ID] ?: defaults.activeProfileId,
            showAllDayEvents = preferences[Keys.SHOW_ALL_DAY_EVENTS] ?: defaults.showAllDayEvents,
            selectedCalendarIds = preferences[Keys.SELECTED_CALENDAR_IDS],
            calendarColors = preferences[Keys.CALENDAR_COLORS].orEmpty().mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap(),
            searchContactsEnabled = preferences[Keys.SEARCH_CONTACTS_ENABLED] ?: defaults.searchContactsEnabled,
            accentFromSystem = preferences[Keys.ACCENT_FROM_SYSTEM] ?: defaults.accentFromSystem,
            customAccentSwatch = preferences[Keys.CUSTOM_ACCENT_SWATCH],
            iconRenderMode = preferences[Keys.ICON_RENDER_MODE]?.let { runCatching { IconRenderMode.valueOf(it) }.getOrNull() }
                ?: defaults.iconRenderMode,
            launcherFontOption = preferences[Keys.LAUNCHER_FONT_OPTION]?.let { runCatching { LauncherFontOption.valueOf(it) }.getOrNull() }
                ?: defaults.launcherFontOption,
            appLabelColorOption = preferences[Keys.APP_LABEL_COLOR_OPTION]?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
                ?: defaults.appLabelColorOption,
            themeMode = preferences[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: defaults.themeMode,
            calendarPermissionRequested = preferences[Keys.CALENDAR_PERMISSION_REQUESTED] ?: defaults.calendarPermissionRequested,
            contactsPermissionRequested = preferences[Keys.CONTACTS_PERMISSION_REQUESTED] ?: defaults.contactsPermissionRequested,
            appRowPosition = preferences[Keys.APP_ROW_POSITION]?.let { runCatching { AppRowPosition.valueOf(it) }.getOrNull() }
                ?: defaults.appRowPosition,
            appRowPresentation = preferences[Keys.APP_ROW_PRESENTATION]?.let { runCatching { AppRowPresentation.valueOf(it) }.getOrNull() }
                ?: defaults.appRowPresentation,
            listContentMode = preferences[Keys.LIST_CONTENT_MODE]?.let { runCatching { ListContentMode.valueOf(it) }.getOrNull() }
                ?: defaults.listContentMode,
            appsToShowCount = preferences[Keys.APPS_TO_SHOW_COUNT] ?: defaults.appsToShowCount,
            clockTemplateId = preferences[Keys.CLOCK_TEMPLATE_ID]?.let { runCatching { ClockTemplateId.valueOf(it) }.getOrNull() }
                ?: defaults.clockTemplateId,
            clockFontOption = preferences[Keys.CLOCK_FONT_OPTION]?.let { runCatching { ClockFontOption.valueOf(it) }.getOrNull() }
                ?: defaults.clockFontOption,
            clockColorOption = preferences[Keys.CLOCK_COLOR_OPTION]?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
                ?: defaults.clockColorOption,
            clockShowMeridiem = preferences[Keys.CLOCK_SHOW_MERIDIEM] ?: defaults.clockShowMeridiem,
            calendarFontOption = preferences[Keys.CALENDAR_FONT_OPTION]?.let { runCatching { ClockFontOption.valueOf(it) }.getOrNull() }
                ?: defaults.calendarFontOption,
            calendarColorOption = preferences[Keys.CALENDAR_COLOR_OPTION]?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
                ?: defaults.calendarColorOption,
            calendarFontWeight = preferences[Keys.CALENDAR_FONT_WEIGHT]?.let { runCatching { FontWeightOption.valueOf(it) }.getOrNull() }
                ?: defaults.calendarFontWeight,
            homeAppsFontWeight = preferences[Keys.HOME_APPS_FONT_WEIGHT]?.let { runCatching { FontWeightOption.valueOf(it) }.getOrNull() }
                ?: defaults.homeAppsFontWeight,
        )
    }

    suspend fun setUse24HourTime(enabled: Boolean) {
        dataStore.edit { it[Keys.USE_24_HOUR_TIME] = enabled }
    }

    suspend fun setDockDisplayMode(mode: DockDisplayMode) {
        dataStore.edit { it[Keys.DOCK_DISPLAY_MODE] = mode.name }
    }

    suspend fun setDrawerPresentation(presentation: DrawerPresentation) {
        dataStore.edit { it[Keys.DRAWER_PRESENTATION] = presentation.name }
    }

    suspend fun setDrawerGridSize(gridSize: DrawerGridSize) {
        dataStore.edit { it[Keys.DRAWER_GRID_SIZE] = gridSize.name }
    }

    suspend fun setDrawerListItemSize(itemSize: DrawerListItemSize) {
        dataStore.edit { it[Keys.DRAWER_LIST_ITEM_SIZE] = itemSize.name }
    }

    suspend fun setDrawerOpacity(opacity: Float) {
        dataStore.edit { it[Keys.DRAWER_OPACITY] = opacity.coerceIn(0f, 1f) }
    }

    suspend fun setNotificationDotsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATION_DOTS_ENABLED] = enabled }
    }

    suspend fun setNotificationBadgeStyle(style: NotificationBadgeStyle) {
        dataStore.edit { it[Keys.NOTIFICATION_BADGE_STYLE] = style.name }
    }

    suspend fun setShowDrawerIcons(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_DRAWER_ICONS] = enabled }
    }

    suspend fun setShowDrawerLabels(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_DRAWER_LABELS] = enabled }
    }

    suspend fun setShowAllDayEvents(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_ALL_DAY_EVENTS] = enabled }
    }

    suspend fun setSearchBarPosition(position: SearchBarPosition) {
        dataStore.edit { it[Keys.SEARCH_BAR_POSITION] = position.name }
    }

    suspend fun setActiveProfileId(profileId: Long) {
        dataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = profileId }
    }

    suspend fun setSelectedCalendarIds(ids: Set<String>) {
        dataStore.edit { it[Keys.SELECTED_CALENDAR_IDS] = ids }
    }

    /** [colors] maps a calendar id to an `AccentSwatch` enum name — see [LauncherSettings.calendarColors]. */
    suspend fun setCalendarColors(colors: Map<String, String>) {
        dataStore.edit { it[Keys.CALENDAR_COLORS] = colors.map { (id, name) -> "$id:$name" }.toSet() }
    }

    suspend fun setSearchContactsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SEARCH_CONTACTS_ENABLED] = enabled }
    }

    suspend fun setAccentFromSystem(enabled: Boolean) {
        dataStore.edit { it[Keys.ACCENT_FROM_SYSTEM] = enabled }
    }

    /** [swatchName] is an `AccentSwatch` enum name — kept as a plain string here, see [LauncherSettings.customAccentSwatch]. */
    suspend fun setCustomAccentSwatch(swatchName: String) {
        dataStore.edit { it[Keys.CUSTOM_ACCENT_SWATCH] = swatchName }
    }

    suspend fun setIconRenderMode(mode: IconRenderMode) {
        dataStore.edit { it[Keys.ICON_RENDER_MODE] = mode.name }
    }

    suspend fun setLauncherFontOption(option: LauncherFontOption) {
        dataStore.edit { it[Keys.LAUNCHER_FONT_OPTION] = option.name }
    }

    suspend fun setAppLabelColorOption(option: ClockColorOption) {
        dataStore.edit { it[Keys.APP_LABEL_COLOR_OPTION] = option.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setCalendarPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.CALENDAR_PERMISSION_REQUESTED] = requested }
    }

    suspend fun setContactsPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.CONTACTS_PERMISSION_REQUESTED] = requested }
    }

    /** The default every profile inherits unless it sets its own override — see [LauncherSettings.appRowPosition]. */
    suspend fun setAppRowPosition(position: AppRowPosition) {
        dataStore.edit { it[Keys.APP_ROW_POSITION] = position.name }
    }

    /** The default every profile inherits unless it sets its own override — see [LauncherSettings.appRowPresentation]. */
    suspend fun setAppRowPresentation(presentation: AppRowPresentation) {
        dataStore.edit { it[Keys.APP_ROW_PRESENTATION] = presentation.name }
    }

    /** The default every profile inherits unless it sets its own override — see [LauncherSettings.listContentMode]. */
    suspend fun setListContentMode(mode: ListContentMode) {
        dataStore.edit { it[Keys.LIST_CONTENT_MODE] = mode.name }
    }

    /** Coerced into README's `3d` 4…8 range. */
    suspend fun setAppsToShowCount(count: Int) {
        dataStore.edit { it[Keys.APPS_TO_SHOW_COUNT] = count.coerceIn(4, 8) }
    }

    suspend fun setClockTemplateId(templateId: ClockTemplateId) {
        dataStore.edit { it[Keys.CLOCK_TEMPLATE_ID] = templateId.name }
    }

    suspend fun setClockFontOption(fontOption: ClockFontOption) {
        dataStore.edit { it[Keys.CLOCK_FONT_OPTION] = fontOption.name }
    }

    suspend fun setClockColorOption(colorOption: ClockColorOption) {
        dataStore.edit { it[Keys.CLOCK_COLOR_OPTION] = colorOption.name }
    }

    suspend fun setClockShowMeridiem(enabled: Boolean) {
        dataStore.edit { it[Keys.CLOCK_SHOW_MERIDIEM] = enabled }
    }

    suspend fun setCalendarFontOption(fontOption: ClockFontOption) {
        dataStore.edit { it[Keys.CALENDAR_FONT_OPTION] = fontOption.name }
    }

    suspend fun setCalendarColorOption(colorOption: ClockColorOption) {
        dataStore.edit { it[Keys.CALENDAR_COLOR_OPTION] = colorOption.name }
    }

    suspend fun setCalendarFontWeight(weight: FontWeightOption) {
        dataStore.edit { it[Keys.CALENDAR_FONT_WEIGHT] = weight.name }
    }

    suspend fun setHomeAppsFontWeight(weight: FontWeightOption) {
        dataStore.edit { it[Keys.HOME_APPS_FONT_WEIGHT] = weight.name }
    }
}
