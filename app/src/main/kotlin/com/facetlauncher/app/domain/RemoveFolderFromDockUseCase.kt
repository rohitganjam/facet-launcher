package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Folder
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Folder counterpart of [RemoveAppFromDockUseCase] — same override resolution, removing a folder instead of an app. */
class RemoveFolderFromDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
) {
    suspend operator fun invoke(folder: Folder) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overrideDock == true) {
            facetDockAppRepository.removeFolderPlacement(facet.id, folder.id)
        } else {
            dockAppRepository.removeFolderFromDock(folder.id)
        }
    }
}
