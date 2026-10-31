package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import java.lang.reflect.Modifier
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Fails when a field is added to [Facet] or [LauncherSettings] without either exporting it in the
 * backup or listing it below as deliberately left out, so a restore can't silently reset it.
 */
class BackupCoverageTest {

    private fun fieldNames(type: Class<*>): Set<String> =
        type.declaredFields.filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }.map { it.name }.toSet()

    @Test
    fun `every facet field is exported or deliberately left out`() {
        // Given the fields a facet row holds, minus those a backup cannot or should not carry
        val notBackedUp = setOf(
            "id", // a Room row id is not portable; the backup uses list order
            "clockStack", // its widgets are AppWidgetHost ids, which are not portable, so they are re-added by hand
        )
        val expected = fieldNames(Facet::class.java) - notBackedUp
        val exported = fieldNames(BackupFacet::class.java) - setOf("dockApps", "favorites", "dockFolderPlacements", "favoriteFolderPlacements")

        // Then the backup holds exactly those
        assertEquals(expected, exported)
    }

    @Test
    fun `every launcher setting is exported or deliberately left out`() {
        // Given the launcher settings, minus those a backup cannot or should not carry
        val notBackedUp = setOf(
            "activeFacetId", // a row id; replaced by activeFacetIndex
            "selectedCalendarIds", "calendarColors", // keyed by this device's calendar row ids
            "calendarPermissionRequested", "contactsPermissionRequested", "bluetoothPermissionRequested", "locationPermissionRequested",
            "onboardingCompleted", "defaultsSeeded", "coachMarksSeen", // this install's first-run progress
            "allowLandscape", // a per-device choice
        )
        // drawerGridDensity is stored as drawerGridSize, and the landscape group as flat landscape* fields
        val renamed = mapOf("drawerGridDensity" to "drawerGridSize")
        val landscapeGroup = setOf(
            "landscapeClockAlignment", "landscapeClockZoneHeightDp", "landscapeClockScale",
            "landscapeAppRowPosition", "landscapeAppListVerticalAlignment", "landscapeAppListColumnAlignment",
        )
        // the chosen weather city is saved as four flat fields (the current location never is)
        val weatherCityGroup = setOf("weatherCityName", "weatherCityLatitude", "weatherCityLongitude", "weatherCityTimezone")
        val expected = (fieldNames(LauncherSettings::class.java) - notBackedUp - "landscape" - "weatherCity")
            .map { renamed[it] ?: it }.toSet() + landscapeGroup + weatherCityGroup
        val exported = fieldNames(BackupSettings::class.java) - "activeFacetIndex"

        // Then the backup holds exactly those
        assertEquals(expected, exported)
    }
}
