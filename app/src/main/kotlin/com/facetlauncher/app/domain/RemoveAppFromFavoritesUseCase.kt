package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Remove from Favorites"/"Remove from facet favorites" row — mirrors
 * [AddAppToFavoritesUseCase]'s own override resolution exactly, but removing instead of adding.
 */
class RemoveAppFromFavoritesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overridingFavorites == true) {
            favoriteAppRepository.removeFavorite(facet.id, app)
        } else {
            defaultFavoriteAppRepository.removeFavorite(app)
        }
    }
}
