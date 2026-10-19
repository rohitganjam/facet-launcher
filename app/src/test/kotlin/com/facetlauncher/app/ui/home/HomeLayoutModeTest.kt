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
    fun `an unfolded foldable that is wider than tall is landscape`() {
        // Given the Pixel Fold's inner display
        assertEquals(HomeLayoutMode.LANDSCAPE, homeLayoutModeFor(width = 841.dp, height = 700.dp))
    }

    @Test
    fun `a tablet in portrait stays portrait`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 800.dp, height = 1280.dp))
    }

    @Test
    fun `a square window is portrait`() {
        assertEquals(HomeLayoutMode.PORTRAIT, homeLayoutModeFor(width = 600.dp, height = 600.dp))
    }
}
