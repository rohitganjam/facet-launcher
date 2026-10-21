package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.FacetEntity
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.CalendarDisplayOptions
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
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

    private fun facet(block: (FacetEntity) -> FacetEntity = { it }) =
        block(FacetEntity(id = 1L, name = "Facet 1", position = 0))

    @Test
    fun `no facet resolves to the launcher-wide values`() {
        // Given no facet
        // When resolving
        val style = resolveFacetStyle(null, settings)

        // Then every field is the launcher's
        assertEquals(ClockTemplateId.RULE_MERIDIEM, style.clock.templateId)
        assertEquals(ClockAlignment.RIGHT, style.clock.alignment)
        assertEquals(1.2f, style.clock.scale, 0f)
        assertEquals(300f, style.clock.zoneHeightDp)
        assertEquals(true, style.clock.use24HourTime)
        assertEquals(AppRowPosition.CENTER, style.appList.rowPosition)
        assertEquals(AppListLayout.GRID, style.appList.layout)
        assertEquals(DockDisplayMode.ICONS, style.dockDisplayMode)
    }

    @Test
    fun `a facet not overriding the clock keeps the launcher's clock even if its own columns differ`() {
        val style = resolveFacetStyle(
            facet { it.copy(overrideClock = false, clockTemplateId = ClockTemplateId.DATE_FORWARD, clockScale = 0.5f) },
            settings,
        )

        assertEquals(ClockTemplateId.RULE_MERIDIEM, style.clock.templateId)
        assertEquals(1.2f, style.clock.scale, 0f)
    }

    @Test
    fun `a facet overriding the clock supplies the whole clock block`() {
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

        assertEquals(ClockTemplateId.DATE_FORWARD, style.clock.templateId)
        assertEquals(ClockAlignment.LEFT, style.clock.alignment)
        assertEquals(0.5f, style.clock.scale, 0f)
        assertEquals(false, style.clock.use24HourTime)
    }

    @Test
    fun `an overriding facet that never dragged its divider falls back to the launcher's zone height`() {
        val style = resolveFacetStyle(facet { it.copy(overrideClock = true, clockZoneHeightDp = null) }, settings)

        assertEquals(300f, style.clock.zoneHeightDp)
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
}
