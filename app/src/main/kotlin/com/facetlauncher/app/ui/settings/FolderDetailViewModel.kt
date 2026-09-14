package com.facetlauncher.app.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderDetailUiState(val folder: Folder? = null)

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
) : ViewModel() {

    val folderId: Long = checkNotNull(savedStateHandle["folderId"])

    val uiState: StateFlow<FolderDetailUiState> = folderRepository.observeFolders()
        .map { folders -> FolderDetailUiState(folder = folders.find { it.id == folderId }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderDetailUiState())

    fun renameFolder(name: String) {
        viewModelScope.launch { folderRepository.renameFolder(folderId, name) }
    }

    fun reorderApps(orderedApps: List<AppInfo>) {
        viewModelScope.launch { folderRepository.reorderFolderApps(folderId, orderedApps) }
    }

    fun removeApp(app: AppInfo) {
        viewModelScope.launch { folderRepository.removeAppFromFolder(folderId, app) }
    }
}
