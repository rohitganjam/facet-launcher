package com.facetlauncher.app.ui.profiles

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.InheritOverrideCard
import com.facetlauncher.app.ui.components.RenameDialog
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.SurfaceContainer

/** Per-profile settings (`3d`), reached from the long-press sheet's "Edit profile" or the carousel's gear. */
@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    onNavigateToAppsList: (profileId: Long) -> Unit,
    onNavigateToDockSettings: (profileId: Long) -> Unit,
    onNavigateToCalendarSettings: (profileId: Long) -> Unit,
    onNavigateToClockStyleGallery: (profileId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileSettingsContent(
        uiState = uiState,
        onBack = onBack,
        onAppsListClick = { onNavigateToAppsList(viewModel.profileId) },
        onDockClick = { onNavigateToDockSettings(viewModel.profileId) },
        onCalendarClick = { onNavigateToCalendarSettings(viewModel.profileId) },
        onClockStyleClick = { onNavigateToClockStyleGallery(viewModel.profileId) },
        onRename = viewModel::renameProfile,
        onOverridingAppsChanged = viewModel::setOverridingApps,
        onOverridingDockChanged = viewModel::setOverridingDock,
        onOverridingClockChanged = viewModel::setOverridingClock,
        modifier = modifier,
    )
}

@Composable
private fun ProfileSettingsContent(
    uiState: ProfileSettingsUiState,
    onBack: () -> Unit,
    onAppsListClick: () -> Unit,
    onDockClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onClockStyleClick: () -> Unit,
    onRename: (String) -> Unit,
    onOverridingAppsChanged: (Boolean) -> Unit,
    onOverridingDockChanged: (Boolean) -> Unit,
    onOverridingClockChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    val profile = uiState.profile

    StickyHeaderLayout(
        modifier = modifier,
        header = { ProfileSettingsHeader(title = profile?.name ?: "Profile", onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("profile_settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight, bottom = 24.dp),
            ) {
                item {
                    SettingsCard {
                        ProfileSettingsRow(
                            title = "Rename profile",
                            subtitle = profile?.name,
                            onClick = { showRenameDialog = true },
                            testTag = "profile_settings_rename_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
                item { SectionHeader("APPS LIST") }
                item {
                    InheritOverrideCard(
                        overriding = uiState.isOverridingApps,
                        onOverridingChanged = onOverridingAppsChanged,
                        testTagPrefix = "profile_apps",
                        inheritSubtitle = uiState.globalListContentMode.displayLabel() +
                            if (uiState.globalListContentMode != ListContentMode.FAVORITES) " · ${uiState.globalAppsToShowCount} apps" else "",
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    SettingsCard {
                        ProfileSettingsRow(
                            title = "Apps list settings",
                            subtitle = appsSubtitle(uiState),
                            onClick = onAppsListClick,
                            testTag = "profile_apps_list_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
                item { SectionHeader("DOCK") }
                item {
                    InheritOverrideCard(
                        overriding = uiState.isOverridingDock,
                        onOverridingChanged = onOverridingDockChanged,
                        testTagPrefix = "profile_dock",
                        inheritSubtitle = "${uiState.globalDockDisplayMode.displayLabel()} · ${uiState.defaultDockApps.size} apps",
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    SettingsCard {
                        ProfileSettingsRow(
                            title = "Dock settings",
                            subtitle = "${uiState.dockDisplayMode.displayLabel()} · ${uiState.effectiveDockApps.size} apps",
                            onClick = onDockClick,
                            testTag = "profile_dock_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(18.dp)) }
                item { SectionHeader("CLOCK & CALENDAR") }
                item {
                    InheritOverrideCard(
                        overriding = uiState.isOverridingClock,
                        onOverridingChanged = onOverridingClockChanged,
                        testTagPrefix = "profile_clock",
                        inheritSubtitle = "${uiState.globalClockTemplateId.name.replace("_", " ")} · ${if (uiState.globalUse24HourTime) "24-hour" else "12-hour"} time",
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item {
                    SettingsCard {
                        ProfileSettingsRow(
                            title = "Clock & Calendar Style",
                            subtitle = if (uiState.isOverridingClock) "Overriding defaults · tap to edit" else "Inherits default · Light stack · tap to preview",
                            onClick = onClockStyleClick,
                            testTag = "profile_clock_style_gallery_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ProfileSettingsRow(
                            title = "Calendars to display",
                            subtitle = "${uiState.selectedCalendarCount} calendars selected · " +
                                if (uiState.effectiveShowAllDayEvents) "All-day events visible" else "All-day events hidden",
                            onClick = onCalendarClick,
                            testTag = "profile_calendar_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }
            }
        },
    )

    if (showRenameDialog && profile != null) {
        RenameDialog(
            title = "Rename profile",
            explanation = "Rename this profile.",
            initialValue = profile.name,
            onSave = { newName -> onRename(newName); showRenameDialog = false },
            onDismiss = { showRenameDialog = false },
        )
    }
}

private fun appsSubtitle(uiState: ProfileSettingsUiState): String {
    val mode = uiState.listContentMode
    return if (mode == ListContentMode.FAVORITES) {
        "${mode.displayLabel()} · ${uiState.favoritesLabel}"
    } else {
        "${mode.displayLabel()} · ${uiState.appsToShowCount} apps"
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileSettingsScreenPreview() {
    FacetLauncherTheme {
        ProfileSettingsContent(
            uiState = ProfileSettingsUiState(),
            onBack = {},
            onAppsListClick = {},
            onDockClick = {},
            onCalendarClick = {},
            onClockStyleClick = {},
            onRename = {},
            onOverridingAppsChanged = {},
            onOverridingDockChanged = {},
            onOverridingClockChanged = {},
        )
    }
}
