package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.ProfileDockAppDao
import com.lumenlauncher.app.data.local.ProfileDockAppEntity
import com.lumenlauncher.app.data.model.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [ProfileDockAppDao] — the per-profile counterpart to [DockAppRepository] (the
 * launcher-wide default dock), mirroring [FavoriteAppRepository]'s shape exactly: stored
 * (packageName, activityName) rows hydrated live against [AppRepository.observeInstalledApps],
 * an entry whose app is no longer installed collapsed out rather than shown as a dead tile.
 * Consumed only while a profile's [com.lumenlauncher.app.data.local.ProfileEntity.overrideDock]
 * is set (see [com.lumenlauncher.app.domain.ObserveHomeScreenStateUseCase]).
 */
@Singleton
class ProfileDockAppRepository @Inject constructor(
    private val profileDockAppDao: ProfileDockAppDao,
    private val appRepository: AppRepository,
) {

    fun observeDockAppsForProfile(profileId: Long): Flow<List<AppInfo>> {
        return combine(profileDockAppDao.observeForProfile(profileId), appRepository.observeInstalledApps()) { entities, installed ->
            hydrate(entities, installed)
        }
    }

    /**
     * Every profile's dock at once, keyed by profile id — for the profile carousel's preview
     * cards, which need every profile's dock while browsing, not just the active one's. Subscribes
     * to the live installed-app list exactly once and reuses it across all [profileIds], mirroring
     * [FavoriteAppRepository.observeFavoritesForProfiles].
     */
    fun observeDockAppsForProfiles(profileIds: List<Long>): Flow<Map<Long, List<AppInfo>>> {
        if (profileIds.isEmpty()) return flowOf(emptyMap())
        val entitiesPerProfile = combine(profileIds.map { profileDockAppDao.observeForProfile(it) }) { it }
        return combine(entitiesPerProfile, appRepository.observeInstalledApps()) { perProfile, installed ->
            profileIds.indices.associate { index -> profileIds[index] to hydrate(perProfile[index], installed) }
        }
    }

    private fun hydrate(entities: List<ProfileDockAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { it.packageName to it.activityName }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
    }

    suspend fun addDockApp(profileId: Long, app: AppInfo, position: Int) {
        profileDockAppDao.upsert(
            ProfileDockAppEntity(
                profileId = profileId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
            ),
        )
    }

    suspend fun removeDockApp(profileId: Long, app: AppInfo) {
        profileDockAppDao.deleteByComponent(profileId, app.packageName, app.activityName)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s dock entry from *every* profile,
     * driven by [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase]. Distinct from
     * [observeDockAppsForProfile]'s own runtime filtering, which only hides a momentarily-missing
     * app without deleting anything.
     */
    suspend fun removeByPackage(packageName: String) {
        profileDockAppDao.deleteByPackage(packageName)
    }

    /** Replaces this profile's entire dock with [apps] — used to seed a clean copy (e.g. of the current default dock) when a profile switches to Override. */
    suspend fun replaceDockApps(profileId: Long, apps: List<AppInfo>) {
        profileDockAppDao.deleteAllForProfile(profileId)
        apps.forEachIndexed { index, app ->
            profileDockAppDao.upsert(
                ProfileDockAppEntity(profileId = profileId, packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDockAppsForProfile(profileId: Long): List<ProfileDockAppEntity> =
        profileDockAppDao.observeForProfile(profileId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDockApp(entity: ProfileDockAppEntity) {
        profileDockAppDao.upsert(entity.copy(id = 0))
    }

    suspend fun reorderDockApps(profileId: Long, orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            profileDockAppDao.upsert(
                ProfileDockAppEntity(
                    profileId = profileId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    position = index,
                ),
            )
        }
    }
}
