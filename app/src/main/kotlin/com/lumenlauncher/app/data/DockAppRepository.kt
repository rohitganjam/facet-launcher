package com.lumenlauncher.app.data

import com.lumenlauncher.app.data.local.DockAppDao
import com.lumenlauncher.app.data.local.DockAppEntity
import com.lumenlauncher.app.data.model.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [DockAppDao], hydrating stored (packageName, activityName) rows against the live
 * installed-app list (via [AppRepository.observeInstalledApps]). An entry whose app is no
 * longer installed is filtered out of the emitted list (F3's uninstall-collapse rule) rather
 * than shown as a dead tile — live, so an uninstall via F12's long-press menu collapses the
 * dock immediately rather than needing a restart.
 */
@Singleton
class DockAppRepository @Inject constructor(
    private val dockAppDao: DockAppDao,
    private val appRepository: AppRepository,
) {

    companion object {
        /** No floor — the dock can be emptied out entirely; Home simply omits the dock row when it is. */
        const val MIN_APPS = 0
        const val MAX_APPS = 5
    }

    fun observeDockApps(): Flow<List<AppInfo>> {
        return combine(dockAppDao.observeAll(), appRepository.observeInstalledApps()) { entities, installed ->
            val installedByComponent = installed.associateBy { it.packageName to it.activityName }
            entities.sortedBy { it.position }
                .mapNotNull { entity -> installedByComponent[entity.packageName to entity.activityName] }
        }
    }

    suspend fun addDockApp(app: AppInfo, position: Int) {
        dockAppDao.upsert(
            DockAppEntity(packageName = app.packageName, activityName = app.activityName, position = position),
        )
    }

    suspend fun removeDockApp(app: AppInfo) {
        dockAppDao.deleteByComponent(app.packageName, app.activityName)
    }

    /**
     * Uninstall cleanup — permanently removes [packageName]'s dock entry (if any), driven by
     * [com.lumenlauncher.app.domain.CleanUpUninstalledAppsUseCase]. Distinct from
     * [observeDockApps]'s own runtime filtering, which reacts to *any* reason an app might be
     * momentarily missing (mid-update via `onPackagesUnavailable`, for instance) without
     * deleting anything — this only runs for a genuine, permanent uninstall.
     */
    suspend fun removeByPackage(packageName: String) {
        dockAppDao.deleteByPackage(packageName)
    }

    suspend fun reorderDockApps(orderedApps: List<AppInfo>) {
        orderedApps.forEachIndexed { index, app ->
            dockAppDao.upsert(
                DockAppEntity(packageName = app.packageName, activityName = app.activityName, position = index),
            )
        }
    }
}
