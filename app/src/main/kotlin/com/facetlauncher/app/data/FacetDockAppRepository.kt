package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDockAppDao
import com.facetlauncher.app.data.local.FacetDockAppEntity
import com.facetlauncher.app.data.local.FacetDockFolderPlacementDao
import com.facetlauncher.app.data.local.FacetDockFolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [FacetDockAppDao] — the per-facet counterpart to [DockAppRepository] (the
 * launcher-wide default dock), mirroring [FavoriteAppRepository]'s shape exactly: stored
 * (packageName, activityName) rows hydrated live against [AppRepository.observeInstalledApps],
 * an entry whose app is no longer installed collapsed out rather than shown as a dead tile.
 * Consumed only while a facet's [com.facetlauncher.app.data.local.FacetEntity.overrideDock]
 * is set (see [com.facetlauncher.app.domain.ObserveHomeScreenStateUseCase]).
 *
 * A folder ([FacetDockFolderPlacementDao]) occupies one slot in the same `position` ordering
 * space as standalone dock apps, merged in Kotlin by [observeDockItems] — the per-facet
 * counterpart of [DockAppRepository.observeDockItems].
 */
@Singleton
class FacetDockAppRepository @Inject constructor(
    private val facetDockAppDao: FacetDockAppDao,
    private val facetDockFolderPlacementDao: FacetDockFolderPlacementDao,
    private val folderRepository: FolderRepository,
    private val appRepository: AppRepository,
) {

    fun observeDockAppsForFacet(facetId: Long): Flow<List<AppInfo>> {
        return combine(facetDockAppDao.observeForFacet(facetId), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }
    }

    /**
     * Every facet's dock at once, keyed by facet id — for the facet carousel's preview
     * cards, which need every facet's dock while browsing, not just the active one's. Subscribes
     * to the live installed-app list exactly once and reuses it across all [facetIds], mirroring
     * [FavoriteAppRepository.observeFavoritesForFacets].
     */
    fun observeDockAppsForFacets(facetIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val entitiesPerFacet = combine(facetIds.map { facetDockAppDao.observeForFacet(it) }) { it }
        return combine(entitiesPerFacet, appRepository.observeInstalledApps()) { perFacet, installed ->
            facetIds.indices.associate { index -> facetIds[index] to hydrate(perFacet[index], installed) }
        }
    }

    /** This facet's full ordered dock content, apps and folders interleaved. A 0-app folder placed here is still emitted. */
    fun observeDockItems(facetId: Long): Flow<List<PlacedItem>> {
        return combine(
            facetDockAppDao.observeForFacet(facetId),
            facetDockFolderPlacementDao.observeForFacet(facetId),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
            mergeItems(appEntities, placements, folders, installed)
        }
    }

    /** Batched counterpart of [observeDockItems], mirroring [observeDockAppsForFacets]. */
    fun observeDockItemsForFacets(facetIds: List<Long>): Flow<Map<Long, List<PlacedItem>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val appsPerFacet = combine(facetIds.map { facetDockAppDao.observeForFacet(it) }) { it }
        val placementsPerFacet = combine(facetIds.map { facetDockFolderPlacementDao.observeForFacet(it) }) { it }
        return combine(appsPerFacet, placementsPerFacet, folderRepository.observeFolders(), appRepository.observeInstalledApps()) { perFacetApps, perFacetPlacements, folders, installed ->
            facetIds.indices.associate { index ->
                facetIds[index] to mergeItems(perFacetApps[index], perFacetPlacements[index], folders, installed)
            }
        }
    }

    private fun mergeItems(
        appEntities: List<FacetDockAppEntity>,
        placements: List<FacetDockFolderPlacementEntity>,
        folders: List<com.facetlauncher.app.data.model.Folder>,
        installed: List<AppInfo>,
    ): List<PlacedItem> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
        val foldersById = folders.associateBy { it.id }

        val appItems = appEntities.mapNotNull { entity ->
            installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)]?.let { app ->
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

    private fun hydrate(entities: List<FacetDockAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)] }
    }

    suspend fun addDockApp(facetId: Long, app: AppInfo, position: Int) {
        facetDockAppDao.upsert(
            FacetDockAppEntity(
                facetId = facetId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
                profile = app.profile,
                userId = app.userHandle.hashCode(),
            ),
        )
    }

    suspend fun removeDockApp(facetId: Long, app: AppInfo) {
        facetDockAppDao.deleteByComponent(facetId, app.packageName, app.activityName, app.userHandle.hashCode())
    }

    suspend fun placeFolder(facetId: Long, folderId: Long, position: Int) {
        facetDockFolderPlacementDao.upsert(FacetDockFolderPlacementEntity(facetId = facetId, folderId = folderId, position = position))
    }

    suspend fun removeFolderPlacement(facetId: Long, folderId: Long) {
        facetDockFolderPlacementDao.deleteByFolderId(facetId, folderId)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s dock entry (belonging to [userId])
     * from *every* facet, driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase].
     * Distinct from [observeDockAppsForFacet]'s own runtime filtering, which only hides a
     * momentarily-missing app without deleting anything.
     */
    suspend fun removeByPackage(packageName: String, userId: Int) {
        facetDockAppDao.deleteByPackage(packageName, userId)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByUserId(userId: Int) {
        facetDockAppDao.deleteByUserId(userId)
    }

    /** Replaces this facet's entire dock with [items] — used to seed a clean copy (e.g. of the current default dock) when a facet switches to Override. Carries folder placements, not just apps. */
    suspend fun replaceItems(facetId: Long, items: List<PlacedItem>) {
        facetDockAppDao.deleteAllForFacet(facetId)
        facetDockFolderPlacementDao.deleteAllForFacet(facetId)
        items.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> facetDockAppDao.upsert(
                    FacetDockAppEntity(
                        facetId = facetId,
                        packageName = item.app.packageName,
                        activityName = item.app.activityName,
                        position = index,
                        profile = item.app.profile,
                        userId = item.app.userHandle.hashCode(),
                    ),
                )
                is PlacedItem.FolderItem -> facetDockFolderPlacementDao.upsert(
                    FacetDockFolderPlacementEntity(facetId = facetId, folderId = item.folder.id, position = index),
                )
            }
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDockAppsForFacet(facetId: Long): List<FacetDockAppEntity> =
        facetDockAppDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore export — raw folder-placement rows for this facet's dock. */
    suspend fun getRawDockFolderPlacementsForFacet(facetId: Long): List<FacetDockFolderPlacementEntity> =
        facetDockFolderPlacementDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDockApp(entity: FacetDockAppEntity) {
        facetDockAppDao.upsert(entity.copy(id = 0))
    }

    /** Rows still at the migration's `-1` `userId` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    suspend fun getOrphanedRows(): List<FacetDockAppEntity> = facetDockAppDao.getOrphaned()

    /** Backfills [entity]'s real `userId` once [RepairOrphanedProfileRowsUseCase] resolves it — updates in place (same `id`), doesn't create a new row. */
    suspend fun backfillUserId(entity: FacetDockAppEntity, userId: Int) {
        facetDockAppDao.upsert(entity.copy(userId = userId))
    }

    /** F14 Backup & Restore import — places a restored folder (by its already-remapped [folderId]) into this facet's dock. */
    suspend fun restoreDockFolderPlacement(facetId: Long, folderId: Long, position: Int) {
        facetDockFolderPlacementDao.upsert(FacetDockFolderPlacementEntity(facetId = facetId, folderId = folderId, position = position))
    }

    suspend fun reorderDockItems(facetId: Long, orderedItems: List<PlacedItem>) {
        orderedItems.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> facetDockAppDao.upsert(
                    FacetDockAppEntity(
                        facetId = facetId,
                        packageName = item.app.packageName,
                        activityName = item.app.activityName,
                        position = index,
                        profile = item.app.profile,
                        userId = item.app.userHandle.hashCode(),
                    ),
                )
                is PlacedItem.FolderItem -> facetDockFolderPlacementDao.upsert(
                    FacetDockFolderPlacementEntity(facetId = facetId, folderId = item.folder.id, position = index),
                )
            }
        }
    }
}
