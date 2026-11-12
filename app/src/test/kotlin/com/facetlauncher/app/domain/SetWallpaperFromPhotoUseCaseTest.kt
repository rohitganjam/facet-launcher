package com.facetlauncher.app.domain

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SetWallpaperFromPhotoUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun settingsRepository(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    /** A real (not Mockito) stub, so the suspend calls and what they were given can be checked directly. */
    private class FakeCopies(var systemId: Int = 9, var saves: Boolean = true) : WallpaperCopyRepository(
        context = RuntimeEnvironment.getApplication(),
        wallpaperManager = mock(WallpaperManager::class.java),
    ) {
        var setBitmap: Bitmap? = null
        var setIncludeLock: Boolean? = null
        var saved: Bitmap? = null
        var removed = false

        override fun portraitScreenSize() = 50 to 100
        override suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int {
            setBitmap = bitmap
            setIncludeLock = includeLock
            return systemId
        }
        override suspend fun save(bitmap: Bitmap): Boolean {
            if (saves) saved = bitmap
            return saves
        }
        override suspend fun remove() { removed = true }
    }

    private fun photo() = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888).also { it.eraseColor(Color.RED) }

    private fun useCase(copies: FakeCopies, settings: SettingsRepository) =
        SetWallpaperFromPhotoUseCase(copies, settings, UnconfinedTestDispatcher())

    @Test
    fun `the framed part is cropped to the screen, set, stored and its system id recorded`() = runTest {
        // Given a photo, and a system that numbers the new wallpaper 9
        val copies = FakeCopies(systemId = 9)
        val settings = settingsRepository()

        // When setting the left half for Home only
        val done = useCase(copies, settings)(photo(), RectF(0f, 0f, 100f, 100f), includeLock = false)

        // Then the copy at screen size was set, stored (the same one), and the id recorded
        assertTrue(done)
        assertEquals(50, copies.setBitmap!!.width)
        assertEquals(100, copies.setBitmap!!.height)
        assertSame(copies.setBitmap, copies.saved)
        assertEquals(false, copies.setIncludeLock)
        assertEquals(9, settings.settings.first().wallpaperCopySystemId)
    }

    @Test
    fun `the lock screen is set too when asked`() = runTest {
        // Given a photo
        val copies = FakeCopies()

        // When setting for Home and lock
        useCase(copies, settingsRepository())(photo(), RectF(0f, 0f, 100f, 100f), includeLock = true)

        // Then the lock screen was included
        assertEquals(true, copies.setIncludeLock)
    }

    @Test
    fun `nothing is stored when the system refuses the wallpaper`() = runTest {
        // Given a system that refuses
        val copies = FakeCopies(systemId = WallpaperCopyRepository.NO_WALLPAPER_ID)
        val settings = settingsRepository()

        // When setting a wallpaper
        val done = useCase(copies, settings)(photo(), RectF(0f, 0f, 100f, 100f), includeLock = false)

        // Then it failed, with no copy and no id, so glass never differs from what is set
        assertFalse(done)
        assertNull(copies.saved)
        assertEquals(-1, settings.settings.first().wallpaperCopySystemId)
    }

    @Test
    fun `no id is recorded when the copy could not be stored`() = runTest {
        // Given storage that fails
        val copies = FakeCopies(saves = false)
        val settings = settingsRepository()

        // When setting a wallpaper
        val done = useCase(copies, settings)(photo(), RectF(0f, 0f, 100f, 100f), includeLock = false)

        // Then it reports failure and records nothing
        assertFalse(done)
        assertEquals(-1, settings.settings.first().wallpaperCopySystemId)
    }

    @Test
    fun `removing the copy deletes it and clears the recorded id`() = runTest {
        // Given a recorded id and a copy
        val copies = FakeCopies()
        val settings = settingsRepository()
        settings.setWallpaperCopySystemId(5)

        // When removed
        RemoveWallpaperCopyUseCase(copies, settings)()

        // Then the copy is gone and the id cleared
        assertTrue(copies.removed)
        assertEquals(-1, settings.settings.first().wallpaperCopySystemId)
    }

    @Test
    fun `cropping keeps only the visible part`() {
        // Given a photo whose left half is red and right half is blue
        val photo = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        for (x in 0 until 200) for (y in 0 until 100) photo.setPixel(x, y, if (x < 100) Color.RED else Color.BLUE)

        // When cropping the right half to a 50 x 100 copy
        val copy = photo.croppedTo(RectF(100f, 0f, 200f, 100f), 50, 100)

        // Then it is the target size and all blue
        assertEquals(50, copy.width)
        assertEquals(100, copy.height)
        assertEquals(Color.BLUE, copy.getPixel(2, 50))
        assertEquals(Color.BLUE, copy.getPixel(47, 50))
    }
}
