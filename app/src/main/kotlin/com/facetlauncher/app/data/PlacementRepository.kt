package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.AppPlacementDao
import com.facetlauncher.app.data.local.AppPlacementEntity
import com.facetlauncher.app.data.local.FolderPlacementDao
import com.facetlauncher.app.data.local.FolderPlacementEntity
import com.facetlauncher.app.data.local.cell
import com.facetlauncher.app.data.local.gridKey
import com.facetlauncher.app.data.local.keepingCellsOf
import com.facetlauncher.app.data.local.withCell
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.BackupGridCells
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.gridKey
import com.facetlauncher.app.domain.GridCell
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Every Dock and Favorites list — the launcher-wide defaults and each facet's own — over
 * `app_placements` and `folder_placements`. A [PlacementTarget] names the list (`DOCK` or
 * `FAVORITES`) and its owner (a facet id, or null for the launcher-wide default).
 *
 * Reads emit the stored rows only; [com.facetlauncher.app.domain.ObservePlacedItemsUseCase] joins
 * them against the live installed-app and folder lists (an uninstalled app drops out there). A
 * folder placement occupies one slot in the same `position` space as the apps; folder identity and
 * membership live in [FolderRepository], this repository only owns *whether* a folder occupies a slot.
 */
// One API over one table pair; it replaces four repositories that each had these same methods.
@Suppress("TooManyFunctions")
@Singleton
class PlacementRepository @Inject constructor(
    private val appPlacementDao: AppPlacementDao,
    private val folderPlacementDao: FolderPlacementDao,
) {

    /** The target's stored app rows, unhydrated. */
    fun observeAppEntities(target: PlacementTarget): Flow<List<AppPlacementEntity>> =
        appPlacementDao.observe(target.list, target.ownerKey)

    /** The target's stored folder-placement rows. */
    fun observeFolderPlacements(target: PlacementTarget): Flow<List<FolderPlacementEntity>> =
        folderPlacementDao.observe(target.list, target.ownerKey)

    /** [observeAppEntities] for every facet at once, in [facetIds] order. */
    fun observeAppEntitiesForFacets(list: PlacementList, facetIds: List<Long>): Flow<List<List<AppPlacementEntity>>> =
        combine(facetIds.map { appPlacementDao.observe(list, it) }) { it.toList() }

    /** [observeFolderPlacements] for every facet at once, in [facetIds] order. */
    fun observeFolderPlacementsForFacets(list: PlacementList, facetIds: List<Long>): Flow<List<List<FolderPlacementEntity>>> =
        combine(facetIds.map { folderPlacementDao.observe(list, it) }) { it.toList() }

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

    /** Rewrites `position` across apps and folder placements to match [orderedItems]'s order; their saved grid cells are kept. */
    suspend fun reorderItems(target: PlacementTarget, orderedItems: List<PlacedItem>) {
        writeOrder(target, orderedItems)
    }

    /** The Home grid cells saved for [landscape] or portrait, by [gridKey]; an item never moved on the grid is absent. */
    fun observeGridCells(target: PlacementTarget, landscape: Boolean): Flow<Map<String, GridCell>> =
        combine(appPlacementDao.observe(target.list, target.ownerKey), folderPlacementDao.observe(target.list, target.ownerKey)) { apps, folders ->
            buildMap {
                apps.forEach { entity -> entity.cell(landscape)?.let { put(entity.gridKey(), it) } }
                folders.forEach { entity -> entity.cell(landscape)?.let { put(entity.gridKey(), it) } }
            }
        }

    /**
     * Saves a grid move: each [cells] entry for the given orientation. The list order (`position`) and the other orientation's
     * cells are left alone, so the list and the grid each keep their own arrangement.
     */
    suspend fun saveGridCells(target: PlacementTarget, landscape: Boolean, cells: Map<String, GridCell>) {
        appPlacementDao.observe(target.list, target.ownerKey).first().forEach { entity ->
            cells[entity.gridKey()]?.let { appPlacementDao.upsert(entity.withCell(landscape, it)) }
        }
        folderPlacementDao.observe(target.list, target.ownerKey).first().forEach { entity ->
            cells[entity.gridKey()]?.let { folderPlacementDao.upsert(entity.withCell(landscape, it)) }
        }
    }

    private suspend fun writeOrder(target: PlacementTarget, orderedItems: List<PlacedItem>) {
        val existingApps = appPlacementDao.observe(target.list, target.ownerKey).first().associateBy { it.gridKey() }
        val existingFolders = folderPlacementDao.observe(target.list, target.ownerKey).first().associateBy { it.gridKey() }
        orderedItems.forEachIndexed { index, item ->
            val key = item.gridKey()
            when (item) {
                is PlacedItem.SingleApp -> appPlacementDao.upsert(item.app.toEntity(target, index).keepingCellsOf(existingApps[key]))
                is PlacedItem.FolderItem -> folderPlacementDao.upsert(
                    FolderPlacementEntity(list = target.list, facetId = target.facetId, folderId = item.folder.id, position = index)
                        .keepingCellsOf(existingFolders[key]),
                )
            }
        }
    }

    /** Makes [target]'s list an exact copy of [source]'s, grid cells included; what [target] held is replaced. */
    suspend fun copyList(source: PlacementTarget, target: PlacementTarget) {
        val apps = getRawApps(source)
        val folders = getRawFolderPlacements(source)
        deleteAll(target)
        apps.forEach { appPlacementDao.upsert(it.copy(id = 0, list = target.list, facetId = target.facetId, ownerKey = target.ownerKey)) }
        folders.forEach { folderPlacementDao.upsert(it.copy(id = 0, list = target.list, facetId = target.facetId, ownerKey = target.ownerKey)) }
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

    /** F14 Backup & Restore import — puts a restored folder placement back on its saved grid cells. */
    suspend fun restoreFolderCells(target: PlacementTarget, folderId: Long, cells: BackupGridCells) {
        folderPlacementDao.observe(target.list, target.ownerKey).first().filter { it.folderId == folderId }.forEach {
            folderPlacementDao.upsert(
                it.copy(
                    portraitRow = cells.portraitRow, portraitColumn = cells.portraitColumn,
                    landscapeRow = cells.landscapeRow, landscapeColumn = cells.landscapeColumn,
                ),
            )
        }
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
}
