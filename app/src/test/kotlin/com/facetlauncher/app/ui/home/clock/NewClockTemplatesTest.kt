package com.facetlauncher.app.ui.home.clock

import androidx.compose.ui.geometry.Size
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewClockTemplatesTest {

    private fun at(hour: Int, minute: Int) = LocalDateTime.of(2026, 11, 20, hour, minute)

    @Test
    fun `the day line starts empty at midnight and is half full at noon`() {
        assertEquals(0f, dayFraction(at(0, 0)), 0f)
        assertEquals(0.5f, dayFraction(at(12, 0)), 0.0001f)
    }

    @Test
    fun `the day line counts minutes through the day`() {
        // Given 10:41, which is 641 of 1440 minutes in
        assertEquals(641f / 1440f, dayFraction(at(10, 41)), 0.0001f)
    }

    @Test
    fun `the day line is nearly full just before midnight`() {
        assertEquals(1439f / 1440f, dayFraction(at(23, 59)), 0.0001f)
    }

    @Test
    fun `the liquid is empty at the top of the hour so no sliver shows`() {
        assertNull(liquidFillPath(Size(100f, 100f), fraction = 0f))
    }
}
