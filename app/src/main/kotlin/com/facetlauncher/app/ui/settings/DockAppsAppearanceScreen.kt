package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.facetlauncher.app.data.model.AppListLayout
import com.facetlauncher.app.data.model.AppListVerticalAlignment
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.data.model.DockDisplayMode
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings → Appearance → Dock & Apps appearance — how the Dock and the Home apps list look and sit (dock style, list layout,
 * position, presentation, vertical alignment). Moved off the main Appearance screen, which now links here. Serves both scopes:
 * with a `facetId` it edits that facet's own look, each field `LAUNCHER_DEFAULT`-sentinel overridable, with no separate
 * Inherit/Override switch. Shares [AppearanceSettingsViewModel] with [AppearanceSettingsScreen].
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
        onAppListLayoutChange = viewModel::setAppListLayout,
        onAppListColumnAlignmentChange = viewModel::setAppListColumnAlignment,
        onAppListGridColumnsChange = viewModel::setAppListGridColumns,
        onAppListGridDisplayModeChange = viewModel::setAppListGridDisplayMode,
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
    onAppListLayoutChange: (AppListLayout) -> Unit,
    onAppListColumnAlignmentChange: (AppListColumnAlignment) -> Unit,
    onAppListGridColumnsChange: (AppListGridColumns) -> Unit,
    onAppListGridDisplayModeChange: (AppListGridDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFacetScoped = uiState.isFacetScoped
    // LAUNCHER_DEFAULT only makes sense where there's a global value above to inherit from —
    // offering it in global mode would be circular (see ClockFontOption's own precedent, which
    // this mirrors).
    val dockDisplayModeOptions = if (isFacetScoped) DockDisplayMode.entries else DockDisplayMode.entries - DockDisplayMode.LAUNCHER_DEFAULT
    val appRowPositionOptions = if (isFacetScoped) AppRowPosition.entries else AppRowPosition.entries - AppRowPosition.LAUNCHER_DEFAULT
    val appRowPresentationOptions = if (isFacetScoped) AppRowPresentation.entries else AppRowPresentation.entries - AppRowPresentation.LAUNCHER_DEFAULT
    val appListVerticalAlignmentOptions = if (isFacetScoped) AppListVerticalAlignment.entries else AppListVerticalAlignment.entries - AppListVerticalAlignment.LAUNCHER_DEFAULT
    val appListLayoutOptions = if (isFacetScoped) AppListLayout.entries else AppListLayout.entries - AppListLayout.LAUNCHER_DEFAULT
    val appListColumnAlignmentOptions = if (isFacetScoped) AppListColumnAlignment.entries else AppListColumnAlignment.entries - AppListColumnAlignment.LAUNCHER_DEFAULT
    val appListGridColumnsOptions = if (isFacetScoped) AppListGridColumns.entries else AppListGridColumns.entries - AppListGridColumns.LAUNCHER_DEFAULT
    val appListGridDisplayModeOptions = if (isFacetScoped) AppListGridDisplayMode.entries else AppListGridDisplayMode.entries - AppListGridDisplayMode.LAUNCHER_DEFAULT
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
                        settings = uiState.clockSettings,
                        appRowPosition = uiState.appRowPosition,
                        appRowPresentation = uiState.appRowPresentation,
                        appListLayout = uiState.appListLayout,
                        appListColumnAlignment = uiState.appListColumnAlignment,
                        appListGridColumns = uiState.appListGridColumns,
                        appListGridDisplayMode = uiState.appListGridDisplayMode,
                        dockDisplayMode = uiState.dockDisplayMode,
                        favorites = uiState.effectiveFavorites,
                        dockItems = uiState.effectiveDockItems,
                        calendarEvents = uiState.calendarEvents,
                        homeWallpaper = homeWallpaper,
                        modifier = Modifier.padding(bottom = 20.dp).testTag("appearance_preview_card"),
                    )
                }
                item {
                    // "How things look/are placed" for Dock and the Home apps list — moved in from
                    // Dock's/Home-Apps-List's own screens (see chat history), always shown at both
                    // scopes: LAUNCHER_DEFAULT itself (filtered from the option list in global mode)
                    // is what lets a facet revert to inheriting, so there's no separate
                    // Inherit/Override switch gating this card the way Clock/Calendar/Apps/Dock's
                    // own content screens have one.
                    SettingsCard {
                        LabeledDropdownRow(
                            title = stringResource(R.string.dock_display_style),
                            options = dockDisplayModeOptions,
                            selected = uiState.dockDisplayMode,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onDockDisplayModeChange,
                            testTag = "appearance_dock_display_style_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.home_apps_list_layout),
                            options = appListLayoutOptions,
                            selected = uiState.appListLayout,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onAppListLayoutChange,
                            testTag = "appearance_app_list_layout_row",
                        )
                        CardDivider()
                        // Each layout mode gets its own alignment/presentation controls — mirrors
                        // how "Apps to show" is already conditionally hidden for Favorites mode on
                        // HomeAppsListSettingsScreen. AppRowPosition (per-row left/center/right)
                        // only makes sense for a single column; AppListColumnAlignment replaces it
                        // for two columns; grid has no per-item alignment concept at all, so
                        // neither position nor the icon/text presentation row below applies —
                        // AppListGridDisplayMode is grid's own separate presentation setting.
                        when (uiState.appListLayout) {
                            AppListLayout.TWO_COLUMN -> {
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_column_alignment),
                                    options = appListColumnAlignmentOptions,
                                    selected = uiState.appListColumnAlignment,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppListColumnAlignmentChange,
                                    testTag = "appearance_app_list_column_alignment_row",
                                )
                                CardDivider()
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_presentation),
                                    options = appRowPresentationOptions,
                                    selected = uiState.appRowPresentation,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppRowPresentationChange,
                                    testTag = "appearance_app_row_presentation_row",
                                )
                                CardDivider()
                            }
                            AppListLayout.GRID -> {
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_grid_columns),
                                    options = appListGridColumnsOptions,
                                    selected = uiState.appListGridColumns,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppListGridColumnsChange,
                                    testTag = "appearance_app_list_grid_columns_row",
                                )
                                CardDivider()
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_grid_display),
                                    options = appListGridDisplayModeOptions,
                                    selected = uiState.appListGridDisplayMode,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppListGridDisplayModeChange,
                                    testTag = "appearance_app_list_grid_display_mode_row",
                                )
                                CardDivider()
                            }
                            AppListLayout.SINGLE_COLUMN, AppListLayout.LAUNCHER_DEFAULT -> {
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_position),
                                    options = appRowPositionOptions,
                                    selected = uiState.appRowPosition,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppRowPositionChange,
                                    testTag = "appearance_app_row_position_row",
                                )
                                CardDivider()
                                LabeledDropdownRow(
                                    title = stringResource(R.string.home_apps_list_presentation),
                                    options = appRowPresentationOptions,
                                    selected = uiState.appRowPresentation,
                                    label = { stringResource(it.displayNameRes) },
                                    onSelect = onAppRowPresentationChange,
                                    testTag = "appearance_app_row_presentation_row",
                                )
                                CardDivider()
                            }
                        }
                        LabeledDropdownRow(
                            title = stringResource(R.string.home_apps_list_list_position),
                            options = appListVerticalAlignmentOptions,
                            selected = uiState.appListVerticalAlignment,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onAppListVerticalAlignmentChange,
                            testTag = "appearance_app_list_vertical_alignment_row",
                        )
                    }
                }
            }
        },
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
            onAppListLayoutChange = {},
            onAppListColumnAlignmentChange = {},
            onAppListGridColumnsChange = {},
            onAppListGridDisplayModeChange = {},
        )
    }
}
