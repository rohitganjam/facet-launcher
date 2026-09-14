package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    /** Every other matching app, in the installed-app list's own (alphabetical) order. */
    val otherResults: List<AppInfo> = emptyList(),
    val folderAppComponents: Set<Pair<String, String>> = emptySet(),
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
) : ViewModel() {

    private val folderId: Long = checkNotNull(savedStateHandle["folderId"])
    private val query = MutableStateFlow("")
    private val installedApps = MutableStateFlow<List<AppInfo>>(emptyList())

    /** Latched from the folder's live membership order the first time it's observed; never updated after — see [FolderAppPickerUiState.selectedResults]. */
    private val loadOrder = MutableStateFlow<List<Pair<String, String>>?>(null)

    val uiState: StateFlow<FolderAppPickerUiState> = combine(
        query,
        installedApps,
        folderRepository.observeFolders(),
        loadOrder,
    ) { query, installed, folders, frozenOrder ->
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
            otherResults = other,
            folderAppComponents = liveOrder.toSet(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderAppPickerUiState())

    init {
        viewModelScope.launch { installedApps.value = getInstalledApps() }
    }

    fun onQueryChanged(newQuery: String) {
        query.value = newQuery
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
