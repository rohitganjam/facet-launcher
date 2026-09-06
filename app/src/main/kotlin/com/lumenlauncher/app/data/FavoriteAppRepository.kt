package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.FavoriteAppDao
import com.lumenlauncher.app.data.local.FavoriteAppEntity
import com.lumenlauncher.app.data.model.AppInfo
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
 * a dead tile (F2's uninstall-collapse rule, applied here per-profile), and now genuinely live
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
        const val MAX_FAVORITES = 8
    }

    fun observeFavoritesForProfile(profileId: Long): Flow<List<AppInfo>> {
        return combine(favoriteAppDao.observeForProfile(profileId), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }
    }

    /**
     * Every profile's favorites at once, keyed by profile id — for the profile carousel's
     * preview cards, which need to show *every* profile's favorites while browsing, not just
     * the active one's. Subscribes to the live installed-app list exactly once and reuses it
     * across all [profileIds], rather than each profile independently re-querying it (as calling
     * [observeFavoritesForProfile] once per profile would do) — with several profiles browsed
     * simultaneously that redundant fan-out of real `LauncherApps` queries is wasteful and, in
     * practice, was slow enough to make callers relying on a single-frame result flaky.
     */
    fun observeFavoritesForProfiles(profileIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (profileIds.isEmpty()) return flowOf(emptyMap())
        val entitiesPerProfile = combine(profileIds.map { favoriteAppDao.observeForProfile(it) }) { it }
        return combine(entitiesPerProfile, appRepository.observeInstalledApps()) { perProfile, installed ->
            profileIds.indices.associate { index -> profileIds[index] to hydrate(perProfile[index], installed) }
        }
    }

    private fun hydrate(entities: List<FavoriteAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { it.packageName to it.activityName }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
    }

    suspend fun addFavorite(profileId: Long, app: AppInfo, position: Int) {
        favoriteAppDao.upsert(
            FavoriteAppEntity(
                profileId = profileId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
            ),
        )
    }

    suspend fun removeFavorite(profileId: Long, app: AppInfo) {
        favoriteAppDao.deleteByComponent(profileId, app.packageName, app.activityName)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s favorite entry from *every*
     * profile, driven by [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase]. Distinct
     * from [observeFavoritesForProfile]'s own runtime filtering, which reacts to any reason an
     * app might be momentarily missing without deleting anything — this only runs for a
     * genuine, permanent uninstall.
     */
    suspend fun removeByPackage(packageName: String) {
        favoriteAppDao.deleteByPackage(packageName)
    }

    /** Replaces this profile's entire favorites list with [apps] — used to seed a clean copy (e.g. of the current default list) when a profile switches to Override. */
    suspend fun replaceFavorites(profileId: Long, apps: List<AppInfo>) {
        favoriteAppDao.deleteAllForProfile(profileId)
        apps.forEachIndexed { index, app ->
            favoriteAppDao.upsert(
                FavoriteAppEntity(profileId = profileId, packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up, unlike every other read here). */
    suspend fun getRawFavoritesForProfile(profileId: Long): List<FavoriteAppEntity> =
        favoriteAppDao.observeForProfile(profileId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreFavorite(entity: FavoriteAppEntity) {
        favoriteAppDao.upsert(entity.copy(id = 0))
    }

    suspend fun reorderFavorites(profileId: Long, orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            favoriteAppDao.upsert(
                FavoriteAppEntity(
                    profileId = profileId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    position = index,
                ),
            )
        }
    }
}
