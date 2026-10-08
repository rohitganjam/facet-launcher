package com.facetlauncher.app.ui.facets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.RenameDialog
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Per-facet settings (`3d`), reached from the long-press sheet's "Edit facet" or the carousel's
 * gear. Purely a directory of navigation rows now (see chat history) — mirrors the main
 * `SettingsScreen`'s own layout: no `InheritOverrideCard`s here any more, each destination screen
 * (Apps list / Dock / Appearance / Calendars) now owns its own Inherit/Override switch, sitting
 * on the same screen as the controls it gates rather than one screen removed from them.
 */
@Composable
fun FacetSettingsScreen(
    onBack: () -> Unit,
    onNavigateToAppsList: (facetId: Long) -> Unit,
    onNavigateToDockSettings: (facetId: Long) -> Unit,
    onNavigateToCalendarSettings: (facetId: Long) -> Unit,
    onNavigateToAppearance: (facetId: Long) -> Unit,
    onFacetApply: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FacetSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FacetSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onAppsListClick = { onNavigateToAppsList(viewModel.facetId) },
        onDockClick = { onNavigateToDockSettings(viewModel.facetId) },
        onCalendarClick = { onNavigateToCalendarSettings(viewModel.facetId) },
        onAppearanceClick = { onNavigateToAppearance(viewModel.facetId) },
        onRename = viewModel::renameFacet,
        onApplyFacet = { viewModel.applyFacet(); onFacetApply() },
        modifier = modifier,
    )
}

@Composable
private fun FacetSettingsContent(
    uiState: FacetSettingsUiState,
    onBack: () -> Unit,
    onAppsListClick: () -> Unit,
    onDockClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onRename: (String) -> Unit,
    onApplyFacet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    val facet = uiState.facet

    StickyHeaderLayout(
        modifier = modifier,
        header = {
            FacetSettingsHeader(
                title = facet?.name ?: stringResource(R.string.facet_settings_default_name),
                isActive = uiState.isActive,
                onBack = onBack,
                onApplyFacet = onApplyFacet,
            )
        },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("facet_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    SettingsCard {
                        FacetSettingsRow(
                            title = stringResource(R.string.facet_settings_rename_facet),
                            subtitle = facet?.name,
                            onClick = { showRenameDialog = true },
                            testTag = "facet_settings_rename_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
                item { SectionHeader(stringResource(R.string.settings_section_home_apps)) }
                item {
                    SettingsCard {
                        FacetSettingsRow(
                            title = stringResource(R.string.facet_settings_apps_list_settings_title),
                            subtitle = appsSubtitle(uiState),
                            onClick = onAppsListClick,
                            testTag = "facet_apps_list_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        FacetSettingsRow(
                            title = stringResource(R.string.facet_settings_dock_settings_title),
                            subtitle = stringResource(
                                R.string.dot_join_2,
                                uiState.dockDisplayMode.displayLabel(),
                                pluralStringResource(R.plurals.format_count_apps, uiState.effectiveDockApps.size, uiState.effectiveDockApps.size),
                            ),
                            onClick = onDockClick,
                            testTag = "facet_dock_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        FacetSettingsRow(
                            title = stringResource(R.string.facet_settings_appearance_title),
                            subtitle = stringResource(R.string.dot_join_2, uiState.dockDisplayMode.displayLabel(), uiState.appRowPresentation.displayLabel()),
                            onClick = onAppearanceClick,
                            testTag = "facet_appearance_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        FacetSettingsRow(
                            title = stringResource(R.string.settings_calendars_title),
                            subtitle = stringResource(
                                R.string.dot_join_2,
                                pluralStringResource(R.plurals.settings_calendars_selected, uiState.selectedCalendarCount, uiState.selectedCalendarCount),
                                stringResource(if (uiState.effectiveShowAllDayEvents) R.string.settings_all_day_events_visible else R.string.settings_all_day_events_hidden),
                            ),
                            onClick = onCalendarClick,
                            testTag = "facet_calendar_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }
            }
        },
    )

    if (showRenameDialog && facet != null) {
        RenameDialog(
            title = stringResource(R.string.facet_settings_rename_facet),
            explanation = stringResource(R.string.facet_settings_rename_explanation),
            initialValue = facet.name,
            onSave = { newName -> onRename(newName); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }
}

@Composable
private fun appsSubtitle(uiState: FacetSettingsUiState): String {
    val mode = uiState.listContentMode
    return if (mode == ListContentMode.FAVORITES) {
        stringResource(R.string.dot_join_2, mode.displayLabel(), uiState.favoritesLabel)
    } else {
        stringResource(R.string.dot_join_2, mode.displayLabel(), pluralStringResource(R.plurals.format_count_apps, uiState.appsToShowCount, uiState.appsToShowCount))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FacetSettingsScreenPreview() {
    FacetLauncherTheme {
        FacetSettingsContent(
            uiState = FacetSettingsUiState(),
            onBack = {},
            onAppsListClick = {},
            onDockClick = {},
            onCalendarClick = {},
            onAppearanceClick = {},
            onRename = {},
            onApplyFacet = {},
        )
    }
}
