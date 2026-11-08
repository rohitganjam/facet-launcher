package com.facetlauncher.app.ui.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.HomeWallpaper
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.facets.appearanceSummaryLabel
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Settings → Appearance (`3c`) — a live preview of Home, then a section per area, each a short list of rows that open their own screen:
 * Look & Feel (global only: "General settings" → [ThemesAndColorsScreen], "Fonts" → [FontsScreen], "Icons" → [IconsScreen]).
 * The App Drawer and Notifications rows are on the main Settings screen's Appearance card.
 */
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    onNavigateToThemesAndColors: () -> Unit,
    onNavigateToFonts: () -> Unit,
    onNavigateToIcons: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppearanceSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    // No grant-change callback for notification access — re-check on resume so coming back from the
    // Notifications page (and its system Settings redirect) updates the row's subtext.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshCalendarAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    AppearanceSettingsContent(
        settings = settings,
        uiState = uiState,
        homeWallpaper = homeWallpaper,
        onBack = onBack,
        onNavigateToThemesAndColors = onNavigateToThemesAndColors,
        onNavigateToFonts = onNavigateToFonts,
        onNavigateToIcons = onNavigateToIcons,
        modifier = modifier,
    )
}

@Composable
private fun AppearanceSettingsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onNavigateToThemesAndColors: () -> Unit,
    onNavigateToFonts: () -> Unit,
    onNavigateToIcons: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StickyHeaderLayout(
        modifier = modifier,
        header = { AppearanceSettingsHeader(title = stringResource(R.string.appearance_header_title), onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("appearance_settings_screen")
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
                generalSection(settings, onNavigateToThemesAndColors, onNavigateToFonts, onNavigateToIcons)
            }
        },
    )
}

/** Global only — a row each into [ThemesAndColorsScreen] (static subtitle), [FontsScreen] and [IconsScreen] (each naming its current value). */
private fun LazyListScope.generalSection(
    settings: LauncherSettings,
    onNavigateToThemesAndColors: () -> Unit,
    onNavigateToFonts: () -> Unit,
    onNavigateToIcons: () -> Unit,
) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_section_look_feel)) }
    item {
        SettingsCard(fullBleedRows = true) {
            ClickableRow(
                title = stringResource(R.string.appearance_general_title),
                subtitle = stringResource(R.string.appearance_general_subtitle),
                onClick = onNavigateToThemesAndColors,
                testTag = "general_themes_colors_row",
                leadingIcon = iconBadge(Icons.Outlined.Palette, SettingsSectionHue.APPEARANCE),
            )
            CardDivider()
            ClickableRow(
                title = stringResource(R.string.appearance_section_fonts),
                subtitle = stringResource(settings.launcherFontOption.displayNameRes),
                onClick = onNavigateToFonts,
                testTag = "general_fonts_row",
                leadingIcon = iconBadge(Icons.Outlined.TextFields, SettingsSectionHue.APPEARANCE),
            )
            CardDivider()
            ClickableRow(
                title = stringResource(R.string.appearance_section_icons),
                subtitle = stringResource(if (settings.iconPackPackage != null) R.string.icons_screen_icon_pack else settings.iconRenderMode.style.displayNameRes),
                onClick = onNavigateToIcons,
                testTag = "general_icons_row",
                leadingIcon = iconBadge(Icons.Outlined.Category, SettingsSectionHue.APPEARANCE),
            )
        }
    }
}

@Composable
internal fun AppearanceSettingsHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppearanceSettingsScreenPreview() {
    FacetLauncherTheme {
        AppearanceSettingsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(favorites = DesignTimeFavorites, dockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToThemesAndColors = {},
            onNavigateToFonts = {},
            onNavigateToIcons = {},
        )
    }
}

@Preview(name = "Facet-scoped", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AppearanceSettingsScreenFacetScopedPreview() {
    FacetLauncherTheme {
        AppearanceSettingsContent(
            settings = LauncherSettings(),
            uiState = AppearanceSettingsUiState(
                facet = com.facetlauncher.app.data.model.Facet(id = 1L, name = "Work", position = 0),
                favorites = DesignTimeFavorites,
                dockItems = DesignTimeDockItems,
            ),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToThemesAndColors = {},
            onNavigateToFonts = {},
            onNavigateToIcons = {},
        )
    }
}
