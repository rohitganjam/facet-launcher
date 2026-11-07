package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyNativeWidgetConfigTest {

    private val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")

    @Test
    fun `a tab-separated setting is rewritten as the current form`() {
        // Given every field in the old form
        val legacy = "cal\t7\t3\nallday\t0\nwx\tcity\tOslo\t59.91\t10.75\tEurope/Oslo"

        // When upgraded and read back
        val config = decodeNativeWidgetConfig(upgradeLegacyNativeWidgetConfig(legacy))

        // Then nothing is lost
        assertEquals(setOf("3", "7"), config.calendarIds)
        assertEquals(false, config.showAllDayEvents)
        assertEquals(WidgetWeatherLocation.City(oslo), config.weatherLocation)
    }

    @Test
    fun `the old current-location line becomes current location`() {
        val config = decodeNativeWidgetConfig(upgradeLegacyNativeWidgetConfig("wx\tcurrent"))

        assertEquals(WidgetWeatherLocation.Current, config.weatherLocation)
    }

    @Test
    fun `an empty old calendar selection stays empty and is not turned into following the launcher`() {
        val config = decodeNativeWidgetConfig(upgradeLegacyNativeWidgetConfig("cal"))

        assertEquals(emptySet<String>(), config.calendarIds)
    }

    @Test
    fun `an old value that holds nothing whole upgrades to nothing`() {
        // Given a half-written city, junk, and null
        // Then none of them is kept, so the widget follows the launcher
        assertNull(upgradeLegacyNativeWidgetConfig("wx\tcity\tOslo"))
        assertNull(upgradeLegacyNativeWidgetConfig("junk"))
        assertNull(upgradeLegacyNativeWidgetConfig(null))
    }

    @Test
    fun `an old setting that is already JSON is not understood and is dropped`() {
        // Given a value in the current form fed to the old reader
        // Then it reads as nothing, which is why the importer only upgrades versions that wrote the old form
        assertNull(upgradeLegacyNativeWidgetConfig("""{"showAllDayEvents":true}"""))
    }
}
