package com.facetlauncher.app.domain

import com.facetlauncher.app.data.local.WidgetPlacementEntity
import com.facetlauncher.app.data.model.BackupNativeStackPage
import com.facetlauncher.app.data.model.NativeWidgetConfig
import com.facetlauncher.app.data.model.NativeWidgetType
import com.facetlauncher.app.data.model.WeatherCity
import com.facetlauncher.app.data.model.WidgetWeatherLocation
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.model.encode
import com.facetlauncher.app.data.clockItem
import com.facetlauncher.app.data.clockItemWith
import com.facetlauncher.app.data.model.ClockCalendarConfig
import com.facetlauncher.app.data.model.ClockTemplateId
import com.facetlauncher.app.data.model.ClockWidgetConfig
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.decodeNativeWidgetConfig
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.WeatherIconStyle
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.BackupAppEntry
import com.facetlauncher.app.data.model.BackupGridCells
import com.facetlauncher.app.data.model.PlacementList
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupMappingTest {

    private val facet = Facet(name = "Work", position = 0)

    @Test
    fun `a facet's landscape row position and column alignment survive a backup round trip`() {
        // Given a facet with landscape values for the two list alignments portrait also backs up
        val original = facet.copy(landscapeAppRowPosition = AppRowPosition.CENTER, landscapeAppListColumnAlignment = AppListColumnAlignment.MIRRORED)

        // When it goes out and comes back
        val restored = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        // Then the landscape values are kept
        assertEquals(AppRowPosition.CENTER, restored.landscapeAppRowPosition)
        assertEquals(AppListColumnAlignment.MIRRORED, restored.landscapeAppListColumnAlignment)
    }

    @Test
    fun `a facet's landscape switch survives a backup round trip`() {
        val restored = facet.copy(allowLandscape = false).toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        assertEquals(false, restored.allowLandscape)
    }

    @Test
    fun `a facet without landscape values restores with none`() {
        val restored = facet.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        assertNull(restored.landscapeAppRowPosition)
        assertNull(restored.landscapeAppListColumnAlignment)
    }

    @Test
    fun `an unrecognized landscape name in a backup is just not set`() {
        val backup = facet.toBackupFacet(favorites = emptyList(), dockApps = emptyList())
            .copy(landscapeAppRowPosition = "NOT_A_REAL_VALUE", landscapeAppListColumnAlignment = "ALSO_NOT_REAL")

        val restored = backup.toFacet()

        assertNull(restored.landscapeAppRowPosition)
        assertNull(restored.landscapeAppListColumnAlignment)
    }

    @Test
    fun `a facet's clock and list settings survive a backup round trip`() {
        // Given a facet with every clock and list field the first backup format left out
        val original = facet.copy(
            clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 210f,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            landscapeClockAlignment = ClockAlignment.CENTER, landscapeClockZoneHeightDp = 300f,
            landscapeAppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
            appListShrinkToFit = com.facetlauncher.app.data.model.AppListShrinkToFit.ON,
        )

        // When it goes out and comes back
        val restored = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        // Then none of them is reset
        assertEquals(original, restored)
    }

    @Test
    fun `a clock's own settings go out in the backup without calendar ids and come back as they were`() {
        // Given a facet whose clock has its own look, weather and a calendar choice
        val oslo = WidgetWeatherLocation.City(WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo"))
        val config = ClockWidgetConfig(
            templateId = ClockTemplateId.entries.last(), weatherIconStyle = WeatherIconStyle.MONOCHROME, weatherLocation = oslo,
            calendar = ClockCalendarConfig(itemsToShow = 2, calendarIds = setOf("3", "4")),
        )
        val original = facet.copy(clockStack = listOf(clockItemWith(config)))

        // When it goes out
        val backup = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList())

        // Then the clock's settings are carried, calendar ids left out as they are device-specific, and decode to the same values
        val expected = config.copy(calendar = config.calendar.copy(calendarIds = null))
        assertEquals(expected, decodeNativeWidgetConfig(backup.clockConfig).clock)
    }

    @Test
    fun `a clock's box goes out in the backup`() {
        val clockPage = clockItem().copy(widthDp = 200, heightDp = 80, landscapeWidthDp = 320, landscapeHeightDp = 130)

        val backup = facet.copy(clockStack = listOf(clockPage)).toBackupFacet(favorites = emptyList(), dockApps = emptyList())

        assertEquals(listOf(200, 80, 320, 130), listOf(backup.clockWidthDp, backup.clockHeightDp, backup.clockLandscapeWidthDp, backup.clockLandscapeHeightDp))
    }

    @Test
    fun `a backup facet written before these fields existed restores to the defaults`() {
        // Given a backup facet carrying only what the first format had
        val old = facet.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).let {
            BackupFacet(
                name = it.name, position = it.position, overrideClock = it.overrideClock, clockTemplateId = it.clockTemplateId,
                clockFontOption = it.clockFontOption, clockColorOption = it.clockColorOption, use24HourTime = it.use24HourTime,
                clockShowMeridiem = it.clockShowMeridiem, overrideApps = it.overrideApps, appRowPosition = it.appRowPosition,
                appRowPresentation = it.appRowPresentation, listContentMode = it.listContentMode, appsToShowCount = it.appsToShowCount,
                overridingFavorites = it.overridingFavorites, overrideCalendar = it.overrideCalendar,
                showAllDayEvents = it.showAllDayEvents, favorites = emptyList(),
            )
        }

        // When restored
        val restored = old.toFacet()

        // Then the new fields take their defaults
        assertEquals(ClockAlignment.LEFT, restored.clockAlignment)
        assertNull(restored.clockZoneHeightDp)
        assertEquals(AppListVerticalAlignment.BOTTOM, restored.appListVerticalAlignment)
        assertNull(restored.landscapeClockAlignment)
    }

    @Test
    fun `a native widget's own location is backed up and its calendar ids are not`() {
        // Given a Hub Agenda set to calendar ids of this device and a Weather widget set to a city
        val oslo = WeatherCity("Oslo", 59.91, 10.75, "Europe/Oslo")
        val agenda = WidgetPlacementEntity(-1, "", "", 0, 0, 4, 2, nativeWidgetType = NativeWidgetType.AGENDA, nativeWidgetConfig = NativeWidgetConfig(calendarIds = setOf("5")).encode())
        val weather = WidgetPlacementEntity(-2, "", "", 2, 0, 4, 2, nativeWidgetType = NativeWidgetType.WEATHER, nativeWidgetConfig = NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.City(oslo)).encode())

        // When mapped to the backup
        val backedAgenda = agenda.toBackupPlacement()
        val backedWeather = weather.toBackupPlacement()

        // Then the calendar ids are dropped and the city is kept
        assertNull(backedAgenda.nativeWidgetConfig)
        assertEquals(WidgetWeatherLocation.City(oslo), decodeNativeWidgetConfig(backedWeather.nativeWidgetConfig).weatherLocation)
    }

    @Test
    fun `a restored Home page keeps the location it was backed up with`() {
        // Given a backed-up Weather page set to current location
        val page = BackupNativeStackPage(type = "WEATHER", nativeWidgetConfig = NativeWidgetConfig(weatherLocation = WidgetWeatherLocation.Current).encode())

        // When restored as a stack item
        val item = page.toStackItem()

        // Then the item follows the same setting
        assertEquals(WidgetWeatherLocation.Current, decodeNativeWidgetConfig(item?.nativeWidgetConfig).weatherLocation)
    }

    @Test
    fun `an app placement's grid cells survive a backup round trip`() {
        // Given an app moved on the grid in portrait only
        val entity = AppPlacementEntity(
            list = PlacementList.FAVORITES, packageName = "a", activityName = ".A", position = 2, userId = 0,
            portraitRow = 1, portraitColumn = 3,
        )

        // When it goes into a backup and back
        val entry = entity.toBackupEntry()
        val restored = entry.toAppPlacementEntity(PlacementList.FAVORITES)

        // Then the cells are kept, and a never-moved app carries none
        assertEquals(BackupGridCells(portraitRow = 1, portraitColumn = 3), entry.gridCells)
        assertEquals(1, restored.portraitRow)
        assertEquals(3, restored.portraitColumn)
        assertNull(restored.landscapeRow)
        assertNull(entity.copy(portraitRow = null, portraitColumn = null).toBackupEntry().gridCells)
    }

    @Test
    fun `a folder placement's grid cells are exported and a backup without cells still reads`() {
        val placement = FolderPlacementEntity(list = PlacementList.FAVORITES, folderId = 7L, position = 0, landscapeRow = 2, landscapeColumn = 0)

        assertEquals(BackupGridCells(landscapeRow = 2, landscapeColumn = 0), placement.toBackupPlacement(mapOf(7L to 0))?.gridCells)
        assertNull(Json.decodeFromString<BackupAppEntry>("""{"packageName":"a","activityName":".A","position":0}""").gridCells)
    }
}
