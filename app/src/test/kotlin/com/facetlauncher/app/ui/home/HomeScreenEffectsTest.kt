package com.facetlauncher.app.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeScreenEffectsTest {
    private val size = IntSize(100, 100)
    private val origin = Offset(10f, 10f)
    private val settled = HandleBounds(basePx = 778f, floorPx = 63f, ceilingPx = 973f, handlePx = 778f)
    private val collapsed = HandleBounds(basePx = 0f, floorPx = 63f, ceilingPx = 63f, handlePx = 63f)

    @Test
    fun `geometry is not measured while the handle is collapsed onto its floor`() {
        // Given the clock box and root are measured but the content height has not reached the geometry
        // When checking
        val result = isClockGeometryMeasured(size, origin, size, collapsed)

        // Then the reclamp must wait, since the clock still sits at the top
        assertFalse(result)
    }

    @Test
    fun `geometry is not measured while any box is missing`() {
        assertFalse(isClockGeometryMeasured(null, origin, size, settled))
        assertFalse(isClockGeometryMeasured(size, null, size, settled))
        assertFalse(isClockGeometryMeasured(size, origin, null, settled))
    }

    @Test
    fun `geometry is measured once everything is present and the handle has room`() {
        assertTrue(isClockGeometryMeasured(size, origin, size, settled))
    }
}
