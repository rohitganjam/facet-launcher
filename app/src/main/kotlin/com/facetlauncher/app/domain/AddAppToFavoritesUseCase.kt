package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * F12's long-press "Add to Favorites"/"Add to facet favorites" row — appends [app] to whichever
 * list [com.facetlauncher.app.ui.components.AppContextMenu] is currently showing that row for
 * (the active facet's own Favorites when it's overriding, otherwise the launcher-wide default),
 * mirroring [ObserveQuickAddStateUseCase]'s own resolution exactly so the write always lands where
 * the row said it would.
 */
class AddAppToFavoritesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
) {
    suspend operator fun invoke(app: AppInfo) {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)
        if (facet?.overridingFavorites == true) {
            val position = favoriteAppRepository.observeFavoritesForFacet(facet.id).first().size
            favoriteAppRepository.addFavorite(facet.id, app, position)
        } else {
            val position = defaultFavoriteAppRepository.observeDefaultFavorites().first().size
            defaultFavoriteAppRepository.addFavorite(app, position)
        }
    }
}
