package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.DefaultFavoriteAppDao
import com.lumenlauncher.app.data.local.DefaultFavoriteAppEntity
import com.lumenlauncher.app.data.model.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
        const val MAX_FAVORITES = 8
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

    /** Uninstall cleanup — driven by [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase]. */
    suspend fun removeByPackage(packageName: String) {
        defaultFavoriteAppDao.deleteByPackage(packageName)
    }

    suspend fun reorderFavorites(orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            defaultFavoriteAppDao.upsert(
                DefaultFavoriteAppEntity(packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }
}
