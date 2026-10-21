package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutModeTest {

    @Test
    fun `a phone held upright is portrait`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 393.dp, height = 851.dp))
    }

    @Test
    fun `a phone on its side is landscape`() {
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 851.dp, height = 393.dp))
    }

    @Test
    fun `an unfolded foldable wider than tall is landscape`() {
        // Given the Pixel Fold's inner display
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 841.dp, height = 700.dp))
    }

    @Test
    fun `a tablet held upright is portrait whatever its size`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 800.dp, height = 1280.dp))
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 1000.dp, height = 1400.dp))
    }

    @Test
    fun `a tablet on its side is landscape`() {
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 1280.dp, height = 800.dp))
    }

    @Test
    fun `a window taller than wide is portrait even when it is 600dp wide or more`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 700.dp, height = 701.dp))
    }

    @Test
    fun `a square window at least 600dp wide is landscape`() {
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 600.dp, height = 600.dp))
    }

    @Test
    fun `a window narrower than 600dp stays portrait even when wider than tall`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 599.dp, height = 400.dp))
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 320.dp, height = 300.dp))
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

    @Test
    fun `landscape allowed leaves the orientation to the device`() {
        assertEquals(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, homeRequestedOrientation(allowLandscape = true))
    }

    @Test
    fun `landscape off locks the screen to portrait`() {
        assertEquals(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, homeRequestedOrientation(allowLandscape = false))
    }
}
