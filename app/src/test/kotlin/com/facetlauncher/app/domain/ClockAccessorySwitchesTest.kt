package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.toEntity
import com.facetlauncher.app.data.local.toFacet
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAccessorySwitchesTest {

    private val launcher = LauncherSettings(showNextAlarm = true, showBatteryPercentage = false, showWeather = true)

    private fun facet(overrideClock: Boolean) =
        Facet(id = 1, name = "Work", position = 0, overrideClock = overrideClock, clockShowAlarm = false, clockShowBattery = true, clockShowWeather = false)

    @Test
    fun `with no facet the launcher's switches apply`() {
        val clock = resolveFacetStyle(null, launcher).clock

        assertTrue(clock.showAlarm)
        assertFalse(clock.showBattery)
        assertTrue(clock.showWeather)
    }

    @Test
    fun `a facet that does not override the clock inherits the launcher's switches`() {
        val clock = resolveFacetStyle(facet(overrideClock = false), launcher).clock

        assertTrue(clock.showAlarm)
        assertFalse(clock.showBattery)
        assertTrue(clock.showWeather)
    }

    @Test
    fun `a facet that overrides the clock uses its own switches`() {
        val clock = resolveFacetStyle(facet(overrideClock = true), launcher).clock

        assertFalse(clock.showAlarm)
        assertTrue(clock.showBattery)
        assertFalse(clock.showWeather)
    }

    @Test
    fun `a new facet shows alarm and battery and not weather`() {
        val facet = Facet(name = "New", position = 0)

        assertTrue(facet.clockShowAlarm)
        assertTrue(facet.clockShowBattery)
        assertFalse(facet.clockShowWeather)
    }

    @Test
    fun `the switches survive the trip to and from a database row`() {
        val facet = facet(overrideClock = true)

        assertEquals(facet, facet.toEntity().toFacet())
    }
}
