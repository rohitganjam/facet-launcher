package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherHour
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherDayTest {

    private val india = ZoneId.of("Asia/Kolkata")
    private val london = ZoneId.of("Europe/London")

    private fun hour(time: String, temperature: Double, rain: Double? = null, symbol: String? = null) =
        WeatherHour(Instant.parse(time), temperature, null, null, null, null, null, rain, symbol)

    @Test
    fun `each local day gets its own low and high`() {
        // Given hours on two local days in India (the second day starts at 18:30 UTC)
        val hours = listOf(
            hour("2026-10-24T05:00:00Z", 26.0), hour("2026-10-24T10:00:00Z", 27.5),
            hour("2026-10-24T20:00:00Z", 23.5), hour("2026-10-25T08:00:00Z", 25.4),
        )

        // When summarised
        val days = dailySummaries(hours, india)

        // Then they split at local midnight, not UTC midnight
        assertEquals(listOf(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 25)), days.map { it.date })
        assertEquals(26.0 to 27.5, days[0].lowC to days[0].highC)
        assertEquals(23.5 to 25.4, days[1].lowC to days[1].highC)
    }

    @Test
    fun `the same hours split differently in another zone`() {
        val hours = listOf(hour("2026-10-24T17:00:00Z", 24.0), hour("2026-10-24T19:00:00Z", 23.0))

        assertEquals(1, dailySummaries(hours, ZoneId.of("UTC")).size)
        assertEquals(2, dailySummaries(hours, india).size)
    }

    @Test
    fun `a day that has the clocks going back keeps both ends of its hours`() {
        // Given 00:30 BST and 23:30 GMT on the day the clocks go back in London (25 October 2026)
        val hours = listOf(hour("2026-10-24T23:30:00Z", 11.0), hour("2026-10-25T23:30:00Z", 9.0))

        val days = dailySummaries(hours, london)

        assertEquals(1, days.size)
        assertEquals(LocalDate.of(2026, 10, 25), days[0].date)
        assertEquals(9.0 to 11.0, days[0].lowC to days[0].highC)
    }

    @Test
    fun `the day's symbol is the point nearest local noon that has one`() {
        // Given a morning, a noon and an evening point (IST 08:30, 12:30, 20:30)
        val hours = listOf(
            hour("2026-10-24T03:00:00Z", 25.0, symbol = "fog"),
            hour("2026-10-24T07:00:00Z", 27.0, symbol = "heavyrain"),
            hour("2026-10-24T15:00:00Z", 24.0, symbol = "clearsky_night"),
            hour("2026-10-24T07:30:00Z", 27.0),
        )

        assertEquals("heavyrain", dailySummaries(hours, india)[0].symbolCode)
    }

    @Test
    fun `a day with no symbols has none`() {
        assertNull(dailySummaries(listOf(hour("2026-10-24T05:00:00Z", 26.0)), india)[0].symbolCode)
    }

    @Test
    fun `rain adds up the hourly amounts and is null when there are none`() {
        val wet = listOf(hour("2026-10-24T05:00:00Z", 26.0, rain = 1.0), hour("2026-10-24T06:00:00Z", 26.0, rain = 2.5), hour("2026-10-24T07:00:00Z", 26.0))
        val sparse = listOf(hour("2026-10-27T00:00:00Z", 21.0), hour("2026-10-27T06:00:00Z", 29.0))

        assertEquals(3.5, dailySummaries(wet, india)[0].precipitationMm!!, 0.001)
        assertNull(dailySummaries(sparse, india)[0].precipitationMm)
    }

    @Test
    fun `a six hourly day still gets a low and a high`() {
        val sparse = listOf(hour("2026-10-27T00:00:00Z", 21.2), hour("2026-10-27T06:00:00Z", 29.3), hour("2026-10-27T12:00:00Z", 27.4))

        val day = dailySummaries(sparse, india)[0]

        assertEquals(21.2 to 29.3, day.lowC to day.highC)
    }

    @Test
    fun `no hours gives no days`() {
        assertEquals(emptyList<WeatherDay>(), dailySummaries(emptyList(), india))
    }
}
