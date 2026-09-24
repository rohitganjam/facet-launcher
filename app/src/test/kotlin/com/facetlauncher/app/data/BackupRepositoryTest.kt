package com.facetlauncher.app.data

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.BackupBundle
import com.facetlauncher.app.data.model.BackupSettings
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest {

    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repository = BackupRepository(context)

    private fun minimalSettings() = BackupSettings(
        use24HourTime = false, dockDisplayMode = "ICONS", drawerPresentation = "LIST", drawerGridSize = "FIVE_BY_SIX",
        drawerListItemSize = "REGULAR", drawerOpacity = 0.6f, notificationDotsEnabled = true, notificationBadgeStyle = "DOT",
        showDrawerIcons = true, showDrawerLabels = true, searchBarPosition = "TOP", activeFacetIndex = null,
        showAllDayEvents = true, searchContactsEnabled = false, themeMode = "SYSTEM", accentFromSystem = true,
        customAccentSwatch = null, wallpaperAccentRole = "PRIMARY", iconRenderMode = "SYSTEM_DEFAULT", launcherFontOption = "SYSTEM",
        appLabelColorOption = "THEME", appRowPosition = "LEFT", appRowPresentation = "ICON_AND_TEXT",
        listContentMode = "FAVORITES", appsToShowCount = 5, clockTemplateId = "LIGHT_STACK",
        clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME", clockShowMeridiem = false,
    )

    @Test
    fun `a written bundle reads back byte-for-byte equal`() = runTest {
        // Given a real file:// destination
        val file = File(context.cacheDir, "backup-test-${System.nanoTime()}.json")
        val uri: Uri = Uri.fromFile(file)
        val bundle = BackupBundle(
            exportedAtEpochMillis = 123456789L,
            settings = minimalSettings(),
            facets = emptyList(),
            dockApps = emptyList(),
            defaultFavoriteApps = emptyList(),
            widgetPlacements = emptyList(),
        )

        // When writing then reading it back
        repository.writeBackup(uri, bundle)
        val readBack = repository.readBackup(uri)

        // Then it round-trips exactly
        assertEquals(bundle, readBack)
    }

    @Test
    fun `a legacy backup file using the pre-Facet-rename JSON keys still imports`() = runTest {
        // Given a real file on disk using the wire format from before the Profile -> Facet rename
        // (top-level "profiles" key, "activeProfileIndex" in settings) — see BackupBundle.kt's
        // @SerialName annotations, which exist specifically to keep this working.
        val file = File(context.cacheDir, "legacy-backup-${System.nanoTime()}.json")
        file.writeText(
            """
            {
                "exportedAtEpochMillis": 123456789,
                "settings": {
                    "use24HourTime": false, "dockDisplayMode": "ICONS", "drawerPresentation": "LIST",
                    "drawerGridSize": "FIVE_BY_SIX", "drawerListItemSize": "REGULAR", "drawerOpacity": 0.6,
                    "notificationDotsEnabled": true, "notificationBadgeStyle": "DOT", "showDrawerIcons": true,
                    "showDrawerLabels": true, "searchBarPosition": "TOP", "activeProfileIndex": 0,
                    "showAllDayEvents": true, "searchContactsEnabled": false, "themeMode": "SYSTEM",
                    "accentFromSystem": true, "customAccentSwatch": null, "wallpaperAccentRole": "PRIMARY",
                    "iconRenderMode": "SYSTEM_DEFAULT", "launcherFontOption": "SYSTEM", "appLabelColorOption": "THEME",
                    "appRowPosition": "LEFT", "appRowPresentation": "ICON_AND_TEXT", "listContentMode": "FAVORITES",
                    "appsToShowCount": 5, "clockTemplateId": "LIGHT_STACK", "clockFontOption": "LAUNCHER_DEFAULT",
                    "clockColorOption": "THEME", "clockShowMeridiem": false, "calendarFontOption": "LAUNCHER_DEFAULT",
                    "calendarColorOption": "THEME"
                },
                "profiles": [],
                "dockApps": [],
                "defaultFavoriteApps": [],
                "widgetPlacements": []
            }
            """.trimIndent(),
        )
        val uri: Uri = Uri.fromFile(file)

        // When importing it under the current, renamed model
        val readBack = repository.readBackup(uri)

        // Then it deserializes into the Facet-named fields instead of being treated as absent
        assertEquals(0, readBack?.settings?.activeFacetIndex)
        assertEquals(emptyList<Any>(), readBack?.facets)
    }

    @Test
    fun `reading a file that isn't valid JSON returns null instead of throwing`() = runTest {
        val file = File(context.cacheDir, "not-a-backup-${System.nanoTime()}.json")
        file.writeText("this is not json")
        val uri: Uri = Uri.fromFile(file)

        val readBack = repository.readBackup(uri)

        assertNull(readBack)
    }
}
