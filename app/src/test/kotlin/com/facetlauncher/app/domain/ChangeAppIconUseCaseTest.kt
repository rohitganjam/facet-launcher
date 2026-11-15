package com.facetlauncher.app.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.IconOverrideRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.iconpack.IconPackRepository
import com.facetlauncher.app.data.local.FacetDatabase
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.IconOverrideSource
import com.facetlauncher.app.data.model.PackIconMatch
import java.io.File
import java.time.Clock
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ChangeAppIconUseCaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val database = Room.inMemoryDatabaseBuilder(context, FacetDatabase::class.java).allowMainThreadQueries().build()

    private class FakePacks(context: Context) : IconPackRepository(context, mock(SettingsRepository::class.java)) {
        val drawn = mutableListOf<String>()
        override suspend fun packUpdatedAt(packPackage: String): Long? = 1L
        override suspend fun drawIcon(packPackage: String, drawableName: String, maxSize: Int): Bitmap? {
            drawn += drawableName
            return Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        }
    }

    private val packs = FakePacks(context)
    private val repository = IconOverrideRepository(context, database.iconOverrideDao(), packs, Clock.systemUTC())
    private val useCase = ChangeAppIconUseCase(packs, repository)
    private val app = AppInfo(packageName = "com.example.calendar", activityName = "com.example.calendar.Main", label = "Calendar", icon = null)

    @After
    fun tearDown() {
        database.close()
        File(context.filesDir, IconOverrideRepository.FOLDER).deleteRecursively()
    }

    @Test
    fun `a pack icon is saved with a snapshot of its drawable`() = runTest {
        // When the app takes a pack's icon
        useCase.usePackIcon(app, PackIconMatch("com.example.pack", "calendar", calendarPrefix = null))

        // Then it's stored with a snapshot of that drawable
        val row = repository.getAll().single()
        assertEquals(IconOverrideSource.PACK, row.source)
        assertEquals(listOf("calendar"), packs.drawn)
        assertNotNull(row.snapshotFile)
    }

    @Test
    fun `a dated-only pack icon snapshots the first of the month`() = runTest {
        // When the app takes a calendar entry that names only a prefix
        useCase.usePackIcon(app, PackIconMatch("com.example.pack", drawableName = null, calendarPrefix = "cal_"))

        // Then the 1st's drawable is the snapshot
        assertEquals(listOf("cal_1"), packs.drawn)
        assertNotNull(repository.getAll().single().snapshotFile)
    }

    @Test
    fun `reset clears only that app's custom icon`() = runTest {
        // Given two apps with custom icons
        val other = app.copy(packageName = "com.example.mail", activityName = "com.example.mail.Main", label = "Mail")
        useCase.useOwnIcon(app)
        useCase.useOwnIcon(other)

        // When one is reset
        useCase.reset(app)

        // Then the other keeps its own
        val left = repository.getAll()
        assertEquals(listOf("com.example.mail"), left.map { it.packageName })

        // And reset all clears the rest
        useCase.resetAll()
        assertTrue(repository.getAll().isEmpty())
    }
}
