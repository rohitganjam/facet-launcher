package com.facetlauncher.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class FacetEntityTest {

    @Test
    fun `resolveOverride returns the global value when there is no facet`() {
        // Given no facet at all
        val facet: FacetEntity? = null

        // When resolving with any overriding/facetValue lambdas
        val result = facet.resolveOverride({ true }, { "facet" }, "global")

        // Then the global value wins, since there's nothing to override with
        assertEquals("global", result)
    }

    @Test
    fun `resolveOverride returns the facet value when overriding is true`() {
        // Given a facet whose override flag is on
        val facet = FacetEntity(name = "Work", position = 0, overrideClock = true)

        // When resolving against that flag
        val result = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the facet's own stored value wins
        assertEquals(facet.clockTemplateId, result)
    }

    @Test
    fun `resolveOverride returns the global value when overriding is false`() {
        // Given a facet whose override flag is off
        val facet = FacetEntity(name = "Work", position = 0, overrideClock = false)

        // When resolving against that flag
        val result = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the global default wins, even though the facet has its own stored value
        assertEquals(com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE, result)
    }
}
