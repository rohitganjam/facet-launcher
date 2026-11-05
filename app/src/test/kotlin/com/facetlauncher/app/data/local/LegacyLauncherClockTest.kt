package com.facetlauncher.app.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.WeatherIconStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyLauncherClockTest {

    @Test
    fun `a launcher that never set anything reads as the default clock`() {
        // Given no stored settings, When read, Then the defaults
        assertEquals(ClockWidgetConfig(), emptyPreferences().legacyLauncherClock())
    }

    @Test
    fun `the launcher-wide clock and calendar keys are read by their stored names`() {
        // Given the values the launcher stored before the clock held its own settings
        val stored = preferencesOf(
            stringPreferencesKey("clock_template_id") to ClockTemplateId.DATE_FORWARD.name,
            stringPreferencesKey("clock_date_style") to ClockDateStyle.CONDENSED.name,
            booleanPreferencesKey("use_24_hour_time") to true,
            booleanPreferencesKey("show_next_alarm") to false,
            booleanPreferencesKey("show_weather") to true,
            stringPreferencesKey("weather_icon_style") to WeatherIconStyle.entries.last().name,
            booleanPreferencesKey("show_calendar_indicator") to false,
            intPreferencesKey("calendar_items_to_show") to 99,
            booleanPreferencesKey("show_all_day_events") to false,
            stringSetPreferencesKey("selected_calendar_ids") to setOf("1", "2"),
        )

        // When read as a clock config
        val clock = stored.legacyLauncherClock()

        // Then each value lands in its field, an out-of-range count is clamped, and an unset key keeps its default
        assertEquals(ClockTemplateId.DATE_FORWARD, clock.templateId)
        assertEquals(ClockDateStyle.CONDENSED, clock.dateStyle)
        assertEquals(true, clock.use24HourTime)
        assertEquals(false, clock.showAlarm)
        assertEquals(true, clock.showWeather)
        assertEquals(WeatherIconStyle.entries.last(), clock.weatherIconStyle)
        assertEquals(true, clock.showBattery)
        assertEquals(ClockCalendarConfig(false, CalendarDisplayOptions.MAX_ITEMS, false, setOf("1", "2")), clock.calendar)
    }

    @Test
    fun `a stored name this version does not know reads as the default`() {
        // Given a template name that no longer exists
        val stored = preferencesOf(stringPreferencesKey("clock_template_id") to "GONE")

        // When read, Then the default template
        assertEquals(ClockWidgetConfig().templateId, stored.legacyLauncherClock().templateId)
    }
}
