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
}
