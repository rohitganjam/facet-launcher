package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.Folder
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Folder counterpart of [AddAppToFavoritesUseCase] — same override resolution, placing a folder instead of an app. */
class AddFolderToFavoritesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(folder: Folder) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overridingFavorites == true) {
            val position = favoriteAppRepository.observeFavoriteItems(facet.id).first().size
            favoriteAppRepository.placeFolder(facet.id, folder.id, position)
        } else {
            val position = defaultFavoriteAppRepository.observeDefaultItems().first().size
            defaultFavoriteAppRepository.placeFolder(folder.id, position)
        }
    }
}
