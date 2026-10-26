package com.facetlauncher.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaledClockTest {

    @Test
    fun `an unscaled clock is drawn in its whole natural box`() {
        assertEquals(0, scaledClockTop(naturalHeight = 760, scale = 1f, originY = 0f))
        assertEquals(0, scaledClockTop(naturalHeight = 760, scale = 1f, originY = 1f))
        assertEquals(760, scaledClockHeight(naturalHeight = 760, scale = 1f))
    }

    @Test
    fun `a clock scaled down from its top is drawn at the top of its box`() {
        // Given the stacked clock measured on the emulator: 761px natural, scaled to 0.66 from its top
        assertEquals(0, scaledClockTop(naturalHeight = 761, scale = 0.66068625f, originY = 0f))
        assertEquals(503, scaledClockHeight(naturalHeight = 761, scale = 0.66068625f))
    }

    @Test
    fun `a clock scaled down from its bottom edge is drawn at the bottom of its box`() {
        // Then half the box is empty above it and the drawn clock is the lower half
        assertEquals(380, scaledClockTop(naturalHeight = 760, scale = 0.5f, originY = 1f))
        assertEquals(380, scaledClockHeight(naturalHeight = 760, scale = 0.5f))
    }

    @Test
    fun `a clock scaled from its middle leaves equal space above and below`() {
        assertEquals(200, scaledClockTop(naturalHeight = 800, scale = 0.5f, originY = 0.5f))
        assertEquals(400, scaledClockHeight(naturalHeight = 800, scale = 0.5f))
    }

    @Test
    fun `a clock scaled up from its bottom edge is drawn above its box`() {
        assertEquals(-500, scaledClockTop(naturalHeight = 500, scale = 2f, originY = 1f))
        assertEquals(1000, scaledClockHeight(naturalHeight = 500, scale = 2f))
    }

    @Test
    fun `the drawn height never goes negative`() {
        assertEquals(0, scaledClockHeight(naturalHeight = 100, scale = -1f))
        assertEquals(0, scaledClockHeight(naturalHeight = 0, scale = 1f))
    }
}
