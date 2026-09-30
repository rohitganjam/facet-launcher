package com.facetlauncher.app.ui.settings

import android.Manifest
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.DrawerFolderDisplayMode
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.RecentlyInstalledPosition
import com.facetlauncher.app.data.model.SearchBarPosition
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LabeledDropdownRow
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Accent
import com.facetlauncher.app.ui.theme.Hairline
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlin.math.roundToInt

/** Settings → Home & Apps → App Drawer. */
@Composable
fun AppDrawerSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppDrawerSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    val requestContactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setSearchContactsEnabled(granted)
    }

    AppDrawerSettingsContent(
        settings = settings,
        onBack = onBack,
        onDrawerPresentationChange = viewModel::setDrawerPresentation,
        onDrawerGridSizeChange = viewModel::setDrawerGridSize,
        onDrawerListItemSizeChange = viewModel::setDrawerListItemSize,
        onShowDrawerIconsChange = viewModel::setShowDrawerIcons,
        onShowDrawerLabelsChange = viewModel::setShowDrawerLabels,
        onSearchContactsToggle = { enabled ->
            // Turning it on triggers the real request; the launcher's own callback (above) is
            // what actually persists the new value once the user responds. Turning it off needs
            // no permission dance — just write the value directly.
            if (enabled) requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) else viewModel.setSearchContactsEnabled(false)
        },
        onSearchSettingsToggle = viewModel::setSearchSettingsEnabled,
        onSearchBarPositionChange = viewModel::setSearchBarPosition,
        onDrawerOpacityChange = viewModel::setDrawerOpacity,
        onDrawerFolderDisplayModeChange = viewModel::setDrawerFolderDisplayMode,
        onRecentlyInstalledPositionChange = viewModel::setRecentlyInstalledPosition,
        modifier = modifier,
    )
}

@Composable
private fun AppDrawerSettingsContent(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onDrawerPresentationChange: (DrawerPresentation) -> Unit,
    onDrawerGridSizeChange: (DrawerGridSize) -> Unit,
    onDrawerListItemSizeChange: (DrawerListItemSize) -> Unit,
    onShowDrawerIconsChange: (Boolean) -> Unit,
    onShowDrawerLabelsChange: (Boolean) -> Unit,
    onSearchContactsToggle: (Boolean) -> Unit,
    onSearchSettingsToggle: (Boolean) -> Unit,
    onSearchBarPositionChange: (SearchBarPosition) -> Unit,
    onDrawerOpacityChange: (Float) -> Unit,
    onDrawerFolderDisplayModeChange: (DrawerFolderDisplayMode) -> Unit,
    onRecentlyInstalledPositionChange: (RecentlyInstalledPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation = settings.drawerPresentation

    StickyHeaderLayout(
        modifier = modifier,
        header = { AppDrawerSettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("app_drawer_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    SettingsCard {
                        LabeledDropdownRow(
                            title = stringResource(R.string.app_drawer_show_apps_as),
                            options = DrawerPresentation.entries,
                            selected = presentation,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onDrawerPresentationChange,
                            testTag = "drawer_presentation_row",
                        )
                        if (presentation == DrawerPresentation.GRID) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = stringResource(R.string.app_drawer_grid_size),
                                options = DrawerGridSize.entries,
                                selected = settings.drawerGridSize,
                                label = { it.appDrawerDisplayLabel() },
                                onSelect = onDrawerGridSizeChange,
                                testTag = "drawer_grid_size_row",
                            )
                        }
                        if (presentation == DrawerPresentation.LIST) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = stringResource(R.string.app_drawer_list_item_size),
                                options = DrawerListItemSize.entries,
                                selected = settings.drawerListItemSize,
                                label = { stringResource(it.displayNameRes) },
                                onSelect = onDrawerListItemSizeChange,
                                testTag = "drawer_list_item_size_row",
                            )
                            CardDivider()
                            AppDrawerToggleRow(
                                title = stringResource(R.string.app_drawer_show_icons_title),
                                subtitle = stringResource(R.string.app_drawer_show_icons_subtitle),
                                checked = settings.showDrawerIcons,
                                onCheckedChange = onShowDrawerIconsChange,
                                testTag = "show_drawer_icons_toggle",
                            )
                        }
                        if (presentation == DrawerPresentation.GRID) {
                            CardDivider()
                            AppDrawerToggleRow(
                                title = stringResource(R.string.app_drawer_show_labels_title),
                                subtitle = stringResource(R.string.app_drawer_show_labels_subtitle),
                                checked = settings.showDrawerLabels,
                                onCheckedChange = onShowDrawerLabelsChange,
                                testTag = "show_drawer_labels_toggle",
                            )
                        }
                        CardDivider()
                        AppDrawerToggleRow(
                            title = stringResource(R.string.app_drawer_search_contacts_title),
                            subtitle = stringResource(R.string.app_drawer_search_contacts_subtitle),
                            checked = settings.searchContactsEnabled,
                            onCheckedChange = onSearchContactsToggle,
                            testTag = "search_contacts_toggle",
                        )
                        CardDivider()
                        AppDrawerToggleRow(
                            title = stringResource(R.string.app_drawer_search_settings_title),
                            subtitle = stringResource(R.string.app_drawer_search_settings_subtitle),
                            checked = settings.searchSettingsEnabled,
                            onCheckedChange = onSearchSettingsToggle,
                            testTag = "search_settings_toggle",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.app_drawer_search_bar_position),
                            options = SearchBarPosition.entries,
                            selected = settings.searchBarPosition,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onSearchBarPositionChange,
                            testTag = "search_bar_position_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.app_drawer_folders_in_drawer),
                            options = DrawerFolderDisplayMode.entries,
                            selected = settings.drawerFolderDisplayMode,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onDrawerFolderDisplayModeChange,
                            testTag = "drawer_folder_display_mode_row",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = stringResource(R.string.app_drawer_recently_installed_position),
                            options = RecentlyInstalledPosition.entries,
                            selected = settings.recentlyInstalledPosition,
                            label = { stringResource(it.displayNameRes) },
                            onSelect = onRecentlyInstalledPositionChange,
                            testTag = "recently_installed_position_row",
                        )
                        CardDivider()
                        DrawerOpacitySlider(opacity = settings.drawerOpacity, onChange = onDrawerOpacityChange)
                    }
                }
            }
        },
    )
}

@Composable
private fun AppDrawerSettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = stringResource(R.string.app_drawer_settings_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/** Not a fixed per-value [androidx.annotation.StringRes] like this file's other enum labels — the
 *  actual column/row counts are [DrawerGridSize]'s own numeric fields, so this stays a format
 *  template rather than 4 near-identical fixed strings. */
@Composable
private fun DrawerGridSize.appDrawerDisplayLabel(): String = stringResource(R.string.drawer_grid_size_label, columns, rows)

@Composable
private fun AppDrawerToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Constrains the label/subtitle to the space left of the Switch so a long subtitle wraps
        // onto a second line instead of pushing the Switch out past the card's own edge.
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(testTag))
    }
}

@Composable
private fun DrawerOpacitySlider(opacity: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("drawer_opacity_slider").padding(vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.app_drawer_opacity_label), style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = stringResource(R.string.percent_format, (opacity * 100).roundToInt()), style = MaterialTheme.typography.bodyMedium, color = Muted)
        }
        // Plain M3 Slider — default thumb/track shape, just this app's Accent/Hairline colors.
        Slider(
            value = opacity,
            onValueChange = onChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Accent,
                activeTrackColor = Accent,
                inactiveTrackColor = Hairline,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppDrawerSettingsScreenPreview() {
    FacetLauncherTheme {
        AppDrawerSettingsContent(
            settings = LauncherSettings(),
            onBack = {},
            onDrawerPresentationChange = {},
            onDrawerGridSizeChange = {},
            onDrawerListItemSizeChange = {},
            onShowDrawerIconsChange = {},
            onShowDrawerLabelsChange = {},
            onSearchContactsToggle = {},
            onSearchSettingsToggle = {},
            onSearchBarPositionChange = {},
            onDrawerOpacityChange = {},
            onDrawerFolderDisplayModeChange = {},
            onRecentlyInstalledPositionChange = {},
        )
    }
}
