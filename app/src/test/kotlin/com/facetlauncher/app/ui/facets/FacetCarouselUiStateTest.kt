package com.facetlauncher.app.ui.facets

import com.facetlauncher.app.data.clockItemWith
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class FacetCarouselUiStateTest {

    private val facets = listOf(
        Facet(id = 1L, name = "Facet 1", position = 0, clockStack = listOf(clockItemWith(ClockWidgetConfig(use24HourTime = false), facetId = 1L))),
        Facet(id = 2L, name = "Facet 2", position = 1, clockStack = listOf(clockItemWith(ClockWidgetConfig(use24HourTime = true), facetId = 2L))),
    )

    @Test
    fun `each facet resolves its own effective 24 hour time independently`() {
        val state = FacetCarouselUiState(facets = facets, globalSettings = LauncherSettings(use24HourTime = false))

        // Each facet's clock shows its own setting
        assertEquals(false, state.style(1L).clock.use24HourTime)
        assertEquals(true, state.style(2L).clock.use24HourTime)
    }
}
