package com.facetlauncher.app.ui.dock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.NO_ACTIVE_FACET_ID
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.ObservePlacedItemsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.domain.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.facetlauncher.app.data.model.AppListLimits
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.PlacementTarget
import com.facetlauncher.app.data.model.PlacementList

data class DockAppPickerUiState(
    /** `false` when this picker is editing the launcher-wide default dock (reached from Settings) rather than one facet's own. */
    val isFacetScoped: Boolean = false,
    val query: String = "",
    /** In-dock apps at the moment this screen was opened, in their dock order — stays exactly
     * this set/order for the rest of this visit no matter what gets checked/unchecked below;
     * only re-latches the next time the screen is freshly opened. Not reorderable here — that
     * lives in Settings' own Dock card. */
    val selectedResults: List<AppInfo> = emptyList(),
    /** Every other matching, not-already-placed app, sorted by [sortOption]/[sortDirection] — a member of a folder currently placed here is excluded (see [placedFolderMemberComponents]) rather than offered as a second, standalone placement. */
    val otherResults: List<AppInfo> = emptyList(),
    val dockPackageComponents: Set<String> = emptySet(),
    /** The full folder library — every folder, regardless of where (if anywhere) it's placed. */
    val folders: List<Folder> = emptyList(),
    /** Which of [folders] are currently placed in *this* Dock — the Folders tab's own checkbox state. */
    val placedFolderIds: Set<Long> = emptySet(),
    val canAddMore: Boolean = true,
    val canRemove: Boolean = true,
    val sortOption: AppSortOption = AppSortOption.ALPHABETICAL,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
) {
    val placedFolderMemberComponents: Set<String>
        get() = folders.filter { it.id in placedFolderIds }.flatMap { it.apps }.map { it.listKey }.toSet()
}

/**
 * Dock app picker (`4k`) — also doubles as one facet's own dock picker (reached from that
 * facet's settings). No `facetId` (or [NO_ACTIVE_FACET_ID]) means the launcher-wide default
 * dock (`PlacementTarget.GlobalDock`) rather than one facet's own — the
 * same split [com.facetlauncher.app.ui.facets.FavoritesPickerViewModel] uses for favorites.
 */
@HiltViewModel
class DockAppPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val placementRepository: PlacementRepository,
    private val observePlacedItems: ObservePlacedItemsUseCase,
    private val folderRepository: FolderRepository,
    private val sortAppsForPicker: SortAppsForPickerUseCase,
    private val usageAccessRepository: UsageAccessRepository,
) : ViewModel() {

    private val facetId: Long? = savedStateHandle.get<Long>("facetId")?.takeIf { it != NO_ACTIVE_FACET_ID }
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val sortOption = MutableStateFlow(AppSortOption.ALPHABETICAL)
    private val sortDirection = MutableStateFlow(SortDirection.ASCENDING)

    private val _usageAccessGranted = MutableStateFlow(usageAccessRepository.isGranted())
    /** `PACKAGE_USAGE_STATS` has no grant-change callback — re-checked via [refreshUsageAccessGranted] on this screen's resume. */
    val usageAccessGranted: StateFlow<Boolean> = _usageAccessGranted.asStateFlow()

    private val target = PlacementTarget(PlacementList.DOCK, facetId)

    private fun observeDockItems(): Flow<List<PlacedItem>> = observePlacedItems.items(target)

    /** Latched from the dock's live order the first time it's observed; never updated after — see [DockAppPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<String>?>(null)

    val uiState: StateFlow<DockAppPickerUiState> = combine(
        query,
        installedApps,
        observeDockItems(),
        loadOrder,
        folderRepository.observeFolders(),
        sortOption,
        sortDirection,
    ) { query, installed, dockItems, frozenOrder, folders, sortOption, sortDirection ->
        val dockApps = dockItems.filterIsInstance<PlacedItem.SingleApp>().map { it.app }
        val liveOrder = dockApps.map { it.listKey }
        if (frozenOrder == null) loadOrder.value = liveOrder
        val order = frozenOrder ?: liveOrder
        val placedFolderIds = dockItems.filterIsInstance<PlacedItem.FolderItem>().map { it.folder.id }.toSet()
        val placedFolderMemberComponents = folders.filter { it.id in placedFolderIds }
            .flatMap { it.apps }.map { it.listKey }.toSet()

        val filtered = installed.filter { it.label.contains(query, ignoreCase = true) }
        val (selected, otherCandidates) = filtered.partition { it.listKey in order }
        // A member of a folder currently placed here is already occupying a Dock slot (inside
        // that folder) — offering it again as a standalone checkbox would let it get added a
        // second time, rendering twice.
        val other = otherCandidates.filterNot { it.listKey in placedFolderMemberComponents }
        // Item-counted, matching every other Dock-capacity check (ObserveQuickAddStateUseCase) —
        // a folder costs exactly one slot regardless of how many apps it holds.
        val itemCount = dockApps.size + placedFolderIds.size
        DockAppPickerUiState(
            isFacetScoped = facetId != null,
            query = query,
            selectedResults = selected.sortedBy { order.indexOf(it.listKey) },
            otherResults = sortAppsForPicker(other, sortOption, sortDirection),
            dockPackageComponents = liveOrder.toSet(),
            folders = folders,
            placedFolderIds = placedFolderIds,
            canAddMore = itemCount < AppListLimits.MAX_DOCK_APPS,
            canRemove = itemCount > AppListLimits.MIN_DOCK_APPS,
            sortOption = sortOption,
            sortDirection = sortDirection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DockAppPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /** No-ops for [AppSortOption.LAST_USED] without Usage Access — the calling screen is expected
     * to route to the permission explanation instead of calling this in that case, but this stays
     * the actual source of truth so [DockAppPickerUiState.sortOption] never silently ends up at
     * [AppSortOption.LAST_USED] without it. */
    fun onSortOptionChange(option: AppSortOption) {
        if (option == AppSortOption.LAST_USED && !usageAccessRepository.isGranted()) return
        sortOption.value = option
    }

    fun onSortDirectionToggle() {
        sortDirection.value = if (sortDirection.value == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING
    }

    fun refreshUsageAccessGranted() {
        _usageAccessGranted.value = usageAccessRepository.isGranted()
    }

    fun toggleDockApp(app: AppInfo) {
        val state = uiState.value
        val isInDock = app.listKey in state.dockPackageComponents
        viewModelScope.launch {
            if (isInDock) {
                if (state.canRemove) placementRepository.removeApp(target, app)
            } else if (state.canAddMore) {
                placementRepository.addApp(target, app, position = state.dockPackageComponents.size)
            }
        }
    }

    /** The Folders tab's own checkbox — un-placing here never deletes the folder, only removes it from this specific Dock. */
    fun toggleFolder(folder: Folder) {
        val state = uiState.value
        val isPlaced = folder.id in state.placedFolderIds
        viewModelScope.launch {
            if (isPlaced) {
                if (state.canRemove) placementRepository.removeFolderPlacement(target, folder.id)
            } else if (state.canAddMore) {
                placementRepository.placeFolder(target, folder.id, position = state.dockPackageComponents.size + state.placedFolderIds.size)
            }
        }
    }
}
