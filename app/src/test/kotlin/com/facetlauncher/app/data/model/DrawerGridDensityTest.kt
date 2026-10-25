package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrawerGridDensityTest {

    @Test
    fun `retired column by row names map to the nearest density`() {
        assertEquals(DrawerGridDensity.COMPACT, DrawerGridDensity.fromStored("FIVE_BY_SIX"))
        assertEquals(DrawerGridDensity.REGULAR, DrawerGridDensity.fromStored("FIVE_BY_FIVE"))
        assertEquals(DrawerGridDensity.REGULAR, DrawerGridDensity.fromStored("FOUR_BY_FIVE"))
        assertEquals(DrawerGridDensity.SPACIOUS, DrawerGridDensity.fromStored("FOUR_BY_FOUR"))
    }

    @Test
    fun `the old airy name maps to spacious`() {
        assertEquals(DrawerGridDensity.SPACIOUS, DrawerGridDensity.fromStored("AIRY"))
    }

    @Test
    fun `current names round trip`() {
        DrawerGridDensity.entries.forEach {
            assertEquals(it, DrawerGridDensity.fromStored(it.name))
        }
    }

    @Test
    fun `unknown value is null so callers fall back to the default`() {
        assertNull(DrawerGridDensity.fromStored("SIX_BY_SIX"))
    }
}
