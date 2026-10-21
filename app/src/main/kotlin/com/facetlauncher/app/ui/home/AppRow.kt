package com.facetlauncher.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.AppRowPosition
import com.facetlauncher.app.data.model.AppRowPresentation
import com.facetlauncher.app.ui.components.AppIcon
import com.facetlauncher.app.ui.components.AppIconSize
import com.facetlauncher.app.ui.components.NotificationBadge
import com.facetlauncher.app.ui.components.longPressReleaseClickable
import com.facetlauncher.app.ui.home.AppRow
import com.facetlauncher.app.ui.theme.homeAppLabelShadow

/**
 * One Home app-list row: icon, label and notification badge, packed by [position]. A tap calls [onClick] (the
 * app's launch by default); a long-press opens the app menu unless [enableLongPressMenu] is off, as on the
 * facet carousel's read-only preview cards. [testTagPrefix] tells this row's tags from another row for the same
 * app elsewhere in the same semantics tree, so any surface other than Home's own list must override it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AppRow(
    app: AppInfo,
    actions: HomeItemActions,
    style: HomeItemStyle,
    modifier: Modifier = Modifier,
    position: AppRowPosition = AppRowPosition.LEFT,
    presentation: AppRowPresentation = AppRowPresentation.ICON_AND_TEXT,
    verticalPadding: Dp = HOME_APP_ROW_REGULAR_VERTICAL_PADDING,
    iconSize: Dp = AppIconSize.ROW_REGULAR,
    enableLongPressMenu: Boolean = true,
    testTagPrefix: String = "home_",
    onClick: () -> Unit = { actions.onAppClick(app) },
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val clickModifier = if (enableLongPressMenu) {
        Modifier.longPressReleaseClickable(onClick = onClick, onLongPress = { menuExpanded = true })
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Box(modifier = modifier) {
        AppRowContent(
            app = app,
            style = style,
            position = position,
            presentation = presentation,
            iconSize = iconSize,
            testTagPrefix = testTagPrefix,
            // The large shape (16dp) clips the ripple to a rounded rect instead of a full-bleed rectangle.
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .then(clickModifier)
                .padding(horizontal = 8.dp, vertical = verticalPadding),
        )
        if (enableLongPressMenu) AppItemMenu(app, menuExpanded, { menuExpanded = false }, actions, style)
    }
}

/** The icon, label and badge of an [AppRow], in the order [position] asks for. */
@Composable
private fun AppRowContent(
    app: AppInfo,
    style: HomeItemStyle,
    position: AppRowPosition,
    presentation: AppRowPresentation,
    iconSize: Dp,
    testTagPrefix: String,
    modifier: Modifier = Modifier,
) {
    val showIcon = presentation != AppRowPresentation.TEXT_ONLY
    val showLabel = presentation != AppRowPresentation.ICON_ONLY
    val badgeCount = style.badgeCounts[app.packageName]
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = appRowArrangement(position)) {
        val icon: @Composable () -> Unit = {
            if (showIcon) AppRowIcon(app, iconSize, badgeCount.takeUnless { showLabel }, showLabel, style, testTagPrefix)
        }
        val label: @Composable () -> Unit = { if (showLabel) AppRowLabel(app, style, testTagPrefix) }
        val badge: @Composable () -> Unit = {
            badgeCount.takeIf { showLabel }?.takeIf { it > 0 }?.let { NotificationBadge(count = it, style = style.badgeStyle) }
        }
        when (position) {
            // Badge, label, icon: packed to the end, the icon lands on the row's true trailing edge.
            AppRowPosition.RIGHT -> {
                badge()
                label()
                icon()
            }
            AppRowPosition.CENTER -> CenteredWithBadge(
                modifier = Modifier.weight(1f),
                badge = badge,
                content = {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        icon()
                        label()
                    }
                },
            )
            AppRowPosition.LEFT, AppRowPosition.LAUNCHER_DEFAULT -> {
                icon()
                label()
                badge()
            }
        }
    }
}

/** [iconBadgeCount] is only set when there is no label to trail the badge, so it sits on the icon's corner like a grid tile's. */
@Composable
private fun AppRowIcon(app: AppInfo, size: Dp, iconBadgeCount: Int?, showLabel: Boolean, style: HomeItemStyle, testTagPrefix: String) {
    AppIcon(
        app = app,
        size = size,
        // With no visible label the icon carries the accessible name, as the Dock's icons-only mode does.
        contentDescription = if (showLabel) null else app.label,
        notificationCount = iconBadgeCount,
        badgeStyle = style.badgeStyle,
        modifier = Modifier.testTag(homeTag("${testTagPrefix}app_icon_${app.packageName}")),
    )
}

@Composable
private fun AppRowLabel(app: AppInfo, style: HomeItemStyle, testTagPrefix: String) {
    Text(
        text = app.label,
        style = MaterialTheme.typography.titleMedium.copy(shadow = homeAppLabelShadow(style.labelColor), fontWeight = style.labelFontWeight),
        color = style.labelColor,
        modifier = Modifier.testTag(homeTag("${testTagPrefix}app_label_${app.packageName}")),
    )
}

/** Centers [content] in the available width and puts [badge] just past its trailing edge, so the badge never shifts it. */
@Composable
internal fun CenteredWithBadge(badge: @Composable () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = { content(); badge() }, modifier = modifier) { measurables, constraints ->
        val gap = 14.dp.roundToPx()
        val badgePlaceable = measurables.getOrNull(1)?.measure(constraints.copy(minWidth = 0))
        val badgeSpace = badgePlaceable?.let { it.width + gap } ?: 0
        // Reserve the badge's room on both sides so a long label stays centered and the badge still fits.
        val contentPlaceable = measurables[0].measure(
            constraints.copy(minWidth = 0, maxWidth = (constraints.maxWidth - 2 * badgeSpace).coerceAtLeast(0)),
        )
        val height = maxOf(contentPlaceable.height, badgePlaceable?.height ?: 0)
        layout(constraints.maxWidth, height) {
            val contentX = (constraints.maxWidth - contentPlaceable.width) / 2
            contentPlaceable.placeRelative(contentX, (height - contentPlaceable.height) / 2)
            badgePlaceable?.placeRelative(contentX + contentPlaceable.width + gap, (height - badgePlaceable.height) / 2)
        }
    }
}
