package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.ClockStackItemKind
import com.facetlauncher.app.data.widgetItem
import com.facetlauncher.app.data.clockItem
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.LandscapeOverrides
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolvedFacetStyleTest {

    private val settings = LauncherSettings(
        clockTemplateId = ClockTemplateId.RULE_MERIDIEM,
        clockAlignment = ClockAlignment.RIGHT,
        clockScale = 1.2f,
        clockZoneHeightDp = 300f,
        use24HourTime = true,
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

        // Then every field is the launcher's
        assertEquals(ClockTemplateId.RULE_MERIDIEM, style.clock.templateId)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.portrait.alignment)
        assertEquals(1.2f, style.clockPlacement.portrait.scale, 0f)
        assertEquals(300f, style.clockPlacement.portrait.zoneHeightDp)
        assertEquals(true, style.clock.use24HourTime)
        assertEquals(AppRowPosition.CENTER, style.appList.rowPosition)
        assertEquals(AppListLayout.GRID, style.appList.layout)
        assertEquals(DockDisplayMode.ICONS, style.dockDisplayMode)
    }

    @Test
    fun `a facet not overriding the clock keeps the launcher's clock style even if its own columns differ`() {
        val style = resolveFacetStyle(
            facet { it.copy(overrideClock = false, clockTemplateId = ClockTemplateId.DATE_FORWARD) },
            settings,
        )

        assertEquals(ClockTemplateId.RULE_MERIDIEM, style.clock.templateId)
    }

    @Test
    fun `a facet overriding the clock supplies the clock style but not the placement`() {
        // Given a facet that overrides the clock, with placement columns that differ from the launcher's but not owned
        val style = resolveFacetStyle(
            facet {
                it.copy(
                    overrideClock = true,
                    clockTemplateId = ClockTemplateId.DATE_FORWARD,
                    clockAlignment = ClockAlignment.LEFT,
                    clockScale = 0.5f,
                    use24HourTime = false,
                )
            },
            settings,
        )

        // Then the style is the facet's, and the placement is still the launcher's fallback
        assertEquals(ClockTemplateId.DATE_FORWARD, style.clock.templateId)
        assertEquals(false, style.clock.use24HourTime)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.portrait.alignment)
        assertEquals(1.2f, style.clockPlacement.portrait.scale, 0f)
    }

    @Test
    fun `a facet that owns its placement uses it whether or not it overrides the clock`() {
        for (overriding in listOf(true, false)) {
            val style = resolveFacetStyle(
                facet { it.copy(overrideClock = overriding, clockPlacementOwn = true, clockAlignment = ClockAlignment.LEFT, clockScale = 0.5f, clockZoneHeightDp = 120f) },
                settings,
            )

            assertEquals(ClockAlignment.LEFT, style.clockPlacement.portrait.alignment)
            assertEquals(0.5f, style.clockPlacement.portrait.scale, 0f)
            assertEquals(120f, style.clockPlacement.portrait.zoneHeightDp)
        }
    }

    @Test
    fun `a facet that owns its placement but never dragged its divider keeps the natural position, not the launcher's height`() {
        val style = resolveFacetStyle(facet { it.copy(clockPlacementOwn = true, clockZoneHeightDp = null) }, settings)

        assertEquals(null, style.clockPlacement.portrait.zoneHeightDp)
    }

    @Test
    fun `a facet that does not own its placement shows the launcher's, in both orientations, whatever its own columns say`() {
        val withLandscape = settings.copy(landscape = LandscapeOverrides(clockScale = 1.5f))
        val style = resolveFacetStyle(facet { it.copy(clockPlacementOwn = false, clockScale = 0.5f, landscapeClockScale = 0.9f) }, withLandscape)

        assertEquals(1.2f, style.clockPlacement.portrait.scale, 0f)
        assertEquals(1.5f, style.clockPlacement.landscape.scale, 0f)
    }

    @Test
    fun `an owning facet's landscape clock values win, then its portrait ones, never the launcher's landscape`() {
        val withLandscapeScale = settings.copy(landscape = LandscapeOverrides(clockScale = 2.0f, clockAlignment = ClockAlignment.CENTER))
        val style = resolveFacetStyle(
            facet { it.copy(clockPlacementOwn = true, clockScale = 0.5f, clockAlignment = ClockAlignment.RIGHT, landscapeClockScale = 0.9f) },
            withLandscapeScale,
        )

        assertEquals(0.9f, style.clockPlacement.landscape.scale, 0f)
        assertEquals(ClockAlignment.RIGHT, style.clockPlacement.landscape.alignment)
    }

    @Test
    fun `list alignment resolves each field through its sentinel, then landscape over it`() {
        val withLandscape = settings.copy(landscape = LandscapeOverrides(appRowPosition = AppRowPosition.RIGHT))
        val inherits = resolveFacetStyle(facet { it.copy(appRowPosition = AppRowPosition.LAUNCHER_DEFAULT) }, withLandscape)
        val explicit = resolveFacetStyle(facet { it.copy(appRowPosition = AppRowPosition.LEFT) }, withLandscape)
        val ownLandscape = resolveFacetStyle(facet { it.copy(landscapeAppRowPosition = AppRowPosition.CENTER) }, withLandscape)

        assertEquals(AppRowPosition.CENTER, inherits.listAlignment.portrait.rowPosition)
        assertEquals(AppRowPosition.RIGHT, inherits.listAlignment.landscape.rowPosition)
        // A facet's explicit portrait value beats the launcher's landscape one
        assertEquals(AppRowPosition.LEFT, explicit.listAlignment.landscape.rowPosition)
        assertEquals(AppRowPosition.CENTER, ownLandscape.listAlignment.landscape.rowPosition)
    }

    @Test
    fun `the hosted clock widget is the facet's own and is not gated by the clock override`() {
        val withWidget = facet { it.copy(overrideClock = false, clockStack = listOf(widgetItem(41, widthDp = 160, heightDp = 90))) }

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
    fun `list content mode follows overrideApps but look fields ignore it`() {
        val inheriting = resolveFacetStyle(
            facet { it.copy(overrideApps = false, listContentMode = ListContentMode.RECENTS, appRowPosition = AppRowPosition.LEFT) },
            settings,
        )
        val overriding = resolveFacetStyle(
            facet { it.copy(overrideApps = true, listContentMode = ListContentMode.RECENTS) },
            settings,
        )

        // Look fields use their own sentinel, independent of overrideApps
        assertEquals(ListContentMode.FAVORITES, inheriting.appList.contentMode)
        assertEquals(AppRowPosition.LEFT, inheriting.appList.rowPosition)
        assertEquals(ListContentMode.RECENTS, overriding.appList.contentMode)
    }

    @Test
    fun `a look field at its LAUNCHER_DEFAULT sentinel inherits the launcher's value`() {
        val style = resolveFacetStyle(
            facet { it.copy(appRowPosition = AppRowPosition.LAUNCHER_DEFAULT, appListLayout = AppListLayout.TWO_COLUMN) },
            settings,
        )

        assertEquals(AppRowPosition.CENTER, style.appList.rowPosition)
        assertEquals(AppListLayout.TWO_COLUMN, style.appList.layout)
    }

    @Test
    fun `dock display mode uses the facet's value unless it is the sentinel`() {
        val own = resolveFacetStyle(facet { it.copy(dockDisplayMode = DockDisplayMode.TEXT) }, settings)
        val inherited = resolveFacetStyle(facet { it.copy(dockDisplayMode = DockDisplayMode.LAUNCHER_DEFAULT) }, settings)

        assertEquals(DockDisplayMode.TEXT, own.dockDisplayMode)
        assertEquals(DockDisplayMode.ICONS, inherited.dockDisplayMode)
    }

    @Test
    fun `calendar display follows overrideCalendar`() {
        val style = resolveFacetStyle(
            facet { it.copy(overrideCalendar = true, showCalendarIndicator = false, calendarItemsToShow = 2) },
            settings,
        )
        val inherited = resolveFacetStyle(facet { it.copy(overrideCalendar = false, calendarItemsToShow = 2) }, settings)

        assertEquals(CalendarDisplayOptions(showIndicator = false, itemsToShow = 2), style.calendarDisplay)
        assertEquals(CalendarDisplayOptions(settings.showCalendarIndicator, settings.calendarItemsToShow), inherited.calendarDisplay)
    }

    @Test
    fun `no facet resolves to a stack of just the launcher clock`() {
        // When the style is resolved for the launcher-wide view
        val style = resolveFacetStyle(null, settings)

        // Then there is one clock page, so a preview draws a clock
        assertEquals(listOf(ClockStackItemKind.CLOCK), style.clockStack.map { it.kind })
    }
}
