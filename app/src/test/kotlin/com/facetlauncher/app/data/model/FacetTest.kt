package com.facetlauncher.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FacetTest {

    @Test
    fun `placementTarget names the facet's own list`() {
        // Given a facet
        val facet = Facet(id = 4, name = "Work", position = 0)

        // Then both of its lists are its own, owned by its id
        assertEquals(PlacementTarget(PlacementList.DOCK, 4), facet.placementTarget(PlacementList.DOCK))
        assertEquals(PlacementTarget(PlacementList.FAVORITES, 4), facet.placementTarget(PlacementList.FAVORITES))
    }

    @Test
    fun `placementTarget is the launcher-wide list when there is no facet`() {
        val facet: Facet? = null

        assertEquals(PlacementTarget.GlobalDock, facet.placementTarget(PlacementList.DOCK))
        assertEquals(PlacementTarget.GlobalFavorites, facet.placementTarget(PlacementList.FAVORITES))
    }

    @Test
    fun `a new facet starts from the launcher's built-in look`() {
        val facet = Facet(name = "Plain", position = 0)

        assertEquals(LauncherSettings().appRowPosition, facet.appRowPosition)
        assertEquals(LauncherSettings().appRowPresentation, facet.appRowPresentation)
        assertEquals(LauncherSettings().appListLayout, facet.appListLayout)
        assertEquals(LauncherSettings().dockDisplayMode, facet.dockDisplayMode)
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
        assertEquals(AppRowPosition.CENTER, resolveLandscape(null, AppRowPosition.RIGHT, false, AppRowPosition.CENTER, AppRowPosition.LEFT))
    }

    @Test
    fun `resolveLandscape ends at the launcher's portrait value with nothing set for landscape`() {
        assertEquals(AppRowPosition.LEFT, resolveLandscape(null, AppRowPosition.RIGHT, false, null, AppRowPosition.LEFT))
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
            appRowPosition = AppRowPosition.CENTER,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            appListColumnAlignment = AppListColumnAlignment.MIRRORED,
        )

        val facet = Facet(name = "Work", position = 0).withLandscape(overrides)

        assertEquals(overrides, facet.landscape)
        assertEquals(LandscapeOverrides(), Facet(name = "Plain", position = 1).landscape)
    }
}
