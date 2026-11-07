package com.facetlauncher.app.data.local

import android.database.Cursor
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.sqlite.db.SupportSQLiteDatabase
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockFontOption
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.model.decodeWeatherLocation
import com.facetlauncher.app.data.model.encode

/**
 * Migration 42→43: gives every clock page without settings the ones it shows today, before 43→44 drops the facet columns they
 * came from. A facet's own values while it overrides the clock or the calendar, the launcher-wide ones ([launcher]) otherwise.
 * Idempotent: a clock page that already has settings is left alone.
 */
internal fun materializeClockConfigs(db: SupportSQLiteDatabase, launcher: ClockWidgetConfig) {
    val perFacet = db.query("SELECT * FROM facets").use { cursor ->
        buildMap { while (cursor.moveToNext()) put(cursor.long("id"), cursor.legacyClock(launcher)) }
    }
    val pages = db.query("SELECT id, facetId, nativeWidgetConfig FROM clock_stack_items WHERE kind = 'CLOCK'").use { cursor ->
        buildList { while (cursor.moveToNext()) add(Triple(cursor.getLong(0), cursor.getLong(1), cursor.getString(2))) }
    }
    pages.forEach { (id, facetId, stored) ->
        val config = decodeNativeWidgetConfig(stored)
        if (config.clock != null) return@forEach
        val written = config.copy(clock = perFacet[facetId] ?: launcher).encode()
        db.execSQL("UPDATE clock_stack_items SET nativeWidgetConfig = ? WHERE id = ?", arrayOf<Any?>(written, id))
    }
}

private fun Cursor.legacyClock(launcher: ClockWidgetConfig): ClockWidgetConfig {
    val calendar = if (bool("overrideCalendar")) {
        ClockCalendarConfig(
            showIndicator = bool("showCalendarIndicator"),
            itemsToShow = int("calendarItemsToShow"),
            showAllDay = bool("showAllDayEvents"),
            calendarIds = string("selectedCalendarIdsCsv")?.split(",")?.filter { it.isNotEmpty() }?.toSet(),
        )
    } else {
        launcher.calendar
    }
    if (!bool("overrideClock")) return launcher.copy(calendar = calendar)
    return ClockWidgetConfig(
        templateId = string("clockTemplateId").enumOr(launcher.templateId),
        fontOption = string("clockFontOption").enumOr(launcher.fontOption),
        colorOption = string("clockColorOption").enumOr(launcher.colorOption),
        accentColorOption = string("clockAccentColorOption").enumOr(launcher.accentColorOption),
        use24HourTime = bool("use24HourTime"),
        showMeridiem = bool("clockShowMeridiem"),
        dateStyle = string("clockDateStyle").enumOr(launcher.dateStyle),
        showAlarm = bool("clockShowAlarm"),
        showBattery = bool("clockShowBattery"),
        showWeather = bool("clockShowWeather"),
        weatherIconStyle = string("clockWeatherIconStyle").enumOr(launcher.weatherIconStyle),
        weatherLocation = decodeWeatherLocation(string("clockWeatherLocation")),
        calendar = calendar,
    )
}

private fun Cursor.long(column: String) = getLong(getColumnIndexOrThrow(column))
private fun Cursor.int(column: String) = getInt(getColumnIndexOrThrow(column))
private fun Cursor.bool(column: String) = getInt(getColumnIndexOrThrow(column)) != 0
private fun Cursor.string(column: String): String? = getString(getColumnIndexOrThrow(column))

private inline fun <reified E : Enum<E>> String?.enumOr(default: E): E = enumValues<E>().firstOrNull { it.name == this } ?: default

/**
 * The launcher-wide clock and calendar values the Home screen used for a facet that did not override them, as stored in the settings
 * DataStore. The keys are spelled out here because the launcher no longer defines most of them: this is the one place left that reads them.
 */
fun Preferences.legacyLauncherClock(): ClockWidgetConfig {
    val defaults = ClockWidgetConfig()
    return ClockWidgetConfig(
        templateId = this[stringPreferencesKey("clock_template_id")].enumOr(defaults.templateId),
        fontOption = this[stringPreferencesKey("clock_font_option")].enumOr(defaults.fontOption),
        colorOption = this[stringPreferencesKey("clock_color_option")].enumOr(defaults.colorOption),
        accentColorOption = this[stringPreferencesKey("clock_accent_color_option")].enumOr(defaults.accentColorOption),
        use24HourTime = this[booleanPreferencesKey("use_24_hour_time")] ?: defaults.use24HourTime,
        showMeridiem = this[booleanPreferencesKey("clock_show_meridiem")] ?: defaults.showMeridiem,
        dateStyle = this[stringPreferencesKey("clock_date_style")].enumOr(defaults.dateStyle),
        showAlarm = this[booleanPreferencesKey("show_next_alarm")] ?: defaults.showAlarm,
        showBattery = this[booleanPreferencesKey("show_battery_percentage")] ?: defaults.showBattery,
        showWeather = this[booleanPreferencesKey("show_weather")] ?: defaults.showWeather,
        weatherIconStyle = this[stringPreferencesKey("weather_icon_style")].enumOr(defaults.weatherIconStyle),
        calendar = ClockCalendarConfig(
            showIndicator = this[booleanPreferencesKey("show_calendar_indicator")] ?: defaults.calendar.showIndicator,
            itemsToShow = (this[intPreferencesKey("calendar_items_to_show")] ?: defaults.calendar.itemsToShow)
                .coerceIn(CalendarDisplayOptions.MIN_ITEMS, CalendarDisplayOptions.MAX_ITEMS),
            showAllDay = this[booleanPreferencesKey("show_all_day_events")] ?: defaults.calendar.showAllDay,
            calendarIds = this[stringSetPreferencesKey("selected_calendar_ids")],
        ),
    )
}
