package com.facetlauncher.app.domain

import com.facetlauncher.app.data.DockAppRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import javax.inject.Inject

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
 * Pure resolver over already-live Dock/Favorites lists — deliberately takes those (and each
 * list's own override facet name, `null` for the launcher-wide default) as plain arguments rather
 * than reading [com.facetlauncher.app.data.FacetRepository]/[com.facetlauncher.app.data.SettingsRepository]/
 * the Dock/Favorites repositories itself. Every real caller (Home's own Dock/Favorites/App Drawer/
 * Search long-press menus, all wired through [com.facetlauncher.app.ui.launcher.HomeDrawerRoute])
 * already has this exact data sitting live in [com.facetlauncher.app.ui.home.HomeUiState] — by the
 * time any of those screens can render at all, `HomeUiState.isLoading` is already `false`, so
 * there's no fresh repository read (and no resulting "sheet opens, then its rows pop in a moment
 * later") left to do (see chat history for the flash this used to cause when this instead did its
 * own one-shot suspend reads per open).
 */
class ObserveQuickAddStateUseCase @Inject constructor() {
    fun forApp(app: AppInfo, dockItems: List<PlacedItem>, favoriteItems: List<PlacedItem>, dockFacetName: String?, favoritesFacetName: String?): QuickAddState {
        val component = app.packageName to app.activityName
        return resolve(
            isMember = { it is PlacedItem.SingleApp && (it.app.packageName to it.app.activityName) == component },
            dockItems = dockItems,
            favoriteItems = favoriteItems,
            dockFacetName = dockFacetName,
            favoritesFacetName = favoritesFacetName,
        )
    }

    fun forFolder(folder: Folder, dockItems: List<PlacedItem>, favoriteItems: List<PlacedItem>, dockFacetName: String?, favoritesFacetName: String?): QuickAddState {
        return resolve(
            isMember = { it is PlacedItem.FolderItem && it.folder.id == folder.id },
            dockItems = dockItems,
            favoriteItems = favoriteItems,
            dockFacetName = dockFacetName,
            favoritesFacetName = favoritesFacetName,
        )
    }

    private fun resolve(
        isMember: (PlacedItem) -> Boolean,
        dockItems: List<PlacedItem>,
        favoriteItems: List<PlacedItem>,
        dockFacetName: String?,
        favoritesFacetName: String?,
    ): QuickAddState = QuickAddState(
        favoritesAction = quickPlacementAction(favoriteItems, isMember, favoritesFacetName, AppListLimits.MAX_FAVORITES),
        dockAction = quickPlacementAction(dockItems, isMember, dockFacetName, DockAppRepository.MAX_APPS),
    )

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
