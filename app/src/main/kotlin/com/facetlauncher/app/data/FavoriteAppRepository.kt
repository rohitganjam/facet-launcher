package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FavoriteAppDao
import com.facetlauncher.app.data.local.FavoriteAppEntity
import com.facetlauncher.app.data.local.FavoriteFolderPlacementDao
import com.facetlauncher.app.data.local.FavoriteFolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
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
 *
 * A folder ([FavoriteFolderPlacementDao]) occupies one slot in the same `position` ordering
 * space as standalone favorite apps, merged in Kotlin by [observeFavoriteItems].
 */
@Singleton
class FavoriteAppRepository @Inject constructor(
    private val favoriteAppDao: FavoriteAppDao,
    private val favoriteFolderPlacementDao: FavoriteFolderPlacementDao,
    private val folderRepository: FolderRepository,
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

    /** This facet's full ordered favorites content, apps and folders interleaved. A 0-app folder placed here is still emitted. */
    fun observeFavoriteItems(facetId: Long): Flow<List<PlacedItem>> {
        return combine(
            favoriteAppDao.observeForFacet(facetId),
            favoriteFolderPlacementDao.observeForFacet(facetId),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
            mergeItems(appEntities, placements, folders, installed)
        }
    }

    /** Batched counterpart of [observeFavoriteItems], mirroring [observeFavoritesForFacets]. */
    fun observeFavoriteItemsForFacets(facetIds: List<Long>): Flow<Map<Long, List<PlacedItem>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val appsPerFacet = combine(facetIds.map { favoriteAppDao.observeForFacet(it) }) { it }
        val placementsPerFacet = combine(facetIds.map { favoriteFolderPlacementDao.observeForFacet(it) }) { it }
        return combine(appsPerFacet, placementsPerFacet, folderRepository.observeFolders(), appRepository.observeInstalledApps()) { perFacetApps, perFacetPlacements, folders, installed ->
            facetIds.indices.associate { index ->
                facetIds[index] to mergeItems(perFacetApps[index], perFacetPlacements[index], folders, installed)
            }
        }
    }

    private fun mergeItems(
        appEntities: List<FavoriteAppEntity>,
        placements: List<FavoriteFolderPlacementEntity>,
        folders: List<com.facetlauncher.app.data.model.Folder>,
        installed: List<AppInfo>,
    ): List<PlacedItem> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.profile) }
        val foldersById = folders.associateBy { it.id }

        val appItems = appEntities.mapNotNull { entity ->
            installedByComponent[Triple(entity.packageName, entity.activityName, entity.profile)]?.let { app ->
                entity.position to PlacedItem.SingleApp(app)
            }
        }
        val folderItems = placements.mapNotNull { placement ->
            foldersById[placement.folderId]?.let { folder ->
                placement.position to PlacedItem.FolderItem(folder)
            }
        }
        return (appItems + folderItems).sortedBy { it.first }.map { it.second }
    }

    private fun hydrate(entities: List<FavoriteAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.profile) }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.profile)] }
    }

    suspend fun addFavorite(facetId: Long, app: AppInfo, position: Int) {
        favoriteAppDao.upsert(
            FavoriteAppEntity(
                facetId = facetId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
                profile = app.profile,
            ),
        )
    }

    suspend fun removeFavorite(facetId: Long, app: AppInfo) {
        favoriteAppDao.deleteByComponent(facetId, app.packageName, app.activityName, app.profile)
    }

    suspend fun placeFolder(facetId: Long, folderId: Long, position: Int) {
        favoriteFolderPlacementDao.upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderId, position = position))
    }

    suspend fun removeFolderPlacement(facetId: Long, folderId: Long) {
        favoriteFolderPlacementDao.deleteByFolderId(facetId, folderId)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s favorite entry (in [profile]) from
     * *every* facet, driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase].
     * Distinct from [observeFavoritesForFacet]'s own runtime filtering, which reacts to any reason
     * an app might be momentarily missing without deleting anything — this only runs for a
     * genuine, permanent uninstall.
     */
    suspend fun removeByPackage(packageName: String, profile: AppProfile) {
        favoriteAppDao.deleteByPackage(packageName, profile)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByProfile(profile: AppProfile) {
        favoriteAppDao.deleteByProfile(profile)
    }

    /** Replaces this facet's entire favorites list with [items] — used to seed a clean copy (e.g. of the current default list) when a facet switches to Override. Carries folder placements, not just apps. */
    suspend fun replaceItems(facetId: Long, items: List<PlacedItem>) {
        favoriteAppDao.deleteAllForFacet(facetId)
        favoriteFolderPlacementDao.deleteAllForFacet(facetId)
        items.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> favoriteAppDao.upsert(
                    FavoriteAppEntity(facetId = facetId, packageName = item.app.packageName, activityName = item.app.activityName, position = index, profile = item.app.profile),
                )
                is PlacedItem.FolderItem -> favoriteFolderPlacementDao.upsert(
                    FavoriteFolderPlacementEntity(facetId = facetId, folderId = item.folder.id, position = index),
                )
            }
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up, unlike every other read here). */
    suspend fun getRawFavoritesForFacet(facetId: Long): List<FavoriteAppEntity> =
        favoriteAppDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore export — raw folder-placement rows for this facet's favorites. */
    suspend fun getRawFavoriteFolderPlacementsForFacet(facetId: Long): List<FavoriteFolderPlacementEntity> =
        favoriteFolderPlacementDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreFavorite(entity: FavoriteAppEntity) {
        favoriteAppDao.upsert(entity.copy(id = 0))
    }

    /** F14 Backup & Restore import — places a restored folder (by its already-remapped [folderId]) into this facet's favorites. */
    suspend fun restoreFavoriteFolderPlacement(facetId: Long, folderId: Long, position: Int) {
        favoriteFolderPlacementDao.upsert(FavoriteFolderPlacementEntity(facetId = facetId, folderId = folderId, position = position))
    }

    suspend fun reorderFavoriteItems(facetId: Long, orderedItems: List<PlacedItem>) {
        orderedItems.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> favoriteAppDao.upsert(
                    FavoriteAppEntity(facetId = facetId, packageName = item.app.packageName, activityName = item.app.activityName, position = index, profile = item.app.profile),
                )
                is PlacedItem.FolderItem -> favoriteFolderPlacementDao.upsert(
                    FavoriteFolderPlacementEntity(facetId = facetId, folderId = item.folder.id, position = index),
                )
            }
        }
    }
}
