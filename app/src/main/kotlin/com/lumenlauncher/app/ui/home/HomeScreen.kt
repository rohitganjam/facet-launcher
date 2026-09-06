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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lumenlauncher.app.data.model.AppInfo
import com.lumenlauncher.app.data.model.AppRowPosition
import com.lumenlauncher.app.data.model.AppRowPresentation
import com.lumenlauncher.app.data.model.AppShortcut
import com.lumenlauncher.app.data.model.CalendarEvent
import com.lumenlauncher.app.data.model.ClockColorOption
import com.lumenlauncher.app.data.model.ClockFontOption
import com.lumenlauncher.app.data.model.ClockTemplateId
import com.lumenlauncher.app.data.model.DockDisplayMode
import com.lumenlauncher.app.data.model.FontWeightOption
import com.lumenlauncher.app.data.model.LauncherFontOption
import com.lumenlauncher.app.data.model.ListContentMode
import com.lumenlauncher.app.data.model.NotificationBadgeStyle
import com.lumenlauncher.app.ui.components.AppContextMenu
import com.lumenlauncher.app.ui.components.AppIcon
import com.lumenlauncher.app.ui.components.NotificationBadge
import com.lumenlauncher.app.ui.theme.Accent
import com.lumenlauncher.app.ui.theme.HomeAppTextColor
import com.lumenlauncher.app.ui.theme.Ink
import com.lumenlauncher.app.ui.theme.LumenLauncherTheme
import com.lumenlauncher.app.ui.theme.Muted
import com.lumenlauncher.app.ui.theme.homeAppLabelShadow
import com.lumenlauncher.app.ui.theme.resolve

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
    appRowPosition: AppRowPosition = AppRowPosition.LEFT,
    appRowPresentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    showUsageAccessPrompt: Boolean = false,
    onUsageAccessPromptClick: () -> Unit = {},
    dockDisplayMode: DockDisplayMode = DockDisplayMode.ICONS,
    notificationBadgeStyle: NotificationBadgeStyle = NotificationBadgeStyle.DOT,
    badgeCounts: Map<String, Int> = emptyMap(),
    use24HourTime: Boolean = false,
    clockTemplateId: ClockTemplateId = ClockTemplateId.LIGHT_STACK,
    clockFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    clockColorOption: ClockColorOption = ClockColorOption.THEME,
    clockShowMeridiem: Boolean = false,
    calendarEvents: List<CalendarEvent> = emptyList(),
    calendarColors: Map<String, String> = emptyMap(),
    calendarFontOption: ClockFontOption = ClockFontOption.SYSTEM,
    calendarColorOption: ClockColorOption = ClockColorOption.THEME,
    calendarFontWeight: FontWeightOption = FontWeightOption.REGULAR,
    onEventClick: (CalendarEvent) -> Unit = {},
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut> = { emptyList() },
    onLaunchShortcut: (AppShortcut) -> Unit = {},
    launcherFontOption: LauncherFontOption = LauncherFontOption.SYSTEM,
    appLabelColorOption: ClockColorOption = ClockColorOption.THEME,
    homeAppsFontWeight: FontWeightOption = FontWeightOption.REGULAR,
) {
    val appLabelColor = appLabelColorOption.resolve()
    val appLabelFontWeight = homeAppsFontWeight.resolve()
    Column(
        modifier = modifier
            .fillMaxSize()
            // Edge-to-edge is enforced unconditionally at this app's targetSdk (36) — without
            // this, the dock at the bottom draws under the gesture/nav bar.
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(bottom = 22.dp),
    ) {
        // Everything but the dock keeps the screen's own 24dp side margin. The dock row is
        // deliberately outside this padding (see below) so Arrangement.SpaceEvenly can space its
        // icons against the true screen edge, not this inset.
        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
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
                calendarFontWeight = calendarFontWeight,
                onEventClick = onEventClick,
                launcherFontOption = launcherFontOption,
            )

            Box(modifier = Modifier.weight(1f))

            if (appListItems.isNotEmpty()) {
                val listLabelColor = Muted
                Text(
                    text = when (listContentMode) {
                        ListContentMode.FAVORITES -> "FAVORITES"
                        ListContentMode.RECENTS -> "RECENTS"
                        ListContentMode.MOST_USED -> "MOST USED"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        shadow = homeAppLabelShadow(listLabelColor),
                        textAlign = if (appRowPosition == AppRowPosition.RIGHT) TextAlign.End else TextAlign.Start,
                    ),
                    color = listLabelColor,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                )
            }
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
                            position = appRowPosition,
                            presentation = appRowPresentation,
                            labelColor = appLabelColor,
                            labelFontWeight = appLabelFontWeight,
                        )
                    }
                }
            }
        }

        // Fixed gap between the app list and whatever follows — the dock, or (when the
        // dock is empty, see MIN_APPS=0) the screen's own bottom padding.
        Spacer(modifier = Modifier.height(16.dp))

        if (dockApps.isNotEmpty()) {
            // Full screen width, no horizontal inset — SpaceEvenly then puts an equal gap before
            // the first icon, between every pair of icons, and after the last, so dock icons sit
            // at the same distance from each other as from the screen edges.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp)
                    .testTag("home_dock_row"),
                horizontalArrangement = Arrangement.SpaceEvenly,
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
                        labelColor = appLabelColor,
                        labelFontWeight = appLabelFontWeight,
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

/**
 * [position] governs both internal ordering (icon-then-label vs. label-then-icon) and whether the
 * row's content packs against the start or end of the available width — `RIGHT` needs both, not
 * just a reversed order while staying left-anchored, for the row to actually hug the screen's
 * right edge. [presentation] independently governs which of icon/label actually render; the
 * unused one's slot composable simply emits nothing rather than branching the whole layout.
 *
 * Not private: reused by the profile carousel's preview cards
 * ([com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen]) so the favorites list renders with
 * the same position/presentation/color/weight styling there as it does on the real Home screen —
 * same reasoning as [DockIcon]'s own visibility.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppRow(
    app: AppInfo,
    onClick: () -> Unit,
    badgeCount: Int?,
    badgeStyle: NotificationBadgeStyle,
    onRequestShortcuts: suspend (AppInfo) -> List<AppShortcut>,
    onLaunchShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the profile carousel's read-only preview cards (see
    // com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                // Large/16dp (MaterialTheme.shapes.large — see CLAUDE.md's shape table, which
                // names this the "navigation drawers" token) clips the ripple/press state to a
                // rounded rect instead of a full-bleed rectangle (see chat history).
                .clip(MaterialTheme.shapes.large)
                .then(
                    if (enableLongPressMenu) {
                        Modifier.combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
                    } else {
                        Modifier.clickable(onClick = onClick)
                    }
                )
                // 16dp — matches App Drawer's own Regular list-item spacing exactly (base 8dp +
                // DrawerListItemSize.REGULAR's 8dp extraRowPaddingDp), per direct request that
                // Home's app list read as the same density as Drawer's default (see chat history).
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            // RIGHT packs the whole row's content against the trailing edge (not just reversed
            // order while left-anchored) so the row visually hugs the screen's right edge.
            horizontalArrangement = if (position == AppRowPosition.RIGHT) {
                Arrangement.spacedBy(14.dp, Alignment.End)
            } else {
                Arrangement.spacedBy(14.dp)
            },
        ) {
            val icon: @Composable () -> Unit = {
                if (showIcon) {
                    AppIcon(
                        icon = app.icon,
                        size = 34.dp,
                        cornerRadius = 10.dp,
                        // No visible label to carry the a11y name when text is hidden — mirrors
                        // the Dock's own icons-only mode (see DockIcon below).
                        contentDescription = if (showLabel) null else app.label,
                        modifier = Modifier.testTag("home_app_icon_${app.packageName}"),
                    )
                }
            }
            val label: @Composable () -> Unit = {
                if (showLabel) {
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                        color = labelColor,
                        modifier = Modifier.testTag("home_app_label_${app.packageName}"),
                    )
                }
            }
            val badge: @Composable () -> Unit = {
                if (badgeCount != null && badgeCount > 0) NotificationBadge(count = badgeCount, style = badgeStyle)
            }
            if (position == AppRowPosition.RIGHT) {
                // Badge first, then label (leftmost of the packed content), icon last — landing
                // on the row's true trailing edge since the row itself is now end-packed.
                badge()
                label()
                icon()
            } else {
                icon()
                label()
                badge()
            }
        }
        if (enableLongPressMenu) {
            AppContextMenu(
                app = app,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
            )
        }
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
    labelColor: Color = HomeAppTextColor,
    labelFontWeight: FontWeight = FontWeight.Normal,
    // False for the profile carousel's read-only preview cards (see
    // com.lumenlauncher.app.ui.profiles.ProfileCarouselScreen) — long-press there must not open
    // Home's real Uninstall/App Info/shortcuts menu, since the preview isn't a place you manage apps.
    enableLongPressMenu: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Box {
        when (displayMode) {
            DockDisplayMode.ICONS -> Box(
                modifier = modifier.then(clickModifier),
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
                style = MaterialTheme.typography.bodyMedium.copy(shadow = homeAppLabelShadow(labelColor), fontWeight = labelFontWeight),
                color = labelColor,
                modifier = modifier
                    .then(clickModifier)
                    .padding(vertical = 14.dp, horizontal = 4.dp),
            )
        }
        if (enableLongPressMenu) {
            AppContextMenu(
                app = app,
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onRequestShortcuts = onRequestShortcuts,
                onLaunchShortcut = onLaunchShortcut,
            )
        }
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

/** Position = Right: rows hug the right edge, label-then-icon. */
@Preview(name = "Right position", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(
    name = "Right position - Dark",
    showBackground = true,
    widthDp = 390,
    heightDp = 844,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenRightPositionPreview() {
    LumenLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps,
            dockApps = emptyList(),
            onAppClick = {},
            appRowPosition = AppRowPosition.RIGHT,
        )
    }
}

/** Presentation = Text Only: no icons in the app list. */
@Preview(name = "Text only presentation", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenTextOnlyPresentationPreview() {
    LumenLauncherTheme {
        val apps = (1..5).map {
            AppInfo(packageName = "com.example.app$it", activityName = ".MainActivity", label = "App $it", icon = null)
        }
        HomeScreen(
            appListItems = apps,
            dockApps = emptyList(),
            onAppClick = {},
            appRowPresentation = AppRowPresentation.TEXT_ONLY,
        )
    }
}
