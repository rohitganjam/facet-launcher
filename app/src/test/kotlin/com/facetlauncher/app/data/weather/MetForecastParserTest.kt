package com.facetlauncher.app.data.weather

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MetForecastParserTest {

    private val fixture = javaClass.getResource("/weather/locationforecast_compact.json")!!.readText()

    @Test
    fun `points come back in time order and unusable ones are skipped`() {
        // When the fixture is parsed (it has a point with no temperature and one with a bad time)
        val forecast = parseMetForecast(fixture)!!

        // Then the five good points are sorted by time
        assertEquals(
            listOf("2026-10-24T05:00:00Z", "2026-10-24T06:00:00Z", "2026-10-26T13:00:00Z", "2026-10-27T00:00:00Z", "2026-11-02T12:00:00Z").map(Instant::parse),
            forecast.hours.map { it.time },
        )
        assertEquals(Instant.parse("2026-10-24T05:21:26Z"), forecast.updatedAt)
    }

    @Test
    fun `an hourly point carries its instant values and next hour`() {
        val hour = parseMetForecast(fixture)!!.hours.first()

        assertEquals(26.6, hour.temperatureC, 0.001)
        assertEquals(78.4, hour.humidityPercent!!, 0.001)
        assertEquals(1013.9, hour.pressureHpa!!, 0.001)
        assertEquals(100.0, hour.cloudPercent!!, 0.001)
        assertEquals(2.9, hour.windSpeedMps!!, 0.001)
        assertEquals(85.9, hour.windFromDegrees!!, 0.001)
        assertEquals(1.0, hour.precipitationMm!!, 0.001)
        assertEquals("heavyrain", hour.symbolCode)
    }

    @Test
    fun `a six hourly point has no hourly rain and takes the six hour symbol`() {
        val hour = parseMetForecast(fixture)!!.hours.first { it.time == Instant.parse("2026-10-27T00:00:00Z") }

        assertNull(hour.precipitationMm)
        assertEquals("cloudy", hour.symbolCode)
    }

    @Test
    fun `the last point has no next blocks at all`() {
        val hour = parseMetForecast(fixture)!!.hours.last()

        assertEquals(29.3, hour.temperatureC, 0.001)
        assertNull(hour.precipitationMm)
        assertNull(hour.symbolCode)
    }

    @Test
    fun `text that is not a forecast gives null`() {
        assertNull(parseMetForecast("<html>Service unavailable</html>"))
        assertNull(parseMetForecast("{}"))
        assertNotNull(parseMetForecast("""{"properties": {"timeseries": []}}"""))
    }
}
