package com.facetlauncher.app.data.dailyphotos

import android.app.WallpaperManager
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.DailyPhotoSource
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DailyPhotoRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /** A clock the test moves, a day at a time. */
    private class TestClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    private class FakeApi : DailyPhotoApi {
        var feed: String? = null
        var feedCalls = 0
        var lastCountry: String? = null
        override suspend fun feedJson(source: DailyPhotoSource, country: String): String? {
            feedCalls++
            lastCountry = country
            return feed
        }
        override suspend fun download(url: String, maxBytes: Int): ByteArray? = null
    }

    private fun feedOf(vararg names: String) = names.joinToString(prefix = "[", postfix = "]") {
        """{"title":"$it","copyright":"© $it","thumbUrl":"https://img.peapix.com/${it}_640.jpg","imageUrl":"https://img.peapix.com/$it.jpg","date":"2026-11-12"}"""
    }

    private val clock = TestClock(Instant.parse("2026-11-12T09:00:00Z"))
    private val api = FakeApi()
    private lateinit var cacheDir: File

    private fun repository(): DailyPhotoRepository {
        cacheDir = tempFolder.newFolder("daily")
        val copies = WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java))
        return DailyPhotoRepository(api, DailyPhotoCache(cacheDir, clock), copies, Dispatchers.Unconfined)
    }

    @Test
    fun `the first open of the day fetches and every later open that day uses the cache`() = runTest {
        // Given a feed and a fresh cache
        api.feed = feedOf("a", "b")
        val repository = repository()

        // When the photos are opened three times in one day
        val first = repository.photos(DailyPhotoSource.BING, "us")
        clock.now = Instant.parse("2026-11-12T22:30:00Z")
        repository.photos(DailyPhotoSource.BING, "us")
        val third = repository.photos(DailyPhotoSource.BING, "us")

        // Then the service was asked once, and every answer has the two photos, none marked as old
        assertEquals(1, api.feedCalls)
        assertEquals(DailyPhotoFeed.Photos(listOf("a", "b").map { name -> (first as DailyPhotoFeed.Photos).photos.first { it.title == name } }, false), third)
        assertEquals(2, (first as DailyPhotoFeed.Photos).photos.size)
    }

    @Test
    fun `the first open of a new day fetches again`() = runTest {
        // Given a feed cached yesterday
        api.feed = feedOf("a")
        val repository = repository()
        repository.photos(DailyPhotoSource.BING, "us")

        // When it is opened the next day, with a new feed up
        clock.now = Instant.parse("2026-11-13T00:05:00Z")
        api.feed = feedOf("c")
        val photos = repository.photos(DailyPhotoSource.BING, "us") as DailyPhotoFeed.Photos

        // Then the service is asked again and the new photo shows
        assertEquals(2, api.feedCalls)
        assertEquals(listOf("c"), photos.photos.map { it.title })
        assertEquals(false, photos.savedEarlier)
    }

    @Test
    fun `offline on a new day shows the photos saved earlier`() = runTest {
        // Given yesterday's feed in the cache, and no connection today
        api.feed = feedOf("a", "b")
        val repository = repository()
        repository.photos(DailyPhotoSource.BING, "us")
        clock.now = Instant.parse("2026-11-13T08:00:00Z")
        api.feed = null

        // When opened
        val photos = repository.photos(DailyPhotoSource.BING, "us") as DailyPhotoFeed.Photos

        // Then yesterday's photos show, marked as saved earlier
        assertEquals(2, photos.photos.size)
        assertTrue(photos.savedEarlier)
    }

    @Test
    fun `with no cache and no connection nothing is available, and a broken feed counts as no connection`() = runTest {
        // Given nothing cached and a service that fails
        val repository = repository()
        api.feed = null
        assertEquals(DailyPhotoFeed.Unavailable, repository.photos(DailyPhotoSource.BING, "us"))

        // And one that answers with something that is not a feed
        api.feed = "<html>busy</html>"
        assertEquals(DailyPhotoFeed.Unavailable, repository.photos(DailyPhotoSource.BING, "us"))

        // And an empty feed
        api.feed = "[]"
        assertEquals(DailyPhotoFeed.Unavailable, repository.photos(DailyPhotoSource.BING, "us"))
    }

    @Test
    fun `each source and each bing country has its own cache`() = runTest {
        // Given Bing for the US cached
        api.feed = feedOf("a")
        val repository = repository()
        repository.photos(DailyPhotoSource.BING, "us")

        // When Bing for Germany and Spotlight are opened the same day
        repository.photos(DailyPhotoSource.BING, "de")
        repository.photos(DailyPhotoSource.SPOTLIGHT, "de")

        // Then each had its own fetch
        assertEquals(3, api.feedCalls)
        assertEquals("de", api.lastCountry)
    }

    @Test
    fun `a new feed drops the thumbnails it no longer lists`() {
        // Given two cached thumbnails
        val cache = DailyPhotoCache(tempFolder.newFolder("cache"), clock)
        cache.writeThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/old.jpg", byteArrayOf(1))
        cache.writeThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/keep.jpg", byteArrayOf(2))

        // When a feed that lists only one of them is stored
        cache.writeFeed(DailyPhotoSource.BING, "us", "[]", listOf("https://img.peapix.com/keep.jpg"))

        // Then the other is gone
        assertNull(cache.readThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/old.jpg"))
        assertEquals(2, cache.readThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/keep.jpg")!![0].toInt())
    }
}
