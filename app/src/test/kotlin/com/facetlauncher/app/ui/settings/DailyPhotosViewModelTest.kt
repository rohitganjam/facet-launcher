package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.graphics.Bitmap
import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoApi
import com.facetlauncher.app.data.dailyphotos.DailyPhotoCache
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.NetworkConditions
import com.facetlauncher.app.domain.DailyWallpaperCoordinator
import com.facetlauncher.app.domain.FakeConditions
import com.facetlauncher.app.domain.FakeDailyWallpaperRepository
import com.facetlauncher.app.domain.dailyTestClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DailyPhotosViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun photo(name: String) = DailyPhoto(title = name, credit = "© $name", thumbUrl = "https://img.peapix.com/${name}_t.jpg", imageUrl = "https://img.peapix.com/$name.jpg")

    /** A repository the test controls: what each source gives, and what a tapped photo's original is. */
    private class FakeRepository(api: DailyPhotoApi, cache: DailyPhotoCache, copies: WallpaperCopyRepository) :
        DailyPhotoRepository(api, cache, copies, Dispatchers.Unconfined) {
        val feeds = mutableMapOf<DailyPhotoSource, DailyPhotoFeed>()
        var original: Bitmap? = null
        val requested = mutableListOf<DailyPhotoSource>()

        override suspend fun photos(source: DailyPhotoSource): DailyPhotoFeed {
            requested += source
            return feeds[source] ?: DailyPhotoFeed.Unavailable
        }
        override suspend fun original(photo: DailyPhoto, maxSide: Int): Bitmap? = original
        override suspend fun thumbnail(source: DailyPhotoSource, photo: DailyPhoto): Bitmap? = null
    }

    private fun repository(): FakeRepository {
        val api = object : DailyPhotoApi {
            override suspend fun feedJson(source: DailyPhotoSource, country: String): String? = null
            override suspend fun download(url: String, maxBytes: Int): ByteArray? = null
        }
        val cache = DailyPhotoCache(tempFolder.newFolder("cache"), java.time.Clock.systemUTC())
        return FakeRepository(api, cache, WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)))
    }

    private val coordinator: DailyWallpaperCoordinator = mock(DailyWallpaperCoordinator::class.java)
    private val conditions = FakeConditions()
    private var dailyRepository: FakeDailyWallpaperRepository? = null
    private val entitlement = FakeEntitlementRepository()

    private fun TestScope.viewModel(repository: DailyPhotoRepository): DailyPhotosViewModel {
        val dataStoreScope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        val daily = FakeDailyWallpaperRepository(tempFolder.newFolder(), dataStoreScope).also { dailyRepository = it }
        return DailyPhotosViewModel(repository, daily, coordinator, conditions, dailyTestClock, entitlement)
    }

    /** Subscribes to the daily wallpaper state, which only runs while collected. */
    private fun DailyPhotosViewModel.observeDaily(scope: kotlinx.coroutines.test.TestScope) =
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { dailyWallpaper.collect { } }

    @Test
    fun `turning the daily wallpaper on opens the sheet preset to the tab on screen`() = runTest {
        // Given the Spotlight tab is on screen, on Wi-Fi
        val repository = repository()
        val viewModel = viewModel(repository)
        viewModel.observeDaily(this)
        viewModel.selectSource(DailyPhotoSource.SPOTLIGHT)

        // When the switch is turned on
        viewModel.onDailyWallpaperSwitch(true)

        // Then the sheet shows Spotlight, Home only, with no mobile data warning
        assertEquals(DailyWallpaperSheet(DailyPhotoSource.SPOTLIGHT, includeLock = false, onMobileData = false), viewModel.dailyWallpaper.value.sheet)
    }

    @Test
    fun `without pro the switch opens facet pro instead of the sheet`() = runTest {
        // Given a free user
        entitlement.proFlow.value = false
        val viewModel = viewModel(repository())
        viewModel.observeDaily(this)

        // When the switch is turned on
        viewModel.onDailyWallpaperSwitch(true)

        // Then Facet Pro is asked for once, with no sheet and nothing turned on
        assertTrue(viewModel.dailyWallpaper.value.openPro)
        assertNull(viewModel.dailyWallpaper.value.sheet)
        verify(coordinator, never()).turnOn(DailyPhotoSource.BING, false, false)
        viewModel.consumeOpenPro()
        assertFalse(viewModel.dailyWallpaper.value.openPro)
    }

    @Test
    fun `without pro a stored daily wallpaper shows as off`() = runTest {
        // Given the daily wallpaper was turned on, and Pro is gone
        entitlement.proFlow.value = false
        val viewModel = viewModel(repository())
        viewModel.observeDaily(this)

        // When it is stored as on
        dailyRepository!!.update { it.copy(enabled = true, current = DailyWallpaperPhoto("u", "t", "c")) }

        // Then the card shows it off, with the Pro pill and no marked photo
        val state = viewModel.dailyWallpaper.value
        assertFalse(state.enabled)
        assertFalse(state.isPro)
        assertEquals(DailyWallpaperStatus.Off, state.status)
        assertNull(state.currentImageUrl)
    }

    @Test
    fun `on mobile data the sheet offers to use it once`() = runTest {
        // Given a metered network
        val viewModel = viewModel(repository())
        viewModel.observeDaily(this)
        conditions.network = NetworkConditions(connected = true, unmetered = false, roaming = false)

        // When the switch is turned on
        viewModel.onDailyWallpaperSwitch(true)

        // Then the sheet says so
        assertTrue(viewModel.dailyWallpaper.value.sheet!!.onMobileData)
    }

    @Test
    fun `turn on passes the sheet's choices and closes it`() = runTest {
        // Given the sheet with Spotlight and Home and lock chosen
        val viewModel = viewModel(repository())
        viewModel.observeDaily(this)
        viewModel.onDailyWallpaperSwitch(true)
        viewModel.onSheetSource(DailyPhotoSource.SPOTLIGHT)
        viewModel.onSheetIncludeLock(true)

        // When "Use mobile data once" is tapped
        viewModel.confirmTurnOn(useMobileData = true)

        // Then the daily change is turned on with them, and the sheet is gone
        verify(coordinator).turnOn(DailyPhotoSource.SPOTLIGHT, true, true)
        assertNull(viewModel.dailyWallpaper.value.sheet)
    }

    @Test
    fun `turning the switch off turns the daily change off without a sheet`() = runTest {
        // Given the view model
        val viewModel = viewModel(repository())

        // When the switch is turned off
        viewModel.onDailyWallpaperSwitch(false)

        // Then it is off
        verify(coordinator).turnOff()
    }

    @Test
    fun `the photo the daily change set is marked while it is on`() = runTest {
        // Given the daily change on, having set photo a
        val viewModel = viewModel(repository())
        viewModel.observeDaily(this)
        dailyRepository!!.update { it.copy(enabled = true, current = DailyWallpaperPhoto("https://img.peapix.com/a.jpg", "a", "")) }

        // Then the screen knows which tile it is
        val state = viewModel.dailyWallpaper.first { it.enabled }
        assertEquals("https://img.peapix.com/a.jpg", state.currentImageUrl)
    }

    @Test
    fun `it opens on bing's photos`() = runTest {
        // Given bing has two photos
        val repository = repository().apply { feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a"), photo("b")), savedEarlier = false) }

        // When the screen's view model starts
        val state = viewModel(repository).uiState.value

        // Then it shows them, not loading and not failed
        assertEquals(DailyPhotoSource.BING, state.source)
        assertEquals(listOf("a", "b"), state.photos.map { it.title })
        assertFalse(state.loading)
        assertFalse(state.failed)
    }

    @Test
    fun `choosing spotlight loads its photos and choosing the shown source again does not reload`() = runTest {
        // Given both sources have photos
        val repository = repository().apply {
            feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a")), false)
            feeds[DailyPhotoSource.SPOTLIGHT] = DailyPhotoFeed.Photos(listOf(photo("s1"), photo("s2")), false)
        }
        val viewModel = viewModel(repository)

        // When Spotlight is chosen, and then chosen again
        viewModel.selectSource(DailyPhotoSource.SPOTLIGHT)
        viewModel.selectSource(DailyPhotoSource.SPOTLIGHT)

        // Then its photos show, and it was asked for once
        assertEquals(listOf("s1", "s2"), viewModel.uiState.value.photos.map { it.title })
        assertEquals(listOf(DailyPhotoSource.BING, DailyPhotoSource.SPOTLIGHT), repository.requested)
    }

    @Test
    fun `photos saved earlier are marked, and a source with nothing fails and can be retried`() = runTest {
        // Given bing offline with yesterday's photos, and Spotlight with nothing
        val repository = repository().apply { feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a")), savedEarlier = true) }
        val viewModel = viewModel(repository)
        assertTrue(viewModel.uiState.value.savedEarlier)

        // When Spotlight is chosen
        viewModel.selectSource(DailyPhotoSource.SPOTLIGHT)

        // Then it has failed, with no photos
        assertTrue(viewModel.uiState.value.failed)
        assertTrue(viewModel.uiState.value.photos.isEmpty())

        // And once it has something, retrying shows it
        repository.feeds[DailyPhotoSource.SPOTLIGHT] = DailyPhotoFeed.Photos(listOf(photo("s")), false)
        viewModel.retry()
        assertFalse(viewModel.uiState.value.failed)
        assertEquals(listOf("s"), viewModel.uiState.value.photos.map { it.title })
    }

    @Test
    fun `picking a photo downloads its original and hands it on with its credit`() = runTest {
        // Given a photo whose original can be downloaded
        val bitmap = Bitmap.createBitmap(10, 20, Bitmap.Config.ARGB_8888)
        val repository = repository().apply {
            feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a")), false)
            original = bitmap
        }
        val viewModel = viewModel(repository)

        // When it is picked
        viewModel.pick(photo("a"))

        // Then the bitmap and the credit as given are ready, and it is no longer downloading
        val picked = viewModel.uiState.value.picked
        assertNotNull(picked)
        assertSame(bitmap, picked!!.bitmap)
        assertEquals("© a", picked.credit)
        assertFalse(viewModel.uiState.value.downloading)

        // And once handed on it is cleared
        viewModel.consumePicked()
        assertNull(viewModel.uiState.value.picked)
    }

    @Test
    fun `a photo that cannot be downloaded says so and leaves the photos showing`() = runTest {
        // Given a photo whose original fails
        val repository = repository().apply {
            feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a")), false)
            original = null
        }
        val viewModel = viewModel(repository)

        // When it is picked
        viewModel.pick(photo("a"))

        // Then the download failure shows, nothing is picked, and the photos are still there
        val state = viewModel.uiState.first { !it.downloading }
        assertTrue(state.downloadFailed)
        assertNull(state.picked)
        assertEquals(1, state.photos.size)
    }
}
