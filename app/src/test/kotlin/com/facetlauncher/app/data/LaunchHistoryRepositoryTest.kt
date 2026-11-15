package com.facetlauncher.app.data

import android.os.UserHandle
import com.facetlauncher.app.data.local.AppLaunchCount
import com.facetlauncher.app.data.local.AppLaunchEntity
import com.facetlauncher.app.data.local.LaunchHistoryDao
import com.facetlauncher.app.data.local.SearchPickEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import com.facetlauncher.app.data.model.SearchSignals
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`

class LaunchHistoryRepositoryTest {

    private val now: Instant = Instant.parse("2026-11-15T10:00:00Z")
    private val today = LocalDate.of(2026, 11, 15).toEpochDay()
    private val windowStart = today - LaunchHistoryRepository.MOST_USED_WINDOW_DAYS + 1

    private val user: UserHandle = mock(UserHandle::class.java)
    private val userId = user.hashCode()

    private val dao: LaunchHistoryDao = mock(LaunchHistoryDao::class.java)
    private val appRepository: AppRepository = mock(AppRepository::class.java)

    private fun app(name: String, profile: AppProfile = AppProfile.PERSONAL, packageName: String = "com.example.$name") =
        AppInfo(packageName = packageName, activityName = ".Main", label = name, icon = null, profile = profile, userHandle = user)

    private val chat = app("chat")
    private val maps = app("maps")
    private val notes = app("notes")

    private fun launch(app: AppInfo, at: Long) = AppLaunchEntity(app.packageName, app.activityName, userId, at)

    private fun count(app: AppInfo, total: Int) = AppLaunchCount(app.packageName, app.activityName, userId, total)

    private fun kotlinx.coroutines.test.TestScope.repository() =
        LaunchHistoryRepository(dao, appRepository, Clock.fixed(now, ZoneOffset.UTC), StandardTestDispatcher(testScheduler))

    @Test
    fun `a launch is recorded for today, keyed by the app's real userId, keeping only the most-used window`() = runTest {
        // Given a personal app
        // When it is opened from Facet
        repository().recordLaunch(chat)

        // Then its last launch and today's count are written, and older days pruned
        verify(dao).recordLaunch(chat.packageName, chat.activityName, userId, now.toEpochMilli(), today, windowStart)
    }

    @Test
    fun `Private Space apps and Facet itself are never recorded`() = runTest {
        // Given a Private Space app and Facet's own entry
        val repository = repository()

        // When each is opened and picked from search
        repository.recordLaunch(app("secret", profile = AppProfile.PRIVATE))
        repository.recordLaunch(app("facet", packageName = FACET_LAUNCHER_PACKAGE_NAME))
        repository.recordSearchPick("sec", app("secret", profile = AppProfile.PRIVATE))

        // Then nothing is written
        verifyNoInteractions(dao)
    }

    @Test
    fun `a search pick is recorded under its query and trimmed to the cap, a blank query is ignored`() = runTest {
        val repository = repository()

        // When a pick is made for "ch" and another for a blank query
        repository.recordSearchPick("ch", chat)
        repository.recordSearchPick(" ", chat)

        // Then only the "ch" pick is written
        verify(dao).recordPick("ch", chat.packageName, chat.activityName, userId, now.toEpochMilli(), LaunchHistoryRepository.MAX_SEARCH_PICKS)
        verifyNoMoreInteractions(dao)
    }

    @Test
    fun `recents are installed apps in last-opened order, skipping uninstalled ones, up to the limit`() = runTest {
        // Given launches newest first: an uninstalled app, maps, chat, notes
        val gone = app("gone")
        `when`(dao.lastLaunches()).thenReturn(listOf(launch(gone, 40), launch(maps, 30), launch(chat, 20), launch(notes, 10)))
        `when`(appRepository.getInstalledApps()).thenReturn(listOf(chat, maps, notes))

        // When the two most recent are asked for
        val recents = repository().getRecentApps(limit = 2)

        // Then the uninstalled app is skipped
        assertEquals(listOf(maps, chat), recents)
    }

    @Test
    fun `most used ranks by launches in the window, ties going to the most recently opened`() = runTest {
        // Given chat and notes tied on 3 launches (notes opened later) and maps on 5
        `when`(dao.launchCountsSince(windowStart)).thenReturn(listOf(count(chat, 3), count(notes, 3), count(maps, 5)))
        `when`(dao.lastLaunches()).thenReturn(listOf(launch(notes, 30), launch(chat, 20), launch(maps, 10)))
        `when`(appRepository.getInstalledApps()).thenReturn(listOf(chat, maps, notes))

        // When most used is asked for
        val mostUsed = repository().getMostUsedApps(limit = 10)

        // Then maps leads, and notes beats chat on recency
        assertEquals(listOf(maps, notes, chat), mostUsed)
    }

    @Test
    fun `last-used timestamps take each package's latest launch across its activities`() = runTest {
        // Given chat opened through two activities
        `when`(dao.lastLaunches()).thenReturn(
            listOf(launch(chat, 50), AppLaunchEntity(chat.packageName, ".Compose", userId, 70), launch(maps, 10)),
        )

        // Then the newest launch counts per package
        assertEquals(mapOf(chat.packageName to 70L, maps.packageName to 10L), repository().getLastUsedTimestamps())
    }

    @Test
    fun `search signals group picks by query and launches by app`() = runTest {
        // Given chat picked twice for "c" and launched 4 times this month
        `when`(dao.observeSearchPicks()).thenReturn(
            flowOf(listOf(SearchPickEntity("c", chat.packageName, chat.activityName, userId, count = 2, lastPickedAt = 1))),
        )
        `when`(dao.observeLaunchCountsSince(windowStart)).thenReturn(flowOf(listOf(count(chat, 4))))

        // When the signals are read
        val signals = repository().observeSearchSignals().first()

        // Then they're keyed the way search looks them up
        assertEquals(SearchSignals(picks = mapOf("c" to mapOf(chat.listKey to 2)), launchCounts = mapOf(chat.listKey to 4)), signals)
    }

    @Test
    fun `uninstall cleanup is passed through for the package's userId`() = runTest {
        val repository = repository()

        repository.removeByPackage("com.example.chat", 10)
        repository.removeByUserId(10)

        verify(dao).removeByPackage("com.example.chat", 10)
        verify(dao).removeByUserId(10)
    }
}
