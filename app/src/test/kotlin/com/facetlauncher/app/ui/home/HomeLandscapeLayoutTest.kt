package com.facetlauncher.app.ui.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLandscapeLayoutTest {

    @Test
    fun `a wide pane keeps the requested columns`() {
        assertEquals(6, landscapeGridColumns(requested = 6, paneWidth = 500.dp))
    }

    @Test
    fun `a narrow pane gets fewer columns so cells stay readable`() {
        // Given a 6 column grid in a pane only 260dp wide
        assertEquals(4, landscapeGridColumns(requested = 6, paneWidth = 260.dp))
    }

    @Test
    fun `never fewer than three columns`() {
        assertEquals(3, landscapeGridColumns(requested = 6, paneWidth = 100.dp))
    }

    @Test
    fun `a request below the fit is left alone`() {
        assertEquals(3, landscapeGridColumns(requested = 3, paneWidth = 500.dp))
    }

    @Test
    fun `before the pane is measured the request is used as is`() {
        assertEquals(5, landscapeGridColumns(requested = 5, paneWidth = 0.dp))
    }

    @Test
    fun `a roomy pane scales the requested columns up with its width`() {
        // Given four requested columns and a pane twice a phone's 360dp
        assertEquals(8, landscapeGridColumns(requested = 4, paneWidth = 720.dp, space = HomeLandscapeSpace.ROOMY))
        assertEquals(5, landscapeGridColumns(requested = 4, paneWidth = 480.dp, space = HomeLandscapeSpace.ROOMY))
    }

    @Test
    fun `a roomy pane never goes past eight columns`() {
        assertEquals(8, landscapeGridColumns(requested = 6, paneWidth = 1200.dp, space = HomeLandscapeSpace.ROOMY))
    }

    @Test
    fun `a roomy pane narrower than a phone's keeps the requested columns`() {
        assertEquals(4, landscapeGridColumns(requested = 4, paneWidth = 300.dp, space = HomeLandscapeSpace.ROOMY))
    }

    @Test
    fun `a roomy pane never shows fewer columns than requested`() {
        assertEquals(6, landscapeGridColumns(requested = 6, paneWidth = 360.dp, space = HomeLandscapeSpace.ROOMY))
    }
}
