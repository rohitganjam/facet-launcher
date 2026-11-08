package com.facetlauncher.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SwipeBetweenTabsTest {

    private val tabs = listOf("PERSONAL", "WORK", "PRIVATE")

    @Test
    fun `a leftward swipe goes to the next tab`() {
        assertEquals("WORK", adjacentTab(tabs, "PERSONAL", dragPx = -120f, thresholdPx = 64f))
        assertEquals("PRIVATE", adjacentTab(tabs, "WORK", dragPx = -120f, thresholdPx = 64f))
    }

    @Test
    fun `a rightward swipe goes to the previous tab`() {
        assertEquals("PERSONAL", adjacentTab(tabs, "WORK", dragPx = 120f, thresholdPx = 64f))
    }

    @Test
    fun `a swipe past either end stays put`() {
        assertNull(adjacentTab(tabs, "PERSONAL", dragPx = 200f, thresholdPx = 64f))
        assertNull(adjacentTab(tabs, "PRIVATE", dragPx = -200f, thresholdPx = 64f))
    }

    @Test
    fun `a short drag stays put`() {
        assertNull(adjacentTab(tabs, "WORK", dragPx = -63f, thresholdPx = 64f))
        assertNull(adjacentTab(tabs, "WORK", dragPx = 20f, thresholdPx = 64f))
    }

    @Test
    fun `an unknown selected tab changes nothing`() {
        assertNull(adjacentTab(tabs, "OTHER", dragPx = -200f, thresholdPx = 64f))
    }
}
