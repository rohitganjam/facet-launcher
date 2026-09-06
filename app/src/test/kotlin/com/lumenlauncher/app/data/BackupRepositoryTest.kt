package com.lumenlauncher.app.data

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.lumenlauncher.app.data.model.BackupBundle
import com.lumenlauncher.app.data.model.BackupSettings
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
        showDrawerIcons = true, showDrawerLabels = true, searchBarPosition = "TOP", activeProfileIndex = null,
        showAllDayEvents = true, searchContactsEnabled = false, themeMode = "SYSTEM", accentFromSystem = true,
        customAccentSwatch = null, iconRenderMode = "SYSTEM_DEFAULT", launcherFontOption = "SYSTEM",
        appLabelColorOption = "THEME", appRowPosition = "LEFT", appRowPresentation = "ICON_AND_TEXT",
        listContentMode = "FAVORITES", appsToShowCount = 5, clockTemplateId = "LIGHT_STACK",
        clockFontOption = "LAUNCHER_DEFAULT", clockColorOption = "THEME", clockShowMeridiem = false,
        calendarFontOption = "LAUNCHER_DEFAULT", calendarColorOption = "THEME",
    )

    @Test
    fun `a written bundle reads back byte-for-byte equal`() = runTest {
        // Given a real file:// destination
        val file = File(context.cacheDir, "backup-test-${System.nanoTime()}.json")
        val uri: Uri = Uri.fromFile(file)
        val bundle = BackupBundle(
            exportedAtEpochMillis = 123456789L,
            settings = minimalSettings(),
            profiles = emptyList(),
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
    fun `reading a file that isn't valid JSON returns null instead of throwing`() = runTest {
        val file = File(context.cacheDir, "not-a-backup-${System.nanoTime()}.json")
        file.writeText("this is not json")
        val uri: Uri = Uri.fromFile(file)

        val readBack = repository.readBackup(uri)

        assertNull(readBack)
    }
}
