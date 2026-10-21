package com.facetlauncher.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeClockHandleGeometryTest {

    @Test
    fun `a native clock lets the handle go up to its smallest scale plus headroom`() {
        // Given a 500px block that is all clock box, a 24px gap, 50px of headroom and a 0.5 smallest scale
        val floor = clockHandleFloorPx(blockHeightPx = 500f, clockBoxHeightPx = 500f, headroomPx = 50f, minGapPx = 24f, minScale = 0.5f, canShrink = true)

        // Then the clock may shrink to 250px: 250 + 50 + 24, not the whole 524px footprint
        assertEquals(324f, floor, 0.01f)
    }

    @Test
    fun `things below the clock box keep their full height`() {
        // Given a 500px block whose clock box is 400px, so 100px sits below it
        val floor = clockHandleFloorPx(blockHeightPx = 500f, clockBoxHeightPx = 400f, headroomPx = 50f, minGapPx = 24f, minScale = 0.5f, canShrink = true)

        assertEquals(100f + 200f + 50f + 24f, floor, 0.01f)
    }

    @Test
    fun `a hosted widget keeps its whole footprint`() {
        val floor = clockHandleFloorPx(blockHeightPx = 500f, clockBoxHeightPx = 500f, headroomPx = 50f, minGapPx = 24f, minScale = 0.5f, canShrink = false)

        assertEquals(524f, floor, 0.01f)
    }

    @Test
    fun `a small clock never gets a floor above its whole footprint`() {
        // Given a clock so short that its headroom outweighs the saving from shrinking
        val floor = clockHandleFloorPx(blockHeightPx = 100f, clockBoxHeightPx = 100f, headroomPx = 120f, minGapPx = 24f, minScale = 0.5f, canShrink = true)

        assertEquals(124f, floor, 0.01f)
    }
}
