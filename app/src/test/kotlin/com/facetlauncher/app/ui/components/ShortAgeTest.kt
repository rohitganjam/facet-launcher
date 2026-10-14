package com.facetlauncher.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ShortAgeTest {

    private fun age(millis: Long) = shortAge(millis)

    @Test
    fun `under a second reads as one second`() {
        assertEquals(ShortAge(1, AgeUnit.SECONDS), age(0))
        assertEquals(ShortAge(1, AgeUnit.SECONDS), age(999))
    }

    @Test
    fun `a negative age from a clock skew reads as one second`() {
        assertEquals(ShortAge(1, AgeUnit.SECONDS), age(-5_000))
    }

    @Test
    fun `seconds count up to fifty nine`() {
        assertEquals(ShortAge(1, AgeUnit.SECONDS), age(1_000))
        assertEquals(ShortAge(59, AgeUnit.SECONDS), age(59_999))
    }

    @Test
    fun `sixty seconds is one minute and minutes run to fifty nine`() {
        assertEquals(ShortAge(1, AgeUnit.MINUTES), age(60_000))
        assertEquals(ShortAge(2, AgeUnit.MINUTES), age(2 * 60_000L + 30_000))
        assertEquals(ShortAge(59, AgeUnit.MINUTES), age(59 * 60_000L + 59_000))
    }

    @Test
    fun `sixty minutes is one hour and hours run to twenty three`() {
        assertEquals(ShortAge(1, AgeUnit.HOURS), age(60 * 60_000L))
        assertEquals(ShortAge(2, AgeUnit.HOURS), age(2 * 3_600_000L + 1_800_000))
        assertEquals(ShortAge(23, AgeUnit.HOURS), age(23 * 3_600_000L + 3_599_000))
    }

    @Test
    fun `twenty four hours is one day and days keep counting`() {
        assertEquals(ShortAge(1, AgeUnit.DAYS), age(24 * 3_600_000L))
        assertEquals(ShortAge(2, AgeUnit.DAYS), age(2 * 86_400_000L + 5_000))
        assertEquals(ShortAge(30, AgeUnit.DAYS), age(30 * 86_400_000L))
    }
}
