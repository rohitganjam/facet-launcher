package com.facetlauncher.app.data.local

import com.facetlauncher.app.data.model.AppRowPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class FacetEntityTest {

    @Test
    fun `resolveSentinel returns the global value when there is no facet`() {
        // Given no facet at all
        val facet: FacetEntity? = null

        // When resolving a sentinel-defaulted field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the global value wins, since there's nothing to resolve against
        assertEquals(AppRowPosition.RIGHT, result)
    }

    @Test
    fun `resolveSentinel returns the facet value when it is not the sentinel`() {
        // Given a facet with its own real, non-sentinel value for this field
        val facet = FacetEntity(name = "Work", position = 0, appRowPosition = AppRowPosition.CENTER)

        // When resolving that field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the facet's own stored value wins, independent of any override flag
        assertEquals(AppRowPosition.CENTER, result)
    }

    @Test
    fun `resolveSentinel returns the global value when the facet's field is the sentinel`() {
        // Given a facet whose field is still at its LAUNCHER_DEFAULT sentinel
        val facet = FacetEntity(name = "Work", position = 0, appRowPosition = AppRowPosition.LAUNCHER_DEFAULT)

        // When resolving that field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the global default wins
        assertEquals(AppRowPosition.RIGHT, result)
    }

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
