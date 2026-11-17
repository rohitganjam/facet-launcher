package com.facetlauncher.app.data

import com.facetlauncher.app.data.di.DefaultDispatcher
import com.facetlauncher.app.data.local.AppLaunchCount
import com.facetlauncher.app.data.local.LaunchHistoryDao
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.FACET_LAUNCHER_PACKAGE_NAME
import com.facetlauncher.app.data.model.SearchSignals
import com.facetlauncher.app.data.model.vaultKey
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Facet's own record of which apps are opened from it, the source of Recents, Most used, the pickers' Last used sort and search
 * ranking. It never leaves the device and needs no permission. Private Space launches are not recorded.
 */
@Singleton
class LaunchHistoryRepository @Inject constructor(
    private val dao: LaunchHistoryDao,
    private val appRepository: AppRepository,
    private val clock: Clock,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    // Defaulted so tests that don't cover the vault needn't build one; Hilt always supplies it.
    private val appVaultRepository: AppVaultRepository? = null,
) {
    suspend fun recordLaunch(app: AppInfo) {
        if (!isRecorded(app) || isVaulted(app)) return
        val today = today()
        dao.recordLaunch(
            packageName = app.packageName,
            activityName = app.activityName,
            userId = app.userHandle.hashCode(),
            at = clock.millis(),
            epochDay = today,
            keepDaysFrom = today - MOST_USED_WINDOW_DAYS + 1,
        )
    }

    /** [normalizedQuery] is the query as search matches it, so "Maps " and "maps" count as one. */
    suspend fun recordSearchPick(normalizedQuery: String, app: AppInfo) {
        if (normalizedQuery.isBlank() || !isRecorded(app) || isVaulted(app)) return
        dao.recordPick(normalizedQuery, app.packageName, app.activityName, app.userHandle.hashCode(), clock.millis(), MAX_SEARCH_PICKS)
    }

    suspend fun getRecentApps(limit: Int): List<AppInfo> = withContext(defaultDispatcher) {
        val installed = installedByKey()
        dao.lastLaunches().mapNotNull { installed[key(it.packageName, it.activityName, it.userId)] }.take(limit)
    }

    /** Most launches in the last [MOST_USED_WINDOW_DAYS] days first; ties go to the most recently opened. */
    suspend fun getMostUsedApps(limit: Int): List<AppInfo> = withContext(defaultDispatcher) {
        val installed = installedByKey()
        val lastLaunched = dao.lastLaunches().associate { key(it.packageName, it.activityName, it.userId) to it.lastLaunchedAt }
        dao.launchCountsSince(windowStart())
            .map { it.key() to it.total }
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenByDescending { lastLaunched[it.first] ?: 0L })
            .mapNotNull { (key, _) -> installed[key] }
            .take(limit)
    }

    /** When each package was last opened from Facet, across its activities and profiles; a package never opened is absent. */
    suspend fun getLastUsedTimestamps(): Map<String, Long> =
        dao.lastLaunches().groupBy { it.packageName }.mapValues { (_, launches) -> launches.maxOf { it.lastLaunchedAt } }

    fun observeSearchSignals(): Flow<SearchSignals> = combine(dao.observeSearchPicks(), dao.observeLaunchCountsSince(windowStart())) { picks, counts ->
        SearchSignals(
            picks = picks.groupBy { it.query }.mapValues { (_, rows) -> rows.associate { key(it.packageName, it.activityName, it.userId) to it.count } },
            launchCounts = counts.associate { it.key() to it.total },
        )
    }

    suspend fun removeByPackage(packageName: String, userId: Int) = dao.removeByPackage(packageName, userId)

    suspend fun removeByUserId(userId: Int) = dao.removeByUserId(userId)

    /** Opening an app from the App Vault leaves no trace in Recents, Most used or search ranking. */
    private suspend fun isVaulted(app: AppInfo) = appVaultRepository?.vaultedKeys?.first()?.contains(app.vaultKey) == true

    private fun isRecorded(app: AppInfo) = app.profile != AppProfile.PRIVATE && app.packageName != FACET_LAUNCHER_PACKAGE_NAME

    private suspend fun installedByKey(): Map<String, AppInfo> =
        appRepository.getInstalledApps().filter { isRecorded(it) }.associateBy { it.listKey }

    private fun today(): Long = LocalDate.now(clock).toEpochDay()

    private fun windowStart(): Long = today() - MOST_USED_WINDOW_DAYS + 1

    private fun AppLaunchCount.key() = key(packageName, activityName, userId)

    private fun key(packageName: String, activityName: String, userId: Int) = "$packageName/$activityName/$userId"

    companion object {
        /** Most used counts launches over this many days, today included. */
        const val MOST_USED_WINDOW_DAYS = 30L
        const val MAX_SEARCH_PICKS = 500
    }
}
