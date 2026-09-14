package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DockAppDao
import com.facetlauncher.app.data.local.DockAppEntity
import com.facetlauncher.app.data.local.DockFolderPlacementDao
import com.facetlauncher.app.data.local.DockFolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [DockAppDao], hydrating stored (packageName, activityName) rows against the live
 * installed-app list (via [AppRepository.observeInstalledApps]). An entry whose app is no
 * longer installed is filtered out of the emitted list (F3's uninstall-collapse rule) rather
 * than shown as a dead tile — live, so an uninstall via F12's long-press menu collapses the
 * dock immediately rather than needing a restart.
 *
 * A folder ([DockFolderPlacementDao]) occupies one slot in the same `position` ordering space as
 * standalone dock apps — merged in Kotlin by [observeDockItems] rather than enforced across
 * tables by Room, the same way [observeDockApps] already merges Room rows against installed
 * apps. Folder identity/membership itself lives in [FolderRepository]; this repository only
 * owns *whether* a given folder currently occupies a Dock slot.
 */
@Singleton
class DockAppRepository @Inject constructor(
    private val dockAppDao: DockAppDao,
    private val dockFolderPlacementDao: DockFolderPlacementDao,
    private val folderRepository: FolderRepository,
    private val appRepository: AppRepository,
) {

    companion object {
        /** No floor — the dock can be emptied out entirely; Home simply omits the dock row when it is. */
        const val MIN_APPS = 0
        const val MAX_APPS = 5
    }

    fun observeDockApps(): Flow<List<AppInfo>> {
        return combine(dockAppDao.observeAll(), appRepository.observeInstalledApps()) { entities, installed ->
            val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.profile) }
            entities.sortedBy { it.position }
                .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.profile)] }
        }
    }

    /**
     * The Dock's full ordered content, apps and folders interleaved by [PlacedItem]'s shared
     * `position` space. A 0-app folder placed in the Dock is still emitted (requirement: folders
     * never auto-hide for being empty) — only an uninstall-collapsed *app* slot is dropped.
     */
    fun observeDockItems(): Flow<List<PlacedItem>> {
        return combine(
            dockAppDao.observeAll(),
            dockFolderPlacementDao.observeAll(),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
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

            (appItems + folderItems).sortedBy { it.first }.map { it.second }
        }
    }

    suspend fun addDockApp(app: AppInfo, position: Int) {
        dockAppDao.upsert(
            DockAppEntity(packageName = app.packageName, activityName = app.activityName, position = position, profile = app.profile),
        )
    }

    suspend fun removeDockApp(app: AppInfo) {
        dockAppDao.deleteByComponent(app.packageName, app.activityName, app.profile)
    }

    suspend fun placeFolderInDock(folderId: Long, position: Int) {
        dockFolderPlacementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = position))
    }

    suspend fun removeFolderFromDock(folderId: Long) {
        dockFolderPlacementDao.deleteByFolderId(folderId)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s dock entry (if any), driven by
     * [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase] (which cleans up
     * [FolderRepository] itself directly, alongside every placement repository). Distinct from
     * [observeDockApps]/[observeDockItems]'s own runtime filtering, which reacts to *any* reason
     * an app might be momentarily missing (mid-update via `onPackagesUnavailable`, for instance)
     * without deleting anything — this only runs for a genuine, permanent uninstall.
     */
    suspend fun removeByPackage(packageName: String, profile: AppProfile) {
        dockAppDao.deleteByPackage(packageName, profile)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByProfile(profile: AppProfile) {
        dockAppDao.deleteByProfile(profile)
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDockApps(): List<DockAppEntity> = dockAppDao.observeAll().first()

    /** F14 Backup & Restore export — raw folder-placement rows (the folder library itself is exported once, globally, by [FolderRepository]). */
    suspend fun getRawDockFolderPlacements(): List<DockFolderPlacementEntity> = dockFolderPlacementDao.observeAll().first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDockApp(entity: DockAppEntity) {
        dockAppDao.upsert(entity.copy(id = 0))
    }

    /** F14 Backup & Restore import — places a restored folder (by its already-remapped [folderId]) into the Dock. */
    suspend fun restoreDockFolderPlacement(folderId: Long, position: Int) {
        dockFolderPlacementDao.upsert(DockFolderPlacementEntity(folderId = folderId, position = position))
    }

    /** F14 Backup & Restore — wipes the whole dock (apps and folder placements) before restoring from a backup. */
    suspend fun deleteAllDockApps() {
        dockAppDao.deleteAll()
        dockFolderPlacementDao.deleteAll()
    }

    suspend fun reorderDockApps(orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            dockAppDao.upsert(
                DockAppEntity(packageName = app.packageName, activityName = app.activityName, position = index, profile = app.profile),
            )
        }
    }

    /** Rewrites `position` across both `dock_apps` and `dock_folder_placements` to match [orderedItems]'s order. */
    suspend fun reorderDockItems(orderedItems: List<PlacedItem>) {
        orderedItems.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> dockAppDao.upsert(
                    DockAppEntity(packageName = item.app.packageName, activityName = item.app.activityName, position = index, profile = item.app.profile),
                )
                is PlacedItem.FolderItem -> dockFolderPlacementDao.upsert(
                    DockFolderPlacementEntity(folderId = item.folder.id, position = index),
                )
            }
        }
    }
}
