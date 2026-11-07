package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.widgetItem
import com.facetlauncher.app.data.clockItem
import com.facetlauncher.app.data.clockItemWith
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.DEFAULT_CLOCK_SCALE
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolvedFacetStyleTest {

    private val settings = LauncherSettings(
        clockAlignment = ClockAlignment.RIGHT,
        clockZoneHeightDp = 300f,
        listContentMode = ListContentMode.FAVORITES,
        appRowPosition = AppRowPosition.CENTER,
        appListLayout = AppListLayout.GRID,
        dockDisplayMode = DockDisplayMode.ICONS,
    )

    private fun facet(block: (Facet) -> Facet = { it }) =
        block(Facet(id = 1L, name = "Facet 1", position = 0))

    @Test
    fun `no facet resolves to the launcher-wide values`() {
        // Given no facet
        // When resolving
        val style = resolveFacetStyle(null, settings)

        // Then the clock is the built-in one and every other field is the launcher's
        assertEquals(ClockWidgetConfig(), style.clock)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.portrait.alignment)
        assertEquals(DEFAULT_CLOCK_SCALE, style.clockPlacement.portrait.scale, 0f)
        assertEquals(300f, style.clockPlacement.portrait.zoneHeightDp)
        assertEquals(AppRowPosition.CENTER, style.appList.rowPosition)
        assertEquals(AppListLayout.GRID, style.appList.layout)
        assertEquals(DockDisplayMode.ICONS, style.dockDisplayMode)
    }

    @Test
    fun `a facet's clock supplies the clock style but not the placement`() {
        // Given a facet whose clock is set to its own look, with placement columns that differ from the launcher's but not owned
        val style = resolveFacetStyle(
            facet {
                it.copy(
                    clockStack = listOf(clockItemWith(ClockWidgetConfig(templateId = ClockTemplateId.DATE_FORWARD, use24HourTime = true))),
                    clockAlignment = ClockAlignment.LEFT,
                )
            },
            settings,
        )

        // Then the style is the clock's, and the placement is still the launcher's fallback
        assertEquals(ClockTemplateId.DATE_FORWARD, style.clock.templateId)
        assertEquals(true, style.clock.use24HourTime)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.portrait.alignment)
        assertEquals(DEFAULT_CLOCK_SCALE, style.clockPlacement.portrait.scale, 0f)
    }

    @Test
    fun `a facet that owns its placement uses it`() {
        val style = resolveFacetStyle(
            facet { it.copy(clockPlacementOwn = true, clockAlignment = ClockAlignment.LEFT, clockZoneHeightDp = 120f) },
            settings,
        )

        assertEquals(ClockAlignment.LEFT, style.clockPlacement.portrait.alignment)
        assertEquals(DEFAULT_CLOCK_SCALE, style.clockPlacement.portrait.scale, 0f)
        assertEquals(120f, style.clockPlacement.portrait.zoneHeightDp)
    }

    @Test
    fun `a facet that owns its placement but never dragged its divider keeps the natural position, not the launcher's height`() {
        val style = resolveFacetStyle(facet { it.copy(clockPlacementOwn = true, clockZoneHeightDp = null) }, settings)

        assertEquals(null, style.clockPlacement.portrait.zoneHeightDp)
    }

    @Test
    fun `a facet that does not own its placement shows the launcher's, in both orientations, whatever its own columns say`() {
        val withLandscape = settings.copy(landscape = LandscapeOverrides(clockZoneHeightDp = 310f))
        val style = resolveFacetStyle(facet { it.copy(clockPlacementOwn = false, clockZoneHeightDp = 55f) }, withLandscape)

        assertEquals(settings.clockZoneHeightDp, style.clockPlacement.portrait.zoneHeightDp)
        assertEquals(310f, style.clockPlacement.landscape.zoneHeightDp)
        // The size is the clock page's saved box, so the placement only carries the default scale for a clock without one
        assertEquals(DEFAULT_CLOCK_SCALE, style.clockPlacement.landscape.scale, 0f)
    }

    @Test
    fun `an owning facet's landscape clock values win, then its portrait ones, never the launcher's landscape`() {
        val withLandscapeScale = settings.copy(landscape = LandscapeOverrides(clockAlignment = ClockAlignment.CENTER))
        val style = resolveFacetStyle(
            facet { it.copy(clockPlacementOwn = true, clockAlignment = ClockAlignment.RIGHT) },
            withLandscapeScale,
        )

        assertEquals(DEFAULT_CLOCK_SCALE, style.clockPlacement.landscape.scale, 0f)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.landscape.alignment)
    }

    @Test
    fun `shrink list to fit is the facet's own, and the launcher's only with no facet`() {
        // Given the launcher shrinking its list
        val shrinking = settings.copy(appListShrinkToFit = com.facetlauncher.app.data.model.AppListShrinkToFit.ON)

        // When a facet leaves shrinking off, and another turns it on
        val off = resolveFacetStyle(facet(), shrinking)
        val on = resolveFacetStyle(facet { it.copy(appListShrinkToFit = com.facetlauncher.app.data.model.AppListShrinkToFit.ON) }, shrinking)

        // Then each resolves to its own value, and only no facet resolves to the launcher's
        assertEquals(com.facetlauncher.app.data.model.AppListShrinkToFit.OFF, off.appList.shrinkToFit)
        assertEquals(com.facetlauncher.app.data.model.AppListShrinkToFit.ON, on.appList.shrinkToFit)
        assertEquals(com.facetlauncher.app.data.model.AppListShrinkToFit.ON, resolveFacetStyle(null, shrinking).appList.shrinkToFit)
    }

    @Test
    fun `list alignment is the facet's own portrait value, with landscape its own twin else portrait`() {
        val withLandscape = settings.copy(landscape = LandscapeOverrides(appRowPosition = AppRowPosition.RIGHT))
        val samePortrait = resolveFacetStyle(facet { it.copy(appRowPosition = AppRowPosition.LEFT) }, withLandscape)
        val ownLandscape = resolveFacetStyle(facet { it.copy(landscapeAppRowPosition = AppRowPosition.CENTER) }, withLandscape)

        // Never the launcher's value, landscape included
        assertEquals(AppRowPosition.LEFT, samePortrait.listAlignment.portrait.rowPosition)
        assertEquals(AppRowPosition.LEFT, samePortrait.listAlignment.landscape.rowPosition)
        assertEquals(AppRowPosition.CENTER, ownLandscape.listAlignment.landscape.rowPosition)
    }

    @Test
    fun `list alignment with no facet is the launcher's, landscape twin over portrait`() {
        val withLandscape = settings.copy(landscape = LandscapeOverrides(appRowPosition = AppRowPosition.RIGHT))

        val style = resolveFacetStyle(null, withLandscape)

        assertEquals(AppRowPosition.CENTER, style.listAlignment.portrait.rowPosition)
        assertEquals(AppRowPosition.RIGHT, style.listAlignment.landscape.rowPosition)
    }

    @Test
    fun `the hosted clock widget is the facet's own`() {
        val withWidget = facet { it.copy(clockStack = listOf(widgetItem(41, widthDp = 160, heightDp = 90))) }

        val style = resolveFacetStyle(withWidget, settings)

        assertEquals(41, style.clockStack.single().appWidgetId)
        assertEquals(160 to 90, style.clockStack.single().sizeDp.portrait)
    }

    @Test
    fun `the hosted widget size in landscape is its own per dimension, else the portrait one`() {
        val widget = facet { it.copy(clockStack = listOf(widgetItem(41, widthDp = 160, heightDp = 90))) }
        val landscapeWidthOnly = facet { it.copy(clockStack = listOf(widgetItem(41, widthDp = 160, heightDp = 90, landscapeWidthDp = 400))) }

        assertEquals(160 to 90, resolveFacetStyle(widget, settings).clockStack.single().sizeDp.landscape)
        assertEquals(400 to 90, resolveFacetStyle(landscapeWidthOnly, settings).clockStack.single().sizeDp.landscape)
    }

    @Test
    fun `there is no stack page without items, and no size before a resize`() {
        assertEquals(emptyList<ResolvedClockItem>(), resolveFacetStyle(facet { it.copy(clockStack = emptyList()) }, settings).clockStack)
        assertEquals(null, resolveFacetStyle(facet { it.copy(clockStack = listOf(widgetItem(41))) }, settings).clockStack.single().sizeDp.portrait)
    }

    @Test
    fun `limitedTo keeps every page with pro and only the first without`() {
        val pages = resolveFacetStyle(facet { it.copy(clockStack = listOf(clockItem(position = 0), widgetItem(41, position = 1), widgetItem(42, position = 2))) }, settings).clockStack

        assertEquals(3, pages.limitedTo(isPro = true).size)
        assertEquals(listOf(ClockStackItemKind.CLOCK), pages.limitedTo(isPro = false).map { it.kind })
    }

    @Test
    fun `the native clock page and the widget page keep the stack's order`() {
        val stacked = facet { it.copy(clockStack = listOf(clockItem(position = 0), widgetItem(41, position = 1))) }

        val kinds = resolveFacetStyle(stacked, settings).clockStack.map { it.kind to it.appWidgetId }

        assertEquals(listOf(ClockStackItemKind.CLOCK to null, ClockStackItemKind.WIDGET to 41), kinds)
    }

    @Test
    fun `a page's landscape alignment is its own, else the portrait one`() {
        val item = widgetItem(41).copy(alignment = ClockAlignment.CENTER)
        val own = item.copy(landscapeAlignment = ClockAlignment.RIGHT)

        assertEquals(ClockAlignment.CENTER, resolveFacetStyle(facet { it.copy(clockStack = listOf(item)) }, settings).clockStack.single().alignment.landscape)
        assertEquals(ClockAlignment.RIGHT, resolveFacetStyle(facet { it.copy(clockStack = listOf(own)) }, settings).clockStack.single().alignment.landscape)
    }

    @Test
    fun `the app list is the facet's own content mode and look, never the launcher's`() {
        val style = resolveFacetStyle(
            facet { it.copy(listContentMode = ListContentMode.RECENTS, appRowPosition = AppRowPosition.LEFT, appListLayout = AppListLayout.TWO_COLUMN) },
            settings,
        )

        // The launcher's settings say Favorites, Center and Grid
        assertEquals(ListContentMode.RECENTS, style.appList.contentMode)
        assertEquals(AppRowPosition.LEFT, style.appList.rowPosition)
        assertEquals(AppListLayout.TWO_COLUMN, style.appList.layout)
    }

    @Test
    fun `dock display mode is the facet's own, and the launcher's only with no facet`() {
        val own = resolveFacetStyle(facet { it.copy(dockDisplayMode = DockDisplayMode.TEXT) }, settings)
        val noFacet = resolveFacetStyle(null, settings)

        assertEquals(DockDisplayMode.TEXT, own.dockDisplayMode)
        assertEquals(DockDisplayMode.ICONS, noFacet.dockDisplayMode)
    }

    @Test
    fun `calendar display follows the clock's own calendar`() {
        val style = resolveFacetStyle(
            facet { it.copy(clockStack = listOf(clockItemWith(ClockWidgetConfig(calendar = ClockCalendarConfig(showIndicator = false, itemsToShow = 2))))) },
            settings,
        )
        val defaults = resolveFacetStyle(facet(), settings)

        assertEquals(CalendarDisplayOptions(showIndicator = false, itemsToShow = 2), style.calendarDisplay)
        assertEquals(CalendarDisplayOptions(), defaults.calendarDisplay)
    }

    @Test
    fun `no facet resolves to a stack of just the launcher clock`() {
        // When the style is resolved for the launcher-wide view
        val style = resolveFacetStyle(null, settings)

        // Then there is one clock page, so a preview draws a clock
        assertEquals(listOf(ClockStackItemKind.CLOCK), style.clockStack.map { it.kind })
    }

    @Test
    fun `a clock's own weather location applies, and none follows Weather settings`() {
        // Given a clock with its own weather location
        val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))

        // Then it shows, and a clock with none, or no facet, follows Weather settings
        assertEquals(oslo, resolveFacetStyle(facet { it.copy(clockStack = listOf(clockItemWith(ClockWidgetConfig(weatherLocation = oslo)))) }, settings).clock.weatherLocation)
        assertEquals(null, resolveFacetStyle(facet(), settings).clock.weatherLocation)
        assertEquals(null, resolveFacetStyle(null, settings).clock.weatherLocation)
    }
}
