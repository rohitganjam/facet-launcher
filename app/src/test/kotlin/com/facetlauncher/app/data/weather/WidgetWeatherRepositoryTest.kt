package com.facetlauncher.app.data.weather

import com.facetlauncher.app.data.model.WeatherForecast
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherPlace
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetWeatherRepositoryTest {

    private class FixedClock(var now: Instant) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
    }

    private class CountingApi : WeatherApi {
        val places = mutableListOf<WeatherPlace>()
        var result: WeatherApiResult = WeatherApiResult.Failed(WeatherFailure.OFFLINE)
        override suspend fun fetch(place: WeatherPlace, ifModifiedSince: String?): WeatherApiResult {
            places += place
            return result
        }
    }

    private val start = Instant.parse("2026-11-04T08:00:00Z")
    private val clock = FixedClock(start)
    private val api = CountingApi()
    private val repository = WidgetWeatherRepository(api, clock)
    private val oslo = WeatherPlace.rounded(59.91, 10.75)
    private val pune = WeatherPlace.rounded(18.52, 73.86)

    private fun success() = WeatherApiResult.Success(
        forecast = WeatherForecast(start, listOf(WeatherHour(start, 5.0, null, null, null, null, null, 0.0, "clearsky_day"))),
        expiresAt = start.plusSeconds(3600),
        lastModified = null,
    )

    @Test
    fun `two widgets on one place share one request`() = runTest {
        // Given a forecast fetched for a place
        api.result = success()
        repository.refresh(oslo)

        // When a second widget set to the same place refreshes before the forecast expires
        val second = repository.refresh(oslo)

        // Then no second request is made and it gets the same hours
        assertEquals(listOf(oslo), api.places)
        assertEquals(1, second?.hours?.size)
    }

    @Test
    fun `each place is fetched on its own`() = runTest {
        // Given two widgets set to different places
        api.result = success()

        // When both refresh
        repository.refresh(oslo)
        repository.refresh(pune)

        // Then each place made one request
        assertEquals(listOf(oslo, pune), api.places)
    }

    @Test
    fun `a place that has expired is fetched again`() = runTest {
        // Given a fetched forecast
        api.result = success()
        repository.refresh(oslo)

        // When MET's expiry has passed
        clock.now = start.plusSeconds(3601)
        repository.refresh(oslo)

        // Then it is requested again
        assertEquals(2, api.places.size)
    }

    @Test
    fun `a failed first fetch leaves nothing to show`() = runTest {
        // Given an offline first fetch
        api.result = WeatherApiResult.Failed(WeatherFailure.OFFLINE)

        // Then there is no forecast, and the next attempt is held back by the gate
        assertNull(repository.refresh(oslo))
        assertNull(repository.refresh(oslo))
        assertEquals(1, api.places.size)
    }

    @Test
    fun `a failed refresh keeps the forecast already held`() = runTest {
        // Given a held forecast that has since expired and an offline refresh
        api.result = success()
        repository.refresh(oslo)
        clock.now = start.plusSeconds(3601)
        api.result = WeatherApiResult.Failed(WeatherFailure.OFFLINE)

        // When refreshing
        val kept = repository.refresh(oslo)

        // Then the earlier hours are still returned
        assertNotNull(kept)
        assertEquals(1, kept?.hours?.size)
    }
}
