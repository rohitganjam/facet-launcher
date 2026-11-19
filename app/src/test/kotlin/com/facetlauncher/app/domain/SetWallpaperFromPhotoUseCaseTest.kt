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
import com.facetlauncher.app.data.model.ScreenSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
        val savedBySize = mutableMapOf<ScreenSize, Bitmap>()
        var extraScreens: List<ScreenSize> = emptyList()
        var removed = false
        var original: ByteArray? = null
        var originalFraming: RectF? = null
        var originalId: Int? = null
        var keepsOriginal = true
        var originalUri: android.net.Uri? = null

        override suspend fun knownScreens(): List<ScreenSize> = (extraScreens + ScreenSize(50, 100)).sortedByDescending { it.area }

        override fun screenSize() = 50 to 100
        override fun windowSize() = 50 to 100
        override fun currentSystemWallpaperId() = systemId
        override suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean): Int {
            setBitmap = bitmap
            setIncludeLock = includeLock
            return systemId
        }
        override suspend fun save(bitmap: Bitmap): Boolean {
            if (saves) saved = bitmap
            return saves
        }
        override suspend fun save(bitmap: Bitmap, size: ScreenSize): Boolean {
            if (saves) savedBySize[size] = bitmap
            return save(bitmap).also { if (!saves) savedBySize.remove(size) }
        }
        override suspend fun remove() { removed = true }
        override suspend fun saveOriginal(bytes: ByteArray, framing: RectF, systemId: Int): Boolean {
            if (!keepsOriginal) return false
            original = bytes
            originalFraming = framing
            originalId = systemId
            return true
        }
        override suspend fun saveOriginal(uri: android.net.Uri, framing: RectF, systemId: Int): Boolean {
            if (!keepsOriginal) return false
            originalUri = uri
            originalFraming = framing
            originalId = systemId
            return true
        }
    }

    private fun photo() = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888).also { it.eraseColor(Color.RED) }

    private fun useCase(copies: FakeCopies, settings: SettingsRepository) =
        SetWallpaperFromPhotoUseCase(copies, settings, UnconfinedTestDispatcher())

    private fun halves(width: Int = 200, height: Int = 100) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        for (x in 0 until width) for (y in 0 until height) bitmap.setPixel(x, y, if (x < width / 2) Color.RED else Color.BLUE)
    }

    @Test
    fun `a photo framed edge to edge is set as framed and every view gets a copy`() = runTest {
        // Given a photo framed to the full height of the screen's shape, with no room for a square around it
        val copies = FakeCopies(systemId = 9)
        val settings = settingsRepository()

        // When setting it for Home only
        val done = useCase(copies, settings)(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)

        // Then the framed part, at the screen's size, was set and the id recorded
        assertTrue(done.set)
        assertEquals(50, copies.setBitmap!!.width)
        assertEquals(100, copies.setBitmap!!.height)
        assertEquals(false, copies.setIncludeLock)
        assertEquals(9, settings.settings.first().wallpaperCopySystemId)
        // And the screen has a copy in each orientation, at the sizes of its views
        assertEquals(setOf(ScreenSize(50, 100), ScreenSize(100, 50)), copies.savedBySize.keys)
    }

    private fun TestScope.backgroundUseCase(copies: FakeCopies) = SetWallpaperFromPhotoUseCase(
        copies, settingsRepository(), UnconfinedTestDispatcher(testScheduler), CoroutineScope(Job() + StandardTestDispatcher(testScheduler)),
    )

    @Test
    fun `only the view showing is stored before it returns and the others finish in the background`() = runTest {
        // Given a screen with two views, one of them showing
        val copies = FakeCopies(systemId = 9)

        // When the wallpaper is set
        val done = backgroundUseCase(copies)(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)

        // Then it returns with the view showing stored, the other one still to come, and the copies marked as being finished
        assertTrue(done.set)
        assertEquals(setOf(ScreenSize(50, 100)), copies.savedBySize.keys)
        assertTrue(copies.isFinishingCopies.first())

        // And once the background work has run the other view has its copy and the mark is gone
        testScheduler.advanceUntilIdle()
        assertEquals(setOf(ScreenSize(50, 100), ScreenSize(100, 50)), copies.savedBySize.keys)
        assertFalse(copies.isFinishingCopies.first())
    }

    @Test
    fun `the others are not stored when the system wallpaper changed meanwhile`() = runTest {
        // Given the wallpaper set and the background work not yet run
        val copies = FakeCopies(systemId = 9)
        backgroundUseCase(copies)(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)

        // When the system wallpaper is changed elsewhere before it runs
        copies.systemId = 10
        testScheduler.advanceUntilIdle()

        // Then no stale copy is stored for the other view, and the mark is gone
        assertEquals(setOf(ScreenSize(50, 100)), copies.savedBySize.keys)
        assertFalse(copies.isFinishingCopies.first())
    }

    @Test
    fun `a failed copy for the view showing ends the finishing mark and stores nothing else`() = runTest {
        // Given the system takes the wallpaper but the first copy cannot be stored
        val copies = FakeCopies(saves = false)

        // When it is set
        val done = backgroundUseCase(copies)(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)
        testScheduler.advanceUntilIdle()

        // Then it fails as a whole and nothing is left marked as being finished
        assertFalse(done.set)
        assertTrue(copies.savedBySize.isEmpty())
        assertFalse(copies.isFinishingCopies.first())
    }

    @Test
    fun `a photo with room is set as a square of the longest side and both orientations are cut from it`() = runTest {
        // Given a photo, red on the left and blue on the right, framed around its middle with room for a square
        val copies = FakeCopies()

        // When setting it
        val done = useCase(copies, settingsRepository())(halves(), RectF(75f, 0f, 125f, 100f), includeLock = false)

        // Then the wallpaper is a square of the screen's longest side, red then blue
        assertTrue(done.set)
        assertEquals(100, copies.setBitmap!!.width)
        assertEquals(100, copies.setBitmap!!.height)
        assertEquals(Color.RED, copies.setBitmap!!.getPixel(5, 50))
        assertEquals(Color.BLUE, copies.setBitmap!!.getPixel(95, 50))
        // And the portrait and landscape views each show the middle of it, both colours
        val portrait = copies.savedBySize[ScreenSize(50, 100)]!!
        val landscape = copies.savedBySize[ScreenSize(100, 50)]!!
        assertEquals(Color.RED, portrait.getPixel(1, 50))
        assertEquals(Color.BLUE, portrait.getPixel(48, 50))
        assertEquals(Color.RED, landscape.getPixel(1, 25))
        assertEquals(Color.BLUE, landscape.getPixel(98, 25))
    }

    @Test
    fun `with a larger screen known the wallpaper is set for it and every view of both screens gets a copy`() = runTest {
        // Given a foldable: the current 50 x 100 screen and a larger 100 x 80 one, and a photo that is red on the left and blue on the right
        val copies = FakeCopies().apply { extraScreens = listOf(ScreenSize(100, 80)) }

        // When setting the whole photo
        val done = useCase(copies, settingsRepository())(halves(), RectF(0f, 0f, 200f, 100f), includeLock = false)

        // Then the system wallpaper keeps the framing's shape, its longer side as long as the longest screen side
        assertTrue(done.set)
        assertEquals(100, copies.setBitmap!!.width)
        assertEquals(50, copies.setBitmap!!.height)
        // And each screen has a copy of each orientation, cut from the middle so both halves show
        assertEquals(setOf(ScreenSize(100, 80), ScreenSize(80, 100), ScreenSize(50, 100), ScreenSize(100, 50)), copies.savedBySize.keys)
        val small = copies.savedBySize[ScreenSize(50, 100)]!!
        assertEquals(Color.RED, small.getPixel(1, 50))
        assertEquals(Color.BLUE, small.getPixel(48, 50))
    }

    @Test
    fun `the original and the framing are kept when there is one`() = runTest {
        // Given a photo picked from a file
        val copies = FakeCopies(systemId = 9)
        val file = byteArrayOf(1, 2, 3)

        // When setting it with its original
        useCase(copies, settingsRepository())(halves(), RectF(50f, 0f, 150f, 100f), includeLock = false, original = KeptOriginal.Bytes(file))

        // Then the file is kept untouched with the framing as fractions of the photo and the system id
        assertEquals(file.toList(), copies.original!!.toList())
        assertEquals(RectF(0.25f, 0f, 0.75f, 1f), copies.originalFraming)
        assertEquals(9, copies.originalId)
    }

    @Test
    fun `no original is kept without one or when the wallpaper was refused`() = runTest {
        // Given a photo with no file, and another whose wallpaper the system refuses
        val kept = FakeCopies()
        useCase(kept, settingsRepository())(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)
        val refused = FakeCopies(systemId = WallpaperCopyRepository.NO_WALLPAPER_ID)
        useCase(refused, settingsRepository())(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false, original = KeptOriginal.Bytes(byteArrayOf(1)))

        // Then neither keeps an original
        assertNull(kept.original)
        assertNull(refused.original)
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
        assertFalse(done.set)
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
        assertFalse(done.set)
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

    @Test
    fun `a picked file is kept by its place rather than read into memory`() = runTest {
        // Given a photo picked from a file at a uri
        val copies = FakeCopies(systemId = 4)
        val uri = android.net.Uri.parse("content://media/external/images/media/1")

        // When setting it with that uri as its original
        val done = useCase(copies, settingsRepository())(halves(), RectF(50f, 0f, 150f, 100f), includeLock = false, original = KeptOriginal.Picked(uri))

        // Then the uri was handed on to be streamed, with the framing and id, and nothing was held as bytes
        assertTrue(done.set)
        assertTrue(done.originalKept)
        assertEquals(uri, copies.originalUri)
        assertEquals(4, copies.originalId)
        assertNull(copies.original)
    }

    @Test
    fun `a wallpaper whose original could not be kept is still set and says so`() = runTest {
        // Given storage that cannot keep the original
        val copies = FakeCopies().apply { keepsOriginal = false }
        val settings = settingsRepository()

        // When setting a wallpaper with an original
        val done = useCase(copies, settings)(halves(), RectF(50f, 0f, 150f, 100f), includeLock = false, original = KeptOriginal.Bytes(byteArrayOf(1)))

        // Then the wallpaper and its copies stand, the id is recorded, and the result says the original was not kept
        assertTrue(done.set)
        assertFalse(done.originalKept)
        assertEquals(9, settings.settings.first().wallpaperCopySystemId)
    }

    @Test
    fun `with no original to keep the result says nothing was lost`() = runTest {
        // Given a photo with no original
        val done = useCase(FakeCopies(), settingsRepository())(photo(), RectF(0f, 0f, 50f, 100f), includeLock = false)

        // Then it counts as kept
        assertTrue(done.originalKept)
    }
}
