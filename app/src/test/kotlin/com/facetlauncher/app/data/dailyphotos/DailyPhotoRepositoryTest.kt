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
        var downloadBytes: ByteArray? = null
        val downloads = mutableListOf<String>()
        override suspend fun download(url: String, maxBytes: Int): ByteArray? {
            downloads += url
            return downloadBytes
        }
    }

    private fun feedOf(vararg names: String) = names.joinToString(prefix = """{"images":[""", postfix = "]}") {
        """{"startdate":"20261112","urlbase":"/th?id=OHR.$it","title":"$it","copyright":"$it (© $it)"}"""
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
        api.feed = """{"images":[]}"""
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
        cache.writeFeed(DailyPhotoSource.BING, "us", "[]", listOf("https://img.peapix.com/keep.jpg"), emptyList())

        // Then the other is gone
        assertNull(cache.readThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/old.jpg"))
        assertEquals(2, cache.readThumbnail(DailyPhotoSource.BING, "https://img.peapix.com/keep.jpg")!![0].toInt())
    }

    private fun photoNamed(name: String) = parseBingFeed(feedOf(name))!!.single()

    private val photo get() = photoNamed("a")

    @Test
    fun `a photo opened full size is downloaded once and kept while it is listed`() = runTest {
        // Given a download that works
        api.downloadBytes = byteArrayOf(1, 2, 3)
        val repository = repository()

        // When the same photo is opened three times in one day
        val first = repository.originalBytes(photo)
        clock.now = Instant.parse("2026-11-12T23:30:00Z")
        repository.originalBytes(photo)
        val third = repository.originalBytes(photo)

        // Then it was downloaded once and every open has the same bytes
        assertEquals(1, api.downloads.size)
        assertEquals(listOf<Byte>(1, 2, 3), first!!.toList())
        assertEquals(listOf<Byte>(1, 2, 3), third!!.toList())
    }

    @Test
    fun `a new day keeps the full size photos its feed still lists and drops the others`() = runTest {
        // Given two photos listed and opened yesterday
        api.feed = feedOf("a", "b")
        api.downloadBytes = byteArrayOf(1)
        val repository = repository()
        repository.photos(DailyPhotoSource.BING, "us")
        repository.originalBytes(photoNamed("a"))
        repository.originalBytes(photoNamed("b"))
        assertEquals(2, api.downloads.size)

        // When the next day's feed lists b and a new c
        clock.now = Instant.parse("2026-11-13T08:00:00Z")
        api.feed = feedOf("b", "c")
        repository.photos(DailyPhotoSource.BING, "us")
        repository.originalBytes(photoNamed("b"))
        repository.originalBytes(photoNamed("a"))

        // Then b came from the cache and a, no longer listed, was downloaded again
        assertEquals(3, api.downloads.size)
        assertTrue(api.downloads.last().endsWith("OHR.a_UHD.jpg"))
    }

    @Test
    fun `a failed download is not kept`() = runTest {
        // Given a download that fails and then works
        val repository = repository()
        api.downloadBytes = null
        assertNull(repository.originalBytes(photo))

        // When it works the next time
        api.downloadBytes = byteArrayOf(9)

        // Then it is fetched, not served from a copy of the failure
        assertEquals(listOf<Byte>(9), repository.originalBytes(photo)!!.toList())
        assertEquals(2, api.downloads.size)
    }

    @Test
    fun `storing one source's feed leaves the other's full size photos alone`() {
        // Given a Bing and a Spotlight photo kept
        val cache = DailyPhotoCache(tempFolder.newFolder("cache2"), clock)
        val bing = "https://www.bing.com/th?id=OHR.a_UHD.jpg"
        val spotlight = "https://res.public.onecdn.static.microsoft/creativeservice/s.jpg"
        cache.writeOriginal(bing, byteArrayOf(1))
        cache.writeOriginal(spotlight, byteArrayOf(2))
        cache.writeThumbnail(DailyPhotoSource.BING, "https://www.bing.com/t.jpg", byteArrayOf(3))

        // When a Bing feed that lists nothing is stored
        cache.writeFeed(DailyPhotoSource.BING, "us", "{}", emptyList(), emptyList())

        // Then Bing's thumbnail and photo go and Spotlight's photo stays
        assertNull(cache.readThumbnail(DailyPhotoSource.BING, "https://www.bing.com/t.jpg"))
        assertNull(cache.readOriginal(bing))
        assertEquals(2, cache.readOriginal(spotlight)!![0].toInt())
    }
}
