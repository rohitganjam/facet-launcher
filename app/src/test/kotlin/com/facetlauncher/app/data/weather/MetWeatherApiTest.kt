package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MetWeatherApiTest {

    @Test
    fun `the request carries two decimal coordinates and an optional altitude`() {
        assertEquals(
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=12.97&lon=77.59",
            MetWeatherApi.requestUrl(WeatherPlace(12.97, 77.59)),
        )
        assertEquals(
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=12.97&lon=77.59&altitude=920",
            MetWeatherApi.requestUrl(WeatherPlace(12.97, 77.59, altitudeM = 920)),
        )
    }

    @Test
    fun `a place is rounded to two decimals so noise never makes a new place`() {
        assertEquals(WeatherPlace(12.97, 77.59), WeatherPlace.rounded(12.9716, 77.5946))
        assertEquals(WeatherPlace.rounded(12.9701, 77.5899), WeatherPlace.rounded(12.9699, 77.5901))
    }

    @Test
    fun `Retry-After in seconds and as a date`() {
        val now = Instant.parse("2026-10-24T10:00:00Z")

        assertEquals(now.plusSeconds(120), MetWeatherApi.parseRetryAfter("120", now))
        assertEquals(Instant.parse("2026-10-24T11:00:00Z"), MetWeatherApi.parseRetryAfter("Sat, 24 Oct 2026 11:00:00 GMT", now))
        assertNull(MetWeatherApi.parseRetryAfter(null, now))
        assertNull(MetWeatherApi.parseRetryAfter("soon", now))
    }

    @Test
    fun `Expires is read as an HTTP date`() {
        assertEquals(Instant.parse("2026-10-24T11:26:00Z"), MetWeatherApi.parseHttpDate("Sat, 24 Oct 2026 11:26:00 GMT"))
        assertNull(MetWeatherApi.parseHttpDate("garbage"))
    }

    @Test
    fun `the User-Agent names the app and a way to reach the developer`() {
        assertTrue(MetWeatherApi.USER_AGENT.startsWith("FacetLauncher/"))
        assertTrue(MetWeatherApi.USER_AGENT.contains("github.com"))
    }
}
