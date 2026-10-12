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
 * Clock & calendar (clock style, calendars to display), Dock & home ("Dock & Apps appearance" → [DockAppsAppearanceScreen]),
 * Notifications (global only → the Notifications page) and General (global only, "Fonts, themes and icons" → [FontsThemesIconsScreen]).
 * With a `facetId` it is a facet's own Appearance screen: the same layout minus the global-only sections.
 */
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToDockAppsAppearance: () -> Unit,
    onNavigateToFontsThemesIcons: () -> Unit,
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
                viewModel.refreshNotificationAccess()
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
        onNavigateToClockStyleGallery = onNavigateToClockStyleGallery,
        onNavigateToCalendarSettings = onNavigateToCalendarSettings,
        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
        onNavigateToDockAppsAppearance = onNavigateToDockAppsAppearance,
        onNavigateToFontsThemesIcons = onNavigateToFontsThemesIcons,
        modifier = modifier,
    )
}

@Composable
private fun AppearanceSettingsContent(
    settings: LauncherSettings,
    uiState: AppearanceSettingsUiState,
    homeWallpaper: HomeWallpaper,
    onBack: () -> Unit,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToDockAppsAppearance: () -> Unit,
    onNavigateToFontsThemesIcons: () -> Unit,
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
                clockAndCalendarSection(uiState, onNavigateToClockStyleGallery, onNavigateToCalendarSettings)
                dockAndHomeSection(uiState, onNavigateToDockAppsAppearance)
                // Global only — no per-facet override exists for notifications, theme, accent, icons or fonts.
                if (!uiState.isFacetScoped) {
                    notificationsSection(settings, uiState.notificationAccessGranted, onNavigateToNotificationSettings)
                    generalSection(onNavigateToFontsThemesIcons)
                }
            }
        },
    )
}

/** The clock-style row, then "Calendars to display" — a link to the (facet-aware) calendar page, which owns the inherit/override switch and the permission request. */
private fun LazyListScope.clockAndCalendarSection(
    uiState: AppearanceSettingsUiState,
    onNavigateToClockStyleGallery: () -> Unit,
    onNavigateToCalendarSettings: () -> Unit,
) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_section_clock)) }
    item {
        SettingsCard(fullBleedRows = true) {
            ClickableRow(
                title = stringResource(R.string.settings_clock_calendar_title),
                subtitle = stringResource(
                    R.string.dot_join_2,
                    stringResource(uiState.clockSettings.clockTemplateId.displayNameRes),
                    stringResource(if (uiState.clockSettings.use24HourTime) R.string.settings_time_format_24h else R.string.settings_time_format_12h),
                ),
                onClick = onNavigateToClockStyleGallery,
                testTag = "clock_style_gallery_row",
                leadingIcon = iconBadge(Icons.Outlined.Schedule, SettingsSectionHue.APPEARANCE),
            )
            CardDivider(startInset = SettingsIconBadgeInset)
            ClickableRow(
                title = stringResource(R.string.settings_calendars_title),
                subtitle = if (uiState.calendarAccessGranted) {
                    stringResource(
                        R.string.dot_join_2,
                        pluralStringResource(R.plurals.settings_calendars_selected, uiState.selectedCalendarCount, uiState.selectedCalendarCount),
                        stringResource(if (uiState.effectiveShowAllDayEvents) R.string.settings_all_day_events_visible else R.string.settings_all_day_events_hidden),
                    )
                } else {
                    // Like the Notifications row: with no access nothing is read, so a stored selection would be a misleading summary.
                    stringResource(R.string.settings_calendars_needs_access)
                },
                onClick = onNavigateToCalendarSettings,
                testTag = "appearance_calendar_settings_row",
                leadingIcon = iconBadge(Icons.Outlined.CalendarMonth, SettingsSectionHue.APPEARANCE),
            )
        }
    }
}

/** Global only — a link to the Notifications page (badges, and the Home media player). */
private fun LazyListScope.notificationsSection(settings: LauncherSettings, accessGranted: Boolean, onNavigateToNotificationSettings: () -> Unit) {
    item { SettingsSectionHeader(stringResource(R.string.settings_notifications_title)) }
    item {
        SettingsCard(fullBleedRows = true) {
            ClickableRow(
                title = stringResource(R.string.settings_notifications_media_row_title),
                subtitle = notificationsSummary(settings, accessGranted),
                onClick = onNavigateToNotificationSettings,
                testTag = "notification_settings_row",
                leadingIcon = iconBadge(Icons.Outlined.Notifications, SettingsSectionHue.APPEARANCE),
            )
        }
    }
}

/**
 * The row's subtext, following the Notifications page: "Dot badges · Media player on", and so on. Both switches on that page
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


/** One row into [DockAppsAppearanceScreen], summarising the dock and Home list as the facet settings row does ("Dock icons · Home icon & text"). */
private fun LazyListScope.dockAndHomeSection(uiState: AppearanceSettingsUiState, onNavigateToDockAppsAppearance: () -> Unit) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_section_dock_home)) }
    item {
        SettingsCard(fullBleedRows = true) {
            ClickableRow(
                title = stringResource(R.string.appearance_dock_apps_title),
                subtitle = stringResource(
                    R.string.dot_join_2,
                    uiState.dockDisplayMode.appearanceSummaryLabel(),
                    uiState.appRowPresentation.appearanceSummaryLabel(),
                ),
                onClick = onNavigateToDockAppsAppearance,
                testTag = "appearance_dock_apps_row",
                leadingIcon = iconBadge(Icons.Outlined.Dock, SettingsSectionHue.APPEARANCE),
            )
        }
    }
}

/** Global only — one row into [FontsThemesIconsScreen]. */
private fun LazyListScope.generalSection(onNavigateToFontsThemesIcons: () -> Unit) {
    item { SettingsSectionHeader(stringResource(R.string.appearance_section_general)) }
    item {
        SettingsCard(fullBleedRows = true) {
            ClickableRow(
                title = stringResource(R.string.appearance_fonts_themes_icons_title),
                subtitle = stringResource(R.string.appearance_fonts_themes_icons_subtitle),
                onClick = onNavigateToFontsThemesIcons,
                testTag = "appearance_fonts_themes_icons_row",
                leadingIcon = iconBadge(Icons.Outlined.TextFields, SettingsSectionHue.APPEARANCE),
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
            uiState = AppearanceSettingsUiState(globalFavorites = DesignTimeFavorites, globalDockItems = DesignTimeDockItems),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToClockStyleGallery = {},
            onNavigateToCalendarSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToDockAppsAppearance = {},
            onNavigateToFontsThemesIcons = {},
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
                facet = com.facetlauncher.app.data.local.FacetEntity(id = 1L, name = "Work", position = 0),
                globalFavorites = DesignTimeFavorites,
                globalDockItems = DesignTimeDockItems,
            ),
            homeWallpaper = HomeWallpaper.Unavailable,
            onBack = {},
            onNavigateToClockStyleGallery = {},
            onNavigateToCalendarSettings = {},
            onNavigateToNotificationSettings = {},
            onNavigateToDockAppsAppearance = {},
            onNavigateToFontsThemesIcons = {},
        )
    }
}
