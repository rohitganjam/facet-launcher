package com.facetlauncher.app.domain

import com.facetlauncher.app.data.minimalBackupSettings
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupNativeStackPage
import com.facetlauncher.app.data.model.BackupWidgetPlacement
import com.facetlauncher.app.data.model.CURRENT_BACKUP_VERSION
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
}
