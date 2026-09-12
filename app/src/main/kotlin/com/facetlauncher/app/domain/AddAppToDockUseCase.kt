package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Add to Dock"/"Add to facet dock" row — mirrors [AddAppToFavoritesUseCase]
 * exactly, but for the Dock (the active facet's own when it's overriding, otherwise the
 * launcher-wide default), matching [ObserveQuickAddStateUseCase]'s own resolution.
 */
class AddAppToDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overrideDock == true) {
            val position = facetDockAppRepository.observeDockAppsForFacet(facet.id).first().size
            facetDockAppRepository.addDockApp(facet.id, app, position)
        } else {
            val position = dockAppRepository.observeDockApps().first().size
            dockAppRepository.addDockApp(app, position)
        }
    }
}
