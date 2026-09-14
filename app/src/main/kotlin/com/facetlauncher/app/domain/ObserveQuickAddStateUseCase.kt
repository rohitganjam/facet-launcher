package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FacetDockAppRepository
import com.facetlauncher.app.data.FacetRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Whether the "Add to X"/"Remove from X" row shows, and which action it performs. [facetName] is
 * `null` for the launcher-wide default list or the active facet's own real name when it's
 * overriding — [com.facetlauncher.app.ui.components.AppContextMenu] renders it as a small
 * trailing badge next to the row's own unconditional "Add to Favorites"/"Add to Dock" label
 * ("Global" when `null`, the facet's name otherwise) rather than folding it into the sentence,
 * so the two cases read unambiguously (see chat history).
 */
sealed interface QuickPlacementAction {
    val facetName: String?
    data class Add(override val facetName: String?) : QuickPlacementAction
    data class Remove(override val facetName: String?) : QuickPlacementAction
}

/**
 * Backs the Favorites/Dock rows in [com.facetlauncher.app.ui.components.AppContextMenu] and
 * [com.facetlauncher.app.ui.components.FolderTileContextMenu]. A `null` field hides its row
 * entirely (the item isn't already a member AND the list is at its cap); a non-null
 * [QuickPlacementAction] shows the row as either Add or Remove, naming either the launcher-wide
 * default list or the active facet's own (see [QuickPlacementAction]'s own doc).
 */
data class QuickAddState(
    val favoritesAction: QuickPlacementAction? = null,
    val dockAction: QuickPlacementAction? = null,
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
    suspend fun forApp(app: AppInfo): QuickAddState {
        val component = app.packageName to app.activityName
        return resolve(
            isMember = { it is PlacedItem.SingleApp && (it.app.packageName to it.app.activityName) == component },
        )
    }

    suspend fun forFolder(folder: Folder): QuickAddState {
        return resolve(
            isMember = { it is PlacedItem.FolderItem && it.folder.id == folder.id },
        )
    }

    private suspend fun resolve(isMember: (PlacedItem) -> Boolean): QuickAddState {
        val activeFacetId = settingsRepository.settings.first().activeFacetId
        val facet = facetRepository.getById(activeFacetId)

        val favoritesAction: QuickPlacementAction?
        if (facet != null && facet.overridingFavorites) {
            val favoriteItems = favoriteAppRepository.observeFavoriteItems(facet.id).first()
            favoritesAction = quickPlacementAction(favoriteItems, isMember, facet.name, AppListLimits.MAX_FAVORITES)
        } else {
            val favoriteItems = defaultFavoriteAppRepository.observeDefaultItems().first()
            favoritesAction = quickPlacementAction(favoriteItems, isMember, facetName = null, AppListLimits.MAX_FAVORITES)
        }

        val dockAction: QuickPlacementAction?
        if (facet != null && facet.overrideDock) {
            val dockItems = facetDockAppRepository.observeDockItems(facet.id).first()
            dockAction = quickPlacementAction(dockItems, isMember, facet.name, DockAppRepository.MAX_APPS)
        } else {
            val dockItems = dockAppRepository.observeDockItems().first()
            dockAction = quickPlacementAction(dockItems, isMember, facetName = null, DockAppRepository.MAX_APPS)
        }

        return QuickAddState(favoritesAction = favoritesAction, dockAction = dockAction)
    }

    private fun quickPlacementAction(
        items: List<PlacedItem>,
        isMember: (PlacedItem) -> Boolean,
        facetName: String?,
        maxItems: Int,
    ): QuickPlacementAction? {
        return when {
            items.any(isMember) -> QuickPlacementAction.Remove(facetName)
            items.size < maxItems -> QuickPlacementAction.Add(facetName)
            else -> null
        }
    }
}
