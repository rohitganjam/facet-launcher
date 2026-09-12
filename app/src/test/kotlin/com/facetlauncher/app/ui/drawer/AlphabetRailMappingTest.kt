package com.facetlauncher.app.ui.drawer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlphabetRailMappingTest {

    private val letters = listOf("A", "B", "C", "D")

    /** A 200px rail band sitting inside a taller activation zone, e.g. bandTop=100, bandBottom=300. */
    private val bandTop = 100f
    private val bandBottom = 300f

    @Test
    fun `maps the top of the band to the first letter`() {
        assertEquals("A", letterAt(y = bandTop, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `maps the bottom of the band to the last letter`() {
        assertEquals("D", letterAt(y = bandBottom - 1f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `maps the middle of the band to a middle letter`() {
        assertEquals("C", letterAt(y = 200f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `a touch above the band clamps to the first letter, not null or a crash`() {
        assertEquals("A", letterAt(y = 0f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `a touch below the band clamps to the last letter, not null or a crash`() {
        assertEquals("D", letterAt(y = 999f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = letters))
    }

    @Test
    fun `returns null when there are no letters or the band has no height yet`() {
        assertNull(letterAt(y = 150f, bandTopPx = bandTop, bandBottomPx = bandBottom, letters = emptyList()))
        assertNull(letterAt(y = 150f, bandTopPx = 100f, bandBottomPx = 100f, letters = letters))
    }
}
