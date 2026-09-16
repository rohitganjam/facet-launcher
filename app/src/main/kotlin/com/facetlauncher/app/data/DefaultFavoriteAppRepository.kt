package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DefaultFavoriteAppDao
import com.facetlauncher.app.data.local.DefaultFavoriteAppEntity
import com.facetlauncher.app.data.local.DefaultFavoriteFolderPlacementDao
import com.facetlauncher.app.data.local.DefaultFavoriteFolderPlacementEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.AppProfile
import com.facetlauncher.app.data.model.PlacedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The launcher-wide default Favorites list — wraps [DefaultFavoriteAppDao], hydrating stored
 * (packageName, activityName) rows against the live installed-app list, mirroring
 * [DockAppRepository]'s uninstall-collapse pattern exactly. This is the baseline Favorites shown
 * by any facet that isn't overriding its own (see [FacetEntity.overridingFavorites]) —
 * edited from Settings' own "Default favorites" card, distinct from [FavoriteAppRepository]'s
 * per-facet lists.
 *
 * A folder ([DefaultFavoriteFolderPlacementDao]) occupies one slot in the same `position`
 * ordering space as standalone favorite apps, merged in Kotlin by [observeDefaultItems].
 */
@Singleton
class DefaultFavoriteAppRepository @Inject constructor(
    private val defaultFavoriteAppDao: DefaultFavoriteAppDao,
    private val defaultFavoriteFolderPlacementDao: DefaultFavoriteFolderPlacementDao,
    private val folderRepository: FolderRepository,
    private val appRepository: AppRepository,
) {

    companion object {
        const val MAX_FAVORITES = AppListLimits.MAX_FAVORITES
    }

    fun observeDefaultFavorites(): Flow<List<AppInfo>> {
        return combine(defaultFavoriteAppDao.observeAll(), appRepository.observeInstalledApps()) { entities, installed ->
            val installedByComponent = installed.associateBy { Triple(it.packageName, it.activityName, it.userHandle.hashCode()) }
            entities.sortedBy { it.position }
                .mapNotNull { entity -> installedByComponent[Triple(entity.packageName, entity.activityName, entity.userId)] }
        }
    }

    /** The default Favorites list's full ordered content, apps and folders interleaved. A 0-app folder placed here is still emitted. */
    fun observeDefaultItems(): Flow<List<PlacedItem>> {
        return combine(
            defaultFavoriteAppDao.observeAll(),
            defaultFavoriteFolderPlacementDao.observeAll(),
            folderRepository.observeFolders(),
            appRepository.observeInstalledApps(),
        ) { appEntities, placements, folders, installed ->
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
            (appItems + folderItems).sortedBy { it.first }.map { it.second }
        }
    }

    suspend fun addFavorite(app: AppInfo, position: Int) {
        defaultFavoriteAppDao.upsert(
            DefaultFavoriteAppEntity(packageName = app.packageName, activityName = app.activityName, position = position, profile = app.profile, userId = app.userHandle.hashCode()),
        )
    }

    suspend fun removeFavorite(app: AppInfo) {
        defaultFavoriteAppDao.deleteByComponent(app.packageName, app.activityName, app.userHandle.hashCode())
    }

    suspend fun placeFolder(folderId: Long, position: Int) {
        defaultFavoriteFolderPlacementDao.upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = position))
    }

    suspend fun removeFolderPlacement(folderId: Long) {
        defaultFavoriteFolderPlacementDao.deleteByFolderId(folderId)
    }

    /** Uninstall cleanup — driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase]. */
    suspend fun removeByPackage(packageName: String, userId: Int) {
        defaultFavoriteAppDao.deleteByPackage(packageName, userId)
    }

    /** Bulk cleanup for a whole profile vanishing (e.g. Work Profile unenrollment) — see [AppRepository.observeProfileRemoved]. */
    suspend fun removeByUserId(userId: Int) {
        defaultFavoriteAppDao.deleteByUserId(userId)
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDefaultFavorites(): List<DefaultFavoriteAppEntity> = defaultFavoriteAppDao.observeAll().first()

    /** F14 Backup & Restore export — raw folder-placement rows for the default favorites list. */
    suspend fun getRawDefaultFavoriteFolderPlacements(): List<DefaultFavoriteFolderPlacementEntity> =
        defaultFavoriteFolderPlacementDao.observeAll().first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDefaultFavorite(entity: DefaultFavoriteAppEntity) {
        defaultFavoriteAppDao.upsert(entity.copy(id = 0))
    }

    /** Rows still at the migration's `-1` `userId` sentinel — see `RepairOrphanedProfileRowsUseCase`. */
    suspend fun getOrphanedRows(): List<DefaultFavoriteAppEntity> = defaultFavoriteAppDao.getOrphaned()

    /** Backfills [entity]'s real `userId` once [RepairOrphanedProfileRowsUseCase] resolves it — updates in place (same `id`), doesn't create a new row. */
    suspend fun backfillUserId(entity: DefaultFavoriteAppEntity, userId: Int) {
        defaultFavoriteAppDao.upsert(entity.copy(userId = userId))
    }

    /** F14 Backup & Restore import — places a restored folder (by its already-remapped [folderId]) into the default favorites list. */
    suspend fun restoreDefaultFavoriteFolderPlacement(folderId: Long, position: Int) {
        defaultFavoriteFolderPlacementDao.upsert(DefaultFavoriteFolderPlacementEntity(folderId = folderId, position = position))
    }

    /** F14 Backup & Restore — wipes the whole list before restoring from a backup. */
    suspend fun deleteAllDefaultFavorites() {
        defaultFavoriteAppDao.deleteAll()
        defaultFavoriteFolderPlacementDao.deleteAll()
    }

    suspend fun reorderItems(orderedItems: List<PlacedItem>) {
        orderedItems.forEachIndexed { index, item ->
            when (item) {
                is PlacedItem.SingleApp -> defaultFavoriteAppDao.upsert(
                    DefaultFavoriteAppEntity(
                        packageName = item.app.packageName,
                        activityName = item.app.activityName,
                        position = index,
                        profile = item.app.profile,
                        userId = item.app.userHandle.hashCode(),
                    ),
                )
                is PlacedItem.FolderItem -> defaultFavoriteFolderPlacementDao.upsert(
                    DefaultFavoriteFolderPlacementEntity(folderId = item.folder.id, position = index),
                )
            }
        }
    }
}
