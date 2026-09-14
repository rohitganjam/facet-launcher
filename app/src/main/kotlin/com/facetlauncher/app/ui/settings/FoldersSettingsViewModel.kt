package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.model.Folder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FoldersSettingsUiState(val folders: List<Folder> = emptyList())

/**
 * Settings' own "Folders" management screen — the whole folder library, sourced directly from
 * [FolderRepository] rather than any one Dock/Favorites list, since a folder is a first-class,
 * independent entity. The only place a folder is actually deleted; un-placing it from a specific
 * Dock/Favorites list happens elsewhere, via that list's own picker checkbox. A folder's own
 * contents (rename, membership, reorder) live on [FolderDetailViewModel], reached by tapping a row.
 */
@HiltViewModel
class FoldersSettingsViewModel @Inject constructor(
    private val folderRepository: FolderRepository,
) : ViewModel() {

    val uiState: StateFlow<FoldersSettingsUiState> = folderRepository.observeFolders()
        .map { folders -> FoldersSettingsUiState(folders = folders) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FoldersSettingsUiState())

    /** The header's own "+ Folder" button — creates a genuinely empty folder directly, with no app required (unlike the Drawer/Dock's own "Add to folder" flow, which always names a folder alongside adding its first member). */
    fun createFolder(name: String) {
        viewModelScope.launch { folderRepository.createFolder(name) }
    }

    /** Cascades through membership and every Dock/Favorites placement — see [FolderRepository.deleteFolder]. */
    fun deleteFolder(folderId: Long) {
        viewModelScope.launch { folderRepository.deleteFolder(folderId) }
    }
}
