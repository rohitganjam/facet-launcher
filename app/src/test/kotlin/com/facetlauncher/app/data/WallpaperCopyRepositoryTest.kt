package com.facetlauncher.app.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.data.model.promptKey
import java.io.ByteArrayOutputStream
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
    fun `a copy is stored as a JPEG and an old PNG of the same view is replaced`() = runTest {
        // Given an old PNG copy of the view
        val size = com.facetlauncher.app.data.model.ScreenSize(40, 80)
        val old = File(context.filesDir, "wallpaper_copy_${size.key}.png")
        old.writeBytes(ByteArrayOutputStream().also { bitmap(40, 80).compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray())

        // When the view is saved again
        assertTrue(repository().save(bitmap(40, 80), size))

        // Then the copy is a JPEG and the PNG is gone
        val jpeg = File(context.filesDir, "wallpaper_copy_${size.key}.jpg")
        assertTrue(jpeg.exists())
        assertEquals(0xFF.toByte(), jpeg.readBytes()[0])
        assertEquals(0xD8.toByte(), jpeg.readBytes()[1])
        assertFalse(old.exists())
        jpeg.delete()
    }

    @Test
    fun `an old PNG copy is still read until that view is saved again`() = runTest {
        // Given a copy stored by an earlier version, as a PNG
        val size = com.facetlauncher.app.data.model.ScreenSize(40, 80)
        val old = File(context.filesDir, "wallpaper_copy_${size.key}.png")
        old.writeBytes(ByteArrayOutputStream().also { bitmap(40, 80).compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray())

        // When a fresh repository reads its storage
        val anywhere = repository().hasCopyAnywhere.first()

        // Then the copy is there
        assertTrue(anywhere)
        old.delete()
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

    @Test
    fun `the original is kept untouched with its framing and system id and read back by a new repository`() = runTest {
        // Given an original and the framing the user chose
        val bytes = ByteArray(1000) { (it % 251).toByte() }
        repository().saveOriginal(bytes, android.graphics.RectF(0.1f, 0.2f, 0.6f, 0.9f), 42)

        // When a new repository reads it
        val kept = repository().original()

        // Then the bytes are exactly as given, with the framing and the id
        assertEquals(bytes.toList(), kept!!.file.readBytes().toList())
        assertEquals(android.graphics.RectF(0.1f, 0.2f, 0.6f, 0.9f), kept.framing)
        assertEquals(42, kept.systemId)
    }

    @Test
    fun `a later original replaces the earlier one`() = runTest {
        // Given an original, then another
        val repository = repository()
        repository.saveOriginal(byteArrayOf(1), android.graphics.RectF(0f, 0f, 1f, 1f), 1)
        repository.saveOriginal(byteArrayOf(2, 3), android.graphics.RectF(0.5f, 0f, 1f, 1f), 2)

        // Then only the later one is kept
        val kept = repository.original()!!
        assertEquals(listOf<Byte>(2, 3), kept.file.readBytes().toList())
        assertEquals(2, kept.systemId)
    }

    @Test
    fun `there is no original until one is saved and removing the copies deletes it`() = runTest {
        // Given nothing saved
        val repository = repository()
        assertNull(repository.original())

        // When an original is saved and the copies removed
        repository.saveOriginal(byteArrayOf(1), android.graphics.RectF(0f, 0f, 1f, 1f), 1)
        assertNotNull(repository.original())
        repository.remove()

        // Then the original is gone too
        assertNull(repository.original())
    }

    /** A repository on a screen the test can change, as a foldable does when it folds, and turn, as a phone does when it rotates. */
    private inner class FoldableRepository(start: ScreenSize) : WallpaperCopyRepository(context, wallpaperManager) {
        /** The view showing: the window's size in the orientation it is in. */
        val screen = MutableStateFlow(start)
        private var rotated = false
        override fun screenSize() = (if (rotated) screen.value.turned else screen.value).let { it.width to it.height }
        override fun windowSize() = screen.value.width to screen.value.height
        override val currentScreen: Flow<ScreenSize> get() = screen

        /** Turns the phone a quarter. */
        fun rotate() {
            rotated = !rotated
            screen.value = screen.value.turned
        }
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

        // Then the outer screen is reported as lacking a current copy, and its own old one is reported out of date rather than borrowing the inner one
        val screens = repository.screens.first()
        assertTrue(screens.first { it.size == inner }.hasCopy)
        assertFalse(screens.first { it.size == outer }.hasCopy)
        repository.screen.value = outer
        assertEquals(10, repository.copy.first()!!.width)
        assertFalse(repository.copyIsCurrent.first())
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
    fun `a screen without a copy shows none even when another screen has one`() = runTest {
        // Given only the outer screen has a copy
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))

        // When the inner screen is showing
        repository.screen.value = inner

        // Then it shows nothing, rather than another screen's copy
        assertNull(repository.copy.first())
    }

    @Test
    fun `each orientation of a screen keeps its own copy`() = runTest {
        // Given a portrait copy and a landscape copy of one phone
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.rotate()
        repository.save(bitmap(30, 15))

        // When the phone is turned back and forth
        val landscape = repository.copy.first()
        repository.rotate()
        val portrait = repository.copy.first()

        // Then each orientation shows its own copy, as it was stored
        assertEquals(30, landscape!!.width)
        assertEquals(10, portrait!!.width)
    }

    @Test
    fun `an orientation without a copy shows none while the other has one`() = runTest {
        // Given only a portrait copy
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))

        // When the phone is turned
        repository.rotate()

        // Then there is no copy, and it is not reported as out of date
        assertNull(repository.copy.first())
        assertTrue(repository.copyIsCurrent.first())
    }

    @Test
    fun `a turned view is the same screen not another one`() = runTest {
        // Given a portrait copy and a landscape copy of one phone
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.rotate()
        repository.save(bitmap(30, 15))

        // Then one screen is known, with a view in each orientation
        assertEquals(listOf(outer), repository.knownScreens())
        assertEquals(listOf(outer, outer.turned), repository.screens.first().map { it.size })
        assertEquals(listOf(outer), repository.screens.first().map { it.screen }.distinct())
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

        // Then both screens are listed in each orientation, the larger first, and only the outer's own view has a copy
        assertEquals(
            listOf(
                ScreenCopy(inner, hasCopy = false),
                ScreenCopy(inner.turned, hasCopy = false, screen = inner),
                ScreenCopy(outer, hasCopy = true),
                ScreenCopy(outer.turned, hasCopy = false, screen = outer),
            ),
            screens,
        )
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
        assertEquals(listOf(ScreenCopy(outer, hasCopy = false), ScreenCopy(outer.turned, hasCopy = false, screen = outer)), repository.screens.first())
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

    @Test
    fun `an original is streamed from its uri untouched`() = runTest {
        // Given an image file with a uri
        val source = File(context.cacheDir, "source.jpg").apply { writeBytes(ByteArray(5000) { (it % 97).toByte() }) }

        // When it is kept from that uri
        val kept = repository().saveOriginal(android.net.Uri.fromFile(source), android.graphics.RectF(0f, 0f, 1f, 1f), 3)

        // Then the stored file is the same bytes, with its id
        assertTrue(kept)
        val stored = repository().original()!!
        assertEquals(source.readBytes().toList(), stored.file.readBytes().toList())
        assertEquals(3, stored.systemId)
    }

    @Test
    fun `an original that cannot be read leaves the earlier one alone`() = runTest {
        // Given an original already kept, and a uri that cannot be opened
        val repository = repository()
        repository.saveOriginal(byteArrayOf(9), android.graphics.RectF(0f, 0f, 1f, 1f), 1)

        // When keeping the unreadable one
        val kept = repository.saveOriginal(android.net.Uri.parse("content://missing/none"), android.graphics.RectF(0f, 0f, 1f, 1f), 2)

        // Then it reports failure and the earlier original is intact
        assertFalse(kept)
        assertEquals(1, repository.original()!!.systemId)
        assertEquals(listOf<Byte>(9), repository.original()!!.file.readBytes().toList())
    }

    @Test
    fun `the original's id is read without the image and the original can be deleted alone`() = runTest {
        // Given an original and a copy
        val repository = repository()
        repository.saveOriginal(byteArrayOf(1), android.graphics.RectF(0f, 0f, 1f, 1f), 11)
        repository.save(bitmap(10, 20), ScreenSize(10, 20))
        assertEquals(11, repository.originalSystemId())

        // When only the original is deleted
        repository.deleteOriginal()

        // Then the id is gone and the copy stays
        assertNull(repository.originalSystemId())
        assertTrue(File(context.filesDir, "wallpaper_copy_10x20.jpg").exists())
    }

    @Test
    fun `the same screen at another resolution replaces the old size and its copies`() = runTest {
        // Given a copy for a screen at one resolution
        val repository = FoldableRepository(ScreenSize(1080, 2400))
        repository.save(bitmap(10, 20))
        repository.save(bitmap(30, 15), ScreenSize(2400, 1080))
        repository.copy.first()

        // When the display size is changed, so the same shape appears at a smaller size
        repository.screen.value = ScreenSize(720, 1600)
        repository.copy.first()

        // Then only the new size is known, and the old copies are deleted
        assertEquals(listOf(ScreenSize(720, 1600)), repository.knownScreens())
        assertFalse(File(context.filesDir, "wallpaper_copy_1080x2400.png").exists())
        assertFalse(File(context.filesDir, "wallpaper_copy_2400x1080.png").exists())
    }

    @Test
    fun `a screen of another shape is another screen not a resolution`() = runTest {
        // Given a foldable's outer screen with a copy
        val repository = FoldableRepository(outer)
        repository.save(bitmap(10, 20))
        repository.copy.first()

        // When the inner screen shows
        repository.screen.value = inner
        repository.copy.first()

        // Then both are known and the outer copy stays
        assertEquals(setOf(outer, inner), repository.knownScreens().toSet())
        assertTrue(File(context.filesDir, "wallpaper_copy_1080x2092.jpg").exists())
    }

    /** A repository on a fixed screen and wallpaper id, so the stored shape can be judged against them. */
    private fun repositoryOn(width: Int, height: Int, systemId: Int) = object : WallpaperCopyRepository(context, wallpaperManager) {
        override fun windowSize() = width to height
        override fun currentSystemWallpaperId() = systemId
    }

    @Test
    fun `the offset is the middle for a wallpaper Facet did not set`() {
        // Given no recorded wallpaper
        val repository = repositoryOn(1080, 2400, systemId = 7)

        // Then the offset is half way
        assertEquals(0.5f, repository.wallpaperOffsetX(), 0f)
    }

    @Test
    fun `the offset is half way in the orientation the wallpaper was not framed in`() {
        // Given a wallpaper framed in portrait, recorded for the current id, and a phone held sideways
        repositoryOn(1080, 2400, systemId = 7).recordWallpaperShape(7, 2400, 2400, framedLandscape = false)
        val sideways = repositoryOn(2400, 1080, systemId = 7)

        // Then the offset is half way
        assertEquals(0.5f, sideways.wallpaperOffsetX(), 0f)
    }

    @Test
    fun `the offset is half way once the system wallpaper has changed since it was recorded`() {
        // Given a recorded wallpaper for id 7 and a different id now
        repositoryOn(1080, 2400, systemId = 7).recordWallpaperShape(7, 2400, 2400, framedLandscape = false)

        // Then the offset is half way
        assertEquals(0.5f, repositoryOn(1080, 2400, systemId = 8).wallpaperOffsetX(), 0f)
    }

    @Test
    fun `the offset is worked out from the recorded size in the orientation it was framed in`() {
        // Given a square wallpaper framed in portrait and the phone held upright
        val repository = repositoryOn(1080, 2400, systemId = 7)
        repository.recordWallpaperShape(7, 2400, 2400, framedLandscape = false)

        // Then the offset is the one for that shape, whatever Android version the test runs on (before 14 it is half way)
        val expected = com.facetlauncher.app.domain.wallpaperOffsetX(2400, 2400, 1080, 2400)
        assertEquals(expected, repository.wallpaperOffsetX(), 0.0001f)
    }
}
