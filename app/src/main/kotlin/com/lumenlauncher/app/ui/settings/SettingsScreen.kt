package com.lumenlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.BackButton
import com.lumenlauncher.app.ui.components.CardDivider
import com.lumenlauncher.app.ui.components.SettingsCard
import com.lumenlauncher.app.ui.components.StickyHeaderLayout
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.SurfaceContainer

/**
 * Canonical Launcher Settings screen (`3c`) — purely a directory of navigation rows now (see chat
 * history for the reasoning behind this grouping): PROFILES, APPEARANCE, CLOCK & CALENDAR,
 * HOME & APPS (Dock / Home Apps List / App Drawer / Notifications — grouped by "what shows on an
 * app icon or in the drawer", not by how narrowly each one's own settings happen to be scoped),
 * and SYSTEM (Permissions / Backup & restore / Set as default launcher — device-level, not a
 * launcher preference).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onViewProfiles: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToDockSettings: () -> Unit,
    onNavigateToHomeAppsListSettings: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onViewProfiles = onViewProfiles,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
        onNavigateToCalendarSettings = onNavigateToCalendarSettings,
        onNavigateToDockSettings = onNavigateToDockSettings,
        onNavigateToHomeAppsListSettings = onNavigateToHomeAppsListSettings,
        onNavigateToAppDrawerSettings = onNavigateToAppDrawerSettings,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToBackupRestore = onNavigateToBackupRestore,
        onRequestDefaultLauncherIntent = viewModel::requestDefaultLauncherIntent,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onViewProfiles: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToDockSettings: () -> Unit,
    onNavigateToHomeAppsListSettings: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onRequestDefaultLauncherIntent: () -> Intent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    StickyHeaderLayout(
        modifier = modifier,
        header = { SettingsHeader(onBack = onBack) },
        content = { headerHeight ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainer)
                    .testTag("settings_screen")
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = headerHeight),
            ) {
                item { SectionHeader("PROFILES") }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = "Profiles",
                            subtitle = "Create and manage profile specific settings",
                            onClick = onViewProfiles,
                            testTag = "view_profiles_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader("APPEARANCE") }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = "Change wallpaper",
                            subtitle = "Change your system wallpaper",
                            onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) } },
                            testTag = "change_wallpaper_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Launcher Appearance",
                            subtitle = "Theme, accent, icons, fonts",
                            onClick = onNavigateToAppearance,
                            testTag = "appearance_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader("CLOCK & CALENDAR") }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = "Clock & Calendar Style",
                            subtitle = uiState.settings.clockTemplateId.displayName + " · " +
                                if (uiState.settings.use24HourTime) "24-hour time" else "12-hour time",
                            onClick = onNavigateToClockStyleGallery,
                            testTag = "clock_style_gallery_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Calendars to display",
                            subtitle = "${uiState.selectedCalendarCount} calendars selected · " +
                                if (uiState.settings.showAllDayEvents) "All-day events visible" else "All-day events hidden",
                            onClick = onNavigateToCalendarSettings,
                            testTag = "calendar_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader("HOME & APPS") }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = "Dock",
                            subtitle = "${uiState.dockApps.size} Dock Apps",
                            onClick = onNavigateToDockSettings,
                            testTag = "dock_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Home Apps List",
                            subtitle = appsListSummary(uiState.settings.listContentMode, uiState.defaultFavorites.size, uiState.settings.appsToShowCount),
                            onClick = onNavigateToHomeAppsListSettings,
                            testTag = "home_apps_list_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "App Drawer",
                            subtitle = null,
                            onClick = onNavigateToAppDrawerSettings,
                            testTag = "app_drawer_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Notifications",
                            subtitle = if (uiState.settings.notificationDotsEnabled) {
                                "Enabled · ${uiState.settings.notificationBadgeStyle.notificationsSummaryLabel()} on Dock, Home & Drawer"
                            } else {
                                "Disabled"
                            },
                            onClick = onNavigateToNotificationSettings,
                            testTag = "notification_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader("SYSTEM") }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = "Permissions",
                            subtitle = "See what Lumen can access and why",
                            onClick = onNavigateToPermissions,
                            testTag = "view_permissions_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Backup & restore",
                            subtitle = "Export settings as a file · widgets need re-adding on import",
                            onClick = onNavigateToBackupRestore,
                            testTag = "backup_restore_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = "Set as default launcher",
                            subtitle = if (uiState.isDefaultLauncher) "Active" else "Not set",
                            onClick = { runCatching { context.startActivity(onRequestDefaultLauncherIntent()) } },
                            testTag = "set_default_launcher_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}

@Composable
private fun SettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceContainer)
            // Only the top inset — this header sits above scrolling content that already claims
            // the bottom nav-bar inset itself; absorbing systemBars' bottom side too would just
            // pad the header taller for no reason (see StickyHeaderLayout's own doc comment).
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BackButton(onClick = onBack)
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/** "4 Favorites"/"5 Most used"/"5 Recents" — the live count for whichever list content mode is active, not just its name. */
private fun appsListSummary(mode: ListContentMode, favoritesCount: Int, appsToShowCount: Int): String = when (mode) {
    ListContentMode.FAVORITES -> "$favoritesCount Favorites"
    ListContentMode.RECENTS -> "$appsToShowCount Recents"
    ListContentMode.MOST_USED -> "$appsToShowCount Most used"
}

/** "Dots"/"Counts" — the actual configured badge style, not a generic "dots/badges" placeholder. */
private fun NotificationBadgeStyle.notificationsSummaryLabel(): String = when (this) {
    NotificationBadgeStyle.DOT -> "Dots"
    NotificationBadgeStyle.COUNT -> "Counts"
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        // Muted, not Faint — this label is read as navigation, not decoration, and Faint's ~30%
        // alpha was too dim for that (see chat history).
        color = Muted,
        modifier = modifier.padding(top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun ClickableRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            // Rounds the row's own ripple/press-highlight — matches this app's other standalone
            // clickable rows (see ProfileCarouselScreen.kt's LauncherSettingsRow/AddProfileRow),
            // M3's Card default shape (CLAUDE.md's Material 3 shape section).
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        trailing?.invoke()
    }
}

/** Trailing affordance for a row that navigates to its own screen. */
@Composable
private fun NavigationChevron(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Muted,
        modifier = modifier,
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    LumenLauncherTheme {
        SettingsContent(
            uiState = SettingsUiState(
                dockApps = (1..4).map {
                    AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null)
                },
            ),
            onBack = {},
            onViewProfiles = {},
            onNavigateToAppearance = {},
            onNavigateToClockStyleGallery = {},
            onNavigateToCalendarSettings = {},
            onNavigateToDockSettings = {},
            onNavigateToHomeAppsListSettings = {},
            onNavigateToAppDrawerSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToPermissions = {},
            onNavigateToBackupRestore = {},
            onRequestDefaultLauncherIntent = { Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) },
        )
    }
}
