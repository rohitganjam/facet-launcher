package com.facetlauncher.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.facetlauncher.app.BuildConfig
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridDensity
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.FontScaleOption
import com.facetlauncher.app.data.model.FontWeightOption
import com.facetlauncher.app.data.model.IconPackFallback
import com.facetlauncher.app.data.model.IconRenderMode
import com.facetlauncher.app.data.model.IconScale
import com.facetlauncher.app.data.model.IconShape
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherFontOption
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PressureUnit
import com.facetlauncher.app.data.model.RainUnit
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.data.model.SystemBarIconStyle
import com.facetlauncher.app.data.model.TemperatureUnit
import com.facetlauncher.app.data.model.ThemeMode
import com.facetlauncher.app.data.model.WallpaperAccentRole
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WindSpeedUnit
import com.facetlauncher.app.data.model.MAX_SAVED_WEATHER_CITIES
import com.facetlauncher.app.data.model.decodeWeatherCities
import com.facetlauncher.app.data.model.encodeWeatherCities
import com.facetlauncher.app.data.model.isSamePlaceAs
import com.facetlauncher.app.data.model.weatherCityOrNull
import com.facetlauncher.app.data.model.whatsNewSeenId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val USE_24_HOUR_TIME = booleanPreferencesKey("use_24_hour_time")
    val DOCK_DISPLAY_MODE = stringPreferencesKey("dock_display_mode")
    val DRAWER_PRESENTATION = stringPreferencesKey("drawer_presentation")
    val DRAWER_GRID_SIZE = stringPreferencesKey("drawer_grid_size")
    val DRAWER_LIST_ITEM_SIZE = stringPreferencesKey("drawer_list_item_size")
    val DRAWER_OPACITY = floatPreferencesKey("drawer_opacity")
    val NOTIFICATION_DOTS_ENABLED = booleanPreferencesKey("notification_dots_enabled")
    val NOTIFICATION_BADGE_STYLE = stringPreferencesKey("notification_badge_style")
    val SHOW_MEDIA_PLAYER_ON_HOME = booleanPreferencesKey("show_media_player_on_home")
    val SHOW_MEDIA_SKIP_BUTTONS = booleanPreferencesKey("show_media_skip_buttons")
    val SHOW_NOTIFICATIONS_IN_APP_MENU = booleanPreferencesKey("show_notifications_in_app_menu")
    val SHOW_DRAWER_ICONS = booleanPreferencesKey("show_drawer_icons")
    val SHOW_DRAWER_LABELS = booleanPreferencesKey("show_drawer_labels")
    val SEARCH_BAR_POSITION = stringPreferencesKey("search_bar_position")
    val DRAWER_FOLDER_DISPLAY_MODE = stringPreferencesKey("drawer_folder_display_mode")
    val RECENTLY_INSTALLED_POSITION = stringPreferencesKey("recently_installed_position")
    val ACTIVE_FACET_ID = longPreferencesKey("active_facet_id")
    val SHOW_ALL_DAY_EVENTS = booleanPreferencesKey("show_all_day_events")
    val SHOW_CALENDAR_INDICATOR = booleanPreferencesKey("show_calendar_indicator")
    val CALENDAR_ITEMS_TO_SHOW = intPreferencesKey("calendar_items_to_show")
    val SELECTED_CALENDAR_IDS = stringSetPreferencesKey("selected_calendar_ids")
    val CALENDAR_COLORS = stringSetPreferencesKey("calendar_colors")
    val SEARCH_CONTACTS_ENABLED = booleanPreferencesKey("search_contacts_enabled")
    val SEARCH_SETTINGS_ENABLED = booleanPreferencesKey("search_settings_enabled")
    val ACCENT_FROM_SYSTEM = booleanPreferencesKey("accent_from_system")
    val CUSTOM_ACCENT_SWATCH = stringPreferencesKey("custom_accent_swatch")
    val WALLPAPER_ACCENT_ROLE = stringPreferencesKey("wallpaper_accent_role")
    val ICON_RENDER_MODE = stringPreferencesKey("icon_render_mode")
    val ICON_SHAPE = stringPreferencesKey("icon_shape")
    val ICON_SCALE_PERCENT = intPreferencesKey("icon_scale_percent")
    val ICON_PACK_PACKAGE = stringPreferencesKey("icon_pack_package")
    val ICON_PACK_FALLBACK = stringPreferencesKey("icon_pack_fallback")
    val LAUNCHER_FONT_OPTION = stringPreferencesKey("launcher_font_option")
    val FONT_SCALE_OPTION = stringPreferencesKey("font_scale_option")
    val APP_LABEL_COLOR_OPTION = stringPreferencesKey("app_label_color_option")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val SYSTEM_BAR_ICON_STYLE = stringPreferencesKey("system_bar_icon_style")
    val CALENDAR_PERMISSION_REQUESTED = booleanPreferencesKey("calendar_permission_requested")
    val CONTACTS_PERMISSION_REQUESTED = booleanPreferencesKey("contacts_permission_requested")
    val BLUETOOTH_PERMISSION_REQUESTED = booleanPreferencesKey("bluetooth_permission_requested")
    val LOCATION_PERMISSION_REQUESTED = booleanPreferencesKey("location_permission_requested")
    val APP_ROW_POSITION = stringPreferencesKey("app_row_position")
    val LANDSCAPE_APP_ROW_POSITION = stringPreferencesKey("landscape_app_row_position")
    val APP_ROW_PRESENTATION = stringPreferencesKey("app_row_presentation")
    val LIST_CONTENT_MODE = stringPreferencesKey("list_content_mode")
    val APPS_TO_SHOW_COUNT = intPreferencesKey("apps_to_show_count")
    val CLOCK_TEMPLATE_ID = stringPreferencesKey("clock_template_id")
    val CLOCK_FONT_OPTION = stringPreferencesKey("clock_font_option")
    val CLOCK_COLOR_OPTION = stringPreferencesKey("clock_color_option")
    val CLOCK_ACCENT_COLOR_OPTION = stringPreferencesKey("clock_accent_color_option")
    val CLOCK_SHOW_MERIDIEM = booleanPreferencesKey("clock_show_meridiem")
    val CLOCK_DATE_STYLE = stringPreferencesKey("clock_date_style")
    val SHOW_NEXT_ALARM = booleanPreferencesKey("show_next_alarm")
    val SHOW_BATTERY_PERCENTAGE = booleanPreferencesKey("show_battery_percentage")
    val SHOW_WEATHER = booleanPreferencesKey("show_weather")
    val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    val WIND_SPEED_UNIT = stringPreferencesKey("wind_speed_unit")
    val RAIN_UNIT = stringPreferencesKey("rain_unit")
    val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
    val WEATHER_LOCATION_MODE = stringPreferencesKey("weather_location_mode")
    val WEATHER_ICON_STYLE = stringPreferencesKey("weather_icon_style")
    val WEATHER_CITY_NAME = stringPreferencesKey("weather_city_name")
    val WEATHER_CITY_LATITUDE = doublePreferencesKey("weather_city_latitude")
    val WEATHER_CITY_LONGITUDE = doublePreferencesKey("weather_city_longitude")
    val WEATHER_CITY_TIMEZONE = stringPreferencesKey("weather_city_timezone")
    val WEATHER_SAVED_CITIES = stringPreferencesKey("weather_saved_cities")
    val HOME_APPS_FONT_WEIGHT = stringPreferencesKey("home_apps_font_weight")
    val CLOCK_ALIGNMENT = stringPreferencesKey("clock_alignment")
    val CLOCK_ZONE_HEIGHT_DP = floatPreferencesKey("clock_zone_height_dp")
    val CLOCK_SCALE = floatPreferencesKey("clock_scale")
    val ALLOW_LANDSCAPE = booleanPreferencesKey("allow_landscape")
    val LANDSCAPE_CLOCK_ALIGNMENT = stringPreferencesKey("landscape_clock_alignment")
    val LANDSCAPE_CLOCK_ZONE_HEIGHT_DP = floatPreferencesKey("landscape_clock_zone_height_dp")
    val LANDSCAPE_CLOCK_SCALE = floatPreferencesKey("landscape_clock_scale")
    val APP_LIST_VERTICAL_ALIGNMENT = stringPreferencesKey("app_list_vertical_alignment")
    val LANDSCAPE_APP_LIST_VERTICAL_ALIGNMENT = stringPreferencesKey("landscape_app_list_vertical_alignment")
    val APP_LIST_LAYOUT = stringPreferencesKey("app_list_layout")
    val APP_LIST_COLUMN_ALIGNMENT = stringPreferencesKey("app_list_column_alignment")
    val LANDSCAPE_APP_LIST_COLUMN_ALIGNMENT = stringPreferencesKey("landscape_app_list_column_alignment")
    val APP_LIST_GRID_COLUMNS = stringPreferencesKey("app_list_grid_columns")
    val APP_LIST_GRID_DISPLAY_MODE = stringPreferencesKey("app_list_grid_display_mode")
    val APP_LIST_SHRINK_TO_FIT = stringPreferencesKey("app_list_shrink_to_fit")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val DEFAULTS_SEEDED = booleanPreferencesKey("defaults_seeded")
    val COACH_MARKS_SEEN = stringSetPreferencesKey("coach_marks_seen")
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
            drawerGridDensity = preferences[Keys.DRAWER_GRID_SIZE]?.let(DrawerGridDensity::fromStored)
                ?: defaults.drawerGridDensity,
            drawerListItemSize = preferences[Keys.DRAWER_LIST_ITEM_SIZE]?.let { runCatching { DrawerListItemSize.valueOf(it) }.getOrNull() }
                ?: defaults.drawerListItemSize,
            drawerOpacity = preferences[Keys.DRAWER_OPACITY] ?: defaults.drawerOpacity,
            notificationDotsEnabled = preferences[Keys.NOTIFICATION_DOTS_ENABLED] ?: defaults.notificationDotsEnabled,
            notificationBadgeStyle = preferences[Keys.NOTIFICATION_BADGE_STYLE]?.let {
                runCatching { NotificationBadgeStyle.valueOf(it) }.getOrNull()
            } ?: defaults.notificationBadgeStyle,
            showMediaPlayerOnHome = preferences[Keys.SHOW_MEDIA_PLAYER_ON_HOME] ?: defaults.showMediaPlayerOnHome,
            showMediaSkipButtons = preferences[Keys.SHOW_MEDIA_SKIP_BUTTONS] ?: defaults.showMediaSkipButtons,
            showNotificationsInAppMenu = preferences[Keys.SHOW_NOTIFICATIONS_IN_APP_MENU] ?: defaults.showNotificationsInAppMenu,
            showDrawerIcons = preferences[Keys.SHOW_DRAWER_ICONS] ?: defaults.showDrawerIcons,
            showDrawerLabels = preferences[Keys.SHOW_DRAWER_LABELS] ?: defaults.showDrawerLabels,
            searchBarPosition = preferences[Keys.SEARCH_BAR_POSITION]?.let {
                runCatching { SearchBarPosition.valueOf(it) }.getOrNull()
            } ?: defaults.searchBarPosition,
            drawerFolderDisplayMode = preferences[Keys.DRAWER_FOLDER_DISPLAY_MODE]?.let {
                runCatching { DrawerFolderDisplayMode.valueOf(it) }.getOrNull()
            } ?: defaults.drawerFolderDisplayMode,
            recentlyInstalledPosition = preferences[Keys.RECENTLY_INSTALLED_POSITION]?.let {
                runCatching { RecentlyInstalledPosition.valueOf(it) }.getOrNull()
            } ?: defaults.recentlyInstalledPosition,
            activeFacetId = preferences[Keys.ACTIVE_FACET_ID] ?: defaults.activeFacetId,
            showAllDayEvents = preferences[Keys.SHOW_ALL_DAY_EVENTS] ?: defaults.showAllDayEvents,
            showCalendarIndicator = preferences[Keys.SHOW_CALENDAR_INDICATOR] ?: defaults.showCalendarIndicator,
            calendarItemsToShow = (preferences[Keys.CALENDAR_ITEMS_TO_SHOW] ?: defaults.calendarItemsToShow)
                .coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS),
            selectedCalendarIds = preferences[Keys.SELECTED_CALENDAR_IDS],
            calendarColors = preferences[Keys.CALENDAR_COLORS].orEmpty().mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }.toMap(),
            searchContactsEnabled = preferences[Keys.SEARCH_CONTACTS_ENABLED] ?: defaults.searchContactsEnabled,
            searchSettingsEnabled = preferences[Keys.SEARCH_SETTINGS_ENABLED] ?: defaults.searchSettingsEnabled,
            accentFromSystem = preferences[Keys.ACCENT_FROM_SYSTEM] ?: defaults.accentFromSystem,
            customAccentSwatch = preferences[Keys.CUSTOM_ACCENT_SWATCH],
            wallpaperAccentRole = preferences[Keys.WALLPAPER_ACCENT_ROLE]?.let { runCatching { WallpaperAccentRole.valueOf(it) }.getOrNull() }
                ?: defaults.wallpaperAccentRole,
            iconRenderMode = preferences[Keys.ICON_RENDER_MODE]?.let { runCatching { IconRenderMode.valueOf(it) }.getOrNull() }
                ?: defaults.iconRenderMode,
            iconShape = preferences[Keys.ICON_SHAPE]?.let { runCatching { IconShape.valueOf(it) }.getOrNull() }
                ?: defaults.iconShape,
            iconScalePercent = (preferences[Keys.ICON_SCALE_PERCENT] ?: defaults.iconScalePercent).coerceIn(IconScale.MIN_PERCENT, IconScale.MAX_PERCENT),
            iconPackPackage = preferences[Keys.ICON_PACK_PACKAGE],
            iconPackFallback = preferences[Keys.ICON_PACK_FALLBACK]?.let { runCatching { IconPackFallback.valueOf(it) }.getOrNull() }
                ?: defaults.iconPackFallback,
            launcherFontOption = preferences[Keys.LAUNCHER_FONT_OPTION]?.let { runCatching { LauncherFontOption.valueOf(it) }.getOrNull() }
                ?: defaults.launcherFontOption,
            fontScaleOption = preferences[Keys.FONT_SCALE_OPTION]?.let { runCatching { FontScaleOption.valueOf(it) }.getOrNull() }
                ?: defaults.fontScaleOption,
            appLabelColorOption = preferences[Keys.APP_LABEL_COLOR_OPTION]?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
                ?: defaults.appLabelColorOption,
            themeMode = preferences[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: defaults.themeMode,
            systemBarIconStyle = preferences[Keys.SYSTEM_BAR_ICON_STYLE]?.let { runCatching { SystemBarIconStyle.valueOf(it) }.getOrNull() }
                ?: defaults.systemBarIconStyle,
            calendarPermissionRequested = preferences[Keys.CALENDAR_PERMISSION_REQUESTED] ?: defaults.calendarPermissionRequested,
            contactsPermissionRequested = preferences[Keys.CONTACTS_PERMISSION_REQUESTED] ?: defaults.contactsPermissionRequested,
            bluetoothPermissionRequested = preferences[Keys.BLUETOOTH_PERMISSION_REQUESTED] ?: defaults.bluetoothPermissionRequested,
            locationPermissionRequested = preferences[Keys.LOCATION_PERMISSION_REQUESTED] ?: defaults.locationPermissionRequested,
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
            clockAccentColorOption = preferences[Keys.CLOCK_ACCENT_COLOR_OPTION]?.let { runCatching { ClockColorOption.valueOf(it) }.getOrNull() }
                ?: defaults.clockAccentColorOption,
            clockShowMeridiem = preferences[Keys.CLOCK_SHOW_MERIDIEM] ?: defaults.clockShowMeridiem,
            clockDateStyle = preferences[Keys.CLOCK_DATE_STYLE]?.let { runCatching { ClockDateStyle.valueOf(it) }.getOrNull() }
                ?: defaults.clockDateStyle,
            showNextAlarm = preferences[Keys.SHOW_NEXT_ALARM] ?: defaults.showNextAlarm,
            showBatteryPercentage = preferences[Keys.SHOW_BATTERY_PERCENTAGE] ?: defaults.showBatteryPercentage,
            showWeather = preferences[Keys.SHOW_WEATHER] ?: defaults.showWeather,
            temperatureUnit = preferences[Keys.TEMPERATURE_UNIT]?.let { runCatching { TemperatureUnit.valueOf(it) }.getOrNull() }
                ?: defaults.temperatureUnit,
            windSpeedUnit = preferences[Keys.WIND_SPEED_UNIT]?.let { runCatching { WindSpeedUnit.valueOf(it) }.getOrNull() }
                ?: defaults.windSpeedUnit,
            rainUnit = preferences[Keys.RAIN_UNIT]?.let { runCatching { RainUnit.valueOf(it) }.getOrNull() }
                ?: defaults.rainUnit,
            pressureUnit = preferences[Keys.PRESSURE_UNIT]?.let { runCatching { PressureUnit.valueOf(it) }.getOrNull() }
                ?: defaults.pressureUnit,
            weatherLocationMode = preferences[Keys.WEATHER_LOCATION_MODE]?.let { runCatching { WeatherLocationMode.valueOf(it) }.getOrNull() }
                ?: defaults.weatherLocationMode,
            weatherIconStyle = preferences[Keys.WEATHER_ICON_STYLE]?.let { runCatching { WeatherIconStyle.valueOf(it) }.getOrNull() }
                ?: defaults.weatherIconStyle,
            weatherCity = preferences.readWeatherCity(),
            savedWeatherCities = preferences.readSavedWeatherCities(),
            homeAppsFontWeight = preferences[Keys.HOME_APPS_FONT_WEIGHT]?.let { runCatching { FontWeightOption.valueOf(it) }.getOrNull() }
                ?: defaults.homeAppsFontWeight,
            clockAlignment = preferences[Keys.CLOCK_ALIGNMENT]?.let { runCatching { ClockAlignment.valueOf(it) }.getOrNull() }
                ?: defaults.clockAlignment,
            clockZoneHeightDp = preferences[Keys.CLOCK_ZONE_HEIGHT_DP],
            clockScale = preferences[Keys.CLOCK_SCALE] ?: defaults.clockScale,
            allowLandscape = preferences[Keys.ALLOW_LANDSCAPE] ?: defaults.allowLandscape,
            landscape = preferences.toLandscapeOverrides(),
            appListVerticalAlignment = preferences[Keys.APP_LIST_VERTICAL_ALIGNMENT]?.let { runCatching { AppListVerticalAlignment.valueOf(it) }.getOrNull() }
                ?: defaults.appListVerticalAlignment,
            appListLayout = preferences[Keys.APP_LIST_LAYOUT]?.let { runCatching { AppListLayout.valueOf(it) }.getOrNull() }
                ?: defaults.appListLayout,
            appListColumnAlignment = preferences[Keys.APP_LIST_COLUMN_ALIGNMENT]?.let { runCatching { AppListColumnAlignment.valueOf(it) }.getOrNull() }
                ?: defaults.appListColumnAlignment,
            appListGridColumns = preferences[Keys.APP_LIST_GRID_COLUMNS]?.let { runCatching { AppListGridColumns.valueOf(it) }.getOrNull() }
                ?: defaults.appListGridColumns,
            appListGridDisplayMode = preferences[Keys.APP_LIST_GRID_DISPLAY_MODE]?.let { runCatching { AppListGridDisplayMode.valueOf(it) }.getOrNull() }
                ?: defaults.appListGridDisplayMode,
            appListShrinkToFit = preferences[Keys.APP_LIST_SHRINK_TO_FIT]?.let { runCatching { AppListShrinkToFit.valueOf(it) }.getOrNull() }
                ?.takeIf { it != AppListShrinkToFit.LAUNCHER_DEFAULT } ?: defaults.appListShrinkToFit,
            onboardingCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted,
            defaultsSeeded = preferences[Keys.DEFAULTS_SEEDED] ?: defaults.defaultsSeeded,
            coachMarksSeen = preferences[Keys.COACH_MARKS_SEEN] ?: defaults.coachMarksSeen,
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

    suspend fun setDrawerGridDensity(density: DrawerGridDensity) {
        dataStore.edit { it[Keys.DRAWER_GRID_SIZE] = density.name }
    }

    suspend fun setDrawerListItemSize(itemSize: DrawerListItemSize) {
        dataStore.edit { it[Keys.DRAWER_LIST_ITEM_SIZE] = itemSize.name }
    }

    suspend fun setDrawerOpacity(opacity: Float) {
        dataStore.edit { it[Keys.DRAWER_OPACITY] = opacity.coerceIn(0f, 1f) }
    }

    suspend fun setDrawerFolderDisplayMode(mode: DrawerFolderDisplayMode) {
        dataStore.edit { it[Keys.DRAWER_FOLDER_DISPLAY_MODE] = mode.name }
    }

    suspend fun setRecentlyInstalledPosition(position: RecentlyInstalledPosition) {
        dataStore.edit { it[Keys.RECENTLY_INSTALLED_POSITION] = position.name }
    }

    suspend fun setNotificationDotsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATION_DOTS_ENABLED] = enabled }
    }

    suspend fun setNotificationBadgeStyle(style: NotificationBadgeStyle) {
        dataStore.edit { it[Keys.NOTIFICATION_BADGE_STYLE] = style.name }
    }

    suspend fun setShowMediaPlayerOnHome(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_MEDIA_PLAYER_ON_HOME] = enabled }
    }

    suspend fun setShowMediaSkipButtons(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_MEDIA_SKIP_BUTTONS] = enabled }
    }

    suspend fun setShowNotificationsInAppMenu(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_NOTIFICATIONS_IN_APP_MENU] = enabled }
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

    suspend fun setShowCalendarIndicator(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_CALENDAR_INDICATOR] = enabled }
    }

    suspend fun setCalendarItemsToShow(count: Int) {
        dataStore.edit { it[Keys.CALENDAR_ITEMS_TO_SHOW] = count.coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS) }
    }

    suspend fun setSearchBarPosition(position: SearchBarPosition) {
        dataStore.edit { it[Keys.SEARCH_BAR_POSITION] = position.name }
    }

    suspend fun setActiveFacetId(facetId: Long) {
        dataStore.edit { it[Keys.ACTIVE_FACET_ID] = facetId }
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

    suspend fun setSearchSettingsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SEARCH_SETTINGS_ENABLED] = enabled }
    }

    suspend fun setAccentFromSystem(enabled: Boolean) {
        dataStore.edit { it[Keys.ACCENT_FROM_SYSTEM] = enabled }
    }

    /** [swatchName] is an `AccentSwatch` enum name — kept as a plain string here, see [LauncherSettings.customAccentSwatch]. */
    suspend fun setCustomAccentSwatch(swatchName: String) {
        dataStore.edit { it[Keys.CUSTOM_ACCENT_SWATCH] = swatchName }
    }

    suspend fun setWallpaperAccentRole(role: WallpaperAccentRole) {
        dataStore.edit { it[Keys.WALLPAPER_ACCENT_ROLE] = role.name }
    }

    suspend fun setIconRenderMode(mode: IconRenderMode) {
        dataStore.edit { it[Keys.ICON_RENDER_MODE] = mode.name }
    }

    suspend fun setIconShape(shape: IconShape) {
        dataStore.edit { it[Keys.ICON_SHAPE] = shape.name }
    }

    suspend fun setIconScalePercent(percent: Int) {
        dataStore.edit { it[Keys.ICON_SCALE_PERCENT] = percent.coerceIn(IconScale.MIN_PERCENT, IconScale.MAX_PERCENT) }
    }

    /** Applies an installed icon pack launcher-wide; null goes back to the built-in [IconRenderMode]. */
    suspend fun setIconPack(packageName: String?) {
        dataStore.edit { if (packageName == null) it.remove(Keys.ICON_PACK_PACKAGE) else it[Keys.ICON_PACK_PACKAGE] = packageName }
    }

    suspend fun setIconPackFallback(fallback: IconPackFallback) {
        dataStore.edit { it[Keys.ICON_PACK_FALLBACK] = fallback.name }
    }

    suspend fun setLauncherFontOption(option: LauncherFontOption) {
        dataStore.edit { it[Keys.LAUNCHER_FONT_OPTION] = option.name }
    }

    suspend fun setFontScaleOption(option: FontScaleOption) {
        dataStore.edit { it[Keys.FONT_SCALE_OPTION] = option.name }
    }

    suspend fun setAppLabelColorOption(option: ClockColorOption) {
        dataStore.edit { it[Keys.APP_LABEL_COLOR_OPTION] = option.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setSystemBarIconStyle(style: SystemBarIconStyle) {
        dataStore.edit { it[Keys.SYSTEM_BAR_ICON_STYLE] = style.name }
    }

    suspend fun setCalendarPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.CALENDAR_PERMISSION_REQUESTED] = requested }
    }

    suspend fun setContactsPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.CONTACTS_PERMISSION_REQUESTED] = requested }
    }

    suspend fun setBluetoothPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.BLUETOOTH_PERMISSION_REQUESTED] = requested }
    }

    suspend fun setLocationPermissionRequested(requested: Boolean) {
        dataStore.edit { it[Keys.LOCATION_PERMISSION_REQUESTED] = requested }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appRowPosition]. */
    suspend fun setAppRowPosition(position: AppRowPosition) {
        dataStore.edit { it[Keys.APP_ROW_POSITION] = position.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appRowPresentation]. */
    suspend fun setAppRowPresentation(presentation: AppRowPresentation) {
        dataStore.edit { it[Keys.APP_ROW_PRESENTATION] = presentation.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.listContentMode]. */
    suspend fun setListContentMode(mode: ListContentMode) {
        dataStore.edit { it[Keys.LIST_CONTENT_MODE] = mode.name }
    }

    /** Coerced into [AppListLimits]' range (reduced from README's original `3d` 4…8 — see chat history). */
    suspend fun setAppsToShowCount(count: Int) {
        dataStore.edit { it[Keys.APPS_TO_SHOW_COUNT] = count.coerceIn(AppListLimits.MIN_APPS_TO_SHOW, AppListLimits.MAX_APPS_TO_SHOW) }
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

    /** See [LauncherSettings.clockAccentColorOption]. */
    suspend fun setClockAccentColorOption(colorOption: ClockColorOption) {
        dataStore.edit { it[Keys.CLOCK_ACCENT_COLOR_OPTION] = colorOption.name }
    }

    suspend fun setClockShowMeridiem(enabled: Boolean) {
        dataStore.edit { it[Keys.CLOCK_SHOW_MERIDIEM] = enabled }
    }

    suspend fun setClockDateStyle(dateStyle: ClockDateStyle) {
        dataStore.edit { it[Keys.CLOCK_DATE_STYLE] = dateStyle.name }
    }

    suspend fun setShowNextAlarm(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_NEXT_ALARM] = enabled }
    }

    suspend fun setShowBatteryPercentage(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_BATTERY_PERCENTAGE] = enabled }
    }

    suspend fun setShowWeather(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_WEATHER] = enabled }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        dataStore.edit { it[Keys.TEMPERATURE_UNIT] = unit.name }
    }

    suspend fun setWindSpeedUnit(unit: WindSpeedUnit) {
        dataStore.edit { it[Keys.WIND_SPEED_UNIT] = unit.name }
    }

    suspend fun setRainUnit(unit: RainUnit) {
        dataStore.edit { it[Keys.RAIN_UNIT] = unit.name }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        dataStore.edit { it[Keys.PRESSURE_UNIT] = unit.name }
    }

    suspend fun setWeatherIconStyle(style: WeatherIconStyle) {
        dataStore.edit { it[Keys.WEATHER_ICON_STYLE] = style.name }
    }

    suspend fun setWeatherLocationMode(mode: WeatherLocationMode) {
        dataStore.edit { it[Keys.WEATHER_LOCATION_MODE] = mode.name }
    }

    /** Stores the chosen city (the four keys together); `null` clears it. The saved list is left as it is. */
    suspend fun setWeatherCity(city: WeatherCity?) {
        dataStore.edit { it.writeWeatherCity(city) }
    }

    /** Replaces the saved cities (a restore); the chosen city is not touched. */
    suspend fun setSavedWeatherCities(cities: List<WeatherCity>) {
        dataStore.edit { it[Keys.WEATHER_SAVED_CITIES] = encodeWeatherCities(cities.take(MAX_SAVED_WEATHER_CITIES)) }
    }

    /**
     * Saves [city] if it is new and makes it the chosen one, in city mode. Returns false, changing nothing, when it is new
     * and [MAX_SAVED_WEATHER_CITIES] are already saved.
     */
    suspend fun chooseWeatherCity(city: WeatherCity): Boolean {
        var accepted = true
        dataStore.edit {
            val saved = it.readSavedWeatherCities()
            val known = saved.firstOrNull { other -> other.isSamePlaceAs(city) }
            if (known == null && saved.size >= MAX_SAVED_WEATHER_CITIES) {
                accepted = false
            } else {
                if (known == null) it[Keys.WEATHER_SAVED_CITIES] = encodeWeatherCities(saved + city)
                it.writeWeatherCity(known ?: city)
                it[Keys.WEATHER_LOCATION_MODE] = WeatherLocationMode.CITY.name
            }
        }
        return accepted
    }

    /** Drops [city] from the saved cities; when it was the chosen one, none is chosen any more. */
    suspend fun removeWeatherCity(city: WeatherCity) {
        dataStore.edit {
            it[Keys.WEATHER_SAVED_CITIES] = encodeWeatherCities(it.readSavedWeatherCities().filterNot { other -> other.isSamePlaceAs(city) })
            if (it.readWeatherCity()?.isSamePlaceAs(city) == true) it.writeWeatherCity(null)
        }
    }

    suspend fun setHomeAppsFontWeight(weight: FontWeightOption) {
        dataStore.edit { it[Keys.HOME_APPS_FONT_WEIGHT] = weight.name }
    }

    suspend fun setAllowLandscape(allow: Boolean) {
        dataStore.edit { it[Keys.ALLOW_LANDSCAPE] = allow }
    }

    suspend fun setClockAlignment(alignment: ClockAlignment) {
        dataStore.edit { it[Keys.CLOCK_ALIGNMENT] = alignment.name }
    }

    suspend fun setClockZoneHeight(heightDp: Float) {
        dataStore.edit { it[Keys.CLOCK_ZONE_HEIGHT_DP] = heightDp }
    }

    /** Settings' "Reset clock height" row — restores [LauncherSettings.clockZoneHeightDp] to `null`, so the clock+calendar block and app list return to their original fixed-top, bottom-anchored layout. */
    suspend fun resetClockZoneHeight() {
        dataStore.edit {
            it.remove(Keys.CLOCK_ZONE_HEIGHT_DP)
            it.remove(Keys.LANDSCAPE_CLOCK_ZONE_HEIGHT_DP)
        }
    }

    suspend fun setClockScale(scale: Float) {
        dataStore.edit {
            it[Keys.CLOCK_SCALE] = scale
        }
    }

    /** Changes the launcher-wide landscape values in one write; a `null` field is removed ("same as portrait"). */
    suspend fun updateLandscape(transform: (LandscapeOverrides) -> LandscapeOverrides) {
        dataStore.edit { it.writeLandscape(transform(it.toLandscapeOverrides())) }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appListVerticalAlignment]. */
    suspend fun setAppListVerticalAlignment(alignment: AppListVerticalAlignment) {
        dataStore.edit { it[Keys.APP_LIST_VERTICAL_ALIGNMENT] = alignment.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appListLayout]. */
    suspend fun setAppListLayout(layout: AppListLayout) {
        dataStore.edit { it[Keys.APP_LIST_LAYOUT] = layout.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appListColumnAlignment]. */
    suspend fun setAppListColumnAlignment(alignment: AppListColumnAlignment) {
        dataStore.edit { it[Keys.APP_LIST_COLUMN_ALIGNMENT] = alignment.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appListGridColumns]. */
    suspend fun setAppListGridColumns(columns: AppListGridColumns) {
        dataStore.edit { it[Keys.APP_LIST_GRID_COLUMNS] = columns.name }
    }

    /** The default every facet inherits unless it sets its own override — see [LauncherSettings.appListGridDisplayMode]. */
    suspend fun setAppListGridDisplayMode(mode: AppListGridDisplayMode) {
        dataStore.edit { it[Keys.APP_LIST_GRID_DISPLAY_MODE] = mode.name }
    }

    /** The default every facet inherits unless it sets its own — see [LauncherSettings.appListShrinkToFit]. */
    suspend fun setAppListShrinkToFit(shrinkToFit: AppListShrinkToFit) {
        dataStore.edit { it[Keys.APP_LIST_SHRINK_TO_FIT] = shrinkToFit.name }
    }

    /** Completing onboarding on a build also marks that version's What's new as seen: a fresh install has nothing new to announce. */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit {
            it[Keys.ONBOARDING_COMPLETED] = completed
            if (completed) it[Keys.COACH_MARKS_SEEN] = (it[Keys.COACH_MARKS_SEEN] ?: emptySet()) + whatsNewSeenId(BuildConfig.VERSION_NAME)
        }
    }

    suspend fun setDefaultsSeeded(seeded: Boolean) {
        dataStore.edit { it[Keys.DEFAULTS_SEEDED] = seeded }
    }

    /** Takes [id] back out of the dismissed-coach-marks set, so that prompt can show again. Used by the debug build's Settings tools. */
    suspend fun clearCoachMarkSeen(id: String) {
        dataStore.edit { it[Keys.COACH_MARKS_SEEN] = (it[Keys.COACH_MARKS_SEEN] ?: emptySet()) - id }
    }

    /** Additive — adds [id] to the dismissed-coach-marks set without disturbing any other id already in it. */
    suspend fun markCoachMarkSeen(id: String) {
        dataStore.edit { it[Keys.COACH_MARKS_SEEN] = (it[Keys.COACH_MARKS_SEEN] ?: emptySet()) + id }
    }
}

/** The chosen city, or `null` unless all four of its keys are present. */
private fun Preferences.readWeatherCity(): WeatherCity? =
    weatherCityOrNull(this[Keys.WEATHER_CITY_NAME], this[Keys.WEATHER_CITY_LATITUDE], this[Keys.WEATHER_CITY_LONGITUDE], this[Keys.WEATHER_CITY_TIMEZONE])

/** The saved cities, with the chosen city first when an older install stored it without a list. */
private fun Preferences.readSavedWeatherCities(): List<WeatherCity> {
    val saved = decodeWeatherCities(this[Keys.WEATHER_SAVED_CITIES])
    val chosen = readWeatherCity()
    return if (chosen != null && saved.none { it.isSamePlaceAs(chosen) }) listOf(chosen) + saved else saved
}

private fun MutablePreferences.writeWeatherCity(city: WeatherCity?) {
    if (city == null) {
        remove(Keys.WEATHER_CITY_NAME)
        remove(Keys.WEATHER_CITY_LATITUDE)
        remove(Keys.WEATHER_CITY_LONGITUDE)
        remove(Keys.WEATHER_CITY_TIMEZONE)
    } else {
        this[Keys.WEATHER_CITY_NAME] = city.name
        this[Keys.WEATHER_CITY_LATITUDE] = city.latitude
        this[Keys.WEATHER_CITY_LONGITUDE] = city.longitude
        this[Keys.WEATHER_CITY_TIMEZONE] = city.timezone
    }
}

private fun Preferences.toLandscapeOverrides() = LandscapeOverrides(
    clockAlignment = this[Keys.LANDSCAPE_CLOCK_ALIGNMENT]?.let { runCatching { ClockAlignment.valueOf(it) }.getOrNull() },
    clockZoneHeightDp = this[Keys.LANDSCAPE_CLOCK_ZONE_HEIGHT_DP],
    clockScale = this[Keys.LANDSCAPE_CLOCK_SCALE],
    appRowPosition = this[Keys.LANDSCAPE_APP_ROW_POSITION]?.let { runCatching { AppRowPosition.valueOf(it) }.getOrNull() },
    appListVerticalAlignment = this[Keys.LANDSCAPE_APP_LIST_VERTICAL_ALIGNMENT]?.let { runCatching { AppListVerticalAlignment.valueOf(it) }.getOrNull() },
    appListColumnAlignment = this[Keys.LANDSCAPE_APP_LIST_COLUMN_ALIGNMENT]?.let { runCatching { AppListColumnAlignment.valueOf(it) }.getOrNull() },
)

/** The widget size is facet-only, so it isn't stored here. */
private fun MutablePreferences.writeLandscape(overrides: LandscapeOverrides) {
    setOrRemove(Keys.LANDSCAPE_CLOCK_ALIGNMENT, overrides.clockAlignment?.name)
    setOrRemove(Keys.LANDSCAPE_CLOCK_ZONE_HEIGHT_DP, overrides.clockZoneHeightDp)
    setOrRemove(Keys.LANDSCAPE_CLOCK_SCALE, overrides.clockScale)
    setOrRemove(Keys.LANDSCAPE_APP_ROW_POSITION, overrides.appRowPosition?.name)
    setOrRemove(Keys.LANDSCAPE_APP_LIST_VERTICAL_ALIGNMENT, overrides.appListVerticalAlignment?.name)
    setOrRemove(Keys.LANDSCAPE_APP_LIST_COLUMN_ALIGNMENT, overrides.appListColumnAlignment?.name)
}

private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
    if (value == null) remove(key) else this[key] = value
}
