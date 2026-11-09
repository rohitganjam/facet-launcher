package com.facetlauncher.app.ui.facets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import androidx.compose.ui.res.stringResource
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
    // The facet to edit when this isn't opened through a facet's route (onboarding edits the first facet's lists).
    facetId: Long? = null,
    viewModel: FavoritesPickerViewModel = hiltViewModel(),
) {
    LaunchedEffect(facetId) { facetId?.let(viewModel::useFacet) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val usageAccessGranted by viewModel.usageAccessGranted.collectAsStateWithLifecycle()
    AppPickerScreen(
        title = stringResource(R.string.home_apps_list_favorites_facet),
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
        onQueryChange = viewModel::onQueryChange,
        onSortOptionChange = { option ->
            if (option == AppSortOption.LAST_USED && !usageAccessGranted) {
                onNavigateToUsageAccessExplanation()
            } else {
                viewModel.onSortOptionChange(option)
            }
        },
        onSortDirectionToggle = viewModel::onSortDirectionToggle,
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
