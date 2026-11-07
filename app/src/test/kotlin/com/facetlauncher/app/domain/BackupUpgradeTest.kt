package com.facetlauncher.app.domain

import com.facetlauncher.app.data.minimalBackupSettings
import com.facetlauncher.app.data.model.BackupAppEntry
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupNativeStackPage
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.model.decodeWeatherLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class BackupUpgradeTest {


    private fun placement(config: String?) = BackupWidgetPlacement("", "", 0, 0, 2, 2, "PERSONAL", 0, "WEATHER", config)

    private fun facet(clockWeather: String?, pageConfig: String?) = Facet(name = "Home", position = 0)
        .toBackupFacet(favorites = emptyList(), dockApps = emptyList())
        .copy(clockWeatherLocation = clockWeather, nativeStackPages = listOf(BackupNativeStackPage(type = "WEATHER", nativeWidgetConfig = pageConfig)))

    private fun bundle(version: Int, facet: BackupFacet, placement: BackupWidgetPlacement) = BackupBundle(
        backupVersion = version, exportedAtEpochMillis = 0L, settings = minimalBackupSettings(), facets = listOf(facet),
        dockApps = emptyList(), defaultFavoriteApps = emptyList(), widgetPlacements = listOf(placement),
    )

    private fun app(name: String) = BackupAppEntry(packageName = "com.example.$name", activityName = ".Main", position = 0)

    /** A version 5 bundle: the launcher has a dock, favorites and a look, and [facet] is the one facet. */
    private fun v5Bundle(facet: BackupFacet) = BackupBundle(
        backupVersion = 5, exportedAtEpochMillis = 0L,
        settings = minimalBackupSettings().copy(
            dockDisplayMode = "TEXT", appRowPosition = "RIGHT", appRowPresentation = "TEXT_ONLY", listContentMode = "RECENTS", appsToShowCount = 9,
            landscapeAppRowPosition = "CENTER",
        ),
        facets = listOf(facet), dockApps = listOf(app("dock")), defaultFavoriteApps = listOf(app("fav")), widgetPlacements = emptyList(),
    )

    private fun inheritingFacet() = Facet(name = "Home", position = 0)
        .toBackupFacet(favorites = listOf(app("stale-fav")), dockApps = listOf(app("stale-dock")))
        .copy(
            overrideApps = false, overridingFavorites = false, overrideDock = false,
            appRowPosition = "LAUNCHER_DEFAULT", appRowPresentation = "LAUNCHER_DEFAULT", appListVerticalAlignment = "LAUNCHER_DEFAULT",
            appListLayout = "LAUNCHER_DEFAULT", appListColumnAlignment = "LAUNCHER_DEFAULT", appListGridColumns = "LAUNCHER_DEFAULT",
            appListGridDisplayMode = "LAUNCHER_DEFAULT", appListShrinkToFit = "LAUNCHER_DEFAULT", dockDisplayMode = "LAUNCHER_DEFAULT",
        )

    @Test
    fun `a version 5 facet that inherited its Home gets the launcher's lists, content and look`() {
        // When a facet that overrode nothing is upgraded
        val facet = v5Bundle(inheritingFacet()).upgradedToCurrent().facets.single()

        // Then it holds the launcher's dock and favorites in place of its stale rows
        assertEquals(listOf(app("dock")), facet.dockApps)
        assertEquals(listOf(app("fav")), facet.favorites)
        // And the launcher's content mode, count and look, with no inherit marker left
        assertEquals("RECENTS", facet.listContentMode)
        assertEquals(9, facet.appsToShowCount)
        assertEquals("TEXT", facet.dockDisplayMode)
        assertEquals("RIGHT", facet.appRowPosition)
        assertEquals("TEXT_ONLY", facet.appRowPresentation)
        assertEquals(minimalBackupSettings().appListLayout, facet.appListLayout)
        assertEquals(null, facet.overrideDock)
        assertEquals(null, facet.overridingFavorites)
        assertEquals(null, facet.overrideApps)
    }

    @Test
    fun `a version 5 facet that overrode its dock, favorites and content keeps its own`() {
        // Given a facet that overrode all three, with its own look
        val own = Facet(name = "Work", position = 0, listContentMode = com.facetlauncher.app.data.model.ListContentMode.MOST_USED, appsToShowCount = 4)
            .toBackupFacet(favorites = listOf(app("own-fav")), dockApps = listOf(app("own-dock")))
            .copy(overrideApps = true, overridingFavorites = true, overrideDock = true)

        // When upgraded
        val facet = v5Bundle(own).upgradedToCurrent().facets.single()

        // Then its own values survive
        assertEquals(listOf(app("own-dock")), facet.dockApps)
        assertEquals(listOf(app("own-fav")), facet.favorites)
        assertEquals("MOST_USED", facet.listContentMode)
        assertEquals(4, facet.appsToShowCount)
    }

    @Test
    fun `a version 5 landscape value follows the launcher's only while the portrait value was inherited`() {
        // Given one facet whose portrait row position was inherited, and one with its own portrait value and no landscape one
        val inherited = v5Bundle(inheritingFacet()).upgradedToCurrent().facets.single()
        val explicit = v5Bundle(inheritingFacet().copy(appRowPosition = "LEFT")).upgradedToCurrent().facets.single()

        // Then the inheriting one takes the launcher's landscape value and the explicit one stays on its portrait one
        assertEquals("CENTER", inherited.landscapeAppRowPosition)
        assertEquals(null, explicit.landscapeAppRowPosition)
    }

    @Test
    fun `a version 3 backup has its widget settings rewritten as JSON`() {
        // Given a version 3 backup with an old-form setting on a Hub widget, a stack page and a facet's clock weather
        val old = bundle(3, facet("wx\tcurrent", "wx\tcurrent"), placement("wx\tcurrent"))

        // When upgraded
        val upgraded = old.upgradedToCurrent()

        // Then each one reads as current location in the current form, and the bundle is the current version
        assertEquals(CURRENT_BACKUP_VERSION, upgraded.backupVersion)
        assertEquals(WidgetWeatherLocation.Current, decodeNativeWidgetConfig(upgraded.widgetPlacements.single().nativeWidgetConfig).weatherLocation)
        assertEquals(WidgetWeatherLocation.Current, decodeNativeWidgetConfig(upgraded.facets.single().nativeStackPages.single().nativeWidgetConfig).weatherLocation)
        assertEquals(WidgetWeatherLocation.Current, decodeWeatherLocation(upgraded.facets.single().clockWeatherLocation))
    }

    @Test
    fun `a current backup is left exactly as it is`() {
        // Given a current backup whose settings are already JSON
        val json = """{"showAllDayEvents":false}"""
        val current = bundle(CURRENT_BACKUP_VERSION, facet(null, json), placement(json))

        // Then upgrading returns the same bundle
        assertSame(current, current.upgradedToCurrent())
    }

    @Test
    fun `settings that follow the launcher stay null`() {
        // Given a version 3 backup with no settings anywhere
        val upgraded = bundle(3, facet(null, null), placement(null)).upgradedToCurrent()

        // Then they are still null
        assertEquals(null, upgraded.widgetPlacements.single().nativeWidgetConfig)
        assertEquals(null, upgraded.facets.single().clockWeatherLocation)
        assertEquals(null, upgraded.facets.single().nativeStackPages.single().nativeWidgetConfig)
    }

    @Test
    fun `an older backup's clock gets the facet's own settings while the facet overrode the clock and the calendar`() {
        // Given a version 4 facet that overrode both, with a launcher that says otherwise
        val old = facet(null, null).copy(
            overrideClock = true, clockTemplateId = "DATE_FORWARD", use24HourTime = true, clockShowAlarm = false, clockShowWeather = true,
            overrideCalendar = true, showAllDayEvents = false, calendarItemsToShow = 2, selectedCalendarIds = listOf("9"),
        )
        val launcher = minimalBackupSettings().copy(clockTemplateId = "LIGHT_STACK", use24HourTime = false)

        // When upgraded
        val clock = decodeNativeWidgetConfig(bundle(4, old, placement(null)).copy(settings = launcher).upgradedToCurrent().facets.single().clockConfig).clock!!

        // Then the clock carries the facet's values
        assertEquals(ClockTemplateId.DATE_FORWARD, clock.templateId)
        assertEquals(true, clock.use24HourTime)
        assertEquals(false, clock.showAlarm)
        assertEquals(true, clock.showWeather)
        assertEquals(ClockCalendarConfig(showIndicator = true, itemsToShow = 2, showAllDay = false, calendarIds = setOf("9")), clock.calendar)
    }

    @Test
    fun `an older backup's clock gets the launcher's settings while the facet inherited them`() {
        // Given a version 4 facet that overrode nothing, and a launcher with its own look and accessories
        val launcher = minimalBackupSettings().copy(
            clockTemplateId = "RULE_MERIDIEM", use24HourTime = true, showNextAlarm = false, showBatteryPercentage = false, showAllDayEvents = false,
        )

        // When upgraded
        val clock = decodeNativeWidgetConfig(bundle(4, facet(null, null), placement(null)).copy(settings = launcher).upgradedToCurrent().facets.single().clockConfig).clock!!

        // Then the clock carries the launcher's values, and follows every calendar
        assertEquals(ClockTemplateId.RULE_MERIDIEM, clock.templateId)
        assertEquals(true, clock.use24HourTime)
        assertEquals(false, clock.showAlarm)
        assertEquals(false, clock.showBattery)
        assertEquals(false, clock.calendar.showAllDay)
        assertEquals(null, clock.calendar.calendarIds)
    }
}
