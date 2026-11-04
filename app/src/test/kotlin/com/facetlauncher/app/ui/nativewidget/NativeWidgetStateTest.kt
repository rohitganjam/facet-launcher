package com.facetlauncher.app.ui.nativewidget

import com.facetlauncher.app.data.model.CalendarEvent
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.domain.NativeWidgetOverride
import com.facetlauncher.app.domain.isConfigurable
import com.facetlauncher.app.ui.nativewidget.NativeWidgetAccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeWidgetStateTest {

    private val launcherEvent = CalendarEvent(1, "1", "Launcher", 0, 1, false)
    private val ownEvent = CalendarEvent(2, "9", "Own", 0, 1, false)

    @Test
    fun `no override leaves the launcher state as it is`() {
        // Given the launcher-wide state
        val state = NativeWidgetState()

        // Then withOverride(null) returns it unchanged
        assertSame(state, state.withOverride(null))
    }

    @Test
    fun `an agenda override replaces the events and keeps the rest`() {
        // Given launcher events and an override with its own
        val state = NativeWidgetState(calendar = com.facetlauncher.app.ui.home.HomeCalendarState(events = kotlinx.collections.immutable.persistentListOf(launcherEvent)))

        // When applying the override
        val shown = state.withOverride(NativeWidgetOverride(events = listOf(ownEvent)))

        // Then the widget shows its own events, and the weather is untouched
        assertEquals(listOf(ownEvent), shown.calendar.events)
        assertEquals(state.weather, shown.weather)
    }

    @Test
    fun `an unavailable weather override hides the forecast`() {
        // When a widget's own location has no forecast
        val shown = NativeWidgetState().withOverride(NativeWidgetOverride(weatherUnavailable = true))

        // Then it shows none rather than the launcher's
        assertNull(shown.weather.weather)
    }

    @Test
    fun `small widgets and Now Playing offer no configure entry`() {
        // A small weather tile and a small agenda strip do not, and Now Playing never does
        assertFalse(NativeWidgetType.WEATHER.isConfigurable(80, 80))
        assertFalse(NativeWidgetType.AGENDA.isConfigurable(300, 80))
        assertFalse(NativeWidgetType.NOW_PLAYING.isConfigurable(300, 300))
    }

    @Test
    fun `medium and large weather and agenda offer a configure entry`() {
        assertTrue(NativeWidgetType.WEATHER.isConfigurable(300, 100))
        assertTrue(NativeWidgetType.AGENDA.isConfigurable(300, 150))
        assertTrue(NativeWidgetType.AGENDA.isConfigurable(300, 300))
    }

    @Test
    fun `a widget unavailable for want of location access says so`() {
        // Given the launcher state with no location problem
        val state = NativeWidgetState()

        // When a widget set to the position has no permission
        val shown = state.withOverride(NativeWidgetOverride(weatherUnavailable = true, locationMissing = true))

        // Then its own state asks for access
        assertTrue(shown.access.locationMissing)
        assertFalse(state.access.locationMissing)
    }

    @Test
    fun `a widget with its own forecast no longer asks for location access`() {
        // Given a launcher state that is missing location access
        val state = NativeWidgetState(access = NativeWidgetAccess(locationMissing = true))

        // When a widget set to a city has a forecast of its own
        val shown = state.withOverride(NativeWidgetOverride(weather = com.facetlauncher.app.ui.home.previewWeather()))

        // Then it shows the forecast with no prompt
        assertFalse(shown.access.locationMissing)
    }

    @Test
    fun `a tap on a Hub weather widget opens that widget's own sheet`() {
        // Given actions that report the Hub widget a weather tap came from
        var opened: Int? = null
        var launcherOpens = 0
        val actions = NativeWidgetActions(onOpenWeather = { launcherOpens++ }, onOpenHubWeather = { opened = it })

        // When the widget with id -4 is tapped
        actions.forHubWidget(-4).onOpenWeather()

        // Then its id is reported, and the launcher-wide weather sheet is not what opens
        assertEquals(-4, opened)
        assertEquals(0, launcherOpens)
    }

    @Test
    fun `the weather sheet of a widget with its own location shows that forecast`() {
        // Given the launcher's weather state
        val launcher = com.facetlauncher.app.ui.home.weather.WeatherUiState(weather = com.facetlauncher.app.ui.home.previewWeather())

        // When a widget whose own location has no forecast is shown
        val shown = launcher.withWeatherOverride(NativeWidgetOverride(weatherUnavailable = true))

        // Then the launcher's forecast is not shown in its place
        assertNull(shown.weather)
        assertSame(launcher, launcher.withWeatherOverride(null))
    }
}
