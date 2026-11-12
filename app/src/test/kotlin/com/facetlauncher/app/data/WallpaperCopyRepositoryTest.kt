package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WallpaperCopyRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val wallpaperManager = mock(WallpaperManager::class.java)

    private fun repository() = WallpaperCopyRepository(context, wallpaperManager)

    private fun bitmap(width: Int = 40, height: Int = 80) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    @After
    fun cleanUp() {
        File(context.filesDir, "wallpaper_copy.png").delete()
    }

    @Test
    fun `no stored copy reads as null`() = runTest {
        // Given nothing was saved
        // When reading the copy
        val copy = repository().copy.first()

        // Then there is none
        assertNull(copy)
    }

    @Test
    fun `a saved copy is read back by a new repository`() = runTest {
        // Given a saved copy
        assertTrue(repository().save(bitmap(40, 80)))

        // When a fresh repository reads it from storage
        val copy = repository().copy.first()

        // Then it is the same size
        assertNotNull(copy)
        assertEquals(40, copy!!.width)
        assertEquals(80, copy.height)
    }

    @Test
    fun `saving replaces the copy the repository is already showing`() = runTest {
        // Given a repository that has loaded one copy
        val repository = repository()
        repository.save(bitmap(40, 80))
        assertEquals(40, repository.copy.first()!!.width)

        // When another is saved
        repository.save(bitmap(60, 120))

        // Then the new one is the copy
        assertEquals(60, repository.copy.first()!!.width)
    }

    @Test
    fun `removing the copy clears it for this and a new repository`() = runTest {
        // Given a saved copy
        val repository = repository()
        repository.save(bitmap())

        // When it is removed
        repository.remove()

        // Then both read none
        assertNull(repository.copy.first())
        assertNull(repository().copy.first())
    }

    @Test
    fun `setting the wallpaper for home only passes the system flag and returns the new id`() = runTest {
        // Given the system numbers its wallpaper 7 once set
        val image = bitmap()
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(7)

        // When setting it for Home only
        val id = repository().setSystemWallpaper(image, includeLock = false)

        // Then only the system screen was set, and the id is returned
        verify(wallpaperManager).setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM)
        assertEquals(7, id)
    }

    @Test
    fun `setting the wallpaper with the lock screen passes both flags`() = runTest {
        // Given a wallpaper to set
        val image = bitmap()
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(8)

        // When setting it for Home and lock
        repository().setSystemWallpaper(image, includeLock = true)

        // Then both screens were asked for
        verify(wallpaperManager).setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
    }

    @Test
    fun `a refused system wallpaper returns no id`() = runTest {
        // Given the system refuses
        val image = bitmap()
        `when`(wallpaperManager.setBitmap(image, null, true, WallpaperManager.FLAG_SYSTEM)).thenThrow(SecurityException("denied"))

        // When setting it
        val id = repository().setSystemWallpaper(image, includeLock = false)

        // Then there is no id to store
        assertEquals(WallpaperCopyRepository.NO_WALLPAPER_ID, id)
    }

    @Test
    fun `the current system wallpaper id comes from the wallpaper manager`() {
        // Given the system's id is 12
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(12)

        // Then the repository reports it
        assertEquals(12, repository().currentSystemWallpaperId())
    }

    @Test
    fun `a large photo is shrunk to the longest side allowed, keeping its shape`() {
        // When the long side is over the limit
        val size = shrunkSize(4000, 3000, maxSide = 2000)

        // Then both sides shrink in proportion
        assertEquals(2000 to 1500, size)
        assertEquals(1500 to 2000, shrunkSize(3000, 4000, maxSide = 2000))
    }

    @Test
    fun `a photo that already fits is left alone`() {
        // Then there is nothing to shrink
        assertNull(shrunkSize(800, 600, maxSide = 2000))
        assertNull(shrunkSize(2000, 1000, maxSide = 2000))
    }

    @Test
    fun `an unreadable photo decodes to null`() = runTest {
        // Given a uri that is not an image
        val missing = android.net.Uri.fromFile(File(context.cacheDir, "missing.png"))

        // Then decoding gives nothing rather than throwing
        assertNull(repository().decodePicked(missing, maxSide = 100))
    }

    @Test
    fun `the screen size is always reported portrait`() {
        // When asking for the screen size
        val (width, height) = repository().portraitScreenSize()

        // Then the short side is the width
        assertTrue(width in 1..height)
    }
}
