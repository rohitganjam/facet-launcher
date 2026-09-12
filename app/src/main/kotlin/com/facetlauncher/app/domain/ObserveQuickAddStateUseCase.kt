package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppListLimits
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Backs the "Add to Favorites"/"Add to Dock" rows in [com.facetlauncher.app.ui.components.AppContextMenu].
 * A `null` field hides its row entirely (that list is already at its cap); a non-null [Boolean]
 * shows it and says whether it reads "Add to facet favorites/dock" (the active facet is
 * overriding its own) or "Add to Favorites/Dock" (the launcher-wide default).
 */
data class QuickAddState(
    val favoritesOverride: Boolean? = null,
    val dockOverride: Boolean? = null,
)

/**
 * Spans [FacetRepository]/[SettingsRepository] (to resolve the active facet and whether it's
 * overriding Favorites/Dock) and both the facet-scoped and launcher-wide default Favorites/Dock
 * repositories, so it's a UseCase rather than living directly in a ViewModel — mirrors
 * [ObserveHomeScreenStateUseCase]'s own override-resolution branching for [HomeScreenState.dockApps]/
 * [HomeScreenState.appListItems].
 */
class ObserveQuickAddStateUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val facetRepository: FacetRepository,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val dockAppRepository: DockAppRepository,
    private val facetDockAppRepository: FacetDockAppRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<QuickAddState> {
        val activeFacet = combine(settingsRepository.settings, facetRepository.observeFacets()) { settings, facets ->
            facets.find { it.id == settings.activeFacetId }
        }

        val favorites = activeFacet.flatMapLatest { facet ->
            if (facet?.overridingFavorites == true) {
                favoriteAppRepository.observeFavoritesForFacet(facet.id).map { it.size to true }
            } else {
                defaultFavoriteAppRepository.observeDefaultFavorites().map { it.size to false }
            }
        }

        val dock = activeFacet.flatMapLatest { facet ->
            if (facet?.overrideDock == true) {
                facetDockAppRepository.observeDockAppsForFacet(facet.id).map { it.size to true }
            } else {
                dockAppRepository.observeDockApps().map { it.size to false }
            }
        }

        return combine(favorites, dock) { (favoritesCount, favoritesOverride), (dockCount, dockOverride) ->
            QuickAddState(
                favoritesOverride = favoritesOverride.takeIf { favoritesCount < AppListLimits.MAX_FAVORITES },
                dockOverride = dockOverride.takeIf { dockCount < DockAppRepository.MAX_APPS },
            )
        }
    }
}
