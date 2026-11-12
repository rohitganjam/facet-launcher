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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facetlauncher.app.R
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.DrawerPresentation
import com.facetlauncher.app.data.model.LauncherSettings
import com.facetlauncher.app.data.model.NotificationBadgeStyle
import com.facetlauncher.app.data.model.PlacedItem
import com.facetlauncher.app.ui.components.BackButton
import com.facetlauncher.app.ui.components.CardDivider
import com.facetlauncher.app.ui.components.LocalSettingsRowInset
import com.facetlauncher.app.ui.components.SettingsCard
import com.facetlauncher.app.ui.components.SettingsIconBadge
import com.facetlauncher.app.ui.components.SettingsIconBadgeInset
import com.facetlauncher.app.ui.components.SettingsSectionHeader
import com.facetlauncher.app.ui.components.StickyHeaderLayout
import com.facetlauncher.app.ui.pro.ProSettingsCard
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import com.facetlauncher.app.ui.theme.Ink
import com.facetlauncher.app.ui.theme.Muted
import com.facetlauncher.app.ui.theme.SettingsSectionHue
import com.facetlauncher.app.ui.theme.SurfaceContainer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Canonical Launcher Settings screen (`3c`) — purely a directory of navigation rows now (see chat
 * history for the reasoning behind this grouping): FACETS, APPEARANCE (also where "Clock style"
 * now lives, as a row inside it — see chat history: calendar/appearance styling consolidation),
 * HOME & APPS (Calendars / Dock / Home Apps List / App Drawer / Notifications — grouped by "what
 * shows on an app icon, on Home, or in the drawer", not by how narrowly each one's own settings
 * happen to be scoped), SYSTEM (Permissions / Backup & restore / Set as default launcher —
 * device-level, not a launcher preference), and ABOUT (version / What's new / updates / Discord).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onViewFacets: () -> Unit,
    onViewFacetAutomation: () -> Unit,
    onViewActiveFacetSettings: (Long) -> Unit,
    onViewFacetPro: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToWallpaper: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToGestures: () -> Unit,
    onNavigateToWeatherSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Notification access has no grant-change callback: re-check on resume, so coming back from the Notifications page updates the row.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshNotificationAccess() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onViewFacets = onViewFacets,
        onViewFacetAutomation = onViewFacetAutomation,
        onViewActiveFacetSettings = onViewActiveFacetSettings,
        onViewFacetPro = onViewFacetPro,
        onNavigateToAppearance = onNavigateToAppearance,
        onNavigateToWallpaper = onNavigateToWallpaper,
        onNavigateToAppDrawerSettings = onNavigateToAppDrawerSettings,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onNavigateToFolders = onNavigateToFolders,
        onNavigateToGestures = onNavigateToGestures,
        onNavigateToWeatherSettings = onNavigateToWeatherSettings,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToBackupRestore = onNavigateToBackupRestore,
        onNavigateToWhatsNew = onNavigateToWhatsNew,
        onNavigateToLicenses = onNavigateToLicenses,
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
    onViewFacetAutomation: () -> Unit,
    onViewActiveFacetSettings: (Long) -> Unit,
    onViewFacetPro: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToWallpaper: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToGestures: () -> Unit,
    onNavigateToWeatherSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onRequestDefaultLauncherIntent: () -> Intent,
    onRequestWorkProfileSettingsIntent: () -> Intent,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val debugEntry = LocalDebugSettingsEntry.current
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
                item { ProSettingsCard(isPro = uiState.isPro, facetCount = uiState.facetCount, triggersRunning = uiState.triggersRunning, onClick = onViewFacetPro) }
                item { SettingsSectionHeader(stringResource(R.string.settings_section_facets)) }
                item { FacetsCard(uiState, onViewActiveFacetSettings, onViewFacets, onViewFacetAutomation) }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_appearance)) }
                item { AppearanceCard(uiState, onNavigateToAppearance, onNavigateToWallpaper, onNavigateToAppDrawerSettings, onNavigateToNotificationSettings) }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_home_apps)) }
                item { HomeAppsCard(uiState, onNavigateToFolders) }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_system)) }
                item {
                    SystemCard(
                        uiState = uiState,
                        onNavigateToGestures = onNavigateToGestures,
                        onNavigateToWeatherSettings = onNavigateToWeatherSettings,
                        onNavigateToPermissions = onNavigateToPermissions,
                        onNavigateToBackupRestore = onNavigateToBackupRestore,
                        onSetDefaultLauncher = { defaultLauncherLauncher.launch(onRequestDefaultLauncherIntent()) },
                        onRequestWorkProfileSettingsIntent = onRequestWorkProfileSettingsIntent,
                    )
                }

                item { SettingsSectionHeader(stringResource(R.string.settings_section_about)) }
                item { AboutCard(onNavigateToWhatsNew, onNavigateToLicenses) }
                item { AboutCredit() }

                debugEntry?.let { entry -> item { entry.Content() } }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        },
    )
}

@Composable
private fun FacetsCard(uiState: SettingsUiState, onViewActiveFacetSettings: (Long) -> Unit, onViewFacets: () -> Unit, onViewFacetAutomation: () -> Unit) {
    val automationRuleCount = uiState.automationRuleCount
    SettingsCard(fullBleedRows = true) {
        val activeId = uiState.activeFacetId
        val activeName = uiState.activeFacetName
        if (activeId != null && activeName != null) {
            ClickableRow(
                title = stringResource(R.string.settings_active_facet_title, activeName),
                subtitle = stringResource(R.string.settings_active_facet_subtitle, activeName),
                onClick = { onViewActiveFacetSettings(activeId) },
                testTag = "active_facet_settings_row",
                leadingIcon = iconBadge(Icons.Outlined.Tune, SettingsSectionHue.FACETS),
            )
            CardDivider(startInset = SettingsIconBadgeInset)
        }
        ClickableRow(
            title = stringResource(R.string.settings_facets_title),
            subtitle = stringResource(R.string.settings_facets_subtitle),
            onClick = onViewFacets,
            testTag = "view_facets_row",
            leadingIcon = iconBadge(Icons.Default.Layers, SettingsSectionHue.FACETS),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_facet_automation_title),
            subtitle = if (automationRuleCount == 0) {
                stringResource(R.string.settings_facet_automation_none)
            } else {
                pluralStringResource(R.plurals.settings_facet_automation_rules, automationRuleCount, automationRuleCount)
            },
            onClick = onViewFacetAutomation,
            testTag = "facet_automation_row",
            leadingIcon = iconBadge(Icons.Outlined.AutoMode, SettingsSectionHue.FACETS),
        )
    }
}

@Composable
private fun AppearanceCard(
    uiState: SettingsUiState,
    onNavigateToAppearance: () -> Unit,
    onNavigateToWallpaper: () -> Unit,
    onNavigateToAppDrawerSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
) {
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.settings_change_wallpaper_title),
            subtitle = stringResource(R.string.settings_change_wallpaper_subtitle),
            onClick = onNavigateToWallpaper,
            testTag = "change_wallpaper_row",
            leadingIcon = iconBadge(Icons.Outlined.Wallpaper, SettingsSectionHue.APPEARANCE),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_appearance_title),
            subtitle = stringResource(R.string.settings_appearance_subtitle),
            onClick = onNavigateToAppearance,
            testTag = "appearance_row",
            leadingIcon = iconBadge(Icons.Outlined.Palette, SettingsSectionHue.APPEARANCE),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_app_drawer_title),
            subtitle = appDrawerSummary(uiState.settings.drawerPresentation, uiState.settings.searchContactsEnabled, uiState.settings.searchSettingsEnabled),
            onClick = onNavigateToAppDrawerSettings,
            testTag = "app_drawer_settings_row",
            leadingIcon = iconBadge(Icons.Outlined.Apps, SettingsSectionHue.APPEARANCE),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_notifications_media_row_title),
            subtitle = notificationsSummary(uiState.settings, uiState.notificationAccessGranted),
            onClick = onNavigateToNotificationSettings,
            testTag = "notification_settings_row",
            leadingIcon = iconBadge(Icons.Outlined.Notifications, SettingsSectionHue.APPEARANCE),
        )
    }
}

/** The App Drawer row's subtext: "Show as list · Contact search off · Settings search off", and so on. */
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

/**
 * The Notifications row's subtext, following that page: "Dot badges · Media player on", and so on. Both switches there
 * read off without notification access, so this says so instead of reporting a stored "on" that isn't working.
 */
@Composable
private fun notificationsSummary(settings: LauncherSettings, accessGranted: Boolean): String {
    if (!accessGranted) return stringResource(R.string.settings_notifications_needs_access)
    val badges = when {
        !settings.notificationDotsEnabled -> R.string.settings_notifications_summary_badges_off
        settings.notificationBadgeStyle == NotificationBadgeStyle.COUNT -> R.string.settings_notifications_summary_badges_count
        else -> R.string.settings_notifications_summary_badges_dot
    }
    val media = if (settings.showMediaPlayerOnHome) R.string.settings_notifications_summary_media_on else R.string.settings_notifications_summary_media_off
    return stringResource(R.string.dot_join_2, stringResource(badges), stringResource(media))
}

@Composable
private fun HomeAppsCard(
    uiState: SettingsUiState,
    onNavigateToFolders: () -> Unit,
) {
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.settings_folders_title),
            subtitle = folderCountSummary(uiState.folderCount),
            onClick = onNavigateToFolders,
            testTag = "folders_settings_row",
            leadingIcon = iconBadge(Icons.Outlined.Folder, SettingsSectionHue.HOME_APPS),
        )
    }
}

@Composable
private fun SystemCard(
    uiState: SettingsUiState,
    onNavigateToGestures: () -> Unit,
    onNavigateToWeatherSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onRequestWorkProfileSettingsIntent: () -> Intent,
) {
    val context = LocalContext.current
    SettingsCard(fullBleedRows = true) {
        ClickableRow(
            title = stringResource(R.string.settings_gestures_title),
            subtitle = stringResource(R.string.settings_gestures_subtitle),
            onClick = onNavigateToGestures,
            testTag = "gestures_row",
            leadingIcon = iconBadge(Icons.Outlined.TouchApp, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_weather_title),
            subtitle = stringResource(R.string.settings_weather_subtitle),
            onClick = onNavigateToWeatherSettings,
            testTag = "weather_settings_row",
            leadingIcon = iconBadge(Icons.Outlined.WbSunny, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_permissions_title),
            subtitle = stringResource(R.string.settings_permissions_subtitle),
            onClick = onNavigateToPermissions,
            testTag = "view_permissions_row",
            leadingIcon = iconBadge(Icons.Outlined.Shield, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_backup_restore_title),
            subtitle = stringResource(R.string.settings_backup_restore_subtitle),
            onClick = onNavigateToBackupRestore,
            testTag = "backup_restore_row",
            leadingIcon = iconBadge(Icons.Outlined.SettingsBackupRestore, SettingsSectionHue.SYSTEM),
        )
        CardDivider(startInset = SettingsIconBadgeInset)
        ClickableRow(
            title = stringResource(R.string.settings_set_default_launcher_title),
            subtitle = stringResource(if (uiState.isDefaultLauncher) R.string.settings_status_active else R.string.settings_status_not_set),
            onClick = onSetDefaultLauncher,
            testTag = "set_default_launcher_row",
            leadingIcon = iconBadge(Icons.Outlined.Home, SettingsSectionHue.SYSTEM),
        )
        // Read-only: this app reflects the Work Profile's state, it doesn't
        // control it — Android already owns a pause/resume toggle (system
        // Settings, and usually a Quick Settings tile); see WorkProfileRepository's
        // own doc for why this app doesn't add a second one. One row per entry —
        // almost always 0 or 1 in practice, since stock Android only provisions one
        // real Work Profile per user, but list-shaped so it stays correct if that
        // ever isn't true.
        uiState.workProfiles.forEach { workProfile ->
            CardDivider(startInset = SettingsIconBadgeInset)
            ClickableRow(
                title = workProfile.label,
                subtitle = stringResource(if (workProfile.isPaused) R.string.settings_status_paused else R.string.settings_status_active),
                onClick = { runCatching { context.startActivity(onRequestWorkProfileSettingsIntent()) } },
                testTag = "work_profile_row",
                leadingIcon = iconBadge(Icons.Outlined.Work, SettingsSectionHue.SYSTEM),
            )
        }
    }
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

/** "No folders yet" / "1 folder" / "3 folders" for the Folders row's own subtitle — the full folder library's size, independent of where (if anywhere) each folder is placed. */
@Composable
private fun folderCountSummary(count: Int): String =
    if (count == 0) stringResource(R.string.settings_folders_none) else pluralStringResource(R.plurals.settings_folders_count, count, count)

/** Package-visible (not private) — also used by [AppearanceSettingsScreen]'s own nav rows. */
@Composable
fun ClickableRow(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    // No clip of its own — the enclosing SettingsCard clips, so the press highlight follows its corners.
    // The horizontal padding comes after clickable so, in a full-bleed card, the highlight spans the card.
    Row(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = LocalSettingsRowInset.current, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        leadingIcon?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = Ink)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        trailing?.invoke()
    }
}

internal fun iconBadge(icon: ImageVector, hue: SettingsSectionHue): @Composable () -> Unit = { SettingsIconBadge(icon, hue) }

/** Trailing affordance for a row that navigates to its own screen. Package-visible — see [ClickableRow]. */
@Composable
fun NavigationChevron(modifier: Modifier = Modifier) {
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
            uiState = SettingsUiState(),
            onBack = {},
            onViewFacets = {},
            onViewFacetAutomation = {},
            onViewActiveFacetSettings = {},
            onNavigateToAppearance = {},
            onNavigateToWallpaper = {},
            onNavigateToAppDrawerSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToFolders = {},
            onNavigateToGestures = {},
            onNavigateToWeatherSettings = {},
            onNavigateToPermissions = {},
            onNavigateToBackupRestore = {},
            onNavigateToWhatsNew = {},
            onNavigateToLicenses = {},
            onViewFacetPro = {},
            onRequestDefaultLauncherIntent = { Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) },
            onRequestWorkProfileSettingsIntent = { Intent(Settings.ACTION_SETTINGS) },
        )
    }
}
