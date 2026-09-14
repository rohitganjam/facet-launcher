package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Folder
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Folder counterpart of [AddAppToDockUseCase] — same override resolution, placing a folder instead of an app. */
class AddFolderToDockUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
) {
    suspend operator fun invoke(folder: Folder) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overrideDock == true) {
            val position = facetDockAppRepository.observeDockItems(facet.id).first().size
            facetDockAppRepository.placeFolder(facet.id, folder.id, position)
        } else {
            val position = dockAppRepository.observeDockItems().first().size
            dockAppRepository.placeFolderInDock(folder.id, position)
        }
    }
}
