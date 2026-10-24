package com.facetlauncher.app.domain

import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WeatherHour
import com.facetlauncher.app.data.model.WeatherLocationMode
import com.facetlauncher.app.data.model.WeatherPlace
import com.facetlauncher.app.data.weather.PlaceNameSource
import com.facetlauncher.app.data.weather.WeatherFetchState
import com.facetlauncher.app.data.weather.WeatherRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ObserveWeatherUseCaseTest {

    private val now = Instant.parse("2026-10-24T05:30:00Z")
    private val place = WeatherPlace(12.97, 77.59)
    private val fetched = Instant.parse("2026-10-24T05:21:00Z")

    private fun hour(time: String, temperature: Double, rain: Double? = 0.0, symbol: String? = "cloudy") =
        WeatherHour(Instant.parse(time), temperature, 70.0, 1010.0, 90.0, 2.0, 80.0, rain, symbol)

    /** Hourly points from 03:00 UTC (earlier today in India) through 30 hours on. */
    private val hours = (0 until 30).map { hour(Instant.parse("2026-10-24T03:00:00Z").plusSeconds(it * 3600L).toString(), 20.0 + it) }

    private fun state(placeName: String? = "Bengaluru", source: PlaceNameSource? = PlaceNameSource.GEOCODER) =
        WeatherFetchState(place = place, fetchedAt = fetched, placeName = placeName, placeNameSource = source)

    private fun build(
        settings: LauncherSettings = LauncherSettings(),
        points: List<WeatherHour> = hours,
        state: WeatherFetchState = state(),
        at: Instant = now,
    ) = ObserveWeatherUseCase.buildWeatherState(settings, points, state, at)

    @Test
    fun `nothing stored gives no state`() {
        assertNull(build(points = emptyList()))
    }

    @Test
    fun `now is the latest point at or before the current moment`() {
        assertEquals(Instant.parse("2026-10-24T05:00:00Z"), build()!!.now.time)
    }

    @Test
    fun `before the first stored point the first one stands in for now`() {
        assertEquals(Instant.parse("2026-10-24T03:00:00Z"), build(at = Instant.parse("2026-10-24T02:00:00Z"))!!.now.time)
    }

    @Test
    fun `the hourly strip starts at the current hour and holds up to 24 points`() {
        val hourly = build()!!.hourly

        assertEquals(24, hourly.size)
        assertEquals(Instant.parse("2026-10-24T05:00:00Z"), hourly.first().time)
    }

    @Test
    fun `six hourly points are not in the hourly strip`() {
        // Given four hourly points (03:00 to 06:00 UTC) and one six hourly point at noon
        val sparse = hours.take(4) + hour("2026-10-24T12:00:00Z", 30.0, rain = null)

        val hourly = build(points = sparse)!!.hourly

        assertEquals(listOf("2026-10-24T05:00:00Z", "2026-10-24T06:00:00Z").map(Instant::parse), hourly.map { it.time })
    }

    @Test
    fun `days start today and today's low and high include the hours stored earlier in the day`() {
        // Given a chosen city in India, where it is 11:00 on 24 October and the day started at 18:30 UTC the evening before
        val settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = WeatherCity("Pune", 18.5, 73.8, "Asia/Kolkata"))

        val today = build(settings = settings)!!.days.first()

        // Then today is the 24th, with a low from 03:00 UTC (08:30 local, before now) and a high from 18:00 UTC (23:30 local)
        assertEquals(LocalDate.of(2026, 10, 24), today.date)
        assertEquals(20.0, today.lowC, 0.001)
        assertEquals(35.0, today.highC, 0.001)
    }

    @Test
    fun `a chosen city sets the name and the zone`() {
        val pune = WeatherCity("Pune", 18.52, 73.86, "Asia/Kolkata")
        val settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = pune)

        val result = build(settings = settings, state = state(placeName = null, source = null))!!

        assertEquals("Pune", result.placeName)
        assertFalse(result.placeNameIsApproximate)
        assertEquals(ZoneId.of("Asia/Kolkata"), result.zone)
    }

    @Test
    fun `a chosen city's zone decides which day it is`() {
        // Given 20:00 UTC, which is already the next day in Kolkata but still the same day in London
        val at = Instant.parse("2026-10-24T20:00:00Z")
        val kolkata = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = WeatherCity("Pune", 18.5, 73.8, "Asia/Kolkata"))
        val london = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = WeatherCity("London", 51.5, -0.1, "Europe/London"))

        assertEquals(LocalDate.of(2026, 10, 25), build(settings = kolkata, at = at)!!.days.first().date)
        assertEquals(LocalDate.of(2026, 10, 24), build(settings = london, at = at)!!.days.first().date)
    }

    @Test
    fun `an unknown timezone falls back to the device's`() {
        val settings = LauncherSettings(weatherLocationMode = WeatherLocationMode.CITY, weatherCity = WeatherCity("X", 1.0, 1.0, "Mars/Olympus"))

        assertEquals(ZoneId.systemDefault(), build(settings = settings)!!.zone)
    }

    @Test
    fun `an offline name is marked approximate and a geocoder name is not`() {
        assertTrue(build(state = state("Bengaluru", PlaceNameSource.OFFLINE))!!.placeNameIsApproximate)
        assertFalse(build(state = state("Bengaluru", PlaceNameSource.GEOCODER))!!.placeNameIsApproximate)
        assertNull(build(state = state(null, null))!!.placeName)
    }

    @Test
    fun `a forecast fetched more than three hours ago is outdated`() {
        assertFalse(build()!!.isOutdated)
        assertTrue(build(at = fetched.plusSeconds(3 * 3600L + 60))!!.isOutdated)
        assertTrue(build(state = WeatherFetchState(place = place))!!.isOutdated)
        assertEquals(fetched, build()!!.updatedAt)
    }

    @Test
    fun `the rain summary is for the next day from now`() {
        val wet = hours.map { if (it.time >= Instant.parse("2026-10-24T09:00:00Z") && it.time < Instant.parse("2026-10-24T12:00:00Z")) it.copy(precipitationMm = 3.0) else it }

        val summary = build(points = wet)!!.summary

        assertTrue(summary is RainSummary.RainLater)
    }

    @Test
    fun `the flow emits the state built from the stores`() = runTest {
        val settingsRepository = mock(SettingsRepository::class.java)
        val weatherRepository = mock(WeatherRepository::class.java)
        `when`(settingsRepository.settings).thenReturn(flowOf(LauncherSettings()))
        `when`(weatherRepository.observeHours()).thenReturn(flowOf(hours))
        `when`(weatherRepository.observeState()).thenReturn(flowOf(state()))
        val clock = Clock.fixed(now, ZoneOffset.UTC)

        val emitted = ObserveWeatherUseCase(settingsRepository, weatherRepository, clock)().first()

        assertEquals("Bengaluru", emitted!!.placeName)
        assertEquals(Instant.parse("2026-10-24T05:00:00Z"), emitted.now.time)
    }
}
