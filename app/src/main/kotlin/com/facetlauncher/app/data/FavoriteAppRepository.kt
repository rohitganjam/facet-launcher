package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FavoriteAppDao
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [FavoriteAppDao], hydrating stored (packageName, activityName) rows against the live
 * installed-app list — mirrors [DockAppRepository]'s uninstall-collapse pattern: an entry
 * whose app is no longer installed is filtered out of the emitted list rather than shown as
 * a dead tile (F2's uninstall-collapse rule, applied here per-facet), and now genuinely live
 * (via [AppRepository.observeInstalledApps]) rather than only re-evaluated the next time this
 * flow happens to be freshly subscribed — an uninstall via F12's long-press menu collapses
 * Favorites immediately instead of needing a restart.
 */
@Singleton
class FavoriteAppRepository @Inject constructor(
    private val favoriteAppDao: FavoriteAppDao,
    private val appRepository: AppRepository,
) {

    companion object {
        const val MAX_FAVORITES = AppListLimits.MAX_FAVORITES
    }

    fun observeFavoritesForFacet(facetId: Long): Flow<List<AppInfo>> {
        return combine(favoriteAppDao.observeForFacet(facetId), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }
    }

    /**
     * Every facet's favorites at once, keyed by facet id — for the facet carousel's
     * preview cards, which need to show *every* facet's favorites while browsing, not just
     * the active one's. Subscribes to the live installed-app list exactly once and reuses it
     * across all [facetIds], rather than each facet independently re-querying it (as calling
     * [observeFavoritesForFacet] once per facet would do) — with several facets browsed
     * simultaneously that redundant fan-out of real `LauncherApps` queries is wasteful and, in
     * practice, was slow enough to make callers relying on a single-frame result flaky.
     */
    fun observeFavoritesForFacets(facetIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val entitiesPerFacet = combine(facetIds.map { favoriteAppDao.observeForFacet(it) }) { it }
        return combine(entitiesPerFacet, appRepository.observeInstalledApps()) { perFacet, installed ->
            facetIds.indices.associate { index -> facetIds[index] to hydrate(perFacet[index], installed) }
        }
    }

    private fun hydrate(entities: List<FavoriteAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { it.packageName to it.activityName }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
    }

    suspend fun addFavorite(facetId: Long, app: AppInfo, position: Int) {
        favoriteAppDao.upsert(
            FavoriteAppEntity(
                facetId = facetId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
            ),
        )
    }

    suspend fun removeFavorite(facetId: Long, app: AppInfo) {
        favoriteAppDao.deleteByComponent(facetId, app.packageName, app.activityName)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s favorite entry from *every*
     * facet, driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase]. Distinct
     * from [observeFavoritesForFacet]'s own runtime filtering, which reacts to any reason an
     * app might be momentarily missing without deleting anything — this only runs for a
     * genuine, permanent uninstall.
     */
    suspend fun removeByPackage(packageName: String) {
        favoriteAppDao.deleteByPackage(packageName)
    }

    /** Replaces this facet's entire favorites list with [apps] — used to seed a clean copy (e.g. of the current default list) when a facet switches to Override. */
    suspend fun replaceFavorites(facetId: Long, apps: List<AppInfo>) {
        favoriteAppDao.deleteAllForFacet(facetId)
        apps.forEachIndexed { index, app ->
            favoriteAppDao.upsert(
                FavoriteAppEntity(facetId = facetId, packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up, unlike every other read here). */
    suspend fun getRawFavoritesForFacet(facetId: Long): List<FavoriteAppEntity> =
        favoriteAppDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreFavorite(entity: FavoriteAppEntity) {
        favoriteAppDao.upsert(entity.copy(id = 0))
    }

    suspend fun reorderFavorites(facetId: Long, orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            favoriteAppDao.upsert(
                FavoriteAppEntity(
                    facetId = facetId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    position = index,
                ),
            )
        }
    }
}
