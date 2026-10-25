package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.ClockAlignment
import com.facetlauncher.app.data.model.ClockColorOption
import com.facetlauncher.app.data.model.ClockDateStyle
import com.facetlauncher.app.data.model.WeatherIconStyle
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
            clockAccentColorOption = ClockColorOption.ACCENT_SECONDARY, clockDateStyle = ClockDateStyle.CONDENSED,
            clockAlignment = ClockAlignment.RIGHT, clockZoneHeightDp = 210f, clockScale = 1.1f,
            appListVerticalAlignment = AppListVerticalAlignment.TOP,
            landscapeClockAlignment = ClockAlignment.CENTER, landscapeClockZoneHeightDp = 300f, landscapeClockScale = 0.9f,
            landscapeAppListVerticalAlignment = AppListVerticalAlignment.BOTTOM,
        )

        // When it goes out and comes back
        val restored = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList()).toFacet()

        // Then none of them is reset
        assertEquals(original, restored)
    }

    @Test
    fun `a facet's weather icon style survives a backup round trip and an unknown name falls back to coloured`() {
        // Given a facet with its own monochrome icons
        val original = facet.copy(clockWeatherIconStyle = WeatherIconStyle.MONOCHROME)

        // When it goes out and comes back
        val backup = original.toBackupFacet(favorites = emptyList(), dockApps = emptyList())

        // Then the style is kept, and a name this version doesn't know restores to the coloured default
        assertEquals(WeatherIconStyle.MONOCHROME, backup.toFacet().clockWeatherIconStyle)
        assertEquals(WeatherIconStyle.COLORED, backup.copy(clockWeatherIconStyle = "SKETCH").toFacet().clockWeatherIconStyle)
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
        assertEquals(0.8f, restored.clockScale)
        assertNull(restored.clockZoneHeightDp)
        assertEquals(AppListVerticalAlignment.LAUNCHER_DEFAULT, restored.appListVerticalAlignment)
        assertNull(restored.landscapeClockAlignment)
    }
}
