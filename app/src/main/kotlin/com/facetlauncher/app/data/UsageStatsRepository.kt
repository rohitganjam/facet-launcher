package com.facetlauncher.app.data

import android.app.usage.UsageStatsManager
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * F2 Recents/Most Used — wraps [UsageStatsManager] (special-access `PACKAGE_USAGE_STATS`, gated
 * by [UsageAccessRepository]) and hydrates ranked package names against [AppRepository]'s
 * installed-app list for labels/icons, the same hydrate-against-installed pattern every other
 * app-list source in this app uses.
 */
@Singleton
class UsageStatsRepository @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val appRepository: AppRepository,
) {
    suspend fun getRecentApps(limit: Int): List<AppInfo> = rankedApps(limit) { lastUsed, _ -> lastUsed }

    suspend fun getMostUsedApps(limit: Int): List<AppInfo> = rankedApps(limit) { _, totalForeground -> totalForeground }

    /**
     * Last-used timestamp per installed package, looking back to epoch rather than
     * [LOOKBACK_MS] — for the app picker screens' "Last used" sort, which should reflect an
     * app's full usage history, not just the last week. A package absent from the result has no
     * recorded usage (including when the Usage Access permission isn't granted, in which case
     * [usageStatsManager] simply returns no data).
     */
    suspend fun getLastUsedTimestamps(): Map<String, Long> = withContext(Dispatchers.Default) {
        aggregateUsage(begin = 0L, end = System.currentTimeMillis()).mapValues { (_, usage) -> usage.first }
    }

    private suspend fun rankedApps(
        limit: Int,
        rankBy: (lastTimeUsed: Long, totalTimeInForeground: Long) -> Long,
    ): List<AppInfo> = withContext(Dispatchers.Default) {
        val end = System.currentTimeMillis()
        val begin = end - LOOKBACK_MS
        val aggregatedByPackage = aggregateUsage(begin, end)

        // Facet itself is foregrounded every time the user returns to Home, so it would
        // otherwise rank near the top of both Recents and Most Used — showing your own launcher
        // in its own app list is meaningless. Excluded here, before ranking/truncation, rather
        // than filtered out of an already-`take(limit)`'d list — that would risk silently
        // showing one fewer than `limit` real apps whenever Facet placed within the top `limit`.
        val installedByPackage = appRepository.getInstalledApps()
            .filterNot { it.packageName == FACET_LAUNCHER_PACKAGE_NAME }
            .associateBy { it.packageName }

        aggregatedByPackage.entries
            .filter { (packageName, usage) -> rankBy(usage.first, usage.second) > 0 && installedByPackage.containsKey(packageName) }
            .sortedByDescending { (_, usage) -> rankBy(usage.first, usage.second) }
            .take(limit)
            .mapNotNull { (packageName, _) -> installedByPackage[packageName] }
    }

    /** A package can report multiple `UsageStats` entries across sub-intervals within the queried
     * range — aggregates to one (last-used, total-foreground) pair per package. */
    private fun aggregateUsage(begin: Long, end: Long): Map<String, Pair<Long, Long>> {
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, begin, end).orEmpty()
        val aggregatedByPackage = mutableMapOf<String, Pair<Long, Long>>()
        for (entry in stats) {
            val (lastUsed, totalForeground) = aggregatedByPackage[entry.packageName] ?: (0L to 0L)
            aggregatedByPackage[entry.packageName] =
                maxOf(lastUsed, entry.lastTimeUsed) to (totalForeground + entry.totalTimeInForeground)
        }
        return aggregatedByPackage
    }

    private companion object {
        val LOOKBACK_MS = TimeUnit.DAYS.toMillis(7)
    }
}
