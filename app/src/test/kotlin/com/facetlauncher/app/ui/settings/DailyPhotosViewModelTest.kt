package com.facetlauncher.app.ui.settings

import android.app.WallpaperManager
import android.graphics.Bitmap
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoApi
import com.facetlauncher.app.data.dailyphotos.DailyPhotoCache
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.dailyphotos.DailyPhotoRepository
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

    @Test
    fun `it opens on bing's photos`() = runTest {
        // Given bing has two photos
        val repository = repository().apply { feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(photo("a"), photo("b")), savedEarlier = false) }

        // When the screen's view model starts
        val state = DailyPhotosViewModel(repository).uiState.value

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
        val viewModel = DailyPhotosViewModel(repository)

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
        val viewModel = DailyPhotosViewModel(repository)
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
        val viewModel = DailyPhotosViewModel(repository)

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
        val viewModel = DailyPhotosViewModel(repository)

        // When it is picked
        viewModel.pick(photo("a"))

        // Then the download failure shows, nothing is picked, and the photos are still there
        val state = viewModel.uiState.first { !it.downloading }
        assertTrue(state.downloadFailed)
        assertNull(state.picked)
        assertEquals(1, state.photos.size)
    }
}
