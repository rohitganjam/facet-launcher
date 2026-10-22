package com.facetlauncher.app.ui.home.clock

import com.facetlauncher.app.data.model.ClockTemplateId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClockTemplateRenderersTest {

    @Test
    fun `every clock template id has a renderer`() {
        // Given every template id
        val missing = ClockTemplateId.entries - ClockTemplateRenderers.keys

        // Then none lacks a renderer (a new id added without one would crash ClockDisplay)
        assertEquals(emptyList<ClockTemplateId>(), missing)
    }
}
