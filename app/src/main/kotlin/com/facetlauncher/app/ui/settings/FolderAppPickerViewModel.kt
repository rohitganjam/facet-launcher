package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.data.model.SortDirection
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import com.facetlauncher.app.domain.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderAppPickerUiState(
    val folderName: String = "",
    val query: String = "",
    /** Apps already in this folder at the moment this screen was opened, in their own order — stays
     * exactly this set/order for the rest of this visit no matter what gets checked/unchecked below;
     * only re-latches the next time the screen is freshly opened. Not reorderable here — that lives
     * on [FolderDetailScreen] itself. */
    val selectedResults: List<AppInfo> = emptyList(),
    /** Every other matching app, sorted by [sortOption]/[sortDirection]. */
    val otherResults: List<AppInfo> = emptyList(),
    val folderAppComponents: Set<Pair<String, String>> = emptySet(),
    val sortOption: AppSortOption = AppSortOption.ALPHABETICAL,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
)

/**
 * A folder's own "Add to folder" picker — apps only, no Folders tab (a folder can't contain
 * another folder), unlike the Dock/Favorites pickers. No membership cap either — a folder has no
 * equivalent of [com.facetlauncher.app.data.DockAppRepository.MAX_APPS].
 */
@HiltViewModel
class FolderAppPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val folderRepository: FolderRepository,
    private val sortAppsForPicker: SortAppsForPickerUseCase,
    private val usageAccessRepository: UsageAccessRepository,
) : ViewModel() {

    private val folderId: Long = checkNotNull(savedStateHandle["folderId"])
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val sortOption = MutableStateFlow(AppSortOption.ALPHABETICAL)
    private val sortDirection = MutableStateFlow(SortDirection.ASCENDING)

    private val _usageAccessGranted = MutableStateFlow(usageAccessRepository.isGranted())
    /** `PACKAGE_USAGE_STATS` has no grant-change callback — re-checked via [refreshUsageAccessGranted] on this screen's resume. */
    val usageAccessGranted: StateFlow<Boolean> = _usageAccessGranted.asStateFlow()

    /** Latched from the folder's live membership order the first time it's observed; never updated after — see [FolderAppPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<FolderAppPickerUiState> = combine(
        query,
        installedApps,
        folderRepository.observeFolders(),
        loadOrder,
        sortOption,
        sortDirection,
    ) { query, installed, folders, frozenOrder, sortOption, sortDirection ->
        val folder = folders.find { it.id == folderId }
        val liveOrder = folder?.apps.orEmpty().map { it.packageName to it.activityName }
        if (frozenOrder == null) loadOrder.value = liveOrder
        val order = frozenOrder ?: liveOrder

        val filtered = installed.filter { it.label.contains(query, ignoreCase = true) }
        val (selected, other) = filtered.partition { (it.packageName to it.activityName) in order }
        FolderAppPickerUiState(
            folderName = folder?.name.orEmpty(),
            query = query,
            selectedResults = selected.sortedBy { order.indexOf(it.packageName to it.activityName) },
            otherResults = sortAppsForPicker(other, sortOption, sortDirection),
            folderAppComponents = liveOrder.toSet(),
            sortOption = sortOption,
            sortDirection = sortDirection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderAppPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /** No-ops for [AppSortOption.LAST_USED] without Usage Access — the calling screen is expected
     * to route to the permission explanation instead of calling this in that case, but this stays
     * the actual source of truth so [FolderAppPickerUiState.sortOption] never silently ends up at
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

    fun toggleApp(app: AppInfo) {
        val isInFolder = (app.packageName to app.activityName) in uiState.value.folderAppComponents
        viewModelScope.launch {
            if (isInFolder) {
                folderRepository.removeAppFromFolder(folderId, app)
            } else {
                folderRepository.addAppToFolder(folderId, app)
            }
        }
    }
}
