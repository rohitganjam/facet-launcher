package com.facetlauncher.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.ui.components.AppPickerScreen

/**
 * A folder's own "Add to folder" picker, reached from [FolderDetailScreen]'s header — thin
 * wrapper around the shared [AppPickerScreen], same idiom as
 * [com.facetlauncher.app.ui.dock.DockAppPickerScreen]'s own Apps tab, but apps-only: a folder
 * can't contain another folder, so `showFoldersTab` is always `false` here. No membership cap
 * either.
 */
@Composable
fun FolderAppPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FolderAppPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AppPickerScreen(
        title = uiState.folderName,
        query = uiState.query,
        selectedResults = uiState.selectedResults,
        otherResults = uiState.otherResults,
        selectedComponents = uiState.folderAppComponents,
        canAddMore = true,
        canRemove = true,
        sortOption = uiState.sortOption,
        sortDirection = uiState.sortDirection,
        selectedSectionLabel = stringResource(R.string.folder_app_picker_in_folder),
        screenTestTag = "folder_app_picker_screen",
        tagPrefix = "folder_app_picker",
        onQueryChange = viewModel::onQueryChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onSortDirectionToggle = viewModel::onSortDirectionToggle,
        onToggleApp = viewModel::toggleApp,
        onDone = onDone,
        modifier = modifier,
        showFoldersTab = false,
    )
}
