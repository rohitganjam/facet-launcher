package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Folder
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Folder counterpart of [RemoveAppFromFavoritesUseCase] — same override resolution, removing a folder instead of an app. */
class RemoveFolderFromFavoritesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(folder: Folder) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overridingFavorites == true) {
            favoriteAppRepository.removeFolderPlacement(facet.id, folder.id)
        } else {
            defaultFavoriteAppRepository.removeFolderPlacement(folder.id)
        }
    }
}
