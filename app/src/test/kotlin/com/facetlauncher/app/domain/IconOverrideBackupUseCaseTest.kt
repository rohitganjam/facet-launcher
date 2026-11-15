package com.facetlauncher.app.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Process
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.BackupIconOverride
import com.facetlauncher.app.data.model.IconOverride
import com.facetlauncher.app.data.model.IconOverrideSource
import java.io.File
import java.time.Clock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconOverrideBackupUseCaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()
    private val repository = IconOverrideRepository(context, database.iconOverrideDao(), mock(IconPackRepository::class.java), Clock.systemUTC())
    private val appRepository: AppRepository = mock(AppRepository::class.java).also {
        `when`(it.resolveUserHandle(AppProfile.PERSONAL)).thenReturn(Process.myUserHandle())
        `when`(it.resolveUserHandle(AppProfile.WORK)).thenReturn(null)
    }
    private val useCase = IconOverrideBackupUseCase(repository, appRepository)
    private val app = AppInfo(packageName = "com.example.music", activityName = "com.example.music.Main", label = "Music", icon = null)

    @After
    fun tearDown() {
        database.close()
        File(context.filesDir, IconOverrideRepository.FOLDER).deleteRecursively()
    }

    @Test
    fun `a pack icon round-trips with its snapshot`() = runTest {
        // Given a pack icon with a red snapshot, exported
        repository.setPackIcon(app, "com.example.pack", "music", calendarPrefix = "music_", snapshot = solid(Color.RED))
        val originalBytes = repository.snapshotBytes(repository.getAll().single())
        val exported = useCase.export()

        // When everything is reset and the backup restored
        repository.resetAll()
        useCase.restore(exported)

        // Then the same icon is back, for this profile, with the same snapshot bytes
        val row = repository.getAll().single()
        assertEquals(IconOverrideSource.PACK, row.source)
        assertEquals("com.example.pack", row.packPackage)
        assertEquals("music", row.drawableName)
        assertEquals("music_", row.calendarPrefix)
        assertEquals(Process.myUserHandle().hashCode(), row.userId)
        assertNotNull(row.snapshotFile)
        assertArrayEquals(originalBytes, repository.snapshotBytes(row))
    }

    @Test
    fun `a profile missing on this device restores unresolved, and an app not installed is kept`() = runTest {
        // Given a backup with a work app's own icon, on a device without a work profile, for an app not installed here
        val entry = BackupIconOverride(packageName = "com.example.notinstalled", activityName = "Main", profile = "WORK", source = "OWN")

        // When restored
        useCase.restore(listOf(entry))

        // Then the row is kept, waiting to match by profile
        val row = repository.getAll().single()
        assertEquals(IconOverride.UNRESOLVED_USER_ID, row.userId)
        assertEquals(AppProfile.WORK, row.profile)
        assertNull(row.snapshotFile)
    }

    @Test
    fun `an entry with an unknown source is skipped and a broken snapshot is dropped`() = runTest {
        // Given a backup from a newer version with a source this one doesn't know, and one with a corrupt snapshot
        val unknown = BackupIconOverride(packageName = "a", activityName = "A", source = "PHOTO")
        val corrupt = BackupIconOverride(packageName = "b", activityName = "B", source = "PACK", packPackage = "p", drawableName = "b", snapshotPng = "not base64!")

        // When restored
        useCase.restore(listOf(unknown, corrupt))

        // Then only the known one is kept, without a snapshot
        val row = repository.getAll().single()
        assertEquals("b", row.packageName)
        assertNull(row.snapshotFile)
    }

    private fun solid(color: Int): Bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
}
