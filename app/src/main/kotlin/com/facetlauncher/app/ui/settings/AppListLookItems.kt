package com.facetlauncher.app.ui.settings

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppListColumnAlignment
import com.facetlauncher.app.data.model.AppListGridColumns
import com.facetlauncher.app.data.model.AppListGridDisplayMode
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListShrinkToFit
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.SettingsToggleRow
import kotlin.enums.EnumEntries

/** An "Allow landscape mode" switch: launcher-wide, so only at launcher scope; off hides the landscape alignment card. */
internal fun LazyListScope.appListLandscapeItem(uiState: HomeAppsListUiState, actions: AppListLookActions) {
    if (!uiState.isFacetScoped) allowLandscapeItem(uiState.allowLandscape, actions.onAllowLandscapeChange)
}

/** Where the Home apps list sits: portrait and landscape alignment. */
internal fun LazyListScope.appListAlignmentItems(uiState: HomeAppsListUiState, actions: AppListLookActions) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_alignment_portrait_title)) }
    item {
        AlignmentCard(
            uiState = uiState,
            landscape = false,
            onRowPositionChange = { it?.let(actions.onRowPositionChange) },
            onColumnAlignmentChange = { it?.let(actions.onColumnAlignmentChange) },
            onVerticalAlignmentChange = { it?.let(actions.onVerticalAlignmentChange) },
        )
    }
    if (uiState.allowLandscape) {
        item { SettingsSectionHeader(stringResource(R.string.appearance_alignment_landscape_title)) }
        item {
            AlignmentCard(
                uiState = uiState,
                landscape = true,
                onRowPositionChange = actions.onLandscapeRowPositionChange,
                onColumnAlignmentChange = actions.onLandscapeColumnAlignmentChange,
                onVerticalAlignmentChange = actions.onLandscapeVerticalAlignmentChange,
            )
        }
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

/** The look shared by both orientations: list layout, and the presentation or grid rows that layout has. Rows only, for a card's content. */
@Composable
internal fun ColumnScope.AppListLayoutRows(uiState: HomeAppsListUiState, actions: AppListLookActions) {
    LabeledDropdownRow(
        title = stringResource(R.string.home_apps_list_layout),
        options = AppListLayout.entries,
        selected = uiState.appListLayout,
        label = { stringResource(it.displayNameRes) },
        onSelect = actions.onLayoutChange,
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
            onSelect = actions.onGridColumnsChange,
            testTag = "appearance_app_list_grid_columns_row",
        )
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.home_apps_list_grid_display),
            options = AppListGridDisplayMode.entries,
            selected = uiState.appListGridDisplayMode,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onGridDisplayModeChange,
            testTag = "appearance_app_list_grid_display_mode_row",
        )
    } else {
        CardDivider()
        LabeledDropdownRow(
            title = stringResource(R.string.home_apps_list_presentation),
            options = AppRowPresentation.entries,
            selected = uiState.appRowPresentation,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onPresentationChange,
            testTag = "appearance_app_row_presentation_row",
        )
        CardDivider()
        // "Shrink list to fit": Compact rows when Regular ones wouldn't fit, else the list scrolls.
        LabeledDropdownRow(
            title = stringResource(R.string.home_apps_list_shrink_to_fit),
            options = AppListShrinkToFit.entries,
            selected = uiState.appListShrinkToFit,
            label = { stringResource(it.displayNameRes) },
            onSelect = actions.onShrinkToFitChange,
            testTag = "appearance_app_list_shrink_to_fit_row",
        )
    }
}

/**
 * The apps list's alignment in one orientation: the layout's horizontal row (none for a grid), then the list
 * position. Landscape's rows add a "Same as portrait" choice, stored as `null`.
 */
@Composable
private fun AlignmentCard(
    uiState: HomeAppsListUiState,
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
