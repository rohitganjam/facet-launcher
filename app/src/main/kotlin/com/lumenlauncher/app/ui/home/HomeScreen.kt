package com.lumenlauncher.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.AppContextMenu
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.NotificationBadge
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.HomeAppTextColorFaint
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.homeTextShadow

/**
 * Home surface (`1a`, Airy density `1e`): clock + a short curated app list + dock. Both
 * [appListItems] (Favorites/Recents/Most Used depending on [listContentMode], from
 * [com.lumenlauncher.app.data.FavoriteAppRepository] or
 * [com.lumenlauncher.app.data.UsageStatsRepository]) and [dockApps] (from
 * [com.lumenlauncher.app.data.DockAppRepository]) are real persisted state — this composable
 * only renders, it never slices/computes them itself.
 */
@Composable
fun HomeScreen(
    appListItems: List<AppInfo>,
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    listContentMode: ListContentMode = ListContentMode.FAVORITES,
    showUsageAccessPrompt: Boolean = false,
    onUsageAccessPromptClick: () -> Unit = {},
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    badgeCounts: Map<String, Int> = emptyMap(),
    use24HourTime: Boolean = false,
    clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    clockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    clockColorOption: ClockColorOption = ClockColorOption.INK,
    clockShowMeridiem: Boolean = false,
    calendarEvents: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    calendarColorOption: ClockColorOption = ClockColorOption.INK,
    onEventClick: (CalendarEvent) -> Unit = {},
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without
            // this, the dock at the bottom draws under the gesture/nav bar.
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(start = 24.dp, end = 24.dp, bottom = 22.dp),
    ) {
        ClockBlock(
            modifier = Modifier.padding(top = 52.dp),
            use24HourTime = use24HourTime,
            templateId = clockTemplateId,
            fontOption = clockFontOption,
            colorOption = clockColorOption,
            showMeridiem = clockShowMeridiem,
            events = calendarEvents,
            calendarColors = calendarColors,
            calendarFontOption = calendarFontOption,
            calendarColorOption = calendarColorOption,
            onEventClick = onEventClick,
        )

        Box(modifier = Modifier.weight(1f))

        val listLabelColor = HomeAppTextColorFaint
        Text(
            text = when (listContentMode) {
                ListContentMode.FAVORITES -> "FAVORITES"
                ListContentMode.RECENTS -> "RECENTS"
                ListContentMode.MOST_USED -> "MOST USED"
            },
            style = MaterialTheme.typography.labelSmall.copy(shadow = homeTextShadow(listLabelColor)),
            color = listLabelColor,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        if (showUsageAccessPrompt) {
            UsageAccessStrip(onClick = onUsageAccessPromptClick)
        } else {
            Column {
                appListItems.forEach { app ->
                    AppRow(
                        app = app,
                        onClick = { onAppClick(app) },
                        badgeCount = badgeCounts[app.packageName],
                        badgeStyle = notificationBadgeStyle,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                    )
                }
            }
        }

        // Fixed gap between the app list and whatever follows — the dock, or (when the
        // dock is empty, see MIN_APPS=0) the screen's own bottom padding.
        Spacer(modifier = Modifier.height(16.dp))

        if (dockApps.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
                    .testTag("home_dock_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                dockApps.forEach { app ->
                    DockIcon(
                        app = app,
                        displayMode = dockDisplayMode,
                        onClick = { onAppClick(app) },
                        badgeCount = badgeCounts[app.packageName],
                        badgeStyle = notificationBadgeStyle,
                        onRequestShortcuts = onRequestShortcuts,
                        onLaunchShortcut = onLaunchShortcut,
                    )
                }
            }
        }
    }
}

/** README `4p`'s shared permission-denied/empty-state strip styling, with a real tap target — shown when [listContentMode] needs `PACKAGE_USAGE_STATS` and it isn't granted. */
@Composable
private fun UsageAccessStrip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("home_usage_access_strip")
            .dashedBorder(color = Ink.copy(alpha = 0.16f), cornerRadius = 10.dp)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Most used needs usage access from system settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.weight(1f),
        )
        Text(text = "Open settings", style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(width = strokeWidth.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppInfo,
    onClick: () -> Unit,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppIcon(icon = app.icon, size = 34.dp, cornerRadius = 10.dp, contentDescription = null)
            val textColor = HomeAppTextColor
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium.copy(shadow = homeTextShadow(textColor)),
                color = textColor,
            )
            if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
        )
    }
}

/** Not private: reused by the profile carousel's preview cards ([com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]) so the dock renders identically there. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DockIcon(
    app: AppInfo,
    displayMode: DockDisplayMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    badgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        when (displayMode) {
            DockDisplayMode.ICONS -> Box(
                modifier = modifier.combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true }),
            ) {
                AppIcon(
                    icon = app.icon,
                    size = 50.dp,
                    cornerRadius = 15.dp,
                    contentDescription = app.label,
                    notificationCount = badgeCount,
                    badgeStyle = badgeStyle,
                )
            }
            DockDisplayMode.TEXT -> Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium.copy(shadow = homeTextShadow(HomeAppTextColor)),
                color = HomeAppTextColor,
                modifier = modifier
                    .combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                    .padding(vertical = 14.dp, horizontal = 4.dp),
            )
        }
        AppContextMenu(
            app = app,
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            onRequestShortcuts = onRequestShortcuts,
            onLaunchShortcut = onLaunchShortcut,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Dark", showBackground = true, widthDp = 390, heightDp = 844, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    LumenLauncherTheme {
        val apps = (1..9).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps.take(5),
            dockApps = apps.drop(5).take(4),
            onAppClick = {},
        )
    }
}
