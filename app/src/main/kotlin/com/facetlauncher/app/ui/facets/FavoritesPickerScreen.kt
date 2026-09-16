package com.facetlauncher.app.ui.facets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.AppSortOption
import com.facetlauncher.app.ui.components.AppPickerScreen

/**
 * Favorites picker (`4j`): also doubles as the "Default favorites" picker reached from Settings —
 * see [FavoritesPickerViewModel]. Thin wrapper around the shared [AppPickerScreen] (search, sort
 * control, checkbox rows, optional Apps/Folders tab) — this file only supplies Favorites' own
 * title, capacity rule, and repository calls via [viewModel]. Picking "Last used" sort while Usage
 * Access isn't granted routes to [onNavigateToUsageAccessExplanation] instead of applying it —
 * [FavoritesPickerUiState.usageAccessGranted] is re-checked on this screen's resume (via
 * [AppPickerScreen]'s own `onResume`), same as [com.facetlauncher.app.ui.settings.UsageAccessExplanationViewModel].
 */
@Composable
fun FavoritesPickerScreen(
    onDone: () -> Unit,
    onNavigateToUsageAccessExplanation: () -> Unit,
    modifier: Modifier = Modifier,
    // False during onboarding — no folder can exist yet that early (they're only ever created
    // via the Drawer/Search's own "Add to folder" long-press flow, which isn't reachable from
    // onboarding), so the tab would only ever show an empty, dead-end Folders page.
    showFoldersTab: Boolean = true,
    viewModel: FavoritesPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val usageAccessGranted by viewModel.usageAccessGranted.collectAsStateWithLifecycle()
    AppPickerScreen(
        title = if (uiState.isFacetScoped) "Favorites" else "Default favorites",
        query = uiState.query,
        selectedResults = uiState.selectedResults,
        otherResults = uiState.otherResults,
        selectedComponents = uiState.favoriteComponents,
        canAddMore = uiState.canAddMore,
        canRemove = true,
        sortOption = uiState.sortOption,
        sortDirection = uiState.sortDirection,
        selectedSectionLabel = "FAVORITES",
        screenTestTag = "favorites_picker_screen",
        tagPrefix = "favorites_picker",
        onQueryChanged = viewModel::onQueryChanged,
        onSortOptionChanged = { option ->
            if (option == AppSortOption.LAST_USED && !usageAccessGranted) {
                onNavigateToUsageAccessExplanation()
            } else {
                viewModel.onSortOptionChanged(option)
            }
        },
        onSortDirectionToggled = viewModel::onSortDirectionToggled,
        onToggleApp = viewModel::toggleFavorite,
        onDone = onDone,
        modifier = modifier,
        folders = uiState.folders,
        placedFolderIds = uiState.placedFolderIds,
        showFoldersTab = showFoldersTab,
        onToggleFolder = viewModel::toggleFolder,
        onResume = viewModel::refreshUsageAccessGranted,
    )
}
