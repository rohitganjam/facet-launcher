package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.CopyToFacetsCard
import com.facetlauncher.app.ui.components.DefaultsNote
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.ResetToDefaultCard
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.SettingsToggleRow
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlin.enums.EnumEntries

/**
 * Dock & Apps appearance — how the Dock and the Home apps list look and sit (dock style, list layout,
 * position, presentation, vertical alignment). Serves both scopes: with a `facetId` it edits that facet's own look and offers
 * Reset to default; without one it edits the defaults a new facet starts with. Shares [AppearanceSettingsViewModel] with [AppearanceSettingsScreen].
 */
@Composable
fun DockAppsAppearanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    DockAppsAppearanceContent(
        uiState = uiState,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onDockDisplayModeChange = viewModel::setDockDisplayMode,
        onAppRowPositionChange = viewModel::setAppRowPosition,
        onAppRowPresentationChange = viewModel::setAppRowPresentation,
        onAppListVerticalAlignmentChange = viewModel::setAppListVerticalAlignment,
        onAllowLandscapeChange = viewModel::setAllowLandscape,
        onLandscapeAppRowPositionChange = viewModel::setLandscapeAppRowPosition,
        onLandscapeAppListVerticalAlignmentChange = viewModel::setLandscapeAppListVerticalAlignment,
        onLandscapeAppListColumnAlignmentChange = viewModel::setLandscapeAppListColumnAlignment,
        onAppListLayoutChange = viewModel::setAppListLayout,
        onAppListColumnAlignmentChange = viewModel::setAppListColumnAlignment,
        onAppListGridColumnsChange = viewModel::setAppListGridColumns,
        onAppListGridDisplayModeChange = viewModel::setAppListGridDisplayMode,
        onAppListShrinkToFitChange = viewModel::setAppListShrinkToFit,
        onResetToDefault = viewModel::resetLookToDefault,
        onCopyToFacets = viewModel::copyLookToFacets,
        modifier = modifier,
    )
}

@Composable
private fun DockAppsAppearanceContent(
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onDockDisplayModeChange: (DockDisplayMode) -> Unit,
    onAppRowPositionChange: (AppRowPosition) -> Unit,
    onAppRowPresentationChange: (AppRowPresentation) -> Unit,
    onAppListVerticalAlignmentChange: (AppListVerticalAlignment) -> Unit,
    onAllowLandscapeChange: (Boolean) -> Unit,
    onLandscapeAppRowPositionChange: (AppRowPosition?) -> Unit,
    onLandscapeAppListVerticalAlignmentChange: (AppListVerticalAlignment?) -> Unit,
    onLandscapeAppListColumnAlignmentChange: (AppListColumnAlignment?) -> Unit,
    onAppListLayoutChange: (AppListLayout) -> Unit,
    onAppListColumnAlignmentChange: (AppListColumnAlignment) -> Unit,
    onAppListGridColumnsChange: (AppListGridColumns) -> Unit,
    onAppListGridDisplayModeChange: (AppListGridDisplayMode) -> Unit,
    onResetToDefault: () -> Unit,
    onCopyToFacets: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    onAppListShrinkToFitChange: (AppListShrinkToFit) -> Unit = {},
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(title = stringResource(R.string.appearance_dock_apps_title), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("dock_apps_appearance_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    AppearancePreviewCard(
                        uiState = uiState,
                        homeWallpaper = homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                // Launcher-wide, so only on the global screen. Off hides the landscape alignment below.
                if (!uiState.isFacetScoped) allowLandscapeItem(uiState.allowLandscape, onAllowLandscapeChange)
                item {
                    SharedLookCard(
                        uiState = uiState,
                        onDockDisplayModeChange = onDockDisplayModeChange,
                        onAppListLayoutChange = onAppListLayoutChange,
                        onAppRowPresentationChange = onAppRowPresentationChange,
                        onAppListGridColumnsChange = onAppListGridColumnsChange,
                        onAppListGridDisplayModeChange = onAppListGridDisplayModeChange,
                        onAppListShrinkToFitChange = onAppListShrinkToFitChange,
                    )
                }
                item { SettingsSectionHeader(stringResource(R.string.appearance_alignment_portrait_title)) }
                item {
                    AlignmentCard(
                        uiState = uiState,
                        landscape = false,
                        onRowPositionChange = { it?.let(onAppRowPositionChange) },
                        onColumnAlignmentChange = { it?.let(onAppListColumnAlignmentChange) },
                        onVerticalAlignmentChange = { it?.let(onAppListVerticalAlignmentChange) },
                    )
                }
                if (uiState.allowLandscape) {
                    landscapeAlignmentItems(
                        uiState,
                        onLandscapeAppRowPositionChange,
                        onLandscapeAppListColumnAlignmentChange,
                        onLandscapeAppListVerticalAlignmentChange,
                    )
                }
                resetOrDefaultsNoteItems(uiState, onResetToDefault, onCopyToFacets)
            }
        },
    )
}

/** A facet's Reset to default row, or at launcher scope Copy to facets and the note that these values only seed new facets. */
private fun LazyListScope.resetOrDefaultsNoteItems(
    uiState: AppearanceSettingsUiState,
    onResetToDefault: () -> Unit,
    onCopyToFacets: (Set<Long>) -> Unit,
) {
    if (uiState.isFacetScoped) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            ResetToDefaultCard(
                subtitle = stringResource(R.string.reset_look_subtitle),
                confirmMessage = stringResource(R.string.reset_look_confirm_message),
                onReset = onResetToDefault,
                testTagPrefix = "dock_apps_appearance",
            )
        }
    } else {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        item {
            CopyToFacetsCard(
                facets = uiState.facets,
                activeFacetId = uiState.settings.activeFacetId,
                subtitle = stringResource(R.string.copy_look_subtitle),
                sheetMessage = stringResource(R.string.copy_look_sheet_message),
                onCopy = onCopyToFacets,
                testTagPrefix = "dock_apps_appearance",
            )
        }
        item { DefaultsNote(modifier = Modifier.padding(top = 12.dp)) }
    }
}

private fun LazyListScope.allowLandscapeItem(allowLandscape: Boolean, onAllowLandscapeChange: (Boolean) -> Unit) {
    item {
        SettingsCard(modifier = Modifier.padding(bottom = 16.dp)) {
            SettingsToggleRow(
                title = stringResource(R.string.appearance_allow_landscape_title),
                subtitle = stringResource(R.string.appearance_allow_landscape_subtitle),
                checked = allowLandscape,
                onCheckedChange = onAllowLandscapeChange,
                testTag = "appearance_allow_landscape_row",
            )
        }
    }
}

private fun LazyListScope.landscapeAlignmentItems(
    uiState: AppearanceSettingsUiState,
    onRowPositionChange: (AppRowPosition?) -> Unit,
    onColumnAlignmentChange: (AppListColumnAlignment?) -> Unit,
    onVerticalAlignmentChange: (AppListVerticalAlignment?) -> Unit,
) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_alignment_landscape_title)) }
    item {
        AlignmentCard(
            uiState = uiState,
            landscape = true,
            onRowPositionChange = onRowPositionChange,
            onColumnAlignmentChange = onColumnAlignmentChange,
            onVerticalAlignmentChange = onVerticalAlignmentChange,
        )
    }
}

/** The look shared by both orientations: dock style, list layout, and the presentation or grid rows that layout has. */
@Composable
private fun SharedLookCard(
    uiState: AppearanceSettingsUiState,
    onDockDisplayModeChange: (DockDisplayMode) -> Unit,
    onAppListLayoutChange: (AppListLayout) -> Unit,
    onAppRowPresentationChange: (AppRowPresentation) -> Unit,
    onAppListGridColumnsChange: (AppListGridColumns) -> Unit,
    onAppListGridDisplayModeChange: (AppListGridDisplayMode) -> Unit,
    onAppListShrinkToFitChange: (AppListShrinkToFit) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        LabeledDropdownRow(
            title = stringResource(R.string.dock_display_style),
            options = DockDisplayMode.entries,
            selected = uiState.dockDisplayMode,
            label = { stringResource(it.displayNameRes) },
            onSelect = onDockDisplayModeChange,
            testTag = "appearance_dock_display_style_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.home_apps_list_layout),
            options = AppListLayout.entries,
            selected = uiState.appListLayout,
            label = { stringResource(it.displayNameRes) },
            onSelect = onAppListLayoutChange,
            testTag = "appearance_app_list_layout_row",
        )
        // Grid has no per-item alignment, so its own display mode replaces the icon/text presentation row.
        if (uiState.appListLayout == AppListLayout.GRID) {
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.home_apps_list_grid_columns),
                options = AppListGridColumns.entries,
                selected = uiState.appListGridColumns,
                label = { stringResource(it.displayNameRes) },
                onSelect = onAppListGridColumnsChange,
                testTag = "appearance_app_list_grid_columns_row",
            )
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.home_apps_list_grid_display),
                options = AppListGridDisplayMode.entries,
                selected = uiState.appListGridDisplayMode,
                label = { stringResource(it.displayNameRes) },
                onSelect = onAppListGridDisplayModeChange,
                testTag = "appearance_app_list_grid_display_mode_row",
            )
        } else {
            CardDivider()
            LabeledDropdownRow(
                title = stringResource(R.string.home_apps_list_presentation),
                options = AppRowPresentation.entries,
                selected = uiState.appRowPresentation,
                label = { stringResource(it.displayNameRes) },
                onSelect = onAppRowPresentationChange,
                testTag = "appearance_app_row_presentation_row",
            )
            CardDivider()
            ShrinkToFitRow(uiState, onAppListShrinkToFitChange)
        }
    }
}

/** "Shrink list to fit", for the list layouts: Compact rows when Regular ones wouldn't fit, else the list scrolls. */
@Composable
private fun ShrinkToFitRow(uiState: AppearanceSettingsUiState, onChange: (AppListShrinkToFit) -> Unit) {
    LabeledDropdownRow(
        title = stringResource(R.string.home_apps_list_shrink_to_fit),
        options = AppListShrinkToFit.entries,
        selected = uiState.appListShrinkToFit,
        label = { stringResource(it.displayNameRes) },
        onSelect = onChange,
        testTag = "appearance_app_list_shrink_to_fit_row",
    )
}

/**
 * The apps list's alignment in one orientation: the layout's horizontal row (none for a grid), then the list
 * position. Landscape's rows add a "Same as portrait" choice, stored as `null`.
 */
@Composable
private fun AlignmentCard(
    uiState: AppearanceSettingsUiState,
    landscape: Boolean,
    onRowPositionChange: (AppRowPosition?) -> Unit,
    onColumnAlignmentChange: (AppListColumnAlignment?) -> Unit,
    onVerticalAlignmentChange: (AppListVerticalAlignment?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tags = if (landscape) "appearance_landscape_" else "appearance_"
    val own = uiState.landscape
    SettingsCard(modifier = modifier) {
        when (uiState.appListLayout) {
            AppListLayout.TWO_COLUMN -> {
                AlignmentRow(
                    title = stringResource(R.string.home_apps_list_column_alignment),
                    entries = AppListColumnAlignment.entries,
                    selected = if (landscape) own.appListColumnAlignment else uiState.appListColumnAlignment,
                    landscape = landscape,
                    nameRes = { it.displayNameRes },
                    onSelect = onColumnAlignmentChange,
                    testTag = "${tags}app_list_column_alignment_row",
                )
                CardDivider()
            }
            AppListLayout.GRID -> Unit
            AppListLayout.SINGLE_COLUMN -> {
                AlignmentRow(
                    title = stringResource(R.string.home_apps_list_position),
                    entries = AppRowPosition.entries,
                    selected = if (landscape) own.appRowPosition else uiState.appRowPosition,
                    landscape = landscape,
                    nameRes = { it.displayNameRes },
                    onSelect = onRowPositionChange,
                    testTag = "${tags}app_row_position_row",
                )
                CardDivider()
            }
        }
        AlignmentRow(
            title = stringResource(R.string.home_apps_list_list_position),
            entries = AppListVerticalAlignment.entries,
            selected = if (landscape) own.appListVerticalAlignment else uiState.appListVerticalAlignment,
            landscape = landscape,
            nameRes = { it.displayNameRes },
            onSelect = onVerticalAlignmentChange,
            testTag = "${tags}app_list_vertical_alignment_row",
        )
    }
}

/** One dropdown of an alignment enum; landscape adds a "Same as portrait" choice, stored as `null`. */
@Composable
private fun <T : Enum<T>> AlignmentRow(
    title: String,
    entries: EnumEntries<T>,
    selected: T?,
    landscape: Boolean,
    nameRes: (T) -> Int,
    onSelect: (T?) -> Unit,
    testTag: String,
) {
    val samePortrait = stringResource(R.string.home_apps_list_same_as_portrait)
    val options: List<T?> = if (landscape) listOf(null) + entries else entries
    LabeledDropdownRow(
        title = title,
        options = options,
        selected = selected,
        label = { option -> option?.let { stringResource(nameRes(it)) } ?: samePortrait },
        // Portrait always has a value, so a null can only come from landscape's "Same as portrait".
        onSelect = { choice -> if (landscape || choice != null) onSelect(choice) },
        testTag = testTag,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DockAppsAppearanceScreenPreview() {
    FacetLauncherTheme {
        DockAppsAppearanceContent(
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onDockDisplayModeChange = {},
            onAppRowPositionChange = {},
            onAppRowPresentationChange = {},
            onAppListVerticalAlignmentChange = {},
            onAllowLandscapeChange = {},
            onLandscapeAppRowPositionChange = {},
            onLandscapeAppListVerticalAlignmentChange = {},
            onLandscapeAppListColumnAlignmentChange = {},
            onAppListLayoutChange = {},
            onAppListColumnAlignmentChange = {},
            onAppListGridColumnsChange = {},
            onAppListGridDisplayModeChange = {},
            onResetToDefault = {},
            onCopyToFacets = {},
        )
    }
}
