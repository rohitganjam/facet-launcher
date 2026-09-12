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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.facetlauncher.app.data.model.DrawerGridSize
import com.facetlauncher.app.data.model.DrawerListItemSize
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
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
        onDrawerPresentationChanged = viewModel::setDrawerPresentation,
        onDrawerGridSizeChanged = viewModel::setDrawerGridSize,
        onDrawerListItemSizeChanged = viewModel::setDrawerListItemSize,
        onShowDrawerIconsChanged = viewModel::setShowDrawerIcons,
        onShowDrawerLabelsChanged = viewModel::setShowDrawerLabels,
        onSearchContactsToggled = { enabled ->
            // Turning it on triggers the real request; the launcher's own callback (above) is
            // what actually persists the new value once the user responds. Turning it off needs
            // no permission dance — just write the value directly.
            if (enabled) requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) else viewModel.setSearchContactsEnabled(false)
        },
        onSearchBarPositionChanged = viewModel::setSearchBarPosition,
        onDrawerOpacityChanged = viewModel::setDrawerOpacity,
        modifier = modifier,
    )
}

@Composable
private fun AppDrawerSettingsContent(
    settings: LauncherSettings,
    onBack: () -> Unit,
    onDrawerPresentationChanged: (DrawerPresentation) -> Unit,
    onDrawerGridSizeChanged: (DrawerGridSize) -> Unit,
    onDrawerListItemSizeChanged: (DrawerListItemSize) -> Unit,
    onShowDrawerIconsChanged: (Boolean) -> Unit,
    onShowDrawerLabelsChanged: (Boolean) -> Unit,
    onSearchContactsToggled: (Boolean) -> Unit,
    onSearchBarPositionChanged: (SearchBarPosition) -> Unit,
    onDrawerOpacityChanged: (Float) -> Unit,
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
                            title = "Show apps as",
                            options = DrawerPresentation.entries,
                            selected = presentation,
                            label = { it.appDrawerDisplayLabel() },
                            onSelect = onDrawerPresentationChanged,
                            testTag = "drawer_presentation_row",
                        )
                        if (presentation == DrawerPresentation.GRID) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = "Grid size",
                                options = DrawerGridSize.entries,
                                selected = settings.drawerGridSize,
                                label = { it.appDrawerDisplayLabel() },
                                onSelect = onDrawerGridSizeChanged,
                                testTag = "drawer_grid_size_row",
                            )
                        }
                        if (presentation == DrawerPresentation.LIST) {
                            CardDivider()
                            LabeledDropdownRow(
                                title = "List item size",
                                options = DrawerListItemSize.entries,
                                selected = settings.drawerListItemSize,
                                label = { it.appDrawerDisplayLabel() },
                                onSelect = onDrawerListItemSizeChanged,
                                testTag = "drawer_list_item_size_row",
                            )
                            CardDivider()
                            AppDrawerToggleRow(
                                title = "Show icons",
                                subtitle = "Shows icons with the application name",
                                checked = settings.showDrawerIcons,
                                onCheckedChange = onShowDrawerIconsChanged,
                                testTag = "show_drawer_icons_toggle",
                            )
                        }
                        if (presentation == DrawerPresentation.GRID) {
                            CardDivider()
                            AppDrawerToggleRow(
                                title = "Show labels",
                                subtitle = "Shows application name below the icon",
                                checked = settings.showDrawerLabels,
                                onCheckedChange = onShowDrawerLabelsChanged,
                                testTag = "show_drawer_labels_toggle",
                            )
                        }
                        CardDivider()
                        AppDrawerToggleRow(
                            title = "Search contacts",
                            subtitle = "Call, message, WhatsApp from results",
                            checked = settings.searchContactsEnabled,
                            onCheckedChange = onSearchContactsToggled,
                            testTag = "search_contacts_toggle",
                        )
                        CardDivider()
                        LabeledDropdownRow(
                            title = "Search bar position",
                            options = SearchBarPosition.entries,
                            selected = settings.searchBarPosition,
                            label = { it.appDrawerDisplayLabel() },
                            onSelect = onSearchBarPositionChanged,
                            testTag = "search_bar_position_row",
                        )
                        CardDivider()
                        DrawerOpacitySlider(opacity = settings.drawerOpacity, onChange = onDrawerOpacityChanged)
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
        Text(text = "App Drawer", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

private fun DrawerPresentation.appDrawerDisplayLabel(): String = when (this) {
    DrawerPresentation.LIST -> "List"
    DrawerPresentation.GRID -> "Grid"
}

private fun DrawerGridSize.appDrawerDisplayLabel(): String = "$columns cols × $rows rows"

private fun DrawerListItemSize.appDrawerDisplayLabel(): String = when (this) {
    DrawerListItemSize.COMPACT -> "Compact"
    DrawerListItemSize.REGULAR -> "Regular"
    DrawerListItemSize.SPACIOUS -> "Spacious"
}

private fun SearchBarPosition.appDrawerDisplayLabel(): String = when (this) {
    SearchBarPosition.TOP -> "Top"
    SearchBarPosition.BOTTOM -> "Bottom"
}

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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
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
            Text(text = "Drawer opacity", style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "${(opacity * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium, color = Muted)
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
            onDrawerPresentationChanged = {},
            onDrawerGridSizeChanged = {},
            onDrawerListItemSizeChanged = {},
            onShowDrawerIconsChanged = {},
            onShowDrawerLabelsChanged = {},
            onSearchContactsToggled = {},
            onSearchBarPositionChanged = {},
            onDrawerOpacityChanged = {},
        )
    }
}
