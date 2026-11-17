package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.data.model.promptKey
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    fun `the screen size is the display's natural size`() {
        // When asking for the screen size on a phone in its natural portrait orientation
        val (width, height) = repository().screenSize()

        // Then the short side is the width
        assertTrue(width in 1..height)
    }

    /** A repository on a screen the test can change, as a foldable does when it folds. */
    private inner class FoldableRepository(start: ScreenSize) : WallpaperCopyRepository(context, wallpaperManager) {
        val screen = MutableStateFlow(start)
        override fun screenSize() = screen.value.width to screen.value.height
        override val currentScreen: Flow<ScreenSize> get() = screen
    }

    private val outer = ScreenSize(1080, 2092)
    private val inner = ScreenSize(2208, 1840)

    @Test
    fun `each screen keeps its own copy and the one showing follows the fold`() = runTest {
        // Given a copy saved on each screen
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.screen.value = inner
        repository.save(bitmap(30, 40))

        // When the screen shown changes
        val onInner = repository.copy.first()
        repository.screen.value = outer
        val onOuter = repository.copy.first()

        // Then each gets its own
        assertEquals(30, onInner!!.width)
        assertEquals(10, onOuter!!.width)
    }

    @Test
    fun `a copy is out of date once the system wallpaper changes`() = runTest {
        // Given a copy stored while the wallpaper had id 1
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(1)
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        assertTrue(repository.copyIsCurrent.first())

        // When the wallpaper is changed elsewhere
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(2)
        repository.recheck()

        // Then the copy is still held but no longer current
        assertFalse(repository.copyIsCurrent.first())
        assertEquals(10, repository.copy.first()!!.width)
    }

    @Test
    fun `a copy on one screen does not make the other screen's old copy current`() = runTest {
        // Given both screens have a copy of wallpaper 1
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(1)
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.screen.value = inner
        repository.save(bitmap(30, 40))

        // When the wallpaper changes and only the inner screen is captured
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(2)
        repository.recheck()
        repository.save(bitmap(50, 60))

        // Then the outer screen is reported as lacking a copy, and shows the inner one rather than its old image
        val screens = repository.screens.first()
        assertTrue(screens.first { it.size == inner }.hasCopy)
        assertFalse(screens.first { it.size == outer }.hasCopy)
        repository.screen.value = outer
        assertEquals(50, repository.copy.first()!!.width)
        assertTrue(repository.copyIsCurrent.first())
    }

    @Test
    fun `a copy from before ids were recorded is out of date once another screen records one`() = runTest {
        // Given a copy on the outer screen stored before ids were tracked
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(1)
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        File(context.filesDir, "wallpaper_copy_ids.txt").delete()
        val fresh = FoldableRepository(outer)
        assertTrue(fresh.copyIsCurrent.first())

        // When the inner screen is captured, which records an id
        fresh.screen.value = inner
        fresh.save(bitmap(30, 40))

        // Then the outer screen's unrecorded copy no longer counts as current
        assertFalse(fresh.screens.first().first { it.size == outer }.hasCopy)
    }

    @Test
    fun `the copy ids are remembered by a new repository`() = runTest {
        // Given a copy stored while the wallpaper had id 1, then the wallpaper changed
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(1)
        repository().save(bitmap(10, 20))
        `when`(wallpaperManager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)).thenReturn(2)

        // When a fresh repository reads it
        val fresh = repository()

        // Then it knows the copy is out of date
        assertFalse(fresh.copyIsCurrent.first())
    }

    @Test
    fun `a screen without a copy shows the other copy closest in shape`() = runTest {
        // Given only the outer screen has a copy
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))

        // When the inner screen is showing
        repository.screen.value = inner

        // Then it shows the outer copy rather than nothing
        assertEquals(10, repository.copy.first()!!.width)
    }

    @Test
    fun `of two other copies the one closest in shape stands in`() = runTest {
        // Given an outer-shaped and a squarer copy, and a screen shaped like the squarer one
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.screen.value = ScreenSize(1500, 1800)
        repository.save(bitmap(50, 60))

        // When a third screen near the squarer one shows
        repository.screen.value = ScreenSize(1700, 2000)

        // Then the squarer copy stands in
        assertEquals(50, repository.copy.first()!!.width)
    }

    @Test
    fun `the screens seen are remembered with whether each has a copy`() = runTest {
        // Given the outer screen was seen and has a copy, and the inner was only seen
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.copy.first()
        repository.screen.value = inner
        repository.copy.first()

        // When a new repository reads them back
        val screens = FoldableRepository(outer).screens.first()

        // Then both are listed, the larger first, and only the outer has a copy
        assertEquals(listOf(ScreenCopy(inner, hasCopy = false), ScreenCopy(outer, hasCopy = true)), screens)
    }

    @Test
    fun `removing copies keeps the screens seen`() = runTest {
        // Given a copy on a screen that was seen
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.copy.first()

        // When the copies are removed
        repository.remove()

        // Then the screen is still known, without a copy
        assertEquals(listOf(ScreenCopy(outer, hasCopy = false)), repository.screens.first())
    }

    @Test
    fun `a copy stored before per-screen copies becomes the copy of its own screen`() = runTest {
        // Given the old single file holding an outer-sized image
        File(context.filesDir, "wallpaper_copy.png").outputStream().use { bitmap(1080, 2092).compress(Bitmap.CompressFormat.PNG, 100, it) }

        // When the repository loads on that screen
        val repository = FoldableRepository(outer)
        val copy = repository.copy.first()

        // Then it is that screen's copy, and the old file is gone
        assertEquals(1080, copy!!.width)
        assertTrue(File(context.filesDir, "wallpaper_copy_1080x2092.png").exists())
        assertFalse(File(context.filesDir, "wallpaper_copy.png").exists())
    }

    @Test
    fun `a dismissed prompt is remembered by a new repository`() = runTest {
        // Given a prompt the user said no to
        FoldableRepository(outer).dismissPrompt(WallpaperCopyRepository.FOLDABLE_PROMPT)

        // When a new repository reads them back
        val said = FoldableRepository(outer).dismissedPrompts.first()

        // Then it is still dismissed
        assertEquals(setOf(WallpaperCopyRepository.FOLDABLE_PROMPT), said)
    }

    @Test
    fun `removing the copies keeps what the user said no to`() = runTest {
        // Given a dismissed prompt and a copy
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.dismissPrompt(inner.promptKey)

        // When the copies are removed
        repository.remove()

        // Then the dismissal stays
        assertEquals(setOf(inner.promptKey), repository.dismissedPrompts.first())
    }

    @Test
    fun `a phone without a hinge sensor is not a possible foldable`() {
        // Then the test phone has no hinge sensor
        assertFalse(repository().maybeFoldable())
    }

    @Test
    fun `a screen size round trips through its key`() {
        // Then parsing the key gives the size, and a bad key gives none
        assertEquals(outer, ScreenSize.parse(outer.key))
        assertNull(ScreenSize.parse("wide"))
        assertNull(ScreenSize.parse("0x10"))
    }
}
