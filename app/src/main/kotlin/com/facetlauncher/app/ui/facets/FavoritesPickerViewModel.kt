package com.facetlauncher.app.ui.facets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.DefaultFavoriteAppRepository
import com.facetlauncher.app.data.FavoriteAppRepository
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritesPickerUiState(
    /** `false` when this picker is editing the launcher-wide default Favorites list (reached from Settings) rather than one facet's own. */
    val isFacetScoped: Boolean = true,
    val query: String = "",
    /** Favorited apps at the moment this screen was opened, in their favorites order — stays
     * exactly this set/order for the rest of this visit no matter what gets checked/unchecked
     * below; only re-latches the next time the screen is freshly opened. Not reorderable here —
     * that lives in this facet's own Settings page. */
    val selectedResults: List<AppInfo> = emptyList(),
    /** Every other matching, not-already-placed app, in the installed-app list's own (alphabetical) order — a member of a folder currently placed here is excluded (see [placedFolderMemberComponents]) rather than offered as a second, standalone placement. */
    val otherResults: List<AppInfo> = emptyList(),
    val favoriteComponents: Set<Pair<String, String>> = emptySet(),
    /** The full folder library — every folder, regardless of where (if anywhere) it's placed. */
    val folders: List<Folder> = emptyList(),
    /** Which of [folders] are currently placed in *this* Favorites list — the Folders tab's own checkbox state. */
    val placedFolderIds: Set<Long> = emptySet(),
    val canAddMore: Boolean = true,
) {
    val placedFolderMemberComponents: Set<Pair<String, String>>
        get() = folders.filter { it.id in placedFolderIds }.flatMap { it.apps }.map { it.packageName to it.activityName }.toSet()
}

/**
 * Favorites picker (`4j`) — also doubles as the "Default favorites" picker reached from Settings.
 * No `facetId` (or [NO_ACTIVE_FACET_ID]) means the launcher-wide default list
 * ([DefaultFavoriteAppRepository]) rather than one facet's own ([FavoriteAppRepository]).
 */
@HiltViewModel
class FavoritesPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val favoriteAppRepository: FavoriteAppRepository,
    private val defaultFavoriteAppRepository: DefaultFavoriteAppRepository,
    private val folderRepository: FolderRepository,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val maxFavorites: Int
        get() = if (facetId != null) FavoriteAppRepository.MAX_FAVORITES else DefaultFavoriteAppRepository.MAX_FAVORITES

    private fun observeItems(): Flow<List<PlacedItem>> =
        facetId?.let { favoriteAppRepository.observeFavoriteItems(it) } ?: defaultFavoriteAppRepository.observeDefaultItems()

    /** Latched from the favorites' live order the first time it's observed; never updated after — see [FavoritesPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<FavoritesPickerUiState> = combine(
        query,
        installedApps,
        observeItems(),
        loadOrder,
        folderRepository.observeFolders(),
    ) { query, installed, items, frozenOrder, folders ->
        val favorites = items.filterIsInstance<PlacedItem.SingleApp>().map { it.app }
        val liveOrder = favorites.map { it.packageName to it.activityName }
        if (frozenOrder == null) loadOrder.value = liveOrder
        val order = frozenOrder ?: liveOrder
        val placedFolderIds = items.filterIsInstance<PlacedItem.FolderItem>().map { it.folder.id }.toSet()
        val placedFolderMemberComponents = folders.filter { it.id in placedFolderIds }
            .flatMap { it.apps }.map { it.packageName to it.activityName }.toSet()

        val filtered = installed.filter { it.label.contains(query, ignoreCase = true) }
        val (selected, otherCandidates) = filtered.partition { (it.packageName to it.activityName) in order }
        // A member of a folder currently placed here is already occupying a Favorites slot
        // (inside that folder) — offering it again as a standalone checkbox would let it get
        // added a second time, rendering twice.
        val other = otherCandidates.filterNot { (it.packageName to it.activityName) in placedFolderMemberComponents }
        // Item-counted, matching every other capacity check — a folder costs exactly one slot
        // regardless of how many apps it holds.
        val itemCount = favorites.size + placedFolderIds.size
        FavoritesPickerUiState(
            isFacetScoped = facetId != null,
            query = query,
            selectedResults = selected.sortedBy { order.indexOf(it.packageName to it.activityName) },
            otherResults = other,
            favoriteComponents = liveOrder.toSet(),
            folders = folders,
            placedFolderIds = placedFolderIds,
            canAddMore = itemCount < maxFavorites,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
    }

    fun toggleFavorite(app: AppInfo) {
        val state = uiState.value
        val isFavorite = (app.packageName to app.activityName) in state.favoriteComponents
        viewModelScope.launch {
            if (facetId != null) {
                if (isFavorite) {
                    favoriteAppRepository.removeFavorite(facetId, app)
                } else if (state.canAddMore) {
                    favoriteAppRepository.addFavorite(facetId, app, position = state.favoriteComponents.size)
                }
            } else {
                if (isFavorite) {
                    defaultFavoriteAppRepository.removeFavorite(app)
                } else if (state.canAddMore) {
                    defaultFavoriteAppRepository.addFavorite(app, position = state.favoriteComponents.size)
                }
            }
        }
    }

    /** The Folders tab's own checkbox — un-placing here never deletes the folder, only removes it from this specific Favorites list. */
    fun toggleFolder(folder: Folder) {
        val state = uiState.value
        val isPlaced = folder.id in state.placedFolderIds
        viewModelScope.launch {
            if (facetId != null) {
                if (isPlaced) {
                    favoriteAppRepository.removeFolderPlacement(facetId, folder.id)
                } else if (state.canAddMore) {
                    favoriteAppRepository.placeFolder(facetId, folder.id, position = state.favoriteComponents.size + state.placedFolderIds.size)
                }
            } else {
                if (isPlaced) {
                    defaultFavoriteAppRepository.removeFolderPlacement(folder.id)
                } else if (state.canAddMore) {
                    defaultFavoriteAppRepository.placeFolder(folder.id, position = state.favoriteComponents.size + state.placedFolderIds.size)
                }
            }
        }
    }
}
