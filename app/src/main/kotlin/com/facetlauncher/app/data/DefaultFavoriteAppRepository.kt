package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.DefaultFavoriteAppDao
import com.facetlauncher.app.data.local.DefaultFavoriteAppEntity
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The launcher-wide default Favorites list — wraps [DefaultFavoriteAppDao], hydrating stored
 * (packageName, activityName) rows against the live installed-app list, mirroring
 * [DockAppRepository]'s uninstall-collapse pattern exactly. This is the baseline Favorites shown
 * by any profile that isn't overriding its own (see [ProfileEntity.overridingFavorites]) —
 * edited from Settings' own "Default favorites" card, distinct from [FavoriteAppRepository]'s
 * per-profile lists.
 */
@Singleton
class DefaultFavoriteAppRepository @Inject constructor(
    private val defaultFavoriteAppDao: DefaultFavoriteAppDao,
    private val appRepository: AppRepository,
) {

    companion object {
        const val MAX_FAVORITES = AppListLimits.MAX_FAVORITES
    }

    fun observeDefaultFavorites(): Flow<List<AppInfo>> {
        return combine(defaultFavoriteAppDao.observeAll(), appRepository.observeInstalledApps()) { entities, installed ->
            val installedByComponent = installed.associateBy { it.packageName to it.activityName }
            entities.sortedBy { it.position }
                .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
        }
    }

    suspend fun addFavorite(app: AppInfo, position: Int) {
        defaultFavoriteAppDao.upsert(
            DefaultFavoriteAppEntity(packageName = app.packageName, activityName = app.activityName, position = position),
        )
    }

    suspend fun removeFavorite(app: AppInfo) {
        defaultFavoriteAppDao.deleteByComponent(app.packageName, app.activityName)
    }

    /** Uninstall cleanup — driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase]. */
    suspend fun removeByPackage(packageName: String) {
        defaultFavoriteAppDao.deleteByPackage(packageName)
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDefaultFavorites(): List<DefaultFavoriteAppEntity> = defaultFavoriteAppDao.observeAll().first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDefaultFavorite(entity: DefaultFavoriteAppEntity) {
        defaultFavoriteAppDao.upsert(entity.copy(id = 0))
    }

    /** F14 Backup & Restore — wipes the whole list before restoring from a backup. */
    suspend fun deleteAllDefaultFavorites() {
        defaultFavoriteAppDao.deleteAll()
    }

    suspend fun reorderFavorites(orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            defaultFavoriteAppDao.upsert(
                DefaultFavoriteAppEntity(packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }
}
