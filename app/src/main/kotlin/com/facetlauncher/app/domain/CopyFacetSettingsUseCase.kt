package com.facetlauncher.app.domain

import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.data.model.PlacementTarget
import javax.inject.Inject

/**
 * "Copy from facet" for the Dock and Apps list screens: the target facet takes the source's apps, folders and grid cells
 * plus that screen's settings, replacing what it had. The widget stack needs the host round trip, so it has its own use case.
 */
class CopyFacetSettingsUseCase @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val facetRepository: FacetRepository,
) {
    suspend fun copyDock(sourceId: Long, targetId: Long) {
        if (sourceId == targetId) return
        placementRepository.copyList(PlacementTarget(PlacementList.DOCK, sourceId), PlacementTarget(PlacementList.DOCK, targetId))
        facetRepository.copyDockSettings(sourceId, targetId)
    }

    suspend fun copyAppsList(sourceId: Long, targetId: Long) {
        if (sourceId == targetId) return
        placementRepository.copyList(PlacementTarget(PlacementList.FAVORITES, sourceId), PlacementTarget(PlacementList.FAVORITES, targetId))
        facetRepository.copyAppsListSettings(sourceId, targetId)
    }
}
