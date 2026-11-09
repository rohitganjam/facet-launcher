package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultAppRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject

/**
 * Puts this device's own default browser, messaging, camera, mail and phone apps (whichever resolve and are
 * installed) into a facet's Dock, so a new facet's Home isn't empty. Favorites start empty.
 */
class SeedFacetDefaultsUseCase @Inject constructor(
    private val defaultAppRepository: DefaultAppRepository,
    private val placementRepository: PlacementRepository,
    private val getInstalledApps: GetInstalledAppsUseCase,
) {
    /** Seeds [facetId]'s Dock, only if it is empty; or, with [replace], clears it first (the Dock's "Reset to default"). */
    suspend operator fun invoke(facetId: Long, replace: Boolean = false) {
        val dock = PlacementTarget(PlacementList.DOCK, facetId)
        if (replace) placementRepository.deleteAll(dock) else if (placementRepository.getRawApps(dock).isNotEmpty()) return
        val installed = getInstalledApps()
        defaultAppRepository.getDefaultAppPackages()
            .mapNotNull { packageName -> installed.firstOrNull { it.packageName == packageName } }
            .take(AppListLimits.MAX_DOCK_APPS)
            .forEachIndexed { position, app -> placementRepository.addApp(dock, app, position) }
    }
}
