package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FakeEntitlementRepository
import com.facetlauncher.app.data.dailyphotos.DailyPhotoFeed
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.data.model.DailyWallpaperPhoto
import com.facetlauncher.app.data.model.DailyWallpaperProblem
import com.facetlauncher.app.data.model.NetworkConditions
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RunDailyWallpaperUseCaseTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val today = LocalDate.of(2026, 11, 13)
    private lateinit var daily: FakeDailyWallpaperRepository
    private val copies = FakeDailyCopies()
    private lateinit var photos: FakeDailyPhotos
    private val conditions = FakeConditions()
    private val entitlement = FakeEntitlementRepository()

    private fun useCase(): RunDailyWallpaperUseCase {
        daily = if (::daily.isInitialized) daily else FakeDailyWallpaperRepository(tempFolder.root)
        photos = if (::photos.isInitialized) photos else FakeDailyPhotos(tempFolder.newFolder(), copies)
        return RunDailyWallpaperUseCase(daily, photos, copies, conditions, dailyTestClock, entitlement)
    }

    /** Bing's week with [names] newest first, each downloadable as a 4000 x 2000 landscape photo. */
    private fun givenBing(vararg names: String) {
        useCase()
        val week = names.mapIndexed { index, name -> dailyPhoto(name, date = today.minusDays(index.toLong()).toString()) }
        photos.feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(week.reversed(), savedEarlier = false)
        givenOriginals(*names)
    }

    private fun givenOriginals(vararg names: String, size: Pair<Int, Int> = 4000 to 2000) {
        names.forEachIndexed { index, name ->
            val marker = (index + 1).toByte()
            photos.originals["https://img.peapix.com/$name.jpg"] = byteArrayOf(marker)
            copies.sizes[marker] = size
        }
    }

    private suspend fun turnOn(source: DailyPhotoSource = DailyPhotoSource.BING) = daily.update { it.copy(enabled = true, source = source) }

    @Test
    fun `nothing runs while the daily wallpaper is off`() = runTest {
        // Given photos to fetch, and the daily wallpaper off
        givenBing("a")

        // When a run starts
        val outcome = useCase()()

        // Then nothing was fetched
        assertEquals(DailyRunOutcome.OFF, outcome)
        assertEquals(0, photos.feedRequests)
    }

    @Test
    fun `nothing runs without pro and the settings are kept`() = runTest {
        // Given the daily wallpaper on, and a free user
        givenBing("a")
        turnOn()
        entitlement.proFlow.value = false

        // When a run starts
        val outcome = useCase()()

        // Then nothing was fetched, no attempt counted, and it is still on for when Pro comes back
        assertEquals(DailyRunOutcome.OFF, outcome)
        assertEquals(0, photos.feedRequests)
        assertEquals(0, daily.get().attempts)
        assertTrue(daily.get().enabled)
    }

    @Test
    fun `the newest bing photo is prepared and today is marked done`() = runTest {
        // Given Bing's week, newest first: today, yesterday, the day before
        givenBing("today", "yesterday", "older")
        turnOn()

        // When a run starts on Wi-Fi
        val outcome = useCase()()

        // Then today's photo is pending, today is checked and one attempt counted
        assertEquals(DailyRunOutcome.READY, outcome)
        val state = daily.get()
        assertEquals("https://img.peapix.com/today.jpg", state.pending?.imageUrl)
        assertEquals(today, state.checkedDay)
        assertEquals(1, state.attemptsOn(today))
        assertNotNull(daily.pendingBitmap)
        assertEquals(listOf("https://img.peapix.com/today.jpg"), photos.downloads)
    }

    @Test
    fun `only the centre strip in the screen's shape is decoded, at a reduced size`() = runTest {
        // Given a 4000 x 2000 photo and a 100 x 200 screen
        givenBing("a")
        turnOn()

        // When it is prepared
        useCase()()

        // Then the centre 1000 x 2000 strip was decoded, sampled down while still covering the screen
        val (region, sample) = copies.decodedRegions.single()
        assertEquals(1500, region.left)
        assertEquals(2500, region.right)
        assertEquals(2000, region.height)
        assertEquals(8, sample)
    }

    @Test
    fun `a day already done does not touch the network`() = runTest {
        // Given today was already checked
        givenBing("a")
        turnOn()
        daily.update { it.copy(checkedDay = today) }

        // When the launcher resumes again
        val outcome = useCase()()

        // Then nothing was fetched
        assertEquals(DailyRunOutcome.DONE_TODAY, outcome)
        assertEquals(0, photos.feedRequests)
    }

    @Test
    fun `with wi-fi only on, a metered network waits and fetches nothing`() = runTest {
        // Given mobile data
        givenBing("a")
        turnOn()
        conditions.network = NetworkConditions(connected = true, unmetered = false, roaming = false)

        // When a run starts
        val outcome = useCase()()

        // Then it waits for Wi-Fi without using an attempt
        assertEquals(DailyRunOutcome.WAITING, outcome)
        assertEquals(DailyWallpaperProblem.WAITING_FOR_WIFI, daily.get().problem)
        assertEquals(0, photos.feedRequests)
        assertEquals(0, daily.get().attemptsOn(today))
    }

    @Test
    fun `the user can allow mobile data for one run`() = runTest {
        // Given mobile data with Wi-Fi only on
        givenBing("a")
        turnOn()
        conditions.network = NetworkConditions(connected = true, unmetered = false, roaming = false)

        // When a run is told to use mobile data
        val outcome = useCase()(useMobileData = true)

        // Then the photo is prepared
        assertEquals(DailyRunOutcome.READY, outcome)
    }

    @Test
    fun `with wi-fi only off, roaming still waits`() = runTest {
        // Given roaming mobile data and Wi-Fi only off
        givenBing("a")
        daily = FakeDailyWallpaperRepository(tempFolder.root)
        daily.update { it.copy(enabled = true, wifiOnly = false) }
        conditions.network = NetworkConditions(connected = true, unmetered = false, roaming = true)

        // When a run starts
        val outcome = useCase()()

        // Then nothing is downloaded
        assertEquals(DailyRunOutcome.WAITING, outcome)
        assertEquals(DailyWallpaperProblem.NO_CONNECTION, daily.get().problem)
    }

    @Test
    fun `battery saver waits`() = runTest {
        // Given Battery Saver on
        givenBing("a")
        turnOn()
        conditions.powerSave = true

        // When a run starts
        val outcome = useCase()()

        // Then it waits
        assertEquals(DailyRunOutcome.WAITING, outcome)
        assertEquals(DailyWallpaperProblem.BATTERY_SAVER, daily.get().problem)
    }

    @Test
    fun `when the newest bing photo is already set there is nothing new`() = runTest {
        // Given today's photo is the one set
        givenBing("today", "yesterday")
        turnOn()
        daily.update { it.copy(current = DailyWallpaperPhoto("https://img.peapix.com/today.jpg", "today", "")) }

        // When a run starts
        val outcome = useCase()()

        // Then nothing is downloaded, and today is done
        assertEquals(DailyRunOutcome.NOTHING_NEW, outcome)
        assertTrue(photos.downloads.isEmpty())
        assertEquals(today, daily.get().checkedDay)
    }

    @Test
    fun `spotlight skips the photos it set recently`() = runTest {
        // Given Spotlight's photos, the first two set before
        useCase()
        photos.feeds[DailyPhotoSource.SPOTLIGHT] = DailyPhotoFeed.Photos(listOf(dailyPhoto("s1"), dailyPhoto("s2"), dailyPhoto("s3")), savedEarlier = false)
        givenOriginals("s1", "s2", "s3")
        turnOn(DailyPhotoSource.SPOTLIGHT)
        daily.update { it.copy(history = listOf("https://img.peapix.com/s1.jpg", "https://img.peapix.com/s2.jpg")) }

        // When a run starts
        useCase()()

        // Then the third is prepared
        assertEquals("https://img.peapix.com/s3.jpg", daily.get().pending?.imageUrl)
    }

    @Test
    fun `a photo too small to look sharp is skipped for the next newest`() = runTest {
        // Given today's photo is so small that its strip would be enlarged almost twice to fill the 100 px wide screen, and yesterday's is large
        givenBing("today", "yesterday")
        copies.sizes[1.toByte()] = 192 to 108
        turnOn()

        // When a run starts
        val outcome = useCase()()

        // Then yesterday's is prepared, today's is not tried again, and the status says why
        assertEquals(DailyRunOutcome.READY, outcome)
        val state = daily.get()
        assertEquals("https://img.peapix.com/yesterday.jpg", state.pending?.imageUrl)
        assertTrue("https://img.peapix.com/today.jpg" in state.history)
        assertEquals(DailyWallpaperProblem.TOO_SMALL, state.problem)
    }

    @Test
    fun `photos kept from an earlier day count as a failed refresh`() = runTest {
        // Given the refresh failed and only an earlier day's feed is available
        useCase()
        photos.feeds[DailyPhotoSource.BING] = DailyPhotoFeed.Photos(listOf(dailyPhoto("old", "2026-11-12")), savedEarlier = true)
        givenOriginals("old")
        turnOn()

        // When a run starts
        val outcome = useCase()()

        // Then it failed, today is not marked done, and nothing was downloaded
        assertEquals(DailyRunOutcome.FAILED, outcome)
        assertNull(daily.get().checkedDay)
        assertEquals(DailyWallpaperProblem.SOURCE_UNAVAILABLE, daily.get().problem)
        assertTrue(photos.downloads.isEmpty())
    }

    @Test
    fun `a failed download counts as an attempt and three attempts end the day`() = runTest {
        // Given the original never downloads
        givenBing("a")
        photos.originals.clear()
        turnOn()

        // When the launcher resumes four times
        val outcomes = (1..4).map { useCase()() }

        // Then three attempts were made, and the fourth gave up without a request
        assertEquals(listOf(DailyRunOutcome.FAILED, DailyRunOutcome.FAILED, DailyRunOutcome.FAILED, DailyRunOutcome.GAVE_UP), outcomes)
        assertEquals(3, photos.feedRequests)
        assertEquals(DailyWallpaperProblem.DOWNLOAD_FAILED, daily.get().problem)
    }

    @Test
    fun `a wallpaper changed outside facet pauses the daily change`() = runTest {
        // Given the daily change last set wallpaper 10, and the system now has 42
        givenBing("a")
        turnOn()
        daily.update { it.copy(systemId = 10) }
        copies.systemId = 42

        // When a run starts
        val outcome = useCase()()

        // Then it pauses and fetches nothing
        assertEquals(DailyRunOutcome.PAUSED, outcome)
        assertTrue(daily.get().paused)
        assertEquals(0, photos.feedRequests)
    }

    @Test
    fun `a lock screen changed outside facet pauses when the lock screen is included`() = runTest {
        // Given Home and lock, with the lock screen changed since
        givenBing("a")
        daily = FakeDailyWallpaperRepository(tempFolder.root)
        daily.update { it.copy(enabled = true, includeLock = true, systemId = 10, lockId = 110) }
        copies.lockId = 7

        // When a run starts
        val outcome = useCase()()

        // Then it pauses
        assertEquals(DailyRunOutcome.PAUSED, outcome)
    }

    @Test
    fun `an interrupted change of its own is not taken for the user's`() = runTest {
        // Given a change was being applied when the process died, and its photo is gone
        givenBing("a")
        turnOn()
        daily.update { it.copy(systemId = 10, applying = true) }
        copies.systemId = 11

        // When a run starts
        val outcome = useCase()()

        // Then the new id is the baseline and the day goes on
        assertFalse(daily.get().paused)
        assertEquals(11, daily.get().systemId)
        assertFalse(daily.get().applying)
        assertEquals(DailyRunOutcome.READY, outcome)
    }
}
