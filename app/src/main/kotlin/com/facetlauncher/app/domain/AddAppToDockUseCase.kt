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
            // Item-counted, not app-counted — a folder occupies a slot too, and an app-only count
            // would let a new app's computed position collide with an existing folder's.
            val position = facetDockAppRepository.observeDockItems(facet.id).first().size
            facetDockAppRepository.addDockApp(facet.id, app, position)
        } else {
            val position = dockAppRepository.observeDockItems().first().size
            dockAppRepository.addDockApp(app, position)
        }
    }
}
