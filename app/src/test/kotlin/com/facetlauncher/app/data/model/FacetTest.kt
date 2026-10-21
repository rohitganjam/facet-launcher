package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FacetTest {

    @Test
    fun `resolveSentinel returns the global value when there is no facet`() {
        // Given no facet at all
        val facet: Facet? = null

        // When resolving a sentinel-defaulted field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the global value wins, since there's nothing to resolve against
        assertEquals(AppRowPosition.RIGHT, result)
    }

    @Test
    fun `resolveSentinel returns the facet value when it is not the sentinel`() {
        // Given a facet with its own real, non-sentinel value for this field
        val facet = Facet(name = "Work", position = 0, appRowPosition = AppRowPosition.CENTER)

        // When resolving that field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the facet's own stored value wins, independent of any override flag
        assertEquals(AppRowPosition.CENTER, result)
    }

    @Test
    fun `resolveSentinel returns the global value when the facet's field is the sentinel`() {
        // Given a facet whose field is still at its LAUNCHER_DEFAULT sentinel
        val facet = Facet(name = "Work", position = 0, appRowPosition = AppRowPosition.LAUNCHER_DEFAULT)

        // When resolving that field
        val result = facet.resolveSentinel({ it.appRowPosition }, AppRowPosition.LAUNCHER_DEFAULT, AppRowPosition.RIGHT)

        // Then the global default wins
        assertEquals(AppRowPosition.RIGHT, result)
    }

    @Test
    fun `resolveOverride returns the global value when there is no facet`() {
        // Given no facet at all
        val facet: Facet? = null

        // When resolving with any overriding/facetValue lambdas
        val result = facet.resolveOverride({ true }, { "facet" }, "global")

        // Then the global value wins, since there's nothing to override with
        assertEquals("global", result)
    }

    @Test
    fun `resolveOverride returns the facet value when overriding is true`() {
        // Given a facet whose override flag is on
        val facet = Facet(name = "Work", position = 0, overrideClock = true)

        // When resolving against that flag
        val result = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the facet's own stored value wins
        assertEquals(facet.clockTemplateId, result)
    }

    @Test
    fun `resolveOverride returns the global value when overriding is false`() {
        // Given a facet whose override flag is off
        val facet = Facet(name = "Work", position = 0, overrideClock = false)

        // When resolving against that flag
        val result = facet.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the global default wins, even though the facet has its own stored value
        assertEquals(com.facetlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE, result)
    }

    @Test
    fun `resolveLandscape prefers the facet's landscape value`() {
        assertEquals(AppRowPosition.CENTER, resolveLandscape(AppRowPosition.CENTER, AppRowPosition.RIGHT, true, AppRowPosition.LEFT, AppRowPosition.LEFT))
    }

    @Test
    fun `resolveLandscape falls back to the facet's portrait value before any launcher value`() {
        assertEquals(AppRowPosition.RIGHT, resolveLandscape(null, AppRowPosition.RIGHT, true, AppRowPosition.CENTER, AppRowPosition.LEFT))
    }

    @Test
    fun `resolveLandscape uses the launcher's landscape value when the facet's portrait value does not apply`() {
        assertEquals(AppRowPosition.CENTER, resolveLandscape(null, AppRowPosition.LAUNCHER_DEFAULT, false, AppRowPosition.CENTER, AppRowPosition.LEFT))
    }

    @Test
    fun `resolveLandscape ends at the launcher's portrait value with nothing set for landscape`() {
        assertEquals(AppRowPosition.LEFT, resolveLandscape(null, AppRowPosition.LAUNCHER_DEFAULT, false, null, AppRowPosition.LEFT))
    }

    @Test
    fun `resolveLandscape keeps a facet's own null portrait value instead of falling to the launcher`() {
        // A clock override that has never had its divider dragged: null means "natural", not "unset".
        assertEquals(null, resolveLandscape<Float?>(null, null, true, 300f, 200f))
    }

    @Test
    fun `a facet's landscape group round-trips through withLandscape`() {
        val overrides = LandscapeOverrides(
            clockAlignment = ClockAlignment.CENTER,
            clockZoneHeightDp = 310f,
            clockScale = 1.1f,
            clockWidgetWidthDp = 400,
            clockWidgetHeightDp = 300,
            appRowPosition = AppRowPosition.CENTER,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            appListColumnAlignment = AppListColumnAlignment.MIRRORED,
        )

        val facet = Facet(name = "Work", position = 0).withLandscape(overrides)

        assertEquals(overrides, facet.landscape)
        assertEquals(LandscapeOverrides(), Facet(name = "Plain", position = 1).landscape)
    }
}
