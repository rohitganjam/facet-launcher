package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FolderAppEntity
import com.facetlauncher.app.data.local.FolderDao
import com.facetlauncher.app.data.local.FolderEntity
import com.facetlauncher.app.data.local.FolderWithApps
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the folder library — a folder's identity (name) and membership, independent of anywhere
 * it might be placed. A folder is never auto-deleted for having zero apps; only [deleteFolder]
 * removes one, which cascades through its membership and every placement table via FK.
 */
@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val appRepository: AppRepository,
) {

    /** Every folder, hydrated against installed apps. A 0-app (or all-uninstalled) folder is still emitted. */
    fun observeFolders(): Flow<List<Folder>> {
        return combine(folderDao.observeAllWithApps(), appRepository.observeInstalledApps()) { folders, installed ->
            val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
            folders.map { it.hydrate(installedByComponent) }
        }
    }

    suspend fun createFolder(name: String): Long = folderDao.insertFolder(FolderEntity(name = name))

    suspend fun renameFolder(folderId: Long, name: String) {
        folderDao.renameFolder(folderId, name)
    }

    /** Cascades through membership and every Dock/Favorites placement via FK. */
    suspend fun deleteFolder(folderId: Long) {
        folderDao.deleteFolder(folderId)
    }

    suspend fun addAppToFolder(folderId: Long, app: AppInfo) {
        val currentSize = folderDao.observeAllWithApps().first().find { it.folder.id == folderId }?.apps?.size ?: 0
        folderDao.upsertFolderApp(
            FolderAppEntity(folderId = folderId, packageName = app.packageName, activityName = app.activityName, position = currentSize, profile = app.profile, userId = app.userHandle.hashCode()),
        )
    }

    /** No auto-delete when this empties the folder out — a 0-app folder persists until explicitly deleted. */
    suspend fun removeAppFromFolder(folderId: Long, app: AppInfo) {
        folderDao.deleteFolderApp(folderId, app.packageName, app.activityName, app.userHandle.hashCode())
    }

    /** Rewrites `position` across this folder's own membership to match [orderedApps]' order — the Folder Detail screen's own drag-to-reorder. */
    suspend fun reorderFolderApps(folderId: Long, orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            folderDao.upsertFolderApp(
                FolderAppEntity(folderId = folderId, packageName = app.packageName, activityName = app.activityName, position = index, profile = app.profile, userId = app.userHandle.hashCode()),
            )
        }
    }

    /** Uninstall cleanup — removes every folder's membership row for [packageName] (belonging to [userId]); folders themselves are untouched. */
    suspend fun removeByPackage(packageName: String, userId: Int) {
        folderDao.deleteFolderAppsByPackage(packageName, userId)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByUserId(userId: Int) {
        folderDao.deleteFolderAppsByUserId(userId)
    }

    /** F14 Backup & Restore export — raw, unhydrated folders + membership rows. */
    suspend fun getRawFolders(): List<FolderWithApps> = folderDao.observeAllWithApps().first()

    /** F14 Backup & Restore import — inserts [folder] and its [members] as brand-new rows; returns the new folder id. */
    suspend fun restoreFolder(folder: FolderEntity, members: List<FolderAppEntity>): Long {
        val folderId = folderDao.insertFolder(folder.copy(id = 0))
        members.forEach { member -> folderDao.upsertFolderApp(member.copy(id = 0, folderId = folderId)) }
        return folderId
    }

    /** F14 Backup & Restore — wipes every folder (and, via cascade, its membership + every placement) before restoring. */
    suspend fun deleteAllFolders() {
        folderDao.deleteAllFolders()
    }

    /** Rows still at the migration's `-1` `userId` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    suspend fun getOrphanedRows(): List<FolderAppEntity> = folderDao.getOrphanedFolderApps()

    /** Backfills [entity]'s real `userId` once [RepairOrphanedProfileRowsUseCase] resolves it — updates in place (same `id`), doesn't create a new row. */
    suspend fun backfillUserId(entity: FolderAppEntity, userId: Int) {
        folderDao.upsertFolderApp(entity.copy(userId = userId))
    }

    private fun FolderWithApps.hydrate(installedByComponent: Map<Triple<String, String, Int>, AppInfo>): Folder {
        val hydratedApps = apps.sortedBy { it.position }
            .mapNotNull { installedByComponent[Triple(it.packageName, it.activityName, it.userId)] }
        return Folder(id = folder.id, name = folder.name, apps = hydratedApps)
    }
}
