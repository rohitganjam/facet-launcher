package com.facetlauncher.app.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.ListContentMode
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SurfaceContainer

/**
 * Canonical Launcher Settings screen (`3c`) — purely a directory of navigation rows now (see chat
 * history for the reasoning behind this grouping): FACETS, APPEARANCE, CLOCK & CALENDAR,
 * HOME & APPS (Dock / Home Apps List / App Drawer / Notifications — grouped by "what shows on an
 * app icon or in the drawer", not by how narrowly each one's own settings happen to be scoped),
 * and SYSTEM (Permissions / Backup & restore / Set as default launcher / About — device-level,
 * not a launcher preference).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onViewFacets: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToDockSettings: () -> Unit,
    onNavigateToHomeAppsListSettings: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onViewFacets = onViewFacets,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
        onNavigateToCalendarSettings = onNavigateToCalendarSettings,
        onNavigateToDockSettings = onNavigateToDockSettings,
        onNavigateToHomeAppsListSettings = onNavigateToHomeAppsListSettings,
        onNavigateToAppDrawerSettings = onNavigateToAppDrawerSettings,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onNavigateToFolders = onNavigateToFolders,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToBackupRestore = onNavigateToBackupRestore,
        onNavigateToAbout = onNavigateToAbout,
        onRequestDefaultLauncherIntent = viewModel::requestDefaultLauncherIntent,
        onRequestWorkProfileSettingsIntent = viewModel::workProfileSettingsIntent,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onViewFacets: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToDockSettings: () -> Unit,
    onNavigateToHomeAppsListSettings: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onRequestDefaultLauncherIntent: () -> Intent,
    onRequestWorkProfileSettingsIntent: () -> Intent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // RoleManager's request-role intent must be launched for a result — some OEM
    // PermissionController builds (see DefaultLauncherRepository) silently self-finish the
    // RequestRoleActivity if it's started with a plain startActivity() instead, so this can't
    // reuse a bare context.startActivity() call the way the other rows on this screen do.
    val defaultLauncherLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {}

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
                item { SectionHeader(stringResource(R.string.settings_section_facets)) }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = stringResource(R.string.settings_facets_title),
                            subtitle = stringResource(R.string.settings_facets_subtitle),
                            onClick = onViewFacets,
                            testTag = "view_facets_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_appearance)) }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = stringResource(R.string.settings_change_wallpaper_title),
                            subtitle = stringResource(R.string.settings_change_wallpaper_subtitle),
                            onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_SET_WALLPAPER)) } },
                            testTag = "change_wallpaper_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_appearance_title),
                            subtitle = stringResource(R.string.settings_appearance_subtitle),
                            onClick = onNavigateToAppearance,
                            testTag = "appearance_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_clock_calendar)) }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = stringResource(R.string.settings_clock_calendar_title),
                            subtitle = stringResource(
                                R.string.dot_join_2,
                                stringResource(uiState.settings.clockTemplateId.displayNameRes),
                                stringResource(if (uiState.settings.use24HourTime) R.string.settings_time_format_24h else R.string.settings_time_format_12h),
                            ),
                            onClick = onNavigateToClockStyleGallery,
                            testTag = "clock_style_gallery_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_calendars_title),
                            subtitle = stringResource(
                                R.string.dot_join_2,
                                pluralStringResource(R.plurals.settings_calendars_selected, uiState.selectedCalendarCount, uiState.selectedCalendarCount),
                                stringResource(if (uiState.settings.showAllDayEvents) R.string.settings_all_day_events_visible else R.string.settings_all_day_events_hidden),
                            ),
                            onClick = onNavigateToCalendarSettings,
                            testTag = "calendar_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_home_apps)) }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = stringResource(R.string.settings_dock_title),
                            subtitle = dockSummary(uiState.dockItems),
                            onClick = onNavigateToDockSettings,
                            testTag = "dock_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_home_apps_list_title),
                            subtitle = appsListSummary(uiState.settings.listContentMode, uiState.defaultFavorites.size, uiState.settings.appsToShowCount),
                            onClick = onNavigateToHomeAppsListSettings,
                            testTag = "home_apps_list_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_app_drawer_title),
                            subtitle = appDrawerSummary(uiState.settings.drawerPresentation, uiState.settings.searchContactsEnabled, uiState.settings.searchSettingsEnabled),
                            onClick = onNavigateToAppDrawerSettings,
                            testTag = "app_drawer_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_notifications_title),
                            subtitle = if (uiState.settings.notificationDotsEnabled) {
                                stringResource(R.string.settings_notifications_enabled_subtitle, uiState.settings.notificationBadgeStyle.notificationsSummaryLabel())
                            } else {
                                stringResource(R.string.settings_notifications_disabled_subtitle)
                            },
                            onClick = onNavigateToNotificationSettings,
                            testTag = "notification_settings_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_folders_title),
                            subtitle = folderCountSummary(uiState.folderCount),
                            onClick = onNavigateToFolders,
                            testTag = "folders_settings_row",
                            trailing = { NavigationChevron() },
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_section_system)) }
                item {
                    SettingsCard {
                        ClickableRow(
                            title = stringResource(R.string.settings_permissions_title),
                            subtitle = stringResource(R.string.settings_permissions_subtitle),
                            onClick = onNavigateToPermissions,
                            testTag = "view_permissions_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_backup_restore_title),
                            subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                            onClick = onNavigateToBackupRestore,
                            testTag = "backup_restore_row",
                            trailing = { NavigationChevron() },
                        )
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_set_default_launcher_title),
                            subtitle = stringResource(if (uiState.isDefaultLauncher) R.string.settings_status_active else R.string.settings_status_not_set),
                            onClick = { defaultLauncherLauncher.launch(onRequestDefaultLauncherIntent()) },
                            testTag = "set_default_launcher_row",
                            trailing = { NavigationChevron() },
                        )
                        // Read-only: this app reflects the Work Profile's state, it doesn't
                        // control it — Android already owns a pause/resume toggle (system
                        // Settings, and usually a Quick Settings tile); see WorkProfileRepository's
                        // own doc for why this app doesn't add a second one. One row per entry —
                        // almost always 0 or 1 in practice, since stock Android only provisions one
                        // real Work Profile per user, but list-shaped so it stays correct if that
                        // ever isn't true.
                        uiState.workProfiles.forEach { workProfile ->
                            CardDivider()
                            ClickableRow(
                                title = workProfile.label,
                                subtitle = stringResource(if (workProfile.isPaused) R.string.settings_status_paused else R.string.settings_status_active),
                                onClick = { runCatching { context.startActivity(onRequestWorkProfileSettingsIntent()) } },
                                testTag = "work_profile_row",
                                trailing = { NavigationChevron() },
                            )
                        }
                        CardDivider()
                        ClickableRow(
                            title = stringResource(R.string.settings_about_title),
                            subtitle = stringResource(R.string.settings_about_subtitle),
                            onClick = onNavigateToAbout,
                            testTag = "about_row",
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
        Text(text = stringResource(R.string.settings_header_title), style = MaterialTheme.typography.headlineSmall, color = Ink)
    }
}

/** "4 Favorites"/"5 Most used"/"5 Recents" — the live count for whichever list content mode is active, not just its name. */
@Composable
private fun appsListSummary(mode: ListContentMode, favoritesCount: Int, appsToShowCount: Int): String = when (mode) {
    ListContentMode.FAVORITES -> pluralStringResource(R.plurals.settings_favorites_count, favoritesCount, favoritesCount)
    ListContentMode.RECENTS -> pluralStringResource(R.plurals.settings_recents_count, appsToShowCount, appsToShowCount)
    ListContentMode.MOST_USED -> stringResource(R.string.settings_most_used_count, appsToShowCount)
}

@Composable
private fun appDrawerSummary(presentation: DrawerPresentation, searchContactsEnabled: Boolean, searchSettingsEnabled: Boolean): String {
    val layout = stringResource(presentation.displayNameRes)
    val contactSearch = stringResource(if (searchContactsEnabled) R.string.settings_search_on else R.string.settings_search_off)
    val settingsSearch = stringResource(if (searchSettingsEnabled) R.string.settings_search_on else R.string.settings_search_off)
    return stringResource(
        R.string.dot_join_3,
        stringResource(R.string.settings_show_as, layout),
        stringResource(R.string.settings_contact_search, contactSearch),
        stringResource(R.string.settings_settings_search, settingsSearch),
    )
}

/** "4 Dock Apps" / "3 Dock Apps, 1 Folder" — Dock capacity is counted by item (a folder costs one slot regardless of how many apps it holds), so this mirrors that rather than the old flat app count, which silently dropped folder members entirely. */
@Composable
private fun dockSummary(items: List<PlacedItem>): String {
    val folderCount = items.count { it is PlacedItem.FolderItem }
    val appCount = items.size - folderCount
    val appsPart = pluralStringResource(R.plurals.settings_dock_apps_count, appCount, appCount)
    return if (folderCount == 0) {
        appsPart
    } else {
        stringResource(R.string.comma_join_2, appsPart, pluralStringResource(R.plurals.settings_dock_folders_count, folderCount, folderCount))
    }
}

/** "No folders yet" / "1 folder" / "3 folders" for the Folders row's own subtitle — the full folder library's size, independent of where (if anywhere) each folder is placed. */
@Composable
private fun folderCountSummary(count: Int): String =
    if (count == 0) stringResource(R.string.settings_folders_none) else pluralStringResource(R.plurals.settings_folders_count, count, count)

/** "Dots"/"Counts" — the actual configured badge style, not a generic "dots/badges" placeholder. */
@Composable
private fun NotificationBadgeStyle.notificationsSummaryLabel(): String = when (this) {
    NotificationBadgeStyle.DOT -> stringResource(R.string.settings_notification_badge_dots)
    NotificationBadgeStyle.COUNT -> stringResource(R.string.settings_notification_badge_counts)
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
            // clickable rows (see FacetCarouselScreen.kt's LauncherSettingsRow/AddFacetRow),
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
    FacetLauncherTheme {
        SettingsContent(
            uiState = SettingsUiState(
                dockItems = (1..4).map {
                    PlacedItem.SingleApp(AppInfo(packageName = "com.example.$it", activityName = ".Main", label = "App $it", icon = null))
                },
            ),
            onBack = {},
            onViewFacets = {},
            onNavigateToAppearance = {},
            onNavigateToClockStyleGallery = {},
            onNavigateToCalendarSettings = {},
            onNavigateToDockSettings = {},
            onNavigateToHomeAppsListSettings = {},
            onNavigateToAppDrawerSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToFolders = {},
            onNavigateToPermissions = {},
            onNavigateToBackupRestore = {},
            onNavigateToAbout = {},
            onRequestDefaultLauncherIntent = { Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) },
            onRequestWorkProfileSettingsIntent = { Intent(Settings.ACTION_SETTINGS) },
        )
    }
}
