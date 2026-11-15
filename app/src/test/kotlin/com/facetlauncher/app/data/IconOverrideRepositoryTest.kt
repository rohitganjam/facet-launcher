package com.facetlauncher.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconOverrideRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
    private val folder = File(context.filesDir, IconOverrideRepository.FOLDER)

    /** A pack that is installed only while [installed], drawing every drawable as solid blue. */
    private class FakePacks(context: Context) : IconPackRepository(context, mock(SettingsRepository::class.java)) {
        var installed = true
        val drawn = mutableListOf<String>()
        override suspend fun packUpdatedAt(packPackage: String): Long? = if (installed) 1L else null
        override suspend fun drawIcon(packPackage: String, drawableName: String, maxSize: Int): Bitmap? {
            drawn += drawableName
            return if (installed) solid(Color.BLUE) else null
        }
    }

    private val packs = FakePacks(context)
    private val repository = IconOverrideRepository(context, database.iconOverrideDao(), packs, Clock.fixed(Instant.parse("2026-11-15T10:00:00Z"), ZoneOffset.UTC))
    private val app = AppInfo(packageName = "com.example.music", activityName = "com.example.music.Main", label = "Music", icon = null)

    @After
    fun tearDown() {
        database.close()
        folder.deleteRecursively()
    }

    @Test
    fun `a pack icon is drawn from the pack, and from its snapshot once the pack is gone`() = runTest {
        // Given the app set to a pack icon, with a red snapshot
        repository.setPackIcon(app, "com.example.pack", "music", calendarPrefix = null, snapshot = solid(Color.RED))
        val stored = repository.getAll().single()

        // When the pack is installed, then once it's uninstalled
        val live = repository.drawPackIcon(stored, dayOfMonth = 15)
        packs.installed = false
        val fallback = repository.drawPackIcon(stored, dayOfMonth = 15)

        // Then the pack draws it first, the snapshot after
        assertEquals(Color.BLUE, live!!.getPixel(0, 0))
        assertEquals(Color.RED, fallback!!.getPixel(0, 0))
    }

    @Test
    fun `a dated pack icon is drawn for the day`() = runTest {
        // Given a calendar app set to a dated pack icon
        repository.setPackIcon(app, "com.example.pack", "calendar", calendarPrefix = "calendar_", snapshot = null)

        // When drawn on the 7th
        repository.drawPackIcon(repository.getAll().single(), dayOfMonth = 7)

        // Then the 7th's drawable is asked for
        assertEquals("calendar_7", packs.drawn.first())
    }

    @Test
    fun `changing an icon replaces the row and deletes the old snapshot`() = runTest {
        // Given a pack icon with a snapshot
        repository.setPackIcon(app, "com.example.pack", "music", calendarPrefix = null, snapshot = solid(Color.RED))
        val firstFile = repository.getAll().single().snapshotFile!!

        // When the app is set to its own icon
        repository.setOwnIcon(app)

        // Then one row remains, an own-icon one, and the snapshot is gone
        val row = repository.getAll().single()
        assertEquals(IconOverrideSource.OWN, row.source)
        assertFalse(File(folder, firstFile).exists())
    }

    @Test
    fun `uninstalling an app drops its custom icon and file`() = runTest {
        // Given a custom icon
        repository.setPackIcon(app, "com.example.pack", "music", calendarPrefix = null, snapshot = solid(Color.RED))
        val file = repository.getAll().single().snapshotFile!!

        // When the app is uninstalled from its profile
        repository.removeByPackage(app.packageName, app.userHandle.hashCode())

        // Then nothing is left
        assertTrue(repository.overrides.first().isEmpty())
        assertFalse(File(folder, file).exists())
    }

    @Test
    fun `restore replaces every custom icon and writes the restored snapshots`() = runTest {
        // Given an existing custom icon, and a backup holding another app's with PNG bytes
        repository.setOwnIcon(app)
        val restored = IconOverride("com.example.maps", "com.example.maps.Main", IconOverride.UNRESOLVED_USER_ID, app.profile, IconOverrideSource.PACK, "com.example.pack", "maps")
        val png = java.io.ByteArrayOutputStream().also { solid(Color.GREEN).compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()

        // When restoring
        repository.restore(listOf(restored to png))

        // Then only the restored row is kept, and its snapshot reads back
        val row = repository.getAll().single()
        assertEquals("com.example.maps", row.packageName)
        assertNotNull(row.snapshotFile)
        assertNotNull(repository.snapshotBytes(row))
        assertNull(repository.getAll().firstOrNull { it.packageName == app.packageName })
    }
}

private fun solid(color: Int): Bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
