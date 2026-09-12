package com.facetlauncher.app.data

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowUsageStatsManager

/**
 * [UsageStats][android.app.usage.UsageStats] has no public constructor, and mocking it directly
 * doesn't reliably intercept under Robolectric's own class instrumentation — so these tests drive
 * the real [UsageStatsManager] service via [ShadowUsageStatsManager], the same shadow-based
 * approach [FacetDaoTest][com.facetlauncher.app.data.local.FacetDaoTest] uses for Room.
 */
@RunWith(RobolectricTestRunner::class)
class UsageStatsRepositoryTest {

    private val usageStatsManager =
        ApplicationProvider.getApplicationContext<Context>().getSystemService(UsageStatsManager::class.java)
    private val shadowUsageStatsManager = shadowOf(usageStatsManager)

    private fun appInfo(packageName: String, label: String) =
        AppInfo(packageName = packageName, activityName = ".Main", label = label, icon = null)

    /**
     * [lastTimeUsed] is added to "now" rather than used as an absolute epoch millis value — the
     * repository queries a rolling 7-day window ending now, and the shadow's own [queryUsageStats]
     * filters by whether `[firstTimeStamp, lastTimeStamp]` overlaps that window, so an absolute
     * value like `1_000L` (1970) would silently fall outside it and never come back.
     */
    private fun addUsageStats(packageName: String, lastTimeUsed: Long, totalTimeInForeground: Long) {
        val timestamp = System.currentTimeMillis() - (10_000L - lastTimeUsed)
        val stats = ShadowUsageStatsManager.UsageStatsBuilder.newBuilder()
            .setPackageName(packageName)
            .setFirstTimeStamp(timestamp)
            .setLastTimeStamp(timestamp)
            .setLastTimeUsed(timestamp)
            .setTotalTimeInForeground(totalTimeInForeground)
            .build()
        shadowUsageStatsManager.addUsageStats(UsageStatsManager.INTERVAL_BEST, stats)
    }

    @Test
    fun `getRecentApps ranks by last-used time, most recent first`() = runTest {
        // Given two apps used at different times
        addUsageStats("com.example.old", lastTimeUsed = 1_000L, totalTimeInForeground = 0L)
        addUsageStats("com.example.new", lastTimeUsed = 2_000L, totalTimeInForeground = 0L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(
            listOf(appInfo("com.example.old", "Old"), appInfo("com.example.new", "New")),
        )
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When fetching recent apps
        val result = repository.getRecentApps(limit = 5)

        // Then the most recently used app comes first
        assertEquals(listOf("New", "Old"), result.map { it.label })
    }

    @Test
    fun `getMostUsedApps ranks by total foreground time, aggregated across sub-intervals`() = runTest {
        // Given one package reported across two sub-intervals within the queried range
        addUsageStats("com.example.a", lastTimeUsed = 1_000L, totalTimeInForeground = 300_000L)
        addUsageStats("com.example.a", lastTimeUsed = 2_000L, totalTimeInForeground = 400_000L)
        addUsageStats("com.example.b", lastTimeUsed = 1_500L, totalTimeInForeground = 500_000L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(
            listOf(appInfo("com.example.a", "A"), appInfo("com.example.b", "B")),
        )
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When fetching most-used apps
        val result = repository.getMostUsedApps(limit = 5)

        // Then A's two sub-interval entries are summed (700_000) ahead of B's single 500_000
        assertEquals(listOf("A", "B"), result.map { it.label })
    }

    @Test
    fun `results are capped at the given limit`() = runTest {
        // Given three used apps but a limit of 2
        addUsageStats("com.example.a", lastTimeUsed = 1_000L, totalTimeInForeground = 0L)
        addUsageStats("com.example.b", lastTimeUsed = 2_000L, totalTimeInForeground = 0L)
        addUsageStats("com.example.c", lastTimeUsed = 3_000L, totalTimeInForeground = 0L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(
            listOf(appInfo("com.example.a", "A"), appInfo("com.example.b", "B"), appInfo("com.example.c", "C")),
        )
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When fetching recent apps with limit=2
        val result = repository.getRecentApps(limit = 2)

        // Then only the top 2 come back
        assertEquals(2, result.size)
        assertEquals(listOf("C", "B"), result.map { it.label })
    }

    @Test
    fun `uninstalled packages are excluded even when usage stats mention them`() = runTest {
        // Given a package reported by usage stats that's no longer installed
        addUsageStats("com.example.uninstalled", lastTimeUsed = 1_000L, totalTimeInForeground = 0L)
        addUsageStats("com.example.used", lastTimeUsed = 2_000L, totalTimeInForeground = 0L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(listOf(appInfo("com.example.used", "Used")))
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When fetching recent apps
        val result = repository.getRecentApps(limit = 5)

        // Then only the still-installed app is returned
        assertEquals(listOf("Used"), result.map { it.label })
    }

    @Test
    fun `Facet's own package is excluded even when it ranks highest, without shorting the requested limit`() = runTest {
        // Given Facet itself is (as it always is, being foregrounded on every Home visit) the
        // single most-used app, ranking ahead of two real apps
        addUsageStats(FACET_LAUNCHER_PACKAGE_NAME, lastTimeUsed = 3_000L, totalTimeInForeground = 999_000L)
        addUsageStats("com.example.a", lastTimeUsed = 1_000L, totalTimeInForeground = 100_000L)
        addUsageStats("com.example.b", lastTimeUsed = 2_000L, totalTimeInForeground = 200_000L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(
            listOf(
                appInfo(FACET_LAUNCHER_PACKAGE_NAME, "Facet Launcher"),
                appInfo("com.example.a", "A"),
                appInfo("com.example.b", "B"),
            ),
        )
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When asking for the top 2 most-used apps
        val result = repository.getMostUsedApps(limit = 2)

        // Then both real apps come through — Facet never displaces one of them out of the limit
        assertEquals(listOf("B", "A"), result.map { it.label })
    }

    @Test
    fun `apps with zero total foreground time are excluded from Most Used`() = runTest {
        // Given one app with real foreground time and one with none (e.g. a background-only ping)
        addUsageStats("com.example.unused", lastTimeUsed = 1_000L, totalTimeInForeground = 0L)
        addUsageStats("com.example.used", lastTimeUsed = 2_000L, totalTimeInForeground = 500_000L)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.getInstalledApps()).thenReturn(
            listOf(appInfo("com.example.unused", "Unused"), appInfo("com.example.used", "Used")),
        )
        val repository = UsageStatsRepository(usageStatsManager, appRepository)

        // When fetching most-used apps
        val result = repository.getMostUsedApps(limit = 5)

        // Then only the app with nonzero foreground time is returned
        assertEquals(listOf("Used"), result.map { it.label })
    }
}
