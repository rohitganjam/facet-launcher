package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.AppPlacementDao
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderPlacementDao
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

/**
 * Every Dock and Favorites list — the launcher-wide defaults and each facet's own — over
 * `app_placements` and `folder_placements`. A [PlacementTarget] names the list (`DOCK` or
 * `FAVORITES`) and its owner (a facet id, or null for the launcher-wide default).
 *
 * Reads hydrate stored `(packageName, activityName, userId)` rows against the live installed-app
 * list ([AppRepository.observeInstalledApps]): an entry whose app is no longer installed is filtered
 * out of the emitted list rather than shown as a dead tile, live, so an uninstall collapses the
 * list immediately. A folder placement occupies one slot in the same `position` space as the
 * apps, merged in Kotlin by [observeItems]; folder identity and membership live in
 * [FolderRepository], this repository only owns *whether* a folder occupies a slot.
 */
// One API over one table pair; it replaces four repositories that each had these same methods.
@Suppress("TooManyFunctions")
@Singleton
class PlacementRepository @Inject constructor(
    private val appPlacementDao: AppPlacementDao,
    private val folderPlacementDao: FolderPlacementDao,
    private val folderRepository: FolderRepository,
    private val appRepository: AppRepository,
) {

    /** The target's apps only, in position order. */
    fun observeApps(target: PlacementTarget): Flow<List<AppInfo>> =
        combine(appPlacementDao.observe(target.list, target.ownerKey), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }

    /**
     * [list] for every facet at once, keyed by facet id — for the facet carousel's preview cards.
     * Subscribes to the live installed-app list once and reuses it across all [facetIds], rather than
     * each facet re-querying it, which with several facets browsed at once was slow enough to make
     * callers relying on a single-frame result flaky.
     */
    fun observeAppsForFacets(list: PlacementList, facetIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val entitiesPerFacet = combine(facetIds.map { appPlacementDao.observe(list, it) }) { it }
        return combine(entitiesPerFacet, appRepository.observeInstalledApps()) { perFacet, installed ->
            facetIds.indices.associate { index -> facetIds[index] to hydrate(perFacet[index], installed) }
        }
    }

    /** The target's full ordered content, apps and folders interleaved. A 0-app folder placed here is still emitted. */
    fun observeItems(target: PlacementTarget): Flow<List<PlacedItem>> =
        combine(
            appPlacementDao.observe(target.list, target.ownerKey),
            folderPlacementDao.observe(target.list, target.ownerKey),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
            mergeItems(appEntities, placements, folders, installed)
        }

    /** Batched counterpart of [observeItems] for [list], mirroring [observeAppsForFacets]. */
    fun observeItemsForFacets(list: PlacementList, facetIds: List<Long>): Flow<Map<Long, List<PlacedItem>>> {
        if (facetIds.isEmpty()) return flowOf(emptyMap())
        val appsPerFacet = combine(facetIds.map { appPlacementDao.observe(list, it) }) { it }
        val placementsPerFacet = combine(facetIds.map { folderPlacementDao.observe(list, it) }) { it }
        return combine(appsPerFacet, placementsPerFacet, folderRepository.observeFolders(), appRepository.observeInstalledApps()) { perFacetApps, perFacetPlacements, folders, installed ->
            facetIds.indices.associate { index ->
                facetIds[index] to mergeItems(perFacetApps[index], perFacetPlacements[index], folders, installed)
            }
        }
    }

    suspend fun addApp(target: PlacementTarget, app: AppInfo, position: Int) {
        appPlacementDao.upsert(app.toEntity(target, position))
    }

    suspend fun removeApp(target: PlacementTarget, app: AppInfo) {
        appPlacementDao.deleteByComponent(target.list, target.ownerKey, app.packageName, app.activityName, app.userHandle.hashCode())
    }

    suspend fun placeFolder(target: PlacementTarget, folderId: Long, position: Int) {
        folderPlacementDao.upsert(FolderPlacementEntity(list = target.list, facetId = target.facetId, folderId = folderId, position = position))
    }

    suspend fun removeFolderPlacement(target: PlacementTarget, folderId: Long) {
        folderPlacementDao.deleteByFolderId(target.list, target.ownerKey, folderId)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s entries (belonging to [userId]) from
     * every list and facet, driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase].
     * Distinct from the reads' runtime filtering, which reacts to any reason an app might be
     * momentarily missing without deleting anything — this only runs for a genuine uninstall.
     */
    suspend fun removeByPackage(packageName: String, userId: Int) {
        appPlacementDao.deleteByPackage(packageName, userId)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByUserId(userId: Int) {
        appPlacementDao.deleteByUserId(userId)
    }

    /** Replaces the target's entire list with [items], carrying folder placements as well as apps — used to seed a clean copy when a facet switches to Override. */
    suspend fun replaceItems(target: PlacementTarget, items: List<PlacedItem>) {
        deleteAll(target)
        reorderItems(target, items)
    }

    /** Rewrites `position` across apps and folder placements to match [orderedItems]'s order. */
    suspend fun reorderItems(target: PlacementTarget, orderedItems: List<PlacedItem>) {
        orderedItems.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> appPlacementDao.upsert(item.app.toEntity(target, index))
                is PlacedItem.FolderItem -> placeFolder(target, item.folder.id, index)
            }
        }
    }

    /** Wipes the target's apps and folder placements — before a backup restore, or when seeding an override. */
    suspend fun deleteAll(target: PlacementTarget) {
        appPlacementDao.deleteAllForOwner(target.list, target.ownerKey)
        folderPlacementDao.deleteAllForOwner(target.list, target.ownerKey)
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawApps(target: PlacementTarget): List<AppPlacementEntity> =
        appPlacementDao.observe(target.list, target.ownerKey).first()

    /** F14 Backup & Restore export — raw folder-placement rows (the folder library itself is exported once, globally, by [FolderRepository]). */
    suspend fun getRawFolderPlacements(target: PlacementTarget): List<FolderPlacementEntity> =
        folderPlacementDao.observe(target.list, target.ownerKey).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreApp(entity: AppPlacementEntity) {
        appPlacementDao.upsert(entity.copy(id = 0))
    }

    /** F14 Backup & Restore import — places a restored folder (by its already-remapped [folderId]) into the target. */
    suspend fun restoreFolderPlacement(target: PlacementTarget, folderId: Long, position: Int) {
        placeFolder(target, folderId, position)
    }

    /** Rows still at the migration's `-1` `userId` sentinel, in every list — see `RepairOrphanedProfileRowsUseCase`. */
    suspend fun getOrphanedRows(): List<AppPlacementEntity> = appPlacementDao.getOrphaned()

    /** Backfills [entity]'s real `userId` once the repair resolves it — updates in place (same `id`), doesn't create a new row. */
    suspend fun backfillUserId(entity: AppPlacementEntity, userId: Int) {
        appPlacementDao.upsert(entity.copy(userId = userId))
    }

    private fun AppInfo.toEntity(target: PlacementTarget, position: Int) = AppPlacementEntity(
        list = target.list,
        facetId = target.facetId,
        packageName = packageName,
        activityName = activityName,
        position = position,
        profile = profile,
        userId = userHandle.hashCode(),
    )

    private fun mergeItems(
        appEntities: List<AppPlacementEntity>,
        placements: List<FolderPlacementEntity>,
        folders: List<Folder>,
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

    private fun hydrate(entities: List<AppPlacementEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)] }
    }
}
