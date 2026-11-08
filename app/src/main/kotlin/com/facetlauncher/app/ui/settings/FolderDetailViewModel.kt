package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.PlacementList
import com.facetlauncher.app.domain.AddToActiveListUseCase
import com.facetlauncher.app.domain.FolderPlacementState
import com.facetlauncher.app.domain.ObserveFolderPlacementUseCase
import com.facetlauncher.app.domain.QuickPlacementAction
import com.facetlauncher.app.domain.RemoveFromActiveListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderDetailUiState(val folder: Folder? = null, val placement: FolderPlacementState = FolderPlacementState())

/**
 * Settings → Folders → one folder's own contents — a real, dedicated screen (not the shared
 * [com.facetlauncher.app.ui.components.FolderContentsSheet] bottom sheet used elsewhere) so its
 * apps can be drag-reordered the same way Dock/Favorites are on their own settings screens.
 * Membership itself is edited via [FolderAppPickerViewModel], reached through this screen's own
 * "Add to folder" header action — this screen only reorders/renames/observes.
 */
@HiltViewModel
class FolderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    observeFolderPlacement: ObserveFolderPlacementUseCase,
    private val addToActiveList: AddToActiveListUseCase,
    private val removeFromActiveList: RemoveFromActiveListUseCase,
) : ViewModel() {

    val folderId: Long = checkNotNull(savedStateHandle["folderId"])

    val uiState: StateFlow<FolderDetailUiState> = combine(folderRepository.observeFolders(), observeFolderPlacement(folderId)) { folders, placement ->
        FolderDetailUiState(folder = folders.find { it.id == folderId }, placement = placement)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderDetailUiState())

    fun renameFolder(name: String) {
        viewModelScope.launch { folderRepository.renameFolder(folderId, name) }
    }

    /** The menu's Add to / Remove from Favorites or Dock, in the active facet's list. */
    fun applyPlacement(list: PlacementList, action: QuickPlacementAction) {
        val folder = uiState.value.folder ?: return
        viewModelScope.launch {
            when (action) {
                is QuickPlacementAction.Add -> addToActiveList(list, PlacedItem.FolderItem(folder))
                is QuickPlacementAction.Remove -> removeFromActiveList(list, PlacedItem.FolderItem(folder))
                is QuickPlacementAction.Full -> Unit
            }
        }
    }

    /** Cascades through membership and every Dock/Favorites placement; the screen leaves once the folder is gone. */
    fun deleteFolder() {
        viewModelScope.launch { folderRepository.deleteFolder(folderId) }
    }

    fun reorderApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { folderRepository.reorderFolderApps(folderId, orderedApps) }
    }

    fun removeApp(app: AppInfo) {
        viewModelScope.launch { folderRepository.removeAppFromFolder(folderId, app) }
    }
}
