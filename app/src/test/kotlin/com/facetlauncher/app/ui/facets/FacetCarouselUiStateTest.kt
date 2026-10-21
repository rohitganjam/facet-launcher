package com.facetlauncher.app.ui.facets

import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class FacetCarouselUiStateTest {

    private val facets = listOf(
        Facet(id = 1L, name = "Facet 1", position = 0, overrideClock = false),
        Facet(id = 2L, name = "Facet 2", position = 1, overrideClock = true, use24HourTime = true),
    )

    @Test
    fun `each facet resolves its own effective 24 hour time independently`() {
        val state = FacetCarouselUiState(facets = facets, globalSettings = LauncherSettings(use24HourTime = false))

        // Facet 1 has no override — falls back to the global default
        assertEquals(false, state.style(1L).clock.use24HourTime)
        // Facet 2 overrides it — reflects its own stored value, not the global default
        assertEquals(true, state.style(2L).clock.use24HourTime)
    }
}
