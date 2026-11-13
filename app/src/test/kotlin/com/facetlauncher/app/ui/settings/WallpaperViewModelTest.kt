package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.WallpaperRepository
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.domain.FakeDailyWallpaperRepository
import com.facetlauncher.app.domain.NoteManualWallpaperUseCase
import com.facetlauncher.app.domain.RemoveWallpaperCopyUseCase
import com.facetlauncher.app.domain.dailyTestClock
import com.facetlauncher.app.domain.SetWallpaperFromPhotoUseCase
import com.facetlauncher.app.domain.UseScreenshotAsWallpaperCopyUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WallpaperViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(dispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    /** A real (not Mockito) stub of the copy store: a copy the test controls, a system id that can change, and a decode that can fail. */
    private class FakeCopies : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        val stored = MutableStateFlow<Bitmap?>(null)
        var systemId = 1
        var decoded: Bitmap? = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        var setSucceeds = true

        override val copy: Flow<Bitmap?> get() = stored
        override fun currentSystemWallpaperId() = systemId
        override fun portraitScreenSize() = 50 to 100
        override suspend fun decodePicked(uri: Uri, maxSide: Int) = decoded
        override suspend fun setSystemWallpaper(bitmap: Bitmap, includeLock: Boolean) = if (setSucceeds) systemId else NO_WALLPAPER_ID
        override suspend fun save(bitmap: Bitmap): Boolean {
            stored.value = bitmap
            return true
        }
        override suspend fun remove() { stored.value = null }
    }

    private fun settings(): SettingsRepository {
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { tempFolder.newFile("t-${System.nanoTime()}.preferences_pb") })
        return SettingsRepository(dataStore)
    }

    private var dailyRepository: FakeDailyWallpaperRepository? = null

    private fun viewModel(copies: FakeCopies, settings: SettingsRepository): WallpaperViewModel {
        val wallpaper = object : WallpaperRepository(mock(WallpaperManager::class.java)) {
            override suspend fun currentHomeWallpaper(): HomeWallpaper = HomeWallpaper.Tones(listOf(1, 2))
        }
        val daily = dailyRepository ?: FakeDailyWallpaperRepository(tempFolder.newFolder()).also { dailyRepository = it }
        return WallpaperViewModel(
            copies, wallpaper, settings, SetWallpaperFromPhotoUseCase(copies, settings, dispatcher), RemoveWallpaperCopyUseCase(copies, settings),
            UseScreenshotAsWallpaperCopyUseCase(copies, settings, dispatcher), daily, NoteManualWallpaperUseCase(daily, copies, dailyTestClock),
        )
    }

    /** Subscribes straight away, so the state flow is live (it only runs while collected). */
    private fun WallpaperViewModel.observe(scope: kotlinx.coroutines.test.TestScope) =
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { uiState.collect { } }

    @Test
    fun `with no copy there is nothing stale and the system wallpaper is previewed`() = runTest {
        // Given no copy
        val viewModel = viewModel(FakeCopies(), settings())
        viewModel.observe(this)

        // When the preview of the system wallpaper has loaded
        val state = viewModel.uiState.first { it.systemWallpaper != HomeWallpaper.Unavailable }

        // Then it is not stale and the tones show
        assertNull(state.copy)
        assertFalse(state.stale)
        assertEquals(HomeWallpaper.Tones(listOf(1, 2)), state.systemWallpaper)
    }

    @Test
    fun `a copy whose recorded id differs from the system's is stale`() = runTest {
        // Given a copy stored when the system id was 1, and the system now at 2
        val copies = FakeCopies().apply { stored.value = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888) }
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        copies.systemId = 2
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)

        // When the screen checks again
        viewModel.refresh()

        // Then the copy is stale
        assertTrue(viewModel.uiState.first { it.copy != null && it.stale }.stale)
    }

    @Test
    fun `a copy whose id still matches is not stale`() = runTest {
        // Given a copy recorded at the system's current id
        val copies = FakeCopies().apply { stored.value = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888) }
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)

        // Then, once it has loaded, it is not stale
        assertFalse(viewModel.uiState.first { it.copy != null }.stale)
    }

    @Test
    fun `picking a photo shows it for framing, and an unreadable one is a failure`() = runTest {
        // Given a readable photo
        val copies = FakeCopies()
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)

        // When one is picked
        viewModel.onPicked(Uri.parse("content://photo/1"))

        // Then it is there to frame
        val framing = viewModel.uiState.first { it.picked != null }
        assertFalse(framing.failed)

        // And when the next one cannot be read, it fails and nothing is kept for framing
        viewModel.cancelFit()
        viewModel.uiState.first { it.picked == null }
        copies.decoded = null
        viewModel.onPicked(Uri.parse("content://photo/2"))
        val failed = viewModel.uiState.first { it.failed }
        assertNull(failed.picked)
    }

    @Test
    fun `confirming the fit stores the copy, clears the framing and records the id`() = runTest {
        // Given a photo being framed
        val copies = FakeCopies().apply { systemId = 4 }
        val settings = settings()
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)
        viewModel.onPicked(Uri.parse("content://photo/1"))
        viewModel.uiState.first { it.picked != null }

        // When confirmed
        viewModel.confirmFit(RectF(0f, 0f, 50f, 100f), includeLock = false)

        // Then the copy is stored and framing is over, the id recorded, and it is not stale
        val state = viewModel.uiState.first { it.copy != null && it.picked == null && !it.isWorking }
        assertFalse(state.failed)
        assertFalse(state.stale)
        assertEquals(4, settings.settings.first { it.wallpaperCopySystemId == 4 }.wallpaperCopySystemId)
    }

    @Test
    fun `a refused wallpaper keeps the photo for another try and reports failure`() = runTest {
        // Given the system refuses to set it
        val copies = FakeCopies().apply { setSucceeds = false }
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)
        viewModel.onPicked(Uri.parse("content://photo/1"))
        viewModel.uiState.first { it.picked != null }

        // When confirmed
        viewModel.confirmFit(RectF(0f, 0f, 50f, 100f), includeLock = true)

        // Then it failed, no copy exists, and the photo is still there to frame again
        val state = viewModel.uiState.first { it.failed }
        assertNull(state.copy)
        assertNotNull(state.picked)
    }

    @Test
    fun `removing the copy clears it and its id`() = runTest {
        // Given a stored copy
        val copies = FakeCopies().apply { stored.value = Bitmap.createBitmap(5, 5, Bitmap.Config.ARGB_8888) }
        val settings = settings().also { it.setWallpaperCopySystemId(1) }
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)
        viewModel.uiState.first { it.copy != null }

        // When removed
        viewModel.removeCopy()

        // Then there is no copy and no id
        viewModel.uiState.first { it.copy == null }
        assertEquals(-1, settings.settings.first { it.wallpaperCopySystemId == -1 }.wallpaperCopySystemId)
    }

    @Test
    fun `a wallpaper changed in the system picker with no matching copy offers to add one`() = runTest {
        // Given no copy and the picker opened at id 1
        val copies = FakeCopies()
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)
        viewModel.onSystemPickerOpened()

        // When the wallpaper changes and the screen comes back
        copies.systemId = 2
        viewModel.refresh()

        // Then the offer shows
        assertTrue(viewModel.uiState.first { it.offerCopy }.offerCopy)
    }

    @Test
    fun `an unchanged wallpaper or a matching copy offers nothing`() = runTest {
        // Given the picker opened and closed without a change
        val copies = FakeCopies()
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)
        viewModel.onSystemPickerOpened()
        viewModel.refresh()
        viewModel.uiState.first { it.systemWallpaper != HomeWallpaper.Unavailable }

        // Then there is no offer
        assertFalse(viewModel.uiState.value.offerCopy)
    }

    @Test
    fun `declining the offer is remembered and a later change is not offered`() = runTest {
        // Given an offer showing
        val copies = FakeCopies()
        val settings = settings()
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)
        viewModel.onSystemPickerOpened()
        copies.systemId = 2
        viewModel.refresh()
        viewModel.uiState.first { it.offerCopy }

        // When declined
        viewModel.declineOffer()

        // Then it is gone and remembered, so another change does not bring it back
        viewModel.uiState.first { !it.offerCopy }
        assertTrue(settings.settings.first { it.wallpaperPromptDismissed }.wallpaperPromptDismissed)
        viewModel.onSystemPickerOpened()
        copies.systemId = 3
        viewModel.refresh()
        viewModel.uiState.first { it.systemWallpaper != HomeWallpaper.Unavailable }
        assertFalse(viewModel.uiState.value.offerCopy)
    }

    @Test
    fun `a screenshot is stored as the copy without setting the wallpaper`() = runTest {
        // Given the system wallpaper at id 7, which must not be set again, and a screenshot being framed
        val copies = FakeCopies().apply { systemId = 7; setSucceeds = false }
        val settings = settings()
        val viewModel = viewModel(copies, settings)
        viewModel.observe(this)
        viewModel.onPickStarted(copyOnly = true)
        viewModel.onPicked(Uri.parse("content://shot/1"))
        assertTrue(viewModel.uiState.first { it.picked != null }.copyOnly)

        // When confirmed
        viewModel.confirmFit(RectF(0f, 0f, 50f, 100f), includeLock = true)

        // Then the copy is stored for the current wallpaper, even though setting a wallpaper would have failed
        val state = viewModel.uiState.first { it.copy != null && it.picked == null && !it.isWorking }
        assertFalse(state.failed)
        assertFalse(state.stale)
        assertEquals(7, settings.settings.first { it.wallpaperCopySystemId == 7 }.wallpaperCopySystemId)
    }

    @Test
    fun `a daily photo is framed with its credit, which goes when framing ends`() = runTest {
        // Given a view model on the wallpaper screen
        val viewModel = viewModel(FakeCopies(), settings())
        viewModel.observe(this)

        // When a daily photo comes in
        viewModel.onPickedBitmap(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), "© Someone/Getty Image")

        // Then it is being framed with the credit exactly as given, and is not a screenshot
        val framing = viewModel.uiState.first { it.picked != null }
        assertEquals("© Someone/Getty Image", framing.credit)
        assertFalse(framing.copyOnly)

        // And cancelling clears both
        viewModel.cancelFit()
        val cleared = viewModel.uiState.first { it.picked == null }
        assertNull(cleared.credit)
    }

    @Test
    fun `a blank credit is not shown and confirming the fit clears the credit`() = runTest {
        // Given a daily photo with no credit
        val viewModel = viewModel(FakeCopies().apply { systemId = 4 }, settings())
        viewModel.observe(this)
        viewModel.onPickedBitmap(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), "  ")
        assertNull(viewModel.uiState.first { it.picked != null }.credit)

        // When a daily photo with a credit is confirmed
        viewModel.onPickedBitmap(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), "© X")
        viewModel.confirmFit(RectF(0f, 0f, 50f, 100f), includeLock = false)

        // Then the credit is gone with the framing
        val done = viewModel.uiState.first { it.picked == null && !it.isWorking && it.copy != null }
        assertNull(done.credit)
    }

    @Test
    fun `framing your own image with the daily wallpaper on warns that it turns it off`() = runTest {
        // Given the daily wallpaper on
        val copies = FakeCopies()
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)
        dailyRepository!!.update { it.copy(enabled = true) }

        // When an image of the user's own is picked
        viewModel.onPicked(android.net.Uri.parse("content://photo/1"))

        // Then the fit step says so, and setting it turns the daily change off
        assertTrue(viewModel.uiState.first { it.picked != null }.turnsOffDailyWallpaper)
        viewModel.confirmFit(android.graphics.RectF(0f, 0f, 100f, 100f), includeLock = false)
        assertFalse(dailyRepository!!.state.first { !it.enabled }.enabled)
    }

    @Test
    fun `framing a daily photo keeps the daily wallpaper on`() = runTest {
        // Given the daily wallpaper on
        val copies = FakeCopies()
        val viewModel = viewModel(copies, settings())
        viewModel.observe(this)
        dailyRepository!!.update { it.copy(enabled = true) }

        // When a daily photo is picked and set
        viewModel.onPickedBitmap(Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888), "© someone")
        assertFalse(viewModel.uiState.first { it.picked != null }.turnsOffDailyWallpaper)
        viewModel.confirmFit(android.graphics.RectF(0f, 0f, 100f, 100f), includeLock = false)
        viewModel.uiState.first { it.picked == null && !it.isWorking }

        // Then it stays on, with today counted as done
        val daily = dailyRepository!!.state.first { it.checkedDay != null }
        assertTrue(daily.enabled)
    }
}
