package com.facetlauncher.app.data

import com.facetlauncher.app.data.local.FacetDockAppDao
import com.facetlauncher.app.data.local.FacetDockAppEntity
import com.facetlauncher.app.data.model.AppInfo
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
 */
@Singleton
class FacetDockAppRepository @Inject constructor(
    private val facetDockAppDao: FacetDockAppDao,
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

    private fun hydrate(entities: List<FacetDockAppEntity>, installed: List<AppInfo>): List<AppInfo> {
        val installedByComponent = installed.associateBy { it.packageName to it.activityName }
        return entities.sortedBy { it.position }
            .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
    }

    suspend fun addDockApp(facetId: Long, app: AppInfo, position: Int) {
        facetDockAppDao.upsert(
            FacetDockAppEntity(
                facetId = facetId,
                packageName = app.packageName,
                activityName = app.activityName,
                position = position,
            ),
        )
    }

    suspend fun removeDockApp(facetId: Long, app: AppInfo) {
        facetDockAppDao.deleteByComponent(facetId, app.packageName, app.activityName)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s dock entry from *every* facet,
     * driven by [com.facetlauncher.app.domain.CleanUpUninstalledAppsUseCase]. Distinct from
     * [observeDockAppsForFacet]'s own runtime filtering, which only hides a momentarily-missing
     * app without deleting anything.
     */
    suspend fun removeByPackage(packageName: String) {
        facetDockAppDao.deleteByPackage(packageName)
    }

    /** Replaces this facet's entire dock with [apps] — used to seed a clean copy (e.g. of the current default dock) when a facet switches to Override. */
    suspend fun replaceDockApps(facetId: Long, apps: List<AppInfo>) {
        facetDockAppDao.deleteAllForFacet(facetId)
        apps.forEachIndexed { index, app ->
            facetDockAppDao.upsert(
                FacetDockAppEntity(facetId = facetId, packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }

    /** F14 Backup & Restore export — raw, unhydrated rows (an app not currently installed still gets backed up). */
    suspend fun getRawDockAppsForFacet(facetId: Long): List<FacetDockAppEntity> =
        facetDockAppDao.observeForFacet(facetId).first()

    /** F14 Backup & Restore import — inserts [entity] as a brand-new row (its own `id` is ignored). */
    suspend fun restoreDockApp(entity: FacetDockAppEntity) {
        facetDockAppDao.upsert(entity.copy(id = 0))
    }

    suspend fun reorderDockApps(facetId: Long, orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            facetDockAppDao.upsert(
                FacetDockAppEntity(
                    facetId = facetId,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    position = index,
                ),
            )
        }
    }
}
