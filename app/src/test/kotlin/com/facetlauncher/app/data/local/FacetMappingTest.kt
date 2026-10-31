package com.facetlauncher.app.data.local

import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.ListContentMode
import org.junit.Assert.assertEquals
import org.junit.Test

class FacetMappingTest {

    private fun fieldNames(type: Class<*>): Set<String> =
        type.declaredFields.filterNot { it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers) }.map { it.name }.toSet()

    @Test
    fun `every FacetEntity column has a Facet field`() {
        // Given the entity's columns and the model's fields
        // (the five hosted-widget columns are retired: that data lives in clock_stack_items, loaded as Facet.clockStack)
        val retiredWidgetColumns = setOf(
            "clockWidgetAppWidgetId", "clockWidgetWidthDp", "clockWidgetHeightDp",
            "landscapeClockWidgetWidthDp", "landscapeClockWidgetHeightDp",
        )
        val entityColumns = fieldNames(FacetEntity::class.java) - "selectedCalendarIdsCsv" - retiredWidgetColumns
        val facetFields = fieldNames(Facet::class.java) - "selectedCalendarIds" - "clockStack"

        // Then they match, so a column added to the entity can't be silently dropped by the mapper
        assertEquals(entityColumns, facetFields)
    }

    @Test
    fun `a facet with every group set round-trips through the entity unchanged`() {
        // Given a facet with non-default values across the clock, landscape, apps, dock and calendar groups
        val facet = Facet(
            id = 7,
            name = "Work",
            position = 2,
            overrideClock = true,
            clockTemplateId = ClockTemplateId.DATE_FORWARD,
            clockAlignment = ClockAlignment.RIGHT,
            clockZoneHeightDp = 210f,
            clockScale = 1.1f,
            landscapeClockAlignment = ClockAlignment.CENTER,
            landscapeClockZoneHeightDp = 300f,
            landscapeClockScale = 0.9f,
            overrideApps = true,
            appRowPosition = AppRowPosition.CENTER,
            landscapeAppRowPosition = AppRowPosition.RIGHT,
            landscapeAppListVerticalAlignment = AppListVerticalAlignment.LAUNCHER_DEFAULT,
            landscapeAppListColumnAlignment = AppListColumnAlignment.LAUNCHER_DEFAULT,
            listContentMode = ListContentMode.MOST_USED,
            appsToShowCount = 5,
            appListLayout = AppListLayout.GRID,
            overridingFavorites = true,
            overrideDock = true,
            dockDisplayMode = DockDisplayMode.TEXT,
            overrideCalendar = true,
            showAllDayEvents = false,
            showCalendarIndicator = false,
            calendarItemsToShow = 2,
            selectedCalendarIds = setOf("3", "4"),
        )

        // When mapped to the Room row and back
        val result = facet.toEntity().toFacet()

        // Then nothing is lost
        assertEquals(facet, result)
    }

    @Test
    fun `calendar ids are stored comma-joined and null means every calendar`() {
        val selected = Facet(name = "A", position = 0, selectedCalendarIds = setOf("1", "2")).toEntity()
        val all = Facet(name = "A", position = 0, selectedCalendarIds = null).toEntity()

        assertEquals("1,2", selected.selectedCalendarIdsCsv)
        assertEquals(null, all.selectedCalendarIdsCsv)
        assertEquals(null, all.toFacet().selectedCalendarIds)
    }
}
