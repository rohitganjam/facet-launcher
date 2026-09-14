package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Remove from Dock"/"Remove from facet dock" row — mirrors [AddAppToDockUseCase]'s
 * own override resolution exactly, but removing instead of adding.
 */
class RemoveAppFromDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overrideDock == true) {
            facetDockAppRepository.removeDockApp(facet.id, app)
        } else {
            dockAppRepository.removeDockApp(app)
        }
    }
}
