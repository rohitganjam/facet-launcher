package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutModeTest {

    @Test
    fun `a phone held upright is portrait`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 393.dp))
    }

    @Test
    fun `a phone on its side is landscape`() {
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 851.dp))
    }

    @Test
    fun `an unfolded foldable is landscape`() {
        // Given the Pixel Fold's inner display
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 841.dp))
    }

    @Test
    fun `a tablet in portrait gets the landscape structure`() {
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 800.dp))
    }

    @Test
    fun `a narrow split screen window is portrait whatever its height`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 320.dp))
    }

    @Test
    fun `the portrait cut is 600dp wide`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 599.dp))
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 600.dp))
    }

    @Test
    fun `a phone on its side is compact`() {
        assertEquals(HomeLandscapeSpace.COMPACT, homeLandscapeSpaceFor(height = 411.dp))
    }

    @Test
    fun `an unfolded foldable is roomy`() {
        assertEquals(HomeLandscapeSpace.ROOMY, homeLandscapeSpaceFor(height = 701.dp))
    }

    @Test
    fun `the compact cut is 480dp tall`() {
        assertEquals(HomeLandscapeSpace.COMPACT, homeLandscapeSpaceFor(height = 479.dp))
        assertEquals(HomeLandscapeSpace.ROOMY, homeLandscapeSpaceFor(height = 480.dp))
    }
}
