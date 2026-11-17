package com.facetlauncher.app.domain

import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupFacet
import com.facetlauncher.app.data.model.BackupSettings
import com.facetlauncher.app.data.model.Facet
import com.facetlauncher.app.data.model.LauncherSettings
import java.lang.reflect.Modifier
import org.junit.Assert.assertEquals
import org.junit.Test
import com.facetlauncher.app.data.local.AutomationRuleEntity
import com.facetlauncher.app.data.model.BackupAutomationRule

/**
 * Fails when a field is added to [Facet] or [LauncherSettings] without either exporting it in the
 * backup or listing it below as deliberately left out, so a restore can't silently reset it.
 */
class BackupCoverageTest {

    private fun fieldNames(type: Class<*>): Set<String> =
        type.declaredFields.filterNot { it.isSynthetic || Modifier.isStatic(it.modifiers) }.map { it.name }.toSet()

    @Test
    fun `the App Vault is never part of a backup`() {
        // Given every class a backup file is made of
        val backupClasses = listOf(BackupBundle::class.java, BackupSettings::class.java, BackupFacet::class.java)

        // Then none of them carries a vault field (the vault stays on the phone, in the excluded facet_vault store)
        backupClasses.forEach { type ->
            assertEquals(type.simpleName, emptySet<String>(), fieldNames(type).filter { it.contains("vault", ignoreCase = true) }.toSet())
        }
    }

    @Test
    fun `every facet field is exported or deliberately left out`() {
        // Given the fields a facet row holds, minus those a backup cannot or should not carry
        val notBackedUp = setOf(
            "id", // a Room row id is not portable; the backup uses list order
            "clockStack", // hosted widgets are AppWidgetHost ids, which are not portable, so they are re-added by hand; the native pages go out as nativeStackPages
        )
        val expected = fieldNames(Facet::class.java) - notBackedUp
        // The clock's settings and box go out through the clock page ([clockConfig] and the box fields); the facet-level clock and calendar fields are legacy, read from older backups only
        val legacyClockFields = setOf(
            "overrideClock", "clockTemplateId", "clockFontOption", "clockColorOption", "use24HourTime", "clockShowMeridiem", "clockAccentColorOption", "clockDateStyle",
            "clockShowAlarm", "clockShowBattery", "clockShowWeather", "clockWeatherIconStyle", "clockWeatherLocation",
            "overrideCalendar", "showAllDayEvents", "showCalendarIndicator", "calendarItemsToShow", "selectedCalendarIds",
        )
        val clockPageFields = setOf("clockConfig", "clockWidthDp", "clockHeightDp", "clockLandscapeWidthDp", "clockLandscapeHeightDp")
        // The override flags of version 5 and older are read once, to give a facet the launcher's lists and look it showed, and no longer written
        val legacyHomeFields = setOf("overrideApps", "overridingFavorites", "overrideDock")
        val exported = fieldNames(BackupFacet::class.java) - setOf("dockApps", "favorites", "dockFolderPlacements", "favoriteFolderPlacements", "nativeStackPages") - legacyClockFields - clockPageFields - legacyHomeFields

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
            "onboardingCompleted", "coachMarksSeen", // this install's first-run progress
            "hideStatusBar", // a per-device choice
            "wallpaperCopySystemId", "wallpaperPromptDismissed", // tied to this device's wallpaper copy, an image that is not in a backup
        )
        // drawerGridDensity is stored as drawerGridSize, and the landscape group as flat landscape* fields
        val renamed = mapOf("drawerGridDensity" to "drawerGridSize")
        val landscapeGroup = setOf(
            "landscapeClockAlignment", "landscapeClockZoneHeightDp",
            "landscapeAppRowPosition", "landscapeAppListVerticalAlignment", "landscapeAppListColumnAlignment",
        )
        // the chosen weather city is saved as four flat fields (the current location never is)
        val weatherCityGroup = setOf("weatherCityName", "weatherCityLatitude", "weatherCityLongitude", "weatherCityTimezone")
        // the per-swipe actions are saved as four flat fields
        val swipeActionGroup = setOf("swipeUpAction", "swipeDownAction", "swipeLeftAction", "swipeRightAction")
        val expected = (fieldNames(LauncherSettings::class.java) - notBackedUp - "landscape" - "weatherCity" - "swipeActions")
            .map { renamed[it] ?: it }.toSet() + landscapeGroup + weatherCityGroup + swipeActionGroup
        // The launcher-wide clock look and accessories of version 4 and older are still read, to give an older backup's clocks their settings, but not written
        val legacyClockFields = setOf(
            "clockTemplateId", "clockFontOption", "clockColorOption", "clockAccentColorOption", "clockShowMeridiem", "clockDateStyle",
            "showNextAlarm", "showBatteryPercentage", "showCalendarIndicator", "calendarItemsToShow",
        )
        // The swipe switches of older versions are still written for them and read from older backups
        val legacySwipeFields = setOf("swipeLeftEnabled", "swipeRightEnabled", "swipeDownEnabled", "swapSwipeSides")
        val exported = fieldNames(BackupSettings::class.java) - "activeFacetIndex" - legacyClockFields - legacySwipeFields

        // Then the backup holds exactly those
        assertEquals(expected, exported)
    }

    @Test
    fun `every automation rule column is exported or deliberately left out`() {
        // Given the columns a rule row holds, minus those a backup cannot or should not carry
        val notBackedUp = setOf(
            "id", // a Room row id is not portable
            "position", // the backup's list order is the rule order
        )
        // Facets are referenced by UUID, not row id, and the device fields carry the trigger's name
        val renamed = mapOf(
            "targetFacetId" to "targetFacetUuid", "endFacetId" to "endFacetUuid",
            "deviceAddress" to "bluetoothAddress", "deviceName" to "bluetoothName",
        )
        val expected = (fieldNames(AutomationRuleEntity::class.java) - notBackedUp).map { renamed[it] ?: it }.toSet()
        // A battery rule's charging state is stored in the row's negated column; the backup names it
        val exported = fieldNames(BackupAutomationRule::class.java) - "batteryWhileCharging"

        // Then the backup holds exactly those
        assertEquals(expected, exported)
    }
}
