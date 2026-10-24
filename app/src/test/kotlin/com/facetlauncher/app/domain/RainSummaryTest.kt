package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WeatherHour
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RainSummaryTest {

    private val base = Instant.parse("2026-10-24T00:00:00Z")

    /** One hourly point per value, starting at [start], with that much rain in the hour (`null` for none known). */
    private fun hours(start: String, vararg rain: Double?): List<WeatherHour> =
        rain.mapIndexed { i, mm ->
            WeatherHour(Instant.parse(start).plusSeconds(i * 3600L), 25.0, null, null, null, null, null, mm, null)
        }

    @Test
    fun `no hourly amounts means no data`() {
        assertEquals(RainSummary.NoData, summarizeRain(hours("2026-10-24T05:00:00Z", null, null), Instant.parse("2026-10-24T05:30:00Z")))
        assertEquals(RainSummary.NoData, summarizeRain(emptyList(), base))
    }

    @Test
    fun `a day of nothing but trace amounts is dry`() {
        val summary = summarizeRain(hours("2026-10-24T05:00:00Z", 0.0, 0.1, 0.0, 0.1), Instant.parse("2026-10-24T05:30:00Z"))

        assertEquals(RainSummary.Dry, summary)
    }

    @Test
    fun `rain later in the day gives its start, end and total`() {
        // Given dry until 15:00 and rain for three hours
        val points = hours("2026-10-24T09:00:00Z", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 1.0, 0.0, 0.0)

        val summary = summarizeRain(points, Instant.parse("2026-10-24T09:30:00Z")) as RainSummary.RainLater

        assertEquals(Instant.parse("2026-10-24T15:00:00Z"), summary.from)
        assertEquals(Instant.parse("2026-10-24T18:00:00Z"), summary.until)
        assertEquals(4.0, summary.totalMm, 0.001)
        assertEquals(RainIntensity.LIGHT, summary.intensity)
    }

    @Test
    fun `rain that is falling now reports how long it goes on and what is left`() {
        // Given it is raining now (0.5 past the first hour) and stops after the third hour
        val points = hours("2026-10-24T05:00:00Z", 3.0, 2.0, 1.0, 0.0, 0.0, 0.0)

        val summary = summarizeRain(points, Instant.parse("2026-10-24T05:30:00Z")) as RainSummary.RainNow

        assertEquals(Instant.parse("2026-10-24T08:00:00Z"), summary.until)
        assertEquals(6.0, summary.totalMm, 0.001)
        assertEquals(RainIntensity.MODERATE, summary.intensity)
    }

    @Test
    fun `an hour already over is not counted as still to fall but the hour in progress is`() {
        // Given the 05:00 hour (5 mm) is over at 06:30, and the 06:00 and 07:00 hours hold 1 mm each
        val points = hours("2026-10-24T05:00:00Z", 5.0, 1.0, 1.0, 0.0)

        val summary = summarizeRain(points, Instant.parse("2026-10-24T06:30:00Z")) as RainSummary.RainNow

        assertEquals(2.0, summary.totalMm, 0.001)
    }

    @Test
    fun `a short dry gap does not end the spell, a longer one does`() {
        // Given two wet hours, a two-hour gap, one wet hour: one spell
        val merged = hours("2026-10-24T05:00:00Z", 1.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0)
        // And with a three-hour gap: two spells, the second smaller
        val split = hours("2026-10-24T05:00:00Z", 3.0, 3.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0)

        val one = summarizeRain(merged, Instant.parse("2026-10-24T05:30:00Z")) as RainSummary.RainNow
        val two = summarizeRain(split, Instant.parse("2026-10-24T05:30:00Z")) as RainSummary.RainNow

        assertEquals(Instant.parse("2026-10-24T10:00:00Z"), one.until)
        assertEquals(Instant.parse("2026-10-24T07:00:00Z"), two.until)
    }

    @Test
    fun `the wetter later spell is reported over a trace now`() {
        // Given a drizzle now (0.3) and a downpour at 14:00
        val points = hours("2026-10-24T09:00:00Z", 0.3, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 4.0, 0.0)

        val summary = summarizeRain(points, Instant.parse("2026-10-24T09:30:00Z")) as RainSummary.RainLater

        assertEquals(Instant.parse("2026-10-24T15:00:00Z"), summary.from)
        assertEquals(12.0, summary.totalMm, 0.001)
        assertEquals(RainIntensity.HEAVY, summary.intensity)
    }

    @Test
    fun `the current spell wins when it holds at least half as much as the best`() {
        // Given 6 mm falling now and a 10 mm spell later
        val points = hours("2026-10-24T05:00:00Z", 3.0, 3.0, 0.0, 0.0, 0.0, 0.0, 5.0, 5.0, 0.0)

        val summary = summarizeRain(points, Instant.parse("2026-10-24T05:30:00Z"))

        assertTrue(summary is RainSummary.RainNow)
    }

    @Test
    fun `rain still falling at the end of the window has no end`() {
        val points = hours("2026-10-24T05:00:00Z", *Array(24) { 1.0 })

        val summary = summarizeRain(points, Instant.parse("2026-10-24T05:30:00Z")) as RainSummary.RainNow

        assertNull(summary.until)
    }

    @Test
    fun `points past the window are ignored`() {
        // Given a dry day and a downpour on the day after the window
        val points = hours("2026-10-24T05:00:00Z", *Array(24) { 0.0 }) + hours("2026-10-25T05:00:00Z", 9.0, 9.0)

        assertEquals(RainSummary.Dry, summarizeRain(points, Instant.parse("2026-10-24T05:30:00Z")))
    }

    @Test
    fun `the storm in the real forecast is heavy rain through the night`() {
        // Given the compact feed's next_1_hours rain from 05:00 UTC on 24 October (Bengaluru) onward
        val rain = listOf(
            1.0, 2.8, 1.5, 0.5, 0.0, 0.0, 0.2, 0.4, 1.1, 0.5, 0.4, 0.4, 0.3, 0.3, 1.3, 1.8, 2.4, 3.5, 5.3, 7.3, 8.4, 9.4, 9.1, 5.4,
            2.5, 2.4, 1.8, 1.8, 2.6, 2.2, 3.3, 1.9, 0.9, 1.1, 0.1, 0.0,
        )
        val points = hours("2026-10-24T05:00:00Z", *rain.toTypedArray())

        // When summarised at 11:00 local, with the spell running to the end of the window
        val now = Instant.parse("2026-10-24T05:30:00Z")
        val summary = summarizeRain(points, now) as RainSummary.RainNow

        // Then it is heavy rain with no end in sight, and the 24-hour total is about 63 mm
        assertNull(summary.until)
        assertEquals(RainIntensity.HEAVY, summary.intensity)
        assertEquals(63.3, summary.totalMm, 0.05)

        // And a day later it is a moderate spell that ends at 15:00 UTC with 20.5 mm still to come
        val later = summarizeRain(points, Instant.parse("2026-10-25T05:30:00Z")) as RainSummary.RainNow
        assertEquals(Instant.parse("2026-10-25T15:00:00Z"), later.until)
        assertEquals(RainIntensity.MODERATE, later.intensity)
        assertEquals(20.5, later.totalMm, 0.05)
    }
}
